package com.jskang.storagenode.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeStatusDao {

    private String hostAddress;
    private double useSize;
    private double totalSize;

    @JsonCreator
    public NodeStatusDao(
        @JsonProperty("hostAddress") String hostAddress,
        @JsonProperty("useSize") double useSize,
        @JsonProperty("totalSize") double totalSize) {
        this.hostAddress = hostAddress;
        this.useSize = useSize;
        this.totalSize = totalSize;
    }

    public String gethostAddress() {
        return hostAddress;
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

    public boolean equalshostAddress(String hostAddress) {
        return this.hostAddress.equals(hostAddress);
    }
}
