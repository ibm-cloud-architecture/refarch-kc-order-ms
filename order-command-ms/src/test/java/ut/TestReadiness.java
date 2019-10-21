package ut;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ibm.gse.orderms.app.StarterReadinessCheck;


public class TestReadiness {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testAppIsReady() {
		StarterReadinessCheck readiness = new StarterReadinessCheck();
		boolean ready =  readiness.isReady();
		Assert.assertTrue(ready );
	}

}
