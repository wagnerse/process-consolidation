package org.bpel4chor.mergechoreography.util;

import java.util.ArrayList;
import java.util.List;

import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.model.topology.impl.MessageLink;

/**
 * 
 * Analyzer Class for determining the surrounding activities of a
 * {@link MessageLink} with multiple senders
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class MultiLinkEnvironmentAnalyzer extends LinkEnvironmentAnalyzer {
	
	private static final long serialVersionUID = 1L;
	
	
	public MultiLinkEnvironmentAnalyzer(MessageLink link, ChoreographyPackage choreographyPackage) {
		super(link, choreographyPackage);
	}
	
	/**
	 * Returns the List of {@link LinkEnvironment}s of the given
	 * {@link MessageLink} with multiple senders involved
	 * 
	 * @return {@link LinkEnvironment}
	 */
	public List<LinkEnvironment> getEnvironments() {
		List<LinkEnvironment> environments = new ArrayList<>();
		for (String sender : this.link.getSenders()) {
			environments.add(this.getEnvironment(sender));
		}
		return environments;
	}
}
