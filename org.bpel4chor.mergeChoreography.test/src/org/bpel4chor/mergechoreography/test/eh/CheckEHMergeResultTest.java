package org.bpel4chor.mergechoreography.test.eh;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import org.bpel4chor.mergechoreography.test.util.Constants;
import org.bpel4chor.mergechoreography.tester.CheckMergeResult;
import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for CheckEHMergeResult.<br>
 * <br>
 * Copyright 2014 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Aleksandar Milutinovic
 * 
 */
public class CheckEHMergeResultTest {
	// get testcase from class ChoreographyMergerEHTest
	private String checkszenario = ChoreographyMergerEHTest.scenario;
	private String pathToMergedEHProcess = Constants.mergeOutputPath + File.separator + checkszenario + File.separator + "ProcessMerged.bpel";
	private String pathToCheckEHProcess = Constants.pathToCheckEHFiles + File.separator + checkszenario +".bpel";
	
	
	@Test
	public void checkMergeResult() {
		FileSystem fs = FileSystems.getDefault();
		boolean result = new CheckMergeResult(fs.getPath(pathToMergedEHProcess), fs.getPath(pathToCheckEHProcess)).isProcessCorrect();
		Assert.assertTrue(result);
	}
}
