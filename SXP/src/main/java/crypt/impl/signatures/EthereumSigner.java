package crypt.impl.signatures;

import crypt.base.AbstractSigner;
import model.entity.EthereumKey;
import org.ethereum.util.ByteUtil;
import protocol.impl.blockChain.*;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.abi.datatypes.Address;

public class EthereumSigner extends AbstractSigner<EthereumSignature, EthereumKey> {

    private EthereumContract contract;
    private EthereumKey key;
    private BlockChainContract bcContract;

    public EthereumSigner(EthereumContract contract, EthereumKey key) {
        this.contract = contract;
        this.key = key;
    }

    public void setBcContract(BlockChainContract bcContract) {
        this.bcContract = bcContract;
    }

    public void setSync(SyncBlockChain sync) {
        this.sync = sync;
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

        if (!signer.sign()) {
            throw new NullPointerException("Failed to sign contract");
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

        TradeSignatureVerifier verfier = new TradeSignatureVerifier(this.contract);

        return verifier.verif(this.bcContract);
    }
}
