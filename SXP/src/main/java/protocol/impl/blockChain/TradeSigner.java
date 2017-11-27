package protocol.impl.blockChain;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class TradeSigner {

	private EthereumContract contract;
	private TransactionReceipt tx;

	public TradeSigner(EthereumContract contract) {
		this.contract = contract;
	}

	public boolean sign() {
		boolean signed = this.contract.sign().send();
        this.tx = contract.getTransactionReceipt().get();
        return signed;
	}

	public TransactionReceipt getTx() {
		return this.tx;
	}
}
