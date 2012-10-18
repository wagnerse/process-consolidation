package org.bpel4chor.mergechoreography.util;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Target;
import org.eclipse.emf.ecore.EObject;

/**
 * 
 * Analyzer Class for determining the surrounding activities of a
 * {@link MessageLink}
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class LinkEnvironmentAnalyzer implements Serializable {
	
	private static final long serialVersionUID = -8831266707756914781L;
	
	/** The concerned MessageLink */
	protected MessageLink link;
	/** The ChoreographyPackage holding all data */
	protected ChoreographyPackage choreographyPackage;
	
	protected Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
	
	
	public LinkEnvironmentAnalyzer(MessageLink link, ChoreographyPackage choreographyPackage) {
		super();
		this.link = link;
		this.choreographyPackage = choreographyPackage;
	}
	
	/**
	 * Returns the {@link LinkEnvironment} of the given {@link MessageLink}
	 * 
	 * @return {@link LinkEnvironment}
	 */
	public LinkEnvironment getEnvironment(String sendProc) {
		
		LinkEnvironment environment = new LinkEnvironment();
		
		// Get Sender and Receiver and corresponding Activities
		Process sender = BPEL4ChorModelHelper.resolveProcessByName(sendProc, this.choreographyPackage);
		Invoke invoke = (Invoke) BPEL4ChorModelHelper.resolveActivity(this.link.getSendActivity(), sender);
		
		Process receiver = BPEL4ChorModelHelper.resolveProcessByName(this.link.getReceiver(), this.choreographyPackage);
		Receive receive = (Receive) BPEL4ChorModelHelper.resolveActivity(this.link.getReceiveActivity(), receiver);
		
		// TODO: extend MyBPELUtil ActivityIterator so that it also concerns EHs
		// and FHs !!
		if (invoke == null) {
			// MyBPELUtils.resolveActivity didn't find the searched invoke
			// activity
			
		}
		
		if (receive == null) {
			// MyBPELUtils.resolveActivity didn't find the searched invoke
			// activity
		}
		
		// Get surrounding container of invoke and receive
		EObject invokeContainer = invoke.eContainer();
		EObject receiveContainer = receive.eContainer();
		
		Invoke inv = invoke;
		Receive rec = receive;
		
		environment.setInvoke(inv);
		environment.setReceive(rec);
		environment.setSender(sender);
		environment.setReceiver(receiver);
		
		// Analyze environment before invoke
		// First check if there is an activity before invoke in surrounding
		// container
		if (!BPEL4ChorModelHelper.isFirstActivityInContainment(inv)) {
			if (invokeContainer instanceof Sequence) {
				// If we have a sequence a container, just check the eList in
				// the container
				Sequence sequenceContainer = (Sequence) invokeContainer;
				environment.getSendBeforeEnvironment().add(sequenceContainer.getActivities().get(sequenceContainer.getActivities().indexOf(inv) - 1));
			} else if (invokeContainer instanceof Flow) {
				// If we have a flow a container, we must check if some of
				// the contained activities in the container has some link
				// as source which is target in the invoke activity
				Flow flowContainer = (Flow) invokeContainer;
				for (Activity activity : flowContainer.getActivities()) {
					if (activity.getSources() != null) {
						for (Source source : activity.getSources().getChildren()) {
							if (inv.getTargets() != null) {
								for (Target target : inv.getTargets().getChildren()) {
									if (source.getLink().getName().equals(target.getLink().getName())) {
										// We have found a candidate, but it
										// could
										// be
										// more than one (!!)
										environment.getSendBeforeEnvironment().add(activity);
									}
								}
							}
						}
					}
				}
			}
		}
		
		// Analyze environment after invoke
		// First check if there is an activity after invoke in surrounding
		// container
		if (!BPEL4ChorModelHelper.isLastActivityInContainment(inv)) {
			if (invokeContainer instanceof Sequence) {
				// If we have a sequence a container, just check the eList in
				// the container
				Sequence sequenceContainer = (Sequence) invokeContainer;
				
				environment.getSendAfterEnvironment().add(sequenceContainer.getActivities().get(sequenceContainer.getActivities().indexOf(inv) + 1));
			} else if (invokeContainer instanceof Flow) {
				// If we have a flow a container, we must check if some of
				// the contained activities in the container has some link
				// as target which are sources in the invoke activity
				Flow flowContainer = (Flow) invokeContainer;
				for (Activity activity : flowContainer.getActivities()) {
					if (activity.getTargets() != null) {
						for (Target target : activity.getTargets().getChildren()) {
							if (inv.getSources() != null) {
								for (Source source : inv.getSources().getChildren()) {
									if (source.getLink().getName().equals(target.getLink().getName())) {
										// We have found a candidate, but it
										// could
										// be
										// more than one (!!)
										environment.getSendAfterEnvironment().add(activity);
									}
								}
							}
						}
					}
				}
			}
		}
		
		// Analyze environment before receive
		// First check if there is an activity before receive in surrounding
		// container
		if (!BPEL4ChorModelHelper.isFirstActivityInContainment(rec)) {
			if (receiveContainer instanceof Sequence) {
				// If we have a sequence a container, just check the eList in
				// the container
				Sequence sequenceContainer = (Sequence) receiveContainer;
				environment.getRecBeforeEnvironment().add(sequenceContainer.getActivities().get(sequenceContainer.getActivities().indexOf(rec) - 1));
			} else if (receiveContainer instanceof Flow) {
				// If we have a flow a container, we must check if some of
				// the contained activities in the container has some link
				// as source which is target in the invoke activity
				Flow flowContainer = (Flow) receiveContainer;
				for (Activity activity : flowContainer.getActivities()) {
					if (activity.getSources() != null) {
						for (Source source : activity.getSources().getChildren()) {
							if (rec.getTargets() != null) {
								for (Target target : rec.getTargets().getChildren()) {
									if (source.getLink().getName().equals(target.getLink().getName())) {
										// We have found a candidate, but it
										// could
										// be
										// more than one (!!)
										environment.getRecBeforeEnvironment().add(activity);
									}
								}
							}
						}
					}
				}
			}
		}
		
		// Analyze environment after receive
		// First check if there is an activity after receive in surrounding
		// container
		if (!BPEL4ChorModelHelper.isLastActivityInContainment(rec)) {
			if (receiveContainer instanceof Sequence) {
				// If we have a sequence a container, just check the eList in
				// the container
				Sequence sequenceContainer = (Sequence) receiveContainer;
				
				environment.getRecAfterEnvironment().add(sequenceContainer.getActivities().get(sequenceContainer.getActivities().indexOf(rec) + 1));
			} else if (receiveContainer instanceof Flow) {
				// If we have a flow a container, we must check if some of
				// the contained activities in the container has some link
				// as target which are sources in the invoke activity
				Flow flowContainer = (Flow) receiveContainer;
				for (Activity activity : flowContainer.getActivities()) {
					if (activity.getTargets() != null) {
						for (Target target : activity.getTargets().getChildren()) {
							if (rec.getSources() != null) {
								for (Source source : rec.getSources().getChildren()) {
									if (source.getLink().getName().equals(target.getLink().getName())) {
										// We have found a candidate, but it
										// could
										// be
										// more than one (!!)
										environment.getRecAfterEnvironment().add(activity);
									}
								}
							}
						}
					}
				}
			}
		}
		return environment;
	}
	
}
