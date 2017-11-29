package crypt.impl.signatures;

import crypt.base.AbstractSigner;
import model.entity.EthereumKey;
import protocol.impl.blockChain.*;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class EthereumSigner extends AbstractSigner<EthereumSignature, EthereumKey> {

    private EthereumContract contract;
    private BlockChainContract bcContract;

    public EthereumSigner(EthereumContract contract, EthereumKey key) {
        this.contract = contract;
    }

    public void setBcContract(BlockChainContract bcContract) {
        this.bcContract = bcContract;
    }

    public BlockChainContract getBcContract() {
        return bcContract;
    }

    @Override
    public EthereumKey getKey() {
        return super.key;
    }

    @Override
    public EthereumSignature sign(byte[] message) {
        TradeSigner signer = new TradeSigner(this.contract);

        try {
			if (!signer.sign()) {
			    throw new NullPointerException("Failed to sign contract");
			}
		} catch (Exception e) {
			// TODO Logging
			e.printStackTrace();
			return null;
		}

        TransactionReceipt tx = signer.getTx();

        if (tx == null) {
            throw new NullPointerException("Sign Tx don't exist on the BlockChain");
        }

        return new EthereumSignature(tx);
    }

    @Override
    public boolean verify(byte[] message, EthereumSignature ethereumSignature) {
        if (bcContract == null) {
            throw new NullPointerException("BlockChainContract not Set");
        }

        TradeSignatureVerifier verifier = null;
        
		try {
			verifier = new TradeSignatureVerifier(this.contract);
		} catch (Exception e) {
			// TODO Logging
			e.printStackTrace();
			return false;
		}

        return verifier.verif(this.bcContract);
    }
}
