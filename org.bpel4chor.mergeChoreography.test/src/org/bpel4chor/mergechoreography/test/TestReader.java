package org.bpel4chor.mergechoreography.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.bpel4chor.model.topology.impl.Topology;
import org.bpel4chor.utils.BPEL4ChorReader;
import org.bpel4chor.utils.BPEL4ChorWriter;

public class TestReader {
	
	public static void main(String[] args) {
		
		String file1 = "D:\\Arbeit\\Diplom\\Git\\chorsam\\src\\org.bpel4chor.splitProcess.test\\files\\OrderInfo\\bpelContent\\OrderingProcess.bpel";
		String file2 = "D:\\Arbeit\\Diplom\\tmpout\\bpel4chor\\1350201138439\\participant1.pbd";
		String file3 = "D:\\Arbeit\\Diplom\\tmpout\\testChoreos\\AsyncPattern4u10Flow2Flow\\topology.xml";
		//
		// Process process = BPEL4ChorReader.readBPEL(file2);
		
		XMLInputFactory factory = XMLInputFactory.newInstance();
		FileInputStream fis = null;
		File file = new File(file3);
		Topology top = null;
		
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		top = BPEL4ChorReader.readTopology(fis);
		try {
			BPEL4ChorWriter.writeTopology(top, System.out);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// System.out.println("" + top);
		
	}
}
