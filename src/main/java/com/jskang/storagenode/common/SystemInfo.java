package com.jskang.storagenode.common;

import com.jskang.storagenode.StorageNodeApplication;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
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
     * get local ip address.
     *
     * @return setting ip.
     */
    public String getLocalIpAddress() {
        if (this.localIpAddress.isBlank()) {
            this.localIpAddress = this.setLocalIpAddress();
        }
        return this.localIpAddress;
    }

    /**
     * get local port.
     *
     * @return setting port.
     */
    public int getPort() {
        return StorageNodeApplication.getSettingPort();
    }

    /**
     * Server's local IP address lookup
     *
     * @return Returns the IP Address.
     */
    private String setLocalIpAddress() {
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
     * get disk total size.
     *
     * @return disk total size.
     */
    public double getDiskTotalSize() {
        File[] drives = File.listRoots();
        return drives[0].getTotalSpace() / Math.pow(1024, 3);
    }

    /**
     * get disk use size.
     *
     * @return disk use size.
     */
    public double getDiskUseSize() {
        File[] drives = File.listRoots();
        return drives[0].getUsableSpace() / Math.pow(1024, 3);
    }
}
