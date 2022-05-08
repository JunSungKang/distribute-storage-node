package com.jskang.storagenode.file;


import com.jskang.storagenode.common.exception.DataSizeRangeException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.web3j.abi.datatypes.generated.Bytes32;

class FileDamageCheckerTest {

    @Test
    void stringToBytes32() throws DataSizeRangeException {
        String str = "sample.mp4";
        if (str.length() > 32) {
            throw new DataSizeRangeException();
        }

        byte[] bytes = new byte[32];
        byte[] byteStr = str.getBytes(StandardCharsets.UTF_8);
        for (int i=0; i<32; i++) {
            if (i < byteStr.length) {
                bytes[i] = byteStr[i];
            } else {
                bytes[i] = 0x00;
            }
        }

        Bytes32 bytes32 = new Bytes32(bytes);

        byte[] success = {115,97,109,112,108,101,46,109,112,52,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        System.out.println(new String(bytes32.getValue())+ " == " +new String(success));
        Assertions.assertEquals(new String(bytes32.getValue()), new String(success));
    }

    @Test
    void bytes32ToString() {
        byte[] bytes = {115,97,109,112,108,101,46,109,112,52,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        Bytes32 bytes32 = new Bytes32(bytes);

        System.out.println(new String(bytes)+ " == " +new String(bytes32.getValue()));
        Assertions.assertEquals(new String(bytes), new String(bytes32.getValue()));
    }

    @Test
    void urlDecoder() {
        String fileName = "%EA%B0%95%EC%A4%80%EC%84%B1-stay.mp3";
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        Assertions.assertEquals(fileName, "강준성-stay.mp3");
    }
}
