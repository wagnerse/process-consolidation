package org.bpel4chor.mergechoreography;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.exceptions.IncompleteZipFileException;
import org.bpel4chor.mergechoreography.util.ChoreoMergeUtil;
import org.bpel4chor.mergechoreography.util.PBDFragmentDuplicator;
import org.bpel4chor.model.grounding.impl.Grounding;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.bpel4chor.model.topology.impl.Topology;
import org.bpel4chor.utils.BPEL4ChorConstants;
import org.bpel4chor.utils.BPEL4ChorReader;
import org.bpel4chor.utils.BPEL4ChorWriter;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.PartnerActivity;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.bpel.model.resource.BPELResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.wst.wsdl.Definition;

import de.uni_stuttgart.iaas.bpel.model.utilities.FragmentDuplicator;
import de.uni_stuttgart.iaas.bpel.model.utilities.MyWSDLUtil;

/**
 * Choreography Package for passing the BPEL4Chor Choreography
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class ChoreographyPackage implements Serializable {
	
	private static final long serialVersionUID = 3949692671093811360L;
	
	protected Logger log = Logger.getLogger(this.getClass().getPackage().getName());
	
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
	/** The merged BPEL Process of the Choreography */
	private Process mergedProcess;
	/** List of already visited {@link MessageLink}s */
	private List<MessageLink> visitedLinks;
	/** Map of wsu:ids associated with the activities in the new mergedProcess */
	private Map<String, BPELExtensibleElement> old2New;
	/** List of "Non-Mergeable-Message-Links" */
	private List<MessageLink> nmml;
	/**
	 * Map of {@link Variable}-to-{@link Variable} relation from the pbds to the
	 * new mergedProcess
	 */
	private Map<Variable, Variable> pbd2MergedVars;
	
	/**
	 * Map if {@link Link}-to-{@link Link} relation from the pbds to the new
	 * mergedProcess
	 */
	private Map<Link, Link> pbd2MergedLinks;
	
	
	public ChoreographyPackage(String fileName) {
		
		this.pbds = new ArrayList<Process>();
		this.wsdls = new ArrayList<Definition>();
		this.visitedLinks = new ArrayList<>();
		this.old2New = new HashMap<>();
		this.nmml = new ArrayList<>();
		this.pbd2MergedVars = new HashMap<>();
		this.pbd2MergedLinks = new HashMap<>();
		
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
		
		this.readInZip(fileName);
		
		if ((this.topology == null) || (this.grounding == null) || (this.pbds.size() == 0) || (this.wsdls.size() == 0)) {
			throw new IncompleteZipFileException("The choreography in the zipfile is incomplete => " + fileName);
		} else {
			this.buildPBD2WSDLMap();
		}
		
	}
	
	/**
	 * Read in the Choreography.zip-File
	 * 
	 * @param fileName The Path to the zip-File
	 */
	private void readInZip(String fileName) {
		final Path path = Paths.get(fileName);
		final java.net.URI uri = java.net.URI.create("jar:file:" + path.toUri().getPath());
		
		final Map<String, String> env = new HashMap<>();
		
		FileSystem zipFileSystem = null;
		
		try {
			zipFileSystem = FileSystems.newFileSystem(uri, env);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		final Path root = zipFileSystem.getPath(File.separator);
		
		// walk the file tree and print out the directory and filenames
		try {
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				
				InputStream groundingTemp = null;
				
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					
					ChoreographyPackage.this.log.log(Level.INFO, "Extracting File => " + file.toUri() + " ... ");
					if (file.getFileName().toString().equals("topology.xml")) {
						ChoreographyPackage.this.topology = BPEL4ChorReader.readTopology(Files.newInputStream(file));
						// Check whether grounding stream was red in first
						if (this.groundingTemp != null) {
							ChoreographyPackage.this.grounding = BPEL4ChorReader.readGrounding(this.groundingTemp, ChoreographyPackage.this.topology);
						}
					} else if (file.getFileName().toString().equals("grounding.xml")) {
						// Check whether topology is already read in
						if (ChoreographyPackage.this.topology != null) {
							ChoreographyPackage.this.grounding = BPEL4ChorReader.readGrounding(Files.newInputStream(file), ChoreographyPackage.this.topology);
						} else {
							this.groundingTemp = Files.newInputStream(file);
						}
					} else if (file.toString().endsWith("wsdl")) {
						Definition defRead = null;
						try {
							defRead = MyWSDLUtil.readWSDLFromURI(file.toUri().toString());
							// We set the URI of the WSDL-Def. read from .zip
							// file to a local one
							// URI uri = URI.createGenericURI("file",
							// "example.com:8042",
							// defRead.getQName().getLocalPart() + ".wsdl");
							defRead.eResource().setURI(URI.createFileURI(defRead.getQName().getLocalPart() + ".wsdl"));
							// defRead.eResource().setURI(uri);
							ChoreographyPackage.this.wsdls.add(defRead);
						} catch (WSDLException e) {
							e.printStackTrace();
						}
					} else if (file.toString().endsWith("pbd")) {
						InputStream stream = Files.newInputStream(file);
						ResourceSet resourceSet = new ResourceSetImpl();
						resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("pbd", new BPELResourceFactoryImpl());
						URI uri = URI.createURI(file.toUri().toString());
						BPELResource resource = (BPELResource) resourceSet.createResource(uri);
						Process proc2Add = BPEL4ChorReader.readBPEL(resource, stream);
						// Set Abstract Process profile !!
						proc2Add.setAbstractProcessProfile(BPEL4ChorConstants.PBD_ABSTRACT_PROCESS_PROFILE);
						ChoreographyPackage.this.pbds.add(proc2Add);
					}
					
					return FileVisitResult.CONTINUE;
				}
				
			});
			
			zipFileSystem.close();
		} catch (IOException e) {
			e.printStackTrace();
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
	 * Save merged Choreography to given fileName
	 * 
	 * @param fileName Filename to save Choreography to
	 */
	public void saveMergedChoreography(String fileName) {
		try {
			// Save wsdl files
			for (Definition def : this.getWsdls()) {
				FileOutputStream outputStream = new FileOutputStream(new File(fileName + def.getQName().getLocalPart() + ".wsdl"));
				BPEL4ChorWriter.writeWSDL(def, outputStream);
			}
			// Save BPEL File of mergedProcess
			FileOutputStream outputStream = new FileOutputStream(new File(fileName + this.getMergedProcess().getName() + ".bpel"));
			BPEL4ChorWriter.writeAbstractBPEL(this.getMergedProcess(), outputStream);
		} catch (IOException | WSDLException e1) {
			e1.printStackTrace();
		}
		
	}
	
	/**
	 * Method for initializing the merged BPEL Process with the leadProcess
	 */
	public void initMergedProcess() {
		
		// Create new BPEL Process
		this.mergedProcess = BPELFactory.eINSTANCE.createProcess();
		
		// TODO: Woher nehmen wir die Infos genaus ?? Neue Parameter in
		// initMergedMethode !!
		
		// Set standard attributes to values of leadingProcess
		this.mergedProcess.setTargetNamespace(this.pbds.get(0).getTargetNamespace());
		this.mergedProcess.setSuppressJoinFailure(this.pbds.get(0).getSuppressJoinFailure());
		this.mergedProcess.setAbstractProcessProfile(BPEL4ChorConstants.PBD_ABSTRACT_PROCESS_PROFILE);
		
		if (this.pbds.get(0).getExtensions() != null) {
			this.mergedProcess.setExtensions(FragmentDuplicator.copyExtensions(this.pbds.get(0).getExtensions()));
		}
		
		String procName = "ProcessMerged";
		
		this.mergedProcess.setName(procName);
		
		this.log.log(Level.INFO, "Initializing mergedProcess => " + this.mergedProcess.getName());
		
		// Initialize PBDFragmentDuplicator with choreographypackage
		PBDFragmentDuplicator.setPkg(this);
		
		// Initialize ChoreoMergeUtil with choreographypackage
		ChoreoMergeUtil.setPkg(this);
		
		// First we create a new Flow inside the new mergedProcess
		if (this.mergedProcess.getActivity() == null) {
			Flow newFlow = BPELFactory.eINSTANCE.createFlow();
			newFlow.setName("MergedFlow");
			this.mergedProcess.setActivity(newFlow);
		}
		
		// Iterate over all PBDs and copy all Process Fragments in separate
		// scopes into the new merged Process Flow
		for (Process process : this.pbds) {
			PBDFragmentDuplicator.copyVarsAndActitivies(process);
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
	
	/**
	 * Adds new wsu:id to {@link Activity} association
	 * 
	 * @param wsuID The wsu:id from the {@link PartnerActivity} in the
	 *            choreography
	 * @param activity The {@link Activity} in the new merged Process
	 */
	public void addOld2NewRelation(String wsuID, BPELExtensibleElement activity) {
		this.log.info("===========================================================================");
		this.log.info("Adding wsu:id -> Activity association : " + wsuID + " , " + activity);
		this.log.info("===========================================================================");
		this.old2New.put(wsuID, activity);
	}
	
	public Process getMergedProcess() {
		return this.mergedProcess;
	}
	
	public void setMergedProcess(Process mergedProcess) {
		this.mergedProcess = mergedProcess;
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
	 * Adds a {@link MessageLink} to the NMML-{@link List}
	 * 
	 * @param link {@link MessageLink} to add
	 */
	public void addNMML(MessageLink link) {
		if (this.nmml.indexOf(link) == -1) {
			this.nmml.add(link);
		}
	}
	
	/**
	 * Adds {@link MessageLink}s to the visited Links
	 * 
	 * @param links The visited {@link MessageLink}s
	 */
	public void addVisitedLinks(List<MessageLink> links) {
		for (MessageLink link : links) {
			this.addVisitedLink(link);
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
	
	public Map<String, BPELExtensibleElement> getOld2New() {
		return this.old2New;
	}
	
	public List<MessageLink> getNMML() {
		return this.nmml;
	}
	
	public Map<Variable, Variable> getPbd2MergedVars() {
		return this.pbd2MergedVars;
	}
	
	/**
	 * Find the PBD with the given Name
	 * 
	 * @param pbdName The name of the searched PBD
	 * @return pbd or null
	 */
	public Process getPBDByName(String pbdName) {
		for (Process pbd : this.pbds) {
			if (pbd.getName().equals(pbdName)) {
				return pbd;
			}
		}
		return null;
	}
	
	public Map<Link, Link> getPbd2MergedLinks() {
		return this.pbd2MergedLinks;
	}
	
}
