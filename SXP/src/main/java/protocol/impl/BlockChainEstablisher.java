package protocol.impl;

import controller.Application;
import crypt.impl.signatures.EthereumSignature;
import crypt.impl.signatures.EthereumSigner;
import model.entity.EthereumKey;
import model.entity.User;
import network.api.EstablisherService;
import network.api.Messages;
import network.api.ServiceListener;
import protocol.api.Establisher;
import protocol.impl.blockChain.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.JsonRpc2_0Web3j;

public class BlockChainEstablisher extends Establisher<BigInteger, EthereumKey, EthereumSignature, EthereumSigner, BlockChainContract> {

    protected final EstablisherService establisherService =
            (EstablisherService) Application.getInstance().getPeer().getService(EstablisherService.NAME);
    protected User establisherUser;
    private boolean shareTxSign;
    protected String contractId;
    protected ArrayList<EthereumKey> othersParties = new ArrayList<>();
    protected EthereumContract ethContract;
    protected Web3j web3j;

    public static Web3j buildHttpClient(String ip, String port) {
		String url = String.format("http://%s:%s", ip, port);
		return new JsonRpc2_0Web3j(new HttpService(url));
	}
    
    public BlockChainEstablisher(User user, HashMap<EthereumKey, String> uri, String ip, String port) {
        // Matching the uris
        uris = uri;
        //Set User who use Establisher instance
        establisherUser = user;
        shareTxSign = false;
		web3j = buildHttpClient(ip, port);
    }

    @Override
    public void initialize(BlockChainContract bcContract) {
        super.contract = bcContract;
        setOthersParties(contract.getParties());
        contractId = contract.getId();
        ethContract = new EthereumContract(this.web3j, this.establisherUser.getEthereumKey());
        super.signer = new EthereumSigner(ethContract, this.establisherUser.getEthereumKey());

        super.signer.setBcContract(contract);

        super.signer.setKey(establisherUser.getEthereumKey());
        bcContract.setSigner(super.signer);
    }

    /**
     * @TODO logging
     * @param bcContract
     * @param deploy
     * @throws Exception 
     */
    public void initialize(BlockChainContract bcContract, boolean deploy) throws Exception {
        this.initialize(bcContract);

        establisherService.addListener(new ServiceListener() {
            @Override
            public void notify(Messages messages) {
                String source = messages.getMessage("sourceId");
                if (!containsParti(source)) {
                    throw new SecurityException("Sender isn't a parti of contract");
                }
                String titleId = messages.getMessage("title");
                if (titleId.equals(contractId)) {
                    throw new SecurityException("ID doesn't match");
                }
                String content = messages.getMessage("contract");
                switch (content.charAt(0)) {
                    case '1' :
                        System.out.println("\n\n[Message retrieved <ContractAddr>] : \n" + source + " -> "
                                + establisherUser.getEthereumKey().toString() + " \n\n");
                        if (!ethContract.isDeployed()) {
                            ethContract.setContractAddress(content.substring(1));
                        }
                        break;
                    case '2' :
                        System.out.println("\n\n[Message retrieved <SolidityHash>] : \n" + source + " -> "
                                + establisherUser.getEthereumKey().toString() + " \n\n");
                        String solidityHash = ethContract.getContractSourceHash();
                        if (!solidityHash.equals(content.substring(1))) {
                            throw new SecurityException("SoliditySrc doesn't match");
                        }
                        break;
                    case '3' :
                        System.out.println("\n\n[Message retrieved <SignatureHash>] : \n" + source
                                + " -> " + establisherUser.getEthereumKey().toString() + " \n\n");
                        String fromWho = messages.getMessage("sourceId");
                        upDateSignatures(fromWho, content.substring(1));
                        if (!shareTxSign && contract.getSignatures().containsKey(establisherUser.getEthereumKey())) {
                            shareSign();
                        }
                        contract.checkContrat(contract);
                        break;
                    default: throw new IllegalArgumentException("Sent a bad content");
                }
            }
        }, establisherUser.getEthereumKey().toString()) ;

        if (deploy && !ethContract.isDeployed()) {
        	ethContract.deploy(
        			new Address(establisherUser.getEthereumKey().getPublicKey()),
        			new Address(othersParties.get(0).getPublicKey()),
        			"item1",
        			"item2",
        			contract.getClauses().get(0),
        			contract.getClauses().get(1));
        }
    }

    @Override
    public void start() {
        if (ethContract.isDeployed()) {
            sendContractAddr();
            sendSolidityHash();
        }
    }

    public void sendContractAddr() {
        if (!ethContract.isDeployed())
            throw new NullPointerException("Couldn't send contract Address, no contracts were deployed");

        for (EthereumKey key : othersParties) {
            establisherService.sendContract(
                    contractId,
                    key.toString(),
                    establisherUser.getEthereumKey().toString(),
                    "1" + ByteUtil.bytesToHex(ethContract.getContractAddress().getBytes())
            );
        }
    }

    public void sendSolidityHash() {
        if (!ethContract.isDeployed())
            throw new NullPointerException("Couldn't send contract Address, no contracts were deployed");

        for (EthereumKey key : othersParties) {
            establisherService.sendContract(
                    contractId,
                    key.toString(),
                    establisherUser.getEthereumKey().toString(),
                    "2" + ethContract.getContractSourceHash()
            );
        }
    }

    public void shareSign() {
        for (EthereumKey key : othersParties) {
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
        for (EthereumKey key : contract.getParties()) {
            if (key.toString().equals(who)) {
                System.out.println("\n\n[Entity Updated] : " + key.toString() + " added by " + establisherUser.getEthereumKey().toString() + "\n\n");
                contract.addSignature(key, new EthereumSignature(TxSign));
            }
        }
    }

    public void sign(BlockChainContract c) {
        contract.sign(super.signer, establisherUser.getEthereumKey());
        contract.checkContrat(c);
        shareSign();
    }

    public void setOthersParties(ArrayList<EthereumKey> parts) {
        for (EthereumKey key : parts)
            if (!key.equals(establisherUser.getEthereumKey()))
                othersParties.add(key);
    }

    public boolean containsParti(String key) {
        for (EthereumKey part : contract.getParties()) {
            if (part.toString().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public EthereumSigner getSigner() {
        return super.signer;
    }
}
