package org.bpel4chor.mergechoreography.test;

import java.io.File;

import org.apache.log4j.Logger;

public class UtilTester {
	
	private static Logger log = Logger.getLogger(UtilTester.class);
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String testDir = "D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput";
		boolean success = (new File(testDir + java.io.File.separator + "testDir")).mkdir();
	}
}
