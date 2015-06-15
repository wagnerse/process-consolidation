package org.bpel4chor.mergechoreography.test.eh;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bpel4chor.mergechoreography.ChoreographyMerger;
import org.bpel4chor.mergechoreography.test.util.Constants;
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
	private static String[] scenarios = {"ReceiveInOnEvent", "ReceiveInOnAlarmTest", "ReceiveInOnAlarmTest2", "RisOnEvent", "ReceiveInOnAlarm" , "ReceiveIn2OnAlarm" , "ReceiveIn2OnAlarmv2" , "ReceiveIn2OnAlarm3p"};
	
	// select scenario here. Applies also for result check class CheckEHMergeResukt
	static String scenario = scenarios[7];
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// ############# Generate all files.
		// initAllFiles();
		// ############# Generate single file.
		initSingleFile(scenario);
	}
	
	private static void initSingleFile(String change) {
		patterns.put(Constants.pathToEHZipFiles + File.separator  + change + ".zip", "Szenario: " + change);
	}
	
	private static void initAllFiles() {
		// reads all zips and put it into patterns
		File[] fileList = new File(Constants.pathToEHZipFiles).listFiles();
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

