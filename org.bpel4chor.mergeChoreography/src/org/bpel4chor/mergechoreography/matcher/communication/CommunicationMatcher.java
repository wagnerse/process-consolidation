package org.bpel4chor.mergechoreography.matcher.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.exceptions.NoApplicableMatcherFoundException;
import org.bpel4chor.mergechoreography.matcher.communication.async.AsyncMatcher1;
import org.bpel4chor.mergechoreography.matcher.communication.async.AsyncMatcher10;
import org.bpel4chor.mergechoreography.matcher.communication.async.AsyncMatcher11;
import org.bpel4chor.mergechoreography.matcher.communication.async.AsyncMatcher2;
import org.bpel4chor.mergechoreography.matcher.communication.async.AsyncMatcher3;
import org.bpel4chor.mergechoreography.matcher.communication.async.AsyncMatcher4;
import org.bpel4chor.mergechoreography.matcher.communication.async.AsyncMatcher5;
import org.bpel4chor.mergechoreography.matcher.communication.async.AsyncMatcher7;
import org.bpel4chor.mergechoreography.matcher.communication.async.AsyncMatcher8;
import org.bpel4chor.mergechoreography.matcher.communication.async.AsyncMatcher9;
import org.bpel4chor.mergechoreography.matcher.communication.sync.SyncMatcher1;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.util.BPEL4ChorModelHelper;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.bpel4chor.utils.MyBPELUtils;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.CatchAll;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Process;

/**
 * Matcher Class for Matching BPEL Process Behavior
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class CommunicationMatcher implements Serializable {
	
	private static final long serialVersionUID = 471002085247211487L;
	
	protected Logger log;
	
	/** Lists of registered LinkMatcher */
	private static List<LinkMatcher> asyncMatcher;
	private static List<LinkMatcher> syncMatcher;
	
	static {
		CommunicationMatcher.asyncMatcher = new ArrayList<>();
		CommunicationMatcher.asyncMatcher.add(new AsyncMatcher9());
		CommunicationMatcher.asyncMatcher.add(new AsyncMatcher1());
		CommunicationMatcher.asyncMatcher.add(new AsyncMatcher2());
		CommunicationMatcher.asyncMatcher.add(new AsyncMatcher3());
		CommunicationMatcher.asyncMatcher.add(new AsyncMatcher4());
		CommunicationMatcher.asyncMatcher.add(new AsyncMatcher5());
		CommunicationMatcher.asyncMatcher.add(new AsyncMatcher7());
		CommunicationMatcher.asyncMatcher.add(new AsyncMatcher8());
		CommunicationMatcher.asyncMatcher.add(new AsyncMatcher10());
		CommunicationMatcher.asyncMatcher.add(new AsyncMatcher11());
		CommunicationMatcher.syncMatcher = new ArrayList<>();
		CommunicationMatcher.syncMatcher.add(new SyncMatcher1());
	}
	
	
	/**
	 * Method for detecting matching merge Pattern
	 * 
	 * @param mergedProcess Merged BPEL Process
	 * @param process PBD to be merged in
	 * @param choreographyPackage The {@link ChoreographyPackage} holding all
	 *            data
	 * @return {@link Pattern} to be applied
	 * @throws NoApplicableMatcherFoundException
	 */
	public CommunicationPattern match(MessageLink link, ChoreographyPackage choreographyPackage) throws NoApplicableMatcherFoundException {
		// Check if link.sendActivity is a sync or async call
		this.log.log(Level.INFO, "Running CommunicationMatcher .....");
		this.log.log(Level.INFO, "For MessageLink => " + link.getName());
		Process sender = null;
		List<Process> senders = null;
		Invoke invoke = null;
		if (link.getSenders().size() > 0) {
			senders = new ArrayList<>();
			for (String senderProc : link.getSenders()) {
				senders.add(BPEL4ChorModelHelper.resolveProcessByName(senderProc, choreographyPackage));
			}
			invoke = (Invoke) MyBPELUtils.resolveActivity(link.getSendActivity(), senders.get(0));
		} else {
			sender = BPEL4ChorModelHelper.resolveProcessByName(link.getSender(), choreographyPackage);
			invoke = (Invoke) BPEL4ChorModelHelper.resolveActivity(link.getSendActivity(), sender);
			
			// TODO: Richtig impln !!!
			if (invoke == null) {
				if ((sender.getFaultHandlers() != null) && ((sender.getFaultHandlers().getCatch().size() > 0) || (sender.getFaultHandlers().getCatchAll() != null))) {
					CatchAll catchAll = sender.getFaultHandlers().getCatchAll();
					for (Activity act : ((Flow) catchAll.getActivity()).getActivities()) {
						if (act.getName().equals(link.getSendActivity())) {
							invoke = (Invoke) act;
						}
					}
				}
			}
			
			this.log.log(Level.INFO, "FOUND invoke => " + invoke);
		}
		if (invoke.getOutputVariable() == null) {
			// We have async communication
			this.log.log(Level.INFO, "Async Invoke found, now running AsyncMatcher ....");
			for (LinkMatcher matcher : CommunicationMatcher.asyncMatcher) {
				CommunicationPattern pattern = matcher.match(link, choreographyPackage);
				if (pattern != null) {
					return pattern;
				}
			}
			throw new NoApplicableMatcherFoundException("No Matching pattern found for MessageLink " + link.getName());
		} else {
			// We have sync communication
			this.log.log(Level.INFO, "Sync Invoke found, now running SyncMatcher ....");
			for (LinkMatcher matcher : CommunicationMatcher.syncMatcher) {
				CommunicationPattern pattern = matcher.match(link, choreographyPackage);
				if (pattern != null) {
					return pattern;
				}
			}
			throw new NoApplicableMatcherFoundException("No Matching pattern found for MessageLink " + link.getName());
		}
	}
	
	public CommunicationMatcher() {
		this.log = Logger.getLogger(this.getClass().getPackage().getName());
	}
}
