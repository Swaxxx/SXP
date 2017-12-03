package protocol.impl.blockChain;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthAccounts;

public class EthereumAccountUtils {

	/**
	 * Ensure that the funds of Ether to deploy the smart contract is enough.
	 */
	public static void ensureFunds(Web3j web3j, String address, BigInteger amountWei) throws Exception {
		BigInteger balance = EthereumCurrencyUtils.getBalanceWei(web3j, address);
		
		if (balance.compareTo(amountWei) >= 0) {
			return;
		}
		
		BigInteger missingAmount = amountWei.subtract(balance);
		EthereumTransactionUtils.transferFromCoinbaseAndWait(web3j, address, missingAmount);
	}

	/**
	 * Returns the list of addresses owned by this client.
	 */
	public static EthAccounts getAccounts(Web3j web3j) throws InterruptedException, ExecutionException {
		return web3j
				.ethAccounts()
				.sendAsync()
				.get();
	}

	/**
	 * @TODO logging
	 */
	public static String getAccount(Web3j web3j, int i) {
		try {
			EthAccounts accountsResponse = web3j.ethAccounts().sendAsync().get();
			List<String> accounts = accountsResponse.getAccounts();

			return accounts.get(i);
		} 
		catch (Exception e) {
			System.out.println(e.getMessage());
			return "<no address>";
		}
	}	
}
