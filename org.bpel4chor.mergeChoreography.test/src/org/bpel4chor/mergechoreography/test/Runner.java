package org.bpel4chor.mergechoreography.test;

import java.io.IOException;

import org.bpel4chor.mergechoreography.ChoreographyMerger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.test.util.Constants;
import org.bpel4chor.utils.BPEL4ChorWriter;
import org.eclipse.bpel.model.Process;

public class Runner {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ChoreographyMerger choreographyMerger1 = new ChoreographyMerger(Constants.asyncPattern2u4Flow2FlowFHChoreo);
		choreographyMerger1.merge("D:\\Arbeit\\Diplom\\tmpout\\testChoreos\\testoutput.bpel", "Process1");
		
		ChoreographyPackage package1 = choreographyMerger1.getChoreographyPackage();
		
		try {
			Process process = package1.getMergedProcess();
			if (process != null) {
				BPEL4ChorWriter.writeBPEL(package1.getMergedProcess(), System.out);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// choreographyMerger1.merge("D:\\Arbeit\\Diplom\\tmpout\\testChoreos\\testoutput.bpel",
		// "Process1");
		
	}
}
