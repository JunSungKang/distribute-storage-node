package com.jskang.storagenode.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
}
