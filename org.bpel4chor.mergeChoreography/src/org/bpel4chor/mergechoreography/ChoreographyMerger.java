package org.bpel4chor.mergechoreography;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.matcher.communication.CommunicationMatcher;
import org.bpel4chor.mergechoreography.pattern.MergePattern;
import org.bpel4chor.mergechoreography.util.ChoreoMergeUtil;
import org.bpel4chor.mergechoreography.util.PBDFragmentDuplicator;
import org.bpel4chor.mergechoreography.util.MergePostProcessor;
import org.bpel4chor.mergechoreography.util.MergePreProcessor;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.bpel4chor.model.topology.impl.Participant;
import org.bpel4chor.model.topology.impl.ParticipantType;
import org.bpel4chor.utils.BPEL4ChorUtil;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.CatchAll;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.If;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.PartnerActivity;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Sources;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Targets;
import org.eclipse.bpel.model.partnerlinktype.PartnerLinkType;
import org.eclipse.bpel.model.partnerlinktype.Role;
import org.eclipse.bpel.model.resource.BPELResourceFactoryImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.wst.wsdl.Definition;
import org.eclipse.wst.wsdl.Operation;
import org.eclipse.wst.wsdl.PortType;
import org.eclipse.wst.wsdl.internal.util.WSDLResourceFactoryImpl;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;

import de.uni_stuttgart.iaas.bpel.model.utilities.FragmentDuplicator;
import de.uni_stuttgart.iaas.bpel.model.utilities.MyWSDLUtil;

/**
 * The BPEL Choreography Merger read in a BPEL4Chor Choreography, merges the
 * processes and then generates BPEL and the associated WSDL Files.
 * 
 * @since Aug 1, 2012
 * @author Peter Debicki
 */
@SuppressWarnings("restriction")
public class ChoreographyMerger implements Serializable {
	
	private static final long serialVersionUID = -6483525635701914879L;
	
	/** The Choreography Package */
	private ChoreographyPackage choreographyPackage;
	public ChoreographyMergerExtension choreographyMergerExtension;
	public ChoreographyPackageExtension choreographyPackageExtension;


	
	protected Logger log = Logger.getLogger(this.getClass().getPackage().getName());
	
	static {
		// setup the extension to factory map, so that the proper
		// ResourceFactory can be used to read the file.
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("pbd", new BPELResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("bpel", new BPELResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("wsdl", new WSDLResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xsd", new XSDResourceFactoryImpl());
		
	}
	
	
	/**
	 * Constructor for ChoreographyMerger
	 * 
	 * @param fileName The Zip-File containing the BPEL4Chor-Choreography
	 */
	public ChoreographyMerger(String fileName) {
		
		// Here we go the read in Choreo
		this.choreographyPackage = new ChoreographyPackage(fileName);

		/**
		 * TODO Added code here
		 */
		choreographyMergerExtension = new ChoreographyMergerExtension(
				choreographyPackage, log);
		choreographyPackageExtension = new ChoreographyPackageExtension(
				choreographyPackage);
		choreographyPackage
				.setChoreographyPackageExtension(choreographyPackageExtension);
		choreographyPackage
				.setChoreographyMergerExtension(choreographyMergerExtension);
	}
	
	/**
	 * The main merge method, returns new merged BPEL Process (executable not
	 * abstract!) made from the given choreography
	 * 
	 * @param fileName The name under which the new Process.zip should be saved
	 * @return ZipFile containing the new merged BPEL Process incl. WSDLs
	 */
	public ZipFile merge(String fileName) {
		// PreProcessing PBDs (alternative invokes)
		MergePreProcessor.startPreProcessing(this.choreographyPackage);
		// Create New initial executable BPEL Process and
		// copy all vars and activities into it
		this.mergeChoreography();
		// PostProcessing of the merged process
		MergePostProcessor.startPostProcessing(this.choreographyPackage);
		this.choreographyPackage.saveMergedChoreography(fileName);
		
		ZipFile temp = null;
		return temp;
	}
	
	/**
	 * Method for initializing and merging the new merged BPEL process
	 * 
	 */
	private void mergeChoreography() {
		// Iinitialize the merged BPEL Process
		this.choreographyPackage.initMergedProcess();
		
		// Now check the MessageLinks and merge
		CommunicationMatcher matcher = new CommunicationMatcher();
		for (MessageLink link : this.choreographyPackage.getTopology().getMessageLinks()) {
			if (!this.choreographyPackage.isLinkVisited(link)) {
				MergePattern pattern = matcher.match(link, this.choreographyPackage);
				// if pattern==null we have a reply-Link or a
				// Non-Mergeable-Message-Link
				if (pattern != null) {
					pattern.merge();
				}
			}
		}

		// After merging configure remaining communication activities from
		// NMML

		choreographyMergerExtension.configureNMMLActivities2();
		if (choreographyMergerExtension.createdNewPartnerLinksForNMML)
			choreographyMergerExtension
					.copyNewPartnerLinksOfNMMLToForEachScope();
		choreographyMergerExtension.handleSeveralCreateInstanceYesCases();
		choreographyMergerExtension.updateScopeNames();
		choreographyMergerExtension.addNewScopesToMap();
		choreographyMergerExtension.configureNMMLActivities2();
		if (choreographyMergerExtension.createdNewPartnerLinksForNMML)
			choreographyMergerExtension
					.copyNewPartnerLinksOfNMMLToForEachScope();

		try {
			choreographyMergerExtension.updateScopeNames();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		// After merging configure remaining communication activities from NMML
		choreographyMergerExtension.performLoopFragmentation();
	}
	
	/**
	 * Get {@link ChoreographyPackage}
	 * 
	 * @return {@link ChoreographyPackage}
	 */
	public ChoreographyPackage getChoreographyPackage() {
		return this.choreographyPackage;
	}
	
}
