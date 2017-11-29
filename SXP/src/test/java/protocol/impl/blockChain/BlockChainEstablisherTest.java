package protocol.impl.blockChain;

import controller.Application;
import crypt.api.hashs.Hasher;
import crypt.factories.EthereumKeyFactory;
import crypt.factories.HasherFactory;
import model.api.SyncManager;
import model.entity.ContractEntity;
import model.entity.EthereumKey;
import model.entity.User;
import model.syncManager.UserSyncManagerImpl;
import org.junit.AfterClass;
import org.junit.Test;

import protocol.impl.BlockChainEstablisher;
import util.TestInputGenerator;
import util.TestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class BlockChainEstablisherTest {

    public static Application application;
    public static final int restPort = 8081;

    public static final int N = 2;

    private ContractEntity[] contractEntity = new ContractEntity[N] ;
    private BlockChainContract bcContractA, bcContractB;

    @SuppressWarnings("unused")
	private ArrayList<String> setEntityContract(String... entity) {
        ArrayList<String> newEntities = new ArrayList<>();
        for (String tmp : entity) {
            newEntities.add(tmp);
        }
        return newEntities;
    }

    @AfterClass
    static public void deleteBaseAndPeer(){
        TestUtils.removeRecursively(new File(".db-" + restPort + "/"));
        TestUtils.removePeerCache();
        application.stop();
    }


    @Test
    public void Test() throws Exception {

        if (Application.getInstance() == null) {
            application = new Application();
            Application.getInstance().runForTests(restPort);
        }

        //Add Ethereum Account
        //EthereumKey keys0 = EthereumKeyFactory.createFromHex("287fc6941394e06872850966e20fe190ad43b3d0a3caa82e42cd077a6aaeb8b5", "0f3bCE1d0d5bf08310Ca3965260b6D0AE3E5b06F");
        //EthereumKey keys1 = EthereumKeyFactory.createFromHex("c41bfd554363e4c8bf221dc1a1353d858c279a4cd460ec4e2f3f40866a2e416f", "e64CF76ECF2c4fCfDf5578ABD069eBece054465C");
        EthereumKey keys0 = EthereumKeyFactory.createFromKeystore(
        		"/home/swa/.ethereum/devnet/keystore/UTC--2017-11-29T02-40-12.552996941Z--0862943ea786e41fb1ae02cf7f93591d388eedc4",
        		"");
        EthereumKey keys1 = EthereumKeyFactory.createFromKeystore(
        		"/home/swa/.ethereum/devnet/keystore/UTC--2017-11-29T02-41-42.424429893Z--0245f1e7c45698e3c7961eed302b80ec69d066ed",
        		"test");

        // Creating the users
        User[] users = new User[N];
        ArrayList<String> parties = new ArrayList<>() ;
        for (int i=0; i<N; i++) {
            String login  = TestInputGenerator.getRandomAlphaWord(20);
            String password = TestInputGenerator.getRandomPwd(20);

            users[i] = new User();
            users[i].setNick(login);
            Hasher hasher = HasherFactory.createDefaultHasher();
            users[i].setSalt(HasherFactory.generateSalt());
            hasher.setSalt(users[i].getSalt());
            users[i].setPasswordHash(hasher.getHash(password.getBytes()));
            users[i].setCreatedAt(new Date());
            //Attribute EthKeys
            if (i==0)
                users[i].setEthereumKey(keys0);
            else
                users[i].setEthereumKey(keys1);

            SyncManager<User> em = new UserSyncManagerImpl();
            em.begin();
            em.persist(users[i]);
            em.end();

            parties.add(users[i].getId()) ;
        }

        ///////////////////////////////
        //Add Entities in Contracts Entity
        for (int i=0 ; i<N ; i++){
            contractEntity[i] = new ContractEntity() ;
            contractEntity[i].setParties(parties);
            System.out.println("USERS : " + contractEntity[i].getParties().toString());

            ArrayList<String> clauses = new ArrayList<>();
            clauses.add(users[0].getId() + " troc item1 with " + users[1].getId());
            clauses.add(users[1].getId() + " troc item2 with " + users[0].getId());
            contractEntity[i].setClauses(clauses);

            contractEntity[i].setCreatedAt(new Date());
        }
        //End Add Entities
        ///////////////////////////////


        ArrayList<EthereumKey> partis = new ArrayList<>();
        partis.add(users[0].getEthereumKey());
        partis.add(users[1].getEthereumKey());


        // Map of URIS
        HashMap<EthereumKey, String> uris = new HashMap<>() ;
        String uri = Application.getInstance().getPeer().getUri();
        for (int k=0; k<N; k++){
            EthereumKey key = new EthereumKey() ;
            key.setPublicKey(users[k].getEthereumKey().getPublicKey()) ;
            uris.put(key, uri);
        }

        bcContractA = new BlockChainContract(contractEntity[0], partis);
        bcContractB = new BlockChainContract(contractEntity[1], partis);


        BlockChainEstablisher bcEstablisherA = new BlockChainEstablisher(users[0], uris, "127.0.0.1", "8545");
        BlockChainEstablisher bcEstablisherB = new BlockChainEstablisher(users[1], uris, "127.0.0.1", "8545");

        bcEstablisherA.initialize(bcContractA, true);

        sleep(2000);

        bcEstablisherB.initialize(bcContractB, false);

        sleep(2000);

        bcEstablisherA.start() ;

        //Time to sendContractAddr and set it
        sleep(2000);

        bcEstablisherA.sign(bcContractB) ;

        sleep(30000);

        bcEstablisherB.sign(bcContractA) ;

        //time to EstablisherA check if finalized when EstablisherB share Tx Signature
        sleep(300000);

        System.out.println("\n\n[Entity<A> Final State] :");
        for (String sign : contractEntity[0].getSignatures().keySet())
            System.out.println("\t" + sign) ;

        System.out.println("\n\n[Entity<B> Final State] :");
        for (String sign : contractEntity[1].getSignatures().keySet())
            System.out.println("\t" + sign) ;

    }

    public void sleep(int i) {
        try{
            Thread.sleep(i);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
