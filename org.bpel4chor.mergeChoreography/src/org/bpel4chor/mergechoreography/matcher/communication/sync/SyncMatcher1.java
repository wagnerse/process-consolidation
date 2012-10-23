package org.bpel4chor.mergechoreography.matcher.communication.sync;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.exceptions.NoApplicableMatcherFoundException;
import org.bpel4chor.mergechoreography.matcher.communication.LinkMatcher;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.pattern.communication.sync.SyncPattern1;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.mergechoreography.util.LinkEnvironmentAnalyzer;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Receive;

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
	
	private Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
	
	
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
		
		// First check if we have a messageExchange set
		// For this we need the surrounding link environment
		LinkEnvironmentAnalyzer analyzer = new LinkEnvironmentAnalyzer(link, choreographyPackage);
		LinkEnvironment environment = analyzer.getEnvironment(link.getSender());
		
		Invoke inv = environment.getInvoke();
		Receive rec = environment.getReceive();
		
		if (rec.getMessageExchange() == null) {
			this.logger.log(Level.INFO, "We have a simple pattern without messageExchange set :) !!!");
			return new SyncPattern1(link, choreographyPackage, environment);
		}
		return null;
	}
}
