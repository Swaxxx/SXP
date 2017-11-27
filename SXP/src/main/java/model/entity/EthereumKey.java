package model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.bouncycastle.pqc.math.linearalgebra.BigIntUtils;
import org.spongycastle.util.encoders.Hex;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.math.BigInteger;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Numeric;
import org.web3j.utils.Convert;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

public class EthereumKey extends ECKey implements AsymKey<BigInteger>, Serializable {

    @NotNull
    @XmlElement(name="privateKey")
    @JsonSerialize(using=controller.tools.BigIntegerSerializer.class)
    @JsonDeserialize(using=controller.tools.BigIntegerDeserializer.class)
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    @JsonIgnore
    private BigInteger privateKey;

    @NotNull
    @XmlElement(name="publicKey")
    @JsonSerialize(using=controller.tools.BigIntegerSerializer.class)
    @JsonDeserialize(using=controller.tools.BigIntegerDeserializer.class)
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private BigInteger publicKey;

    @NotNull
    @XmlElement(name="address")
    private String address;

    @NotNull
    @XmlElement(name="credentials")
    private Credentials credentials;
    
    public EthereumKey(BigInteger privateKey, BigInteger publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.credentials = Credentials.create(new ECKeyPair(privateKey, publicKey));
        this.address = credentials.getAddress();
    }

    public EthereumKey(BigInteger privateKey, BigInteger publicKey, Credentials credentials) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.credentials = credentials;
        this.address = credentials.getAddress();
    }

    public EthereumKey(BigInteger privateKey, BigInteger publicKey, Credentials credentials, String address) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.credentials = credentials;
        this.address = address;
    }

    @Override
    public BigInteger getPublicKey() {
        return publicKey;
    }
    @Override
    public BigInteger getPrivateKey() {
        return privateKey;
    }

    @Override
    public BigInteger getParam(String p) {
        return null;
    }

    @Override
    public void setPublicKey(BigInteger pbk) {
        publicKey = pbk;
    }
    @Override
    public void setPrivateKey(BigInteger pk) {
        privateKey = pk;
    }

    public String getStringPrivateKey() {
        return Numeric.toHexStringWithPrefix(privateKey);
    }

    public String getStringPublicKey() {
        return Numeric.toHexStringWithPrefix(publicKey);
    }

    public String getAddress() {
        return this.address;
    }

    public Credentials getCredentials() {
        return this.credentials;
    }

    public boolean equals(EthereumKey account) {
        return this.address.equals(account.getAddress()) &&
            this.credentials.equals(account.getCredentials());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Private key : \n");
        builder.append(this.getStringPrivateKey());
        builder.append("\n\n");
        builder.append("Private public : \n");
        builder.append(this.getStringPublicKey());
        return builder.toString();
    }
}
