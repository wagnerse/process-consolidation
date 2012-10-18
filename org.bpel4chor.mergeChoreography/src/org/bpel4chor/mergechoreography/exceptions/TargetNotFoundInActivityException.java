package org.bpel4chor.mergechoreography.exceptions;

/**
 * Exception thrown when linkName is not found as a target in an activity
 * 
 * @since Aug 14, 2012
 * @author Peter Debicki
 */
public class TargetNotFoundInActivityException extends Exception {
	
	private static final long serialVersionUID = 8557133304674457295L;
	
	
	public TargetNotFoundInActivityException() {
		super();
	}
	
	public TargetNotFoundInActivityException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public TargetNotFoundInActivityException(String message) {
		super(message);
	}
	
	public TargetNotFoundInActivityException(Throwable cause) {
	}
}
