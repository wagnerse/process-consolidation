package org.bpel4chor.mergechoreography.test.looputil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bpel4chor.mergechoreography.ChoreographyMerger;
import org.bpel4chor.mergechoreography.test.util.Constants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for ChoreographyMergerLoopTest
 * 
 * Copyright 2014 IAAS University of Stuttgart
 * 
 * @author Shruthi V Kukillaya
 * 
 */

public class ChoreographyMergerLoopTest {
	
	private static Map<String, String> patterns = new HashMap<>();
	
	private static String outputPath = Constants.mergeOutputPath;
	
	// put all scenarios 
	private static String[] scenarios = {
		"StaticLoop",						//  0: <while> loop having counter value at design time
		"StaticLoop3Participants", 			//  1: <while> loop amongst 3 participants
		"DynamicLoop", 						//  2: <while> loop having counter value at run time
		"OneToManySend", 					//  3: <forEach> loop following a One-to-many-Send service interaction pattern
		"OneFromManyReceive", 				//  4: <forEach> loop following a One-from-many-Receive service interaction pattern
		"OneToManySendReceive", 			//  5: <forEach> loop following a One-to-many-Send/Receive service interaction pattern
		"StaticLoopwso2",					//  6: <while> loop having counter value at design time
		};
		
	// scenarios are listed in file "scenarios.pptx"
	
	// select scenario here. Applies also for result check class CheckLoopMergeResult
	static String scenario = scenarios[6];
		
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ChoreographyMergerLoopTest.patterns.put(Constants.pathToLoopZipFiles + scenario + ".zip", scenario);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	public void test(){
		for (Map.Entry<String, String> entry : ChoreographyMergerLoopTest.patterns.entrySet()) {
			boolean success = (new File(ChoreographyMergerLoopTest.outputPath + java.io.File.separator + entry.getValue())).mkdir();
			ChoreographyMerger choreographyMerger = new ChoreographyMerger(entry.getKey());
			choreographyMerger.merge(ChoreographyMergerLoopTest.outputPath + java.io.File.separator + entry.getValue() + java.io.File.separator);
		}
	}
}
