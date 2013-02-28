package org.bpel4chor.mergechoreography.matcher.communication.async;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.matcher.communication.AsyncMatcher;
import org.bpel4chor.mergechoreography.pattern.MergePattern;
import org.bpel4chor.mergechoreography.pattern.communication.async.AsyncPattern15;
import org.bpel4chor.mergechoreography.pattern.conditions.Condition;
import org.bpel4chor.mergechoreography.util.MLEnvironment;
import org.bpel4chor.mergechoreography.util.MLEnvironmentAnalyzer;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Receive;

/**
 * Matcher Class for Matching BPEL Process Behavior (Async)
 * 
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class AsyncMatcher15 implements AsyncMatcher {
	
	protected Logger log = Logger.getLogger(this.getClass().getPackage().getName());
	
	/** The {@link MLEnvironment} of the given {@link MessageLink} */
	private MLEnvironment env;
	
	/** The {@link AsyncPattern15} of this {@link AsyncMatcher} */
	private MergePattern pattern = null;
	
	/** The results of the evaluation of conditions */
	private List<Boolean> results = new ArrayList<>();
	
	
	/**
	 * Method for detecting matching merge Pattern
	 * 
	 * @param link {@link MessageLink} to be analyzed
	 * @param pkg The {@link ChoreographyPackage} holding all data
	 * @return {@link CommunicationPattern} to be applied
	 */
	@Override
	public MergePattern match(MessageLink link, ChoreographyPackage pkg) {
		
		// clear previous results
		this.results.clear();
		
		// Test Call to our new LinkEnvironmentAnalyzer
		MLEnvironmentAnalyzer analyzer = new MLEnvironmentAnalyzer(link, pkg);
		this.env = analyzer.getEnvironment();
		
		// Set MessageLink to visited
		pkg.addVisitedLink(link);
		
		// Pass the Information to the Merge Pattern
		this.pattern = new AsyncPattern15(this.env, pkg);
		
		// Set the conditions
		Condition cond1 = new Condition(this.env.getPreR().size() == 0);
		Condition cond2 = new Condition(this.env.getR() instanceof Receive);
		Condition cond3 = new Condition((this.env.getR() instanceof Receive) && (((Receive) this.env.getR()).getCreateInstance() == true));
		
		this.results.add(cond1.evaluate());
		this.log.info("AsyncMatcher 1.5 PreR().size() == 0 : " + cond1.evaluate());
		this.results.add(cond2.evaluate());
		this.log.info("AsyncMatcher 1.5 R() instanceof Receive : " + cond2.evaluate());
		this.results.add(cond3.evaluate());
		this.log.info("AsyncMatcher 1.5 R() instanceof Receive && createInstance == true: " + cond3.evaluate());
		
		return this.pattern;
		
	}
	
	@Override
	public List<Boolean> evaluateConditions() {
		return this.results;
	}
	
	@Override
	public MergePattern getPattern() {
		return this.pattern;
	}
}
