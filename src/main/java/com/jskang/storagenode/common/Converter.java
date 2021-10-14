package com.jskang.storagenode.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static Map jsonToMap(String json) {
        try {
            return mapper.readValue(json, new TypeReference<Map>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static List stringArrayToList(String stringArray) {
        try {
            return mapper.readValue(stringArray, new TypeReference<List>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
