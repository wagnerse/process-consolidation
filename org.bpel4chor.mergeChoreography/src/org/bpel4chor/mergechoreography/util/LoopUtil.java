package org.bpel4chor.mergechoreography.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.bpel4chor.model.topology.impl.Participant;
import org.bpel4chor.model.topology.impl.ParticipantSet;
import org.bpel4chor.model.topology.impl.ParticipantType;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Assign;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Condition;
import org.eclipse.bpel.model.Copy;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.ForEach;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Link;
import org.eclipse.bpel.model.PartnerLink;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.While;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;

import de.uni_stuttgart.iaas.bpel.model.utilities.ActivityIterator;
import de.uni_stuttgart.iaas.bpel.model.utilities.FragmentDuplicator;
import de.uni_stuttgart.iaas.bpel.model.utilities.MyBPELUtils;

/**
 * The LoopUtil helps in merging static and dynamic loops. 
 * It also helps in unrolling participant sets based on the interaction
 * pattern it is modeled for.
 * The loop constructs for static & dynamic loops is While and
 * for interaction loop is ForEach
 * 
 * @since August 1, 2016
 * @author Shruthi V Kukillaya
 */

public class LoopUtil implements Constants {
	private static Logger log = Logger.getLogger(LoopUtil.class.getPackage().getName());

	private static ChoreographyPackage choreographyPackage;

	private static List<String> staticLoopTypesFound = new ArrayList<>();
	private static Map<Link, List<Link>> newLinksForStatic = new HashMap<>();
	private static List<Link> linkUnrolled = new ArrayList<>();
	private static List<Link> mergedLinkUnrolled = new ArrayList<>();
	private static List<Link> linkUpliftForStatic = new ArrayList<>();
	
	private static List<String> dynamicLoopTypesFound = new ArrayList<>();
	private static List<Link> linkUplift = new ArrayList<>();
	private static Map<Link, Scope> newDynamicScope = new HashMap<>();
	
	private static List<Link> orgLinks = new ArrayList<>();
	private static Map<String, String> LoopUtilLinks = new HashMap<>();

	/**
	 * Process each {@link Link} of the merged {@link Flow} for loop determination
	 * Invoke isElementInLoop function for the {@link Source} & {@link Target}
	 * 
	 * @param choreographyPackage
	 *            {@link ChoreographyPackage} contains information for
	 *            processing
	 * @return null
	 */

	public static void process(ChoreographyPackage choreographyPackage) {
		LoopUtil.log.log(Level.INFO, "Running LoopUtil .....");
		LoopUtil.log.info(" ");

		LoopUtil.choreographyPackage = choreographyPackage;
		Flow mergedFlow = (Flow) choreographyPackage.getMergedProcess().getActivity();

		for (Link link : mergedFlow.getLinks().getChildren()) {
			LoopUtil.log.info("Link: " + link);

			Activity source = link.getSources().get(0).getActivity();
			isElementInLoop(source, link);

			Activity target = link.getTargets().get(0).getActivity();
			isElementInLoop(target, link);
		}
		
		// add the newly created links for static unrolling to the merged process
		for (Link l : mergedLinkUnrolled){
			for (Link l1 : newLinksForStatic.get(l)) {
				ChoreoMergeUtil.addLinkToFlow(mergedFlow, l1); 
			}
			ChoreoMergeUtil.removeLinkFromFlow(mergedFlow, l);
		}
		
		for (Link l : linkUpliftForStatic) {
			ChoreoMergeUtil.addLinkToFlow(mergedFlow, l); 
		}
		
		// Dynamic related links to the merged process
		for (Link l : linkUplift) {
			ChoreoMergeUtil.addLinkToFlow(mergedFlow, l);
		}
		
		for (Link l : newDynamicScope.keySet()) {
			mergedFlow.getActivities().add(newDynamicScope.get(l));
		}
	}

	/**
	 * Determine if the {@link Activity} is in a loop and call the required functions
	 * based on the type of loop : static or dynamic
	 * 
	 * @param linkActivity
	 *            {@link Activity} of control link (source or target)
	 * @param mergedLink
	 *            {@link Link} of the merged process
	 */

	private static void isElementInLoop(Activity linkActivity, Link mergedLink) {
		
		List<Class<? extends BPELExtensibleElement>> typesToCheck = new ArrayList<>();
		typesToCheck.add(While.class);
		
		Boolean success = false;

		// Iterate upwards through the containers of the link activity and check if it is
		// contained within a loop construct. 
		EObject container = linkActivity.eContainer();
		while (!(container instanceof Process) && !success) {
			for (Class<?> class1 : typesToCheck) {
				if (class1.isInstance(container)) {
					LoopUtil.log.info("Link activity is contained within a loop construct: " + linkActivity.toString());

					if (container instanceof While) {
						While loopActivity = (While) container; 
						// Static loop is identified by the BPEL Extensible Element "max" which is present as a attribute
						// to the loop construct
						if (loopActivity.getElement().hasAttributeNS("http://www.iaas.uni-stuttgart.de/loopconsolidation/extension", "max")){ 
							staticLoopUnroll(loopActivity, loopActivity.getActivity(), Integer.parseInt(loopActivity.getElement().getAttribute("ext:max")), loopActivity.getCondition(), mergedLink); 
							success = true; 
							break;
						}
						else {
							dynamicLoopMerge(loopActivity, loopActivity.getActivity(), loopActivity.getCondition().getBody(), mergedLink);
							success = true; 
							break;
						}
					}
				}
			}
			// Climb up
			container = container.eContainer();
		}
	}

	/**
	 * Processing of static loop
	 * 
	 * @param loopActivity
	 *            loop construct {@link Activity}
	 * @param loopChildActivity
	 *            child {@link Activity} of the loop construct
	 * @param max
	 *            maximum iteration
	 * @param loopCondition
	 *            {@link Condition} of the loop construct           
	 * @param mergedLink
	 *            {@link Link} of the merged process under analysis         
	 * @return null
	 */

