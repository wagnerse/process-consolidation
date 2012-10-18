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
public class AsyncPattern2 extends CommunicationBasePattern implements CommunicationPattern {
	
	public AsyncPattern2(MessageLink messageLink, ChoreographyPackage choreographyPackage, LinkEnvironment environment) {
		super(messageLink, choreographyPackage, environment);
	}
	
	@Override
	public void merge() {
		
		Activity a1 = this.environment.getSendBeforeEnvironment().get(0);
		Invoke inv = this.environment.getInvoke();
		Activity b1 = this.environment.getRecBeforeEnvironment().get(0);
		Receive rec = this.environment.getReceive();
		Activity b2 = this.environment.getRecAfterEnvironment().get(0);
		
		Process sender = this.environment.getSender();
		
		Process receiver = this.environment.getReceiver();
		
		Activity a1Merged = this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(), sender);
		Invoke invMerged = (Invoke) this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(), sender);
		Activity b1Merged = this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(), receiver);
		Receive recMerged = (Receive) this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(), receiver);
		Activity b2Merged = this.choreographyPackage.resolveActivityInMergedProcess(b2.getName(), receiver);
		
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(), sender));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(), sender));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(), receiver));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(), receiver));
		this.logger.log(Level.INFO, "" + this.choreographyPackage.resolveActivityInMergedProcess(b2.getName(), receiver));
		
		// Check ob invoke und receive im gleichen Container enthalten sind
		// if (invMerged.eContainer() == recMerged.eContainer()) {
		
		// Get inputVariable of invoke in merged Process
		// Get variable of receive in merged Process
		Variable inInv = this.choreographyPackage.resolveVariableInMergedProcess(inv.getInputVariable().getName(), sender);
		Variable inRec = this.choreographyPackage.resolveVariableInMergedProcess(rec.getVariable().getName(), receiver);
		
		this.logger.log(Level.INFO, "inInv = " + inInv);
		this.logger.log(Level.INFO, "inRec = " + inRec);
		
		// Get all Links from A1, which have Invoke as target
		Source sourceToBend = BPEL4ChorModelHelper.getMatchingSource(a1Merged, invMerged);
		String jc1 = BPEL4ChorModelHelper.getJoinConditionForTarget(invMerged, sourceToBend.getLink().getName());
		
		// Get all Links from B1, which have Receive as target
		Source sourceToBend2 = BPEL4ChorModelHelper.getMatchingSource(b1Merged, recMerged);
		String jc2 = BPEL4ChorModelHelper.getJoinConditionForTarget(recMerged, sourceToBend2.getLink().getName());
		
		// Get all Links from B2, which have Receive as source
		Target targetToBend = BPEL4ChorModelHelper.getMatchingTarget(recMerged, b2Merged);
		
		// Remove all links from B2, which have receive as source
		this.logger.log(Level.INFO, "Kicking following target links from B2 => " + b2Merged.getName());
		this.logger.log(Level.INFO, "	 => " + targetToBend.getLink().getName());
		
		b2Merged.getTargets().getChildren().remove(targetToBend);
		// Check if removedTarget has been involved in a joinCondition, if
		// so remove it
		String jc3 = BPEL4ChorModelHelper.getJoinConditionForTarget(b2Merged, targetToBend.getLink().getName());
		if (jc3 != null) {
			String conditonExp = b2Merged.getTargets().getJoinCondition().getBody().toString();
			conditonExp = conditonExp.replace("$" + jc3, "");
			b2Merged.getTargets().getJoinCondition().setBody(conditonExp);
		}
		
		this.logger.log(Level.INFO, "jc1  => " + jc1);
		this.logger.log(Level.INFO, "jc2  => " + jc2);
		// If jc1 and jc2 are not null extend joinCondition with them
		if (jc1 != null) {
			String conditonExp = b2Merged.getTargets().getJoinCondition().getBody().toString();
			this.logger.log(Level.INFO, "conditonExp  => " + conditonExp);
			if ((conditonExp == null) || (conditonExp.equals(""))) {
				// Its empty
				conditonExp = "$" + jc1;
				b2Merged.getTargets().getJoinCondition().setBody(conditonExp);
			} else {
				conditonExp += " OR $" + jc1;
				b2Merged.getTargets().getJoinCondition().setBody(conditonExp);
			}
		}
		if (jc2 != null) {
			String conditonExp = b2Merged.getTargets().getJoinCondition().getBody().toString();
			this.logger.log(Level.INFO, "conditonExp  => " + conditonExp);
			if ((conditonExp == null) || (conditonExp.equals(""))) {
				// Its empty
				conditonExp = "$" + jc2;
				b2Merged.getTargets().getJoinCondition().setBody(conditonExp);
			} else {
				conditonExp += " OR $" + jc2;
				b2Merged.getTargets().getJoinCondition().setBody(conditonExp);
			}
		}
		
		// Now set all links from B1 and A1 (which have Rec/Inv as target)
		// as target links for B2
		Target target = BPELFactory.eINSTANCE.createTarget();
		target.setLink(sourceToBend.getLink());
		b2Merged.getTargets().getChildren().add(target);
		
		target = BPELFactory.eINSTANCE.createTarget();
		target.setLink(sourceToBend2.getLink());
		b2Merged.getTargets().getChildren().add(target);
		
		// Remove sending and receiving activities from their
		// containers
		BPEL4ChorModelHelper.removeActivityFromContainer(recMerged);
		BPEL4ChorModelHelper.removeActivityFromContainer(invMerged);
		
	}
	
	// }
}
