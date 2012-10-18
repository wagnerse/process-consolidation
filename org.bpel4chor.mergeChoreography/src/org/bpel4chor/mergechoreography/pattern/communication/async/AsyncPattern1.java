package org.bpel4chor.mergechoreography.pattern.communication.async;

import org.apache.log4j.Level;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.pattern.CommunicationBasePattern;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.util.BPEL4ChorModelHelper;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Variable;
import org.eclipse.emf.ecore.EObject;

/**
 * Pattern for Merging Simple Invoke
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class AsyncPattern1 extends CommunicationBasePattern implements CommunicationPattern {
	
	public AsyncPattern1(MessageLink messageLink, ChoreographyPackage choreographyPackage, LinkEnvironment environment) {
		super(messageLink, choreographyPackage, environment);
	}
	
	@Override
	public void merge() {
		
		Activity a1 = this.environment.getSendBeforeEnvironment().get(0);
		Invoke inv = this.environment.getInvoke();
		Activity a2 = this.environment.getSendAfterEnvironment().get(0);
		Activity b1 = this.environment.getRecBeforeEnvironment().get(0);
		Receive rec = this.environment.getReceive();
		
		Process sender = this.environment.getSender();
		
		Process receiver = this.environment.getReceiver();
		
		Activity a1Merged = this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(), sender);
		Invoke invMerged = (Invoke) this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(), sender);
		Activity a2Merged = this.choreographyPackage.resolveActivityInMergedProcess(a2.getName(), sender);
		Activity b1Merged = this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(), receiver);
		Receive recMerged = (Receive) this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(), receiver);
		
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(), sender));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(), sender));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(a2.getName(), sender));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(), receiver));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(), receiver));
		
		// TODO: Eigentlich sollte hier erstmal nach Links gechecked werden, wir
		// machen den Scheiﬂ sp‰ter ;)
		
		// Get inputVariable of invoke in merged Process
		// Get variable of receive in merged Process
		Variable inInv = this.choreographyPackage.resolveVariableInMergedProcess(inv.getInputVariable().getName(), sender);
		Variable inRec = this.choreographyPackage.resolveVariableInMergedProcess(rec.getVariable().getName(), receiver);
		
		this.logger.log(Level.INFO, "inInv = " + inInv);
		this.logger.log(Level.INFO, "inRec = " + inRec);
		
		// Get all Links from A1, which have Invoke as target
		Source sourceToBend = BPEL4ChorModelHelper.getMatchingSource(a1Merged, invMerged);
		
		// Get all Links from A2, which have Invoke as source
		Target targetToBend = BPEL4ChorModelHelper.getMatchingTarget(invMerged, a2Merged);
		
		// Get all Links from B1, which have Receive as target
		Source sourceToBend2 = BPEL4ChorModelHelper.getMatchingSource(b1Merged, recMerged);
		// Remove these links from B1
		b1Merged.getSources().getChildren().remove(sourceToBend2);
		
		// Remove all links from A1 with invoke as target
		a1Merged.getSources().getChildren().remove(sourceToBend);
		
		// Set new Links which have A2 as target and invoke as source
		// to new source A1 as source
		Source source = BPELFactory.eINSTANCE.createSource();
		source.setLink(targetToBend.getLink());
		a1Merged.getSources().getChildren().add(source);
		
		BPEL4ChorModelHelper.removeActivityFromContainer(invMerged);
		BPEL4ChorModelHelper.removeActivityFromContainer(recMerged);
		
	}
	
}
