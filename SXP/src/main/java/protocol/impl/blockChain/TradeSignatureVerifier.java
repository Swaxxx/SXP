package protocol.impl.blockchain;

import org.web3j.abi.datatypes.Address;

public class TradeSignatureVerifier {

	private Address addr1;
    private Address addr2;
    private String item1;
    private String item2;
    private String clauseA;
    private String clauseB;

	public TradeSignatureVerifier(EthereumContract contract) {
		this.addr1 = contract.getAddr1().send();
		this.addr2 = contract.getAddr2().send();
		this.item1 = contract.getItem1().send();
		this.item2 = contract.getItem2().send();
		this.clauseA = contract.getClause1().send();
		this.clauseB = contract.getClause2().send();
	}

	public Address getAddr1() {
        return addr1;
    }

    public Address getAddr2() {
        return addr2;
    }

    public byte[] getMsgSender() {
        return msgSender;
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
        while (difference && i < bc.getParties().size()) {
            EthereumKey key = bc.getParties().get(i);
            if (ByteUtil.toHexString(ByteUtil.bigIntegerToBytes(key.getPublicKey())).equals(ByteUtil.toHexString(getAddr1()))) {
                difference = false;
            }
            i++;
        }
        if (difference) {
            return false;
        } else {
            difference = true;
        }

        while (difference && i < bc.getParties().size()) {
            EthereumKey key = bc.getParties().get(i);
            if (ByteUtil.toHexString(ByteUtil.bigIntegerToBytes(key.getPublicKey())).equals(ByteUtil.toHexString(getAddr2()))) {
                difference = false;
            }
            i++;
        }
        if (difference)
            return false;

        if (!bc.getClauses().contains(getClauseA())) {
            return false;
        }
        if (!bc.getClauses().contains(getClauseB())) {
            return false;
        }

        return true;
	}
}
