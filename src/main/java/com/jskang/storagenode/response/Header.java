package com.jskang.storagenode.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("header")
public class Header {

    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;

    public Header(
        @JsonProperty("code") int code,
        @JsonProperty("message") String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Header{" +
            "code=" + code +
            ", message='" + message + '\'' +
            '}';
    }
}
