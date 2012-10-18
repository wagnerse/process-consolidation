package org.bpel4chor.mergechoreography.pattern;

import java.util.List;

import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.model.topology.impl.MessageLink;

/**
 * Base class for Merge Patterns with Multiple senders
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public abstract class MultiCommunicationBasePattern {
	
	/** The Choreograohy Package */
	protected ChoreographyPackage choreographyPackage;
	
	/** The Message Link */
	protected MessageLink messageLink;
	
	/** The Information about the analyzed environment of the link */
	protected List<LinkEnvironment> environments;
	
	protected Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
	
	
	public MultiCommunicationBasePattern(MessageLink messageLink, ChoreographyPackage choreographyPackage, List<LinkEnvironment> environments) {
		this.choreographyPackage = choreographyPackage;
		this.messageLink = messageLink;
		this.environments = environments;
	}
}
