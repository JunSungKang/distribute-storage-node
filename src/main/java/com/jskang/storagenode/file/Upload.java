package com.jskang.storagenode.file;

import com.jskang.storagenode.common.CommonValue;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.SystemInfo;
import com.jskang.storagenode.node.NodeStatusDao;
import com.jskang.storagenode.node.NodeStatusDaos;
import com.jskang.storagenode.response.ResponseResult;
import com.jskang.storagenode.smartcontract.SmartContract;
import java.io.File;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

            List<Bytes32> fileNames = new ArrayList<>();
            List<Bytes32> fileHashs = new ArrayList<>();
            return request.body(BodyExtractors.toMultipartData())
                .flatMap(parts -> {
                    Map<String, Part> map = parts.toSingleValueMap();
                    if (map.get("file") instanceof FilePart) {
                        FilePart filePart = (FilePart) map.get("file");
                        FileManage.addPosition(fileName, filePart.filename());
                        Path path = Paths.get(CommonValue.UPLOAD_PATH, filePart.filename());

                        try {
                            // 이더리움에 파일 변조체크를 위한 해시 변환
                            fileNames.add(
                                new Bytes32(filePart.filename().getBytes(StandardCharsets.UTF_8))
                            );

                            MessageDigest hash = MessageDigest.getInstance("MD5", filePart.filename());
                            hash.update(filePart.filename().getBytes(StandardCharsets.UTF_8));
                            fileHashs.add(
                                new Bytes32(hash.digest())
                            );
                        } catch (NoSuchProviderException e) {
                            LOG.error("MD5 hash change fail.");
                            LOG.debug(e.getMessage());
                        } catch (NoSuchAlgorithmException e) {
                            LOG.error("MD5 hash change fail.");
                            LOG.debug(e.getMessage());
                        }
                        return filePart
                            .transferTo(path)
                            .doOnError(
                                throwable -> ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR))
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
                    // File smartcontract generate.
                    SmartContract smartContract = new SmartContract();
                    smartContract.connection();
                    smartContract.setFileHashValue(
                        "0xc1dC7f8561921729A42eB8481864BA939dED5198", "1234",
                        new Bytes32(new byte[]{}), fileNames, fileHashs);
                })
                .then(ResponseResult.success(""));
        } else {
            LOG.error("request query 'fileName' empty.");
            return ResponseResult.fail(HttpStatus.BAD_REQUEST);
        }
    }
}