	private static void staticLoopUnroll(Activity loopActivity, Activity loopChildActivity, int max, Condition loopCondition, Link mergedLink) {

 		Boolean alreadyVisited = false;
		int i, index;
		Map <String, Link> otherLinks = new HashMap<>();
		List <Link> nextLinkForTarget = new ArrayList<>();
		
		LoopUtil.log.info("STATIC LOOP ");
		
		// Check if the loop construct is already visited
		for (String name : staticLoopTypesFound) {
			if (name.equals(loopActivity.toString())) {
				LoopUtil.log.info("Loop construct already visited ");
				if (mergedLinkUnrolled.indexOf(mergedLink) == -1){
					mergedLinkUnrolled.add(mergedLink);
				}
				alreadyVisited = true;
				break;
			}
		}

		if (!alreadyVisited) {
			LoopUtil.log.info("Copy sequence initiated ");

			Flow unrolledFlow = BPELFactory.eINSTANCE.createFlow();
			unrolledFlow.setName("UnrolledLoop");

			Link entryLink = initializeUnrolledFlow(unrolledFlow, loopActivity, loopCondition); 
			
			orgLinks.clear();
			// find the other links present in the loop construct children activities
			otherLinks = findOtherLinks(loopChildActivity, mergedLink);
			
			for (i = 0; i < max; i++) {
				// for the current link as well as the other links existing in the loop construct children activities
				// create a new link with a new name
				createLinkForALoopInteration(mergedLink, i);
				if (otherLinks != null)
					for (String linkName : otherLinks.keySet()) {
						createLinkForALoopInteration(otherLinks.get(linkName), i);
					}

				// modify the links associated with the direct loop child activity by removing
				// the existing links & adding the newly created links
				modifyLinksOfLoopChildActivity(loopChildActivity, otherLinks, i);
				
				// copy the child activity of the loop construct
				Activity newActivity = PBDFragmentDuplicator.copyActivity(loopChildActivity);
				newActivity.setName(newActivity.getName() + (i + 1));
				
				// change the condition body of the join and transition condition to reflect
				// the new link names
				modifyJoinOrTransitionCondition (newActivity);
				
				Link loopNext = BPELFactory.eINSTANCE.createLink();
				
				// for first iteration attach the entry link
				if (i == 0)
					ChoreoMergeUtil.createTarget4LinkInActivity(entryLink, newActivity);
				
				// for each iteration except the last; create a "Next" link, attach the source of this link 
				// and the target of the previously created "Next" link with the newly copied activity 
				if (i != max - 1) {
					loopNext.setName(createLinkName("Next", i));
					Condition loopConditionForNext = FragmentDuplicator.copyCondition(loopCondition);
					ChoreoMergeUtil.createSource4LinkInActivity(loopNext, newActivity).setTransitionCondition(loopConditionForNext);
					ChoreoMergeUtil.addLinkToFlow(unrolledFlow, loopNext);
					
					if (i > 0) {
						ChoreoMergeUtil.createTarget4LinkInActivity(nextLinkForTarget.get(i-1), newActivity);
					}
					
					nextLinkForTarget.add(loopNext);
				}
				
				// for last iteration attach the target of the previously created "Next" link with the 
				// newly copied activity
				if (i == max - 1) {
					ChoreoMergeUtil.createTarget4LinkInActivity(nextLinkForTarget.get(i-1), newActivity);
				}
				
				unrolledFlow.getActivities().add(newActivity); 
			}

			// add the unrolledFlow to the root of the loop activity & remove the loop activity from
			// the merged process
			EObject rootOfLoopConstruct = loopActivity.eContainer();
			while (!(rootOfLoopConstruct instanceof Process)) {
				if (rootOfLoopConstruct instanceof Sequence) {
					Sequence rootSeq = (Sequence) rootOfLoopConstruct;
					index = rootSeq.getActivities().indexOf(loopActivity);
					rootSeq.getActivities().add(index, unrolledFlow);
					break;
				} else if (rootOfLoopConstruct instanceof Flow) {
					Flow rootFlow = (Flow) rootOfLoopConstruct;
					index = rootFlow.getActivities().indexOf(loopActivity);
					rootFlow.getActivities().add(index, unrolledFlow);
					break;
				}
				// Climb up
				rootOfLoopConstruct = rootOfLoopConstruct.eContainer();
			}

			ChoreoMergeUtil.removeActivityFromContainer(loopActivity);
			
			staticLoopTypesFound.add(loopActivity.toString());
			
			// add all the original links unrolled by the above lines. The unrolled links are kept
			// track so that new links are not created for unrolled links but reused
			if (linkUnrolled.indexOf(mergedLink) == -1) {
				linkUnrolled.add(mergedLink);
			}
			if (otherLinks != null)
				for (String linkName : otherLinks.keySet()) 
					if(linkUnrolled.indexOf(otherLinks.get(linkName)) == -1) {
						linkUnrolled.add(otherLinks.get(linkName));
					}	
			
			// keep track of the merged process links unrolled in a seperate list
			if (mergedLinkUnrolled.indexOf(mergedLink) == -1){
				mergedLinkUnrolled.add(mergedLink);
			}
		}
	}

	/**
	 * Create the initial constructs of the unrolled {@link Flow} i.e entry {@link Activity}, start 
	 * & entry {@link Link}s and associate the {@link Link}s with the related activities.
	 * Check if the loop activity has any {@link Source} or {@link Target} and copy the same to the unrolledFlow
	 * 
	 * @param unrolledFlow
	 *            created {@link Flow} construct for loop unroll
	 * @param loopActivity
	 *            loop construct {@link Activity}
	 * @param loopCondition
	 *            {@link Condition} of the loop construct required for transition condition           
	 * @return newly created entry {@link Link} outgoing from the entry activity 
	 */
	
