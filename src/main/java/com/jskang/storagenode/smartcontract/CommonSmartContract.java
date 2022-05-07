package com.jskang.storagenode.smartcontract;

import java.io.IOException;
import java.util.List;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.Web3j;

interface CommonSmartContract {

	boolean connection();
	void disConnection();
	Web3j getWeb3j();
	boolean unlockAccount(String address, String password) throws IOException;
	List<String> getFileHash(Bytes32 key) throws IOException ;
	void setFileHashValue(String address, String password, Bytes32 key, List<Bytes32> fileNames, List<Bytes32> fileHashs);
}
