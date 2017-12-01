package protocol.impl.blockChain;

import com.fasterxml.jackson.core.type.TypeReference;
import controller.Users;
import controller.tools.JsonTools;
import crypt.impl.hashs.SHA256Hasher;
import crypt.impl.signatures.EthereumSignature;
import crypt.impl.signatures.EthereumSigner;
import model.api.Status;
import model.api.Wish;
import model.api.EstablisherType;
import model.entity.ContractEntity;
import model.entity.EthereumKey;
import model.entity.User;

import org.bouncycastle.util.Arrays;
import protocol.api.EstablisherContract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class BlockChainContract extends EstablisherContract<BigInteger, EthereumKey, EthereumSignature, EthereumSigner>{
    
	private String id;
    private Date date;
	
    // List of parties keys
    protected ArrayList<EthereumKey> parties = new ArrayList<>();
    // Maps the keys with the id of a user
    protected HashMap<EthereumKey,String> partiesId = new HashMap<EthereumKey, String>();
    // Maps the keys with the signatures
    protected HashMap<EthereumKey, EthereumSignature> signatures = new HashMap<EthereumKey, EthereumSignature>();
    // Clauses in the format we need them
    protected ArrayList<String> clauses = null;
    // Signer object
    protected EthereumSigner signer;
   
    // Basic constructor
    public BlockChainContract(){
        super();
        this.contract = new ContractEntity();
        date = contract.getCreatedAt();
        contract.setClauses(new ArrayList<String>());
        contract.setParties(new ArrayList<String>());
        contract.setSignatures(new HashMap<String,String>());
        contract.setEstablisherType(EstablisherType.Ethereum);
        id = ByteUtil.bytesToHex(getHashableData());
        contract.setTitle(id);
    }
    
    // Constructor from a ContractEntity (what will be most used)
    public BlockChainContract(ContractEntity c){
        super();
        this.contract = c;
        this.setClauses(contract.getClauses());
        this.setParties(contract.getParties());
        this.contract.setEstablisherType(EstablisherType.Ethereum);
        date = contract.getCreatedAt();
        id = ByteUtil.bytesToHex(getHashableData());
        contract.setTitle(id);
    }
    
    public BlockChainContract(ContractEntity c, ArrayList<EthereumKey> parties){
        super();
        this.contract = c;
        this.setClauses(contract.getClauses());
        this.setParties(contract.getParties());
        this.contract.setEstablisherType(EstablisherType.Ethereum);
        date = contract.getCreatedAt();
        id = ByteUtil.bytesToHex(getHashableData());
        setPartiesAsKeys(parties);
        contract.setTitle(id);
    }

    /************* GETTERS ***********/
    public ArrayList<String> getClauses(){
        return clauses;
    }
    
    public ArrayList<EthereumKey> getParties(){
        return parties;
    }
    
    public String getId() {
    	return id;
    }
    
    public HashMap<EthereumKey, EthereumSignature> getSignatures() {
    	return signatures;
    }
    
    /************* SETTERS ***********/
    public void setClauses(ArrayList<String> c){
        this.clauses = c;
        this.contract.setClauses(c);
    }
    
    /**
     * Find the parties keys
     * @param s : List of user ids
     */
    public void setParties(ArrayList<String> s){
        for (String u : s){
            JsonTools<User> json = new JsonTools<>(new TypeReference<User>(){});
            Users users = new Users();
            User user = json.toEntity(users.get(u));
            this.parties.add(user.getEthereumKey());
            this.partiesId.put(user.getEthereumKey(), user.getId());
        }
        this.contract.setParties(s);
        
        // Order parties by publicKey (useful to get hashable data
        this.parties.sort(new Comparator<EthereumKey>(){
            @Override
            public int compare(EthereumKey k1, EthereumKey k2){
                return k1.getPublicKey().compareTo(k2.getPublicKey());
            }
        });
    }
    
    public void setPartiesAsKeys(ArrayList<EthereumKey> s) {
    	parties.addAll(s) ;
        int i=0 ;
        for(EthereumKey tmp : s) {
        	partiesId.put(tmp, contract.getParties().get(i)) ;
            i++ ;
        }
        setClauses(contract.getClauses()) ;
        id = getHashableData().toString() ;
    }
    
    public void setSigner(EthereumSigner ethereumSigner) {
		this.signer = ethereumSigner;
		
	}
    
    /************* STATUS / WISH ***********/
    @Override
    public Status getStatus(){
        return contract.getStatus();
    }
    @Override
    public void setStatus(Status s){
        contract.setStatus(s);
    }
    
    @Override
    public Wish getWish(){
        return contract.getWish();
    }
    @Override
    public void setWish(Wish w){
        contract.setWish(w);
    }
    
    /************* Abstract method implementation **********/
    
    @Override
    public boolean isFinalized() {
    	if (signer == null) {
            throw new NullPointerException("Signer not initialized yet");
        }
    	
        for (EthereumKey key : parties) {
            if (!signatures.containsKey(key)) {
                return false;
            }
        }
        
        for (EthereumSignature partSign : signatures.values()) {
            if (!signer.verify(new byte[0], partSign)) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void addSignature(EthereumKey k, EthereumSignature s) {
        if(k == null || !this.parties.contains(k)) {
            throw new RuntimeException("invalid key");
        }
        signatures.put(k, s);
        contract.getSignatures().put(this.partiesId.get(k), s.toString());
        
        if (this.isFinalized()) {
        	this.setStatus(Status.FINALIZED);
        }
    }
    
    @Override
    public boolean checkContrat(EstablisherContract<BigInteger, EthereumKey, EthereumSignature, EthereumSigner> contract) {
    	if (!this.equals(contract) && !this.isFinalized()) {
    		return false;
    	}
    	
        setStatus(Status.FINALIZED);
        
        /**
         * TODO Logging of System.out.println("\n[CONTRACT FINALIZED]\n");
         */
        
        return true;
    }
    
    @Override
    public boolean equals(EstablisherContract<BigInteger, EthereumKey, EthereumSignature, EthereumSigner> c) {
        if (!(c instanceof BlockChainContract))
            return false;
        BlockChainContract contract = (BlockChainContract) c;
        if (contract.clauses == null)
            return false;
        return Arrays.areEqual(this.getHashableData(), contract.getHashableData());
    }
    
    @Override
    public byte[] getHashableData() {
    	String hashParties = parties.toString() ;
        String hashClauses = clauses.toString() ;
        String concat = hashParties + hashClauses + date.toString() ;
        return new SHA256Hasher().getHash(concat.getBytes()) ;
    }
    
    @Override
    public EthereumSignature sign(EthereumSigner signer, EthereumKey k) {
    	setStatus(Status.SIGNING);
        EthereumSignature signature = signer.sign(new byte[0]);

        /**
         * TODO Logging of System.out.println("\n\n[Signature done] : " + k.toString() + "\n\n"); 
         */

        if (signature == null) {
            throw new NullPointerException("Signature of " + k.getPublicKey() + " impossible");
        }
        addSignature(k, signature);
        
        return signature;
    }
}
