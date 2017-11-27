package protocol.impl.blockChain;

import com.fasterxml.jackson.core.type.TypeReference;
import controller.Users;
import controller.tools.JsonTools;
import crypt.api.signatures.Signer;
import crypt.impl.hashs.SHA256Hasher;
import crypt.impl.signatures.EthereumSignature;
import crypt.impl.signatures.EthereumSigner;
import model.api.Status;
import model.api.EstablisherType;
import model.entity.ContractEntity;
import model.entity.EthereumKey;
import model.entity.User;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import protocol.api.EstablisherContract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class EthereumContract extends EstablisherContract<BigInteger, EthereumKey, EthereumSignature, EthereumSigner>{
    
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
    public EthereumContract(){
        super();
        this.signer = new EthereumSigner();
        this.contract = new ContractEntity();
        contract.setClauses(new ArrayList<String>());
        contract.setParties(new ArrayList<String>());
        contract.setSignatures(new HashMap<String,String>());
        contract.setEstablisherType(EstablisherType.Ethereum);
    }
    
    // Constructor from clauses (problem when resolve, because no partiesId set)
    public EthereumContract(ArrayList<String> clauses){
        super();
        this.signer = new EthereumSigner();
        this.contract = new ContractEntity();
        this.setClauses(clauses);
        this.contract.setParties(new ArrayList<String>());
        this.contract.setSignatures(new HashMap<String,String>());
        this.contract.setEstablisherType(EstablisherType.Ethereum);
    }
    
    // Constructor from a ContractEntity (what will be most used)
    public EthereumContract(ContractEntity c){
        super();
        this.contract = c;
        this.signer = new EthereumSigner();
        this.setClauses(contract.getClauses());
        this.setParties(contract.getParties());
        this.contract.setEstablisherType(EstablisherType.Ethereum);
    }

    /************* GETTERS ***********/
    public ArrayList<String> getClauses(){
        return clauses;
    }
    public ArrayList<EthereumKey> getParties(){
        return parties;
    }
    public EthereumKey getTrentKey(){
        return signer.getTrentK();
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
            this.parties.add(user.getKey());
            this.partiesId.put(user.getKey(), user.getId());
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

    /**
     * Set Trent key and store it into Establishement data
     */
    public void setTrentKey (EthereumKey k){
        signer.setTrentK(k);
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
        boolean result = false;
        
        if (this.getTrentKey() == null){
            return false;}
        
        
        for(EthereumKey k: parties) {
            signer.setReceiverK(k);
            if(signatures.get(k) == null){
                return false;
            }
            
            byte[] data = (new String(this.getHashableData())).getBytes();
            if (signer.verify(data, signatures.get(k)))
                return true;
            
            for (int round = 1; round < parties.size() + 2; round++){
                data = (new String(this.getHashableData()) + round).getBytes();
                if (signer.verify(data, signatures.get(k))){
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void addSignature(EthereumKey k, EthereumSignature s) {
        if(k == null || !this.parties.contains(k)) {
            throw new RuntimeException("invalid key");
        }
        signatures.put(k, s);
        contract.getSignatures().put(this.partiesId.get(k), s.toString());
        
        if (this.isFinalized())
            this.setStatus(Status.FINALIZED);
    }
    
    @Override
    public boolean checkContrat(EstablisherContract<BigInteger, EthereumKey, EthereumSignature, EthereumSigner> contract) {
        return this.equals(contract) && this.isFinalized();
    }
    
    @Override
    public boolean equals(EstablisherContract<BigInteger, EthereumKey, EthereumSignature, EthereumSigner> c) {
        if (!(c instanceof EthereumContract))
            return false;
        EthereumContract contract = (EthereumContract) c;
        if (contract.clauses == null)
            return false;
        return Arrays.areEqual(this.getHashableData(), contract.getHashableData());
    }
    
    @Override
    public byte[] getHashableData() {
        BigInteger sum = BigInteger.ZERO;
        for(EthereumKey k: parties) {
            sum = sum.add(k.getPublicKey());
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(sum.toString());
        byte[] signable = this.clauses.getHashableData();
        
        int signableL = signable.length;
        int bufferL = buffer.toString().getBytes().length;
        byte[] concate = new byte[signableL + bufferL];
        System.arraycopy(new String(buffer).getBytes(), 0, concate, 0, bufferL);
        System.arraycopy(signable, 0, concate, bufferL, signableL);
        
        return concate;
    }
    
    @Override
    public EthereumSignature sign(EthereumSigner signer, EthereumKey k) {
        signer.setKey(k);
        return signer.sign(this.getHashableData());
    }
}
