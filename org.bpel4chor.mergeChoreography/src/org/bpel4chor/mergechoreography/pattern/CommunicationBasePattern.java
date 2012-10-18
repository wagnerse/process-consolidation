package org.bpel4chor.mergechoreography.pattern;

import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.model.topology.impl.MessageLink;

/**
 * Base class for Merge Patterns
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public abstract class CommunicationBasePattern {
	
	/** The Choreograohy Package */
	protected ChoreographyPackage choreographyPackage;
	
	/** The Message Link */
	protected MessageLink messageLink;
	
	/** The Information about the analyzed environment of the link */
	protected LinkEnvironment environment;
	
	protected Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
	
	
	public CommunicationBasePattern(MessageLink messageLink, ChoreographyPackage choreographyPackage, LinkEnvironment environment) {
		this.choreographyPackage = choreographyPackage;
		this.messageLink = messageLink;
		this.environment = environment;
	}
	
}
