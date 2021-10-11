package com.jskang.storagenode.node;

public class NodeStatusDao {

    private String hostAddress;
    private double useSize;
    private double totalSize;

    public NodeStatusDao(String hostAddress, double useSize, double totalSize) {
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