	private static Link initializeUnrolledFlow(Flow unrolledFlow, Activity loopActivity, Condition loopCondition) {
		
		Empty entryActivity = BPELFactory.eINSTANCE.createEmpty();
		entryActivity.setName("EntryActivity");
		
		Link entryLink = BPELFactory.eINSTANCE.createLink();
		entryLink.setName(createLinkName(loopActivity.getName() +"_Entry"));
		
		Condition loopConditionForEntry = FragmentDuplicator.copyCondition(loopCondition);
		
		ChoreoMergeUtil.createSource4LinkInActivity(entryLink, entryActivity).setTransitionCondition(loopConditionForEntry);
		ChoreoMergeUtil.addLinkToFlow(unrolledFlow, entryLink);
		unrolledFlow.getActivities().add(entryActivity);
		
		if (loopActivity.getSources() != null) {
			for (Source source : loopActivity.getSources().getChildren()) {
				choreographyPackage.getPbd2MergedLinks().put(source.getLink(), source.getLink());
			}
			PBDFragmentDuplicator.copySources(loopActivity.getSources(), unrolledFlow);
		}
		
		if (loopActivity.getTargets() != null) {
			for (Target target : loopActivity.getTargets().getChildren()) {
				choreographyPackage.getPbd2MergedLinks().put(target.getLink(), target.getLink());
			}
			PBDFragmentDuplicator.copyTargets(loopActivity.getTargets(), unrolledFlow);
		}
			
		return entryLink;
	}


	/**
	 * Iterate through the loop construct child {@link Activity} and find the other incoming
	 * or outgoing {@link Link}s present in the loop construct children
	 * 
	 * @param loopChildActivity
	 *            child {@link Activity} of the loop construct
	 * @param mergedLink
	 *            {@link Link} of the merged process under analysis           
	 * @return {@link List} of other links
	 */
	
	private static Map<String, Link> findOtherLinks(Activity loopChildActivity, Link mergedLink) {
		
		Map<String, Link> otherLinks = new HashMap<>();
		
		if (loopChildActivity instanceof Flow){
			for (Activity act : ((Flow) loopChildActivity).getActivities()){
				if (act.getSources() != null) {
					for (Source source : act.getSources().getChildren()){
						if (!mergedLink.equals(source.getLink()))
							otherLinks.put(source.getLink().getName(), source.getLink());
					}
				}
					
				if (act.getTargets() != null) {
					for (Target target : act.getTargets().getChildren()){
						if (!mergedLink.equals(target.getLink()))
							otherLinks.put(target.getLink().getName(), target.getLink());
					}
				}
			}
		}
		return otherLinks;
	}

	/**
	 * For the current iteration if unrolled links have not been created, create a new
	 * {@link Link} with a new name and append it into the newLinks {@link Map} 
	 * which maps the original {@link Link} with the list of newly created {@link Link}s 
	 * 
	 * @param oldLink
	 *            {@link Link} of the merged process (under analysis or other links)            
	 * @param i
	 *            current iteration number
	 * @return null
	 */
	
	private static void createLinkForALoopInteration(Link oldLink, int i) {
		
		List<Link> loopLinkList = new ArrayList<>();
		
		// check if links have already been created
		if (linkUnrolled.indexOf(oldLink)== -1){
			Link loopLink = BPELFactory.eINSTANCE.createLink();
			loopLink.setName(createLinkName(oldLink.getName(), i));
			choreographyPackage.getPbd2MergedLinks().put(oldLink, loopLink);
			LoopUtilLinks.put(oldLink.getName(), loopLink.getName());
			LoopUtilLinks.put(loopLink.getName(), oldLink.getName());
			
			// append the new link into the newLinks map
			if (newLinksForStatic.containsKey(oldLink)){
				List<Link> tempList = newLinksForStatic.get(oldLink);
				tempList.add(loopLink);
				newLinksForStatic.put(oldLink, tempList);
			}
			else {
				loopLinkList.add(loopLink);
				newLinksForStatic.put(oldLink, loopLinkList);
			}
		}
		
		else{
			choreographyPackage.getPbd2MergedLinks().put(oldLink, newLinksForStatic.get(oldLink).get(i));
			LoopUtilLinks.put(oldLink.getName(), newLinksForStatic.get(oldLink).get(i).getName());
		}
	}

	/**
	 * Update the loop child {@link Activity} with the relevant {@link Link}s i.e the loop child 
	 * {@link Activity} will contain {@link Link}s relevant to the current iteration
	 * 
	 * @param loopChildActivity
	 *             child {@link Activity} of the loop construct
	 * @param otherLinks
	 *            {@link List} of other {@link Links}           
	 * @param i
	 *            current iteration number
	 * @return null
	 */
	
	private static void modifyLinksOfLoopChildActivity(Activity loopChildActivity, Map <String, Link> otherLinks, int i) {
		
		List <Link> removeLinks = new ArrayList<>();
		
		if (loopChildActivity instanceof Flow) {
			Flow loopChildFlow = (Flow) loopChildActivity;
			
			for (Link orgLink : loopChildFlow.getLinks().getChildren()) {
				for (String linkName : otherLinks.keySet()){
					if (orgLink.equals(otherLinks.get(linkName))) {
						removeLinks.add(orgLink);
						orgLinks.add(orgLink);
						break;
					}
				}
			}
			
			for (Link l : removeLinks)
				ChoreoMergeUtil.removeLinkFromFlow(loopChildFlow, l);
			
			for (Link l : orgLinks)
				ChoreoMergeUtil.addLinkToFlow(loopChildFlow, newLinksForStatic.get(l).get(i));
			
			if (i > 0) {
				for (Link l : newLinksForStatic.keySet())
					ChoreoMergeUtil.removeLinkFromFlow(loopChildFlow, newLinksForStatic.get(l).get(i - 1));
			}
		}
		
	}
	
	/**
	 * Update the Join or Transition {@link Condition} of the copied children of the 
	 * loop construct to reflect the new link names
	 * 
	 * @param newActivity
	 *             copied {@link Activity} of the loop construct children
	 * @return null
	 */
	
