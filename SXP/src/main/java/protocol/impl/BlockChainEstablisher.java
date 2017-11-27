package protocol.impl;

import controller.Application;
import controller.Users;
import crypt.impl.signatures.EthereumSignature;
import crypt.impl.signatures.EthereumSigner;
import model.api.Status;
import model.api.UserSyncManager;
import model.entity.EthereumKey;
import model.entity.User;
import model.syncManager.UserSyncManagerImpl;
import network.api.EstablisherService;
import network.api.Messages;
import network.api.Peer;
import network.api.ServiceListener;
import network.api.advertisement.EstablisherAdvertisementInterface;
import network.factories.AdvertisementFactory;
import org.ethereum.core.Transaction;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import protocol.api.Establisher;
import protocol.impl.blockChain.*;*
import rest.api.Authentifier;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alex on 10/05/17.
 */
public class BlockChainEstablisher extends Establisher<BigInteger, EthereumKey, EthereumSignature, EthereumSigner, BlockChainContract> {

    protected final EstablisherService establisherService =
            (EstablisherService) Application.getInstance().getPeer().getService(EstablisherService.NAME);

    protected User establisherUser;
    private boolean shareTxSign;

    protected String contractId;
    protected ArrayList<EthereumKey> othersParties = new ArrayList<>();

    protected EthereumContract ethContract;

    public BlockChainEstablisher(User user, HashMap<EthereumKey, String> uri) {
        // Matching the uris
        uris = uri;
        //Set User who use Establisher instance
        establisherUser = user;
        shareTxSign = false;
    }

    @Override
    public void initialize(BlockChainContract bcContract) {
        super.contract = bcContract;
        setOthersParties(contract.getParties());
        contractId = contract.getId();
        try {
            ethContract = new EthereumContract();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.signer = new EthereumSigner(ethContract, sync);

        super.signer.setBcContract(contract);

        super.signer.setKey(establisherUser.getEthereumKey());
        bcContract.setSigner(super.signer);
    }

    public void initialize(BlockChainContract bcContract, boolean deploy) {
        this.initialize(bcContract);

        establisherService.addListener(new ServiceListener() {
            @Override
            public void notify(Messages messages) {
                String source = messages.getMessage("sourceId");
                if(!containsParti(source)) {
                    throw new SecurityException("Sender isn't a parti of contract");
                }
                String titleId = messages.getMessage("title");
                if(titleId.equals(contractId)) {
                    throw new SecurityException("ID doesn't match");
                }
                String content = messages.getMessage("contract");
                switch (content.charAt(0)) {
                    case '1' :
                        System.out.println("\n\n[Message retrieved <ContractAddr>] : \n" + source + " -> "
                                + establisherUser.getEthereumKey().toString() + " \n\n");
                        if(!ethContract.isDeployed()) {
                            ethContract.setContractAdr(Hex.decode(content.substring(1)));
                        }
                        break;
                    case '2' :
                        System.out.println("\n\n[Message retrieved <SolidityHash>] : \n" + source + " -> "
                                + establisherUser.getEthereumKey().toString() + " \n\n");
                        String solidityHash = ethContract.gethashSolidity();
                        if(!solidityHash.equals(content.substring(1))) {
                            throw new SecurityException("SoliditySrc doesn't match");
                        }
                        break;
                    case '3' :
                        System.out.println("\n\n[Message retrieved <SignatureHash>] : \n" + source
                                + " -> " + establisherUser.getEthereumKey().toString() + " \n\n");
                        String fromWho = messages.getMessage("sourceId");
                        upDateSignatures(fromWho, content.substring(1));
                        if(!shareTxSign && contract.getSignatures().containsKey(establisherUser.getEthereumKey())) {
                            shareSign();
                        }
                        sync.run();
                        contract.getSigner().setSync(sync);
                        contract.checkContrat(contract);
                        sync.closeSync();
                        break;
                    default: throw new IllegalArgumentException("Sent a bad content");
                }
            }
        }, establisherUser.getEthereumKey().toString()) ;

        if(deploy && !ethContract.isDeployed()) {
            new DeployContract(sync, ethContract, establisherUser.getEthereumKey()).run();
            new SolidityConstructor(
                    sync,
                    ethContract,
                    establisherUser.getEthereumKey(),
                    ByteUtil.bigIntegerToBytes(establisherUser.getEthereumKey().getPublicKey()),
                    ByteUtil.bigIntegerToBytes(othersParties.get(0).getPublicKey()),
                    "",
                    "",
                    contract.getClauses().get(0),
                    contract.getClauses().get(1)
            ).run();
        }
    }

    @Override
    public void start() {
        if(ethContract.isDeployed()) {
            sendContractAddr();
            sendSolidityHash();
        }
    }

    public void sendContractAddr() {
        if(!ethContract.isDeployed())
            throw new NullPointerException("Couldn't send contract Address, no contracts were deployed");

        for(EthereumKey key : othersParties) {
            establisherService.sendContract(
                    contractId,
                    key.toString(),
                    establisherUser.getEthereumKey().toString(),
                    "1" + ByteUtil.toHexString(ethContract.getContractAdr())
            );
        }
    }

    public void sendSolidityHash() {
        if(!ethContract.isDeployed())
            throw new NullPointerException("Couldn't send contract Address, no contracts were deployed");

        for(EthereumKey key : othersParties) {
            establisherService.sendContract(
                    contractId,
                    key.toString(),
                    establisherUser.getEthereumKey().toString(),
                    "2" + ethContract.gethashSolidity().toString()
            );
        }
    }

    public void shareSign() {
        for(EthereumKey key : othersParties) {
            establisherService.sendContract(
                    contractId,
                    key.toString(),
                    establisherUser.getEthereumKey().toString(),
                    "3" + contract.getSignatures().get(establisherUser.getEthereumKey()).getStringEncoded()
            );
        }
        shareTxSign = true;
    }

    public void upDateSignatures(String who, String TxSign) {
        for(EthereumKey key : contract.getParties()) {
            if (key.toString().equals(who)) {
                System.out.println("\n\n[Entity Updated] : " + key.toString() + " added by " + establisherUser.getEthereumKey().toString() + "\n\n");
                contract.addSignature(key, new EthereumSignature(TxSign));
            }
        }
    }

    public void sign(BlockChainContract c) {
        sync.run();
        contract.getSigner().setSync(sync);
        contract.sign(super.signer, establisherUser.getEthereumKey());
        contract.checkContrat(c);
        shareSign();
    }

    public void setOthersParties(ArrayList<EthereumKey> parts) {
        for (EthereumKey key : parts)
            if(!key.equals(establisherUser.getEthereumKey()))
                othersParties.add(key);
    }

    public boolean containsParti(String key) {
        for(EthereumKey part : contract.getParties()) {
            if(part.toString().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public void stopSync() {
        sync.closeSync();
    }

    public EthereumSigner getSigner() {
        return super.signer;
    }

}
