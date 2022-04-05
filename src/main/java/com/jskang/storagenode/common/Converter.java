package com.jskang.storagenode.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jskang.storagenode.common.exception.DataSizeRangeException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.web3j.abi.datatypes.generated.Bytes32;

public class Converter {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String objToJson(Object map) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static Object jsonToObj(String json, TypeReference typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static Object objToObj(Object json, TypeReference typeReference) {
        return mapper.convertValue(json, typeReference);
    }

    public static Object fileToObj(File json, TypeReference typeReference) {
        try {
            return mapper.readValue(json, new TypeReference<Map>() {});
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static Map jsonToMap(String json) {
        try {
            return mapper.readValue(json, new TypeReference<Map>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static List jsonToList(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static Bytes32 stringToBytes32(String str) throws DataSizeRangeException {
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

        return new Bytes32(bytes);
    }

    public static String bytes32ToString(byte[] bytes) throws DataSizeRangeException {
        final int dataSize = bytes.length;
        if (dataSize != 32) {
            throw new DataSizeRangeException();
        }
        Bytes32 bytes32 = new Bytes32(bytes);

        return new String(bytes32.getValue());
    }
}
