package org.bpel4chor.mergechoreography.pattern.communication.async;

import org.apache.log4j.Level;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.exceptions.TargetNotFoundInActivityException;
import org.bpel4chor.mergechoreography.pattern.CommunicationBasePattern;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.util.BPEL4ChorModelHelper;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.Variable;
import org.eclipse.emf.ecore.EObject;

public class AsyncPattern10 extends CommunicationBasePattern implements CommunicationPattern {
	
	public AsyncPattern10(MessageLink messageLink, ChoreographyPackage choreographyPackage, LinkEnvironment environment) {
		super(messageLink, choreographyPackage, environment);
	}
	
	@Override
	public void merge() {
		
		Activity a1 = this.environment.getSendBeforeEnvironment().get(0);
		Invoke inv = this.environment.getInvoke();
		Activity a2 = this.environment.getSendAfterEnvironment().get(0);
		Activity b1 = this.environment.getRecBeforeEnvironment().get(0);
		Receive rec = this.environment.getReceive();
		Activity b2 = this.environment.getRecAfterEnvironment().get(0);
		
		Process sender = this.environment.getSender();
		
		Process receiver = this.environment.getReceiver();
		
		Activity a1Merged = this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(), sender);
		Invoke invMerged = (Invoke) this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(), sender);
		Activity a2Merged = this.choreographyPackage.resolveActivityInMergedProcess(a2.getName(), sender);
		Activity b1Merged = this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(), receiver);
		Receive recMerged = (Receive) this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(), receiver);
		Activity b2Merged = this.choreographyPackage.resolveActivityInMergedProcess(b2.getName(), receiver);
		this.logger.log(Level.INFO, "" + a1Merged);
		this.logger.log(Level.INFO, "" + invMerged);
		this.logger.log(Level.INFO, "" + a2Merged);
		this.logger.log(Level.INFO, "" + b1Merged);
		this.logger.log(Level.INFO, "" + recMerged);
		this.logger.log(Level.INFO, "" + b2Merged);
		
		// TODO: Eigentlich sollte hier erstmal nach Links gechecked werden,
		// machen den Scheiﬂ sp‰ter ;)
		
		// Get inputVariable of invoke in merged Process
		// Get variable of receive in merged Process
		Variable inInv = this.choreographyPackage.resolveVariableInMergedProcess(inv.getInputVariable().getName(), sender);
		Variable inRec = this.choreographyPackage.resolveVariableInMergedProcess(rec.getVariable().getName(), receiver);
		
		this.logger.log(Level.INFO, "inInv = " + inInv);
		this.logger.log(Level.INFO, "inRec = " + inRec);
		
		// Create new Assign with Copy from inInv to invRec
		Assign assignNew = BPELFactory.eINSTANCE.createAssign();
		
		// Set name of new Assign to "Assign" + Inv.name + Rec.name
		assignNew.setName("Assign" + inv.getName() + rec.getName());
		
		// Create Copy from inInv to invRec
		Copy copyAssign = BPELFactory.eINSTANCE.createCopy();
		// Create new From and set inInv as variable
		From fromNew = BPELFactory.eINSTANCE.createFrom();
		fromNew.setVariable(inInv);
		// Create new To and set inRec as variable
		To toNew = BPELFactory.eINSTANCE.createTo();
		toNew.setVariable(inRec);
		copyAssign.setFrom(fromNew);
		copyAssign.setTo(toNew);
		
		assignNew.getCopy().add(copyAssign);
		
		BPEL4ChorModelHelper.copyTargets(invMerged, assignNew);
		BPEL4ChorModelHelper.copySources(invMerged, assignNew);
		BPEL4ChorModelHelper.copySources(recMerged, assignNew);
		BPEL4ChorModelHelper.copyTargets(recMerged, assignNew);
		
		// Find the link which originates in b1 und targets recMerged and
		// remove him from the targets of the new assign
		Source sourceB1 = BPEL4ChorModelHelper.getMatchingSource(b1Merged, recMerged);
		Source sourceRec = BPEL4ChorModelHelper.getMatchingSource(recMerged, b2Merged);
		try {
			BPEL4ChorModelHelper.removeTargetLink(assignNew, sourceB1.getLink().getName());
		} catch (TargetNotFoundInActivityException e) {
			e.printStackTrace();
		}
		// Instead create a new target with this link in b2
		Target newTarget = BPELFactory.eINSTANCE.createTarget();
		newTarget.setLink(sourceB1.getLink());
		b2Merged.getTargets().getChildren().add(newTarget);
		
		// Now we have to change the "or" semantic of the target links
		// to "and" semantic
		// We introduce a new joinCondition and "and" the target links
		// originating in the new assign and the old b1
		if (b2Merged.getTargets().getJoinCondition() == null) {
			b2Merged.getTargets().setJoinCondition(BPELFactory.eINSTANCE.createCondition());
		}
		Condition joinCondition = b2Merged.getTargets().getJoinCondition();
		
		String expression = null;
		// Check if there is already a condition value
		if (joinCondition.getBody() != null) {
			expression = "(" + joinCondition.getBody() + ")" + " OR ($" + sourceB1.getLink().getName() + " AND $" + sourceRec.getLink().getName() + ")";
			joinCondition.setBody(expression);
		} else {
			expression = "$" + sourceB1.getLink().getName() + " AND $" + sourceRec.getLink().getName();
			joinCondition.setBody(expression);
		}
		
		BPEL4ChorModelHelper.replaceActivity(invMerged, assignNew);
		BPEL4ChorModelHelper.removeActivityFromContainer(recMerged);
		
	}
	
}
