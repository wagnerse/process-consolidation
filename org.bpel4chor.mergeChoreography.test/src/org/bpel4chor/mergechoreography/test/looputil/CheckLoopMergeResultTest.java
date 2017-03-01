package org.bpel4chor.mergechoreography.test.looputil;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import org.bpel4chor.mergechoreography.test.util.Constants;
import org.bpel4chor.mergechoreography.tester.CheckMergeResult;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for CheckLoopMergeResultTest
 * 
 * Copyright 2013 IAAS University of Stuttgart 
 * 
 * @author Shruthi V Kukillaya
 * 
 */

public class CheckLoopMergeResultTest {
	private String checkscenario = ChoreographyMergerLoopTest.scenario;
	private String pathToMergedLoop = Constants.mergeOutputPath + File.separator + checkscenario + File.separator + "ProcessMerged.bpel";
	private String pathToCheckLoop = Constants.pathToCheckLoopFiles + File.separator + checkscenario +".bpel";
	
	
	@Test
	public void checkMergeResult() {
		FileSystem fs = FileSystems.getDefault();
		boolean result = new CheckMergeResult(fs.getPath(pathToMergedLoop), fs.getPath(pathToCheckLoop)).isProcessCorrect();
		Assert.assertTrue(result);
	}
}
