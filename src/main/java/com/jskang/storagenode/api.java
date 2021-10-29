package com.jskang.storagenode;

import com.jskang.storagenode.file.Download;
import com.jskang.storagenode.file.Upload;
import com.jskang.storagenode.node.Node;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Component
public class api {

    static Node node = new Node();

    @Bean
    public RouterFunction<ServerResponse> router() {
        return RouterFunctions.route()
            .GET("node/list", request -> node.getNodeLists())
            .GET("node/status", request -> node.getNodeStatus())
            .POST("node/join", request -> node.networkJoin(request))
            .POST("file/upload",
                RequestPredicates.accept(MediaType.MULTIPART_FORM_DATA),
                request -> new Upload().fileUpload(request))
            .POST("file/download",
                RequestPredicates.accept(MediaType.TEXT_PLAIN),
                request -> new Download().fileDownload())
            .build();
    }
}
