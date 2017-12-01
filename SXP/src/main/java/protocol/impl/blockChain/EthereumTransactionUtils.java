package protocol.impl.blockChain;

import java.math.BigInteger;
import java.util.Optional;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class EthereumTransactionUtils {

	public static String transferWei(Web3j web3j, String from, String to, BigInteger amountWei) throws Exception {
		BigInteger nonce = EthereumCurrencyUtils.getNonce(web3j, from);
		Transaction transaction = Transaction.createEtherTransaction(
				from, nonce, EthereumConstants.GAS_PRICE, EthereumConstants.GAS_LIMIT_ETHER_TX, to, amountWei);

		EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(transaction).sendAsync().get();

		/**
		 * @TODO : Logging of
		 * System.out.println("transferEther. nonce: " + nonce + " amount: " + amountWei + " to: " + to);
		 */

		String txHash = ethSendTransaction.getTransactionHash(); 
		EthereumTransactionUtils.waitForReceipt(web3j, txHash);
		
		return txHash;
	}
	
	/**
	 * Transfers the specified amount of Wei from the coinbase to the specified account.
	 * The method waits for the transfer to complete using method {@link waitForReceipt}.  
	 */
	public static TransactionReceipt transferFromCoinbaseAndWait(Web3j web3j, String to, BigInteger amountWei) throws Exception {
		String coinbase = EthereumCurrencyUtils.getCoinbase(web3j).getResult();
		BigInteger nonce = EthereumCurrencyUtils.getNonce(web3j, coinbase);
		// this is a contract method call -> gas limit higher than simple fund transfer
		BigInteger gasLimit = EthereumConstants.GAS_LIMIT_ETHER_TX.multiply(BigInteger.valueOf(2)); 
		Transaction transaction = Transaction.createEtherTransaction(
				coinbase, 
				nonce, 
				EthereumConstants.GAS_PRICE, 
				gasLimit, 
				to, 
				amountWei);

		EthSendTransaction ethSendTransaction = web3j
				.ethSendTransaction(transaction)
				.sendAsync()
				.get();

		String txHash = ethSendTransaction.getTransactionHash();
		
		return EthereumTransactionUtils.waitForReceipt(web3j, txHash);
	}

	/**
	 * Waits for the receipt for the transaction specified by the provided tx hash.
	 * Makes 40 attempts (waiting 1 sec. inbetween attempts) to get the receipt object.
	 * In the happy case the tx receipt object is returned.
	 * Otherwise, a runtime exception is thrown. 
	 */
	public static TransactionReceipt waitForReceipt(Web3j web3j, String transactionHash) throws Exception {

		int attempts = EthereumConstants.CONFIRMATION_ATTEMPTS;
		int sleep_millis = EthereumConstants.SLEEP_DURATION;
		
		Optional<TransactionReceipt> receipt = EthereumTransactionUtils.getReceipt(web3j, transactionHash);

		while (attempts-- > 0 && !receipt.isPresent()) {
			Thread.sleep(sleep_millis);
			receipt = EthereumTransactionUtils.getReceipt(web3j, transactionHash);
		}

		if (attempts <= 0) {
			throw new RuntimeException("No Tx receipt received");
		}

		return receipt.get();
	}

	/**
	 * Returns the TransactionRecipt for the specified tx hash as an optional.
	 */
	public static Optional<TransactionReceipt> getReceipt(Web3j web3j, String transactionHash) throws Exception {
		EthGetTransactionReceipt receipt = web3j
				.ethGetTransactionReceipt(transactionHash)
				.sendAsync()
				.get();

		return receipt.getTransactionReceipt();
	}
}
