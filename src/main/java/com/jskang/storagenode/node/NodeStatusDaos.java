package com.jskang.storagenode.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;

public class NodeStatusDaos {

    private long version = 0;
    private List<NodeStatusDao> nodeStatusDaos = new LinkedList<>();

    @JsonCreator
    public NodeStatusDaos(
        @JsonProperty("version") long version,
        @JsonProperty("nodeStatusDaos") List<NodeStatusDao> nodeStatusDaos) {
        this.version = version;
        this.nodeStatusDaos = nodeStatusDaos;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Get version max value.
     *
     * @param version Generating version.
     * @return
     */
    public long compareToVersion(int version) {
        return this.version > version ? this.version : version;
    }

    public NodeStatusDao[] getNodeStatusDaos() {
        NodeStatusDao[] result = new NodeStatusDao[this.nodeStatusDaos.size()];
        result = this.nodeStatusDaos.toArray(result);
        return result;
    }

    public void addNodeStatusDao(NodeStatusDao nodeStatusDao) {
        this.nodeStatusDaos.add(nodeStatusDao);
    }

    /**
     * NodeStatusDaos overwrite.
     *
     * @param nodeStatusDaos Input type linkedList.
     */
    public void setNodeStatusDaos(List<NodeStatusDao> nodeStatusDaos) {
        this.nodeStatusDaos = nodeStatusDaos;
    }

    /**
     * NodeStatusDaos overwrite.
     *
     * @param nodeStatusDaos Input type NodeStatusDao Array.
     */
    public void setArrayNodeStatusDaos(NodeStatusDao[] nodeStatusDaos) {
        this.nodeStatusDaos.clear();

        for (int i = 0; i < nodeStatusDaos.length; i++) {
            this.nodeStatusDaos.add(nodeStatusDaos[i]);
        }
    }
}
