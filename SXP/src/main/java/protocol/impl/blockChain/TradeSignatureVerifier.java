package protocol.impl.blockChain;

import java.io.IOException;

import org.web3j.abi.datatypes.Address;

import model.entity.EthereumKey;

public class TradeSignatureVerifier {

	private Address addr1;
    private Address addr2;
    private String item1;
    private String item2;
    private String clauseA;
    private String clauseB;

	public TradeSignatureVerifier(EthereumContract contract) throws IOException, Exception {
		this.addr1 = contract.getContract().getAddr1().send();
		this.addr2 = contract.getContract().getAddr2().send();
		this.item1 = contract.getContract().getItem1().send().toString();
		this.item2 = contract.getContract().getItem2().send().toString();
		this.clauseA = contract.getContract().getClause1().send().toString();
		this.clauseB = contract.getContract().getClause2().send().toString();
	}

	public Address getAddr1() {
        return addr1;
    }

    public Address getAddr2() {
        return addr2;
    }

    public String getItem1() {
        return item1;
    }

    public String getItem2() {
        return item2;
    }

    public String getClauseA() {
        return clauseA;
    }

    public String getClauseB() {
        return clauseB;
    }

	public boolean verif(BlockChainContract bcContract) {
		boolean difference = true;
        int i = 0;
        while (difference && i < bcContract.getParties().size()) {
            EthereumKey key = bcContract.getParties().get(i);
            if (ByteUtil.bytesToHex(ByteUtil.bigIntegerToBytes(key.getPublicKey())).equals((getAddr1().toString()))) {
                difference = false;
            }
            i++;
        }
        if (difference) {
            return false;
        } else {
            difference = true;
        }

        while (difference && i < bcContract.getParties().size()) {
            EthereumKey key = bcContract.getParties().get(i);
            if (ByteUtil.bytesToHex(ByteUtil.bigIntegerToBytes(key.getPublicKey())).equals(getAddr2().toString())) {
                difference = false;
            }
            i++;
        }
        if (difference)
            return false;

        if (!bcContract.getClauses().contains(getClauseA())) {
            return false;
        }
        if (!bcContract.getClauses().contains(getClauseB())) {
            return false;
        }

        return true;
	}
}
