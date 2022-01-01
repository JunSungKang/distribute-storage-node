package com.jskang.storagenode.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NodeStatusDaos implements Serializable {

    private static long version = 0;
    private static List<NodeStatusDao> nodeStatusDaos = new LinkedList<>();

    @JsonCreator
    public NodeStatusDaos(
        @JsonProperty("version") long version,
        @JsonProperty("nodeStatusDaos") List<NodeStatusDao> nodeStatusDaos) {
        this.version = version;
        this.nodeStatusDaos = nodeStatusDaos;
    }

    public static long getVersion() {
        return version;
    }

    public static void setVersion(long lastedVersion) {
        version = lastedVersion;
    }

    /**
     * 새로운 버전으로 갱신
     */
    public static void updateVersion() {
        version = new Date().getTime();
    }

    /**
     * 새로 받은 버전이 현재 버전보다 최신버전인지 확인
     *
     * @param version 비교할 버전
     * @return 둘 중 가장 최신버전을 반환
     */
    public static long compareToVersion(int version) {
        return version > version ? version : version;
    }

    /**
     * 모든 노드 정보 조회
     *
     * @return 모든 노드 정보를 Array 형태로 반환 (버전 정보는 포함하지 않음)
     */
    public static NodeStatusDao[] getNodeStatusDaos() {
        NodeStatusDao[] result = new NodeStatusDao[nodeStatusDaos.size()];
        result = nodeStatusDaos.toArray(result);
        return result;
    }

    /**
     * 모든 노드 정보를 현재 버전 정보화 함께 조회
     *
     * @return 모든 노드 정보를 버전 정보와 함께 반환
     */
    public static Map<String, Object> getNodeStatusAlls() {
        Map<String, Object> result = new HashMap<>();
        result.put("version", version);
        result.put("nodeStatusDaos", nodeStatusDaos);
        return result;
    }

    /**
     * 노드 추가
     *
     * @param nodeStatusDao 추가할 노드 정보
     */
    public static void addNodeStatusDao(NodeStatusDao nodeStatusDao) {
        nodeStatusDaos.add(nodeStatusDao);
    }

    /**
     * 현재 추가된 모든 노드 정보를 새로운 노드 정보로 덮어쓰기
     * (최신화된 노드 정보를 덮어쓰는데 사용)
     *
     * @param nodeStatusDaos 새로운 노드 정보
     */
    public static void setNodeStatusDaos(List<NodeStatusDao> nodeStatusDaos) {
        nodeStatusDaos = nodeStatusDaos;
    }

    /**
     * 현재 추가된 모든 노드 정보를 새로운 노드 정보로 덮어쓰기
     * (최신화된 노드 정보를 덮어쓰는데 사용)
     *
     * @param arrayNodeStatusDaos Input type NodeStatusDao Array.
     */
    public static void setArrayNodeStatusDaos(NodeStatusDao[] arrayNodeStatusDaos) {
        nodeStatusDaos.clear();

        for (int i = 0; i < arrayNodeStatusDaos.length; i++) {
            nodeStatusDaos.add(arrayNodeStatusDaos[i]);
        }
    }

    /**
     * 노드 정보를 삭제
     *
     * @param hostName 삭제할 노드의 호스트명
     */
    public static void removeNodeStatusDaos(String hostName) {
        Optional<NodeStatusDao> nodeStatusDao = nodeSearch(hostName);
        if (nodeStatusDao.isPresent()) {
            nodeStatusDaos.remove(nodeStatusDao.get());
        }
    }

    /**
     * 특정 노드 상태 정보 갱신
     *
     * @param hostName      갱신할 노드의 호스트명
     * @param nodeStatusDao 노드의 상태 정보
     * @return 상태 정보 갱신에 성공하면 -1보다 큰 값, 실패한 경우 음수 값 반환
     */
    public static int editNodeStatusDaos(String hostName, NodeStatusDao nodeStatusDao) {
        int idx = -1;
        if (nodeStatusDaos.size() < 1) {
            nodeStatusDaos.add(nodeStatusDao);
        } else {
            for (int i = 0; i < nodeStatusDaos.size(); i++) {
                if (nodeStatusDaos.get(i).getHostName().equals(hostName)) {
                    idx = i;
                    break;
                }
            }

            if (idx > -1) {
                nodeStatusDaos.set(idx, nodeStatusDao);
            }
        }
        return idx;
    }

    /**
     * 특정 노드 상태 정보 확인
     *
     * @param hostName 확인할 노드의 호스트명
     * @return 노드가 존재하지 않는 경우 isPresent()를 통해서 false 반환, 존재하는 경우에는 true를 반환
     */
    public static Optional<NodeStatusDao> nodeSearch(String hostName) {
        return nodeStatusDaos.stream()
            .filter(nodeStatusDao -> nodeStatusDao.getHostName().equals(hostName))
            .findFirst();
    }
}
