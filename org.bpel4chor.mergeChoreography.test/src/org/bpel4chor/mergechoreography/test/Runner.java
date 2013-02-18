package org.bpel4chor.mergechoreography.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.WSDLException;

import org.bpel4chor.mergechoreography.ChoreographyMerger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.test.util.Constants;
import org.bpel4chor.utils.AbstractBPELWriter;
import org.bpel4chor.utils.BPEL4ChorReader;
import org.bpel4chor.utils.BPEL4ChorWriter;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.resource.BPELResource;
import org.eclipse.wst.wsdl.Definition;

public class Runner {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String outputPath = "D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\";
		Map<String, String> args2 = new HashMap<>();
		
		args2.put("", "");
		
		// ASP11
		ChoreographyMerger choreographyMerger1 = new ChoreographyMerger(Constants.asyncPattern11Choreo);
		ChoreographyPackage package1 = choreographyMerger1.getChoreographyPackage();
		choreographyMerger1.merge(outputPath + "ASP11\\" + "outputASP11.bpel");
		Process process1 = package1.getMergedProcess();
		if (process1 != null) {
			try {
				
				for (Definition def : package1.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP11\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP11\\" + "outputASP11.bpel").eResource(), System.out, args2);
				
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP11 with FH and CH in <invoke> s
		ChoreographyMerger choreographyMerger2 = new ChoreographyMerger(Constants.asyncPattern11InvFHCHChoreo);
		ChoreographyPackage package2 = choreographyMerger2.getChoreographyPackage();
		choreographyMerger2.merge(outputPath + "ASP11InvFHCH\\" + "outputASP11InvFHCH.bpel");
		Process process2 = package2.getMergedProcess();
		if (process2 != null) {
			try {
				for (Definition def : package2.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP11InvFHCH\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP11InvFHCH\\" + "outputASP11InvFHCH.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP12
		ChoreographyMerger choreographyMerger3 = new ChoreographyMerger(Constants.asyncPattern12Choreo);
		ChoreographyPackage package3 = choreographyMerger3.getChoreographyPackage();
		choreographyMerger3.merge(outputPath + "ASP12\\" + "outputASP12.bpel");
		Process process3 = package3.getMergedProcess();
		if (process3 != null) {
			try {
				for (Definition def : package3.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP12\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP12\\" + "outputASP12.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP13
		ChoreographyMerger choreographyMerger4 = new ChoreographyMerger(Constants.asyncPattern13Choreo);
		ChoreographyPackage package4 = choreographyMerger4.getChoreographyPackage();
		choreographyMerger4.merge(outputPath + "ASP13\\" + "outputASP13.bpel");
		Process process4 = package4.getMergedProcess();
		if (process4 != null) {
			try {
				for (Definition def : package4.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP13\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP13\\" + "outputASP13.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP14
		ChoreographyMerger choreographyMerger5 = new ChoreographyMerger(Constants.asyncPattern14Choreo);
		ChoreographyPackage package5 = choreographyMerger5.getChoreographyPackage();
		choreographyMerger5.merge(outputPath + "ASP14\\" + "outputASP14.bpel");
		Process process5 = package5.getMergedProcess();
		if (process5 != null) {
			try {
				for (Definition def : package5.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP14\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP14\\" + "outputASP14.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP15
		ChoreographyMerger choreographyMerger6 = new ChoreographyMerger(Constants.asyncPattern15Choreo);
		ChoreographyPackage package6 = choreographyMerger6.getChoreographyPackage();
		choreographyMerger6.merge(outputPath + "ASP15\\" + "outputASP15.bpel");
		Process process6 = package6.getMergedProcess();
		if (process6 != null) {
			try {
				for (Definition def : package6.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP15\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP15\\" + "outputASP15.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP15 with branches after <receive> r
		ChoreographyMerger choreographyMerger7 = new ChoreographyMerger(Constants.asyncPattern15BranchSuccRChoreo);
		ChoreographyPackage package7 = choreographyMerger7.getChoreographyPackage();
		choreographyMerger7.merge(outputPath + "ASP15BranchSuccR\\" + "outputASP15BranchSuccR.bpel");
		Process process7 = package7.getMergedProcess();
		if (process7 != null) {
			try {
				for (Definition def : package7.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP15BranchSuccR\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP15BranchSuccR\\" + "outputASP15BranchSuccR.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP16
		ChoreographyMerger choreographyMerger8 = new ChoreographyMerger(Constants.asyncPattern16Choreo);
		ChoreographyPackage package8 = choreographyMerger8.getChoreographyPackage();
		choreographyMerger8.merge(outputPath + "ASP16\\" + "outputASP16.bpel");
		Process process8 = package8.getMergedProcess();
		if (process8 != null) {
			try {
				for (Definition def : package8.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP16\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP16\\" + "outputASP16.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP17 (Khalaf Split)
		ChoreographyMerger choreographyMerger9 = new ChoreographyMerger(Constants.asyncPattern17Choreo);
		ChoreographyPackage package9 = choreographyMerger9.getChoreographyPackage();
		choreographyMerger9.merge(outputPath + "ASP17\\" + "outputASP17.bpel");
		Process process9 = package9.getMergedProcess();
		if (process9 != null) {
			try {
				for (Definition def : package9.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP17\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP17\\" + "outputASP17.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP18
		ChoreographyMerger choreographyMerger10 = new ChoreographyMerger(Constants.asyncPattern18Choreo);
		ChoreographyPackage package10 = choreographyMerger10.getChoreographyPackage();
		choreographyMerger10.merge(outputPath + "ASP18\\" + "outputASP18.bpel");
		Process process10 = package10.getMergedProcess();
		if (process10 != null) {
			try {
				for (Definition def : package10.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP18\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP18\\" + "outputASP18.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP21
		ChoreographyMerger choreographyMerger11 = new ChoreographyMerger(Constants.asyncPattern21Choreo);
		ChoreographyPackage package11 = choreographyMerger11.getChoreographyPackage();
		choreographyMerger11.merge(outputPath + "ASP21\\" + "outputASP21.bpel");
		Process process11 = package11.getMergedProcess();
		if (process11 != null) {
			try {
				for (Definition def : package11.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP21\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP21\\" + "outputASP21.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP22 2 Send to one Pick with distinct <onMessage>-branches, which
		// uses same variable
		ChoreographyMerger choreographyMerger12 = new ChoreographyMerger(Constants.asyncPattern222SendSameVarChoreo);
		ChoreographyPackage package12 = choreographyMerger12.getChoreographyPackage();
		choreographyMerger12.merge(outputPath + "ASP222SendSameVar\\" + "outputASP222SendSameVar.bpel");
		Process process12 = package12.getMergedProcess();
		if (process12 != null) {
			try {
				for (Definition def : package12.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP222SendSameVar\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP222SendSameVar\\" + "outputASP222SendSameVar.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP22 2 Send to one Pick with distinct <onMessage>-branches, which
		// uses different variables
		ChoreographyMerger choreographyMerger13 = new ChoreographyMerger(Constants.asyncPattern222SendDiffVarsChoreo);
		ChoreographyPackage package13 = choreographyMerger13.getChoreographyPackage();
		choreographyMerger13.merge(outputPath + "ASP222SendDiffVars\\" + "outputASP222SendDiffVars.bpel");
		Process process13 = package13.getMergedProcess();
		if (process13 != null) {
			try {
				for (Definition def : package13.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP222SendDiffVars\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP222SendDiffVars\\" + "outputASP222SendDiffVars.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP23
		ChoreographyMerger choreographyMerger14 = new ChoreographyMerger(Constants.asyncPattern23Choreo);
		ChoreographyPackage package14 = choreographyMerger14.getChoreographyPackage();
		choreographyMerger14.merge(outputPath + "ASP23\\" + "outputASP23.bpel");
		Process process14 = package14.getMergedProcess();
		if (process14 != null) {
			try {
				for (Definition def : package14.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP23\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP23\\" + "outputASP23.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// ASP23 with createInstance=yes in <pick>
		ChoreographyMerger choreographyMerger15 = new ChoreographyMerger(Constants.asyncPattern23CreateInstanceChoreo);
		ChoreographyPackage package15 = choreographyMerger15.getChoreographyPackage();
		choreographyMerger15.merge(outputPath + "ASP23CreateInstance\\" + "outputASP23CreateInstance.bpel");
		Process process15 = package15.getMergedProcess();
		if (process15 != null) {
			try {
				for (Definition def : package15.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "ASP23CreateInstance\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "ASP23CreateInstance\\" + "outputASP23CreateInstance.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP11
		ChoreographyMerger choreographyMerger16 = new ChoreographyMerger(Constants.syncPattern11Choreo);
		ChoreographyPackage package16 = choreographyMerger16.getChoreographyPackage();
		choreographyMerger16.merge(outputPath + "SP11\\" + "outputSP11.bpel");
		Process process16 = package16.getMergedProcess();
		if (process16 != null) {
			try {
				for (Definition def : package16.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP11\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP11\\" + "outputSP11.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP12
		ChoreographyMerger choreographyMerger17 = new ChoreographyMerger(Constants.syncPattern12Choreo);
		ChoreographyPackage package17 = choreographyMerger17.getChoreographyPackage();
		choreographyMerger17.merge(outputPath + "SP12\\" + "outputSP12.bpel");
		Process process17 = package17.getMergedProcess();
		if (process17 != null) {
			try {
				for (Definition def : package17.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP12\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP12\\" + "outputSP12.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP13
		ChoreographyMerger choreographyMerger18 = new ChoreographyMerger(Constants.syncPattern13Choreo);
		ChoreographyPackage package18 = choreographyMerger18.getChoreographyPackage();
		choreographyMerger18.merge(outputPath + "SP13\\" + "outputSP13.bpel");
		Process process18 = package18.getMergedProcess();
		if (process18 != null) {
			try {
				for (Definition def : package18.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP13\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP13\\" + "outputSP13.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP14 With Fault in <invoke> s
		ChoreographyMerger choreographyMerger19 = new ChoreographyMerger(Constants.syncPattern14WithFaultChoreo);
		ChoreographyPackage package19 = choreographyMerger19.getChoreographyPackage();
		choreographyMerger19.merge(outputPath + "SP14WithFault\\" + "outputSP14WithFault.bpel");
		Process process19 = package19.getMergedProcess();
		if (process19 != null) {
			try {
				for (Definition def : package19.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP14WithFault\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP14WithFault\\" + "outputSP14WithFault.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP14
		ChoreographyMerger choreographyMerger20 = new ChoreographyMerger(Constants.syncPattern14Choreo);
		ChoreographyPackage package20 = choreographyMerger20.getChoreographyPackage();
		choreographyMerger20.merge(outputPath + "SP14\\" + "outputSP14.bpel");
		Process process20 = package20.getMergedProcess();
		if (process20 != null) {
			try {
				for (Definition def : package20.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP14\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP14\\" + "outputSP14.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP15 with <invoke> in FH
		ChoreographyMerger choreographyMerger21 = new ChoreographyMerger(Constants.syncPattern15FHChoreo);
		ChoreographyPackage package21 = choreographyMerger21.getChoreographyPackage();
		choreographyMerger21.merge(outputPath + "SP15FH\\" + "outputSP15FH.bpel");
		Process process21 = package21.getMergedProcess();
		if (process21 != null) {
			try {
				for (Definition def : package21.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP15FH\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP15FH\\" + "outputSP15FH.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// // SP15 with <invoke> in EH
		ChoreographyMerger choreographyMerger22 = new ChoreographyMerger(Constants.syncPattern15EHChoreo);
		ChoreographyPackage package22 = choreographyMerger22.getChoreographyPackage();
		choreographyMerger22.merge(outputPath + "SP15EH\\" + "outputSP15EH.bpel");
		Process process22 = package22.getMergedProcess();
		if (process22 != null) {
			try {
				for (Definition def : package22.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP15EH\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP15EH\\" + "outputSP15EH.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP15 with <invoke> in CH
		ChoreographyMerger choreographyMerger23 = new ChoreographyMerger(Constants.syncPattern15CHChoreo);
		ChoreographyPackage package23 = choreographyMerger23.getChoreographyPackage();
		choreographyMerger23.merge(outputPath + "SP15CH\\" + "outputSP15CH.bpel");
		Process process23 = package23.getMergedProcess();
		if (process23 != null) {
			try {
				for (Definition def : package23.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP15CH\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP15CH\\" + "outputSP15CH.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// // SP15 with <invoke> in TH
		ChoreographyMerger choreographyMerger24 = new ChoreographyMerger(Constants.syncPattern15THChoreo);
		ChoreographyPackage package24 = choreographyMerger24.getChoreographyPackage();
		choreographyMerger24.merge(outputPath + "SP15TH\\" + "outputSP15TH.bpel");
		Process process24 = package24.getMergedProcess();
		if (process24 != null) {
			try {
				for (Definition def : package24.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP15TH\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP15TH\\" + "outputSP15TH.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP21
		ChoreographyMerger choreographyMerger25 = new ChoreographyMerger(Constants.syncPattern21Choreo);
		ChoreographyPackage package25 = choreographyMerger25.getChoreographyPackage();
		choreographyMerger25.merge(outputPath + "SP21\\" + "outputSP21.bpel");
		Process process25 = package25.getMergedProcess();
		if (process25 != null) {
			try {
				for (Definition def : package25.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP21\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP21\\" + "outputSP21.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP22
		ChoreographyMerger choreographyMerger26 = new ChoreographyMerger(Constants.syncPattern22Choreo);
		ChoreographyPackage package26 = choreographyMerger26.getChoreographyPackage();
		choreographyMerger26.merge(outputPath + "SP22\\" + "outputSP22.bpel");
		Process process26 = package26.getMergedProcess();
		if (process26 != null) {
			try {
				for (Definition def : package26.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP22\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP22\\" + "outputSP22.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP23
		ChoreographyMerger choreographyMerger27 = new ChoreographyMerger(Constants.syncPattern23Choreo);
		ChoreographyPackage package27 = choreographyMerger27.getChoreographyPackage();
		choreographyMerger27.merge(outputPath + "SP23\\" + "outputSP23.bpel");
		Process process27 = package27.getMergedProcess();
		if (process27 != null) {
			try {
				for (Definition def : package27.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP23\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP23\\" + "outputSP23.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// SP24
		ChoreographyMerger choreographyMerger28 = new ChoreographyMerger(Constants.syncPattern24Choreo);
		ChoreographyPackage package28 = choreographyMerger28.getChoreographyPackage();
		choreographyMerger28.merge(outputPath + "SP24\\" + "outputSP24.bpel");
		Process process28 = package28.getMergedProcess();
		if (process28 != null) {
			try {
				for (Definition def : package28.getWsdls()) {
					FileOutputStream outputStream = new FileOutputStream(new File(outputPath + "SP24\\" + def.getQName().getLocalPart() + ".wsdl"));
					BPEL4ChorWriter.writeWSDL(def, outputStream);
				}
				
				AbstractBPELWriter writer = new AbstractBPELWriter();
				
				writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath + "SP24\\" + "outputSP24.bpel").eResource(), System.out, args2);
			} catch (IOException | WSDLException e) {
				e.printStackTrace();
			}
		}
		
		// // ASP30 <receive> is <onEvent>
		// ChoreographyMerger choreographyMerger29 = new
		// ChoreographyMerger(Constants.asyncPattern30RisEHChoreo);
		// ChoreographyPackage package29 =
		// choreographyMerger29.getChoreographyPackage();
		// choreographyMerger29.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputASP30RisEH.bpel");
		// Process process29 = package29.getMergedProcess();
		// if (process29 != null) {
		// try {
		// BPEL4ChorWriter.writeAbstractBPEL(package29.getMergedProcess(),
		// System.out);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		
		// // ASP30 <invoke> is in <compensationHandler>
		// ChoreographyMerger choreographyMerger30 = new
		// ChoreographyMerger(Constants.asyncPattern30SinCHChoreo);
		// ChoreographyPackage package30 =
		// choreographyMerger30.getChoreographyPackage();
		// choreographyMerger30.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputASP30SinCH.bpel");
		// Process process30 = package30.getMergedProcess();
		// if (process30 != null) {
		// try {
		// BPEL4ChorWriter.writeAbstractBPEL(package30.getMergedProcess(),
		// System.out);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		
		// // ASP30 <receive> is in <onEvent>
		// ChoreographyMerger choreographyMerger31 = new
		// ChoreographyMerger(Constants.asyncPattern30RinEHChoreo);
		// ChoreographyPackage package31 =
		// choreographyMerger31.getChoreographyPackage();
		// choreographyMerger31.merge(outputPath + "ASP30RinEH\\" +
		// "outputASP30RinEH.bpel");
		// Process process31 = package31.getMergedProcess();
		// if (process31 != null) {
		// try {
		// for (Definition def : package31.getWsdls()) {
		// FileOutputStream outputStream = new FileOutputStream(new
		// File(outputPath + "ASP30RinEH\\" + def.getQName().getLocalPart() +
		// ".wsdl"));
		// BPEL4ChorWriter.writeWSDL(def, outputStream);
		// }
		//
		// AbstractBPELWriter writer = new AbstractBPELWriter();
		// Map args2 = new HashMap();
		//
		// args2.put("", "");
		// writer.write((BPELResource) BPEL4ChorReader.readBPEL(outputPath +
		// "ASP30RinEH\\" + "outputASP30RinEH.bpel").eResource(), System.out,
		// args2);
		// } catch (IOException | WSDLException e) {
		// e.printStackTrace();
		// }
		// }
		
		// // ASP30 <invoke> is in <while>
		// ChoreographyMerger choreographyMerger32 = new
		// ChoreographyMerger(Constants.asyncPattern30SinWhileChoreo);
		// ChoreographyPackage package32 =
		// choreographyMerger32.getChoreographyPackage();
		// choreographyMerger32.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputASP30SinWhile.bpel");
		// Process process32 = package32.getMergedProcess();
		// if (process32 != null) {
		// try {
		// BPEL4ChorWriter.writeAbstractBPEL(package32.getMergedProcess(),
		// System.out);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		
		// // ASP30 <receive> is in <while>
		// ChoreographyMerger choreographyMerger33 = new
		// ChoreographyMerger(Constants.asyncPattern30RinWhileChoreo);
		// ChoreographyPackage package33 =
		// choreographyMerger33.getChoreographyPackage();
		// choreographyMerger33.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputASP30RinWhile.bpel");
		// Process process33 = package33.getMergedProcess();
		// if (process33 != null) {
		// try {
		// BPEL4ChorWriter.writeAbstractBPEL(package33.getMergedProcess(),
		// System.out);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		
		// // SP30 <receive> is in <onEvent>
		// ChoreographyMerger choreographyMerger34 = new
		// ChoreographyMerger(Constants.syncPattern30RisEHChoreo);
		// ChoreographyPackage package34 =
		// choreographyMerger34.getChoreographyPackage();
		// choreographyMerger34.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputSP30RisEH.bpel");
		// Process process34 = package34.getMergedProcess();
		// if (process34 != null) {
		// try {
		// BPEL4ChorWriter.writeAbstractBPEL(package34.getMergedProcess(),
		// System.out);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		
		// // SP30 <invoke> is in <onEvent>
		// ChoreographyMerger choreographyMerger35 = new
		// ChoreographyMerger(Constants.syncPattern30SinEHChoreo);
		// ChoreographyPackage package35 =
		// choreographyMerger35.getChoreographyPackage();
		// choreographyMerger35.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputSP30SinEH.bpel");
		// Process process35 = package35.getMergedProcess();
		// if (process35 != null) {
		// try {
		// BPEL4ChorWriter.writeAbstractBPEL(package35.getMergedProcess(),
		// System.out);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		
		// // SP30 <invoke> and <receive> in FCTE-Handler
		// ChoreographyMerger choreographyMerger36 = new
		// ChoreographyMerger(Constants.syncPattern30SRinFCTEChoreo);
		// ChoreographyPackage package36 =
		// choreographyMerger36.getChoreographyPackage();
		// choreographyMerger36.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputSP30SRinFCTE.bpel");
		// Process process36 = package36.getMergedProcess();
		// if (process36 != null) {
		// try {
		// BPEL4ChorWriter.writeAbstractBPEL(package36.getMergedProcess(),
		// System.out);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		//
		// // SP30 <invoke> and receiving <onMessage> of <Pick> in FCTE-Handler
		// ChoreographyMerger choreographyMerger37 = new
		// ChoreographyMerger(Constants.syncPattern30RisPickInFHChoreo);
		// ChoreographyPackage package37 =
		// choreographyMerger37.getChoreographyPackage();
		// choreographyMerger37.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputSP30RisPickInFH.bpel");
		// Process process37 = package37.getMergedProcess();
		// if (process37 != null) {
		// try {
		// BPEL4ChorWriter.writeAbstractBPEL(package37.getMergedProcess(),
		// System.out);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		
		// // SP30 <invoke> is in <while>
		// ChoreographyMerger choreographyMerger38 = new
		// ChoreographyMerger(Constants.syncPattern30SinWhileChoreo);
		// ChoreographyPackage package38 =
		// choreographyMerger38.getChoreographyPackage();
		// choreographyMerger38.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputSP30SinWhile.bpel");
		// Process process38 = package38.getMergedProcess();
		// if (process38 != null) {
		// try {
		// BPEL4ChorWriter.writeAbstractBPEL(package38.getMergedProcess(),
		// System.out);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		
		// // SP30 <invoke> is in <while> multiple <reply>s
		// ChoreographyMerger choreographyMerger39 = new
		// ChoreographyMerger(Constants.syncPattern30SinWhileMultiReplyChoreo);
		// ChoreographyPackage package39 =
		// choreographyMerger39.getChoreographyPackage();
		// choreographyMerger39.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputSP30SinWhileMultiReply.bpel");
		// Process process39 = package39.getMergedProcess();
		// if (process39 != null) {
		// try {
		// BPEL4ChorWriter.writeAbstractBPEL(package39.getMergedProcess(),
		// System.out);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// // SP30 <invoke> is in <while> multiple <reply>s
		// ChoreographyMerger choreographyMerger39 = new
		// ChoreographyMerger(Constants.syncPattern30SinWhileMultiReplyWSDLChoreo);
		// ChoreographyPackage package39 =
		// choreographyMerger39.getChoreographyPackage();
		// choreographyMerger39.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputSP30SinWhileMultiReplyWSDL.bpel");
		// Process process39 = package39.getMergedProcess();
		// if (process39 != null) {
		// try {
		//
		// for (Definition def : package39.getWsdls()) {
		// String preStr =
		// "D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\";
		// FileOutputStream outputStream = new FileOutputStream(new File(preStr
		// + def.getQName().getLocalPart() + ".wsdl"));
		// BPEL4ChorWriter.writeWSDL(def, outputStream);
		// }
		//
		// AbstractBPELWriter writer = new AbstractBPELWriter();
		// Map args2 = new HashMap();
		//
		// args2.put("", "");
		// writer.write((BPELResource)
		// BPEL4ChorReader.readBPEL("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputSP30SinWhileMultiReplyWSDL.bpel").eResource(),
		// System.out, args2);
		//
		// } catch (IOException | WSDLException e) {
		// e.printStackTrace();
		// }
		// }
		
		// // ASP30 <receive> is in <while>
		// ChoreographyMerger choreographyMerger40 = new
		// ChoreographyMerger(Constants.asyncPattern30RinWhileChoreo);
		// ChoreographyPackage package40 =
		// choreographyMerger40.getChoreographyPackage();
		// choreographyMerger40.merge("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputASP30RinWhile.bpel");
		// Process process40 = package40.getMergedProcess();
		// if (process40 != null) {
		// try {
		//
		// for (Definition def : package40.getWsdls()) {
		// String preStr =
		// "D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\";
		// FileOutputStream outputStream = new FileOutputStream(new File(preStr
		// + def.getQName().getLocalPart() + ".wsdl"));
		// BPEL4ChorWriter.writeWSDL(def, outputStream);
		// }
		//
		// AbstractBPELWriter writer = new AbstractBPELWriter();
		// Map args2 = new HashMap();
		//
		// args2.put("", "");
		// writer.write((BPELResource)
		// BPEL4ChorReader.readBPEL("D:\\Arbeit\\Diplom\\eclwkspBPEL\\MeineTestChoreos\\bpelContent\\MergeOutput\\outputASP30RinWhile.bpel").eResource(),
		// System.out, args2);
		//
		// } catch (IOException | WSDLException e) {
		// e.printStackTrace();
		// }
		// }
		
	}
}
