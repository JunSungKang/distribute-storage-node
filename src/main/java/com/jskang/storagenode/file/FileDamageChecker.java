package com.jskang.storagenode.file;

import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.exception.DataSizeRangeException;
import com.jskang.storagenode.response.ResponseResult;
import com.jskang.storagenode.smartcontract.SmartContract;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.web3j.abi.datatypes.generated.Bytes32;
import reactor.core.publisher.Mono;

public class FileDamageChecker {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 파일이 손상되었는지 체크
     *
     * @return if damage file {fileIdx}, safe file {empty}, the other exception {null}.
     */
    public Mono<ServerResponse> damageCheck(ServerRequest request, String key) {
        String fileName = Converter.getQueryParam(request, key);

        List<Integer> damageFileIdx = new ArrayList<>();
        SmartContract contract = new SmartContract();
        if (!contract.connection()) {
            LOG.error("Smart-Contract connection fail.");
            return ResponseResult.fail(HttpStatus.BAD_REQUEST, "Smart-Contract connection fail.");
        }

        try {
            Bytes32 bytes32 = new Bytes32(Converter.converterSHA256(fileName));

            // 서버에서 가지고 있는 실제 파일 해시
            List<String> realFiles = new LinkedList<>();
            for (String file : FileManage.getFilePosition(fileName)) {
                try (InputStream is = Files.newInputStream(Paths.get(file))) {
                    byte[] fileHash = Converter.converterSHA256(is);
                    realFiles.add(Converter.bytes32ToString(fileHash));
                }
            }

            // 스마트컨트랙트에 등록된 파일 해시
            List<String> contractFiles = contract.getFileHash(bytes32);
            for (int i = 0; i < realFiles.size(); i++) {
                String realFileHash = realFiles.get(i);
                String contractFileHash = contractFiles.get(i+2);
                if (
                    realFileHash == null || contractFileHash == null || !contractFileHash.equals(realFileHash)
                ) {
                    // 손상된 파일
                    damageFileIdx.add(i);
                }
            }
        } catch (IOException e) {
            LOG.error("File metadata read fail.");
            LOG.debug(e.getMessage());
            return ResponseResult.fail(HttpStatus.NOT_FOUND, "File metadata read fail.");
        } catch (DataSizeRangeException e) {
            LOG.error("Smart-Contract get filehash fail.");
            LOG.debug(e.getMessage());
            return ResponseResult.fail(HttpStatus.BAD_REQUEST, "Smart-Contract get filehash fail.");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("FileName sha-256 converte fail.");
            LOG.debug(e.getMessage());
            return ResponseResult.fail(HttpStatus.NOT_FOUND, "FileName sha-256 converte fail.");
        }

        return ResponseResult.success(damageFileIdx);
    }
}
