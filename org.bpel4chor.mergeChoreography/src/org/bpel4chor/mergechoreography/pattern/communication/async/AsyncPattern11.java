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

public class AsyncPattern11 extends CommunicationBasePattern implements CommunicationPattern {
	
	public AsyncPattern11(MessageLink messageLink, ChoreographyPackage choreographyPackage, LinkEnvironment environment) {
		super(messageLink, choreographyPackage, environment);
	}
	
	@Override
	public void merge() {
		Activity a1 = this.environment.getSendBeforeEnvironment().get(0);
		Invoke inv = this.environment.getInvoke();
		Receive rec2 = (Receive) this.environment.getSendAfterEnvironment().get(0);
		Activity b1 = this.environment.getRecBeforeEnvironment().get(0);
		Receive rec = this.environment.getReceive();
		Invoke inv2 = (Invoke) this.environment.getRecAfterEnvironment().get(0);
		
		Process sender = this.environment.getSender();
		
		Process receiver = this.environment.getReceiver();
		
		Activity a1Merged = this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(), sender);
		Invoke invMerged = (Invoke) this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(), sender);
		Receive rec2Merged = (Receive) this.choreographyPackage.resolveActivityInMergedProcess(rec2.getName(), sender);
		Activity b1Merged = this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(), receiver);
		Receive recMerged = (Receive) this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(), receiver);
		Invoke inv2Merged = (Invoke) this.choreographyPackage.resolveActivityInMergedProcess(inv2.getName(), receiver);
		this.logger.log(Level.INFO, "" + a1Merged);
		this.logger.log(Level.INFO, "" + invMerged);
		this.logger.log(Level.INFO, "" + rec2Merged);
		this.logger.log(Level.INFO, "" + b1Merged);
		this.logger.log(Level.INFO, "" + recMerged);
		this.logger.log(Level.INFO, "" + inv2Merged);
		
		// Now check for outgoing links from a1 and b1
		// if they are the incoming links of invoke and receive remove them
		Source sourceToDelete = BPEL4ChorModelHelper.getMatchingSource(a1Merged, invMerged);
		a1Merged.getSources().getChildren().remove(sourceToDelete);
		
		Source sourceToDelete2 = BPEL4ChorModelHelper.getMatchingSource(b1Merged, recMerged);
		b1Merged.getSources().getChildren().remove(sourceToDelete2);
		
		BPEL4ChorModelHelper.removeActivityFromContainer(invMerged);
		BPEL4ChorModelHelper.removeActivityFromContainer(inv2Merged);
		BPEL4ChorModelHelper.removeActivityFromContainer(recMerged);
		BPEL4ChorModelHelper.removeActivityFromContainer(rec2Merged);
		
	}
	
}
