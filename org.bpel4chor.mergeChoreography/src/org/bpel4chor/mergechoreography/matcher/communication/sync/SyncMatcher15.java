package org.bpel4chor.mergechoreography.matcher.communication.sync;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.matcher.communication.AsyncMatcher;
import org.bpel4chor.mergechoreography.matcher.communication.SyncMatcher;
import org.bpel4chor.mergechoreography.pattern.MergePattern;
import org.bpel4chor.mergechoreography.pattern.communication.async.AsyncPattern11;
import org.bpel4chor.mergechoreography.pattern.communication.sync.SyncPattern15;
import org.bpel4chor.mergechoreography.pattern.conditions.Condition;
import org.bpel4chor.mergechoreography.util.ChoreoMergeUtil;
import org.bpel4chor.mergechoreography.util.MLEnvironment;
import org.bpel4chor.mergechoreography.util.MLEnvironmentAnalyzer;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Receive;

/**
 * Matcher Class for Matching BPEL Process Behavior (Sync)
 * 
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class SyncMatcher15 implements SyncMatcher {
	
	protected Logger log = Logger.getLogger(this.getClass().getPackage().getName());
	
	/** The {@link MLEnvironment} of the given {@link MessageLink} mlSend */
	private MLEnvironment envSend;
	
	/** The {@link MLEnvironment} of the given {@link MessageLink} mReply */
	private MLEnvironment envReply;
	
	/** The {@link AsyncPattern11} of this {@link AsyncMatcher} */
	private MergePattern pattern = null;
	
	/** The results of the evaluation of conditions */
	private List<Boolean> results = new ArrayList<>();
	
	
	@Override
	public List<Boolean> evaluateConditions() {
		return this.results;
	}
	
	@Override
	public MergePattern match(MessageLink mlSend, MessageLink mlReply, ChoreographyPackage pkg) {
		
		// clear previous results
		this.results.clear();
		
		// LinkEnvironmentAnalyzer
		MLEnvironmentAnalyzer analyzerSend = new MLEnvironmentAnalyzer(mlSend, pkg);
		this.envSend = analyzerSend.getEnvironment();
		MLEnvironmentAnalyzer analyzerReply = new MLEnvironmentAnalyzer(mlReply, pkg);
		this.envReply = analyzerReply.getEnvironment();
		
		// Set MessageLink to visited
		pkg.addVisitedLink(mlSend);
		pkg.addVisitedLink(mlReply);
		
		// Pass the Information to the Merge Pattern
		this.pattern = new SyncPattern15(this.envSend, this.envReply, pkg);
		
		// Set the conditions
		Condition cond1 = new Condition(this.envSend.getR() instanceof Receive);
		// Check if <invoke> is in FCTE-Handler
		Condition cond2 = new Condition(ChoreoMergeUtil.isActivityInFCTEHandler(this.envSend.getS()));
		Condition cond3 = new Condition(this.envSend.getPreR().size() == 0);
		Condition cond4 = new Condition(this.envReply.getSuccS().size() == 0);
		Condition cond5 = new Condition((this.envSend.getR() instanceof Receive) && (((Receive) this.envSend.getR()).getCreateInstance() == true));
		// TODO: Check if all activities between <receive> and <reply> lie on a
		// direct path
		
		// this.results.add(cond1.evaluate());
		// this.log.info("SyncMatcher 1.5 R() instanceof Receive : " +
		// cond1.evaluate());
		// this.results.add(cond2.evaluate());
		// this.log.info("SyncMatcher 1.5 R() <invoke> is contained in FCTE-Handler : "
		// + cond2.evaluate());
		// this.results.add(cond3.evaluate());
		// this.log.info("SyncMatcher 1.5 R() envSend.PreR().size() == 0 : " +
		// cond3.evaluate());
		// this.results.add(cond4.evaluate());
		// this.log.info("SyncMatcher 1.5 R() envReply.getSuccS().size() == 0 : "
		// + cond4.evaluate());
		// this.results.add(cond5.evaluate());
		// this.log.info("SyncMatcher 1.5 R() instanceof Receive && createInstance == true : "
		// + cond5.evaluate());
		
		return this.pattern;
	}
	
	@Override
	public MergePattern getPattern() {
		return this.pattern;
	}
	
}
