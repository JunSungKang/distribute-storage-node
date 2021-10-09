package kr.jskang.storagenode;

import kr.jskang.storagenode.module.Node;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Component
public class api {

    static Node node = new Node();

    @Bean
    public RouterFunction<ServerResponse> router(){
        return RouterFunctions.route()
                .GET("node/status", request -> node.getNodeStatus())
                .build();
    }

    /**
     * 10분(600초)마다 스토리지 네트워크에 합류한 노드 상태 정보 갱신
     */
    @Scheduled(fixedRate = 600000)
    public void nodeReload(){
        node.reloadNodeList();
    }
}
