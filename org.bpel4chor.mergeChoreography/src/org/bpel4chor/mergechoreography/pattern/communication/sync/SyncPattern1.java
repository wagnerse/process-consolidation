package org.bpel4chor.mergechoreography.pattern.communication.sync;

import org.apache.log4j.Level;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.pattern.CommunicationBasePattern;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.util.BPEL4ChorModelHelper;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Reply;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
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
public class SyncPattern1 extends CommunicationBasePattern implements CommunicationPattern {
	
	public SyncPattern1(MessageLink messageLink, ChoreographyPackage choreographyPackage, LinkEnvironment environment) {
		super(messageLink, choreographyPackage, environment);
	}
	
	@Override
	public void merge() {
		
		MessageLink replyLink = BPEL4ChorModelHelper.findReplyingMessageLink(this.messageLink, this.choreographyPackage);
		this.logger.log(Level.INFO, "Found Replying ML => " + replyLink.getName());
		
		Invoke invOrig = this.environment.getInvoke();
		Receive recOrig = this.environment.getReceive();
		
		// The receiving and replying process
		Process recReplyOrig = this.environment.getReceiver();
		
		// The invoking process
		Process sender = this.environment.getSender();
		
		Reply replyOrig = (Reply) BPEL4ChorModelHelper.resolveActivity(replyLink.getSendActivity(), recReplyOrig);
		
		this.logger.log(Level.INFO, "Found following data: ");
		this.logger.log(Level.INFO, "invoke => " + invOrig.getName());
		this.logger.log(Level.INFO, "receive => " + recOrig.getName());
		this.logger.log(Level.INFO, "reply => " + replyOrig.getName());
		
		Invoke invMerged = (Invoke) this.choreographyPackage.resolveActivityInMergedProcess(invOrig.getName(), sender);
		Receive recMerged = (Receive) this.choreographyPackage.resolveActivityInMergedProcess(recOrig.getName(), recReplyOrig);
		Reply replyMerged = (Reply) this.choreographyPackage.resolveActivityInMergedProcess(replyOrig.getName(), recReplyOrig);
		
		this.logger.log(Level.INFO, "Found following data in merged Process: ");
		this.logger.log(Level.INFO, "invoke => " + invMerged.getName());
		this.logger.log(Level.INFO, "receive => " + recMerged.getName());
		this.logger.log(Level.INFO, "reply => " + replyMerged.getName());
		
		// Now we need to get all activities between receive and reply
		EObject recMergedContainer = recMerged.eContainer();
		this.logger.log(Level.INFO, "recMergedContainer => " + recMergedContainer);
		int posRecInCont = recMergedContainer.eContents().indexOf(recMerged);
		this.logger.log(Level.INFO, "Pos of recMerged in this container => " + posRecInCont);
		int posReplyInCont = recMergedContainer.eContents().indexOf(replyMerged);
		this.logger.log(Level.INFO, "Pos of replyMerged in this container => " + posReplyInCont);
		
		EList<Activity> toBeCutActs = new BasicEList<>();
		for (int i = (posRecInCont + 1); i < posReplyInCont; i++) {
			toBeCutActs.add((Activity) recMergedContainer.eContents().get(i));
		}
		
		this.logger.log(Level.INFO, "Found following activities between receive and reply to cut: ");
		for (Activity activity : toBeCutActs) {
			this.logger.log(Level.INFO, " " + activity.getName());
		}
		
		// sendProc =
		// BPEL4ChorModelHelper.resolveProcessByName(link.getSender(),
		// choreographyPackage);
		// recProc =
		// BPEL4ChorModelHelper.resolveProcessByName(link.getReceiver(),
		// choreographyPackage);
		// sending =
		// BPEL4ChorModelHelper.resolveActivity(link.getSendActivity(),
		// sendProc);
		// receiving =
		// BPEL4ChorModelHelper.resolveActivity(link.getReceiveActivity(),
		// recProc);
		
		// Activity a1 = this.environment.getSendBeforeEnvironment().get(0);
		// Invoke inv = this.environment.getInvoke();
		// Activity a2 = this.environment.getSendAfterEnvironment().get(0);
		// Activity b1 = this.environment.getRecBeforeEnvironment().get(0);
		// Receive rec = this.environment.getReceive();
		//
		// Process sender = this.environment.getSender();
		//
		// Process receiver = this.environment.getReceiver();
		//
		// Activity a1Merged =
		// this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(),
		// sender);
		// Invoke invMerged = (Invoke)
		// this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(),
		// sender);
		// Activity a2Merged =
		// this.choreographyPackage.resolveActivityInMergedProcess(a2.getName(),
		// sender);
		// Activity b1Merged =
		// this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(),
		// receiver);
		// Receive recMerged = (Receive)
		// this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(),
		// receiver);
		//
		// this.logger.log(Level.INFO, "" +
		// this.choreographyPackage.resolveActivityInMergedProcess(a1.getName(),
		// sender));
		// this.logger.log(Level.INFO, "" +
		// this.choreographyPackage.resolveActivityInMergedProcess(inv.getName(),
		// sender));
		// this.logger.log(Level.INFO, "" +
		// this.choreographyPackage.resolveActivityInMergedProcess(a2.getName(),
		// sender));
		// this.logger.log(Level.INFO, "" +
		// this.choreographyPackage.resolveActivityInMergedProcess(rec.getName(),
		// receiver));
		// this.logger.log(Level.INFO, "" +
		// this.choreographyPackage.resolveActivityInMergedProcess(b1.getName(),
		// receiver));
		//
		// // TODO: Eigentlich sollte hier erstmal nach Links gechecked werden,
		// wir
		// // machen den Scheiß später ;)
		//
		// // Get inputVariable of invoke in merged Process
		// // Get variable of receive in merged Process
		// Variable inInv =
		// this.choreographyPackage.resolveVariableInMergedProcess(inv.getInputVariable().getName(),
		// sender);
		// Variable inRec =
		// this.choreographyPackage.resolveVariableInMergedProcess(rec.getVariable().getName(),
		// receiver);
		//
		// this.logger.log(Level.INFO, "inInv = " + inInv);
		// this.logger.log(Level.INFO, "inRec = " + inRec);
		//
		// // Get all Links from A1, which have Invoke as target
		// Source sourceToBend =
		// BPEL4ChorModelHelper.getMatchingSource(a1Merged, invMerged);
		//
		// // Get all Links from A2, which have Invoke as source
		// Target targetToBend =
		// BPEL4ChorModelHelper.getMatchingTarget(invMerged, a2Merged);
		//
		// // Get all Links from B1, which have Receive as target
		// Source sourceToBend2 =
		// BPEL4ChorModelHelper.getMatchingSource(b1Merged, recMerged);
		// // Remove these links from B1
		// b1Merged.getSources().getChildren().remove(sourceToBend2);
		//
		// // Remove all links from A1 with invoke as target
		// a1Merged.getSources().getChildren().remove(sourceToBend);
		//
		// // Set new Links which have A2 as target and invoke as source
		// // to new source A1 as source
		// Source source = BPELFactory.eINSTANCE.createSource();
		// source.setLink(targetToBend.getLink());
		// a1Merged.getSources().getChildren().add(source);
		//
		// BPEL4ChorModelHelper.removeActivityFromContainer(invMerged);
		// BPEL4ChorModelHelper.removeActivityFromContainer(recMerged);
		
	}
	
}
