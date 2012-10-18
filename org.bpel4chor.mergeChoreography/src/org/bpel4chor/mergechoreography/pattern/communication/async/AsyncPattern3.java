package org.bpel4chor.mergechoreography.pattern.communication.async;

import org.apache.log4j.Level;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.pattern.CommunicationBasePattern;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.util.BPEL4ChorModelHelper;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Source;
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
public class AsyncPattern3 extends CommunicationBasePattern implements CommunicationPattern {
	
	public AsyncPattern3(MessageLink messageLink, ChoreographyPackage choreographyPackage, LinkEnvironment environment) {
		super(messageLink, choreographyPackage, environment);
	}
	
	@Override
	public void merge() {
		
		Activity a1 = this.environment.getSendBeforeEnvironment().get(0);
		Invoke inv = this.environment.getInvoke();
		Activity b1 = this.environment.getRecBeforeEnvironment().get(0);
		Receive rec = this.environment.getReceive();
		
		Process sender = this.environment.getSender();
		
		Process receiver = this.environment.getReceiver();
		
		Activity a1Merged = this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(), sender);
		Invoke invMerged = (Invoke) this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(), sender);
		Activity b1Merged = this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(), receiver);
		Receive recMerged = (Receive) this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(), receiver);
		
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(), sender));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(), sender));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(), receiver));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(), receiver));
		
		// Now check for outgoing links from a1 and b1
		// if they are the incoming links of invoke and receive remove them
		Source sourceToDelete = BPEL4ChorModelHelper.getMatchingSource(a1Merged, invMerged);
		a1Merged.getSources().getChildren().remove(sourceToDelete);
		
		Source sourceToDelete2 = BPEL4ChorModelHelper.getMatchingSource(b1Merged, recMerged);
		b1Merged.getSources().getChildren().remove(sourceToDelete2);
		
		BPEL4ChorModelHelper.removeActivityFromContainer(invMerged);
		BPEL4ChorModelHelper.removeActivityFromContainer(recMerged);
		
	}
	
}
