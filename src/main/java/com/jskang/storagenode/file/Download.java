package com.jskang.storagenode.file;

import com.jskang.storagenode.response.ResponseResult;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class Download {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 파일 다운로드 요청시 파일 다운로드 기능
     *
     * @return 다운로드에 성공하면 다운로드할 파일을 반환, 실패할 경우 400(BAD_REQUEST) 반환
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
            return ResponseResult.fail(HttpStatus.BAD_REQUEST);
        }
    }

}
