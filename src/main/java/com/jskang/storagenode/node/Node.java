package com.jskang.storagenode.node;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jskang.storagenode.StorageNodeApplication;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.RequestApi;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class Node {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private RequestApi requestApi = new RequestApi();
    private static NodeStatusDaos nodeStatusDaos = new NodeStatusDaos(0, new LinkedList<>());

    /**
     * Generating version
     */
    private long generatingVersion() {
        long version = new Date().getTime();
        return version;
    }

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
     * Current own node disk status information inquiry
     *
     * @return Returns the host name, current disk usage, and total disk size.
     */
    private NodeStatusDao nodeStatus() {
        String hostAddress = "";
        double totalSize = 0;
        double useSize = 0;

        hostAddress = getLocalIpAddress().concat(":" + StorageNodeApplication.getSettingPort());
        File[] drives = File.listRoots();
        totalSize = drives[0].getTotalSpace() / Math.pow(1024, 3);
        useSize = drives[0].getUsableSpace() / Math.pow(1024, 3);

        NodeStatusDao nodeStatusDao = new NodeStatusDao(hostAddress, useSize, totalSize);
        return nodeStatusDao;
    }

    /**
     * Node refresh.
     */
    @Scheduled(fixedDelay = 5000)
    private void nodeRefresh() {
        if (this.nodeStatusDaos.getNodeStatusDaos().length > 1) {
            int random = new Random().nextInt(this.nodeStatusDaos.getNodeStatusDaos().length);
            NodeStatusDao[] nodeStatusDao = this.nodeStatusDaos.getNodeStatusDaos();

            String hostName = nodeStatusDao[random].getHostName();
            Object data = this.requestApi.get(hostName + "/node/list");
            if (data instanceof String && data.equals("connect fail")) {
                this.nodeStatusDaos.setVersion(this.generatingVersion());
                this.nodeStatusDaos.removeNodeStatusDaos(hostName);
                LOG.info("Connect node remove [" +hostName+ "]");
            } else {
                NodeStatusDaos nodeStatusDaos =
                    (NodeStatusDaos) Converter.objToObj(data, new TypeReference<NodeStatusDaos>() {
                    });

                if (this.nodeStatusDaos.getVersion() < nodeStatusDaos.getVersion()) {
                    this.nodeStatusDaos.setVersion(nodeStatusDaos.getVersion());
                    this.nodeStatusDaos.setArrayNodeStatusDaos(nodeStatusDaos.getNodeStatusDaos());
                }
            }
        }
    }

    /**
     * Search the list of all registered nodes.
     *
     * @return all nodes.
     */
    public Mono<ServerResponse> getNodeLists() {
        return ok().bodyValue(this.nodeStatusDaos);
    }

    /**
     * Current own node disk status information inquiry
     *
     * @return Returns the host name, current disk usage, and total disk size.
     */
    public Mono<ServerResponse> getNodeStatus() {
        LOG.info("Select node information.");
        NodeStatusDao nodeStatusDao = this.nodeStatus();
        if (nodeStatusDao == null) {
            return ok().bodyValue(new ArrayList<>());
        } else {
            return ok().bodyValue(nodeStatusDao);
        }
    }

    /**
     * Node join request
     *
     * @throws Exception
     */
    public void networkJoinRequest() throws Exception {
        String localIp = this.getLocalIpAddress();

        String url = "192.168.55.23:"
            .concat("20040/node/join?ip=" + localIp)
            .concat("&port=" + StorageNodeApplication.getSettingPort());

        Map<String, Object> data = new HashMap<>();
        data.put("nodeStatus", this.nodeStatus());

        Object result = this.requestApi.post(url, null, data);
        NodeStatusDaos nodeStatusDaos = (NodeStatusDaos) Converter
            .objToObj(result, new TypeReference<NodeStatusDaos>() {
            });

        this.nodeStatusDaos.setVersion(nodeStatusDaos.getVersion());
        this.nodeStatusDaos.setArrayNodeStatusDaos(nodeStatusDaos.getNodeStatusDaos());
    }

    /**
     * Add the node's network participation list
     *
     * @param request restAPI post data.
     * @return if success, node status info. other case exception message.
     */
    public Mono<ServerResponse> networkJoin(ServerRequest request) {
        try {
            return request.bodyToMono(String.class)
                .flatMap(str -> {
                    Map<String, Map<String, Object>> data = Converter.jsonToMap(str);
                    NodeStatusDao nodeStatusDao = (NodeStatusDao) Converter
                        .objToObj(data.get("nodeStatus"), new TypeReference<NodeStatusDao>() {
                        });
                    this.nodeStatusDaos.setVersion(this.generatingVersion());
                    this.nodeStatusDaos.addNodeStatusDao(nodeStatusDao);

                    // init seed node
                    if (
                        !this.nodeStatusDaos
                            .nodeSearch(this.nodeStatus().getHostName())
                            .isPresent()
                    ) {
                        this.nodeStatusDaos.addNodeStatusDao(this.nodeStatus());
                    }

                    return ok().bodyValue(this.nodeStatusDaos);
                });
        } catch (Exception e) {
            return ServerResponse.badRequest().bodyValue(e.getMessage());
        }
    }
}
