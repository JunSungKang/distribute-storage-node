package com.jskang.storagenode.smartcontract;

import java.io.IOException;
import java.math.BigInteger;
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
    private String adminAddress = "0x4161e78793712124b5653cE6a1d592B64B654b72";
    private String adminPassword = "1234567890";
    private String contractAddress = "0xbE587c126465137aB58388610EC453d4670924BD";

    @BeforeEach
    void setUp() {
        this.smartContract = new SmartContract("127.0.0.1", "8545");
        this.smartContract.connection();
    }

    @Test
    public void getFileHash() throws IOException {
        // 0. 데이터 생성
        String rawInput = "0x0000000000000000000000000000000000000000000000000000000000000000";
        byte[] rawInputbytes = Numeric.hexStringToByteArray(rawInput);
        Bytes32 bytes32 = new Bytes32(rawInputbytes);

        // 1. ethereum을 호출할 함수 생성
        Function function = new Function("getFileHash",
            Arrays.asList(bytes32),
            Arrays.asList(
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
        List<Type> decode = FunctionReturnDecoder.decode(ethCall.getResult(),
            function.getOutputParameters());

        Object decode1 = FunctionReturnDecoder.decodeIndexedValue(ethCall.getResult(), new TypeReference<Bytes32>() {});

        System.out.println("ethCall.getResult() = " + ethCall.getResult());
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(0).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(1).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(2).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(3).getValue() ));
        System.out.println("getValue = " + byteArrayToString( (byte[])decode.get(4).getValue() ));
    }

    @Test
    public void setFileHashValue() throws IOException, InterruptedException, ExecutionException {
        // 테스트용으로 0x00000000000000000000000000000000 를 키 값으로 진행
        // 0. 데이터 생성
        String rawInput = "0x0000000000000000000000000000000000000000000000000000000000000000";
        byte[] rawInputbytes = Numeric.hexStringToByteArray(rawInput);
        Bytes32 bytes32 = new Bytes32(rawInputbytes);

        String rawInput_1 = "0x4000000000000000000000000000000000000000000000000000000000000000";
        byte[] rawInputbytes_1 = Numeric.hexStringToByteArray(rawInput_1);
        Bytes32 bytes32_1 = new Bytes32(rawInputbytes_1);

        String rawInput_2 = "0x5000000000000000000000000000000000000000000000000000000000000000";
        byte[] rawInputbytes_2 = Numeric.hexStringToByteArray(rawInput_2);
        Bytes32 bytes32_2 = new Bytes32(rawInputbytes_2);

        String rawInput_3 = "0x6000000000000000000000000000000000000000000000000000000000000000";
        byte[] rawInputbytes_3 = Numeric.hexStringToByteArray(rawInput_3);
        Bytes32 bytes32_3 = new Bytes32(rawInputbytes_3);

        // 1. ethereum을 호출할 함수 생성
        Array sourceIp = new DynamicArray(Bytes32.class, bytes32_1, bytes32_2, bytes32_3);
        Array fileHash = new DynamicArray(Bytes32.class, bytes32_1, bytes32_2, bytes32_3);
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
