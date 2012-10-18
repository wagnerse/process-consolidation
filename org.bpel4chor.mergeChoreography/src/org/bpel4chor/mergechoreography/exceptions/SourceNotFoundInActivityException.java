package org.bpel4chor.mergechoreography.exceptions;

/**
 * Exception thrown when linkName is not found as a source in an activity
 * 
 * @since Aug 14, 2012
 * @author Peter Debicki
 */
public class SourceNotFoundInActivityException extends Exception {
	
	private static final long serialVersionUID = -2024488496708087704L;
	
	
	public SourceNotFoundInActivityException() {
		super();
	}
	
	public SourceNotFoundInActivityException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SourceNotFoundInActivityException(String message) {
		super(message);
	}
	
	public SourceNotFoundInActivityException(Throwable cause) {
		super(cause);
	}
}
