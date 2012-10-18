package org.bpel4chor.mergechoreography.pattern.communication.async;

import org.apache.log4j.Level;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.pattern.CommunicationBasePattern;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.util.BPEL4ChorModelHelper;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.Variable;
import org.eclipse.emf.ecore.EObject;

public class AsyncPattern8 extends CommunicationBasePattern implements CommunicationPattern {
	
	public AsyncPattern8(MessageLink messageLink, ChoreographyPackage choreographyPackage, LinkEnvironment environment) {
		super(messageLink, choreographyPackage, environment);
	}
	
	@Override
	public void merge() {
		Activity a1 = this.environment.getSendBeforeEnvironment().get(0);
		Invoke inv = this.environment.getInvoke();
		Invoke inv2 = (Invoke) this.environment.getSendAfterEnvironment().get(0);
		Activity b1 = this.environment.getRecBeforeEnvironment().get(0);
		Receive rec = this.environment.getReceive();
		Receive rec2 = (Receive) this.environment.getRecAfterEnvironment().get(0);
		
		Process sender = this.environment.getSender();
		
		Process receiver = this.environment.getReceiver();
		
		Activity a1Merged = this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(), sender);
		Invoke invMerged = (Invoke) this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(), sender);
		Invoke inv2Merged = (Invoke) this.choreographyPackage.resolveActivityInMergedProcess(inv2.getName(), sender);
		Activity b1Merged = this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(), receiver);
		Receive recMerged = (Receive) this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(), receiver);
		Receive rec2Merged = (Receive) this.choreographyPackage.resolveActivityInMergedProcess(rec2.getName(), receiver);
		
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(), sender));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(), sender));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(inv2.getName(), sender));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(), receiver));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(), receiver));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(rec2.getName(), receiver));
		
		// Get inputVariable of invoke in merged Process
		// Get variable of receive in merged Process
		Variable inInv = this.choreographyPackage.resolveVariableInMergedProcess(inv.getInputVariable().getName(), sender);
		Variable inRec = this.choreographyPackage.resolveVariableInMergedProcess(rec.getVariable().getName(), receiver);
		Variable in2Inv = this.choreographyPackage.resolveVariableInMergedProcess(inv2.getInputVariable().getName(), sender);
		Variable in2Rec = this.choreographyPackage.resolveVariableInMergedProcess(rec2.getVariable().getName(), receiver);
		
		this.logger.log(Level.INFO, "inInv = " + inInv);
		this.logger.log(Level.INFO, "inRec = " + inRec);
		this.logger.log(Level.INFO, "in2Inv = " + in2Inv);
		this.logger.log(Level.INFO, "in2Rec = " + in2Rec);
		
		// Create new Assign with Copy from inInv to invRec
		Assign assignNew = BPELFactory.eINSTANCE.createAssign();
		
		// Set name of new Assign to "Assign" + Inv.name + Rec.name
		assignNew.setName("Assign" + inv.getName() + rec.getName());
		
		// Create Copy from inInv to invRec
		Copy copyAssign = BPELFactory.eINSTANCE.createCopy();
		Copy copyAssign2 = BPELFactory.eINSTANCE.createCopy();
		// Create new From and set inInv as variable
		From fromNew = BPELFactory.eINSTANCE.createFrom();
		From fromNew2 = BPELFactory.eINSTANCE.createFrom();
		fromNew.setVariable(inInv);
		fromNew2.setVariable(in2Inv);
		// Create new To and set inRec as variable
		To toNew = BPELFactory.eINSTANCE.createTo();
		To toNew2 = BPELFactory.eINSTANCE.createTo();
		toNew.setVariable(inRec);
		toNew2.setVariable(in2Rec);
		copyAssign.setFrom(fromNew);
		copyAssign.setTo(toNew);
		copyAssign2.setFrom(fromNew2);
		copyAssign2.setTo(toNew2);
		
		assignNew.getCopy().add(copyAssign);
		assignNew.getCopy().add(copyAssign2);
		
		BPEL4ChorModelHelper.copyTargets(invMerged, assignNew);
		BPEL4ChorModelHelper.copySources(invMerged, assignNew);
		BPEL4ChorModelHelper.copyTargets(recMerged, assignNew);
		BPEL4ChorModelHelper.copySources(recMerged, assignNew);
		
		BPEL4ChorModelHelper.replaceActivity(invMerged, assignNew);
		BPEL4ChorModelHelper.removeActivityFromContainer(recMerged);
		BPEL4ChorModelHelper.removeActivityFromContainer(rec2Merged);
		BPEL4ChorModelHelper.removeActivityFromContainer(inv2Merged);
		
	}
	
}
