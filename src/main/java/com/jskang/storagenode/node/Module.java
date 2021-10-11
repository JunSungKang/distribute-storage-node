package com.jskang.storagenode.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jskang.storagenode.common.RequestApi;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class Module {

    private static List<String> seedNodes = new ArrayList<>();
    private List<NodeStatusDao> nodeStatusDaos = new ArrayList<>();

    /**
     * Server's local IP address lookup
     *
     * @return Returns the IP Address.
     */
    private String getLocalIpAddress() {
        try {
            System.out.println("Look up the hostname or IP address.");

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
            System.out.println("Hostname or IP address lookup failed.");
            return "";
        }

        return "";
    }

    public static String[] getSeedNodes() {
        return seedNodes.toArray(String[]::new);
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
        System.out.println("Retrieve node information.");

        String hostAddress = "";
        double totalSize = 0;
        double useSize = 0;

        hostAddress = getLocalIpAddress();
        File[] drives = File.listRoots();
        totalSize = drives[0].getTotalSpace() / Math.pow(1024, 3);
        useSize = drives[0].getUsableSpace() / Math.pow(1024, 3);

        NodeStatusDao nodeStatusDao = new NodeStatusDao(hostAddress, useSize, totalSize);
        return ServerResponse.ok().bodyValue(nodeStatusDao);
    }

    /**
     * Updating information of all nodes joining the storage network
     */
    public void reloadNodeList() {
        System.out.println("Update all node information.");

        nodeStatusDaos = nodeStatusDaos.stream()
            .map(nodeStatusDao -> {
                //TODO: 모든 호스트 시스템 상태 정보 갱신 및 모든 참가 노드 갱신(RestAPI 요청)
                nodeStatusDao.setUseSize(new Random().nextDouble());
                nodeStatusDao.setTotalSize(new Random().nextDouble());
                return nodeStatusDao;
            })
            .collect(Collectors.toList());
    }

    /**
     * Request to connect to the first Seed Node when running
     */
    public static void networkSeedConnect() {
        if (!seedNodes.contains("127.0.0.1:20040")) {
            seedNodes.add("127.0.0.1:20040");
            System.out.println("Registered the first Seed Node.");
        } else {
            System.out.println("The first Seed Node already exists.");
        }
    }

    /**
     * Node join request
     *
     * @throws Exception
     */
    public void networkJoinRequest() throws Exception {
        String localIp = this.getLocalIpAddress();

        String seedNode = Module.getSeedNodes()[0];
        String ip = seedNode.split(":")[0];
        String port = seedNode.split(":")[1];
        // TODO: 자기 자신이 오픈되는 포트로 자동 선택되도록 변경해야함
        String url = "http://".concat(ip).concat(":").concat(port).concat("/node/join?ip=")
            .concat(localIp).concat("&port=20040");
        new RequestApi().post(url, null, null);
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
            RequestApi requestApi = new RequestApi();
            NodeStatusDao nodeStatusDao = new ObjectMapper()
                .convertValue(requestApi.get("http://" + ip + ":" + port + "/node/status"),
                    NodeStatusDao.class);
            this.nodeStatusDaos.add(nodeStatusDao);

            return ServerResponse.ok().bodyValue(this.nodeStatusDaos);
        } catch (Exception e) {
            return ServerResponse.badRequest().bodyValue(e.getMessage());
        }
    }
}
