package org.bpel4chor.mergechoreography.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.bpel4chor.mergechoreography.ChoreographyPackage;
import org.bpel4chor.mergechoreography.util.model.ScopeHandlerBean;
import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELExtensibleElement;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Catch;
import org.eclipse.bpel.model.CompensateScope;
import org.eclipse.bpel.model.CompensationHandler;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.EventHandler;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.Flow;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.OnAlarm;
import org.eclipse.bpel.model.OnEvent;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Sequence;
import org.eclipse.bpel.model.Throw;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Variables;
import org.eclipse.bpel.model.Wait;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.wst.wsdl.WSDLElement;
import org.w3c.dom.Element;

import de.uni_stuttgart.iaas.bpel.model.utilities.FragmentDuplicator;

/**
 * Preprocessing Class for {@link EventHandler} containing {@link MessageLink}s
 * 
 * <br>
 * Copyright 2014 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Aleksandar Milutinovic
 * 
 * 
 */

public class MergePreProcessorForEH implements Constants {

	protected static Logger log = Logger.getLogger(MergePreProcessor.class);

	public static void startPreProcessing(ChoreographyPackage choreographyPackage) {
		// alternative EventHandler
		startPreprocessingForEventHandler(choreographyPackage);
		// ### EXTEND PreProcessing code here EXTEND ###
	}