	private static void modifyJoinOrTransitionCondition(Activity newActivity) {
		
		if (newActivity instanceof Flow){
			for (Activity act : ((Flow) newActivity).getActivities()){
				if (act.getSources() != null) {
					for (Source source : act.getSources().getChildren()){
						if (source.getTransitionCondition() != null) {
							String orgLinkName = LoopUtilLinks.get(source.getLink().getName());
							source.getTransitionCondition().setBody(source.getTransitionCondition().getBody().toString().replace(orgLinkName, LoopUtilLinks.get(orgLinkName)));
						}
					}
				}

				if (act.getTargets() != null) {
					if (act.getTargets().getJoinCondition() != null) {
						for (Target target : act.getTargets().getChildren()) {
							String orgLinkName = LoopUtilLinks.get(target.getLink().getName());
							act.getTargets().getJoinCondition().setBody(act.getTargets().getJoinCondition().getBody().toString().replace(orgLinkName, LoopUtilLinks.get(orgLinkName)));
						}	
					}
				}
				
				if (act instanceof Flow) {
					modifyJoinOrTransitionCondition(act);
				}
			}
		}
	}

	/**
	 * Create a name for the unrolled {@link Link}
	 * 
	 * @param name
	 *             name for the link          
	 * @param i
	 *            current iteration number
	 * @return concatenated name string
	 */
	
	private static String createLinkName(String name, int i) {
		return PREFIX_NAME_STATIC_LOOP_LINK + name + (i+1);
	}

	private static String createLinkName(String name) {
		return PREFIX_NAME_STATIC_LOOP_LINK + name;
	}

	/**
	 * Merge processing of dynamic loop
	 * 
	 * @param loopActivity
	 *            loop construct {@link Activity}
	 * @param loopChildActivity
	 *            child {@link Activity} of the loop construct
	 * @param loopCondition
	 *            {@link Condition} of the loop construct           
	 * @param mergedLink
	 *            {@link Link} of the merged process under analysis                        
	 * @return null
	 */
	
	private static void dynamicLoopMerge(Activity loopActivity, Activity loopChildActivity, Object loopCondition, Link mergedLink) {
		Boolean alreadyVisited = false;
		Map <String, Link> otherLinks = new HashMap<>();
	
		Scope newScope;
		
		LoopUtil.log.info("DYNAMIC LOOP ");

		// Check if the loop construct is already visited
		for (String name : dynamicLoopTypesFound) {
			if (name.equals(loopActivity.toString())) {
				LoopUtil.log.info("Loop construct already visited " + loopActivity.toString());
				alreadyVisited = true;
				break;
			}
		}

		if (!alreadyVisited) {
			LoopUtil.log.info("Copy sequence initiated ");

			//uplift any variables present in the loop construct body to the main merged process flow
			upliftVariables(loopChildActivity);
			// find the other links present in the loop construct children activities
			otherLinks = findOtherLinks(loopChildActivity, mergedLink);

			if (otherLinks != null)
				for (String linkName : otherLinks.keySet()) 
					choreographyPackage.getPbd2MergedLinks().put(otherLinks.get(linkName), otherLinks.get(linkName));
			choreographyPackage.getPbd2MergedLinks().put(mergedLink, mergedLink);
			
			//check if a dynamic scope is already created for the link under analysis. If already created,
			//the body and condition of the loop needs to be combined with the created dynamic while activity
			if (newDynamicScope.containsKey(mergedLink)){
				newScope = newDynamicScope.get(mergedLink);
				newScope.setName(newScope.getName() + loopActivity.getName());
				
				//Add the successor and predecessor activities of the loop activity to the dynamic scope in order to preserve
				//original trace of activities
				if (loopActivity.getTargets() != null) {		
					addPredecessorActivities(loopActivity, newScope);
				}
						
				if (loopActivity.getSources() != null) {
					addSuccessorActivities(loopActivity, newScope);
				}
				
				While dynamicWhile = (While) newScope.getActivity();
				dynamicWhile.setName(dynamicWhile.getName() + loopActivity.getName());

				//Combine the condition of the loop with the existing condition of the dynamic loop
				Condition joinCondition = BPELFactory.eINSTANCE.createCondition();
				joinCondition.setBody(dynamicWhile.getCondition().getBody() + " or " + loopCondition);
				dynamicWhile.setCondition(joinCondition);

				Empty entryActivity = (Empty) ((Flow)dynamicWhile.getActivity()).getActivities().get(0);
					
				Activity newActivity = PBDFragmentDuplicator.copyActivity(loopChildActivity);
				
				//Create a new link with name & condition of the loop activity and connect the entry
				//activity with duplicated body of the loop 	
				entryActivity = createEntryLink(loopActivity, entryActivity, loopCondition, newActivity);
							
				ChoreoMergeUtil.replaceActivity(((Flow)dynamicWhile.getActivity()).getActivities().get(0), entryActivity);
					
				((Flow)dynamicWhile.getActivity()).getActivities().add(newActivity);
				ChoreoMergeUtil.replaceActivity(newScope.getActivity(), dynamicWhile);
			}
			else {
				newScope = BPELFactory.eINSTANCE.createScope();
				newScope.setName(loopActivity.getName());
					
				//Add the successor and predecessor activities of the loop activity to the dynamic scope in order to preserve
				//original trace of activities
				if (loopActivity.getTargets() != null) {
					addPredecessorActivities(loopActivity, newScope);
				}
				
				if (loopActivity.getSources() != null) {
					addSuccessorActivities(loopActivity, newScope);
				}	
				
				//Initialize the dynamic loop with the basic activities i.e a new while activity which will contain the merged loop bodies
				//and an entry activity which decides based on condition which loop body is to be executed
				
				While dynamicWhile = BPELFactory.eINSTANCE.createWhile();
				dynamicWhile.setName(loopActivity.getName());
					
				Condition condition = BPELFactory.eINSTANCE.createCondition();
				condition.setBody(loopCondition);
				dynamicWhile.setCondition(condition);
					
				Flow dynamicFlow = BPELFactory.eINSTANCE.createFlow();
				dynamicFlow.setName("DynamicFlow");
					
				Empty entryActivity = BPELFactory.eINSTANCE.createEmpty();
				entryActivity.setName("EntryActivity");
				
				Activity newActivity = PBDFragmentDuplicator.copyActivity(loopChildActivity);

				//Create a new link with name & condition of the loop activity and connect the entry
				//activity with duplicated body of the loop 
				createEntryLink(loopActivity, entryActivity, loopCondition, newActivity);
					
				dynamicFlow.getActivities().add(entryActivity);
				dynamicFlow.getActivities().add(newActivity);
				dynamicWhile.setActivity(dynamicFlow);
				newScope.setActivity(dynamicWhile);
			}
			
			ChoreoMergeUtil.removeActivityFromContainer(loopActivity);
			
			dynamicLoopTypesFound.add(loopActivity.toString());

			newDynamicScope.put(mergedLink, newScope);
		}
	}

