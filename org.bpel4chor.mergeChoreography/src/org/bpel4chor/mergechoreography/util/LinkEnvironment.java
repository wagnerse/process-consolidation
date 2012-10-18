package org.bpel4chor.mergechoreography.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bpel4chor.model.topology.impl.MessageLink;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;

/**
 * Data Class for holding all data from surrounding activities of a
 * {@link MessageLink}
 * 
 * Copyright 2012 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author Peter.Debicki
 * 
 */
public class LinkEnvironment implements Serializable {
	
	private static final long serialVersionUID = 7072180475599257326L;
	
	/** List of activities preceding the invoke on the sender side */
	private List<Activity> sendBeforeEnvironment = new ArrayList<>();
	/** The invoke activity */
	private Invoke invoke;
	/** List of activities succeeding the invoke on the sender side */
	private List<Activity> sendAfterEnvironment = new ArrayList<>();
	
	/** List of activities preceding the receive on the receiver side */
	private List<Activity> recBeforeEnvironment = new ArrayList<>();
	/** The receive activity */
	private Receive receive;
	/** List of activities succeeding the receive on the receiver side */
	private List<Activity> recAfterEnvironment = new ArrayList<>();
	/** The sending process */
	private Process sender;
	/** The receiving process */
	private Process receiver;
	
	
	public List<Activity> getSendBeforeEnvironment() {
		return this.sendBeforeEnvironment;
	}
	
	public void setSendBeforeEnvironment(List<Activity> sendBeforeEnvironment) {
		this.sendBeforeEnvironment = sendBeforeEnvironment;
	}
	
	public Invoke getInvoke() {
		return this.invoke;
	}
	
	public void setInvoke(Invoke invoke) {
		this.invoke = invoke;
	}
	
	public List<Activity> getSendAfterEnvironment() {
		return this.sendAfterEnvironment;
	}
	
	public void setSendAfterEnvironment(List<Activity> sendAfterEnvironment) {
		this.sendAfterEnvironment = sendAfterEnvironment;
	}
	
	public List<Activity> getRecBeforeEnvironment() {
		return this.recBeforeEnvironment;
	}
	
	public void setRecBeforeEnvironment(List<Activity> recBeforeEnvironment) {
		this.recBeforeEnvironment = recBeforeEnvironment;
	}
	
	public Receive getReceive() {
		return this.receive;
	}
	
	public void setReceive(Receive receive) {
		this.receive = receive;
	}
	
	public List<Activity> getRecAfterEnvironment() {
		return this.recAfterEnvironment;
	}
	
	public void setRecAfterEnvironment(List<Activity> recAfterEnvironment) {
		this.recAfterEnvironment = recAfterEnvironment;
	}
	
	public Process getSender() {
		return this.sender;
	}
	
	public void setSender(Process sender) {
		this.sender = sender;
	}
	
	public Process getReceiver() {
		return this.receiver;
	}
	
	public void setReceiver(Process receiver) {
		this.receiver = receiver;
	}
	
}
