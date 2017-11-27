package crypt.impl.signatures;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlElement;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.abi.datatypes.Address;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public class EthereumSignature {

    @XmlElement(name="tx")
    private TransactionReceipt tx ;

    @XmlElement(name="contractAddr")
    private Address contractAddr;
    
    public EthereumSignature(@JsonProperty("hashSign") TransactionReceipt tx) {
        this.tx = tx;
        this.contractAddr = tx.getContractAddress();
    }

    public TransactionReceipt getTx() {
        return tx;
    }

    public Address getContractAddr() {
        return contractAddr;
    }

    public String toString() {
        return ByteUtil.toHexString(tx.getHash());
    }

    public String getStringEncoded() {
        //return ByteUtil.toHexString(tx.getEncoded());
        return ByteUtil.toHexString(tx.getHash());
    }
}
