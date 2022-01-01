package com.jskang.storagenode.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jskang.storagenode.file.FileManage;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeStatusDao implements Serializable {

    private String hostName;
    private double freeSize;
    private Map<String, List<Path>> fileManage;

    @JsonCreator
    public NodeStatusDao(
        @JsonProperty("hostAddress") String hostName,
        @JsonProperty("freeSize") double freeSize) {
        this.hostName = hostName;
        this.freeSize = freeSize;
        this.fileManage = new HashMap<>();
    }

    public String getHostName() {
        return hostName;
    }

    public double getFreeSize() {
        return freeSize;
    }

    public Map<String, List<Path>> getFileManage() {
        return fileManage;
    }

    public void updateFileManage() {
        this.fileManage = FileManage.getAllFileManage();
    }

    public boolean isHostName(String hostName) {
        return this.hostName.equals(hostName);
    }
}
