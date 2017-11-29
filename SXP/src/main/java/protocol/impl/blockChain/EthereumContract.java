package protocol.impl.blockChain;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import crypt.impl.hashs.SHA256Hasher;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;

import model.entity.EthereumKey;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.function.Consumer;

public class EthereumContract {
	
	private Web3j web3j;
	private EthereumKey key;
	private TradeContract contract;
	private String contractAddress = null;
	private String deployedContractHash = null;
	
	@SuppressWarnings("unused")
	private boolean compiled = false;
	
	private String contractSource;
	private String contractSourceHash;
	
	public EthereumContract(Web3j web3j, EthereumKey key) {
		this.web3j = web3j;
		this.key = key;
		this.contractSource = TradeContract.BINARY;
		this.contractSourceHash = ByteUtil.bytesToHex(new SHA256Hasher().getHash(contractSource.getBytes()));
	}
	
	public EthereumContract(Web3j web3j, EthereumKey key, String contractSource) {
		throw new UnsupportedOperationException();
	}
	
	public String getContractSource() {
		throw new UnsupportedOperationException();
	}
	
	public String getContractSourceHash() {
		return contractSourceHash;
	}

	public String getContractAddress() {
		return this.contractAddress;
	}
	
	public void setContractAddress(String contractAddress) {
		this.contractAddress = contractAddress;
	}
	
	public TradeContract getContract() {
		return this.contract;
	}
	
	public String getDeployedContractHash() {
		return deployedContractHash;
	}
	
	public boolean isCompiled() {
		throw new UnsupportedOperationException();
	}
	
	public boolean isDeployed() {
		return this.deployedContractHash != null;
	}
	
	public boolean compile() {
		throw new UnsupportedOperationException();
	}
	
	public boolean deploy(Address part1, Address part2, 
			String item1, String item2, 
			String clause1, String clause2) throws Exception {
		
		/* Move funds to contract owner (amount in wei) to deploy the contract */
		Credentials credentials = this.key.getCredentials();
		String contractOwnerAdress = credentials.getAddress();
		BigInteger initialBalance = BigInteger.valueOf(25_000_000_000_000_000L);
		EthereumAccountUtils.ensureFunds(this.web3j, contractOwnerAdress, initialBalance);
		BigInteger ownerBalanceBeforeDeploy = EthereumCurrencyUtils.getBalanceWei(this.web3j, contractOwnerAdress);
		
		this.contract = TradeContract
				.deploy(
					web3j, 
					credentials, 
					EthereumConstants.GAS_PRICE, 
					EthereumConstants.GAS_LIMIT_GREETER_TX,
					BigInteger.ZERO, 
					part1,
					part2,
					new Utf8String(item1),
					new Utf8String(item2),
					new Utf8String(clause1),
					new Utf8String(clause2))
				.send();
		
		if (this.contract == null) {
			return false;
		}

		TransactionReceipt txReceipt = this.contract
				.getTransactionReceipt()
				.get();
		
		this.deployedContractHash = txReceipt.getTransactionHash();
		BigInteger deployFees = txReceipt.getCumulativeGasUsed().multiply(EthereumConstants.GAS_PRICE);
		BigInteger expectedBalanceAfterDeploy = ownerBalanceBeforeDeploy.subtract(deployFees);
		BigInteger actualBalanceAfterDeploy = EthereumCurrencyUtils.getBalanceWei(this.web3j, contractOwnerAdress);

		/**
		 * @TODO logging
		 */
		if (expectedBalanceAfterDeploy.compareTo(actualBalanceAfterDeploy) != 0) {
			throw new RuntimeException("Unexpected contract owner balance after contract deployement. Deploy hash : " + deployedContractHash + " ; deploy fees : " + deployFees);
		}
		
		/* get contract address (after deploy) */
		this.contractAddress = contract.getContractAddress();

		return this.contractAddress != null;
	}

	/**
	 * Reads the specified solidity file and returns it as a single line string.
	 * Includes some preprocessing: removing '//' comments and \s+ are replaced by ' ' globally. 
	 * Can be use in future to compile Solity source from file name.
	 */
	@SuppressWarnings("unused")
	private static String readSolidityFile(String fileName) {
		try {
			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			final StringBuffer text = new StringBuffer();

			br.lines().forEach(new Consumer<String>() {
				@Override
				public void accept(String line) {
					if(text.length() > 0) {
						text.append(" ");
					}

					// treat comments
					if(line.contains("//")) {
						line = line.substring(0, line.indexOf("//"));
					}

					text.append(line);
				}
			});
			
			br.close();

			String sourceCode = text.toString();
			return sourceCode.replaceAll("\\s+", " ");
		} 
		catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
