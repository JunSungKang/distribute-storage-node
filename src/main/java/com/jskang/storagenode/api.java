package com.jskang.storagenode;

import com.jskang.storagenode.file.Download;
import com.jskang.storagenode.file.FileManage;
import com.jskang.storagenode.file.Upload;
import com.jskang.storagenode.node.Node;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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
            .GET("file/position_info",
                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                request -> {
                    Optional<String> optionalFileName = request.queryParam("fileName");
                    if (optionalFileName.isPresent()) {
                        String fileName = optionalFileName.get();
                        List<String> positions = (List<String>) FileManage
                            .getFilePostionStream(fileName)
                            .map(o -> ((Path) o).getFileName().toString())
                            .collect(Collectors.toList());

                        return ServerResponse.ok()
                            .body(BodyInserters
                                .fromProducer(Mono.just(positions), List.class));
                    } else {
                        return ServerResponse.badRequest().body(
                            BodyInserters
                                .fromProducer(Mono.just("{\"statusCode\": 400}"), String.class)
                        );
                    }
                })
            .GET("file/download",
                RequestPredicates.accept(MediaType.APPLICATION_JSON),
                request -> new Download().fileDownload(request))
            .build();
    }
}
