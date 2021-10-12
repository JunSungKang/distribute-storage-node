package com.jskang.storagenode.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeStatusDao {

    private String hostName;
    private double useSize;
    private double totalSize;

    @JsonCreator
    public NodeStatusDao(
        @JsonProperty("hostAddress") String hostName,
        @JsonProperty("useSize") double useSize,
        @JsonProperty("totalSize") double totalSize) {
        this.hostName = hostName;
        this.useSize = useSize;
        this.totalSize = totalSize;
    }

    public String getHostName() {
        return hostName;
    }

    public double getUseSize() {
        return useSize;
    }

    public double getTotalSize() {
        return totalSize;
    }

    public void setUseSize(double useSize) {
        this.useSize = useSize;
    }

    public void setTotalSize(double totalSize) {
        this.totalSize = totalSize;
    }

    public boolean isHostName(String hostName) {
        return this.hostName.equals(hostName);
    }
}
