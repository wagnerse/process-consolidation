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
		"ReceiveIsOnEvent", // OnEvent is receiving Activity --C
		"ReceiveInOnAlarm", // Receive within OnAlarm --C
		// FIXME: // OnAlarm with sync communication
		"ReceiveInOnAlarmRepeatEvery", // OnAlarm with <repeatEvery>-Tag
		"ReceiveIsAndInOnEvent", // OnEvent with Receive in EH-Logic-Scope --C
		"SyncReplyInOnEvent", // OnEvent with sync (reply) communication --C
		"ReceiveInOnEvent", // OnEvent containing <receive> with external Activation
		"ReceiveIs2OnEvent3p", // 2 OnEvent in same EH activated by 2 processes --C
		// FIXME: // 2 OnEvent in 2 Processes
		"ReceiveIn2OnAlarm2p", // 2 OnAlarm in same EH with receive from 1 process --C
		"ReceiveIn2OnAlarm3p", // 2 OnAlarm in same EH with receive from 2 processes --C
		"ReceiveInOAandOE3p", // OnAlarm and OnEvent in same EH with receive from 2 processes --C
		"ReceiveInOAandOE2p", // OnAlarm and OnEvent in same EH with receive from 1 process --C
		"ReceiveIs2OnEvent2p", // 2 OnEvent in same EH activated by 1 process --C
		
		// FIXME: //EH2EH Files
		
		"ProcessHasEH", // <process> has attached EH --C
		};
	
	// select scenario here. Applies also for result check class CheckEHMergeResult
	static String scenario = scenarios[12];
	
	
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

