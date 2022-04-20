package com.jskang.storagenode.file;

import com.jskang.storagenode.common.CommonValue;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.exception.DataSizeRangeException;
import com.jskang.storagenode.response.ResponseResult;
import com.jskang.storagenode.smartcontract.SmartContract;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
        String fileName = Converter.getQueryParam(request, "fileName");

        List<Integer> damageFileIdx = new ArrayList<>();
        SmartContract contract = new SmartContract();

        try {
            Bytes32 bytes32 = new Bytes32(converterSHA256(fileName));

            // 서버에서 가지고 있는 실제 파일 해시
            List<String> realFiles = new LinkedList<>();
            for (String file : FileManage.getFilePosition(fileName)) {
                byte[] bytes = converterSHA256(file);
                realFiles.add(Converter.bytes32ToString(bytes));
            }

            // 스마트컨트랙트에 등록된 파일 해시
            List<String> contractFiles = contract.getFileHash(bytes32);
            for (int i = 0; i < contractFiles.size(); i++) {
                String realFileHash = realFiles.get(i);
                String contractFileHash = contractFiles.get(i);
                if (!contractFileHash.equals(realFileHash)) {
                    // 손상된 파일
                    damageFileIdx.add(i);
                }
            }
        } catch (IOException e) {
            LOG.error("Smart-Contract get filehash fail.");
            LOG.debug(e.getMessage());
            return ResponseResult.fail(HttpStatus.NOT_FOUND, "Smart-Contract get filehash fail.");
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

    private byte[] converterSHA256(String data) throws NoSuchAlgorithmException {
        MessageDigest hash = MessageDigest.getInstance(CommonValue.HASH_ALGORITHM_SHA256);
        hash.update(data.getBytes(StandardCharsets.UTF_8));
        return hash.digest();
    }

}
