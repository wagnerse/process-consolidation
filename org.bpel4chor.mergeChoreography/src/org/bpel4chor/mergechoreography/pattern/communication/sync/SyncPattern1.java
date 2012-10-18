package org.bpel4chor.mergechoreography.pattern.communication.sync;

import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.pattern.CommunicationBasePattern;
import org.bpel4chor.mergechoreography.pattern.communication.CommunicationPattern;
import org.bpel4chor.mergechoreography.util.LinkEnvironment;
import org.bpel4chor.model.topology.impl.MessageLink;

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
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void merge() {
		// TODO Auto-generated method stub
		
	}
	
}
