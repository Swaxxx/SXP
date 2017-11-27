package protocol.impl.blockChain;

import org.web3j.tx.Contract;

public class TradeContractDeployer {

	private TradeContract contract;

	public TradeContractDeployer(TradeContract contract) {
		this.contract = contract;
	}

	public boolean deploy() {
		TradeContract deployed = TradeContract
			.deploy(
				web3j, 
				credentials, 
				Web3jConstants.GAS_PRICE, 
				Web3jConstants.GAS_LIMIT_GREETER_TX,
				BigInteger.ZERO, 
				greeting)
			.send();

		/* address add1, address add2, string item1, string item2, string clause1, string clause2 */

		if (deployed == null) {
			throw new RuntimeException("Failed to deploy contract to Ethereum blockchain");
		}

		return true;
	}
}
