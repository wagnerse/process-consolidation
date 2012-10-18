package org.bpel4chor.mergechoreography.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.exceptions.SourceNotFoundInActivityException;
import org.bpel4chor.mergechoreography.exceptions.TargetNotFoundInActivityException;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.bpel4chor.model.topology.impl.Participant;
import org.bpel4chor.model.topology.impl.ParticipantType;
import org.bpel4chor.model.topology.impl.Topology;
import org.bpel4chor.utils.BPEL4ChorUtil;
import org.bpel4chor.utils.ExtendedActivityIterator;
import org.bpel4chor.utils.MyBPELUtils;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Target;
import org.eclipse.emf.ecore.EObject;

/**
 * Helper Class for Navigating the BPEL4Chor Choreography
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class BPEL4ChorModelHelper {
	
	private static Logger log;
	
	static {
		BPEL4ChorModelHelper.log = Logger.getLogger(BPEL4ChorModelHelper.class.getPackage().getName());
	}
	
	
	/**
	 * Returns found ParticipantType by given name
	 * 
	 * @param name The name of the {@link ParticipantType}
	 * @param topology The {@link Topology} of the Choreography
	 * @return
	 */
	public static ParticipantType getTypeByName(String name, Topology topology) {
		for (ParticipantType type : topology.getParticipantTypes()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Checks whether given Processes communicates with each other
	 * 
	 * @param mergedProcess First {@link Process}
	 * @param process Second {@link Process}
	 * @param choreographyPackage The {@link ChoreographyPackage}
	 * @return {@link MessageLink} if true, null of false
	 */
	public static MessageLink isCommunicating(Process mergedProcess, Process process, ChoreographyPackage choreographyPackage) {
		Topology topology = choreographyPackage.getTopology();
		
		for (MessageLink link : topology.getMessageLinks()) {
			
			String senderProcess = BPEL4ChorModelHelper.getTypeByName(BPEL4ChorUtil.resolveParticipant(topology, link.getSender()).getType(), topology).getParticipantBehaviorDescription().getLocalPart();
			String receiverProcess = BPEL4ChorModelHelper.getTypeByName(BPEL4ChorUtil.resolveParticipant(topology, link.getReceiver()).getType(), topology).getParticipantBehaviorDescription().getLocalPart();
			
			if ((senderProcess.equals(mergedProcess.getName()) && receiverProcess.equals(process.getName())) || (receiverProcess.equals(mergedProcess.getName()) && senderProcess.equals(process.getName()))) {
				return link;
			}
			
		}
		return null;
	}
	
	/**
	 * Checks whether given Processes communicates synchronously with each other
	 * 
	 * @param mergedProcess First {@link Process}
	 * @param process Second {@link Process}
	 * @param choreographyPackage The {@link ChoreographyPackage}
	 * @return {@link MessageLink} if true, null of false
	 */
	public static MessageLink isSyncCommunicating(Process mergedProcess, Process process, ChoreographyPackage choreographyPackage) {
		Topology topology = choreographyPackage.getTopology();
		
		for (MessageLink link : topology.getMessageLinks()) {
			
			String senderProcess = BPEL4ChorModelHelper.getTypeByName(BPEL4ChorUtil.resolveParticipant(topology, link.getSender()).getType(), topology).getParticipantBehaviorDescription().getLocalPart();
			String receiverProcess = BPEL4ChorModelHelper.getTypeByName(BPEL4ChorUtil.resolveParticipant(topology, link.getReceiver()).getType(), topology).getParticipantBehaviorDescription().getLocalPart();
			
			// Check if process1 sends to process2
			if ((senderProcess.equals(mergedProcess.getName()) && receiverProcess.equals(process.getName()))) {
				// Now check if sendActivity has an outputVariable
				// (Request-Response)
				
				// First Lookup Send Activity
				Invoke invoke = (Invoke) MyBPELUtils.resolveActivity(link.getSendActivity(), mergedProcess);
				
				if ((invoke != null) && (invoke.getOutputVariable() != null)) {
					return link;
				}
			}
			
		}
		return null;
	}
	
	/**
	 * Checks whether given Processes communicates asynchronously with each
	 * other
	 * 
	 * @param mergedProcess First {@link Process}
	 * @param process Second {@link Process}
	 * @param choreographyPackage The {@link ChoreographyPackage}
	 * @return {@link MessageLink} if true, null of false
	 */
	public static MessageLink isAsyncCommunicating(Process mergedProcess, Process process, ChoreographyPackage choreographyPackage) {
		Topology topology = choreographyPackage.getTopology();
		
		for (MessageLink link : topology.getMessageLinks()) {
			
			String senderProcess = BPEL4ChorModelHelper.getTypeByName(BPEL4ChorUtil.resolveParticipant(topology, link.getSender()).getType(), topology).getParticipantBehaviorDescription().getLocalPart();
			String receiverProcess = BPEL4ChorModelHelper.getTypeByName(BPEL4ChorUtil.resolveParticipant(topology, link.getReceiver()).getType(), topology).getParticipantBehaviorDescription().getLocalPart();
			
			// Check if process1 sends to process2
			if ((senderProcess.equals(mergedProcess.getName()) && receiverProcess.equals(process.getName()))) {
				// Now check if sendActivity has an outputVariable
				// (Request-Response)
				
				// First Lookup Send Activity
				Invoke invoke = (Invoke) MyBPELUtils.resolveActivity(link.getSendActivity(), mergedProcess);
				
				if ((invoke != null) && (invoke.getOutputVariable() == null)) {
					return link;
				}
			}
			
		}
		return null;
	}
	
	/**
	 * Resolves the ParticipantType from the topology with the given Name
	 * 
	 * @param topology {@link Topology} of the choreo
	 * @param partName {@link Participant} to find the type for
	 * @return {@link ParticipantType} for the given name, or null
	 */
	public static ParticipantType resolveParticipantType(Topology topology, Participant partName) {
		for (ParticipantType type : topology.getParticipantTypes()) {
			if (type.getName().equals(partName.getType())) {
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Checks whether given Processes communicates asynchronously with each
	 * other
	 * 
	 * @param mergedProcess First {@link Process}
	 * @param process Second {@link Process}
	 * @param choreographyPackage The {@link ChoreographyPackage}
	 * @return {@link MessageLink} if true, null of false
	 */
	public static MessageLink isAsyncWithReplyCommunicating(MessageLink link, ChoreographyPackage choreographyPackage) {
		
		// Check for Invoke from receiving process to sending process
		Process processRec = BPEL4ChorModelHelper.resolveProcessByName(link.getReceiver(), choreographyPackage);
		Process processSend = BPEL4ChorModelHelper.resolveProcessByName(link.getSender(), choreographyPackage);
		
		MessageLink link2 = BPEL4ChorModelHelper.isAsyncCommunicating(processRec, processSend, choreographyPackage);
		
		return link2;
	}
	
	/**
	 * Checks whether given receiving process uses the link with multiple invoke
	 * partners other
	 * 
	 * @param mergedProcess First {@link Process}
	 * @param process Second {@link Process}
	 * @param choreographyPackage The {@link ChoreographyPackage}
	 * @return {@link MessageLink} if true, null of false
	 */
	public static MessageLink isAsyncWithMultiCommunicating(MessageLink link, ChoreographyPackage choreographyPackage) {
		
		// Check for multiple Senders
		if (link.getSenders() != null) {
			return link;
		}
		
		return null;
	}
	
	/**
	 * Resolves the given Process from the given choreographyPackage
	 * 
	 * @param name The name of the {@link Participant}
	 * @param choreographyPackage The {@link ChoreographyPackage}
	 * @return The found {@link Process}, or null
	 */
	public static Process resolveProcessByName(String name, ChoreographyPackage choreographyPackage) {
		String processName = BPEL4ChorModelHelper.getTypeByName(BPEL4ChorUtil.resolveParticipant(choreographyPackage.getTopology(), name).getType(), choreographyPackage.getTopology()).getParticipantBehaviorDescription().getLocalPart();
		for (Process process : choreographyPackage.getPbds()) {
			if (process.getName().equals(processName)) {
				return process;
			}
		}
		return null;
	}
	
	/**
	 * Returns the corresponding {@link MessageLink} from the {@link Topology}
	 * 
	 * @param invoke The {@link Invoke}
	 * @param choreographyPackage The {@link ChoreographyPackage} holding all
	 *            data
	 * @return The searched {@link MessageLink}
	 */
	public static MessageLink findByInvokeActivity(Invoke invoke, ChoreographyPackage choreographyPackage) {
		for (MessageLink link : choreographyPackage.getTopology().getMessageLinks()) {
			if (link.getSendActivity().equals(invoke.getName())) {
				return link;
			}
		}
		return null;
	}
	
	/**
	 * Checks if given activity is a non-communicating
	 * 
	 * @param activity {@link Activity} to check
	 * @return true/false whether non-communicating or not
	 */
	public static boolean isNonCommunicatingActivity(Activity activity) {
		if (!(activity instanceof Invoke) && !(activity instanceof Receive) && !(activity instanceof Reply) && !(activity instanceof Reply) && !(activity instanceof Pick)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks if given activity is the last in surrounding context
	 * 
	 * @param activity The {@link Activity} to check
	 * @return true if last, false otherwise
	 */
	public static boolean isLastActivityInContainment(Activity activity) {
		EObject container = activity.eContainer();
		
		if (container instanceof Sequence) {
			// We have a sequence
			Sequence sequenceContainer = (Sequence) container;
			if (sequenceContainer.getActivities().indexOf(activity) == (sequenceContainer.getActivities().size() - 1)) {
				return true;
			}
		}
		
		if (container instanceof Flow) {
			// We have a flow
			Flow flow = (Flow) container;
			// Now check if activity is the last in flow
			// For this we must check, whether there are some source links from
			// the given activity which are targets in another activity in the
			// same
			// flow
			for (Activity flowAct : flow.getActivities()) {
				if (flowAct.getTargets() != null) {
					for (Target target : flowAct.getTargets().getChildren()) {
						if (activity.getSources() != null) {
							for (Source source : activity.getSources().getChildren()) {
								if (target.getLink().getName().equals(source.getLink().getName())) {
									// There are some succeeding activities..
									return false;
								}
							}
						}
					}
				}
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if given activity is the first in surrounding context
	 * 
	 * @param activity The {@link Activity} to check
	 * @return true if first, false otherwise
	 */
	public static boolean isFirstActivityInContainment(Activity activity) {
		EObject container = activity.eContainer();
		
		if (container instanceof Sequence) {
			// If we have a sequence a container, just check the eList in the
			// container
			Sequence sequenceContainer = (Sequence) container;
			if (sequenceContainer.getActivities().indexOf(activity) == 0) {
				return true;
			} else {
				return false;
			}
		}
		
		if (container instanceof Flow) {
			// We have a flow
			Flow flow = (Flow) container;
			// Now check if activity is the first in flow
			// For this we must check, whether there are some target links from
			// the given activity which are sources in another activity in the
			// same
			// flow
			for (Activity flowAct : flow.getActivities()) {
				if (flowAct.getSources() != null) {
					for (Source source : flowAct.getSources().getChildren()) {
						if (activity.getTargets() != null) {
							for (Target target : activity.getTargets().getChildren()) {
								if (target.getLink().getName().equals(source.getLink().getName())) {
									// There are some preceding activities..
									return false;
								}
							}
						}
					}
				}
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns the source from a1 which have a2 as target
	 * 
	 * @param act1 The {@link Activity} with searched sources
	 * @param act2 The {@link Activity} with corresponding targets
	 * @return Found {@link Source}, null otherwise
	 */
	public static Source getMatchingSource(Activity act1, Activity act2) {
		if (act1.getSources() != null) {
			for (Source source : act1.getSources().getChildren()) {
				if (act2.getTargets() != null) {
					for (Target target : act2.getTargets().getChildren()) {
						if (target.getLink().getName().equals(source.getLink().getName())) {
							return source;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the target from a2 which have a1 as source
	 * 
	 * @param act1 The {@link Activity} with searched sources
	 * @param act2 The {@link Activity} with corresponding targets
	 * @return The found {@link Target}, null otherwise
	 */
	public static Target getMatchingTarget(Activity act1, Activity act2) {
		if (act1.getSources() != null) {
			for (Source source : act1.getSources().getChildren()) {
				if (act2.getTargets() != null) {
					for (Target target : act2.getTargets().getChildren()) {
						if (target.getLink().getName().equals(source.getLink().getName())) {
							return target;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Copies all sources from act1 to act2 if any existed
	 * 
	 * @param act1 {@link Activity} with sources to be copied
	 * @param act2 {@link Activity} to copy sources to
	 */
	public static void copySources(Activity act1, Activity act2) {
		if (act1.getSources() != null) {
			if (act2.getSources() == null) {
				act2.setSources(BPELFactory.eINSTANCE.createSources());
			}
			for (Source source : act1.getSources().getChildren()) {
				Source newSource = BPELFactory.eINSTANCE.createSource();
				newSource.setLink(source.getLink());
				BPEL4ChorModelHelper.log.log(Level.INFO, "Copying source with linkName: " + newSource.getLink().getName() + " to activity : " + act2.getName());
				act2.getSources().getChildren().add(newSource);
			}
		}
	}
	
	/**
	 * Copies all targets from act1 to act2 if any existed
	 * 
	 * @param act1 {@link Activity} with targets to be copied
	 * @param act2 {@link Activity} to copy targets to
	 */
	public static void copyTargets(Activity act1, Activity act2) {
		if (act1.getTargets() != null) {
			if (act2.getTargets() == null) {
				act2.setTargets(BPELFactory.eINSTANCE.createTargets());
			}
			for (Target target : act1.getTargets().getChildren()) {
				Target newTarget = BPELFactory.eINSTANCE.createTarget();
				newTarget.setLink(target.getLink());
				BPEL4ChorModelHelper.log.log(Level.INFO, "Copying target with linkName: " + newTarget.getLink().getName() + " to activity : " + act2.getName());
				act2.getTargets().getChildren().add(newTarget);
			}
		}
	}
	
	/**
	 * Removes the link with the given name from the sources of the given
	 * {@link Activity}
	 * 
	 * @param activity The {@link Activity} to erase the source from
	 * @param linkName The name of the link of the source
	 * @throws SourceNotFoundInActivityException
	 */
	public static void removeSourceLink(Activity activity, String linkName) throws SourceNotFoundInActivityException {
		Source found = null;
		for (Source source : activity.getSources().getChildren()) {
			if (source.getLink().getName().equals(linkName)) {
				found = source;
			}
		}
		if (found != null) {
			activity.getSources().getChildren().remove(found);
		} else {
			throw new SourceNotFoundInActivityException("There is no link with name " + linkName + " as Source in " + activity.getName());
		}
	}
	
	/**
	 * Removes the link with the given name from the targets of the given
	 * {@link Activity}
	 * 
	 * @param activity The {@link Activity} to erase the target from
	 * @param linkName The name of the link of the source
	 * @throws TargetNotFoundInActivityException
	 */
	public static void removeTargetLink(Activity activity, String linkName) throws TargetNotFoundInActivityException {
		Target found = null;
		for (Target target : activity.getTargets().getChildren()) {
			if (target.getLink().getName().equals(linkName)) {
				found = target;
			}
		}
		if (found != null) {
			activity.getTargets().getChildren().remove(found);
		} else {
			throw new TargetNotFoundInActivityException("There is no link with name " + linkName + " as Target in " + activity.getName());
		}
	}
	
	/**
	 * Looks up the source with the given name in the given activity
	 * 
	 * @param activity The {@link Activity} containing the source
	 * @param linkName The name of the link of the source
	 * @return The found {@link Source} or null
	 */
	public static Source findSourceInActivity(Activity activity, String linkName) {
		for (Source source : activity.getSources().getChildren()) {
			if (source.getLink().getName().equals(linkName)) {
				return source;
			}
		}
		return null;
	}
	
	/**
	 * Looks up the target with the given name in the given activity
	 * 
	 * @param activity The {@link Activity} containing the target
	 * @param linkName The name of the link of the target
	 * @return The found {@link Target} or null
	 */
	public static Target findTargetInActivity(Activity activity, String linkName) {
		for (Target target : activity.getTargets().getChildren()) {
			if (target.getLink().getName().equals(linkName)) {
				return target;
			}
		}
		return null;
	}
	
	/**
	 * Removes the source from the given activity with the given linkName
	 * 
	 * @param activity {@link Activity} to remove source from
	 * @param linkName The name of the link of the source
	 */
	public static void removeSourceFromActivity(Activity activity, String linkName) {
		Source found = null;
		for (Source source : activity.getSources().getChildren()) {
			if (source.getLink().getName().equals(linkName)) {
				found = source;
				break;
			}
		}
		if (found != null) {
			activity.getSources().getChildren().remove(found);
		}
	}
	
	/**
	 * Removes the target from the given activity with the given linkName
	 * 
	 * @param activity {@link Activity} to remove target from
	 * @param linkName The name of the link of the target
	 */
	public static void removeTargetFromActivity(Activity activity, String linkName) {
		Target found = null;
		for (Target target : activity.getTargets().getChildren()) {
			if (target.getLink().getName().equals(linkName)) {
				found = target;
				break;
			}
		}
		if (found != null) {
			activity.getTargets().getChildren().remove(found);
		}
	}
	
	/**
	 * Adds the given target to the given activity if it's not already contained
	 * 
	 * @param activity The {@link Activity} to add the target to
	 * @param target The {@link Target} to be added
	 */
	public static void addTargetToActivity(Activity activity, Target target) {
		if (BPEL4ChorModelHelper.findTargetInActivity(activity, target.getLink().getName()) == null) {
			activity.getTargets().getChildren().add(target);
		}
	}
	
	/**
	 * Adds the given source to the given activity if it's not already contained
	 * 
	 * @param activity The {@link Activity} to add the source to
	 * @param target The {@link Source} to be added
	 */
	public static void addSourceToActivity(Activity activity, Source source) {
		if (BPEL4ChorModelHelper.findSourceInActivity(activity, source.getLink().getName()) == null) {
			activity.getSources().getChildren().add(source);
		}
	}
	
	/**
	 * Returns the condition expression of the target with the given name from
	 * the given activity
	 * 
	 * @param activity The {@link Activity} containing the target
	 * @param linkName The name of the link of the {@link Target}
	 * @return Condition expression String, null if no condition found
	 */
	public static String getJoinConditionForTarget(Activity activity, String linkName) {
		// TODO: Do we really need to parse the expression !?!?!
		if (activity.getTargets().getJoinCondition() != null) {
			if (activity.getTargets().getJoinCondition().getBody().toString().indexOf(linkName) != -1) {
				return linkName;
			}
		}
		return null;
	}
	
	/**
	 * Removes the given activity from the given container activity
	 * 
	 * @param act {@link Activity} to remove
	 */
	public static void removeActivityFromContainer(Activity act) {
		EObject container = act.eContainer();
		if (container instanceof Flow) {
			((Flow) container).getActivities().remove(act);
		} else if (container instanceof Sequence) {
			((Sequence) container).getActivities().remove(act);
		}
	}
	
	/**
	 * Replaces the actOld {@link Activity} by the actNew {@link Activity}
	 * 
	 * @param actOld {@link Activity} to be replaced
	 * @param actNew The new {@link Activity}
	 */
	public static void replaceActivity(Activity actOld, Activity actNew) {
		EObject container = actOld.eContainer();
		
		// Check type of container
		if (container instanceof Flow) {
			((Flow) container).getActivities().set(((Flow) container).getActivities().indexOf(actOld), actNew);
		} else if (container instanceof Sequence) {
			((Sequence) container).getActivities().set(((Sequence) container).getActivities().indexOf(actOld), actNew);
		}
	}
	
	/**
	 * Find the nearest common eContainer from given activities
	 * 
	 * @param act1 {@link Activity}
	 * @param act2 {@link Activity}
	 * @return The found common {@link EObject}, null else
	 */
	public static EObject findNearestCommonContainer(Activity act1, Activity act2) {
		EObject act1Cont = act1.eContainer();
		EObject act2Cont = act2.eContainer();
		while ((act1Cont != act2Cont) && (!(act1Cont instanceof Process) || (act2Cont instanceof Process))) {
			act1Cont = act1Cont.eContainer();
			act2Cont = act2Cont.eContainer();
			// log.log(Level.INFO, "act1Cont = " + act1Cont);
			// log.log(Level.INFO, "act2Cont = " + act2Cont);
		}
		if (act1Cont == act2Cont) {
			return act1Cont;
		} else {
			return null;
		}
	}
	
	/**
	 * Look up the activity with the name given in the process
	 * 
	 * @param actName Activity name
	 * @param process The process
	 * @return The found activity or null
	 */
	public static Activity resolveActivity(String actName, Process process) {
		
		if ((actName == null) || (process == null)) {
			throw new NullPointerException();
		}
		
		// ActivityFinder finder = new ActivityFinder(process);
		//
		// try {
		// Activity found = finder.find(actName);
		// return found;
		// } catch (ActivityNotFoundException e) {
		// return null;
		// }
		
		ExtendedActivityIterator actIterator = new ExtendedActivityIterator(process);
		while (actIterator.hasNext()) {
			Activity act = actIterator.next();
			if (act.getName().equals(actName)) {
				return act;
			}
		}
		return null;
	}
}
