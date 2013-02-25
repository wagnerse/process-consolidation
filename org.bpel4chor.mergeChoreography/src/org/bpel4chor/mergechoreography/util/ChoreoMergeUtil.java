package org.bpel4chor.mergechoreography.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

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
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.CatchAll;
import org.eclipse.bpel.model.Compensate;
import org.eclipse.bpel.model.CompensationHandler;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.Correlation;
import org.eclipse.bpel.model.Else;
import org.eclipse.bpel.model.ElseIf;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.EventHandler;
import org.eclipse.bpel.model.Expression;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.From;
import org.eclipse.bpel.model.FromPart;
import org.eclipse.bpel.model.If;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.OnMessage;
import org.eclipse.bpel.model.PartnerActivity;
import org.eclipse.bpel.model.Pick;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.RepeatUntil;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Sources;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Targets;
import org.eclipse.bpel.model.TerminationHandler;
import org.eclipse.bpel.model.Throw;
import org.eclipse.bpel.model.To;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Wait;
import org.eclipse.bpel.model.While;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDTypeDefinition;

import de.uni_stuttgart.iaas.bpel.model.utilities.FragmentDuplicator;
import de.uni_stuttgart.iaas.bpel.model.utilities.MyBPELUtils;

/**
 * Helper Class for Navigating the BPEL4Chor Choreography
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class ChoreoMergeUtil {
	
	private static Logger log;
	
	private static ChoreographyPackage pkg;
	
	static {
		ChoreoMergeUtil.log = Logger.getLogger(ChoreoMergeUtil.class.getPackage().getName());
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
	 * Resolves the given Process from the given choreographyPackage
	 * 
	 * @param name The name of the {@link Participant}
	 * @return The found {@link Process}, or null
	 */
	public static Process resolveProcessByName(String name) {
		String processName = ChoreoMergeUtil.getTypeByName(BPEL4ChorUtil.resolveParticipant(ChoreoMergeUtil.pkg.getTopology(), name).getType(), ChoreoMergeUtil.pkg.getTopology()).getParticipantBehaviorDescription().getLocalPart();
		for (Process process : ChoreoMergeUtil.pkg.getPbds()) {
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
	 * @return The searched {@link MessageLink}
	 */
	public static MessageLink findByInvokeActivity(Invoke invoke) {
		for (MessageLink link : ChoreoMergeUtil.pkg.getTopology().getMessageLinks()) {
			if (link.getSendActivity().equals(invoke.getName())) {
				return link;
			}
		}
		return null;
	}
	
	/**
	 * Return {@link Activity} in given {@link EObject} with {@link Link} as
	 * {@link Target}
	 * 
	 * @param cont {@link EObject}
	 * @param link {@link Link}
	 * @return
	 */
	public static Activity getActivityByTarget(EObject cont, Link link) {
		if (cont instanceof Flow) {
			for (Activity act : ((Flow) cont).getActivities()) {
				if (act.getTargets() != null) {
					for (Target target : act.getTargets().getChildren()) {
						if (target.getLink().getName().equals(link.getName())) {
							return act;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Find the {@link Source}s of the given {@link Activity} following
	 * {@link Target} links
	 * 
	 * @param activity The {@link Activity} to check
	 * @return {@link List} of {@link Activity}s if there are some, null else
	 */
	public static Set<Activity> findSourcesOfActivity(Activity activity) {
		Set<Activity> foundActs = null;
		if (activity.getTargets() != null) {
			for (Target target : activity.getTargets().getChildren()) {
				Activity source = target.getLink().getSources().get(0).getActivity();
				if (source != null) {
					if (foundActs == null) {
						foundActs = new HashSet<>();
					}
					foundActs.add(source);
				}
			}
			if (activity.getTargets().getChildren().size() != foundActs.size()) {
				throw new RuntimeException("Not all sources of activity " + activity.getName() + " were found !!");
			}
		}
		return foundActs;
	}
	
	/**
	 * Find the {@link Target}s of the given {@link Activity} following
	 * {@link Source} links
	 * 
	 * @param activity The {@link Activity} to check
	 * @return {@link List} of {@link Activity}s if there are some, null else
	 */
	public static Set<Activity> findTargetsOfActivity(Activity activity) {
		Set<Activity> foundActs = null;
		if (activity.getSources() != null) {
			for (Source source : activity.getSources().getChildren()) {
				Activity target = source.getLink().getTargets().get(0).getActivity();
				if (target != null) {
					if (foundActs == null) {
						foundActs = new HashSet<>();
					}
					foundActs.add(target);
				}
			}
			if (activity.getSources().getChildren().size() != foundActs.size()) {
				throw new RuntimeException("Not all targets of activity " + activity.getName() + " were found !!");
			}
		}
		return foundActs;
	}
	
	/**
	 * Find {@link Activity} which is targeted by the given {@link Source}
	 * 
	 * @param cont The {@link EObject} containing the {@link Activity}
	 * @param source The {@link Source} to follow
	 * @return Found {@link Activity}, null else
	 */
	public static Activity findActivityBySource(EObject cont, Source source) {
		while (!(cont instanceof Process)) {
			if (cont instanceof Sequence) {
				Sequence seq = (Sequence) cont;
				for (Activity act : seq.getActivities()) {
					if (act.getTargets() != null) {
						for (Target target : act.getTargets().getChildren()) {
							if (target.getLink().getName().equals(source.getLink().getName())) {
								return act;
							}
						}
					}
				}
			} else if (cont instanceof Flow) {
				Flow flow = (Flow) cont;
				for (Activity act : flow.getActivities()) {
					if (act.getTargets() != null) {
						for (Target target : act.getTargets().getChildren()) {
							if (target.getLink().getName().equals(source.getLink().getName())) {
								return act;
							}
						}
					}
				}
			}
			// We couldn't find the activity so walk up
			cont = cont.eContainer();
		}
		
		return null;
	}
	
	/**
	 * Find {@link Activity} which has source by the given {@link Target}
	 * 
	 * @param cont The {@link EObject} containing the {@link Activity}
	 * @param source The {@link Target} to follow
	 * @return Found {@link Activity}, null else
	 */
	public static Activity findActivityByTarget(EObject cont, Target target) {
		while (!(cont instanceof Process)) {
			
			if ((cont instanceof Sequence) || (cont instanceof Flow)) {
				for (Activity act : (cont instanceof Sequence ? ((Sequence) cont).getActivities() : ((Flow) cont).getActivities())) {
					if (act.getSources() != null) {
						for (Source source : act.getSources().getChildren()) {
							if (source.getLink().getName().equals(target.getLink().getName())) {
								return act;
							}
						}
					}
				}
			}
			// We couldn't find the activity so walk up
			cont = cont.eContainer();
		}
		return null;
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
	 * Check whether given process has a non-propagating <catchAll>-FH
	 * 
	 * @param process {@link Process} to examine
	 * @return true or false
	 */
	public static boolean hasNPCatchAllFH(Process process) {
		if ((process.getFaultHandlers() != null) && (process.getFaultHandlers().getCatchAll() != null)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Create a new non-propagating <catchAll>-FH
	 * 
	 * @return
	 */
	public static CatchAll createNPCatchAll() {
		CatchAll catchAll = BPELFactory.eINSTANCE.createCatchAll();
		Compensate compensate = BPELFactory.eINSTANCE.createCompensate();
		catchAll.setActivity(compensate);
		return catchAll;
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
				ChoreoMergeUtil.log.log(Level.INFO, "Copying source with linkName: " + newSource.getLink().getName() + " to activity : " + act2.getName());
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
				ChoreoMergeUtil.log.log(Level.INFO, "Copying target with linkName: " + newTarget.getLink().getName() + " to activity : " + act2.getName());
				act2.getTargets().getChildren().add(newTarget);
			}
			if (act1.getTargets().getJoinCondition() != null) {
				act2.getTargets().setJoinCondition(FragmentDuplicator.copyCondition(act1.getTargets().getJoinCondition()));
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
				break;
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
				break;
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
		if (activity.getSources() != null) {
			for (Source source : activity.getSources().getChildren()) {
				if (source.getLink().getName().equals(linkName)) {
					return source;
				}
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
		if ((activity == null) || (linkName == null)) {
			throw new NullPointerException("Null parameter error! activity == null:" + (activity == null) + " linkName == null:" + (linkName == null));
		}
		if (activity.getTargets() != null) {
			for (Target target : activity.getTargets().getChildren()) {
				if (target.getLink().getName().equals(linkName)) {
					return target;
				}
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
	 * Removes the source from the given activity
	 * 
	 * @param act The {@link Activity} containing the source
	 * @param source The {@link Source} to be removed
	 */
	public static void removeSourceFromActivity(Activity act, Source source) {
		act.getSources().getChildren().remove(source);
		if (act.getSources().getChildren().size() == 0) {
			act.setSources(null);
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
	 * Removes the target from the given activity
	 * 
	 * @param act
	 * @param target
	 */
	public static void removeTargetFromActivity(Activity act, Target target) {
		act.getTargets().getChildren().remove(target);
		if (act.getTargets().getChildren().size() == 0) {
			act.setTargets(null);
		}
	}
	
	/**
	 * Adds the given target to the given activity if it's not already contained
	 * 
	 * @param activity The {@link Activity} to add the target to
	 * @param target The {@link Target} to be added
	 */
	public static void addTargetToActivity(Activity activity, Target target) {
		if (activity.getTargets() == null) {
			activity.setTargets(BPELFactory.eINSTANCE.createTargets());
		} else {
			if (ChoreoMergeUtil.findTargetInActivity(activity, target.getLink().getName()) == null) {
				activity.getTargets().getChildren().add(target);
			}
		}
	}
	
	/**
	 * Adds the given source to the given activity if it's not already contained
	 * 
	 * @param activity The {@link Activity} to add the source to
	 * @param target The {@link Source} to be added
	 */
	public static void addSourceToActivity(Activity activity, Source source) {
		if (activity.getSources() == null) {
			activity.setSources(BPELFactory.eINSTANCE.createSources());
		} else {
			if (ChoreoMergeUtil.findSourceInActivity(activity, source.getLink().getName()) == null) {
				activity.getSources().getChildren().add(source);
			}
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
		if ((activity.getTargets() != null) && (activity.getTargets().getJoinCondition() != null)) {
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
			Flow flow = (Flow) container;
			flow.getActivities().remove(act);
			if (flow.getActivities().size() == 0) {
				// Check if there are no <sources> and <targets> remained
				if (((flow.getSources() == null) || (flow.getSources().getChildren().size() == 0)) && ((flow.getTargets() == null) || (flow.getTargets().getChildren().size() == 0))) {
					ChoreoMergeUtil.removeActivityFromContainer(flow);
				}
			}
		} else if (container instanceof Sequence) {
			Sequence seq = (Sequence) container;
			seq.getActivities().remove(act);
			if (seq.getActivities().size() == 0) {
				// Check if there are no <sources> and <targets> remained
				if (((seq.getSources() == null) || (seq.getSources().getChildren().size() == 0)) && ((seq.getTargets() == null) || (seq.getTargets().getChildren().size() == 0))) {
					ChoreoMergeUtil.removeActivityFromContainer(seq);
				}
			}
		} else if (container instanceof Scope) {
			Scope scp = (Scope) container;
			scp.setActivity(null);
			if (((scp.getSources() == null) || (scp.getSources().getChildren().size() == 0)) && ((scp.getTargets() == null) || (scp.getTargets().getChildren().size() == 0))) {
				ChoreoMergeUtil.removeActivityFromContainer(scp);
			}
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
		} else if (container instanceof Scope) {
			((Scope) container).setActivity(actNew);
		} else if (container instanceof CompensationHandler) {
			((CompensationHandler) container).setActivity(actNew);
		} else if (container instanceof If) {
			((If) container).setActivity(actNew);
		} else if (container instanceof Else) {
			((Else) container).setActivity(actNew);
		} else if (container instanceof ElseIf) {
			((ElseIf) container).setActivity(actNew);
		} else if (container instanceof While) {
			((While) container).setActivity(actNew);
		} else if (container instanceof RepeatUntil) {
			((RepeatUntil) container).setActivity(actNew);
		} else if (container instanceof OnAlarm) {
			((OnAlarm) container).setActivity(actNew);
		} else if (container instanceof OnMessage) {
			((OnMessage) container).setActivity(actNew);
		} else if (container instanceof ForEach) {
			((ForEach) container).setActivity(actNew);
		} else if (container instanceof Catch) {
			((Catch) container).setActivity(actNew);
		} else if (container instanceof CatchAll) {
			((CatchAll) container).setActivity(actNew);
		} else if (container instanceof TerminationHandler) {
			((TerminationHandler) container).setActivity(actNew);
		} else if (container instanceof OnEvent) {
			((OnEvent) container).setActivity(actNew);
		}
	}
	
	/**
	 * Check for preceding {@link Activity}s
	 * 
	 * @param act The {@link Activity} to check
	 * @return Preceding {@link Activity}s, null else
	 */
	public static List<Activity> getPreceedingActivities(Activity act) {
		
		Set<Activity> before = ChoreoMergeUtil.findSourcesOfActivity(act);
		
		// Get the container containing the activity
		EObject actContainer = act.eContainer();
		
		if (actContainer instanceof Process) {
			return (before != null ? new ArrayList<>(before) : null);
		}
		
		Activity seqPreAct = ChoreoMergeUtil.getPreActivityInSequence(act);
		if (seqPreAct != null) {
			// New !!
			ChoreoMergeUtil.log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			ChoreoMergeUtil.log.info("Found following preceding Activity in <sequence> for act : " + act);
			ChoreoMergeUtil.log.info(" => " + seqPreAct);
			ChoreoMergeUtil.log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			// End new !!
			if (before == null) {
				before = new HashSet<>();
			}
			before.add(seqPreAct);
		}
		
		// // if we have a sequence
		// if (actContainer instanceof Sequence) {
		// Sequence seq = (Sequence) actContainer;
		// // check if activity has predecessor
		// int posAct = seq.getActivities().indexOf(act);
		// if (posAct != 0) {
		// // we have some
		// if (before == null) {
		// before = new ArrayList<>();
		// }
		// before.add(seq.getActivities().get(posAct - 1));
		// return before;
		// } else {
		// // else check if the sequence have any
		// return (before != null ? before :
		// ChoreoMergeUtil.getPreceedingActivities(seq));
		//
		// }
		// } else {
		// while (!(actContainer instanceof Activity) && !(actContainer
		// instanceof Process)) {
		// // we walk up till we get an activity container
		// actContainer = actContainer.eContainer();
		// }
		// return (before != null ? before :
		// ChoreoMergeUtil.getPreceedingActivities((Activity) actContainer));
		// }
		return (before != null ? new ArrayList<>(before) : null);
	}
	
	/**
	 * Check for succeeding {@link Activity}s
	 * 
	 * @param act The {@link Activity} to check
	 * @return Preceding {@link Activity}s, null else
	 */
	public static List<Activity> getSucceedingActivities(Activity act) {
		
		Set<Activity> after = ChoreoMergeUtil.findTargetsOfActivity(act);
		
		// Get the container containing the activity
		EObject actContainer = act.eContainer();
		
		if (actContainer instanceof Process) {
			return (after != null ? new ArrayList<>(after) : null);
		}
		
		Activity seqSuccAct = ChoreoMergeUtil.getSuccActivityInSequence(act);
		if (seqSuccAct != null) {
			// New !!
			ChoreoMergeUtil.log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			ChoreoMergeUtil.log.info("Found following succeeding Activity in <sequence> for act : " + act);
			ChoreoMergeUtil.log.info(" => " + seqSuccAct);
			ChoreoMergeUtil.log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			// End new !!
			if (after == null) {
				after = new HashSet<>();
			}
			after.add(seqSuccAct);
		}
		
		// // if we have a sequence
		// if (actContainer instanceof Sequence) {
		// Sequence seq = (Sequence) actContainer;
		// // check if activity has predecessor
		// int posAct = seq.getActivities().indexOf(act);
		// if (posAct < (seq.getActivities().size() - 1)) {
		// // we have some
		// if (after == null) {
		// after = new ArrayList<>();
		// }
		// after.add(seq.getActivities().get(posAct + 1));
		// return after;
		// } else {
		// // else check if the sequence have any
		// return (after != null ? after :
		// ChoreoMergeUtil.getSucceedingActivities(seq));
		//
		// }
		// } else {
		// while (!(actContainer instanceof Activity) && !(actContainer
		// instanceof Process)) {
		// // we walk up till we get an activity container
		// actContainer = actContainer.eContainer();
		// }
		// return (after != null ? after :
		// ChoreoMergeUtil.getSucceedingActivities((Activity) actContainer));
		// }
		return (after != null ? new ArrayList<>(after) : null);
	}
	
	/**
	 * Look up the {@link Activity} with the given wsu:id in the merged
	 * {@link Process}
	 * 
	 * @param wsuID The wsu:id of the searched {@link Activity}
	 * 
	 * @return Found {@link Activity} else null
	 */
	public static BPELExtensibleElement resolveActivity(String wsuID) {
		if ((wsuID == null) || (ChoreoMergeUtil.pkg == null)) {
			throw new NullPointerException("wsuID == null : " + (wsuID == null) + " pkg == null : " + (ChoreoMergeUtil.pkg == null));
		}
		return ChoreoMergeUtil.pkg.getOld2New().get(wsuID);
	}
	
	/**
	 * Look up the wsu:id for the given {@link BPELExtensibleElement} in the
	 * merged {@link Process}
	 * 
	 * @param elem The {@link BPELExtensibleElement} to search for the
	 *            corresponding WSU:ID
	 * @return Found wsu:id or null
	 */
	public static String resolveWSU_ID(BPELExtensibleElement elem) {
		for (Entry<String, BPELExtensibleElement> entry : ChoreoMergeUtil.pkg.getOld2New().entrySet()) {
			if (elem == entry.getValue()) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	/**
	 * Resolve {@link Variable} with given name in container(s) containing
	 * {@link Activity} act
	 * 
	 * @param varName The name of the {@link Variable} to be resolved
	 * @param act The {@link Activity} using the {@link Variable}
	 * @return found {@link Variable} or null else
	 */
	public static Variable resolveVariable(String varName, Activity act) {
		if ((varName == null) || (act == null)) {
			throw new NullPointerException("argument is null. varName == null:" + (varName == null) + " act == null:" + (act == null));
		}
		List<Variable> vars = null;
		EObject container = act.eContainer();
		while (container != null) {
			while (!(container instanceof Scope) && !(container instanceof Process)) {
				container = container.eContainer();
			}
			if (container instanceof Scope) {
				if (((Scope) container).getVariables() != null) {
					vars = ((Scope) container).getVariables().getChildren();
				}
			} else {
				if (((Process) container).getVariables() != null) {
					vars = ((Process) container).getVariables().getChildren();
				}
			}
			if (vars != null) {
				for (Variable variable : vars) {
					if (variable.getName().equals(varName)) {
						return variable;
					}
				}
			}
			// Climb higher
			container = container.eContainer();
		}
		return null;
	}
	
	/**
	 * Get the highest {@link Scope} of given {@link Activity} in the merged
	 * {@link Process}. It's the {@link Scope} inside the merged {@link Flow}.
	 * 
	 * @param act {@link Activity} to get the highest {@link Scope} of
	 * @return Highest {@link Scope} or null
	 */
	public static Scope getHighestScopeOfActivity(Activity act) {
		if (act == null) {
			throw new NullPointerException("argument is null. act == null:" + (act == null));
		}
		EObject containerSub = act;
		EObject container = act.eContainer();
		while (!(container instanceof Process)) {
			while (!(container instanceof Flow) && !(container instanceof Process)) {
				containerSub = container;
				container = container.eContainer();
			}
			if (((Flow) container).getName().equals("MergedFlow")) {
				// We found the containing <scope>
				ChoreoMergeUtil.log.info("Found <scope> " + ((Scope) containerSub).getName() + " for activity " + act.getName());
				return (Scope) containerSub;
			}
			container = container.eContainer();
		}
		return null;
	}
	
	/**
	 * Check if given {@link Activity} is contained in a {@link Sequence}. If so
	 * get succeeding {@link Activity}, if present.
	 * 
	 * @param act {@link Activity} to check
	 * @return Succeeding {@link Activity} or null
	 */
	public static Activity getSuccActivityInSequence(Activity act) {
		if (act == null) {
			throw new NullPointerException("argument is null. act == null:" + (act == null));
		}
		Activity succAct = null;
		// EObject containerSub = act;
		// EObject container = act.eContainer();
		// while (!(container instanceof Process) && !(container instanceof
		// Sequence)) {
		// containerSub = container;
		// container = container.eContainer();
		// }
		Activity[] result = ChoreoMergeUtil.getSequenceAndInsideActivity(act);
		if (result != null) {
			Sequence seq = (Sequence) result[0];
			Activity inSeq = result[1];
			int posAct = seq.getActivities().indexOf(inSeq);
			if (posAct < (seq.getActivities().size() - 1)) {
				// we have some
				succAct = seq.getActivities().get(posAct + 1);
			}
			
		}
		
		return succAct;
	}
	
	/**
	 * Check if given {@link Activity} is contained in a {@link Sequence}. If so
	 * get preceding {@link Activity}, if present.
	 * 
	 * @param act {@link Activity} to check
	 * @return Preceding {@link Activity} or null
	 */
	public static Activity getPreActivityInSequence(Activity act) {
		if (act == null) {
			throw new NullPointerException("argument is null. act == null:" + (act == null));
		}
		Activity preAct = null;
		// EObject containerSub = act;
		// EObject container = act.eContainer();
		// while (!(container instanceof Process) && !(container instanceof
		// Sequence)) {
		// containerSub = container;
		// container = container.eContainer();
		// }
		Activity[] result = ChoreoMergeUtil.getSequenceAndInsideActivity(act);
		if (result != null) {
			Sequence seq = (Sequence) result[0];
			Activity inSeq = result[1];
			int posAct = seq.getActivities().indexOf(inSeq);
			if (posAct > 0) {
				// we have some
				preAct = seq.getActivities().get(posAct - 1);
			}
			
		}
		return preAct;
	}
	
	/**
	 * Check if given {@link Activity} is contained is a {@link Sequence}
	 * 
	 * @param act {@link Activity} to check
	 * @return {@link Activity}-Array with first element as the {@link Sequence}
	 *         and the second element the {@link Activity}, or null
	 */
	public static Activity[] getSequenceAndInsideActivity(Activity act) {
		if (act == null) {
			throw new NullPointerException("argument is null. act == null:" + (act == null));
		}
		Activity[] result = null;
		EObject containerSub = act;
		EObject container = act.eContainer();
		while (!(container instanceof Process) && !(container instanceof Sequence)) {
			containerSub = container;
			container = container.eContainer();
		}
		if (container instanceof Sequence) {
			result = new Activity[] {(Sequence) container, (Activity) containerSub};
		}
		return result;
	}
	
	/**
	 * Return owning {@link Process} of given {@link Activity}
	 * 
	 * @param act {@link Activity}
	 * @return Found {@link Process} or null
	 */
	public static Process getProcessOfActivity(Activity act) {
		if (act == null) {
			throw new NullPointerException("argument is null. act == null:" + (act == null));
		}
		EObject container = act.eContainer();
		while (!(container instanceof Process)) {
			// Climb up
			container = container.eContainer();
		}
		return (Process) container;
	}
	
	/**
	 * Return owning {@link Process} of given {@link BPELExtensibleElement}
	 * 
	 * @param elem {@link BPELExtensibleElement}
	 * @return Found {@link Process} or null
	 */
	public static Process getProcessOfElement(BPELExtensibleElement elem) {
		if (elem == null) {
			throw new NullPointerException("argument is null. elem == null:" + (elem == null));
		}
		EObject container = elem.eContainer();
		while (!(container instanceof Process)) {
			// Climb up
			container = container.eContainer();
		}
		return (Process) container;
	}
	
	/**
	 * Find the corresponding replying messagelink
	 * 
	 * @param ml The invoking {@link MessageLink}
	 * 
	 * @return found {@link MessageLink} or null
	 */
	public static MessageLink findReplyingMessageLink(MessageLink ml) {
		
		for (MessageLink mLink : ChoreoMergeUtil.pkg.getTopology().getMessageLinks()) {
			if (!ChoreoMergeUtil.pkg.isLinkVisited(mLink)) {
				if (mLink.getReceiveActivity().equals(ml.getSendActivity())) {
					return mLink;
				}
			}
		}
		return null;
	}
	
	/**
	 * Resolve {@link Variable} in Merged Process
	 * 
	 * @param var {@link Variable} to resolve
	 * @return searched {@link Variable} or null
	 */
	public static Variable resolveVariableInMergedProcess(Variable var) {
		return ChoreoMergeUtil.pkg.getPbd2MergedVars().get(var);
	}
	
	/**
	 * Resolve {@link Link} in Merged Process
	 * 
	 * @param link {@link Link} to resolve
	 * @return searched {@link Link} or null
	 */
	public static Link resolveLinkInMergedProcess(Link link) {
		return ChoreoMergeUtil.pkg.getPbd2MergedLinks().get(link);
	}
	
	/**
	 * Remove the given link from the given flow
	 * 
	 * @param flow
	 * @param link
	 */
	public static void removeLinkFromFlow(Flow flow, Link link) {
		// flow.getLinks().getChildren().remove(link);
		// Let's try it dumpfbackig
		Link found = null;
		for (Link lk : flow.getLinks().getChildren()) {
			if (lk.getName().equals(link.getName())) {
				found = lk;
				break;
			}
		}
		if (found != null) {
			flow.getLinks().getChildren().remove(found);
			if (flow.getLinks().getChildren().size() == 0) {
				flow.setLinks(null);
			}
		}
	}
	
	/**
	 * Rename the link from given activity to newName
	 * 
	 * @param act
	 * @param oldName
	 * @param newName
	 */
	public static void renameLinkInActivity(Activity act, String oldName, String newName) {
		for (Source source : act.getSources().getChildren()) {
			if (source.getLink().getName().equals(oldName)) {
				source.getLink().setName(newName);
				return;
			}
		}
		for (Target target : act.getTargets().getChildren()) {
			if (target.getLink().getName().equals(oldName)) {
				target.getLink().setName(newName);
				return;
			}
		}
	}
	
	/**
	 * Check whether given invoke activity is async communicating
	 * 
	 * @param activity The activity to check
	 * @return
	 */
	public static boolean isInvokeAsync(PartnerActivity activity) {
		if (activity instanceof Invoke) {
			if (((Invoke) activity).getOutputVariable() == null) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add the newLink to given {@link Flow}
	 * 
	 * @param flow The {@link Flow}
	 * @param newLink The new {@link Link}
	 */
	public static void addLinkToFlow(Flow flow, Link newLink) {
		if (flow.getLinks() == null) {
			flow.setLinks(BPELFactory.eINSTANCE.createLinks());
		}
		
		// Check if there's already a link with the same name
		for (Link link : flow.getLinks().getChildren()) {
			if (link.getName().equals(newLink.getName())) {
				throw new RuntimeException("There's already a link " + newLink.getName() + " in flow " + flow.getName());
			}
		}
		flow.getLinks().getChildren().add(newLink);
	}
	
	/**
	 * Create a new {@link Target} with given {@link Link} in given
	 * {@link Activity}
	 * 
	 * @param link The {@link Link}
	 * @param act The {@link Activity} for the new {@link Target}
	 * @return new {@link Target}
	 */
	public static Target createTarget4LinkInActivity(Link link, Activity act) {
		if (act.getTargets() == null) {
			act.setTargets(BPELFactory.eINSTANCE.createTargets());
		}
		Target newTarget = BPELFactory.eINSTANCE.createTarget();
		newTarget.setLink(link);
		act.getTargets().getChildren().add(newTarget);
		return newTarget;
	}
	
	/**
	 * Create a new {@link Source} with given {@link Link} in given
	 * {@link Activity}
	 * 
	 * @param link The {@link Link}
	 * @param act The {@link Activity} for the new {@link Source}
	 */
	public static void createSource4LinkInActivity(Link link, Activity act) {
		if (act.getSources() == null) {
			act.setSources(BPELFactory.eINSTANCE.createSources());
		}
		Source newSource = BPELFactory.eINSTANCE.createSource();
		newSource.setLink(link);
		act.getSources().getChildren().add(newSource);
	}
	
	// TODO: Implement the next section correct !!
	/**
	 * Find the flow which contains the link with the given name starting from
	 * given activity upstream
	 * 
	 * @param act
	 * @param linkName
	 * @return
	 */
	public static Flow findLinkOwnerFlow(Activity act, String linkName) {
		EObject cont = act.eContainer();
		while (!(cont instanceof Process)) {
			if (cont instanceof Flow) {
				Flow flowCont = (Flow) cont;
				if (MyBPELUtils.resolveLink(flowCont, linkName) != null) {
					return flowCont;
				}
			}
			cont = cont.eContainer();
			
		}
		return null;
	}
	
	/**
	 * Find the {@link Flow} which contains given {@link Activity}
	 * 
	 * @param act {@link Activity} to find owning {@link Flow} of
	 * @return {@link Flow} or null
	 */
	public static Flow findActivityOwnerFlow(Activity act) {
		EObject cont = act.eContainer();
		while (!(cont instanceof Process)) {
			if (cont instanceof Flow) {
				return (Flow) cont;
			}
			cont = cont.eContainer();
		}
		return null;
	}
	
	/**
	 * Conjunct the linknames of the {@link Target}s of the given
	 * {@link Activity}
	 * 
	 * @param act {@link Activity} with {@link Target}s
	 * @return conjuncted linknames string
	 */
	public static String targetDisjunctor(Activity act) {
		String expression = "";
		for (int i = 0; i < act.getTargets().getChildren().size(); i++) {
			expression += "$" + act.getTargets().getChildren().get(i).getLink().getName();
			// Check if there are other links coming
			if (i < (act.getTargets().getChildren().size() - 1)) {
				expression += " or ";
			}
		}
		return expression;
	}
	
	/**
	 * Conjunct the linknames of the given {@link Target}s
	 * 
	 * @param targets The {@link Target}s to be conjuncted
	 * @return conjuncted linknames string
	 */
	public static String targetDisjunctor(List<Target> targets) {
		String expression = "";
		for (int i = 0; i < targets.size(); i++) {
			expression += "$" + targets.get(i).getLink().getName();
			// Check if there are other links coming
			if (i < (targets.size() - 1)) {
				expression += " or ";
			}
		}
		return expression;
	}
	
	/**
	 * Generate string of given given {@link List} of {@link Activity}s
	 * 
	 * @param acts {@link List} of {@link Activity}s
	 * @return String
	 */
	public static String getTextOfList(List<Activity> acts) {
		String text = "";
		for (Activity act : acts) {
			text += act.getName() + " ";
		}
		return text;
	}
	
	/**
	 * Create new {@link Assign} from given {@link Invoke} with copy from
	 * {@link Variable} invoke.inputVariable to {@link Variable} vR. All
	 * {@link Sources} and {@link Targets} are also copied.
	 * 
	 * @param s {@link Invoke} with {@link From} part {@link Variable}
	 * @param vR {@link Variable} to {@link Copy} {@link To}
	 * @return new {@link Assign}
	 */
	public static Assign createAssignFromInvoke(Invoke s, Variable vR) {
		Assign newAssign = ChoreoMergeUtil.createAssignFromSendAct(s, vR);
		return newAssign;
	}
	
	/**
	 * Create new {@link Assign} from given {@link PartnerActivity} with copy
	 * from {@link Variable} act.variable to {@link Variable} vR. All
	 * {@link Sources} and {@link Targets} are also copied.
	 * 
	 * @param act {@link PartnerActivity} with {@link From} part
	 *            {@link Variable}
	 * @param vR {@link Variable} to {@link Copy} {@link To}
	 * @return new {@link Assign}
	 */
	public static Assign createAssignFromSendAct(PartnerActivity act, Variable vR) {
		Assign newAssign = BPELFactory.eINSTANCE.createAssign();
		FragmentDuplicator.copyStandardAttributes(act, newAssign);
		FragmentDuplicator.copyStandardElements(act, newAssign);
		newAssign.setName(act.getName());
		Copy newCopy = BPELFactory.eINSTANCE.createCopy();
		From newFrom = BPELFactory.eINSTANCE.createFrom();
		if (act instanceof Invoke) {
			newFrom.setVariable(((Invoke) act).getInputVariable());
		} else {
			newFrom.setVariable(((Reply) act).getVariable());
		}
		To newTo = BPELFactory.eINSTANCE.createTo();
		newTo.setVariable(vR);
		newAssign.getCopy().add(newCopy);
		newCopy.setFrom(newFrom);
		newCopy.setTo(newTo);
		return newAssign;
	}
	
	/**
	 * Create new {@link Copy} {@link From} from {@link To} to
	 * 
	 * @param from {@link Variable}
	 * @param to {@link Variable}
	 * @return New {@link Copy}
	 */
	public static Copy createCopy(Variable from, Variable to) {
		if ((from == null) || (to == null)) {
			throw new NullPointerException("Arguments not set, from == null" + (from == null) + " to == null " + (to == null));
		}
		Copy newCopy = BPELFactory.eINSTANCE.createCopy();
		From newFrom = BPELFactory.eINSTANCE.createFrom();
		newFrom.setVariable(from);
		To newTo = BPELFactory.eINSTANCE.createTo();
		newTo.setVariable(to);
		newCopy.setFrom(newFrom);
		newCopy.setTo(newTo);
		return newCopy;
	}
	
	/**
	 * Create new {@link Scope} from given {@link Invoke} with new
	 * {@link Assign} with copy from {@link Variable} invoke.inputVariable to
	 * {@link Variable} vR. All {@link Sources} and {@link Targets} are also
	 * copied to the {@link Scope}
	 * 
	 * @param s {@link Invoke} with {@link From} part {@link Variable}
	 * @param vR {@link Variable} to {@link Copy} {@link To}
	 * @return new {@link Scope}
	 */
	public static Scope createScopeFromInvoke(Invoke s, Variable vR) {
		Scope newScope = BPELFactory.eINSTANCE.createScope();
		FragmentDuplicator.copyStandardAttributes(s, newScope);
		FragmentDuplicator.copyStandardElements(s, newScope);
		
		// Move FaultHandlers
		if ((s.getFaultHandler() != null) && ((s.getFaultHandler().getCatch().size() > 0) || (s.getFaultHandler().getCatchAll() != null))) {
			FaultHandler handler = BPELFactory.eINSTANCE.createFaultHandler();
			if (s.getFaultHandler().getCatch().size() > 0) {
				handler.getCatch().addAll(s.getFaultHandler().getCatch());
			}
			
			newScope.setFaultHandlers(handler);
			
			if (s.getFaultHandler().getCatchAll() != null) {
				CatchAll pbdCatAll = s.getFaultHandler().getCatchAll();
				newScope.getFaultHandlers().setCatchAll(pbdCatAll);
			}
		}
		
		// Move CompensationHandler
		if (s.getCompensationHandler() != null) {
			newScope.setCompensationHandler(s.getCompensationHandler());
		}
		
		Assign newAssign = ChoreoMergeUtil.createAssignFromInvoke(s, vR);
		newAssign.setSources(null);
		newAssign.setTargets(null);
		
		newScope.setActivity(newAssign);
		
		return newScope;
	}
	
	/**
	 * Create new Guard {@link Variable} in Merged Process and initialize it
	 * with false()
	 * 
	 * @param varName Name of new {@link Variable}
	 * @return New Guard {@link Variable}
	 */
	public static Variable createGuardVariableInMergedProcess(String varName) {
		Variable newVar = BPELFactory.eINSTANCE.createVariable();
		newVar.setName(varName);
		XSDTypeDefinition type = XSDFactory.eINSTANCE.createXSDSimpleTypeDefinition();
		type.setName("boolean");
		type.setTargetNamespace("http://www.w3.org/2001/XMLSchema");
		newVar.setType(type);
		
		Expression newFromExp = BPELFactory.eINSTANCE.createExpression();
		newFromExp.setBody("false()");
		From newFrom = BPELFactory.eINSTANCE.createFrom();
		newFrom.setExpression(newFromExp);
		newVar.setFrom(newFrom);
		
		if (ChoreoMergeUtil.pkg.getMergedProcess().getVariables() == null) {
			ChoreoMergeUtil.pkg.getMergedProcess().setVariables(BPELFactory.eINSTANCE.createVariables());
		}
		
		ChoreoMergeUtil.pkg.getMergedProcess().getVariables().getChildren().add(newVar);
		
		return newVar;
	}
	
	/**
	 * Check whether given {@link Invoke} declares some {@link FaultHandler}s or
	 * {@link CompensationHandler}
	 * 
	 * @param s {@link Invoke} to check
	 * @return true or false
	 */
	public static boolean hasFHsOrCH(Invoke s) {
		
		if ((s.getFaultHandler() != null) && ((s.getFaultHandler().getCatch().size() > 0) || (s.getFaultHandler().getCatchAll() != null))) {
			return true;
		}
		
		if (s.getCompensationHandler() != null) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Create new {@link Empty} from given {@link Activity}. All {@link Sources}
	 * and {@link Targets} are also copied.
	 * 
	 * @param act {@link Activity} to create {@link Empty} from
	 * @return new {@link Empty}
	 */
	public static Empty createEmptyFromActivity(Activity act) {
		Empty newEmpty = BPELFactory.eINSTANCE.createEmpty();
		FragmentDuplicator.copyStandardAttributes(act, newEmpty);
		newEmpty.setName(act.getName());
		FragmentDuplicator.copyStandardElements(act, newEmpty);
		return newEmpty;
	}
	
	/**
	 * Create new {@link Scope} from given {@link Activity}. All {@link Sources}
	 * and {@link Targets} are also copied. The {@link Scope} contains a
	 * {@link Flow} and a {@link CatchAll} {@link FaultHandler} with an
	 * {@link Empty}.
	 * 
	 * @param act {@link Activity} to create {@link Scope} from
	 * @return new {@link Scope}
	 */
	public static Scope createRCScopeFromActivity(Activity act) {
		Scope newScope = BPELFactory.eINSTANCE.createScope();
		FragmentDuplicator.copyStandardAttributes(act, newScope);
		FragmentDuplicator.copyStandardElements(act, newScope);
		Flow newFlow = BPELFactory.eINSTANCE.createFlow();
		newScope.setActivity(newFlow);
		newScope.setFaultHandlers(BPELFactory.eINSTANCE.createFaultHandler());
		CatchAll newCatchAll = BPELFactory.eINSTANCE.createCatchAll();
		Empty newEmpty = BPELFactory.eINSTANCE.createEmpty();
		newCatchAll.setActivity(newEmpty);
		newScope.getFaultHandlers().setCatchAll(newCatchAll);
		return newScope;
	}
	
	/**
	 * Create new {@link Scope} from given {@link Pick}. All {@link Sources} and
	 * {@link Targets} are also copied. The {@link Scope} contains a
	 * {@link Flow}. For every {@link OnMessage}, except all in om, there's a
	 * {@link Sequence} contained with a {@link Receive} replacing the
	 * {@link OnMessage} and an {@link Assign} which copies true() to the given
	 * Guard {@link Variable} and a {@link Throw} for a user-defined BPEL-Fault.
	 * This Fault is {@link Catch}ed by a {@link FaultHandler} in the
	 * surrounding {@link Scope} which contains the original {@link Activity}
	 * from the {@link OnMessage} branch. If {@link OnAlarm}s are defined, they
	 * are transformed to a similar {@link Sequence}, but instead a
	 * {@link Receive} it contains a {@link Wait} as first {@link Activity}.
	 * 
	 * @param pick {@link Pick} to be transformed
	 * @param om {@link OnMessage} branch to be ommitted
	 * @param vGuard Guard {@link Variable} to be used in {@link Assign}s
	 * @return new {@link Scope}
	 */
	public static Scope createScopeFromPickWithoutOM(Pick pick, List<OnMessage> om, Variable vGuard) {
		Scope newScope = BPELFactory.eINSTANCE.createScope();
		FragmentDuplicator.copyStandardAttributes(pick, newScope);
		FragmentDuplicator.copyStandardElements(pick, newScope);
		Flow newFlow = BPELFactory.eINSTANCE.createFlow();
		newScope.setActivity(newFlow);
		
		// create <sequences> for all <onMessage>-branches, except om
		for (OnMessage onMessage : pick.getMessages()) {
			
			// create <throw>
			String name = ChoreoMergeUtil.resolveWSU_ID(onMessage);
			
			if (name == null) {
				throw new RuntimeException("Cannot resolve " + onMessage + " in Merged Process !!");
			}
			
			QName qName = new QName(ChoreoMergeUtil.pkg.getMergedProcess().getTargetNamespace(), name + "Fault");
			Throw newThrow = ChoreoMergeUtil.createThrowForUserDefinedFault(qName);
			
			// add <catch>-Fault Handler for newThrow and onMessage.activity in
			// newScope
			if (newScope.getFaultHandlers() == null) {
				newScope.setFaultHandlers(BPELFactory.eINSTANCE.createFaultHandler());
			}
			Catch newCatch = BPELFactory.eINSTANCE.createCatch();
			newCatch.setFaultName(qName);
			newCatch.setActivity(onMessage.getActivity());
			newScope.getFaultHandlers().getCatch().add(newCatch);
			
			if (om.contains(onMessage)) {
				// Skip
				continue;
			}
			Sequence newSeq = BPELFactory.eINSTANCE.createSequence();
			
			newFlow.getActivities().add(newSeq);
			
			// create <receive> from <onMessage>
			newSeq.getActivities().add(ChoreoMergeUtil.createReceiveFromOnMessage(onMessage, pick.getCreateInstance()));
			
			// create <assign> with <copy> <from> true() <to> vGuard
			// Create the <assign>
			Assign newAssign = BPELFactory.eINSTANCE.createAssign();
			Copy newCopy1 = BPELFactory.eINSTANCE.createCopy();
			Expression newFromExp1 = BPELFactory.eINSTANCE.createExpression();
			newFromExp1.setBody("true()");
			From newFrom1 = BPELFactory.eINSTANCE.createFrom();
			newFrom1.setExpression(newFromExp1);
			To newTo1 = BPELFactory.eINSTANCE.createTo();
			newTo1.setVariable(vGuard);
			newCopy1.setFrom(newFrom1);
			newCopy1.setTo(newTo1);
			newAssign.getCopy().add(newCopy1);
			newSeq.getActivities().add(newAssign);
			
			newSeq.getActivities().add(newThrow);
			
		}
		
		int counter = 0;
		for (OnAlarm onAlarm : pick.getAlarm()) {
			
			QName qName = new QName(ChoreoMergeUtil.pkg.getMergedProcess().getTargetNamespace(), "wait" + (counter++) + "Fault");
			Throw newThrow = ChoreoMergeUtil.createThrowForUserDefinedFault(qName);
			
			// add <catch>-Fault Handler for newThrow and onMessage.activity in
			// newScope
			if (newScope.getFaultHandlers() == null) {
				newScope.setFaultHandlers(BPELFactory.eINSTANCE.createFaultHandler());
			}
			Catch newCatch = BPELFactory.eINSTANCE.createCatch();
			newCatch.setFaultName(qName);
			newCatch.setActivity(onAlarm.getActivity());
			newScope.getFaultHandlers().getCatch().add(newCatch);
			
			Sequence newSeq = BPELFactory.eINSTANCE.createSequence();
			
			newFlow.getActivities().add(newSeq);
			
			// create <receive> from <onMessage>
			newSeq.getActivities().add(ChoreoMergeUtil.createWaitFromOnAlarm(onAlarm));
			
			// create <assign> with <copy> <from> true() <to> vGuard
			// Create the <assign>
			Assign newAssign = BPELFactory.eINSTANCE.createAssign();
			Copy newCopy1 = BPELFactory.eINSTANCE.createCopy();
			Expression newFromExp1 = BPELFactory.eINSTANCE.createExpression();
			newFromExp1.setBody("true()");
			From newFrom1 = BPELFactory.eINSTANCE.createFrom();
			newFrom1.setExpression(newFromExp1);
			To newTo1 = BPELFactory.eINSTANCE.createTo();
			newTo1.setVariable(vGuard);
			newCopy1.setFrom(newFrom1);
			newCopy1.setTo(newTo1);
			newAssign.getCopy().add(newCopy1);
			newSeq.getActivities().add(newAssign);
			
			newSeq.getActivities().add(newThrow);
		}
		return newScope;
	}
	
	/**
	 * Create equivalent {@link Wait} from given {@link OnAlarm}
	 * 
	 * @param onAlarm {@link OnAlarm} to create {@link Wait} from
	 * @return new {@link Wait}
	 */
	public static Wait createWaitFromOnAlarm(OnAlarm onAlarm) {
		Wait newWait = BPELFactory.eINSTANCE.createWait();
		if (onAlarm.getFor() != null) {
			newWait.setFor(onAlarm.getFor());
		}
		
		if (onAlarm.getUntil() != null) {
			newWait.setUntil(onAlarm.getUntil());
		}
		return newWait;
	}
	
	/**
	 * Create new {@link Receive} from given {@link OnMessage} branch
	 * 
	 * @param om {@link OnMessage} branch to create new {@link Receive} from
	 * @param createInstance Attribute from surrounding {@link Pick}
	 * @return new {@link Receive}
	 */
	public static Receive createReceiveFromOnMessage(OnMessage om, Boolean createInstance) {
		Receive newReceive = BPELFactory.eINSTANCE.createReceive();
		if (createInstance != null) {
			newReceive.setCreateInstance(createInstance);
		}
		
		// name as wsu:id
		String name = ChoreoMergeUtil.resolveWSU_ID(om);
		
		if (name != null) {
			newReceive.setName(name);
		}
		
		if (om.getMessageExchange() != null) {
			newReceive.setMessageExchange(om.getMessageExchange());
		}
		
		// Variable
		newReceive.setVariable(om.getVariable());
		
		// fromParts
		if ((om.getFromParts() != null) && (om.getFromParts().getChildren().size() > 0)) {
			newReceive.setFromParts(BPELFactory.eINSTANCE.createFromParts());
			for (FromPart fromPart : om.getFromParts().getChildren()) {
				FromPart newFromPart = PBDFragmentDuplicator.copyFromPart(fromPart);
				newReceive.getFromParts().getChildren().add(newFromPart);
			}
		}
		
		// correlations
		if ((om.getCorrelations() != null) && (om.getCorrelations().getChildren().size() > 0)) {
			newReceive.setCorrelations(BPELFactory.eINSTANCE.createCorrelations());
			for (Correlation correlation : om.getCorrelations().getChildren()) {
				Correlation newCorrelation = PBDFragmentDuplicator.copyCorrelation(correlation);
				newReceive.getCorrelations().getChildren().add(newCorrelation);
			}
		}
		
		return newReceive;
	}
	
	/**
	 * Create new {@link If} from given {@link Invoke}. All {@link Sources} and
	 * {@link Targets} are also copied. The {@link If} has a {@link Condition}
	 * checking if Guard {@link Variable} var is still false() and an
	 * {@link Assign} with {@link From}true() {@link To}gVar and {@link From}vS
	 * {@link To}vR
	 * 
	 * @param inv {@link Invoke} to create {@link If} from
	 * @param gVar Guard {@link Variable}
	 * @param vR {@link Receive}ing {@link Variable}
	 * @return new {@link If}
	 */
	public static If createIfFromInvoke(Invoke inv, Variable gVar, Variable vR) {
		If newIf = BPELFactory.eINSTANCE.createIf();
		FragmentDuplicator.copyStandardAttributes(inv, newIf);
		FragmentDuplicator.copyStandardElements(inv, newIf);
		Condition ifCondition = BPELFactory.eINSTANCE.createCondition();
		ifCondition.setBody(gVar.getName() + "!=true()");
		newIf.setCondition(ifCondition);
		
		// Create the <assign>
		Assign newAssign = BPELFactory.eINSTANCE.createAssign();
		Copy newCopy1 = BPELFactory.eINSTANCE.createCopy();
		Expression newFromExp1 = BPELFactory.eINSTANCE.createExpression();
		newFromExp1.setBody("true()");
		From newFrom1 = BPELFactory.eINSTANCE.createFrom();
		newFrom1.setExpression(newFromExp1);
		To newTo1 = BPELFactory.eINSTANCE.createTo();
		newTo1.setVariable(gVar);
		newCopy1.setFrom(newFrom1);
		newCopy1.setTo(newTo1);
		Copy newCopy2 = BPELFactory.eINSTANCE.createCopy();
		From newFrom2 = BPELFactory.eINSTANCE.createFrom();
		newFrom2.setVariable(inv.getInputVariable());
		To newTo2 = BPELFactory.eINSTANCE.createTo();
		newTo2.setVariable(vR);
		newCopy2.setFrom(newFrom2);
		newCopy2.setTo(newTo2);
		newAssign.getCopy().add(newCopy1);
		newAssign.getCopy().add(newCopy2);
		newIf.setActivity(newAssign);
		
		return newIf;
	}
	
	/**
	 * Create new {@link Throw} for bpel:joinFailure
	 * 
	 * @return new {@link Throw}
	 */
	public static Throw createThrowBPELJoinFailure() {
		Throw newThrow = BPELFactory.eINSTANCE.createThrow();
		QName newQName = new QName("", "joinFailure", "");
		newThrow.setFaultName(newQName);
		return newThrow;
	}
	
	/**
	 * Create new {@link Throw} given Fault
	 * 
	 * @param faultName {@link QName} of the Fault
	 * @return new {@link Throw}
	 */
	public static Throw createThrowForUserDefinedFault(QName faultName) {
		Throw newThrow = BPELFactory.eINSTANCE.createThrow();
		newThrow.setFaultName(faultName);
		return newThrow;
	}
	
	/**
	 * Create new {@link Catch}-Fault Handler for given Fault
	 * 
	 * @param faultName {@link QName} of the Fault
	 * @return new {@link Catch}
	 */
	public static Catch createCatchForUserDefinedFault(QName faultName) {
		Catch newCatch = BPELFactory.eINSTANCE.createCatch();
		newCatch.setFaultName(faultName);
		return newCatch;
	}
	
	/**
	 * Add given {@link Catch}-Fault Handler to given
	 * {@link BPELExtensibleElement} (It must be an {@link Process} or a
	 * {@link Scope}!)
	 * 
	 * @param fh {@link Catch}-Fault Handler to add
	 * @param elem {@link BPELExtensibleElement} to add the {@link Catch} to
	 */
	public static void addCatchToBPELExtensibleElement(Catch fh, BPELExtensibleElement elem) {
		if (elem instanceof Process) {
			Process proc = (Process) elem;
			if (proc.getFaultHandlers() == null) {
				proc.setFaultHandlers(BPELFactory.eINSTANCE.createFaultHandler());
			}
			proc.getFaultHandlers().getCatch().add(fh);
		} else {
			// elem must be a <scope>
			Scope scope = (Scope) elem;
			if (scope.getFaultHandlers() == null) {
				scope.setFaultHandlers(BPELFactory.eINSTANCE.createFaultHandler());
			}
			scope.getFaultHandlers().getCatch().add(fh);
		}
	}
	
	/**
	 * Check whether given {@link Activity} is contained in {@link FaultHandler}
	 * , {@link CompensationHandler}, {@link TerminationHandler} or
	 * {@link EventHandler}
	 * 
	 * @param act {@link Activity} to check
	 * @return true or false
	 */
	public static boolean isActivityInFCTEHandler(Activity act) {
		if (act == null) {
			throw new NullPointerException("argument is null. act == null:" + (act == null));
		}
		EObject container = act.eContainer();
		while (!(container instanceof Process)) {
			if ((container instanceof FaultHandler) || (container instanceof CompensationHandler) || (container instanceof TerminationHandler) || (container instanceof EventHandler)) {
				return true;
			}
			// Climb up
			container = container.eContainer();
		}
		
		return false;
	}
	
	/**
	 * Check whether given {@link BPELExtensibleElement} is in an
	 * {@link CompensationHandler} or {@link EventHandler}
	 * 
	 * @param elem {@link BPELExtensibleElement} to check
	 * @return true or false
	 */
	public static boolean isElementInCEHandler(BPELExtensibleElement elem) {
		if (elem == null) {
			throw new NullPointerException("argument is null. elem == null:" + (elem == null));
		}
		
		return ChoreoMergeUtil.isElementContainedIn(elem, Arrays.asList(CompensationHandler.class, EventHandler.class));
	}
	
	/**
	 * Check whether given {@link BPELExtensibleElement} is in an FCTE-Handler
	 * 
	 * @param elem {@link BPELExtensibleElement} to check
	 * @return true or false
	 */
	public static boolean isElementInFCTEHandler(BPELExtensibleElement elem) {
		if (elem == null) {
			throw new NullPointerException("argument is null. elem == null:" + (elem == null));
		}
		List<Class<? extends BPELExtensibleElement>> typesToCheck = Arrays.asList(FaultHandler.class, CompensationHandler.class, TerminationHandler.class, EventHandler.class);
		return ChoreoMergeUtil.isElementContainedIn(elem, typesToCheck);
	}
	
	/**
	 * Check whether given {@link BPELExtensibleElement} is in a Loop
	 * 
	 * @param elem {@link BPELExtensibleElement} to check
	 * @return true or false
	 */
	public static boolean isElementInLoop(BPELExtensibleElement elem) {
		if (elem == null) {
			throw new NullPointerException("argument is null. elem == null:" + (elem == null));
		}
		List<Class<? extends BPELExtensibleElement>> typesToCheck = new ArrayList<>();
		typesToCheck.add(While.class);
		typesToCheck.add(RepeatUntil.class);
		typesToCheck.add(ForEach.class);
		return ChoreoMergeUtil.isElementContainedIn(elem, typesToCheck);
	}
	
	/**
	 * Check whether given {@link BPELExtensibleElement} is contained in one of
	 * the given types
	 * 
	 * @param elem {@link BPELExtensibleElement} to check
	 * @param list {@link List} of {@link BPELExtensibleElement}
	 * @return true or false
	 */
	public static boolean isElementContainedIn(BPELExtensibleElement elem, List<Class<? extends BPELExtensibleElement>> list) {
		if ((list == null) || (list.size() == 0)) {
			throw new RuntimeException("types is null : " + (list == null) + " or includes no Classes : " + (list.size() == 0));
		}
		EObject container = elem.eContainer();
		while (!(container instanceof Process)) {
			for (Class<?> class1 : list) {
				if (class1.isInstance(container)) {
					return true;
				}
			}
			// Climb up
			container = container.eContainer();
		}
		return false;
	}
	
	/**
	 * Get the {@link FaultHandler} or {@link CompensationHandler} or
	 * {@link TerminationHandler} or {@link EventHandler} which contain the
	 * given {@link Activity}
	 * 
	 * @param act {@link Activity} to get FCTE-Handler of
	 * @return {@link BPELExtensibleElement}
	 */
	public static BPELExtensibleElement getFCTEHandlerOfActivity(Activity act) {
		if (act == null) {
			throw new NullPointerException("argument is null. act == null:" + (act == null));
		}
		EObject container = act.eContainer();
		while (!(container instanceof Process)) {
			if ((container instanceof Catch) || (container instanceof CatchAll) || (container instanceof CompensationHandler) || (container instanceof TerminationHandler) || (container instanceof OnEvent) || (container instanceof OnAlarm)) {
				return (BPELExtensibleElement) container;
			}
			// Climb up
			container = container.eContainer();
		}
		return null;
	}
	
	/**
	 * Get {@link Activity} contained in given FCTE-Handler
	 * 
	 * @param fcte FCTE-Handler
	 * @return Contained {@link Activity}
	 */
	public static Activity getActivityFromFCTEHandler(BPELExtensibleElement fcte) {
		if (fcte == null) {
			throw new NullPointerException("argument is null. fcte == null:" + (fcte == null));
		}
		Activity act = null;
		if (fcte instanceof Catch) {
			act = ((Catch) fcte).getActivity();
		} else if (fcte instanceof CatchAll) {
			act = ((CatchAll) fcte).getActivity();
		} else if (fcte instanceof CompensationHandler) {
			act = ((CompensationHandler) fcte).getActivity();
		} else if (fcte instanceof TerminationHandler) {
			act = ((TerminationHandler) fcte).getActivity();
		} else if (fcte instanceof OnEvent) {
			act = ((OnEvent) fcte).getActivity();
		} else if (fcte instanceof OnAlarm) {
			act = ((OnAlarm) fcte).getActivity();
		}
		return act;
	}
	
	/**
	 * Set given {@link Activity} to given FCTE-Handler
	 * 
	 * @param fcte FCTE-Handler
	 * @param act {@link Activity} to set
	 */
	public static void setActivityForFCTEHandler(BPELExtensibleElement fcte, Activity act) {
		if ((fcte == null) || (act == null)) {
			throw new NullPointerException("argument is null. fcte == null:" + (fcte == null) + " act == null: " + (act == null));
		}
		if (fcte instanceof Catch) {
			((Catch) fcte).setActivity(act);
		} else if (fcte instanceof CatchAll) {
			((CatchAll) fcte).setActivity(act);
		} else if (fcte instanceof CompensationHandler) {
			((CompensationHandler) fcte).setActivity(act);
		} else if (fcte instanceof TerminationHandler) {
			((TerminationHandler) fcte).setActivity(act);
		} else if (fcte instanceof OnEvent) {
			((OnEvent) fcte).setActivity(act);
		} else if (fcte instanceof OnAlarm) {
			((OnAlarm) fcte).setActivity(act);
		}
	}
	
	/**
	 * Uplift the given {@link Variable} declaration to the given
	 * {@link Process}
	 * 
	 * @param v {@link Variable} to uplift the declaration of
	 * @param process {@link Process} for new declaration
	 */
	public static void upliftVariableToProcessScope(Variable v, Process process) {
		ChoreoMergeUtil.log.info("Uplifting variable " + v.getName());
		// Get the <scope> declaring v
		Scope scopeOfv = (Scope) v.eContainer().eContainer();
		ChoreoMergeUtil.log.info("From scope v-<scope> " + scopeOfv);
		ChoreoMergeUtil.log.info("To process-<scope> of " + process.getName());
		
		// Remove v from scopeOfv
		scopeOfv.getVariables().getChildren().remove(v);
		
		// Add v to process<scope> of the given process
		if (process.getVariables() == null) {
			process.setVariables(BPELFactory.eINSTANCE.createVariables());
		}
		
		// Check if there's already another variable with the same name
		for (Variable variable : process.getVariables().getChildren()) {
			if (variable.getName().equals(v.getName())) {
				throw new RuntimeException("Problem : There's already another variable declared in process " + process.getName() + "\n" + "with the name " + v.getName());
			}
		}
		
		process.getVariables().getChildren().add(v);
	}
	
	/**
	 * Create new combined joinCondition in given {@link Activity} and given
	 * {@link Link}
	 * 
	 * @param act {@link Activity} to combine joinCondition in
	 * @param link {@link Link} to be combined in new joinCondition
	 */
	public static void combineJCWithLink(Activity act, Link link) {
		String jcNew = "$" + link.getName();
		if (act.getTargets() != null) {
			if (act.getTargets().getJoinCondition() != null) {
				jcNew += " and (" + act.getTargets().getJoinCondition().getBody() + ")";
			} else {
				String jcOld = ChoreoMergeUtil.targetDisjunctor(act);
				if (!jcOld.equals("")) {
					jcNew += " and (" + jcOld + ")";
				}
			}
			Condition newCondition = BPELFactory.eINSTANCE.createCondition();
			newCondition.setBody(jcNew);
			act.getTargets().setJoinCondition(newCondition);
		}
		
	}
	
	public static void setPkg(ChoreographyPackage pkg) {
		ChoreoMergeUtil.pkg = pkg;
	}
}
