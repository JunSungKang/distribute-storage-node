package com.jskang.storagenode.file;

import com.jskang.storagenode.common.SystemInfo;
import com.jskang.storagenode.node.NodeStatusDao;
import com.jskang.storagenode.node.NodeStatusDaos;
import com.jskang.storagenode.response.ResponseResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class Upload {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private SystemInfo systemInfo = new SystemInfo();

    /**
     * File upload
     *
     * @param request upload file metadata
     * @return if upload ok 'success', others 'fail'
     */
    public Mono<ServerResponse> fileUpload(ServerRequest request) {
        Optional<String> optionalFileName = request.queryParam("fileName");

        if (optionalFileName.isPresent()) {
            final String fileName = optionalFileName.get();

            return request.body(BodyExtractors.toMultipartData())
                .flatMap(parts -> {
                    Map<String, Part> map = parts.toSingleValueMap();
                    if (map.get("file") instanceof FilePart) {
                        FilePart filePart = (FilePart) map.get("file");
                        Path path = Paths.get("upload\\" + filePart.filename());
                        FileManage.addPosition(fileName, path);
                        return filePart
                            .transferTo(path)
                            .doOnError(
                                throwable -> ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR))
                            .doOnSuccess(unused -> ResponseResult.success(""));
                    }
                    return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR);
                })
                .doOnSuccess(o -> {
                    String hostName = this.systemInfo.getHostName();
                    NodeStatusDao nodeStatusDao = new NodeStatusDao(
                        hostName,
                        this.systemInfo.getDiskTotalSize() - this.systemInfo.getDiskUseSize()
                    );
                    nodeStatusDao.updateFileManage();

                    NodeStatusDaos.editNodeStatusDaos(hostName, nodeStatusDao);
                    NodeStatusDaos.updateVersion();
                    LOG.info("file upload success.");
                })
                .doOnError(throwable -> {
                    LOG.error(throwable.getMessage());
                })
                .then(ResponseResult.success(""));
        } else {
            LOG.error("request query 'fileName' empty.");
            return ResponseResult.fail(HttpStatus.BAD_REQUEST);
        }
    }
}
