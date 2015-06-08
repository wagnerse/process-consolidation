package org.bpel4chor.mergechoreography.util.model;

import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.FaultHandler;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.Source;
import org.eclipse.bpel.model.Target;
import org.eclipse.bpel.model.TerminationHandler;

/**
 * Data Class to tell which handler should be processed.<br>
 * <br>
 * Copyright 2013 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter Berger
 * @author Aleksandar Milutinovic
 * 
 */
public class ScopeHandlerBean {
	
	private boolean terminationHandler = false;
	private boolean faultHandler = false;
	private boolean eventHandler = false;
	private boolean EHOnEvent = false;
	private boolean EHOnAlarm = false;
	private boolean pbdScope = false;
	
	
	/**
	 * It is a flag which tells if this {@link Scope} have an associated PBD.
	 * 
	 * @return the pbdScope
	 */
	public boolean isPbdScope() {
		return pbdScope;
	}
	
	/**
	 * @param pbdScope the pbdScope to set
	 */
	public void setPbdScope(boolean pbdScope) {
		this.pbdScope = pbdScope;
	}
	
	/**
	 * It is a flag which tells if any {@link Activity} in the
	 * {@link TerminationHandler} has a {@link Target} and this {@link Target}
	 * is connected with a {@link Source} outside of the
	 * {@link TerminationHandler}.
	 * 
	 * @return true if {@link TerminationHandler} contains {@link Target} that
	 *         is connected to a {@link Source} outside of the
	 *         {@link TerminationHandler}<br>
	 *         otherwise false
	 */
	public boolean isTerminationHandler() {
		return terminationHandler;
	}
	
	/**
	 * @param terminationHandler the terminationHandler to set
	 */
	public void setTerminationHandler(boolean terminationHandler) {
		this.terminationHandler = terminationHandler;
	}
	
	/**
	 * It is a flag which tells if any {@link Activity} in the
	 * {@link FaultHandler} has a {@link Target} and this {@link Target} is
	 * connected with a {@link Source} outside of the {@link FaultHandler}.
	 * 
	 * @return true if {@link FaultHandler} contains {@link Target} that is
	 *         connected to a {@link Source} outside of the {@link FaultHandler}<br>
	 *         otherwise false
	 */
	public boolean isFaultHandler() {
		return faultHandler;
	}
	
	/**
	 * @param faultHandler the faultHandler to set
	 */
	public void setFaultHandler(boolean faultHandler) {
		this.faultHandler = faultHandler;
	}
	
	/**
	 * @return true if {@link Scope} should be processed<br>
	 *         otherwise false
	 */
	public boolean processScope() {
		return isFaultHandler() || isTerminationHandler() || isEventHandler();
	}

	/**
	 * It is a flag which tells if any {@link Activity} in the
	 * {@link EventHandler} has a {@link Target} and this {@link Target} is
	 * connected with a {@link Source} outside of the {@link EventHandler}
	 * or vice versa has a {@link Source} which is connected with a 
	 * {@link Target} outside of the {@link EventHandler}.
	 * 
	 * @return true if {@link EventHandler} contains a {@link Target} or
	 *         {@link Source} which is connected outside of the {@link EventHandler}<br>
	 *         otherwise false
	 */
	public boolean isEventHandler() {
		return eventHandler;
	}
	
	/**
	 * @param EHOnEvent Set true if Scope is instance of OnEvent in EH
	 */
	public void setEHOnEvent(boolean EHOnEvent) {
		this.EHOnEvent = EHOnEvent;
	}
	
	public boolean isEHOnEvent() {
		return EHOnEvent;
	}
	
	/**
	 * @param EHOnEvent Set true if Scope is instance of OnAlarm in EH
	 */
	public void setEHOnAlarm(boolean EHOnAlarm) {
		this.EHOnAlarm = EHOnAlarm;
	}
	/**
	 * It is a flag which tells if the {@link Scope} in an {@link EventHandler} is within an OnAlarm
	 * @return true if {@link Scope} is the OnAlarm Scope in an {@link EventHandler}
	 */
	public boolean isEHOnAlarm() {
		return EHOnAlarm;
	}
	
	/**
	 * @param eventHandler the eventHandler to set
	 */
	public void setEventHandler(boolean eventHandler) {
		this.eventHandler = eventHandler;
		
	}
}
