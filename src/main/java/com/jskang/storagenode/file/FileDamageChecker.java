package com.jskang.storagenode.file;

import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.exception.DataSizeRangeException;
import com.jskang.storagenode.smartcontract.SmartContract;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.generated.Bytes32;

public class FileDamageChecker {
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    public int[] getDamangeFile(List<String> fileHash) {
        return null;
    }

    /**
     * 파일이 손상되었는지 체크
     * @return if damage file {1}, safe file {0}, the other exception {-1}
     */
    public int isDamage(String fileName){
        if (fileName.length() > 32) {
            LOG.error("The size range is incorrect. ( 0 < DataSize < 32)");
            return -1;
        }

        SmartContract contract = new SmartContract();
        Bytes32 bytes32 = new Bytes32(fileName.getBytes(StandardCharsets.UTF_8));
        try {
            List<String> fileInfos = contract.getFileHash(bytes32);
            for(String fileInfo : fileInfos) {
                Bytes32 originalFileName = Converter.stringToBytes32(fileInfo);
                String distributeFileName = new String(originalFileName.getValue());

            }
        } catch (IOException e) {
            LOG.error("Smart-Contract get filehash fail.");
            LOG.debug(e.getMessage());
            return -1;
        } catch (DataSizeRangeException e) {
            LOG.error("Casting string to bytes32 fail.");
            LOG.debug(e.getMessage());
            return -1;
        }

        return 0;
    }

}
