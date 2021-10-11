package com.jskang.storagenode.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

public class Converter {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String mapToJson(Map map) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static Map jsonToMap(String json) {
        return mapper.convertValue(json, new TypeReference<Map>() {
        });
    }

    public static String listToStringArray(List list) {
        return mapper.convertValue(list, new TypeReference<String>() {
        });
    }

    public static List stringArrayToList(String stringArray) {
        return mapper.convertValue(stringArray, new TypeReference<List>() {
        });
    }
}