	/**
	 * Searches for {@link EventHandler}s with {@link MessageLink}s within
	 * scopes of {@link OnEvent} or {@link OnAlarm} and generates new
	 * {@link Scope}s which emulate the corresponding {@link EventHandler} in
	 * order to merge successfully
	 * 
	 * @param pkg
	 *            contains the {@link Process}es with {@link EventHandler}s
	 */
	private static void startPreprocessingForEventHandler(ChoreographyPackage pkg) {

		// Tasks:
		// find MessageLinks within EH
		// sort found MessageLinks and determine cases
		// install alternative EventHandler-Logic

		List<OnEvent> onEventList = null;
		List<OnAlarm> onAlarmList = null;

		List<MessageLink> onEventML = null;
		List<MessageLink> onEventReceive = null;
		List<MessageLink> onAlarmML = null;
		onEventML = new ArrayList<>();
		onEventReceive = new ArrayList<>();
		onAlarmML = new ArrayList<>();

		onEventList = new ArrayList<>();
		onAlarmList = new ArrayList<>();

		// check all MessageLinks
		for (MessageLink link : pkg.getTopology().getMessageLinks()) {

			// Resolve send- and receiveActivity
			BPELExtensibleElement s = ChoreoMergeUtil.resolveActivity(link.getSendActivity());
			BPELExtensibleElement r = ChoreoMergeUtil.resolveActivity(link.getReceiveActivity());
			
			// FIXME: Check here if s and r are in EH
			
			if (ChoreoMergeUtil.isElementInEHandler(r) && ChoreoMergeUtil.isElementInEHandler(s)) {
				log.info("EH-Preprocessing: receive and send are both within EventHandler-Scope");
				if ((ChoreoMergeUtil.getEHandlerOfActivity(r) instanceof OnAlarm) && 
						(ChoreoMergeUtil.getEHandlerOfActivity(s) instanceof OnAlarm)) {
					log.info("EH-Preprocessing: s and r are in OnAlarms. No Problem here. Starting later with modifying EH-scope of s");
				} else {
					if ((ChoreoMergeUtil.getEHandlerOfActivity(r) instanceof OnEvent) && 
							(ChoreoMergeUtil.getEHandlerOfActivity(s) instanceof OnAlarm)) {
						log.info("EH-Preprocessing: s is in OnAlarm. r is OnEvent. No Problem here. Starting with OnAlarm first");
						//TODO: Problem hier: OnEvent wird zuerst umgebaut. Checken ob die Logik korrekt in den OnAlarm Scope kopiert wird.
					} else {
						// 2 cases left:
						// OnEvent to OnEvent - or OnEvent to OnAlarm.
					}
					
				}
				ChoreoMergeUtil.getEHandlerOfActivity(r);
				
			}

			// Check if r is an OnEvent of EH
			if (r instanceof OnEvent && !ChoreoMergeUtil.isElementInLoop(r)) {
				log.info("EH-Preprocessing: receive is <OnEvent> in EventHandler");
				onEventReceive.add(link);
				onEventList.add((OnEvent) r);

				// Check if r or s are within an EH
				// Build List of messagelinks within EHs - dont duplicate
			} else {
				// Check if Scope is after OnEvent or OnAlarm within EventHandler
				if (ChoreoMergeUtil.isElementInEHandler(r) || ChoreoMergeUtil.isElementInEHandler(s)) {
					log.info("EH-Preprocessing: receive or send is within EventHandler-Scope");

					// checking OnEvent and OnAlarm cases
					// check r first
					if (ChoreoMergeUtil.isElementInEHandler(r)) {
						if (ChoreoMergeUtil.getEHandlerOfActivity((Activity) r) instanceof OnEvent) {
							// Communication in OnEvent Scope found. Merge
							// OnEvent through copy scope
							log.info("EH-Preprocessing: receive is in EventHandler after OnEvent");
							onEventML.add(link);
						} else {
							// check for repeatEvery-Tag
							// we can't merge OnAlarms with repeatEvery-Tag
							// because it would generate multiple instances
							// lifetime issues of the scope and timing issues
							// are the reason for limitation
							if ((ChoreoMergeUtil.hasRepeatEveryTag((OnAlarm) ChoreoMergeUtil.getFCTEHandlerOfActivity((Activity) r)))) {
								log.info("EH-Preprocessing: OnAlarm has <repeatEvery>-Tag. Can't merge this MessageLink");
							} else {
								// no repeatEvery-Tag found
								if (ChoreoMergeUtil.getEHandlerOfActivity((Activity) r) instanceof OnAlarm) {
									// Communication in OnAlarm Scope found.
									// Merge OnAlarm through wait
									log.info("EH-Preprocessing: receive is in EventHandler after OnAlarm");
									// check if link is already in list (sync comm would cause double elements in onAlarmList
									if (!onAlarmList.contains((OnAlarm) ChoreoMergeUtil.getEHandlerOfActivity((Activity) r))) {
										onAlarmML.add(link);
										onAlarmList.add((OnAlarm) ChoreoMergeUtil.getEHandlerOfActivity((Activity) r));
									}
								}
							}
						}
					}
					
					// check s
					if (ChoreoMergeUtil.isElementInEHandler(s)) {
						if (ChoreoMergeUtil.getEHandlerOfActivity((Activity) s) instanceof OnEvent) {
							// Communication in OnEvent Scope found. Merge
							// OnEvent through copy scope
							log.info("EH-Preprocessing: send is in EventHandler after OnEvent");
							onEventML.add(link);
						} else {
							// check for repeatEvery-Tag
							if ((ChoreoMergeUtil.hasRepeatEveryTag((OnAlarm) s.getContainer()))) {
								log.info("EH-Preprocessing: OnAlarm has <repeatEvery>-Tag. Can't merge this MessageLink");
							} else {
								// no repeatEvery-Tag found
								if (ChoreoMergeUtil.getEHandlerOfActivity((Activity) s) instanceof OnEvent) {
									// Communication in OnAlarm Scope found.
									// Merge OnAlarm through wait
									log.info("EH-Preprocessing: send is in EventHandler after OnAlarm");
									if (!onAlarmList.contains((OnAlarm) ChoreoMergeUtil.getEHandlerOfActivity((Activity) s))) {
										onAlarmML.add(link);
										onAlarmList.add((OnAlarm) ChoreoMergeUtil.getEHandlerOfActivity((Activity) s));
									}
								}
							}
						}
					}
				}
			}
		}
		
		// onEvent List cleanup		
		if (onEventReceive.isEmpty()) {
			// if there are no MessageLinks to OnEvents - no other MessageLinks within OnEvents can be merged
			onEventML.clear();
		}
		
		// cross checking the two lists onEventReceive and onEventML
		// for every OnEvent there must exist a ML in onEventML		
		List<MessageLink> deleteOnEventMLitem = null;
		deleteOnEventMLitem = new ArrayList<>();
		if (onEventML.size() > 1 ) {
			for (MessageLink elink : onEventML) {
				boolean found = false;
				boolean bothinEH = false;
				boolean foundbothEH = false;
				BPELExtensibleElement s = ChoreoMergeUtil.resolveActivity(elink.getSendActivity());
				BPELExtensibleElement r = ChoreoMergeUtil.resolveActivity(elink.getReceiveActivity());
				// check if OnEvent is activated by internal communication
				// if there is no internal receive on the OnEvent it is external
				// activated and the containing ML within the EH cannot be resolved
				for (MessageLink oneventmlink : onEventReceive) {
					BPELExtensibleElement onevtrec = ChoreoMergeUtil.resolveActivity(oneventmlink.getReceiveActivity());
					if ((ChoreoMergeUtil.getEHandlerOfActivity(s) == ChoreoMergeUtil.getEHandlerOfActivity(onevtrec))
							|| (ChoreoMergeUtil.getEHandlerOfActivity(r) == ChoreoMergeUtil.getEHandlerOfActivity(onevtrec))) {
						// Link found - skip other elements
						found = true;
						
					}
					// check here if s and r are both in EH
					if (ChoreoMergeUtil.isElementInEHandler(s) && (ChoreoMergeUtil.getEHandlerOfActivity(s) instanceof OnEvent)) {
						log.info("EH-Preprocessing: s is in EH within OnEvent, r is OnEvent. Checking for initial activation of OnEvent");
						bothinEH = true;
						foundbothEH = checkOE2OE(oneventmlink, pkg);	
					}	
					
				}
				// if found: great, if not: delete link
				if ((!found) || (bothinEH && !foundbothEH)) {
					log.info("EH-Preprocessing: MessageLink is in external activated OnEvent - can't merge this MessageLink: " + elink);
					// mark link for deletetion
					deleteOnEventMLitem.add(elink);
					// removing MLinks here would cause the loop to exit prematurely
					//onEventML.remove(elink);
				}


				
			}
		}
		// delete all found Links in onEventML List
		for (MessageLink dellink : deleteOnEventMLitem) {
			onEventML.remove(dellink);
		}
		
		//FIXME: check here for OnEvents activating other OnEvents

		
		
		
		// onAlarm List doesn't need to be compactified since this is already done in the detection above
		// but we need a list of already modified EH within the onEvent processing for the case that 
		// one EventHandler contains a modified onEvent and onAlarm
		List<EventHandler> modifiedEHList = null;
		modifiedEHList = new ArrayList<>();

		// Check if there are OnEvents to modify
		if (!onEventReceive.isEmpty()) {
			createAlternateOnEvent(onEventML, onEventReceive, pkg, modifiedEHList);
		}
		// Check if there are OnAlarms to modify
		if (!onAlarmML.isEmpty()) {
			createAlternateOnAlarm(onAlarmList, onAlarmML, pkg, modifiedEHList);
		}
		// Check if there are EHs to remove
		garbageCollectionEH(onEventList, onAlarmList);
	}

