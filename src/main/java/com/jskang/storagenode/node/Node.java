package com.jskang.storagenode.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.RequestApi;
import com.jskang.storagenode.common.SystemInfo;
import com.jskang.storagenode.response.ResponseResult;
import com.jskang.storagenode.response.ResponseResult.ResponseData;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class Node {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private RequestApi requestApi = new RequestApi();
    private SystemInfo systemInfo = new SystemInfo();

    /**
     * 주변 노드에 노드 정보를 요청하여 가장 최신화된 노드의 정보로 갱신
     */
    @Scheduled(fixedDelay = 5000)
    private void nodeRefresh() {
        if (NodeStatusDaos.getNodeStatusDaos().length > 1) {
            boolean isChange = false;
            int random = new Random().nextInt(NodeStatusDaos.getNodeStatusDaos().length);
            NodeStatusDao[] nodeStatusDao = NodeStatusDaos.getNodeStatusDaos();

            String hostName = nodeStatusDao[random].getHostName();
            ResponseData data = (ResponseData) Converter.objToObj(
                this.requestApi.get(hostName + "/node/list"), new TypeReference<ResponseData>() {
                });

            // If there is no response.
            if (data.getBody() instanceof String && data.equals("connect fail")) {
                isChange = true;
                NodeStatusDaos.updateVersion();
                NodeStatusDaos.removeNodeStatusDaos(hostName);
                LOG.info("Connect node remove [" + hostName + "]");
            }
            // if there is a response.
            else {
                NodeStatusDaos nodeStatusDaos =
                    (NodeStatusDaos) Converter
                        .objToObj(data.getBody(), new TypeReference<NodeStatusDaos>() {
                        });

                // If you have a higher version than yourself.
                if (NodeStatusDaos.getVersion() < nodeStatusDaos.getVersion()) {
                    isChange = true;
                    NodeStatusDaos.setVersion(nodeStatusDaos.getVersion());
                    NodeStatusDaos.setArrayNodeStatusDaos(nodeStatusDaos.getNodeStatusDaos());
                }
            }

            // When node information is changed.
            if (isChange) {
                try {
                    File file = Paths.get("data", "FileManage.fm").toFile();
                    file.mkdirs();
                    FileOutputStream out = new FileOutputStream(file);

                    String json = Converter.objToJson(NodeStatusDaos.getNodeStatusAlls());
                    out.write(json.getBytes(StandardCharsets.UTF_8));
                    out.close();
                } catch (FileNotFoundException e) {
                    LOG.error(e.getMessage());
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }

    /**
     * 등록된 모든 노드의 목록을 검색
     *
     * @return 등록된 노드 목록
     */
    public Mono<ServerResponse> getNodeLists() {
        return ResponseResult.success(NodeStatusDaos.getNodeStatusAlls());
    }

    /**
     * 현재 자체 노드 디스크 상태 정보 조회
     *
     * @return 호스트명, 현재 디스크 남은 용량, 전체 디스크 용량을 반환
     */
    public Mono<ServerResponse> getNodeStatus() {
        LOG.info("Select node information.");
        NodeStatusDao nodeStatusDao = new NodeStatusDao(
            this.systemInfo.getHostName(),
            this.systemInfo.getDiskTotalSize() - this.systemInfo.getDiskUseSize()
        );
        nodeStatusDao.updateFileManage();

        if (nodeStatusDao == null) {
            return ResponseResult.fail(HttpStatus.NOT_FOUND);
        } else {
            return ResponseResult.success(nodeStatusDao);
        }
    }

    /**
     * 새로운 클러스터에 노드 가입 요청
     *
     * @throws Exception
     */
    public void networkJoinRequest() throws Exception {
        String localIp = this.systemInfo.getLocalIpAddress();

        String url = "192.168.55.23:"
            .concat("20040/node/join?ip=" + localIp)
            .concat("&port=" + this.systemInfo.getPort());

        NodeStatusDao nodeStatusDao = new NodeStatusDao(
            this.systemInfo.getHostName(),
            this.systemInfo.getDiskTotalSize() - this.systemInfo.getDiskUseSize()
        );
        nodeStatusDao.updateFileManage();

        Map<String, Object> data = new HashMap<>();
        data.put("nodeStatus", nodeStatusDao);

        ResponseData result = (ResponseData) Converter.objToObj(
            this.requestApi.post(url, null, data), new TypeReference<ResponseData>() {
            });

        NodeStatusDaos nodeStatusDaos = (NodeStatusDaos) Converter
            .objToObj(result.getBody(), new TypeReference<NodeStatusDaos>() {
            });

        NodeStatusDaos.setVersion(nodeStatusDaos.getVersion());
        NodeStatusDaos.setArrayNodeStatusDaos(nodeStatusDaos.getNodeStatusDaos());
    }

    /**
     * 현재 클러스터에 가입 요청한 노드를 추가 
     *
     * @param request 노드 가입 요청에 필요한 메타데이터
     * @return 노드가 가입에 성공하면 가입된 모든 노드 상태 정보, 가입에 실패하면 익셉션 메세지를 반환
     */
    public Mono<ServerResponse> networkJoin(ServerRequest request) {
        try {
            return request.bodyToMono(String.class)
                .flatMap(str -> {
                    Map<String, Map<String, Object>> data = Converter.jsonToMap(str);
                    NodeStatusDao nodeStatusDao = (NodeStatusDao) Converter
                        .objToObj(data.get("nodeStatus"), new TypeReference<NodeStatusDao>() {
                        });
                    NodeStatusDaos.updateVersion();
                    NodeStatusDaos.addNodeStatusDao(nodeStatusDao);

                    return ResponseResult.success(NodeStatusDaos.getNodeStatusAlls());
                });
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return ResponseResult.fail(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
