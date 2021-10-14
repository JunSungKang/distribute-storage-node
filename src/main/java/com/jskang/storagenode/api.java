package com.jskang.storagenode;

import com.jskang.storagenode.node.Node;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
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
            .POST("node/join", request -> node.networkJoin(
                request.queryParam("ip").get(), Integer.valueOf(request.queryParam("port").get()))
            )
            .build();
    }
}
