package org.bpel4chor.mergechoreography.exceptions;

/**
 * Exception thrown, when trying to read a merged Process of a not yet merged
 * choreography
 * 
 * @since Aug 14, 2012
 * @author Peter Debicki
 */
public class ChoreoNotYetMergedException extends Exception {
	
	private static final long serialVersionUID = -97954722583239362L;
	
	
	public ChoreoNotYetMergedException() {
		super();
	}
	
	public ChoreoNotYetMergedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ChoreoNotYetMergedException(String message) {
		super(message);
	}
	
	public ChoreoNotYetMergedException(Throwable cause) {
		super(cause);
	}
	
}
