package org.bpel4chor.mergechoreography.test;

import org.bpel4chor.mergechoreography.test.choreographyMerger.ChoreographyMergerTest;
import org.bpel4chor.mergechoreography.test.choreographyPackage.ChoreoraphyPackageInitTest;
import org.bpel4chor.mergechoreography.test.util.XPathUtilTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ChoreoraphyPackageInitTest.class, ChoreographyMergerTest.class, XPathUtilTest.class})
public class AllTests {
	
}
