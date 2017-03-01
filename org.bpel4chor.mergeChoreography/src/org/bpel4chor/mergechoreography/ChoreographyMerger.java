package org.bpel4chor.mergechoreography;

import java.io.Serializable;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.matcher.communication.CommunicationMatcher;
import org.bpel4chor.mergechoreography.pattern.MergePattern;
import org.bpel4chor.mergechoreography.util.ChoreoMergeUtil;
import org.bpel4chor.mergechoreography.util.MergePostProcessor;
import org.bpel4chor.mergechoreography.util.MergePreProcessor;
import org.bpel4chor.mergechoreography.util.MergePreProcessorForEH;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.bpel4chor.model.topology.impl.Participant;
import org.bpel4chor.model.topology.impl.ParticipantType;
import org.bpel4chor.utils.BPEL4ChorUtil;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.PartnerActivity;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.partnerlinktype.PartnerLinkType;
import org.eclipse.bpel.model.partnerlinktype.Role;
import org.eclipse.bpel.model.resource.BPELResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.wst.wsdl.Definition;
import org.eclipse.wst.wsdl.Operation;
import org.eclipse.wst.wsdl.PortType;
import org.eclipse.wst.wsdl.internal.util.WSDLResourceFactoryImpl;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;

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
		
		// Debug: Check EH related Code
		MergePreProcessorForEH.startPreProcessing(this.choreographyPackage);
		
		for (String s : this.choreographyPackage.getOld2New().keySet()) {
			this.log.info("Link : " + s + " " + this.choreographyPackage.getOld2New().get(s));
		}
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
		
		// After merging configure remaining communication activities from NMML
		this.configureNMMLActivities();
	}
	
	/**
	 * Configure remaining communication activities from NMML
	 */
	private void configureNMMLActivities() {
		if (this.choreographyPackage.getNMML().size() > 0) {
			this.log.info("Following Message Links couldn't be merged : ");
			for (MessageLink ml : this.choreographyPackage.getNMML()) {
				
				// If sendActivity is <reply> skip the link, it will be
				// configured after the <receive>
				if (ChoreoMergeUtil.resolveActivity(ml.getSendActivity()) instanceof Reply) {
					continue;
				}
				
				// Grounding-Topology-WSDL-Informations
				this.log.info("ML : " + ml);
				org.bpel4chor.model.grounding.impl.MessageLink grndMl = BPEL4ChorUtil.resolveGroundingMessageLinkByName(this.choreographyPackage.getGrounding(), ml.getName());
				this.log.info("The corresponding Grounding Message Link is : " + grndMl);
				Participant sender = BPEL4ChorUtil.resolveParticipant(this.choreographyPackage.getTopology(), ml.getSender());
				Participant receiver = BPEL4ChorUtil.resolveParticipant(this.choreographyPackage.getTopology(), ml.getReceiver());
				this.log.info("Sender Participant is : " + sender);
				this.log.info("Receiver Participant is : " + receiver);
				ParticipantType sendPT = ChoreoMergeUtil.resolveParticipantType(this.choreographyPackage.getTopology(), sender);
				ParticipantType recPT = ChoreoMergeUtil.resolveParticipantType(this.choreographyPackage.getTopology(), receiver);
				this.log.info("Sender ParticipantType is : " + sendPT);
				this.log.info("Receiver ParticipantType is : " + recPT);
				Process sendPBD = this.choreographyPackage.getPBDByName(sendPT.getParticipantBehaviorDescription().getLocalPart());
				Process recPBD = this.choreographyPackage.getPBDByName(recPT.getParticipantBehaviorDescription().getLocalPart());
				this.log.info("Sender PBD is : " + sendPBD);
				this.log.info("Receiver PBD is : " + recPBD);
				Definition sendDef = this.choreographyPackage.getPbd2wsdl().get(sendPBD);
				Definition recDef = this.choreographyPackage.getPbd2wsdl().get(recPBD);
				this.log.info("Sender WSDL : " + sendDef);
				this.log.info("Receiver WSDL : " + recDef);
				PortType recPortType = MyWSDLUtil.findPortType(recDef, grndMl.getPortType().getLocalPart());
				// CHECK: again only one wsdl is permitted
				if (recPortType == null)
					for (Definition def : this.getChoreographyPackage().getWsdls()) {
						recPortType = MyWSDLUtil.findPortType(def, grndMl.getPortType().getLocalPart());
						if (recPortType != null)
							break;
					}
				this.log.info("Receiver PortType is : " + recPortType);
				Operation recOperation = MyWSDLUtil.resolveOperation(recDef, recPortType.getQName(), grndMl.getOperation());
				this.log.info("Receiver Operation is : " + recOperation);
				Role recPLRole = MyWSDLUtil.findPartnerLinkTypeRole(recDef, recPortType);
				// CHECK: again ...
				if (recPLRole == null)
					for (Definition def : this.getChoreographyPackage().getWsdls()) {
						recPLRole = MyWSDLUtil.findPartnerLinkTypeRole(def, recPortType);
						if (recPLRole != null)
							break;
					}
				this.log.info("PartnerLinkType-Role which supports PortType above is : " + recPLRole);
				
				// Technical Activity configuration
				BPELExtensibleElement sendAct = ChoreoMergeUtil.resolveActivity(ml.getSendActivity());
				BPELExtensibleElement recAct = ChoreoMergeUtil.resolveActivity(ml.getReceiveActivity());
				
				Invoke s = (Invoke) sendAct;
				BPELExtensibleElement r = null; // (Receive) recAct;
				boolean rIsOnEventOfProcess = false;
				if (recAct instanceof Receive) {
					r = recAct;
				} else if (recAct instanceof OnMessage) {
					r = (Activity) recAct.eContainer();
				} else if (recAct instanceof OnEvent) {
					if (recAct.eContainer() instanceof Process) {
						// If we have an <onEvent> of <process>
						r = (Process) recAct.eContainer().eContainer();
					} else {
						// If we have an <onEvent> of <scope>
						r = (Scope) recAct.eContainer().eContainer();
					}
					
				}
				Scope scpS = ChoreoMergeUtil.getHighestScopeOfActivity(s);
				
				BPELExtensibleElement scpR = null; // ChoreoMergeUtil.getHighestScopeOfActivity(r);
				if (r instanceof Process) {
					scpR = r;
				} else if (r instanceof Scope) {
					scpR = r;
				} else {
					scpR = ChoreoMergeUtil.getHighestScopeOfActivity((Activity) r);
				}
				
				String recName = (r instanceof Process ? ((Process) r).getName() : ((Activity) r).getName());
				
				// Set PartnerLinks, Operation and PortType for s
				if (scpS.getPartnerLinks() == null) {
					scpS.setPartnerLinks(BPELFactory.eINSTANCE.createPartnerLinks());
				}
				PartnerLink newPLS = BPELFactory.eINSTANCE.createPartnerLink();
				newPLS.setName(s.getName() + "TO" + recName + "PLS");
				newPLS.setPartnerLinkType((PartnerLinkType) recPLRole.eContainer());
				newPLS.setPartnerRole(recPLRole);
				scpS.getPartnerLinks().getChildren().add(newPLS);
				s.setPartnerLink(newPLS);
				s.setOperation(recOperation);
				s.setPortType(recPortType);
				
				// Set PartnerLinks, Operation and PortType for r
				
				// if (scpR.getPartnerLinks() == null) {
				// scpR.setPartnerLinks(BPELFactory.eINSTANCE.createPartnerLinks());
				// }
				
				PartnerLink newPLR = BPELFactory.eINSTANCE.createPartnerLink();
				newPLR.setName(s.getName() + "TO" + recName + "PLR");
				newPLR.setPartnerLinkType((PartnerLinkType) recPLRole.eContainer());
				newPLR.setMyRole(recPLRole);
				
				if (scpR instanceof Process) {
					Process proc = (Process) scpR;
					if (proc.getPartnerLinks() == null) {
						proc.setPartnerLinks(BPELFactory.eINSTANCE.createPartnerLinks());
					}
					proc.getPartnerLinks().getChildren().add(newPLR);
				} else {
					Scope scp = (Scope) scpR;
					if (scp.getPartnerLinks() == null) {
						scp.setPartnerLinks(BPELFactory.eINSTANCE.createPartnerLinks());
					}
					scp.getPartnerLinks().getChildren().add(newPLR);
				}
				
				// scpR.getPartnerLinks().getChildren().add(newPLR);
				
				if (recAct instanceof Receive) {
					((PartnerActivity) recAct).setPartnerLink(newPLR);
					((PartnerActivity) recAct).setOperation(recOperation);
					((PartnerActivity) recAct).setPortType(recPortType);
				} else if (recAct instanceof OnMessage) {
					OnMessage om = (OnMessage) recAct;
					om.setPartnerLink(newPLR);
					om.setOperation(recOperation);
					om.setPortType(recPortType);
				} else if (recAct instanceof OnEvent) {
					OnEvent oe = (OnEvent) recAct;
					oe.setPartnerLink(newPLR);
					oe.setOperation(recOperation);
					oe.setPortType(recPortType);
				}
				
				if (!ChoreoMergeUtil.isInvokeAsync(s)) {
					// Find the <reply>ing links for s in NMML
					for (MessageLink messageLink : this.choreographyPackage.getNMML()) {
						if (messageLink.getReceiveActivity().equals(s.getName())) {
							Reply repl = (Reply) ChoreoMergeUtil.resolveActivity(messageLink.getSendActivity());
							repl.setPartnerLink(newPLR);
							repl.setOperation(recOperation);
							repl.setPortType(recPortType);
						}
					}
				}
				
			}
		}
		
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
