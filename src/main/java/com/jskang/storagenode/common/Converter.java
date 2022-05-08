package com.jskang.storagenode.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jskang.storagenode.common.exception.DataSizeRangeException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.web.reactive.function.server.ServerRequest;
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
            return mapper.readValue(json, typeReference);
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

    public static byte[] converterSHA256(String data) throws NoSuchAlgorithmException {
        MessageDigest hash = MessageDigest.getInstance(CommonValue.HASH_ALGORITHM_SHA256);
        hash.update(data.getBytes(StandardCharsets.UTF_8));
        return hash.digest();
    }

    public static byte[] converterSHA256(InputStream is) throws NoSuchAlgorithmException {
        final int BUFFER_SIZE = 1024 * 1024;
        Objects.requireNonNull(is);

        try {
            final byte[] buffer = new byte[BUFFER_SIZE];
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");

            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 hashing algorithm unknown in this VM.", e);
        } catch (IOException e) {
            throw new IllegalStateException("file io fail.", e);
        }
    }

    public static String settingString32Size(String str) {
        int needStrLength = 32 - str.length();
        for (int i=0; i<needStrLength; i++) {
            str += "-";
        }

        return str;
    }

    public static Bytes32 stringToBytes32(String str) throws DataSizeRangeException {
        if (str.length() > 32) {
            throw new DataSizeRangeException(str);
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

    public static String getQueryParam(ServerRequest request, String key) {
        Optional<String> param = request.queryParam(key);
        if (param.isPresent()) {
            return URLDecoder.decode(param.get(), StandardCharsets.UTF_8);
        }
        return "";
    }
}
