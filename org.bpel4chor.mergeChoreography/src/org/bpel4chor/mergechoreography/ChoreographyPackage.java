package org.bpel4chor.mergechoreography;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.wsdl.WSDLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.exceptions.IncompleteZipFileException;
import org.bpel4chor.mergechoreography.util.PBDFragmentDuplicator;
import org.bpel4chor.model.grounding.impl.Grounding;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.bpel4chor.model.topology.impl.Topology;
import org.bpel4chor.splitprocess.utils.RandomIdGenerator;
import org.bpel4chor.splitprocess.utils.SplitProcessConstants;
import org.bpel4chor.utils.BPEL4ChorConstants;
import org.bpel4chor.utils.BPEL4ChorReader;
import org.bpel4chor.utils.FragmentDuplicator;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.CatchAll;
import org.eclipse.bpel.model.CorrelationSet;
import org.eclipse.bpel.model.EventHandler;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Import;
import org.eclipse.bpel.model.MessageExchange;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.wst.wsdl.Definition;
import org.eclipse.wst.wsdl.internal.util.WSDLResourceFactoryImpl;

/**
 * Choreography Package for passing the BPEL4Chor Choreography
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
@SuppressWarnings("restriction")
public class ChoreographyPackage implements Serializable {
	
	private static final long serialVersionUID = 3949692671093811360L;
	
	
	private class ReplacementMap {
		
		private Map<Variable, Process> varToPBD;
		private Map<Activity, Process> actToPBD;
		private Map<CorrelationSet, Process> corSetToPBD;
		
		
		public ReplacementMap() {
			this.varToPBD = new HashMap<>();
			this.actToPBD = new HashMap<>();
			this.corSetToPBD = new HashMap<>();
		}
		
		public Map<Variable, Process> getVarToPBD() {
			return this.varToPBD;
		}
		
		public void setVarToPBD(Map<Variable, Process> varToPBD) {
			this.varToPBD = varToPBD;
		}
		
		public Map<Activity, Process> getActToPBD() {
			return this.actToPBD;
		}
		
		public void setActToPBD(Map<Activity, Process> actToPBD) {
			this.actToPBD = actToPBD;
		}
		
		public Map<CorrelationSet, Process> getCorSetToPBD() {
			return this.corSetToPBD;
		}
		
		public void setCorSetToPBD(Map<CorrelationSet, Process> corSetToPBD) {
			this.corSetToPBD = corSetToPBD;
		}
		
	}
	
	
	/** Participant Behavior Descriptions */
	private List<Process> pbds;
	/** WSDLs */
	private List<Definition> wsdls;
	/** Topology */
	private Topology topology;
	/** Grounding */
	private Grounding grounding;
	/** Mapping from pbd to wsdl */
	private Map<Process, Definition> pbd2wsdl;
	/** Mapping from old to new names */
	private ReplacementMap replacementMap;
	/** The merged BPEL Process of the Choreography */
	private Process mergedProcess;
	/** The merged WSDL of the Chroeography */
	private Definition mergedWSDL;
	/** List of already visited {@link MessageLink}s */
	private List<MessageLink> visitedLinks;
	/** The "Leading" PBD */
	private Process leadPBD;
	
	private Logger log;
	
	
	public ChoreographyPackage(String fileName) {
		
		this.log = Logger.getLogger(ChoreographyPackage.class.getPackage().getName());
		
		this.pbds = new ArrayList<Process>();
		this.wsdls = new ArrayList<Definition>();
		this.visitedLinks = new ArrayList<>();
		this.replacementMap = new ReplacementMap();
		
		// read in the given file
		try {
			this.init(fileName);
		} catch (IncompleteZipFileException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Init for reading in the given zipfile (choreography)
	 * 
	 * @param fileName Path to the ZipFile
	 * @throws IncompleteZipFileException
	 */
	private void init(String fileName) throws IncompleteZipFileException {
		ZipFile zipFile = null;
		
		try {
			zipFile = new ZipFile(fileName);
			InputStream groundingTemp = null;
			// Check Entries of the Zip File
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (!entry.isDirectory()) {
					this.log.log(Level.INFO, "Extracting File => " + entry.getName() + " ... ");
					
					// Check what type the entry is and read it in
					if (entry.getName().equals("topology.xml")) {
						this.topology = BPEL4ChorReader.readTopology(zipFile.getInputStream(entry));
						// Check wether grounding stream was red in first
						if (groundingTemp != null) {
							this.grounding = BPEL4ChorReader.readGrounding(groundingTemp, this.topology);
						}
					} else if (entry.getName().equals("grounding.xml")) {
						// Check wether topology is already read in
						if (this.topology != null) {
							this.grounding = BPEL4ChorReader.readGrounding(zipFile.getInputStream(entry), this.topology);
						} else {
							groundingTemp = zipFile.getInputStream(entry);
						}
					} else if (entry.getName().split("-")[0].equals("pbd")) {
						InputStream stream = zipFile.getInputStream(entry);
						
						ResourceSet resourceSet = new ResourceSetImpl();
						
						URI uri = URI.createFileURI(entry.getName());
						BPELResource resource = (BPELResource) resourceSet.createResource(uri);
						Process proc2Add = BPEL4ChorReader.readBPEL(resource, stream);
						// Set Abstract Process profile !!
						proc2Add.setAbstractProcessProfile(BPEL4ChorConstants.PBD_ABSTRACT_PROCESS_PROFILE);
						this.pbds.add(proc2Add);
					} else if (entry.getName().substring(entry.getName().length() - 4, entry.getName().length()).equals("wsdl")) {
						ResourceSet rs = new ResourceSetImpl();
						rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("wsdl", new WSDLResourceFactoryImpl());
						InputStream stream = zipFile.getInputStream(entry);
						
						// Create Temp File to read in the WSDL
						String tStr = entry.getName().substring(0, entry.getName().length() - 5);
						// Length of prefix of tempFile should be min. 3
						if (tStr.length() < 3) {
							tStr += RandomIdGenerator.getId();
						}
						File temp = File.createTempFile(tStr, ".wsdl");
						temp.deleteOnExit();
						
						OutputStream out = new FileOutputStream(temp);
						
						int read = 0;
						byte[] bytes = new byte[1024];
						
						while ((read = stream.read(bytes)) != -1) {
							out.write(bytes, 0, read);
						}
						
						stream.close();
						out.flush();
						out.close();
						
						try {
							this.wsdls.add(BPEL4ChorReader.readWSDL(temp.getAbsolutePath()));
						} catch (WSDLException e) {
							e.printStackTrace();
						}
						
					}
					
				}
			}
			
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if ((this.topology == null) || (this.grounding == null) || (this.pbds.size() == 0) || (this.wsdls.size() == 0)) {
			throw new IncompleteZipFileException("The choreography in the zipfile is incomplete => " + fileName);
		} else {
			this.buildPBD2WSDLMap();
		}
		
	}
	
	/**
	 * After reading in the data, the map for storing pbd->wsdl is filled
	 */
	private void buildPBD2WSDLMap() {
		this.pbd2wsdl = new HashMap<Process, Definition>();
		for (Process process : this.pbds) {
			for (Definition definition : this.wsdls) {
				if (process.getName().equals(definition.getQName().getLocalPart())) {
					this.pbd2wsdl.put(process, definition);
				}
			}
		}
	}
	
	/**
	 * Method for initializing the merged BPEL Process with the leadProcess
	 * 
	 * @param leadProcess The leading {@link Process}
	 */
	public void initMergedProcess(Process leadProcess) {
		
		// Create new BPEL Process
		this.mergedProcess = BPELFactory.eINSTANCE.createProcess();
		
		// Set the leading PBD
		this.leadPBD = leadProcess;
		
		// Set standard attributes to values of leadingProcess
		this.mergedProcess.setTargetNamespace(leadProcess.getTargetNamespace());
		this.mergedProcess.setSuppressJoinFailure(leadProcess.getSuppressJoinFailure());
		
		if (leadProcess.getExtensions() != null) {
			this.mergedProcess.setExtensions(leadProcess.getExtensions());
		}
		
		// TODO: Benamsung !!! Namespace manuell
		// Give the new Process a name (PBD1 + PBD2 + ... + PBDn + "Merged")
		String procName = "";
		for (Process process : this.pbds) {
			procName += process.getName();
		}
		procName += "Merged";
		
		this.mergedProcess.setName(procName);
		
		this.log.log(Level.INFO, "Initializing mergedProcess => " + this.mergedProcess.getName());
		
		// Create Merged WSDL File Import
		// TODO: Impln !!
		Import wsdlImport = BPELFactory.eINSTANCE.createImport();
		
		// TODO: Nachschauen wieso hier Null-Pointer kommt auch bei gesetzter
		// URI wsdlImport.setLocation("");
		wsdlImport.setNamespace(this.mergedProcess.getTargetNamespace());
		wsdlImport.setImportType(SplitProcessConstants.NAMESPACE_WSDL);
		this.mergedProcess.getImports().add(wsdlImport);
		
		// First we create a new Flow inside the new mergedProcess
		if (this.mergedProcess.getActivity() == null) {
			Flow newFlow = BPELFactory.eINSTANCE.createFlow();
			newFlow.setName("MergedFlow");
			this.mergedProcess.setActivity(newFlow);
		}
		
		// Iterate over all PBDs and copy all Process Fragments in separate
		// scopes into the new merged Process Flow
		for (Process process : this.pbds) {
			this.copyVarsAndActitivies(process);
		}
		
	}
	
	/**
	 * Copy all activities from the given PBD to the new merged Process Note: If
	 * we have name collisions we check the new name into a map
	 * 
	 * @param pbd The PBD to be copied over
	 */
	private void copyVarsAndActitivies(Process pbd) {
		this.log.log(Level.INFO, "Copying Variables and Activities from PBD : " + pbd.getName());
		
		Scope newScope = BPELFactory.eINSTANCE.createScope();
		newScope.setName(pbd.getName());
		
		newScope.setExitOnStandardFault(pbd.getExitOnStandardFault());
		
		newScope.setSuppressJoinFailure(pbd.getSuppressJoinFailure());
		
		// Copy PartnerLinks
		if ((pbd.getPartnerLinks() != null) && (pbd.getPartnerLinks().getChildren().size() > 0)) {
			if (newScope.getPartnerLinks() == null) {
				newScope.setPartnerLinks(BPELFactory.eINSTANCE.createPartnerLinks());
			}
			for (PartnerLink link : pbd.getPartnerLinks().getChildren()) {
				PartnerLink newLink = FragmentDuplicator.copyPartnerLink(link);
				newScope.getPartnerLinks().getChildren().add(newLink);
			}
		}
		
		// Copy MessageExchanges
		// TODO: Impln !!!
		if ((pbd.getMessageExchanges() != null) && (pbd.getMessageExchanges().getChildren().size() > 0)) {
			if (newScope.getMessageExchanges() == null) {
				newScope.setMessageExchanges(BPELFactory.eINSTANCE.createMessageExchanges());
			}
			for (MessageExchange mex : pbd.getMessageExchanges().getChildren()) {
				MessageExchange newMex = FragmentDuplicator.copyMessageExchange(mex);
				newScope.getMessageExchanges().getChildren().add(newMex);
			}
		}
		
		// Copy CorrelationSets
		if ((pbd.getCorrelationSets() != null) && (pbd.getCorrelationSets().getChildren().size() > 0)) {
			if (newScope.getCorrelationSets() == null) {
				newScope.setCorrelationSets(BPELFactory.eINSTANCE.createCorrelationSets());
			}
			for (CorrelationSet corSet : pbd.getCorrelationSets().getChildren()) {
				CorrelationSet newCorSet = FragmentDuplicator.copyCorrelationSet(corSet);
				this.addCorSetReplacement(newCorSet, pbd);
				newScope.getCorrelationSets().getChildren().add(newCorSet);
			}
		}
		
		// Copy Variables
		if ((pbd.getVariables() != null) && (pbd.getVariables().getChildren().size() > 0)) {
			if (newScope.getVariables() == null) {
				newScope.setVariables(BPELFactory.eINSTANCE.createVariables());
			}
			for (Variable variable : pbd.getVariables().getChildren()) {
				Variable newVariable = FragmentDuplicator.copyVariable(variable);
				this.addVarReplacement(newVariable, pbd);
				newScope.getVariables().getChildren().add(newVariable);
			}
		}
		
		// Copy FaultHandlers
		if ((pbd.getFaultHandlers() != null) && ((pbd.getFaultHandlers().getCatch().size() > 0) || (pbd.getFaultHandlers().getCatch() != null))) {
			// TODO: Impln !!
			FaultHandler handler = BPELFactory.eINSTANCE.createFaultHandler();
			for (Catch cat : pbd.getFaultHandlers().getCatch()) {
				Catch newCatch = BPELFactory.eINSTANCE.createCatch();
				Activity newAct = FragmentDuplicator.copyActivity(cat.getActivity(), pbd, null);
				this.addActReplacement(newAct, pbd);
				newCatch.setActivity(newAct);
				// New make a deep copy of all contained subactivities
				// if any exist
				this.copySubActivities(newAct, cat.getActivity());
				newCatch.setDocumentation(cat.getDocumentation());
				newCatch.setElement(cat.getElement());
				newCatch.setEnclosingDefinition(cat.getEnclosingDefinition());
				newCatch.setFaultElement(cat.getFaultElement());
				newCatch.setFaultMessageType(cat.getFaultMessageType());
				newCatch.setFaultName(cat.getFaultName());
				newCatch.setFaultVariable(cat.getFaultVariable());
				handler.getCatch().add(newCatch);
			}
			newScope.setFaultHandlers(handler);
			
			if (pbd.getFaultHandlers().getCatchAll() != null) {
				CatchAll pbdCatAll = pbd.getFaultHandlers().getCatchAll();
				CatchAll newCatAll = BPELFactory.eINSTANCE.createCatchAll();
				Activity newAct = FragmentDuplicator.copyActivity(pbdCatAll.getActivity(), pbd, null);
				this.addActReplacement(newAct, pbd);
				// New make a deep copy of all contained subactivities
				// if any exist
				this.copySubActivities(newAct, pbdCatAll.getActivity());
				newCatAll.setActivity(newAct);
				newCatAll.setDocumentation(pbdCatAll.getDocumentation());
				newCatAll.setDocumentationElement(pbdCatAll.getDocumentationElement());
				newCatAll.setElement(pbdCatAll.getElement());
				newCatAll.setEnclosingDefinition(pbdCatAll.getEnclosingDefinition());
				newScope.getFaultHandlers().setCatchAll(newCatAll);
			}
			
		}
		
		// Copy EventHandlers
		if ((pbd.getEventHandlers() != null) && ((pbd.getEventHandlers().getAlarm().size() > 0) || (pbd.getEventHandlers().getEvents().size() > 0))) {
			// TODO: Impln. !!!
			EventHandler handler = BPELFactory.eINSTANCE.createEventHandler();
			for (OnAlarm alarm : pbd.getEventHandlers().getAlarm()) {
				OnAlarm newAlarm = BPELFactory.eINSTANCE.createOnAlarm();
				Activity newAct = FragmentDuplicator.copyActivity(alarm.getActivity(), pbd, null);
				this.addActReplacement(newAct, pbd);
				newAlarm.setActivity(newAct);
				// New make a deep copy of all contained subactivities
				// if any exist
				this.copySubActivities(newAct, alarm.getActivity());
				newAlarm.setActivity(newAct);
				newAlarm.setDocumentation(alarm.getDocumentation());
				newAlarm.setDocumentationElement(alarm.getDocumentationElement());
				newAlarm.setElement(alarm.getElement());
				newAlarm.setEnclosingDefinition(alarm.getEnclosingDefinition());
				newAlarm.setFor(alarm.getFor());
				newAlarm.setRepeatEvery(alarm.getRepeatEvery());
				newAlarm.setUntil(alarm.getUntil());
				handler.getAlarm().add(newAlarm);
			}
			
			for (OnEvent event : pbd.getEventHandlers().getEvents()) {
				OnEvent newEvent = BPELFactory.eINSTANCE.createOnEvent();
				
				Activity newAct = FragmentDuplicator.copyActivity(event.getActivity(), pbd, null);
				this.addActReplacement(newAct, pbd);
				newEvent.setActivity(newAct);
				// New make a deep copy of all contained subactivities
				// if any exist
				this.copySubActivities(newAct, event.getActivity());
				
				newEvent.setActivity(newAct);
				newEvent.setCorrelations(event.getCorrelations());
				newEvent.setCorrelationSets(event.getCorrelationSets());
				newEvent.setDocumentation(event.getDocumentation());
				newEvent.setDocumentationElement(event.getDocumentationElement());
				newEvent.setElement(event.getElement());
				newEvent.setEnclosingDefinition(event.getEnclosingDefinition());
				newEvent.setFromParts(event.getFromParts());
				newEvent.setMessageExchange(event.getMessageExchange());
				newEvent.setMessageType(event.getMessageType());
				newEvent.setOperation(event.getOperation());
				newEvent.setPartnerLink(event.getPartnerLink());
				newEvent.setPortType(event.getPortType());
				newEvent.setVariable(event.getVariable());
				newEvent.setXSDElement(event.getXSDElement());
				handler.getEvents().add(newEvent);
			}
		}
		
		// Copy Activity from the Fragment Process to the new Scope
		Activity newActivity = FragmentDuplicator.copyActivity(pbd.getActivity(), pbd, null);
		this.addActReplacement(newActivity, pbd);
		newScope.setActivity(newActivity);
		
		// New make a deep copy of all contained subactivities
		// if any exist
		this.copySubActivities(newActivity, pbd.getActivity());
		
		// Copy newScope into MergedProcess Flow
		((Flow) this.mergedProcess.getActivity()).getActivities().add(newScope);
		// this.copyVarsAndSetsToScope(pbd, this.mergedProcess, null);
		
	}
	
	/**
	 * Copies all sub activities of the given pbdAct to newAct if any present
	 * 
	 * @param newAct The new {@link Activity}
	 * @param pbdAct The {@link Activity} from the fragment process
	 */
	private void copySubActivities(Activity newAct, Activity pbdAct) {
		if (pbdAct instanceof Sequence) {
			Sequence pbdSeq = (Sequence) pbdAct;
			Sequence newSeq = (Sequence) newAct;
			for (Activity act : pbdSeq.getActivities()) {
				Activity newActivity = PBDFragmentDuplicator.copyActivity(act, this.resolveActReplacement(newAct), null);
				this.addActReplacement(newActivity, this.resolveActReplacement(newAct));
				newSeq.getActivities().add(newActivity);
			}
		} else if (pbdAct instanceof Flow) {
			Flow pbdFlow = (Flow) pbdAct;
			Flow newFlow = (Flow) newAct;
			for (Activity act : pbdFlow.getActivities()) {
				Activity newActivity = PBDFragmentDuplicator.copyActivity(act, this.resolveActReplacement(newAct), null);
				this.addActReplacement(newActivity, this.resolveActReplacement(newAct));
				newFlow.getActivities().add(newActivity);
			}
		} else if (pbdAct instanceof Scope) {
			Scope pbdScope = (Scope) pbdAct;
			Scope newScope = (Scope) newAct;
			if (pbdScope.getActivity() != null) {
				Activity newActivity = PBDFragmentDuplicator.copyActivity(pbdScope.getActivity(), this.resolveActReplacement(newAct), null);
				this.addActReplacement(newActivity, this.resolveActReplacement(newAct));
				newScope.setActivity(newActivity);
			}
		}
	}
	
	/**
	 * Returns the PBD with the given Name from the PBD List
	 * 
	 * @param procName The name of the PBD
	 * @return PBD with given name, or null
	 */
	public Process getPBDbyProcessName(String procName) {
		for (Process process : this.pbds) {
			if (process.getName().equals(procName)) {
				return process;
			}
		}
		return null;
	}
	
	public List<Process> getPbds() {
		return this.pbds;
	}
	
	public void setPbds(List<Process> pbds) {
		this.pbds = pbds;
	}
	
	public List<Definition> getWsdls() {
		return this.wsdls;
	}
	
	public void setWsdls(List<Definition> wsdls) {
		this.wsdls = wsdls;
	}
	
	public Topology getTopology() {
		return this.topology;
	}
	
	public void setTopology(Topology topology) {
		this.topology = topology;
	}
	
	public Grounding getGrounding() {
		return this.grounding;
	}
	
	public void setGrounding(Grounding grounding) {
		this.grounding = grounding;
	}
	
	public Map<Process, Definition> getPbd2wsdl() {
		return this.pbd2wsdl;
	}
	
	public void setPbd2wsdl(Map<Process, Definition> pbd2wsdl) {
		this.pbd2wsdl = pbd2wsdl;
	}
	
	public ReplacementMap getReplacementMap() {
		return this.replacementMap;
	}
	
	public void setReplacementMap(ReplacementMap replacementMap) {
		this.replacementMap = replacementMap;
	}
	
	/**
	 * Returns the corresponding process from the replacement map
	 * 
	 * @param activity The searched {@link Activity}
	 * @return {@link Process}
	 */
	public Process resolveActReplacement(Activity activity) {
		return this.replacementMap.getActToPBD().get(activity);
	}
	
	/**
	 * Returns the corresponding process from the replacement map
	 * 
	 * @param set The searched {@link CorrelationSet}
	 * @return {@link Process}
	 */
	public Process resolveCorSetReplacement(CorrelationSet set) {
		return this.replacementMap.getCorSetToPBD().get(set);
	}
	
	/**
	 * Returns the corresponding process from the replacement map
	 * 
	 * @param variable The searched {@link Variable}
	 * @return {@link Process}
	 */
	public Process resolveVarReplacement(Variable variable) {
		return this.replacementMap.getVarToPBD().get(variable);
	}
	
	/**
	 * Adds an activity replacement to the replacement map
	 * 
	 * @param activity The {@link Activity} to be added
	 * @param pbd The corresponding {@link Process}
	 */
	public void addActReplacement(Activity activity, Process pbd) {
		this.log.log(Level.INFO, "Adding Activity Replacement => activity = " + activity.getName() + " , PBD = " + pbd.getName());
		this.replacementMap.getActToPBD().put(activity, pbd);
	}
	
	/**
	 * Adds an correlation set replacement to the replacement map
	 * 
	 * @param correlationSet The {@link CorrelationSet} to be added
	 * @param pbd The corresponding {@link Process}
	 */
	public void addCorSetReplacement(CorrelationSet correlationSet, Process pbd) {
		this.log.log(Level.INFO, "Adding Correlation Set Replacement => correlationSet = " + correlationSet + " , PBD = " + pbd);
		this.replacementMap.getCorSetToPBD().put(correlationSet, pbd);
	}
	
	/**
	 * Adds an variable replacement to the replacement map
	 * 
	 * @param variable The {@link Variable} to be added
	 * @param pbd The corresponding {@link Process}
	 */
	public void addVarReplacement(Variable variable, Process pbd) {
		this.log.log(Level.INFO, "Adding Variable Replacement => variable = " + variable + " , PBD = " + pbd);
		this.replacementMap.getVarToPBD().put(variable, pbd);
	}
	
	public Process getMergedProcess() {
		return this.mergedProcess;
	}
	
	public void setMergedProcess(Process mergedProcess) {
		this.mergedProcess = mergedProcess;
	}
	
	public Definition getMergedWSDL() {
		return this.mergedWSDL;
	}
	
	public void setMergedWSDL(Definition mergedWSDL) {
		this.mergedWSDL = mergedWSDL;
	}
	
	/**
	 * Returns the activity with the given name from the merged Process which
	 * belongs to the given pbd
	 * 
	 * @param name The name of the {@link Activity}
	 * @param pbd The {@link Process}
	 * @return The found {@link Activity} or null
	 */
	public Activity resolveActivityInMergedProcess(String name, Process pbd) {
		for (Map.Entry<Activity, Process> entry : this.getReplacementMap().getActToPBD().entrySet()) {
			if (entry.getKey().getName().equals(name) && (entry.getValue() == pbd)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	/**
	 * Returns the variable with the given name from the merged Process which
	 * belongs to the given pbd
	 * 
	 * @param name The name of the {@link Variable}
	 * @param pbd The {@link Process}
	 * @return The found {@link Variable} or null
	 */
	public Variable resolveVariableInMergedProcess(String name, Process pbd) {
		for (Map.Entry<Variable, Process> entry : this.getReplacementMap().getVarToPBD().entrySet()) {
			if (entry.getKey().getName().equals(name) && (entry.getValue() == pbd)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	/**
	 * Returns the correlation set with the given name from the merged Process
	 * which belongs to the given pbd
	 * 
	 * @param name The name of the {@link CorrelationSet}
	 * @param pbd The {@link Process}
	 * @return The found {@link CorrelationSet} or null
	 */
	public CorrelationSet resolveCorSetInMergedProcess(String name, Process pbd) {
		for (Map.Entry<CorrelationSet, Process> entry : this.getReplacementMap().getCorSetToPBD().entrySet()) {
			if (entry.getKey().getName().equals(name) && (entry.getValue() == pbd)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	/**
	 * Adds a {@link MessageLink} to the visited Links
	 * 
	 * @param link The visited {@link MessageLink}
	 */
	public void addVisitedLink(MessageLink link) {
		if (this.visitedLinks.indexOf(link) == -1) {
			this.visitedLinks.add(link);
		}
	}
	
	/**
	 * Checks whether given {@link MessageLink} have been already visited
	 * 
	 * @param link The {@link MessageLink} to check
	 * @return true, if {@link MessageLink} have been visited, false otherwise
	 */
	public boolean isLinkVisited(MessageLink link) {
		if (this.visitedLinks.indexOf(link) != -1) {
			return true;
		}
		return false;
	}
	
	public Process getLeadPBD() {
		return this.leadPBD;
	}
	
	public void setLeadPBD(Process leadPBD) {
		this.leadPBD = leadPBD;
	}
	
}
