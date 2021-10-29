package com.jskang.storagenode.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.NodeChangeEvent;

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

    /**
     * Connect node remove.
     * @param hostName remove is hostname.
     */
    public void removeNodeStatusDaos(String hostName){
        Optional<NodeStatusDao> nodeStatusDao = this.nodeSearch(hostName);
        if (nodeStatusDao.isPresent()){
            this.nodeStatusDaos.remove( nodeStatusDao.get() );
        }
    }

    /**
     * is node check.
     * @param hostName search hostname.
     * @return if true search count > 1, others search count = 0.
     */
    public Optional<NodeStatusDao> nodeSearch(String hostName){
        return this.nodeStatusDaos.stream()
            .filter(nodeStatusDao -> nodeStatusDao.getHostName().equals(hostName))
            .findFirst();
    }
}
