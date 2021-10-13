package com.jskang.storagenode.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jskang.storagenode.StorageNodeApplication;
import com.jskang.storagenode.common.RequestApi;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class Module {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private RequestApi requestApi = new RequestApi();
    private static List<NodeStatusDao> nodeStatusDaos = new ArrayList<>();

    /**
     * Server's local IP address lookup
     *
     * @return Returns the IP Address.
     */
    private String getLocalIpAddress() {
        try {
            LOG.info("Select the hostname or ip address.");

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                    enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()
                        && inetAddress
                        .isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            LOG.error("Select the hostname or ip address failed.");
            return "";
        }

        return "";
    }

    /**
     * Search the list of all registered nodes.
     *
     * @return all nodes.
     */
    public Mono<ServerResponse> getNodeLists() {
        return ServerResponse.ok().bodyValue(this.nodeStatusDaos);
    }

    /**
     * Current own node disk status information inquiry
     *
     * @return Returns the host name, current disk usage, and total disk size.
     */
    public Mono<ServerResponse> getNodeStatus() {
        LOG.info("Select node information.");

        String hostAddress = "";
        double totalSize = 0;
        double useSize = 0;

        hostAddress = getLocalIpAddress().concat(":" + StorageNodeApplication.getSettingPort());
        File[] drives = File.listRoots();
        totalSize = drives[0].getTotalSpace() / Math.pow(1024, 3);
        useSize = drives[0].getUsableSpace() / Math.pow(1024, 3);

        NodeStatusDao nodeStatusDao = new NodeStatusDao(hostAddress, useSize, totalSize);
        if (nodeStatusDao == null) {
            return ServerResponse.ok().bodyValue(new ArrayList<>());
        } else {
            return ServerResponse.ok().bodyValue(nodeStatusDao);
        }
    }

    /**
     * Updating information of all nodes joining the storage network
     */
    public void reloadNodeList() {
        LOG.info("Update all node information.");

        nodeStatusDaos = nodeStatusDaos.stream()
            .map(nodeStatusDao -> {
                NodeStatusDao nodeStatus = (NodeStatusDao) requestApi
                    .get("http://".concat(nodeStatusDao.getHostName()).concat("/node/status"));

                nodeStatusDao.setUseSize(nodeStatus.getUseSize());
                nodeStatusDao.setTotalSize(nodeStatus.getTotalSize());
                return nodeStatusDao;
            })
            .collect(Collectors.toList());
    }

    /**
     * Node join request
     *
     * @throws Exception
     */
    public void networkJoinRequest() throws Exception {
        String localIp = this.getLocalIpAddress();

        String url = "http://".concat("192.168.55.23").concat(":").concat("20040")
            .concat("/node/join?ip=")
            .concat(localIp)
            .concat("&port=" + StorageNodeApplication.getSettingPort());
        List<NodeStatusDao> nodeStatusDaos = (List<NodeStatusDao>) this.requestApi
            .post(url, null, null);
        this.nodeStatusDaos.addAll(nodeStatusDaos);
    }

    /**
     * Add the node's network participation list
     *
     * @param ip   IP of the node to be added
     * @param port Port of the node to be added
     * @return if success, node status info. other case exception message.
     */
    public Mono<ServerResponse> networkJoin(String ip, int port) {
        try {
            NodeStatusDao nodeStatusDao = new ObjectMapper()
                .convertValue(this.requestApi.get("http://" + ip + ":" + port + "/node/status"),
                    NodeStatusDao.class);
            this.nodeStatusDaos.add(nodeStatusDao);

            return ServerResponse.ok().bodyValue(this.nodeStatusDaos);
        } catch (Exception e) {
            return ServerResponse.badRequest().bodyValue(e.getMessage());
        }
    }
}
