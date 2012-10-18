package org.bpel4chor.mergechoreography.matcher.communication.async;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.matcher.communication.LinkMatcher;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.pattern.communication.async.AsyncPattern4;
import org.bpel4chor.mergechoreography.util.BPEL4ChorModelHelper;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.mergechoreography.util.LinkEnvironmentAnalyzer;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Receive;

/**
 * Matcher Class for Matching BPEL Process Behavior (Async)
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class AsyncMatcher4 implements LinkMatcher {
	
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
		if (link.getSenders().size() == 0) {
			
			// Test Call to our new LinkEnvironmentAnalyzer
			LinkEnvironmentAnalyzer analyzer = new LinkEnvironmentAnalyzer(link, choreographyPackage);
			LinkEnvironment environment = analyzer.getEnvironment(link.getSender());
			
			Activity a1 = null;
			Activity a2 = null;
			Activity b1 = null;
			Activity b2 = null;
			Invoke inv = null;
			Receive rec = null;
			
			// Set the activities from the pattern
			// Here we already can match, we only want just one
			// preceding activity for the involved invoke and one succeeding
			// The receive activity should just be followed by one activity
			if ((environment.getSendBeforeEnvironment().size() == 1) && (environment.getSendAfterEnvironment().size() == 1) && (environment.getRecBeforeEnvironment().size() == 0) && (environment.getRecAfterEnvironment().size() == 1)) {
				a1 = environment.getSendBeforeEnvironment().get(0);
				a2 = environment.getSendAfterEnvironment().get(0);
				b1 = null;
				b2 = environment.getRecAfterEnvironment().get(0);
				inv = environment.getInvoke();
				rec = environment.getReceive();
			} else {
				return null;
			}
			
			// Check if all activities surrounding the communicating ones are
			// non-communicating
			if (BPEL4ChorModelHelper.isNonCommunicatingActivity(a1) && BPEL4ChorModelHelper.isNonCommunicatingActivity(a2) && BPEL4ChorModelHelper.isNonCommunicatingActivity(b2)) {
				// We found our Pattern !!!
				this.logger.log(Level.INFO, "AsyncPattern4 found for link => " + link.getName() + " with sending Process => " + environment.getSender().getName() + ",  and receiving Process => " + environment.getReceiver().getName() + ".");
				this.logger.log(Level.INFO, "       (A1)");
				this.logger.log(Level.INFO, "        |");
				this.logger.log(Level.INFO, "    (Invoke)  -> (Receive)");
				this.logger.log(Level.INFO, "        |           |");
				this.logger.log(Level.INFO, "       (A2)        (B2)");
				this.logger.log(Level.INFO, "With A1 : " + a1.getName());
				this.logger.log(Level.INFO, "With Invoke : " + inv.getName());
				this.logger.log(Level.INFO, "With A2 : " + a2.getName());
				this.logger.log(Level.INFO, "With B1 : " + b1);
				this.logger.log(Level.INFO, "With Receive : " + rec.getName());
				this.logger.log(Level.INFO, "With B2 : " + b2.getName());
				// Set MessageLink to visited
				choreographyPackage.addVisitedLink(link);
				return new AsyncPattern4(link, choreographyPackage, environment);
			}
			
		}
		return null;
		
	}
}
