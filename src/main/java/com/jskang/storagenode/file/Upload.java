package com.jskang.storagenode.file;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.nio.file.Paths;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class Upload {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * File upload
     *
     * @param request upload file metadata
     * @return if upload ok 'success', others 'fail'
     */
    public Mono<ServerResponse> fileUpload(ServerRequest request) {
        return request.body(BodyExtractors.toMultipartData())
            .flatMap(parts -> {
                Map<String, Part> map = parts.toSingleValueMap();
                if (map.get("file") instanceof FilePart) {
                    FilePart filePart = (FilePart) map.get("file");
                    return filePart.transferTo( Paths.get("upload\\" +filePart.filename()) );
                }
                return null;
            })
            .then(ok().body(
                BodyInserters.fromProducer(Mono.just("{\"statusCode\": 200}"), String.class)));
    }
}
