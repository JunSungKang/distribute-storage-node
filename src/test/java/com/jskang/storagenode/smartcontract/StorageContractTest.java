package com.jskang.storagenode.smartcontract;

import com.jskang.storagenode.common.CommonValue;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.exception.DataSizeRangeException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

class StorageContractTest {

    private SmartContract smartContract;
    private String adminAddress = "0x4161e78793712124b5653ce6a1d592b64b654b72";
    private String adminPassword = "1234";
    private String contractAddress = "0xbE587c126465137aB58388610EC453d4670924BD";

    @BeforeEach
    void setUp() {
        this.smartContract = new SmartContract("192.168.55.121", "8545");
        this.smartContract.connection();
    }

    @Test
    public void getFileHash() throws IOException, DataSizeRangeException, NoSuchAlgorithmException {
        // 0. 데이터 생성
        String rawInput = "sample_jskang.mp4";
        MessageDigest hash = MessageDigest.getInstance(CommonValue.HASH_ALGORITHM_SHA256);
        hash.update(rawInput.getBytes(StandardCharsets.UTF_8));
        Bytes32 bytes32 = new Bytes32(hash.digest());

        // 1. ethereum을 호출할 함수 생성
        Function function = new Function("getFileHash",
            Arrays.asList(bytes32),
            Arrays.asList(
                new TypeReference<Bytes32>() {},
                new TypeReference<Bytes32>() {},
                new TypeReference<Bytes32>() {},
                new TypeReference<Bytes32>() {},
                new TypeReference<Bytes32>() {},
                new TypeReference<Bytes32>() {},
                new TypeReference<Bytes32>() {},
                new TypeReference<Bytes32>() {},
                new TypeReference<Bytes32>() {},
                new TypeReference<Bytes32>() {},
                new TypeReference<Bytes32>() {}
            ));

        // 2. transaction 제작
        Transaction transaction = Transaction.createEthCallTransaction(
            this.adminAddress,
            this.contractAddress,
            FunctionEncoder.encode(function));

        // 3. ethereum 호출후 결과 가져오기
        Web3j web3j = this.smartContract.getWeb3j();
        EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();

        // 4. 결과값 decode
        List<Type> decode = FunctionReturnDecoder.decode(ethCall.getResult(),function.getOutputParameters());

        Object decode1 = FunctionReturnDecoder.decodeIndexedValue(ethCall.getResult(), new TypeReference<Bytes32>() {});

        System.out.println("ethCall.getResult() = " + ethCall.getResult());
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(0).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(1).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(2).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(3).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(4).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(5).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(6).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(7).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(8).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(9).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(10).getValue() ));
    }

    @Test
    public void setFileHashValue()
        throws IOException, InterruptedException, ExecutionException, DataSizeRangeException, NoSuchAlgorithmException {
        // 테스트용으로 0x00000000000000000000000000000000 를 키 값으로 진행
        // 0. 데이터 생성
        String rawInput = "sample.mp4";
        MessageDigest hash = MessageDigest.getInstance(CommonValue.HASH_ALGORITHM_SHA256);
        hash.update(rawInput.getBytes(StandardCharsets.UTF_8));
        Bytes32 bytes32 = new Bytes32(hash.digest());

        List<Bytes32> bytes32sIP = new ArrayList<>();
        for (int i=0; i<9; i++) {
            String rawInput2 = "sample.mp4." +i;
            hash = MessageDigest.getInstance(CommonValue.HASH_ALGORITHM_SHA256);
            hash.update(rawInput2.getBytes(StandardCharsets.UTF_8));
            bytes32sIP.add(new Bytes32(hash.digest()));
        }

        List<Bytes32> bytes32sHASH = new ArrayList<>();
        for (int i=0; i<9; i++) {
            String rawInput2 = "sample2.mp4." +i;
            hash = MessageDigest.getInstance(CommonValue.HASH_ALGORITHM_SHA256);
            hash.update(rawInput2.getBytes(StandardCharsets.UTF_8));
            bytes32sHASH.add(new Bytes32(hash.digest()));
        }

        // 1. ethereum을 호출할 함수 생성
        Array sourceIp = new DynamicArray(Bytes32.class, bytes32sIP);
        Array fileHash = new DynamicArray(Bytes32.class, bytes32sHASH);
        Function function = new Function("setFileHashValue",
            Arrays.asList( bytes32, sourceIp, fileHash),
            Collections.emptyList());

        // 3. Account lock 해제
        boolean unlockAccount = this.smartContract.unlockAccount(adminAddress, adminPassword);
        if (!unlockAccount) {
            System.out.println("UnlockAccount Fail.");
            return;
        }

        //4. account에 대한 nonce값 가져오기.
        Web3j web3j = this.smartContract.getWeb3j();
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
            adminAddress, DefaultBlockParameterName.LATEST).sendAsync().get();

        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        //5. Transaction값 제작
        Transaction transaction = Transaction.createFunctionCallTransaction(
            adminAddress, nonce, Transaction.DEFAULT_GAS,
            null, contractAddress,
            FunctionEncoder.encode(function));

        // 6. ethereum Call
        EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(transaction).send();

        // transaction에 대한 transaction Hash값 얻기.
        String transactionHash = ethSendTransaction.getTransactionHash();

        // ledger에 쓰여지기 까지 기다리기.
        Thread.sleep(5000);

        System.out.println(transactionHash);

        StringBuffer sb = new StringBuffer();
        for (int i=0; i<bytes32.getValue().length; i++) {
            sb.append(bytes32.getValue()[i]);
        }
        System.out.println(sb.toString());
    }

    public String byteArrayToString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<bytes.length; i++) {
            if (bytes[i] < 9) {
                sb.append("0"+bytes[i]);
            } else {
                sb.append(bytes[i]);
            }
        }
        return sb.toString();
    }
}
