package org.bpel4chor.mergechoreography;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.exceptions.NoApplicableMatcherFoundException;
import org.bpel4chor.mergechoreography.exceptions.PBDNotFoundException;
import org.bpel4chor.mergechoreography.matcher.communication.CommunicationMatcher;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.bpel4chor.utils.BPEL4ChorWriter;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.resource.BPELResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.wst.wsdl.internal.util.WSDLResourceFactoryImpl;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;

/**
 * The BPEL Choreography Merger read in a BPEL4Chor Choreography, merges the
 * processes and then generates BPEL and the associated WSDL.
 * 
 * @since Aug 1, 2012
 * @author Peter Debicki
 */
@SuppressWarnings("restriction")
public class ChoreographyMerger implements Serializable {
	
	private static final long serialVersionUID = -6483525635701914879L;
	
	/** The Choreography Package */
	private ChoreographyPackage choreographyPackage;
	
	private Logger log;
	
	static {
		// setup the extension to factory map, so that the proper
		// ResourceFactory can be used to read the file.
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("bpel", new BPELResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("wsdl", new WSDLResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xsd", new XSDResourceFactoryImpl());
		
	}
	
	
	public ChoreographyMerger(String fileName) {
		
		this.log = Logger.getLogger(ChoreographyMerger.class.getPackage().getName());
		
		// Here we get the read in Choreo
		this.choreographyPackage = new ChoreographyPackage(fileName);
	}
	
	/**
	 * The main merge method, returns new merged BPEL Process (executable not
	 * abstract!) made from the given choreography
	 * 
	 * @param fileName The name under which the new Process.zip should be saved
	 * @return ZipFile contianing the new merged BPEL Process incl. WSDLs
	 */
	public ZipFile merge(String fileName, String leadingProcName) {
		// Create New initial executable BPEL Process and
		// provision all vars and activities into it
		try {
			this.mergeProcess(leadingProcName);
		} catch (PBDNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			FileOutputStream outputStream = new FileOutputStream(new File(fileName));
			BPEL4ChorWriter.writeBPEL(this.choreographyPackage.getMergedProcess(), outputStream);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// TODO: Später fertig coden
		ZipFile temp = null;
		return temp;
	}
	
	/**
	 * Method for initialising the new merged BPEL process
	 * 
	 * @param leadingProcName The name of the leading PBD
	 * @throws PBDNotFoundException
	 */
	private void mergeProcess(String leadingProcName) throws PBDNotFoundException {
		// First find the given process in the PBD List
		Process leadProcess = this.choreographyPackage.getPBDbyProcessName(leadingProcName);
		
		if (leadProcess == null) {
			throw new PBDNotFoundException("The PBD with name " + leadingProcName + " was not found !!");
		}
		
		// Iinitialize the merged BPEL Process
		this.choreographyPackage.initMergedProcess(leadProcess);
		
		// Now check the MessageLinks and replace invoke/receive through
		// matching assigns
		// TODO: Merge wenig Transformation
		for (MessageLink link : this.choreographyPackage.getTopology().getMessageLinks()) {
			CommunicationMatcher matcher = new CommunicationMatcher();
			try {
				if (!this.choreographyPackage.isLinkVisited(link)) {
					CommunicationPattern pattern = matcher.match(link, this.choreographyPackage);
					pattern.merge();
				}
				// this.log.log(Level.INFO, " CommunicationPattern " + pattern +
				// " found !!");
			} catch (NoApplicableMatcherFoundException e) {
				e.printStackTrace();
			}
		}
		
		// TODO: Korrektur Patterns !!!
	}
	
	public ChoreographyPackage getChoreographyPackage() {
		return this.choreographyPackage;
	}
	
	public void setChoreographyPackage(ChoreographyPackage choreographyPackage) {
		this.choreographyPackage = choreographyPackage;
	}
	
}
