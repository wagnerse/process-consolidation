package org.bpel4chor.mergechoreography.test.choreographyPackage;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.test.util.Constants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ChoreohraphyPackageInitTest {
	
	private static List<String> patterns = new ArrayList<>();
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern11Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern11InvFHCHChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern12Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern13Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern14Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern15Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern15BranchSuccRChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern16Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern17Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern21Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern222SendDiffVarsChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern222SendSameVarChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern23Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern23CreateInstanceChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern30RinEHChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern30RinWhileChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern30RisEHChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern30SinCHChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.asyncPattern30SinWhileChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern11Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern12Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern13Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern14Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern14WithFaultChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern15CHChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern15EHChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern15FHChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern15THChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern21Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern22Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern23Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern24Choreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern30RisEHChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern30RisPickInFHChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern30SinEHChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern30SinWhileChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern30SinWhileMultiReplyChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern30SinWhileMultiReplyWSDLChoreo);
		ChoreohraphyPackageInitTest.patterns.add(Constants.syncPattern30SRinFCTEChoreo);
		
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	public void testChoreographyPackageInit() {
		for (String choreoName : ChoreohraphyPackageInitTest.patterns) {
			ChoreographyPackage choreoPkg = new ChoreographyPackage(choreoName);
			Assert.assertNotNull(choreoPkg.getGrounding());
			Assert.assertNotNull(choreoPkg.getTopology());
			Assert.assertEquals(choreoPkg.getPbds().size(), choreoPkg.getWsdls().size());
			Assert.assertEquals(choreoPkg.getPbd2wsdl().size(), choreoPkg.getPbds().size());
			Assert.assertEquals(choreoPkg.getPbd2wsdl().size(), choreoPkg.getWsdls().size());
		}
		
	}
	
}