	// Modifying logic for OnEvents starts here
	/**
	 * Modifies communicating OnEvents
	 * 
	 * @param onEventML
	 * 			List of {@link OnEvent}s to be modified
	 * @param onEventReceive
	 * 			List of {@link OnEvent}s as {@link Receive}-Activity
	 * @param pkg
	 * 			Choreography Package
	 * @param modifiedEHList
	 * 			List of already modified {@link EventHandler}s
	 */
	private static void createAlternateOnEvent(List<MessageLink> onEventML,
			List<MessageLink> onEventReceive, ChoreographyPackage pkg, List<EventHandler> modifiedEHList) {


		for (MessageLink link : onEventReceive) {
			// local vars
			Scope onEventScope = null;
			Scope oldEventHandlerScope = null;
			Scope invokingScope = null;
			Scope newSurScope = null;
			Scope newOnEventScope = null;
			Scope newThrowScope = null;
			Flow newThrowFlow = null;		
			Flow newSurFlow = null;
			Flow mergedflow = null;
			OnEvent oneventobj = null;
			boolean alreadymodified = false;
			boolean scopeAndEHmod = false;

			// Resolve send- and receiveActivity
			BPELExtensibleElement s = ChoreoMergeUtil.resolveActivity(link.getSendActivity());
			BPELExtensibleElement r = ChoreoMergeUtil.resolveActivity(link.getReceiveActivity());

			// Get OnEvent Object
			oneventobj = (OnEvent) r;
			
			// Get OnEvent-Scope with Logic
			onEventScope = (Scope) ((OnEvent) r).getActivity();

			// Get Scope Containing EH
			oldEventHandlerScope = (Scope) r.eContainer().eContainer();

			// Get Scope Invoking EH
			invokingScope = ChoreoMergeUtil.getParentScopeOfActivity((Activity) s);
			
			// Add EH to already modified List
			if (modifiedEHList.contains(oneventobj.eContainer())) {
				alreadymodified = true;
			} else {
				modifiedEHList.add((EventHandler) oneventobj.eContainer());
			}
			
			// Get Container to host new surrounding scope - should be Flow named "MergedFlow"
			mergedflow = ChoreoMergeUtil.getMergedFlow(oldEventHandlerScope);

			// Output for Debug
			log.info("EH-Preprocessing: Alternate OnEvent logic started for Scope: " + oldEventHandlerScope.getName());
			
			if (alreadymodified) {
				log.info("EH-Preprocessing: EH already modified for "+ oldEventHandlerScope.getName() + ". Checking for invoke in " + invokingScope.getName());
				// get all scopes aus mergedflow
				// then look for scope named "EH_NAME_NEW_SUR_SCOPE + invokingScope.getName()" 
				// if found = newSurScope, if not then a new surrounding scope is needed, but EH not modified
				for (Activity mact : mergedflow.getActivities()) {
					// fixed: check for highest Scope of invoke... if modified it should be the Scope with EH_SurScope prefix
					// OLD: if (mact.getName().toString().equals(EH_NAME_NEW_SUR_SCOPE + invokingScope.getName())) {
					// added prefix check since getHighesScopeOfActivity does not always return the modified Scope!
					if (mact.getName().toString().equals(ChoreoMergeUtil.getHighestScopeOfActivity(invokingScope).getName().toString())
							&& (ChoreoMergeUtil.getHighestScopeOfActivity(invokingScope).getName().toString().startsWith(EH_NAME_NEW_SUR_SCOPE)))  {					
						log.info("EH-Preprocessing OnEvent: EH and invoking Scope already modified");
						newSurScope = (Scope) mact;
						scopeAndEHmod = true;
						// link found, break
						break;
					}					
				}			
			} 
			
			if (!alreadymodified || !scopeAndEHmod) {
				// Create Parent-Scope with Variables from OldEventHandlerScope
				// If Parent-Scope of EventHandler has no Vars, get Vars from Highest Process Scope
				Scope scopewithvars = oldEventHandlerScope;
				if (oldEventHandlerScope.getVariables() == null) {
					scopewithvars = ChoreoMergeUtil.getHighestScopeOfActivity((Activity) r.eContainer().eContainer());
				}		
				newSurScope = createNewSurroundingScope(scopewithvars);
				// Set Surrounding Scope Name to invokingScope
				newSurScope.setName(EH_NAME_NEW_SUR_SCOPE + invokingScope.getName());				
			}

			
			if ((!alreadymodified) && (newSurScope.getVariables() != null)){
				List<Variable> vars = null;
				vars = new ArrayList<>();
				vars = newSurScope.getVariables().getChildren();
								
				// uplift vars to <process> in case different processes initiate same EH
				
				// Variable uplift already in preprocessing (in Pmerged), Create Variables element in Pmerged
				if (pkg.getMergedProcess().getVariables() == null) {
						pkg.getMergedProcess().setVariables(newSurScope.getVariables());
						newSurScope.setVariables(null);
				} else {	
					// there are already vars in process - uplift every var additionally to process
					int i = vars.size();
					for (int j = 0; j < i; j++) {
						// since Iterator also modifies List, this simple loop always uplifts first element in list
						ChoreoMergeUtil.upliftVariableToProcessScope(vars.get(0), pkg.getMergedProcess());
					}
				}

			}
			

			if (!alreadymodified || !scopeAndEHmod) {
				// Create new Surrounding Scope with Flow in Scope with attached EventHandler
				newSurFlow = BPELFactory.eINSTANCE.createFlow();
				newSurFlow.setName(EH_NAME_FLOW + invokingScope.getName());
			} else {
				// Flow already created - get object
				newSurFlow = (Flow) newSurScope.getActivity();
			}
			
			// construct new Scope and modified EH structure
			if (!alreadymodified || !scopeAndEHmod) {
				// Add new Surrounding Scope to MergedFlow
				mergedflow.getActivities().add(newSurScope);
	
				// add invoking Scope to Flow
				newSurFlow.getActivities().add(invokingScope);
	
				// add flow to scope
				newSurScope.setActivity(newSurFlow);
			}

			if (!alreadymodified || !scopeAndEHmod) {				
				// create Surrounding Scope for <throw> in EH
				newThrowScope = BPELFactory.eINSTANCE.createScope();
				// this scope is the one in which modified EH-Scopes will be added
				newThrowScope.setName(EH_NAME_EH_SCOPE + "Throw" + "_" + oldEventHandlerScope.getName());
	
				// Create new Flow in Scope with attached Throw
				newThrowFlow = BPELFactory.eINSTANCE.createFlow();
				newThrowFlow.setName(EH_NAME_FLOW + "Throw" + "_" + oldEventHandlerScope.getName());
	
				//newSurFlow.getActivities().add(newThrowScope);
				newThrowScope.setActivity(newThrowFlow);
				
	
				// add EH Copied scope to Flow
				newSurFlow.getActivities().add(newThrowScope);

			} else {
				// find already created newThrowScope
				for (Activity act : newSurFlow.getActivities()) {
					if (act.getName().toString().equals(EH_NAME_EH_SCOPE + "Throw" + "_" + oldEventHandlerScope.getName())) {
						newThrowScope = (Scope) act;
						break;
					}
				}
				// get the scope's acttivity
				newThrowFlow = (Flow) newThrowScope.getActivity();
			}
			
			// add EH-Logic Scope to Flow
			// copying the EH-Logic into a new Scope
			newOnEventScope = BPELFactory.eINSTANCE.createScope();
	
			// create new Receive and set wsu-id of new Receive
			Receive newOnEventReceive = BPELFactory.eINSTANCE.createReceive();
			newOnEventReceive = ChoreoMergeUtil.createReceiveFromOnEvent((OnEvent) r);

			// Add Receive to top of Sequence in EH-Logic Scope
			Sequence seq = BPELFactory.eINSTANCE.createSequence();
			seq.getActivities().add(newOnEventReceive);
			// move the first Activity of Scope to the sequence
			seq.getActivities().add(onEventScope.getActivity());
			// Sequence can be nested and/or duplicated
			newOnEventScope.setActivity(seq);
			
			// add new OnAlarmScope to right position
			newThrowFlow.getActivities().add(newOnEventScope);
			
			
			// Change MessageLink from OnEvent to new Receive activity
			// first get corresponding link from choreo package:
			MessageLink choreolink = null;
			for (MessageLink mlink : pkg.getTopology().getMessageLinks()) {
				if (mlink.equals(link)) {
					choreolink = mlink;
					// Link found - skip other elements
					break;
				}
			}

			// Getting wsu:id from OnEvent
			String wsu = ChoreoMergeUtil.resolveWSU_ID(ChoreoMergeUtil.resolveActivity(choreolink.getReceiveActivity()));
			
			// Naming Scope containing EH-Logic to be executed
			newOnEventScope.setName(EH_NAME_EH_SCOPE + wsu + "_" + oldEventHandlerScope.getName());

			// Output for Debug
			log.info("EH-Preprocessing: Old Receive Activity from MessageLink: " + r);

			// associating WSU ID with new created activity
			pkg.addOld2NewRelation(wsu, newOnEventReceive);

			// Output for Debug
			log.info("EH-Preprocessing: New Receive Activity for MessageLink: " + ChoreoMergeUtil.resolveActivity(choreolink.getReceiveActivity()));

			// adding EH-Lifetime Simulation
			addFTHtoScope(oldEventHandlerScope, newThrowFlow, newThrowScope, invokingScope.getName(), alreadymodified);


		}

	}

