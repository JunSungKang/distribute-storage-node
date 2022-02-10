package com.jskang.storagenode.common;

import com.jskang.storagenode.StorageNodeApplication;
import java.io.File;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemInfo {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private String localIpAddress = this.setLocalIpAddress();
    private int port = StorageNodeApplication.getSettingPort();

    public String getHostName() {
        return this.getLocalIpAddress() + ":" + this.getPort();
    }

    /**
     * 노드 자기 자신의 아이피 주소 확인
     *
     * @return 노드 아이피
     */
    public String getLocalIpAddress() {
        if (this.localIpAddress.isBlank()) {
            this.localIpAddress = this.setLocalIpAddress();
        }
        return this.localIpAddress;
    }

    /**
     * 현재 사용중인 포트 확인
     *
     * @return 노드 포트
     */
    public int getPort() {
        return StorageNodeApplication.getSettingPort();
    }

    /**
     * 서버의 아이피 주소 목록을 불러온 후, 아이피 주소 확인
     *
     * @return 외부에서 접속가능한 아이피 주소를 반환
     */
    private String setLocalIpAddress() {
        try {
            LOG.info("Select the hostname or ip address.");

            // 자신의 IP 출력
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException | SocketException ex) {
            LOG.error("Select the hostname or ip address failed.");
            return "";
        }
    }

    /**
     * 디스크 전체 용량 확인
     *
     * @return 디스크 전체 용량
     */
    public double getDiskTotalSize() {
        File[] drives = File.listRoots();
        return drives[0].getTotalSpace() / Math.pow(1024, 3);
    }

    /**
     * 디스크 현재 사용중인 용량 확인
     *
     * @return 사용중인 디스크 용량
     */
    public double getDiskUseSize() {
        File[] drives = File.listRoots();
        return drives[0].getUsableSpace() / Math.pow(1024, 3);
    }
}
