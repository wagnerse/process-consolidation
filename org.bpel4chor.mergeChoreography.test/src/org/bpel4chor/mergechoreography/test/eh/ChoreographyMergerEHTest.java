package org.bpel4chor.mergechoreography.test.eh;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bpel4chor.mergechoreography.ChoreographyMerger;
import org.bpel4chor.mergechoreography.test.util.Constants;
import org.bpel4chor.mergechoreography.test.util.ZipFilenameFilter;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for ChoreographyMergerEHTest.<br>
 * <br>
 * Copyright 2014 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Aleksandar Milutinovic
 * 
 */


public class ChoreographyMergerEHTest {

	// path to zip file, new dir name
	private static Map<String, String> patterns = new HashMap<>();
	
	private static String outputPath = Constants.mergeOutputPath;
	
	// put all scenarios 
	private static String[] scenarios = {
		"ReceiveIsOnEvent",				//  0: OnEvent is receiving Activity
		"ReceiveInOnAlarm", 			//  1: Receive within OnAlarm
		"SyncInOnAlarm", 				//  2: OnAlarm with sync communication 
		"ReceiveInOnAlarmRepeatEvery", 	//  3: OnAlarm with <repeatEvery>-Tag
		"ReceiveIsAndInOnEvent", 		//  4: OnEvent with Receive in EH-Logic-Scope 
		"SyncReplyInOnEvent", 			//  5: OnEvent with sync (reply) communication 
		"ReceiveInOnEvent",				//  6: OnEvent containing <receive> with external Activation
		"ReceiveIs2OnEvent3p", 			//  7: 2 OnEvent in same EH activated by 2 processes 
		"ReceiveIs2diffOnEvent3p", 		//  8: 2 OnEvent in 2 Processes
		"ReceiveIs2diffOnAlarm3p",		//  9: 2 OnAlarm in 2 Processes
		"ReceiveIn2OnAlarm2p", 			// 10: 2 OnAlarm in same EH with receive from 1 process 
		"ReceiveIn2OnAlarm3p", 			// 11: 2 OnAlarm in same EH with receive from 2 processes 
		"ReceiveInOAandOE3p", 			// 12: OnAlarm and OnEvent in same EH with receive from 2 processes 
		"ReceiveInOAandOE2p", 			// 13: OnAlarm and OnEvent in same EH with receive from 1 process
		"ReceiveIs2OnEvent2p", 			// 14: 2 OnEvent in same EH activated by 1 process 
		"OnAlarm2OnEvent",				// 15: OnAlarm calls OnEvent
		"OnAlarm2OnAlarm",				// 16: OnAlarm sends to another OnAlarm
		"NestedExternalOnEvent",		// 17: OnEvent calls OnEvent, but initial OnEvent is not in Choreo
		"NestedOnEvent", 				// 18: 3 OnEvent, nested first EH calls second EH	
		"ProcessHasEH", 				// 19: <process> has attached EH
		};
	
	// scenarios are listed in file "scenarios.pptx"
	
	// select scenario here. Applies also for result check class CheckEHMergeResult
	static String scenario = scenarios[16];
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// ############# Generate all files.
		//initAllFiles();
		// ############# Generate single file.
		initSingleFile(scenario);
	}
	
	private static void initSingleFile(String change) {
		patterns.put(Constants.pathToEHZipFiles + File.separator  + change + ".zip", change);
	}
	
	private static void initAllFiles() {
		// reads all zips and put it into patterns
		File[] fileList = new File(Constants.pathToEHZipFiles).listFiles(new ZipFilenameFilter());
		for (File f : fileList)
			patterns.put(f.getAbsolutePath(), f.getName().substring(0, f.getName().length() - 4));
	}
	
	@Test
	public void test() {
		ChoreographyMerger choreographyMerger = null;
		for (Map.Entry<String, String> entry : patterns.entrySet()) {
			// create directory
			(new File(outputPath + File.separator + entry.getValue())).mkdir();
			// start merge
			choreographyMerger = new ChoreographyMerger(entry.getKey());
			choreographyMerger.merge(outputPath + File.separator + entry.getValue() + File.separator);
		}
	}
}