	// Modifying logic for OnAlarms starts here
	/**
	 * Modifies communicating {@link OnAlarms}
	 * 
	 * @param onAlarmList
	 * 			List of {@link OnAlarm}s to be modified
	 * @param onAlarmMl
	 * 			List of {@link MessageLinks} in {@link OnAlarms}
	 * @param pkg
	 * 			Choreography Package
	 * @param modifiedEHList
	 * 			List of already modified {@link EventHandler}s
	 */
	private static void createAlternateOnAlarm(List<OnAlarm> onAlarmList,
			List<MessageLink> onAlarmMl, ChoreographyPackage pkg, List<EventHandler> modifiedEHList) {

		// Note: repeating onAlarms are not allowed. repeatEvery tag is not allowed since we cant create
		// multiple instances within the lifetime of the EH-calling scope
		for (MessageLink link : onAlarmMl) {
			// local vars
			Scope onAlarmScope = null;
			Scope oldEventHandlerScope = null;
			Scope newSurScope = null;
			Scope newOnAlarmScope = null;
			Scope newThrowScope = null;
			Scope invokingScope = null;
			Flow mergedflow = null;
			Flow newThrowFlow = null;		
			Flow newSurFlow = null;
			boolean alreadymodified = false;
			boolean scopeAndEHmod = false;
			boolean bothinOnAlarm = false;
			BPELExtensibleElement communicator = null;
			BPELExtensibleElement ehcommunicator = null;

			BPELExtensibleElement s = ChoreoMergeUtil.resolveActivity(link.getSendActivity());
			BPELExtensibleElement r = ChoreoMergeUtil.resolveActivity(link.getReceiveActivity());
			
			// get communicating element in EH separately
			if ((ChoreoMergeUtil.getEHandlerOfActivity(r) instanceof OnAlarm)) {
				// r is in EH
				ehcommunicator = r;
				communicator = s;
			} else if ((ChoreoMergeUtil.getEHandlerOfActivity(s) instanceof OnAlarm)) {
				// s is in EH
				ehcommunicator = s;
				communicator = r;				
			} else if ((ChoreoMergeUtil.isElementInEHandler(r)) && (ChoreoMergeUtil.isElementInEHandler(s))) {
				log.info("EH-Preprocessing: Both messagelink-Ends are in EH");
				if ((ChoreoMergeUtil.getEHandlerOfActivity(r) instanceof OnAlarm) && 
						(ChoreoMergeUtil.getEHandlerOfActivity(s) instanceof OnAlarm)) {
					log.info("EH-Preprocessing: s and r are in OnAlarms. No Problem here. Starting with modifying EH-scope of s");
					// add link again to list for r
					bothinOnAlarm = true;
				} 
			}
			

			// Get Scope communication with EH
			invokingScope = ChoreoMergeUtil.getParentScopeOfActivity((Activity) communicator);

			// Get OnEvent-Scope with Logic
			OnAlarm onalarmobj = (OnAlarm) ChoreoMergeUtil.getEHandlerOfActivity(ehcommunicator);
	
			// Add EH to already modified List
			if (modifiedEHList.contains(onalarmobj.eContainer())) {
				alreadymodified = true;
			} else {
				modifiedEHList.add((EventHandler) onalarmobj.eContainer());
			}
			
			// get the EH-Logic Scope with the execution logic after OnAlarm
			onAlarmScope = (Scope) (onalarmobj.getActivity());

			// Get Scope Containing EH
			oldEventHandlerScope = (Scope) onalarmobj.eContainer().eContainer();

			// Get Container to host new surrounding scope
			// should be Flow named "MergedFlow" per definition
			mergedflow = ChoreoMergeUtil.getMergedFlow(oldEventHandlerScope);

			// Output for Debug
			log.info("EH-Preprocessing: Alternate OnAlarm logic started for Scope: "+ oldEventHandlerScope.getName());

			if (alreadymodified) {
				log.info("EH-Preprocessing: EH already modified for "+ oldEventHandlerScope.getName() + ". Checking for invoke in " + invokingScope.getName());
				// idee: hole alle scopes aus mergedflow
				// suche dann nach "EH_NAME_NEW_SUR_SCOPE + invokingScope.getName()" 
				// wenn ja, dann ist das der newSurScope, wenn nein dann ist der invoking scope neu, aber EH nicht
				
				for (Activity mact : mergedflow.getActivities()) {
					// fixed: check for highest Scope of invoke... if modified it should be the Scope with EH_SurScope prefix
					// OLD: if (mact.getName().toString().equals(EH_NAME_NEW_SUR_SCOPE + invokingScope.getName())) {
					// added prefix check since getHighesScopeOfActivity does not always return the modified Scope!
					if (mact.getName().toString().equals(ChoreoMergeUtil.getHighestScopeOfActivity(invokingScope).getName().toString())
							&& (ChoreoMergeUtil.getHighestScopeOfActivity(invokingScope).getName().toString().startsWith(EH_NAME_NEW_SUR_SCOPE)))  {	
						log.info("EH-Preprocessing: EH and invoking Scope already modified");
						newSurScope = (Scope) mact;
						scopeAndEHmod = true;
						// link found, break
						break;
					}					
				}			
			} 
			
			if (!alreadymodified || !scopeAndEHmod) {
			// Create Parent-Scope with Variables from OldEventHandlerScope
			newSurScope = createNewSurroundingScope(oldEventHandlerScope);
			
			// Set Surrounding Scope Name to invokingScope
			newSurScope.setName(EH_NAME_NEW_SUR_SCOPE + invokingScope.getName());
			}
			
			if (!alreadymodified) {
				List<Variable> vars = null;
				vars = new ArrayList<>();
				vars = newSurScope.getVariables().getChildren();
								
				// uplift vars to <process> in case different processes initiate same EH
				
				// Variablen uplift already in preprocessing (in Pmerged), Create Variables element in Pmerged
				if (pkg.getMergedProcess().getVariables() == null) {
						pkg.getMergedProcess().setVariables(newSurScope.getVariables());
						newSurScope.setVariables(null);
				} else {	
					// there are already vars in process - uplift every var additionally to process
					int i = vars.size();
					for (int j = 0; j < i; j++) {
						// since Iterator also modifies List, this simple loop always uplifts first element in list
						ChoreoMergeUtil.upliftVariableToProcessScope(vars.get(0), pkg.getMergedProcess());
					}
				}

			}
			

			if (!alreadymodified || !scopeAndEHmod) {
				// Create new Surrounding Scope with Flow in Scope with attached EventHandler
				newSurFlow = BPELFactory.eINSTANCE.createFlow();
				newSurFlow.setName(EH_NAME_FLOW + invokingScope.getName());
			} else {
				// Flow already created - get object
				newSurFlow = (Flow) newSurScope.getActivity();
			}
				
			// construct new Scope and modified EH structure
			if (!alreadymodified || !scopeAndEHmod) {
				// invokeContainer muss FLow sein (pmerged baut ja Flows
				// drumherum...
				mergedflow.getActivities().add(newSurScope);
	
				// add invoking Scope to Flow
				newSurFlow.getActivities().add(invokingScope);
	
				// add flow to scope
				newSurScope.setActivity(newSurFlow);
			}


			// add EH-Logic Scope to Flow
			// copying the EH-Logic into a new Scope
			newOnAlarmScope = BPELFactory.eINSTANCE.createScope();
			
			// setting a (unique) name for the EH-Logic Scope
			if (onAlarmScope.getName() == null) {				
				newOnAlarmScope.setName(EH_NAME_EH_LOGIC_SCOPE + onAlarmMl.indexOf(link) + "_EH" + oldEventHandlerScope.getName());
			} else {
			newOnAlarmScope.setName(EH_NAME_EH_LOGIC_SCOPE + onAlarmScope.getName() + "_EH" + oldEventHandlerScope.getName());
			}
	
			if (!alreadymodified || !scopeAndEHmod) {				
				// create Surrounding Scope for <throw> in EH
				newThrowScope = BPELFactory.eINSTANCE.createScope();
				// this scope is the one in which modified EH-Scopes will be added
				newThrowScope.setName(EH_NAME_EH_SCOPE + "Throw" + "_" + oldEventHandlerScope.getName());
	
				// Create new Flow in Scope with attached Throw
				newThrowFlow = BPELFactory.eINSTANCE.createFlow();
				newThrowFlow.setName(EH_NAME_FLOW + "Throw" + "_" + oldEventHandlerScope.getName());
	
				//newSurFlow.getActivities().add(newThrowScope);
				newThrowScope.setActivity(newThrowFlow);
	
				// add EH Copied scope to Flow
				newSurFlow.getActivities().add(newThrowScope);

			} else {
				// find already created newThrowScope
				for (Activity act : newSurFlow.getActivities()) {
					if (act.getName().toString().equals(EH_NAME_EH_SCOPE + "Throw" + "_" + oldEventHandlerScope.getName())) {
						newThrowScope = (Scope) act;
						break;
					}
				}
				// get the scope's activity
				newThrowFlow = (Flow) newThrowScope.getActivity();
			}
			
			// add new OnAlarmScope to right position
			newThrowFlow.getActivities().add(newOnAlarmScope);
			
			// create new <wait>
			Wait alternateOnAlarmWait = BPELFactory.eINSTANCE.createWait();
			alternateOnAlarmWait = ChoreoMergeUtil.createWaitFromOnAlarm(onalarmobj);

			// Add wait to top of Sequence in EH-Logic Scope
			Sequence seq = BPELFactory.eINSTANCE.createSequence();
			seq.getActivities().add(alternateOnAlarmWait);
			// move Activity from OnAlarm-Scope to new scope
			seq.getActivities().add(onAlarmScope.getActivity());
			// Info: Sequence can be nested / duplicated (Also following Flow)
			newOnAlarmScope.setActivity(seq);

			// adding EH-Lifetime Simulation
			if (!alreadymodified || !scopeAndEHmod) {	
				addFTHtoScope(oldEventHandlerScope, newThrowFlow, newThrowScope, invokingScope.getName(), alreadymodified);
			}
			
			// if s and r were in OnAlarm this var is true and link needs to be modified again
			if (bothinOnAlarm) {
				List<MessageLink> bothonAlarmMl = new ArrayList<>();
				bothonAlarmMl.add(link);
				createAlternateOnAlarm(onAlarmList, bothonAlarmMl, pkg, modifiedEHList);
			}
		}

	}

