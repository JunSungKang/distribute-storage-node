package com.jskang.storagenode.smartcontract;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

class StorageContractTest {

    private SmartContract smartContract;
    private String adminAddress = "0x779eb887a63efef51db5f16da4e42aa95297b119";
    private String contractAddress = "0x2957187403769b0e175a7f5833971B09F8717b5f";
    private Credentials credentials = null;

    @BeforeEach
    void setUp() {
        this.smartContract = new SmartContract("192.168.56.1", "8545");
        this.smartContract.connection();
    }

    @Test
    public byte[] getFileHash() throws IOException {
        // 테스트용으로 0x00000000000000000000000000000000 를 키 값으로 진행
        byte[] bytes = new byte[32];
        Bytes32 bytes32 = new Bytes32(bytes);

        // 1. ethereum을 호출할 함수 생성
        Function function = new Function("getFileHash",
            Arrays.asList(bytes32),
            Arrays.asList(new TypeReference<Bytes32>() {}));

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

        System.out.println("ethCall.getResult() = " + ethCall.getResult());
        System.out.println("getValue = " + decode.get(0).getValue());
        System.out.println("getType = " + decode.get(0).getTypeAsString());

        return (byte[])decode.get(0).getValue();
    }
}
