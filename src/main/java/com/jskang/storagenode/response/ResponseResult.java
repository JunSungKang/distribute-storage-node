package com.jskang.storagenode.response;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class ResponseResult {

    public static Mono<ServerResponse> success(Object data) {
        Header header = new Header(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase());
        return ok().bodyValue(new ResponseData(header, data));
    }

    public static Mono<ServerResponse> fail(HttpStatus httpStatus) {
        Header header = new Header(httpStatus.value(), httpStatus.getReasonPhrase());
        return ok().bodyValue(new ResponseData(header, ""));
    }

    public static Mono<ServerResponse> download(Mono<Resource> resourceMono, String fileName) {
        return ok().header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + fileName + "\"")
            .body(BodyInserters.fromProducer(resourceMono, Resource.class));
    }

    @JsonRootName("response")
    public static class ResponseData {

        @JsonProperty("header")
        private Header header;
        @JsonProperty("body")
        private Object body;

        private ResponseData(
            @JsonProperty("header") Header header,
            @JsonProperty("body") Object body) {
            this.header = header;
            this.body = body;
        }

        public Header getHeader() {
            return header;
        }

        public Object getBody() {
            return body;
        }

        @Override
        public String toString() {
            return "ResponseData{" +
                "header=" + header +
                ", body=" + body +
                '}';
        }
    }

}
