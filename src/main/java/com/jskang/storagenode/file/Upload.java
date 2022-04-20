package com.jskang.storagenode.file;

import com.jskang.storagenode.common.CommonValue;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.SystemInfo;
import com.jskang.storagenode.common.exception.DataSizeOutBoundException;
import com.jskang.storagenode.common.exception.DataSizeRangeException;
import com.jskang.storagenode.node.NodeStatusDao;
import com.jskang.storagenode.node.NodeStatusDaos;
import com.jskang.storagenode.response.ResponseResult;
import com.jskang.storagenode.smartcontract.SmartContract;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.web3j.abi.datatypes.generated.Bytes32;
import reactor.core.publisher.Mono;

public class Upload {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private SystemInfo systemInfo = new SystemInfo();
    private static Map<String, List<Bytes32>> fileNamesMap = new HashMap<>();
    private static Map<String, List<Bytes32>> fileHashsMap = new HashMap<>();

    /**
     * 파일 업로드 요청시, 파일 분산 후 업로드 기능
     *
     * @param request 업로드 파일 메타 데이터
     * @return 업로드 성공시 200 (SUCCESS) 반환, 실패시 500(INTERNAL_SERVER_ERROR) 반환.
     */
    public Mono<ServerResponse> fileUpload(ServerRequest request) {
        Optional<String> optionalFileName = request.queryParam("fileName");

        if (optionalFileName.isPresent()) {
            final String fileName = optionalFileName.get();

            return request.body(BodyExtractors.toMultipartData())
                .flatMap(parts -> {
                    Map<String, Part> map = parts.toSingleValueMap();
                    if (map.get("file") instanceof FilePart) {
                        FilePart filePart = (FilePart) map.get("file");
                        FileManage.addPosition(fileName, filePart.filename());
                        Path path = Paths.get(CommonValue.UPLOAD_PATH, filePart.filename());

                        try {
                            // 파일명
                            List<Bytes32> fileNames = fileNamesMap.get(fileName+ "-filename");
                            if (fileNames == null) {
                                fileNames = new ArrayList<>();
                            }
                            fileNames.add(
                                Converter.stringToBytes32(filePart.filename())
                            );
                            fileNamesMap.put(fileName+ "-filename", fileNames);

                            // 파일 해시
                            List<Bytes32> fileHashs = fileHashsMap.get(fileName+ "-filehash");
                            if (fileHashs == null) {
                                fileHashs = new ArrayList<>();
                            }
                            MessageDigest hash = MessageDigest.getInstance(CommonValue.HASH_ALGORITHM_SHA256);
                            hash.update(filePart.filename().getBytes(StandardCharsets.UTF_8));
                            fileHashs.add(
                                new Bytes32(hash.digest())
                            );
                            fileHashsMap.put(fileName+ "-filehash", fileHashs);
                        } catch (NoSuchAlgorithmException e) {
                            LOG.error(CommonValue.HASH_ALGORITHM_SHA256+ " hash change fail.");
                            LOG.debug(e.getMessage());
                        } catch (DataSizeRangeException e) {
                            LOG.error("need filename length size == 32");
                            LOG.debug(e.getMessage());
                        }
                        return filePart
                            .transferTo(path)
                            .doOnError(throwable -> ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR))
                            .doOnSuccess(unused -> ResponseResult.success(""));
                    }
                    return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR);
                })
                .doOnSuccess(o -> {
                    String hostName = this.systemInfo.getHostName();
                    NodeStatusDao nodeStatusDao = new NodeStatusDao(
                        CommonValue.UPLOAD_PATH,
                        hostName,
                        this.systemInfo.getDiskTotalSize() - this.systemInfo.getDiskUseSize()
                    );
                    nodeStatusDao.updateFileManage();

                    NodeStatusDaos nodeStatusDaos = FileManage.readFileManager();
                    nodeStatusDaos.editNodeStatusDaos(hostName, nodeStatusDao);
                    nodeStatusDaos.updateVersion();

                    // FileManage refresh.
                    try {
                        File file = Paths.get("data", "FileManage.fm").toFile();
                        FileOutputStream out = new FileOutputStream(file);

                        String json = Converter.objToJson(NodeStatusDaos.getNodeStatusAlls());
                        out.write(json.getBytes(StandardCharsets.UTF_8));
                        out.close();
                    } catch (FileNotFoundException e) {
                        LOG.error(e.getMessage());
                    } catch (IOException e) {
                        LOG.error(e.getMessage());
                    }
                    LOG.info("file upload success.");
                })
                .doOnError(throwable -> {
                    LOG.error(throwable.getMessage());
                })
                .doFinally(result -> {
                    List<Bytes32> fileNames = fileNamesMap.get(fileName+ "-filename");
                    List<Bytes32> fileHashs = fileHashsMap.get(fileName+ "-filehash");
                    if (result.toString().equals("onComplete") && fileNames.size() == 9 && fileHashs.size() == 9) {
                        // File smartcontract generate.
                        SmartContract smartContract = new SmartContract();
                        boolean isCheck = smartContract.connection();
                        if (!isCheck) {
                            LOG.error("Smart-Contract connection fail.");
                        } else {
                            byte[] hashValue = null;
                            try {
                                MessageDigest hash = MessageDigest.getInstance(CommonValue.HASH_ALGORITHM_SHA256);
                                hash.update(fileName.getBytes(StandardCharsets.UTF_8));
                                hashValue = hash.digest();
                            } catch (NoSuchAlgorithmException e) {
                                LOG.error(CommonValue.HASH_ALGORITHM_SHA256+ " hash change fail.");
                                LOG.debug(e.getMessage());
                            }

                            smartContract.setFileHashValue(
                                CommonValue.ADMIN_ADDRESS, CommonValue.ADMIN_PASSWORD,
                                new Bytes32(hashValue),
                                fileNames.parallelStream().collect(Collectors.toList()),
                                fileHashs.parallelStream().collect(Collectors.toList())
                            );
                            // 완료된 작업 초기화
                            fileNamesMap.remove(fileName+ "-filename");
                            fileHashsMap.remove(fileName+ "-filehash");
                        }
                    }
                })
                .then(ResponseResult.success(""));
        } else {
            LOG.error("request query 'fileName' empty.");
            return ResponseResult.fail(HttpStatus.BAD_REQUEST);
        }
    }
}
