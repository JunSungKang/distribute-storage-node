package com.jskang.storagenode.file;

import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.SystemInfo;
import com.jskang.storagenode.node.NodeStatusDao;
import com.jskang.storagenode.node.NodeStatusDaos;
import com.jskang.storagenode.response.ResponseResult;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class Download {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 파일 다운로드 요청시 파일 다운로드 기능
     *
     * @return 다운로드에 성공하면 다운로드할 파일을 반환, 실패할 경우 400(BAD_REQUEST) 반환
     */
    public Mono<ServerResponse> fileDownload(ServerRequest request) {
        String fileName = Converter.getQueryParam(request, "fileName");
        if (fileName.isBlank()) {
            LOG.error("request query 'fileName' empty.");
            return ResponseResult.fail(HttpStatus.BAD_REQUEST);
        }

        LOG.info("File '" + fileName + "' download start.");

        // 기본 값은 현재 경로의 upload 디렉토리
        String homePath = "upload";
        String hostName = new SystemInfo().getHostName();
        for (NodeStatusDao nodeStatusDao : NodeStatusDaos.getNodeStatusDaos()) {
            if (nodeStatusDao.getHostName().equals(hostName)) {
                homePath = nodeStatusDao.getHomePath();
                break;
            }
        }
        Resource resource = new FileSystemResource(homePath + File.separator + fileName);
        Mono<Resource> mapper = Mono.just(resource);

        return ResponseResult.download(mapper, fileName);
    }

}
