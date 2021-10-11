package com.jskang.storagenode.module;

import com.jskang.storagenode.model.NodeStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Node {

    List<NodeStatus> nodeStatuses = new ArrayList<>();

    /**
     * 서버의 로컬 IP Address 조회
     * @return IP Address
     */
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            return "";
        }
        return "";
    }

    /**
     * 현재 자기 자신의 노드 디스크 상태 정보 조회
     * @return 호스트명, 디스크 현재 사용량, 디스크 전체 사이즈 반환
     */
    public Mono<ServerResponse> getNodeStatus() {
        String hostAddress = "";
        double totalSize = 0;
        double useSize = 0;

        hostAddress = getLocalIpAddress();
        File[] drives = File.listRoots();
        totalSize = drives[0].getTotalSpace() / Math.pow(1024, 3);
        useSize = drives[0].getUsableSpace() / Math.pow(1024, 3);

        NodeStatus nodeStatus = new NodeStatus(hostAddress, useSize, totalSize);
        return ServerResponse.ok().bodyValue(nodeStatus);

    }

    /**
     * 스토리지 네트워크에 합류된 모든 노드 정보를 최신화
     */
    public void reloadNodeList() {
        System.out.println("노드 갱신");
        nodeStatuses = nodeStatuses.stream()
                .map(nodeStatus -> {
                    //TODO: 모든 호스트 시스템 상태 정보 갱신 (RestAPI 요청)
                    nodeStatus.setUseSize(new Random().nextDouble());
                    nodeStatus.setTotalSize(new Random().nextDouble());
                    return nodeStatus;
                })
                .collect(Collectors.toList());
    }
}
