package org.bpel4chor.mergechoreography.matcher.communication.sync;

import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.exceptions.NoApplicableMatcherFoundException;
import org.bpel4chor.mergechoreography.matcher.communication.LinkMatcher;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.pattern.communication.sync.SyncPattern1;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.model.topology.impl.MessageLink;

/**
 * Matcher Class for Matching BPEL Process Behavior (Sync)
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class SyncMatcher1 implements LinkMatcher {
	
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
	@Override
	public CommunicationPattern match(MessageLink link, ChoreographyPackage choreographyPackage) {
		return new SyncPattern1(link, choreographyPackage, new LinkEnvironment());
	}
}
