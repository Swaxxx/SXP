package crypt.factories;

import model.entity.EthereumKey;

import java.math.BigInteger;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

public class EthereumKeyFactory {

	public static EthereumKey create() {
		ECKeyPair keyPair = Keys.createEcKeyPair();

		BigInteger publicKey = keyPair.getPublicKey();		
		BigInteger privateKey = keyPair.getPrivateKey();

		Credentials credentials = Credentials.create(new ECKeyPair(privateKey, publicKey));
		String address = credentials.getAddress();

		return new EthereumKey(privateKey, publicKey, credentials, address);
	}
}
