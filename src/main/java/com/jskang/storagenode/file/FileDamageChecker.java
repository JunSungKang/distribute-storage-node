package com.jskang.storagenode.file;

import com.jskang.storagenode.common.CommonValue;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.exception.DataSizeRangeException;
import com.jskang.storagenode.smartcontract.SmartContract;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

public class FileDamageChecker {
    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    public List<String> getFileHash(String fileKey) {
        try {
            List<String> data = new ArrayList<>();
            Bytes32 bytes32 = null;
            bytes32 = Converter.stringToBytes32(fileKey);

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
                    new TypeReference<Bytes32>() {}
                ));

            // 2. transaction 제작
            Transaction transaction = Transaction.createEthCallTransaction(
                CommonValue.ADMIN_ADDRESS,
                CommonValue.CONTRACT_ADDRESS,
                FunctionEncoder.encode(function));

            // 3. ethereum 호출후 결과 가져오기
            SmartContract smartContract = new SmartContract("192.168.55.121", "8545");
            Web3j web3j = smartContract.getWeb3j();
            EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();

            // 4. 결과값 decode
            List<Type> decode = FunctionReturnDecoder.decode(ethCall.getResult(), function.getOutputParameters());
            LOG.debug("ethCall.getResult() = " + ethCall.getResult());
            for (int i=0; i<10; i++) {
                data.add(Converter.bytes32ToString((byte[])decode.get(i).getValue()));
            }
            return data;
        } catch (DataSizeRangeException e) {
            LOG.error(e.getMessage());
            return new ArrayList<>();
        } catch (IOException e) {
            LOG.error(e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    public int[] getDamangeFile(List<String> fileHash) {
        return null;
    }

}
