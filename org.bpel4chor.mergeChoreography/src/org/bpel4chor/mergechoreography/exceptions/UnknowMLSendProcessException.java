package org.bpel4chor.mergechoreography.exceptions;

import org.bpel4chor.mergechoreography.util.MLEnvironmentAnalyzer;
import org.eclipse.bpel.model.Process;

/**
 * Exception thrown when sender {@link Process} name is unknown to
 * {@link MLEnvironmentAnalyzer}
 * 
 * @since Aug 14, 2012
 * @author Peter Debicki
 */
public class UnknowMLSendProcessException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4786160165415608855L;
	
	
	public UnknowMLSendProcessException() {
		super();
	}
	
	public UnknowMLSendProcessException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnknowMLSendProcessException(String message) {
		super(message);
	}
	
	public UnknowMLSendProcessException(Throwable cause) {
	}
	
}