	/**
	 * Adds the simulated {@link FaultHandler} and {@link TerminationHandler} to
	 * the {@link Scope} that contained the original {@link EventHandler}. This
	 * simulates the Lifetime of the {@link EventHandler}-{@link Scope}
	 * 
	 * @param oldEventHandlerScope
	 *            the Scope the EH was attatched to originally
	 * @param newThrowFlow
	 *            created Flow for adding Throw in simulated EH
	 * @param newThrowScope
	 *            created Scope for hosting Flow
	 * @param name
	 * 			  Name of InvokingScope for Link-naming purposes
	 * @param alreadymodified
	 * 			  info whether EH was already modified and FH/TH already added	
	 * 
	 * @return
	 */
	private static void addFTHtoScope(Scope oldEventHandlerScope,
			Flow newThrowFlow, Scope newThrowScope, String name, Boolean alreadymodified) {

		// throw and Scope lifetime for Simulated EH
		Throw ehThrow = BPELFactory.eINSTANCE.createThrow();
		ehThrow = ChoreoMergeUtil.createThrowForEHFault();
		ehThrow.setName("ThrowSimEH" + name);
		// add Throw to Flow in simulated EH
		newThrowFlow.getActivities().add(ehThrow);

		// Create Empties for linking in FH and TH
		Empty emptyinFH = BPELFactory.eINSTANCE.createEmpty();
		emptyinFH.setName("CallThrowSimEHfromFH");
		Empty emptyinTH = BPELFactory.eINSTANCE.createEmpty();
		emptyinTH.setName("CallThrowSimEHfromTH");

		// Setting Empty in FH and TH
		Catch catchEH = BPELFactory.eINSTANCE.createCatch();
		// add emtpy to created catch
		ChoreoMergeUtil.setActivityForFCTEHandler(catchEH, emptyinFH);

		// adding FaultHandler to Scope containing the original EH
		if (oldEventHandlerScope.getFaultHandlers() == null) {
			oldEventHandlerScope.setFaultHandlers(BPELFactory.eINSTANCE.createFaultHandler());
			oldEventHandlerScope.getFaultHandlers().setCatchAll(ChoreoMergeUtil.createNPCatchAll());
			oldEventHandlerScope.getFaultHandlers().getCatchAll().setActivity(emptyinFH);

		} else {
			// Scope has already FH
			// Check if emptyinFH was already added
			if (!alreadymodified) {				
				// create a sequence with activity from CatchAll and add Empty on first place
				Sequence seqFH = BPELFactory.eINSTANCE.createSequence();
				seqFH.getActivities().add(emptyinFH);
				seqFH.getActivities().add(oldEventHandlerScope.getFaultHandlers().getCatchAll().getActivity());
				oldEventHandlerScope.getFaultHandlers().getCatchAll().setActivity(seqFH);
			} else {
				// alreadymodified means that sequence was added with Empty in first position, since there is always a CH added
				emptyinFH = (Empty) ((Sequence) oldEventHandlerScope.getFaultHandlers().getCatchAll().getActivity()).getActivities().get(0);

			}
		}

		// adding TerminationHandler to Scope containing the original EH
		if (oldEventHandlerScope.getTerminationHandler() == null) {
			oldEventHandlerScope.setTerminationHandler(BPELFactory.eINSTANCE.createTerminationHandler());
			oldEventHandlerScope.getTerminationHandler().setActivity(emptyinTH);
		} else {
			// scope has already TH
			// Check if emptyinFH was already added
			if (!alreadymodified) {		
				// create a sequence with activity from TH and add Empty on first place
				Sequence seqTH = BPELFactory.eINSTANCE.createSequence();
				seqTH.getActivities().add(emptyinTH);
				seqTH.getActivities().add(oldEventHandlerScope.getTerminationHandler().getActivity());
				oldEventHandlerScope.getTerminationHandler().setActivity(seqTH);
			} else {
				// alreadymodified means that sequence was added with Empty in first position
				if ((oldEventHandlerScope.getTerminationHandler().getActivity()) instanceof Sequence) {
					emptyinTH = (Empty) ((Sequence) oldEventHandlerScope.getTerminationHandler().getActivity()).getActivities().get(0);
				} else {
					emptyinTH = (Empty) oldEventHandlerScope.getTerminationHandler().getActivity();
				}
				

			}
		}

		// Adding links to Throw in Simulated EH Scope
		ChoreoMergeUtil.addLinkToThrowEH(ehThrow, emptyinFH);
		ChoreoMergeUtil.addLinkToThrowEH(ehThrow, emptyinTH);

		// add specific (lifetimeEHFailure) Catchall to EH-Scope to catch throw
		newThrowScope.setFaultHandlers(BPELFactory.eINSTANCE.createFaultHandler());
		// special catch for FH
		Catch catchEHthrow = BPELFactory.eINSTANCE.createCatch();
		Empty emptyEHthrow = BPELFactory.eINSTANCE.createEmpty();
		catchEHthrow.setActivity(emptyEHthrow);
		catchEHthrow.getActivity().setName("defindedEHcatch");
		QName ehFault = new QName("", "lifetimeEHFailure", "");
		catchEHthrow.setFaultName(ehFault);
		// add catch to FaultHandler
		ChoreoMergeUtil.addCatchToBPELExtensibleElement(catchEHthrow, newThrowScope);
	}

