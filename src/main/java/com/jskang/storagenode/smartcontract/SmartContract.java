package com.jskang.storagenode.smartcontract;

import com.jskang.storagenode.common.CommonValue;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.exception.DataSizeRangeException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;

public class SmartContract implements CommonSmartContract{

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    public static String DEFAULT_IP = "192.168.55.121";
    public static String DEFAULT_PORT = "8545";
    private String host = "";
    private BigInteger GAS_LIMIT = BigInteger.valueOf(1000000L);
    private BigInteger GAS_PRICE = BigInteger.valueOf(10000000L);

    private Web3j web3j = null;

    public SmartContract() {
        this.host = "http://" +DEFAULT_IP+ ":" +DEFAULT_PORT;
    }

    public SmartContract(String hostIp, String hostPort) {
        this.host = "http://" + hostIp + ":" + hostPort;
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

    /**
     *
     * @param address wallet address
     * @param password waller password
     * @return if unlock success true, the other false.
     * @throws IOException
     */
    public boolean unlockAccount(String address, String password) throws IOException {
        Admin admin = Admin.build(new HttpService(host));
        PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(address, password).send();
        return personalUnlockAccount.accountUnlocked();
    }

    /**
     * ???????????? ????????? ????????? ????????????
     * @param key key ???????????? ?????? ?????????
     * @return
     * @throws IOException
     */
    public List<String> getFileHash(Bytes32 key) throws IOException {
        // 1. ethereum??? ????????? ?????? ??????
        Function function = new Function("getFileHash",
            Arrays.asList(key),
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

        // 2. transaction ??????
        Transaction transaction = Transaction.createEthCallTransaction(
            CommonValue.ADMIN_ADDRESS,
            CommonValue.CONTRACT_ADDRESS,
            FunctionEncoder.encode(function));

        // 3. ethereum ????????? ?????? ????????????
        Web3j web3j = this.getWeb3j();
        EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();

        // 4. ????????? decode
        List<Type> decode = FunctionReturnDecoder.decode(ethCall.getResult(),
            function.getOutputParameters());

        List<String> values = new LinkedList<>();
        try {
            for (Type type : decode) {
                String value = Converter.bytes32ToString((byte[]) type.getValue());
                values.add(value);
            }
        } catch (DataSizeRangeException e) {
            LOG.error("Bytes32 to String mapping fail.");
            LOG.debug(e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            LOG.debug(e.getMessage());
            return new ArrayList<>();
        }
        return values;
    }

    /**
     * ?????? ???????????? ???, ?????? ?????? ????????????
     * @param address ???????????? ?????? ????????? ??????
     * @param password ???????????? ?????? ????????????
     * @param key ???????????? ?????? ?????????
     * @param fileNames ???????????? ????????? ???????????? ?????? ??????
     * @param fileHashs ???????????? ????????? ???????????? ????????? ?????? ??????
     */
    public void setFileHashValue(String address, String password, Bytes32 key, List<Bytes32> fileNames, List<Bytes32> fileHashs) {
        if (fileNames.size() != 9 || fileHashs.size() != 9) {
            LOG.error("File be lost metadata. Smart-Contract fail.");
            return;
        }
        // 1. ethereum??? ????????? ?????? ??????
        Array sourceIp = new DynamicArray(Bytes32.class, fileNames);
        Array fileHash = new DynamicArray(Bytes32.class, fileHashs);
        Function function = new Function("setFileHashValue",
                                         Arrays.asList(key, sourceIp, fileHash),
                                         Collections.emptyList());

        // 2. Account lock ??????
        try {
            boolean unlockAccount = this.unlockAccount(address, password);
            if (!unlockAccount) {
                LOG.error("UnlockAccount Fail.");
                return;
            }
        } catch (IOException e) {
            LOG.error("UnlockAccount Fail.");
            LOG.debug(e.getMessage());
            return;
        } catch (Exception e) {
            LOG.error("UnlockAccount Fail.");
            LOG.debug(e.getMessage());
            return;
        }

        //4. account??? ?????? nonce??? ????????????.
        Web3j web3j = this.getWeb3j();
        EthGetTransactionCount ethGetTransactionCount = null;
        try {
            ethGetTransactionCount = web3j.ethGetTransactionCount(
                address, DefaultBlockParameterName.LATEST
            ).sendAsync().get();
        } catch (InterruptedException e) {
            LOG.error("Get nonce value fail. (Interrupted)");
            LOG.debug(e.getMessage());
        } catch (ExecutionException e) {
            LOG.error("Get nonce value fail. (Execution)");
            LOG.debug(e.getMessage());
        } catch (Exception e) {
            LOG.error("Get nonce value fail. (Execution)");
            LOG.debug(e.getMessage());
        }

        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        //5. Transaction??? ??????
        Transaction transaction = Transaction.createFunctionCallTransaction(
            address, nonce, Transaction.DEFAULT_GAS,
            null, CommonValue.CONTRACT_ADDRESS,
            FunctionEncoder.encode(function));

        // 6. ethereum Call
        try {
            EthSendTransaction ethSendTransaction
                = web3j.ethSendTransaction(transaction)
                .send();
            LOG.debug(ethSendTransaction.getResult());
        } catch (IOException e) {
            LOG.error("ETH Transaction fail.");
            LOG.debug(e.getMessage());
        } catch (Exception e) {
            LOG.error("ETH Transaction fail.");
            LOG.debug(e.getMessage());
        }
    }
}
