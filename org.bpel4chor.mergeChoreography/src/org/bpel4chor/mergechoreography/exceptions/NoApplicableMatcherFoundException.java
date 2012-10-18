package org.bpel4chor.mergechoreography.exceptions;

/**
 * Exception from Matcher
 * 
 * @since Aug 14, 2012
 * @author Peter Debicki
 */
public class NoApplicableMatcherFoundException extends Exception {
	
	private static final long serialVersionUID = -135973333673850295L;
	
	
	public NoApplicableMatcherFoundException() {
		super();
	}
	
	public NoApplicableMatcherFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NoApplicableMatcherFoundException(String message) {
		super(message);
	}
	
	public NoApplicableMatcherFoundException(Throwable cause) {
		super(cause);
	}
}
