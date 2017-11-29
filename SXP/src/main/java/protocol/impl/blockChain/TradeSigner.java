package protocol.impl.blockChain;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class TradeSigner {

	private EthereumContract contract;
	private TransactionReceipt tx;

	public TradeSigner(EthereumContract contract) {
		this.contract = contract;
	}

	public boolean sign() throws Exception {
		boolean signed = this.contract.getContract().sign().send().getValue();
        this.tx = contract.getContract().getTransactionReceipt().get();
        return signed;
	}

	public TransactionReceipt getTx() {
		return this.tx;
	}
}