	/**
	 * Uplifts the {@link Variable}'s present in the loop body. Before uplifting 
	 * the {@link Variable}'s a check is made to determine if the variable name 
	 * is not already present in the merged {@link Flow} 
	 * 
	 * @param loopChildActivity
	 *            loop child {@link Activity}
	 * @return null
	 */
	
	private static void upliftVariables(Activity loopChildActivity) {
		Process mergedProcess = choreographyPackage.getMergedProcess();
		List <Variable> upliftVariable = new ArrayList<>();
		List <Variable> upliftCandidate = new ArrayList<>();
		Boolean noUplift;
		
		if (loopChildActivity instanceof Flow) {
			for (Activity act : ((Flow) loopChildActivity).getActivities()){
				if (act instanceof Assign){
					if (((Assign) act).getCopy() != null) {
						for (Copy copy : ((Assign) act).getCopy()){
							upliftCandidate.add(copy.getFrom().getVariable());
							upliftCandidate.add(copy.getTo().getVariable());
						}
						
						for (Variable var : upliftCandidate) {
							noUplift = false;
							for (Variable variable : mergedProcess.getVariables().getChildren()) {
								if (var.getName().equals(variable.getName()))
									noUplift = true;
							}
							if (!noUplift)
								upliftVariable.add(var);
						}
					} 
				}
			}
		}
		
		for (Variable variable : upliftVariable) {
			LoopUtil.log.info("Uplift " + variable.getName());
			ChoreoMergeUtil.upliftVariableToProcessScope(variable, mergedProcess);
		}
	}

	/**
	 * Create a new {@link Link} with name & {@link Condition} of the loop {@link Activity} 
	 * and connect the entry {@link Activity} with duplicated body of the loop 
	 * 
	 * @param loopActivity
	 *            loop construct {@link Activity}
	 * @param entryActivity
	 *            entry {@link Activity} of the dynamic loop
	 * @param loopCondition
	 *            {@link Condition} of the loop construct           
	 * @param newActivity
	 *            duplicated body of loop {@link Activity} 
	 * @return entryActivity
	 *            entry {@link Activity} of the dynamic loop
	 */
	
	private static Empty createEntryLink(Activity loopActivity, Empty entryActivity, Object loopCondition, Activity newActivity) {
		Link loopBodyLink = BPELFactory.eINSTANCE.createLink();
		loopBodyLink.setName(createDynamicLinkName(loopActivity));
		
		Source sourceEntryActivity = ChoreoMergeUtil.createSource4LinkInActivity(loopBodyLink, entryActivity);
		Condition entryActivityCondition = BPELFactory.eINSTANCE.createCondition();
		entryActivityCondition.setBody(loopCondition);
		sourceEntryActivity.setTransitionCondition(entryActivityCondition);
	
		ChoreoMergeUtil.createTarget4LinkInActivity(loopBodyLink, newActivity);

		linkUplift.add(loopBodyLink);
		
		return entryActivity;
	}
	
	/**
	 * Add the successor {@link Activity}'s of the loop {@link Activity} to the 
	 * {@link Scope} {@link Activity} of the dynamic loop
	 * 
	 * @param loopActivity
	 *            loop construct {@link Activity}
	 * @param newScope
	 *            {@link Scope} {@link Activity} of the dynamic loop           
	 * @return null
	 */
	
	private static void addSuccessorActivities(Activity loopActivity, Scope newScope) {
		List <Source> dynamicSource = new ArrayList<>();
		for (Source source : loopActivity.getSources().getChildren()) {
			dynamicSource.add(source);
			Flow linkOwner = ChoreoMergeUtil.findLinkOwnerFlow (loopActivity, source.getLink().getName());
			ChoreoMergeUtil.removeLinkFromFlow(linkOwner, source.getLink());
			linkUplift.add(source.getLink());
		}

		for (Source source : dynamicSource) {
			ChoreoMergeUtil.addSourceToActivity(newScope, source);
		}
	}
	
	/**
	 * Add the predecessor {@link Activity}'s of the loop {@link Activity} to the 
	 * {@link Scope} {@link Activity} of the dynamic loop
	 * 
	 * @param loopActivity
	 *            loop construct {@link Activity}
	 * @param newScope
	 *            {@link Scope} {@link Activity} of the dynamic loop           
	 * @return null
	 */
	
	private static void addPredecessorActivities(Activity loopActivity, Scope newScope) {
		List <Target> dynamicTarget = new ArrayList<>();
		for (Target target : loopActivity.getTargets().getChildren()) {
			Flow linkOwner = ChoreoMergeUtil.findLinkOwnerFlow (loopActivity, target.getLink().getName());
			ChoreoMergeUtil.removeLinkFromFlow(linkOwner, target.getLink());
			linkUplift.add(target.getLink());
			dynamicTarget.add(target);
		}

		for (Target target : dynamicTarget) {
			ChoreoMergeUtil.combineJCWithLink(newScope, target.getLink());
			ChoreoMergeUtil.addTargetToActivity(newScope, target);
		}
	}
	
