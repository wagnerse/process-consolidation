package org.bpel4chor.mergechoreography.matcher.communication.sync;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.matcher.communication.AsyncMatcher;
import org.bpel4chor.mergechoreography.matcher.communication.SyncMatcher;
import org.bpel4chor.mergechoreography.pattern.MergePattern;
import org.bpel4chor.mergechoreography.pattern.communication.async.AsyncPattern11;
import org.bpel4chor.mergechoreography.pattern.communication.sync.SyncPattern14;
import org.bpel4chor.mergechoreography.pattern.communication.sync.SyncPattern24;
import org.bpel4chor.mergechoreography.pattern.conditions.Condition;
import org.bpel4chor.mergechoreography.util.ChoreoMergeUtil;
import org.bpel4chor.mergechoreography.util.MLEnvironment;
import org.bpel4chor.mergechoreography.util.MLEnvironmentAnalyzer;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Reply;

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
public class SyncMatcher24 implements SyncMatcher {
	
	protected Logger log = Logger.getLogger(this.getClass().getPackage().getName());
	
	/** The {@link MLEnvironment} of the given {@link MessageLink} mlSend */
	private MLEnvironment envSend;
	
	/** The {@link MLEnvironment} of the given {@link MessageLink} mReply */
	private MLEnvironment envReply;
	
	/** The {@link AsyncPattern11} of this {@link AsyncMatcher} */
	private MergePattern pattern = null;
	
	/** The results of the evaluation of conditions */
	private List<Boolean> results = new ArrayList<>();
	
	/** The ChoreographyPackage containing all information */
	private ChoreographyPackage pkg;
	
	
	@Override
	public List<Boolean> evaluateConditions() {
		return this.results;
	}
	
	@Override
	public MergePattern match(MessageLink mlSend, MessageLink mlReply, ChoreographyPackage pkg) {
		
		this.pkg = pkg;
		
		// LinkEnvironmentAnalyzer
		MLEnvironmentAnalyzer analyzerSend = new MLEnvironmentAnalyzer(mlSend, pkg);
		this.envSend = analyzerSend.getEnvironment();
		MLEnvironmentAnalyzer analyzerReply = new MLEnvironmentAnalyzer(mlReply, pkg);
		this.envReply = analyzerReply.getEnvironment();
		
		// Set MessageLink to visited
		pkg.addVisitedLink(mlSend);
		pkg.addVisitedLink(mlReply);
		
		// Pass the Information to the Merge Pattern
		this.pattern = new SyncPattern24(this.envSend, this.envReply, pkg);
		
		SyncPattern24 pattern24 = (SyncPattern24) this.pattern;
		
		pattern24.setWsuIDrec(mlSend.getReceiveActivity());
		
		// Set the conditions
		Condition cond1 = new Condition(this.envSend.getR() instanceof Pick);
		
		if (cond1.evaluate()) {
			// Check if there exist other replying Message Links
			this.checkForOtherReplyML();
		}
		
		Condition cond2 = new Condition(pattern24.getReplyMLinks().size() > 0);
		
		this.results.add(cond1.evaluate());
		this.log.info("SyncMatcher 2.4 R() instanceof Pick : " + cond1.evaluate());
		this.results.add(cond2.evaluate());
		this.log.info("SyncMatcher 2.4 There exist other MessageLinks replying to the same <invoke> : " + cond2.evaluate());
		if (cond2.evaluate()) {
			this.log.info("These MessageLinks are : ");
			for (MessageLink mLink : pattern24.getReplyMLinks()) {
				this.log.info(" => " + mLink.getName());
			}
		}
		
		return this.pattern;
	}
	
	/**
	 * Check for other {@link Reply}ing {@link MessageLink}s and add it to the
	 * {@link SyncPattern14}
	 */
	private void checkForOtherReplyML() {
		for (MessageLink mLink : this.pkg.getTopology().getMessageLinks()) {
			if (!this.pkg.isLinkVisited(mLink)) {
				this.log.info("checkForOtherReplyML .... ");
				if (mLink.getReceiveActivity().equals(ChoreoMergeUtil.resolveWSU_ID(this.envSend.getS()))) {
					this.pkg.addVisitedLink(mLink);
					MLEnvironmentAnalyzer analyzerReply = new MLEnvironmentAnalyzer(mLink, this.pkg);
					((SyncPattern24) this.pattern).addReplyMLinkEnv(analyzerReply.getEnvironment());
					((SyncPattern24) this.pattern).addReplyMessageLink(mLink);
				}
			}
		}
		
	}
	
	@Override
	public MergePattern getPattern() {
		return this.pattern;
	}
	
}
