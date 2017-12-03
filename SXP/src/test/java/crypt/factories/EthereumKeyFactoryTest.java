package protocol.impl.blockChain;

import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;

public class EthereumKeyFactoryTest {

	@Test
    public void test() {
    	EthereumKey account = EthereumKeyFactory();

    	Assert.assertNotNull(account.getPublicKey());
    	System.out.println("Public key : " + account.getPublicKey());

    	Assert.assertNotNull(account.getPrivateKey());
    	System.out.println("Private key : " + account.getPrivateKey());

    	Assert.assertNotNull(account.getAddress());
    	System.out.println("Address : " + account.getAddress());
    }
}
