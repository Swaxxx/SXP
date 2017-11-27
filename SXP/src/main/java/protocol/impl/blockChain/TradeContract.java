package web3j.demo.contract;

import java.io.IOException;
import java.lang.String;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

public final class TradeContract extends Contract {
    private static final String BINARY = "";

    private TradeContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
    	super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private TradeContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    @SuppressWarnings("rawtypes")
	public RemoteCall<Address> getSender() throws IOException {
        Function function = new Function("getSender", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Boolean> sign() {
        Function function = new Function("sign", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Boolean>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Boolean> isSigned() {
        Function function = new Function("isSigned", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Boolean>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Address> getSender() throws IOException {
        Function function = new Function("getSender", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Address> getAddr1() throws IOException {
        Function function = new Function("getAddr1", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Address> getAddr2() throws IOException {
        Function function = new Function("getAddr2", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Utf8String> getItem1() throws IOException {
        Function function = new Function("getItem1", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Utf8String> getItem2() throws IOException {
        Function function = new Function("getItem2", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Utf8String> getClause1() throws IOException {
        Function function = new Function("getClause1", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<Utf8String> getClause2() throws IOException {
        Function function = new Function("getClause2", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function);
    }

    @SuppressWarnings("rawtypes")
    public RemoteCall<TransactionReceipt> kill() throws IOException, TransactionException {
		Function function = new Function("kill", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @SuppressWarnings("rawtypes")
    public static RemoteCall<TradeContract> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialValue, Utf8String _greeting) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(_greeting));
        return deployRemoteCall(TradeContract.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor, initialValue);
    }

    @SuppressWarnings("rawtypes")
    public static RemoteCall<TradeContract> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialValue, Utf8String _greeting) {
    	String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(_greeting));
        return deployRemoteCall(TradeContract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor, initialValue);
    }

    public static TradeContract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TradeContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static TradeContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TradeContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
}
