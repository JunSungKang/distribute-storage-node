package com.jskang.storagenode.smartcontract;

import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class SmartContract {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private String hostIp = "192.168.56.1";
    private String hostPort = "8545";
    private String host = "";
    private BigInteger GAS_LIMIT = BigInteger.valueOf(1000000L);
    private BigInteger GAS_PRICE = BigInteger.valueOf(10000000L);

    private Web3j web3j = null;

    public SmartContract() {
        this.host = "http://" + this.hostIp + ":" + this.hostPort;
    }

    public SmartContract(String hostIp, String hostPort) {
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        this.host = "http://" + this.hostIp + ":" + this.hostPort;
    }

    public boolean connection() {
        LOG.info("SmartContract connecting ...");
        try {
            this.web3j = Web3j.build(new HttpService(host));
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
        LOG.info("SmartContract connect success.");
        return true;
    }

    public void disConnection() {
        this.web3j.shutdown();
    }

    public Web3j getWeb3j() {
        return this.web3j;
    }
}
