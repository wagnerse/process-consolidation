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

	public static void startPreProcessing(
		ChoreographyPackage choreographyPackage) {
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
	 * @param choreographyPackage
	 *            contains the {@link Process}es with {@link EventHandler}s
	 */
	private static void startPreprocessingForEventHandler(ChoreographyPackage pkg) {

		// Reihenfolge:
		// Message Links durchsuchen
		// gefundene Links in Liste sortieren und fall herausfinden
		// dann Umbauen

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

			// Check if r is an OnEvent of EH
			if (r instanceof OnEvent && !ChoreoMergeUtil.isElementInLoop(r)) {
				log.info("EH-Preprocessing: receive is <OnEvent> in EventHandler");
				onEventReceive.add(link);
				onEventList.add((OnEvent) r);

				// Check if r or s are within an EH
				// Build List of messagelinks within EHs - dont duplicate
			} else {
				// Check if Scope is after OnEvent or OnAlarm within
				// EventHandler
				if (ChoreoMergeUtil.isElementInEHandler(r) || ChoreoMergeUtil.isElementInEHandler(s)) {
					log.info("EH-Preprocessing: receive or send is within EventHandler-Scope");

					// checking OnEvent and OnAlarm cases

					// neu: 12.03.15
					// liste von ML in EH bei OnEvent aufbauen, checken ob ein
					// anderer link den EH schon markiert hat
					// am ende gegenchecken ob das OnEvent intern aufgerufen
					// wird, wenn nicht - NMML
					// separate Liste für OnAlarm, repeatEvery Tag hier abprüfen

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
		if (!onEventML.isEmpty()) {
			for (MessageLink elink : onEventML) {
				boolean found = false;
				BPELExtensibleElement s = ChoreoMergeUtil.resolveActivity(elink.getSendActivity());
				BPELExtensibleElement r = ChoreoMergeUtil.resolveActivity(elink.getReceiveActivity());
				// check if OnEvent is activated by internal communication
				// if there is no internal receive on the OnEvent it is external
				// activated and the containing ML within
				// the EH cannot be resolved
				for (MessageLink oneventmlink : onEventReceive) {
					BPELExtensibleElement onevtrec = ChoreoMergeUtil.resolveActivity(oneventmlink.getReceiveActivity());
					if ((ChoreoMergeUtil.getEHandlerOfActivity(s) == ChoreoMergeUtil.getEHandlerOfActivity(onevtrec))
							|| (ChoreoMergeUtil.getEHandlerOfActivity(r) == ChoreoMergeUtil.getEHandlerOfActivity(onevtrec))) {
						// Link found - skip other elements
						found = true;
						break;
					}
					
				}
				// if found: great, if not: delete link
				if (!found) {
					onEventML.remove(elink);
				}
				
			}
		}
		// onAlarm List doesn't need to be compactified since this is already done in the detection above

		// Check if there are OnEvents to modify
		if (!onEventReceive.isEmpty()) {
			createAlternateOnEvent(onEventML, onEventReceive, pkg);
		}
		// Check if there are OnAlarms to modify
		if (!onAlarmML.isEmpty()) {
			createAlternateOnAlarm(onAlarmList, onAlarmML, pkg);
		}
		// Check if there are EHs to remove
		garbageCollectionEH(onEventList, onAlarmList);
	}

	// Modifying logic for OnEvents starts here
	private static void createAlternateOnEvent(List<MessageLink> onEventML,
			List<MessageLink> onEventReceive, ChoreographyPackage pkg) {

		Scope onEventScope = null;
		Scope oldEventHandlerScope = null;
		Scope invokingScope = null;
		Flow mergedflow = null;

		// Resolve send- and receiveActivity
		// TODO: Schleife zum durchlaufen aller links aus Übergabeparameter
		for (MessageLink link : onEventReceive) {
			BPELExtensibleElement s = ChoreoMergeUtil.resolveActivity(link.getSendActivity());
			BPELExtensibleElement r = ChoreoMergeUtil.resolveActivity(link.getReceiveActivity());

			// Get OnEvent-Scope with Logic
			onEventScope = (Scope) ((OnEvent) r).getActivity();

			// Get Scope Containing EH
			oldEventHandlerScope = (Scope) r.eContainer().eContainer();

			// Get Scope Invoking EH
			invokingScope = ChoreoMergeUtil.getParentScopeOfActivity((Activity) s);

			
			// FIXME: copy alreadymodified logic from OnAlarm
			
			// Get Container to host new surrounding scope
			// should be Flow named "MergedFlow"
			mergedflow = ChoreoMergeUtil.getMergedFlow(oldEventHandlerScope);

			// Output for Debug
			log.info("EH-Preprocessing: Alternate OnEvent logic started for Scope: " + oldEventHandlerScope.getName());

			// TODO: immer checken ob der neue EH-Scope schon erstellt wurde
			// Dann checken ob der aktuelle EH der gleiche ist -> copy in den
			// Scope, wenn nicht -> neuen scope
			// Bedeutet aber alle Scopes mit dem Präfix EH_ durchgehen (Naming)
			// wichtig für OnAlarm, da der ja danach umgebaut wird

			// Create Parent-Scope with Variables from OldEventHandlerScope
			Scope newSurScope = createNewSurroundingScope(oldEventHandlerScope);

			// Create new Surrounding Scope with Flow in Scope with attached EventHandler
			Flow newSurFlow = BPELFactory.eINSTANCE.createFlow();
			newSurFlow.setName(EH_NAME_FLOW + invokingScope.getName());

			// Set Surrounding Scope Name to invokingScope
			newSurScope.setName(EH_NAME_NEW_SUR_SCOPE + invokingScope.getName());

			// invokeContainer muss FLow sein (pmerged baut ja Flows
			// drumherum...
			mergedflow.getActivities().add(newSurScope);

			// add invoking Scope to Flow
			newSurFlow.getActivities().add(invokingScope);

			// add new Flow to scope
			newSurScope.setActivity(newSurFlow);

			// add EH-Logic Scope to Flow
			// copying the EH-Logic into a new Scope
			Scope newOnEventScope = BPELFactory.eINSTANCE.createScope();
			


			// create Surrounding Scope for <throw> in EH
			Scope newThrowScope = BPELFactory.eINSTANCE.createScope();
			// TODO: Der Scope in den weitere OnEvents reinkommen würden!!!!!!!
			newThrowScope.setName(EH_NAME_EH_SCOPE + "Throw" + "_" + oldEventHandlerScope.getName());

			// Create new Flow in Scope with attached Throw
			Flow newThrowFlow = BPELFactory.eINSTANCE.createFlow();
			newThrowFlow.setName(EH_NAME_FLOW + "Throw" + "_" + oldEventHandlerScope.getName());

			newSurFlow.getActivities().add(newThrowScope);
			newThrowScope.setActivity(newThrowFlow);

			newThrowFlow.getActivities().add(newOnEventScope);

			// add EH Copied scope to Flow
			newSurFlow.getActivities().add(newThrowScope);

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
			log.info("EH-Preprocessing: Old Receive Activity from MessageLink" + r);

			// associating WSU ID with new created activity
			pkg.addOld2NewRelation(wsu, newOnEventReceive);

			// Output for Debug
			log.info("EH-Preprocessing: New Receive Activity for MessageLink" + ChoreoMergeUtil.resolveActivity(choreolink.getReceiveActivity()));

			// adding EH-Lifetime Simulation
			addFTHtoScope(oldEventHandlerScope, newThrowFlow, newThrowScope, invokingScope.getName(), true);
		


			// FIXME: testfall: <process> hat selbst EH

			// FIXME: utils zum benamen von scopes die keinen namen haben.


			// copy EH-scope under flow (P-logic and EH-logic should be parallel
			// First Activity in EH-Scope is Receive (ID Copy from OnEvent - add
			// Prefix to OnEvent)
			// Remove Link from EH and move to
			// add FH and TH to new OnEventScope with catchall
			// add Specific Throw to old Scope within FH and TH
			// Set Links from FTH (oldEHScope) to FTH in new OnEventScope

			// ChoreoMergeUtil.addLinkToThrowEH(EHthrow, actsource);

			// postprocessing OnEvent (Eventuell erst in cleanup, da man die ja
			// zum
			// checken braucht):
			// IF original EH-Scope has no send,receive,invoke: stehen lassen,
			// ELSE:
			// remove OnEvent from EH
			// Hilfe: Schauen ob scope ein oder ausgehende MLs hat - gibts schon
			// im
			// FCTE Util
			// Check if original EH is Empty - if yes: remove

			// Check for multiple ONEvents! -> IF proc scope is already in flow
			// and
			// ... another EH was built
			// MessageLinks "umhängen" sowohl im scope als auch zum OnAlarm

			// falls liste weitere elemente hat
			// TODO hier umbauen in bereits bestehenden scope, FALLS die im
			// gleichen
			// Container sind

		}

	}


	private static void createAlternateOnAlarm(List<OnAlarm> onAlarmList,
			List<MessageLink> onAlarmMl, ChoreographyPackage pkg) {

		// on Alarm wiederholens (bspw. alle 3 minuten eine neue Instanz geht
		// nicht).
		// for: zeitdauer - nach xmin until: angabe genauer zeitpunkt
		// repeat every for OnAlarm: at most once als ausführung -> wir können
		// wait benutzen
		// wir vebieten OnAlarms, bzw. können nicht gemergt werden, wenn OnAlarm
		// ML zu anderen Processen enthält und repeatEvery tag hat.

		
		List<EventHandler> modifiedEHList = null;
		modifiedEHList = new ArrayList<>();


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

			BPELExtensibleElement s = ChoreoMergeUtil.resolveActivity(link.getSendActivity());
			BPELExtensibleElement r = ChoreoMergeUtil.resolveActivity(link.getReceiveActivity());
			

			// Get Scope Invoking EH
			invokingScope = ChoreoMergeUtil.getParentScopeOfActivity((Activity) s);

			// Get OnEvent-Scope with Logic
			OnAlarm onalarmobj = (OnAlarm) ChoreoMergeUtil.getEHandlerOfActivity(r);
			
			// EH objekt hier in Liste rein, falls noch nicht drin. Falls drin -> Scopes und so schon gebaut. Anders ran holen!
				if (modifiedEHList.contains(onalarmobj.eContainer())) {
					alreadymodified = true;
				} else {
					modifiedEHList.add((EventHandler) onalarmobj.eContainer());
				}
			

			onAlarmScope = (Scope) (onalarmobj.getActivity());
			
			// FIXME: check if scope has structured activities. If not -> single receive is in it. Build sequence to avoid AsyncMatcher12
			onAlarmScope.getActivity() ;

			// Get Scope Containing EH
			oldEventHandlerScope = (Scope) onalarmobj.eContainer().eContainer();

			// Get Container to host new surrounding scope
			// should be Flow named "MergedFlow"
			mergedflow = ChoreoMergeUtil.getMergedFlow(oldEventHandlerScope);

			// Output for Debug
			log.info("EH-Preprocessing: Alternate OnAlarm logic started for Scope: "+ oldEventHandlerScope.getName());

			if (alreadymodified) {
				log.info("EH-Preprocessing: EH already modified for Scope: "+ oldEventHandlerScope.getName() + ". Checking for invoking Scope:" + invokingScope.getName());
				// idee: hole alle scopes aus mergedflow
				// suche dann nach "EH_NAME_NEW_SUR_SCOPE + invokingScope.getName()" 
				// wenn ja, dann ist das der newSurScope, wenn nein dann ist der invoking scope neu, aber EH nicht
				
				for (Activity mact : mergedflow.getActivities()) {
					if (mact.getName().toString().equals(EH_NAME_NEW_SUR_SCOPE + invokingScope.getName())) {
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
			
			// FIXME: Uplift vars here to <process>?
			if (!alreadymodified) {
				
				


				List<Variable> vars = null;
				vars = new ArrayList<>();
				vars = newSurScope.getVariables().getChildren();
				Iterator<Variable> iter = vars.iterator();
				
				
				// uplift vars to <process> in case different processes initiate same EH
				// TODO: Variablen uplift already in preprocessing (in Pmerged), Create Variables element in Pmerged
//				if (pkg.getMergedProcess().getVariables() == null) {
////					pkg.getMergedProcess().setVariables(newSurScope.getVariables());
////					newSurScope.setVariables(null);
//				} else {
				
				int i = vars.size();
				for (int j = 0; j < i; j++) {
					ChoreoMergeUtil.upliftVariableToProcessScope(vars.get(0), pkg.getMergedProcess());
				}
//				while (iter.hasNext()) {
//					ChoreoMergeUtil.upliftVariableToProcessScope(iter.next(), pkg.getMergedProcess());
//					iter.remove();
//				}

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
				// TODO: Der Scope in den weitere OnEvents reinkommen würden!!!!!!!
				newThrowScope.setName(EH_NAME_EH_SCOPE + "Throw" + "_"
						+ oldEventHandlerScope.getName());
	
				// Create new Flow in Scope with attached Throw
				newThrowFlow = BPELFactory.eINSTANCE.createFlow();
				newThrowFlow.setName(EH_NAME_FLOW + "Throw" + "_"
						+ oldEventHandlerScope.getName());
	
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
			
			// add new OnAlarmScope to right position
			newThrowFlow.getActivities().add(newOnAlarmScope);
			
			// create new <wait>
			Wait alternateOnAlarmWait = BPELFactory.eINSTANCE.createWait();
			alternateOnAlarmWait = ChoreoMergeUtil.createWaitFromOnAlarm(onalarmobj);

			// Add wait to top of Sequence in EH-Logic Scope
			Sequence seq = BPELFactory.eINSTANCE.createSequence();
			seq.getActivities().add(alternateOnAlarmWait);
			// TODO: Activity Kopieren!
			seq.getActivities().add(onAlarmScope.getActivity());
			// TODO: Sequence kann verschachtelt/doppelt sein (Auch folgendes
			// Flow)
			newOnAlarmScope.setActivity(seq);

			// adding EH-Lifetime Simulation
			if (!alreadymodified || !scopeAndEHmod) {	
				addFTHtoScope(oldEventHandlerScope, newThrowFlow, newThrowScope, invokingScope.getName(), alreadymodified);
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
		// TODO: Empty zum FH hinzufügen
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
				// FIXME: find corresponding Empty from FH
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
				// FIXME: find corresponding Empty from TH
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
		// FIXME: or just uplift the single VAR from message link?
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
				// FIXME: wie bei onalarm umbauen
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