	/**
	 * Creates a new surrounding {@link Scope} that is needed to access
	 * {@link Variable}s from the new {@link EventHandler}-{@link Scope}s
	 * 
	 * @param scope
	 *            contains Variables that should be lifted up
	 * 
	 * @return
	 */
	private static Scope createNewSurroundingScope(Scope scope) {
		Scope newSurScope = BPELFactory.eINSTANCE.createScope();
		newSurScope.setName(createSurroundingScopeName(scope.getName()));
		// copy Variables, we need it to use them in the new scopes for every EH case
		newSurScope.setVariables(scope.getVariables());
		// we dont need them anymore in the processScope
		scope.setVariables(null);
		return newSurScope;
	}
	
	/**
	 * Checks for empty {@link EventHandler}.
	 * 
	 * @param 
	 *         List of modified OnEvents
	 * @param 
	 * 		   List of modified OnAlarms
	 * @return
	 */
	private static void garbageCollectionEH(List<OnEvent> onEventList, List<OnAlarm> onAlarmList) {

		// first check OnEvents
		if (!onEventList.isEmpty()) {
		for (OnEvent oe : onEventList) {
			// getting OnEvent Activity (must be Scope)
			Scope oeScope = (Scope) oe.getActivity();
			Scope removeEH = (Scope) oeScope.eContainer().eContainer().eContainer();
			if (oeScope.getActivity() == null) {
				// empty Scope found - OnEvent should be removed
				removeEH.getEventHandlers().getEvents().remove(oe);
			}
			// now checking Old Event Handler Scope if EH is empty
			if ((removeEH.getEventHandlers().getEvents().isEmpty())
					&& (removeEH.getEventHandlers().getAlarm().isEmpty())) {
				removeEH.setEventHandlers(null);
			}
		}
		}
		
		// checking if original EH is empty and can be removed
		if (!onAlarmList.isEmpty()) {
					for (OnAlarm oa : onAlarmList) {
						// getting OnEvent Activity (must be Scope)
						Scope oeScope = (Scope) oa.getActivity();
						Scope removeEH = (Scope) oeScope.eContainer().eContainer().eContainer();
						if (oeScope.getActivity() == null) {
							// empty Scope found - OnEvent should be removed
							removeEH.getEventHandlers().getAlarm().remove(oa);							
						}
						// now checking Old Event Handler Scope if EH is empty
						if ((removeEH.getEventHandlers().getEvents().isEmpty())
								&& (removeEH.getEventHandlers().getAlarm().isEmpty())) {
							removeEH.setEventHandlers(null);
						}
					}
		}
		

	}
	
	
	/**
	 * Checks if OE activating OE is finally activated trough non-EH
	 * 
	 * @param mlink 
	 * @param pkg
	 *            
	 * @return true if last OnEvent ist initiated from non-EventHandler
	 */
	public static boolean checkOE2OE(MessageLink mlink, ChoreographyPackage pkg) {
		
		BPELExtensibleElement s = ChoreoMergeUtil.resolveActivity(mlink.getSendActivity());
		
		boolean found = false;
		
		
		if (ChoreoMergeUtil.isElementInEHandler(s)) {
			log.info("EH-Preprocessing: invoke calling OnEvent is within EventHandler");
			// caller needs to be another OnEvent (we got no problems with OnAlarm)
			if ((ChoreoMergeUtil.getEHandlerOfActivity(s) instanceof OnEvent)) {
				// Search for the new MessageLink with the OnEvent and check it
			for (MessageLink link : pkg.getTopology().getMessageLinks()) {
				BPELExtensibleElement r = ChoreoMergeUtil.resolveActivity(link.getReceiveActivity());
				if (ChoreoMergeUtil.getEHandlerOfActivity(s) == ChoreoMergeUtil.getEHandlerOfActivity(r)) {
					// rekursiver aufruf
					checkOE2OE(link, pkg);
				}
				
			}
			}
			
		} else {
			// s is not in EH - so true
			found = true;
		}
		
		return found;
	}


	/**
	 * Creates a name for the surrounding {@link Scope}.
	 * 
	 * @param currentScopeName
	 *            which will be used as postfix
	 * @return
	 */
	private static String createSurroundingScopeName(String currentScopeName) {
		return EH_NAME_NEW_SUR_SCOPE + "_" + currentScopeName;
	}

}
