package com.jskang.storagenode.smartcontract;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;

class SmartContractTest {

    private SmartContract smartContract;

    @BeforeEach
    void setUp() {
        this.smartContract = new SmartContract("192.168.56.1", "8545");
    }

    @Test
    void connection1() throws IOException {
        boolean isConnect = this.smartContract.connection();
        assertEquals(true, isConnect);

        Web3j web3j = this.smartContract.getWeb3j();
        Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().send();
        System.out.println(web3ClientVersion.getWeb3ClientVersion());
    }

    @Test
    void connection2() throws ExecutionException, InterruptedException {
        boolean isConnect = this.smartContract.connection();
        assertEquals(true, isConnect);

        Web3j web3j = this.smartContract.getWeb3j();
        Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().sendAsync().get();
        System.out.println(web3ClientVersion.getWeb3ClientVersion());
    }

    @Test
    void connection3() {
        boolean isConnect = this.smartContract.connection();
        assertEquals(true, isConnect);

        Web3j web3j = this.smartContract.getWeb3j();
        web3j.web3ClientVersion().flowable().subscribe(x -> {
            System.out.println(x.getWeb3ClientVersion());
        });
    }
}
