package com.jskang.storagenode.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jskang.storagenode.file.FileManage;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class NodeStatusDao implements Serializable {

    private String hostName;
    private double freeSize;
    private String homePath;
    private Map<String, List<String>> fileManage;

    @JsonCreator
    public NodeStatusDao(
        @JsonProperty("homePath") String homePath,
        @JsonProperty("hostAddress") String hostName,
        @JsonProperty("freeSize") double freeSize) {
        this.homePath = homePath;
        this.hostName = hostName;
        this.freeSize = freeSize;
        this.fileManage = new HashMap<>();
    }

    public void newFileManage() {
        this.fileManage = new HashMap<>();
    }

    public void updateFileManage() {
        this.fileManage = FileManage.getAllFileManage();
    }

    public boolean isHostName(String hostName) {
        return this.hostName.equals(hostName);
    }
}