	/**
	 * Create a name required for dynamic loop {@link Link}
	 * 
	 * @param loopActivity
	 *             {@link Activity}          
	 * @return concatenated name string
	 */
	
	private static String createDynamicLinkName(Activity loopActivity) {
		return PREFIX_NAME_DYNAMIC_LOOP_LINK + loopActivity.getName();
	}
	
	/**
	 * Find the participant to be duplicated and duplicate the same. The duplicated
	 * process instance is added to the merged process
	 * 
	 * @param choreographyPackage
	 *            {@link ChoreographyPackage} contains information for
	 *            processing
	 * @return null
	 */
	
	public static void participantDuplication(ChoreographyPackage choreographyPackage) {
		LoopUtil.log.log(Level.INFO, "Running LoopUtil .....");
		LoopUtil.log.info(" ");
		
		String pSetName = null;
		String duplicateProcess = null;
		String participantName = null;
		
		// Iterate through the participant set and match the participant type within the 
		// set with the participant type present in the topology to get the participant
		// which needs to be duplicated.
		if (choreographyPackage.getTopology().getParticipantSets() != null){
			for (ParticipantSet ps: choreographyPackage.getTopology().getParticipantSets()){
				pSetName = ps.getName();
				for (Participant p: ps.getParticipantList()){
					String participantType = p.getType(); 
					participantName = p.getName();
					for (ParticipantType pt : choreographyPackage.getTopology().getParticipantTypes()){
						if (participantType.equals(pt.getParticipantBehaviorDescription().getLocalPart().toString()))
							duplicateProcess = pt.getParticipantBehaviorDescription().getLocalPart();
					}
				}
			}
		}
		
		List <Process> participantsUnrolled = new ArrayList<>();
		Map <MessageLink, List<MessageLink>> oldToNewParticipantsLinks = new HashMap<>();
		List <MessageLink> participantsUnrolledLinks = new ArrayList<>();
		List <Variable> newVariables = new ArrayList<>();
		Map <MessageLink, String> reqLink = new HashMap<>();
		List <Process> initialProcess = choreographyPackage.getPbds();
		Activity loopActivity = null;
		
		if (duplicateProcess != null){
			LoopUtil.log.info("PROCESS DUPLICATION ");
			LoopUtil.log.info(" ");
			
			LoopUtil.log.log(Level.INFO, "Duplicating process: " + duplicateProcess);
	
			// Iterate through all PBDs and match with participant to be duplicated
			for (Process process : choreographyPackage.getPbds()) {
				if (duplicateProcess.equals(process.getName())){
					
					// Iterate through all the message links present in the topology and match the 
					// participant name of the participant set with the receiver or sender attribute
					// of the message link. This match defines what type of interaction service pattern
					// the choreography is modeled upon. This matched message link is added to reqLink
					// along with the pattern name to be processed later
					for (MessageLink link : choreographyPackage.getTopology().getMessageLinks()) {
						if (link.getReceiver().equals(participantName)) {
							reqLink.put(link, "OneToManySend");	
						}
						if (link.getSender().equals(participantName)) {
							reqLink.put(link, "OneFromManyReceive");	
						}
					}
					
					// The number of times the participant is to be duplicated is given by the size of 
					// the participants defined in participantset.xml 
					// The participant is duplicated one less than the size as the existing participant 
					// is taken as the first participant
					for (int i = 1; i < choreographyPackage.getPSet2Participants().get(pSetName).size(); i++) {
					
						Process duplicate = BPELFactory.eINSTANCE.createProcess();
					
						duplicate.setSuppressJoinFailure(process.getSuppressJoinFailure());
						for (PartnerLink pl : process.getPartnerLinks().getChildren()){
							PartnerLink newLink = PBDFragmentDuplicator.copyPartnerLink(pl);
							if (duplicate.getPartnerLinks() == null) {
								duplicate.setPartnerLinks(BPELFactory.eINSTANCE.createPartnerLinks());
								duplicate.getPartnerLinks().getChildren().add(newLink);
							}
							else
								duplicate.getPartnerLinks().getChildren().add(newLink);
						}	
					
						for (Variable variable : process.getVariables().getChildren()) {
							Variable newVariable = PBDFragmentDuplicator.copyVariable(variable);
							if (duplicate.getVariables() == null) {
								duplicate.setVariables(BPELFactory.eINSTANCE.createVariables());
								duplicate.getVariables().getChildren().add(newVariable);
							}
							else
								duplicate.getVariables().getChildren().add(newVariable);
	
							// Add <variable>-to-<variable> relation to our
							// pbd2MergdVars-Map
							choreographyPackage.getPbd2MergedVars().put(variable, newVariable);
							newVariables.add(newVariable);
						}
					
						Activity newActivity = PBDFragmentDuplicator.copyActivity(process.getActivity());
						
						duplicate.setActivity(newActivity); 
						
						// Name of the duplicated process is given in participantset.xml 
						duplicate.setName(choreographyPackage.getPSet2Participants().get(pSetName).get(i));
						
						// Iterate through the duplicated process and change the names of the variables of 
						// receive and invoke activity. The names are changed so that it doesn't clash with
						// the names of the existing participant variables
						for (Variable v: newVariables) {
							ActivityIterator actIterator = new ActivityIterator(duplicate);
							while (actIterator.hasNext()) {
								Activity act = actIterator.next();
								if (act instanceof Receive) {
									Receive actReceive = (Receive) act;
									if (actReceive.getVariable() != null) {
										if (actReceive.getVariable().getName().equals(v.getName())) {
											duplicate.getVariables().getChildren().remove(duplicate.getVariables().getChildren().indexOf(actReceive.getVariable()));
											actReceive.getVariable().setName(duplicate.getName() + "_" + actReceive.getVariable().getName());
											duplicate.getVariables().getChildren().add(actReceive.getVariable());
											break;
										}
									}
								}
								else
									if (act instanceof Invoke) {
										Invoke actInvoke = (Invoke) act;
										if (actInvoke.getInputVariable() != null) {
											if (actInvoke.getInputVariable().getName().equals(v.getName())) {
												duplicate.getVariables().getChildren().remove(duplicate.getVariables().getChildren().indexOf(actInvoke.getInputVariable()));
												actInvoke.getInputVariable().setName(duplicate.getName() + "_" + actInvoke.getInputVariable().getName());
												duplicate.getVariables().getChildren().add(actInvoke.getInputVariable());
												break;
											}
										}
										else
											if (actInvoke.getOutputVariable() != null) {
												if (actInvoke.getOutputVariable().getName().equals(v.getName())) {
													duplicate.getVariables().getChildren().remove(duplicate.getVariables().getChildren().indexOf(actInvoke.getOutputVariable()));
													actInvoke.getOutputVariable().setName(duplicate.getName() + "_" + actInvoke.getOutputVariable().getName());		
													duplicate.getVariables().getChildren().add(actInvoke.getOutputVariable());
													break;
												}
											}
									}
							}
						}
					
						// Add the duplicated process to participantsUnrolled in order to add it at the later stage to 
						// the existing PBDs.
						participantsUnrolled.add(duplicate);
						
						// Iterate through reqLink, duplicate the links and modify the name of the send/receive activity
						// of the link present in the duplicate process based on the service interaction pattern
						for (MessageLink ml : reqLink.keySet()) {
							MessageLink newMessageLink = new MessageLink();
							newMessageLink.setName(ml.getName() + i);
							newMessageLink.setMessageName(ml.getMessageName()); 
							newMessageLink.setReceiver(ml.getReceiver());
							newMessageLink.setSender(ml.getSender());
							
							if (reqLink.get(ml).equals("OneToManySend")) {
								newMessageLink.setSendActivity(ml.getSendActivity());
							
								Activity r = (Activity) ChoreoMergeUtil.resolveActivity(ml.getReceiveActivity());
							
								Activity act = MyBPELUtils.resolveActivity(r.getName() , duplicate);
								
								String orgActivityName = act.getName();
								act.setName(orgActivityName + i);
								act.getElement().setAttribute("wsu:id", orgActivityName + i);
								
								LoopUtil.log.log(Level.INFO, "newReceiveActivity => " + act.getName());
								ChoreoMergeUtil.replaceActivity(MyBPELUtils.resolveActivity(r.getName() , duplicate), act);
								newMessageLink.setReceiveActivity(act.getName());
								choreographyPackage.getOld2New().put(act.getName(), act);
							}
							else
								if (reqLink.get(ml).equals("OneFromManyReceive")) {
									newMessageLink.setReceiveActivity(ml.getReceiveActivity());
									
									Activity r = (Activity) ChoreoMergeUtil.resolveActivity(ml.getSendActivity());
								
									Activity act = MyBPELUtils.resolveActivity(r.getName() , duplicate);
									
									String orgActivityName = act.getName();
									act.setName(orgActivityName + i);
									act.getElement().setAttribute("wsu:id", orgActivityName + i);
									
									LoopUtil.log.log(Level.INFO, "newSendActivity => " + act.getName());
									ChoreoMergeUtil.replaceActivity(MyBPELUtils.resolveActivity(r.getName() , duplicate), act);
									newMessageLink.setSendActivity(act.getName());
									choreographyPackage.getOld2New().put(act.getName(), act);
								}
							
							if (oldToNewParticipantsLinks.containsKey(ml)){
								List<MessageLink> tempList = oldToNewParticipantsLinks.get(ml);
								tempList.add(newMessageLink);
								oldToNewParticipantsLinks.put(ml, tempList);
							}
							else {
								List <MessageLink> loopLinkList = new ArrayList<>();
								loopLinkList.add(newMessageLink);
								oldToNewParticipantsLinks.put(ml, loopLinkList);
							}
							participantsUnrolledLinks.add(newMessageLink);
						}
					}

					// Once the duplication of the participant and associated message links are done
					// the activity present in an interaction loop must be unrolled.
					// Iterate through reqLink variable and based on the type of service interaction
					// pattern, obtain the loop activity which would be the send/receive activity
					// of the message link. Call interactionLoop function and pass the loop activity,
					// list of duplicated message links and the type of interaction pattern.
					String loopAct = null;
					String interactionPattern = null;
					for (MessageLink ml : reqLink.keySet()) {
						if (reqLink.get(ml).equals("OneToManySend")) {
							loopAct = ml.getSendActivity();
							interactionPattern = "OneToManySend";
						}
						else
							if (reqLink.get(ml).equals("OneFromManyReceive")) {
								loopAct = ml.getReceiveActivity();
								interactionPattern = "OneFromManyReceive";
							}
						
						for (Process p: initialProcess) {
							ActivityIterator actIterator = new ActivityIterator(p);
							while (actIterator.hasNext()) {
								Activity act = actIterator.next();
								if (act.getElement() != null && act.getElement().hasAttribute("wsu:id")) {
									if (act.getElement().getAttribute("wsu:id").equals(loopAct)) {
										loopActivity = act;
										break;
									}
								}
							}
						}
						Process sendActivityProcess = ChoreoMergeUtil.getProcessOfActivity(loopActivity);
						for (Variable variable : sendActivityProcess.getVariables().getChildren()) {
							choreographyPackage.getPbd2MergedVars().put(variable, variable);
						}
	
						interactionLoop(choreographyPackage, loopActivity, oldToNewParticipantsLinks.get(ml), reqLink.get(ml));
					}
					process.setName(choreographyPackage.getPSet2Participants().get(pSetName).get(0));
				}
			}
		}
		
		// Add the duplicated participants and message links to the choreography package
		for (Process p : participantsUnrolled)
			choreographyPackage.getPbds().add(p);
		
		for (MessageLink ml : participantsUnrolledLinks)
			choreographyPackage.getTopology().add(ml);
	}
	
