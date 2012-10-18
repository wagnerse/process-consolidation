package org.bpel4chor.mergechoreography.matcher.communication;

import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.model.topology.impl.MessageLink;

/**
 * Matcher Interface for Matching BPEL Process Behavior
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public interface LinkMatcher {
	
	public CommunicationPattern match(MessageLink link, ChoreographyPackage choreographyPackage);
	
}
