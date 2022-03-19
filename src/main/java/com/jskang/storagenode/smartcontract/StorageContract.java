package com.jskang.storagenode.smartcontract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

public class StorageContract extends Contract {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected StorageContract(String contractBinary, String contractAddress,
        Web3j web3j,
        TransactionManager transactionManager,
        ContractGasProvider gasProvider) {
        super(contractBinary, contractAddress, web3j, transactionManager, gasProvider);
    }

    protected StorageContract(EnsResolver ensResolver, String contractBinary,
        String contractAddress, Web3j web3j, TransactionManager transactionManager,
        ContractGasProvider gasProvider) {
        super(ensResolver, contractBinary, contractAddress, web3j, transactionManager, gasProvider);
    }

    protected StorageContract(String contractBinary, String contractAddress, Web3j web3j,
        Credentials credentials, ContractGasProvider gasProvider) {
        super(contractBinary, contractAddress, web3j, credentials, gasProvider);
    }

    public boolean execute() {
        List<Type> params = new ArrayList<>();
        List<TypeReference<?>> outputs = new ArrayList<>();
        Function function = new Function("distribute", params, outputs);

        try {
            TransactionReceipt transactionReceipt = this.executeTransaction(function);
            return transactionReceipt.isStatusOK();
        } catch (TransactionException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return false;
    }
}
