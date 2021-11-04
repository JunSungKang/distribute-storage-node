package com.jskang.storagenode.common;

import com.jskang.storagenode.StorageNodeApplication;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkInfo {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private String localIpAddress = "";
    private int port = -1;

    /**
     * get local ip address.
     *
     * @return setting ip.
     */
    public String getLocalIpAddress() {
        if (this.localIpAddress.isBlank()) {
            this.setLocalIpAddress();
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
}
