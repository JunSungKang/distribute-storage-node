package com.jskang.storagenode.file;

import com.jskang.storagenode.response.ResponseResult;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class Download {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * File download
     *
     * @return if upload ok 'success', others 'fail'
     */
    public Mono<ServerResponse> fileDownload(ServerRequest request) {
        Optional<String> optionalFileName = request.queryParam("fileName");

        if (optionalFileName.isPresent()) {
            String fileName = optionalFileName.get();
            LOG.info("File '" + fileName + "' download start.");

            Resource resource = new FileSystemResource("upload\\" + fileName);
            Mono<Resource> mapper = Mono.just(resource);

            return ResponseResult.download(mapper, fileName);
        } else {
            LOG.error("request query 'fileName' empty.");
            return ServerResponse.badRequest().body(
                BodyInserters.fromProducer(Mono.just("{\"statusCode\": 400}"), String.class)
            );
        }
    }

}
