package crypt.impl.signatures;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlElement;

import org.glassfish.jersey.internal.util.Base64;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class EthereumSignature {

	@XmlElement(name="transactionReceipt")
    private TransactionReceipt transactionReceipt;

    @XmlElement(name="contractAddr")
    private String contractAddr;
    
    public EthereumSignature(@JsonProperty("hashSign") TransactionReceipt transactionReceipt) {
        this.transactionReceipt = transactionReceipt;
        this.contractAddr = transactionReceipt.getContractAddress();
    }
    
    public EthereumSignature(String transactionReceiptEncoded) {
    	String transactionReceiptDecoded = Base64.decodeAsString(transactionReceiptEncoded);
    	String[] splited = transactionReceiptDecoded.split("\\s+");
    	this.transactionReceipt = new TransactionReceipt();
    	this.transactionReceipt.setTransactionHash(splited[0]);
    	this.transactionReceipt.setFrom(splited[1]);
    	this.transactionReceipt.setBlockHash(splited[2]);
    	this.transactionReceipt.setTo(splited[3]);
    	this.transactionReceipt.setGasUsed(splited[4]);
    	this.transactionReceipt.setContractAddress(splited[5]);
    	this.contractAddr = splited[5];
    	
    }
    
    public TransactionReceipt getTransactionReceipt() {
    	return transactionReceipt;
    }

    public String getContractAddr() {
        return contractAddr;
    }

    public String getStringEncoded() {
        return Base64.encodeAsString(
        		transactionReceipt.getTransactionHash() + " " + 
        	    transactionReceipt.getFrom() + " " +
				transactionReceipt.getBlockHash() + " " + 
        	    transactionReceipt.getTo() + " " +
				transactionReceipt.getGasUsedRaw() + " " +
				transactionReceipt.getContractAddress());
    }
}
