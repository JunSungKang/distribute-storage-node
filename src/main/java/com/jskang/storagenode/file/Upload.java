package com.jskang.storagenode.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        LOG.info("File upload start.");
        return null;
    }
}
