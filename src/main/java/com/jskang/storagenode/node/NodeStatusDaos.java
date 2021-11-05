package com.jskang.storagenode.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NodeStatusDaos {

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
     * Generating version
     */
    public static void updateVersion() {
        version = new Date().getTime();
    }

    /**
     * Get version max value.
     *
     * @param version Generating version.
     * @return
     */
    public static long compareToVersion(int version) {
        return version > version ? version : version;
    }

    public static NodeStatusDao[] getNodeStatusDaos() {
        NodeStatusDao[] result = new NodeStatusDao[nodeStatusDaos.size()];
        result = nodeStatusDaos.toArray(result);
        return result;
    }

    public static Map<String, Object> getNodeStatusAlls() {
        Map<String, Object> result = new HashMap<>();
        result.put("version", version);
        result.put("nodeStatusDaos", nodeStatusDaos);
        return result;
    }

    public static void addNodeStatusDao(NodeStatusDao nodeStatusDao) {
        nodeStatusDaos.add(nodeStatusDao);
    }

    /**
     * NodeStatusDaos overwrite.
     *
     * @param nodeStatusDaos Input type linkedList.
     */
    public static void setNodeStatusDaos(List<NodeStatusDao> nodeStatusDaos) {
        nodeStatusDaos = nodeStatusDaos;
    }

    /**
     * NodeStatusDaos overwrite.
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
     * Connect node remove.
     *
     * @param hostName remove is hostname.
     */
    public static void removeNodeStatusDaos(String hostName) {
        Optional<NodeStatusDao> nodeStatusDao = nodeSearch(hostName);
        if (nodeStatusDao.isPresent()) {
            nodeStatusDaos.remove(nodeStatusDao.get());
        }
    }

    /**
     * Update node status.
     *
     * @param hostName      input hostname.
     * @param nodeStatusDao nodeStatusDao.
     * @return Success if idx value greater than -1, failure otherwise.
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
     * is node check.
     *
     * @param hostName search hostname.
     * @return if true search count > 1, others search count = 0.
     */
    public static Optional<NodeStatusDao> nodeSearch(String hostName) {
        return nodeStatusDaos.stream()
            .filter(nodeStatusDao -> nodeStatusDao.getHostName().equals(hostName))
            .findFirst();
    }
}
