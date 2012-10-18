package org.bpel4chor.mergechoreography.matcher.communication.async;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.matcher.communication.LinkMatcher;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.pattern.communication.async.AsyncPattern9;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.mergechoreography.util.MultiLinkEnvironmentAnalyzer;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Activity;

/**
 * Matcher Class for Matching BPEL Process Behavior (Async)
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class AsyncMatcher9 implements LinkMatcher {
	
	private Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
	
	
	/**
	 * Method for detecting matching merge Pattern
	 * 
	 * @param link {@link MessageLink} to be analyzed
	 * @param choreographyPackage The {@link ChoreographyPackage} holding all
	 *            data
	 * @return {@link CommunicationPattern} to be applied
	 */
	@Override
	public CommunicationPattern match(MessageLink link, ChoreographyPackage choreographyPackage) {
		
		// First check, that we have a single sender (!!)
		if (link.getSenders().size() > 0) {
			
			// Test Call to our new LinkEnvironmentAnalyzer
			MultiLinkEnvironmentAnalyzer analyzer = new MultiLinkEnvironmentAnalyzer(link, choreographyPackage);
			List<LinkEnvironment> environments = analyzer.getEnvironments();
			
			this.printEnvironments(environments, link);
			
			// Now we check if in every linkenvironment we have one pre- and one
			// succeeding activity on the sender side and the same on the
			// receiver side
			for (LinkEnvironment linkEnvironment : environments) {
				if ((linkEnvironment.getSendBeforeEnvironment().size() != 1) && (linkEnvironment.getSendAfterEnvironment().size() != 1) && (linkEnvironment.getRecBeforeEnvironment().size() != 1) && (linkEnvironment.getRecAfterEnvironment().size() != 1)) {
					this.logger.log(Level.INFO, "----------------------Pattern mismatch in AsyncMatcher9!!!---------------------");
					return null;
				}
			}
			this.logger.log(Level.INFO, "AsyncPattern9 found for link => " + link.getName() + " with sending Processes => ");// +
																																// environment.getSender().getName()
																																// +
																																// ",  and receiving Process => "
																																// +
																																// environment.getReceiver().getName()
																																// +
																																// ".");
			for (LinkEnvironment environment : environments) {
				this.logger.log(Level.INFO, "	" + environment.getSender().getName());
			}
			this.logger.log(Level.INFO, "And receiving Process => " + environments.get(0).getReceiver().getName());
			return new AsyncPattern9(link, choreographyPackage, environments);
		}
		return null;
	}
	
	/**
	 * Logging Method for printing analyzed LinkEnvironments
	 * 
	 * @param environments The {@link LinkEnvironment}s to be printed
	 * @param link The concerned {@link MessageLink}
	 */
	private void printEnvironments(List<LinkEnvironment> environments, MessageLink link) {
		this.logger.log(Level.INFO, "================================================================");
		this.logger.log(Level.INFO, "Following linkenvironments found for link " + link.getName());
		for (LinkEnvironment linkEnvironment : environments) {
			this.logger.log(Level.INFO, "---------------------------------------------------------------");
			this.logger.log(Level.INFO, "Sender => " + linkEnvironment.getSender().getName());
			this.logger.log(Level.INFO, "Receiver => " + linkEnvironment.getReceiver().getName());
			this.logger.log(Level.INFO, "Activities preceding the invoke are : ");
			for (Activity activity : linkEnvironment.getSendBeforeEnvironment()) {
				this.logger.log(Level.INFO, "	 => " + activity.getName());
			}
			this.logger.log(Level.INFO, "Invoke activity =>  " + linkEnvironment.getInvoke().getName());
			this.logger.log(Level.INFO, "Activities succeeding the invoke are : ");
			for (Activity activity : linkEnvironment.getSendAfterEnvironment()) {
				this.logger.log(Level.INFO, "	 => " + activity.getName());
			}
			this.logger.log(Level.INFO, "Activities preceding the receive are : ");
			for (Activity activity : linkEnvironment.getRecBeforeEnvironment()) {
				this.logger.log(Level.INFO, "	 => " + activity.getName());
			}
			this.logger.log(Level.INFO, "Receive activity =>  " + linkEnvironment.getReceive().getName());
			this.logger.log(Level.INFO, "Activities succeeding the receive are : ");
			for (Activity activity : linkEnvironment.getRecAfterEnvironment()) {
				this.logger.log(Level.INFO, "	 => " + activity.getName());
			}
			this.logger.log(Level.INFO, "---------------------------------------------------------------");
		}
		this.logger.log(Level.INFO, "================================================================");
	}
}