	/**
	 * Processing of interaction loop
	 * 
	 * @param choreographyPackage
	 *            {@link ChoreographyPackage} contains information for processing
	 * @param linkActivity
	 *            {@link MessageLink} {@link Activity} which to be inspected
	 * @param participantsUnrolledLinks
	 *            list of duplicated {@link MessageLink}
	 * @param interactionPattern
	 *            type of service interaction pattern                  
	 * @return null
	 */
	
	public static void interactionLoop (ChoreographyPackage choreographyPackage, Activity linkActivity, List <MessageLink> participantsUnrolledLinks, String interactionPattern) {
		List <Link> nextLinkForTarget = new ArrayList<>();
		int i, index;				

		Boolean success = false;
		
		// Iterate upwards through the containers of the link activity and check if it is
		// contained within a loop construct, ForEach. 
		EObject container = linkActivity.eContainer();
		while (!(container instanceof Process) && !success) {
			if (ForEach.class.isInstance(container)) {
				LoopUtil.log.info("INTERACTION LOOP");
				
				LoopUtil.log.info("Link activity is contained within a loop construct: " + linkActivity.toString());

				ForEach loopActivity = (ForEach) container;
				Activity loopChildActivity = loopActivity.getActivity();
					
				Flow unrolledFlow = BPELFactory.eINSTANCE.createFlow();
				unrolledFlow.setName(loopActivity.getName() + "_UnrolledLoop");
				
				// Add the predecessor and successor activities to the unrolled flow
				if (loopActivity.getSources() != null) {
					for (Source source : loopActivity.getSources().getChildren()) {
						choreographyPackage.getPbd2MergedLinks().put(source.getLink(), source.getLink());
					}
					PBDFragmentDuplicator.copySources(loopActivity.getSources(), unrolledFlow);
				}
				
				if (loopActivity.getTargets() != null) {
					for (Target target : loopActivity.getTargets().getChildren()) {
						choreographyPackage.getPbd2MergedLinks().put(target.getLink(), target.getLink());
					}
					PBDFragmentDuplicator.copyTargets(loopActivity.getTargets(), unrolledFlow);
				}
				
				// Duplicate the body of the loop construct. Number of times it is to 
				// duplicated is given by the number of participants present in participantset.xml
				for (i = 0; i < participantsUnrolledLinks.size() + 1; i++) {
					Activity newActivity = PBDFragmentDuplicator.copyActivity(loopChildActivity);
						
					// Iterate through the duplicated loop body modify the name of the send/receive activity
					// of the link present in the loop body based on the service interaction pattern
					if (i > 0) {
						TreeIterator<?> iterator = newActivity.eAllContents();
						Object oElement = null;
						while (iterator.hasNext()) {
							oElement = iterator.next();
							if (oElement instanceof Activity) {
								Activity oldAct = (Activity) oElement;
								if (interactionPattern.equals("OneToManySend")) {
									if (oldAct.getName().equals(participantsUnrolledLinks.get(i-1).getSendActivity())) {
										oldAct.setName(oldAct.getName() + i);
										oldAct.getElement().setAttribute("wsu:id", oldAct.getName());
										participantsUnrolledLinks.get(i-1).setSendActivity(oldAct.getName());
										choreographyPackage.getOld2New().put(oldAct.getName(), oldAct);
										break;
									}
								}
								else
									if (interactionPattern.equals("OneFromManyReceive")) {
										if (oldAct.getName().equals(participantsUnrolledLinks.get(i-1).getReceiveActivity())) {
											oldAct.setName(oldAct.getName() + i);
											oldAct.getElement().setAttribute("wsu:id", oldAct.getName());
											participantsUnrolledLinks.get(i-1).setReceiveActivity(oldAct.getName());
											choreographyPackage.getOld2New().put(oldAct.getName(), oldAct);
											break;
										}
									}
							}
						}
						newActivity.setName(newActivity.getName() + i);
						if (loopActivity.getParallel() == false)
							ChoreoMergeUtil.createTarget4LinkInActivity(nextLinkForTarget.get(i-1), newActivity);
					}
					
					// If ForEach is not set for parallel, the loop body must be executed sequentially.
					// Thus, loopNext link keeps that order
					if (loopActivity.getParallel() == false) {
						if (i != participantsUnrolledLinks.size()) {
							Link loopNext = BPELFactory.eINSTANCE.createLink();
							loopNext.setName(createInteractionPLinkName("Next", (i+1)));
							ChoreoMergeUtil.createSource4LinkInActivity(loopNext, newActivity);
							nextLinkForTarget.add(loopNext);
							ChoreoMergeUtil.addLinkToFlow(unrolledFlow, loopNext);
						}
					}
				
					unrolledFlow.getActivities().add(newActivity);
				}
					
				// Add the unrolledFlow to the root of the loop activity & remove the loop activity from
				// the process
				EObject root = loopActivity.eContainer();
				while (!(root instanceof Process)) {
					if (root instanceof Sequence) {
						Sequence rootSeq = (Sequence) root;
						index = rootSeq.getActivities().indexOf(loopActivity);
						rootSeq.getActivities().add(index, unrolledFlow);
						break;
					} else if (root instanceof Flow) {
						Flow rootFlow = (Flow) root;
						index = rootFlow.getActivities().indexOf(loopActivity);
						rootFlow.getActivities().add(index, unrolledFlow);
						break;
					}
					// Climb up
					root = root.eContainer();
				}
				ChoreoMergeUtil.removeActivityFromContainer(loopActivity);
				success = true;
			}
			// Climb up
			container = container.eContainer();
		}
	}

	
	/**
	 * Create a name required for interaction loop {@link Link}
	 * 
	 * @param name
	 *             name for the link          
	 * @param i
	 *            current iteration number         
	 * @return concatenated name string
	 */
	
	private static String createInteractionPLinkName(String name, int i) {
		return PREFIX_NAME_INTERACTIONPATTERN_LOOP_LINK + name + i;
	}
}
