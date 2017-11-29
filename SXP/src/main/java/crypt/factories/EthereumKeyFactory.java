package crypt.factories;

import model.entity.EthereumKey;
import protocol.impl.blockChain.ByteUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;

public class EthereumKeyFactory {

	public static EthereumKey createNew() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
		ECKeyPair keyPair = Keys.createEcKeyPair();

		BigInteger publicKey = keyPair.getPublicKey();		
		BigInteger privateKey = keyPair.getPrivateKey();

		Credentials credentials = Credentials.create(new ECKeyPair(privateKey, publicKey));
		String address = credentials.getAddress();

		return new EthereumKey(privateKey, publicKey, credentials, address);
	}
	
	public static EthereumKey createFromHex(String privateKeyHex, String publicKeyHex) {
		BigInteger publicKey = ByteUtil.bytesToBigInteger(Hex.decode(publicKeyHex));
		BigInteger privateKey = ByteUtil.bytesToBigInteger(Hex.decode(privateKeyHex));
		
		Credentials credentials = Credentials.create(new ECKeyPair(privateKey, publicKey));
		String address = credentials.getAddress();
		
		return new EthereumKey(privateKey, publicKey, credentials, address);
	}
	
	public static EthereumKey createFromKeystore(String keystoreFilePath, String password) throws IOException, CipherException {
		Credentials credentials = WalletUtils.loadCredentials(password, keystoreFilePath);
		String address = credentials.getAddress();
		BigInteger publicKey = credentials.getEcKeyPair().getPublicKey();
		BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
		
		return new EthereumKey(privateKey, publicKey, credentials, address);
	}
}
