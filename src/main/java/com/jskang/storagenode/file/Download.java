package com.jskang.storagenode.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class Download {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * File download
     *
     * @return if upload ok 'success', others 'fail'
     */
    public Mono<ServerResponse> fileDownload() {
        LOG.info("File download start.");
        return null;
    }

}
