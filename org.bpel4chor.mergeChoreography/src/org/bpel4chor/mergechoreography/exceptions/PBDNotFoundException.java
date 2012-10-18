package org.bpel4chor.mergechoreography.exceptions;

/**
 * Exception thrown when given process is not found inside the PBD list
 * 
 * @since Aug 14, 2012
 * @author Peter Debicki
 */
public class PBDNotFoundException extends Exception {
	
	private static final long serialVersionUID = -2674880192326095397L;
	
	
	public PBDNotFoundException() {
		super();
	}
	
	public PBDNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PBDNotFoundException(String message) {
		super(message);
	}
	
	public PBDNotFoundException(Throwable cause) {
		super(cause);
	}
	
}
