package org.bpel4chor.mergechoreography.util;

import org.apache.log4j.Logger;
import org.bpel4chor.utils.FragmentDuplicator;
import org.bpel4chor.utils.MyBPELUtils;
import org.eclipse.bpel.model.Activity;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.Correlations;
import org.eclipse.bpel.model.Invoke;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Receive;
import org.eclipse.bpel.model.Reply;
import org.eclipse.bpel.model.Variable;
import org.eclipse.wst.wsdl.Definition;

/**
 * The PBDFragmentDuplicator helps copying PBD Communication Activities
 * (Receive, Invoke, Reply, ForEach), for the rest the normal
 * {@link FragmentDuplicator} is used
 * 
 * @since Oktober 15, 2012
 * @author Peter Debicki
 */
public class PBDFragmentDuplicator {
	
	protected static Logger logger = Logger.getLogger(PBDFragmentDuplicator.class);
	
	
	/**
	 * Copy the original activity and return a new one.
	 * 
	 * <p>
	 * 1. Note that this copy is not recursively, because we just need a copy of
	 * the activity currently given, not its children.
	 * 
	 * @param origAct The original activity
	 * @param fragProc The fragment process
	 * @param fragDefn The fragment definition
	 * @return The new activity
	 */
	public static Activity copyActivity(Activity origAct, Process fragProc, Definition fragDefn) {
		
		if (origAct == null) {
			throw new NullPointerException("argument is null");
		}
		
		Activity newActivity = null;
		
		if (origAct instanceof Receive) {
			newActivity = PBDFragmentDuplicator.copyReceivePBD((Receive) origAct, fragProc, fragDefn);
		} else if (origAct instanceof Invoke) {
			newActivity = PBDFragmentDuplicator.copyInvokePBD((Invoke) origAct, fragProc, fragDefn);
		} else if (origAct instanceof Reply) {
			newActivity = PBDFragmentDuplicator.copyReplyPBD((Reply) origAct, fragProc, fragDefn);
		} else {
			newActivity = FragmentDuplicator.copyActivity(origAct, fragProc, fragDefn);
		}
		return newActivity;
	}
	
	/**
	 * Copy the original reply activity and return a new one.
	 * 
	 * @param origAct
	 * @return
	 */
	public static Receive copyReceivePBD(Receive origAct, Process fragProc, Definition fragDefn) {
		
		if (origAct == null) {
			return null;
		}
		
		Receive newReceive = null;
		// To copy receive: (1) partner link (2) portType (3)operation (4)
		// variable (5) createInstance (6) messageExchange (7) correlations (8)
		// fromParts (9) standard-elements(targets/sources)
		
		newReceive = BPELFactory.eINSTANCE.createReceive();
		FragmentDuplicator.copyStandardAttributes(origAct, newReceive);
		
		// variable
		if (origAct.getVariable() != null) {
			String varName = origAct.getVariable().getName();
			Variable var = MyBPELUtils.resolveVariable(varName, fragProc);
			if (var == null) {
				throw new IllegalStateException("Variable " + varName + " is not found in fragment process " + fragProc.getName());
			}
			newReceive.setVariable(var);
		}
		
		// correlations
		if (origAct.getCorrelations() != null) {
			
			Correlations newCorrelations = FragmentDuplicator.copyCorrelations(origAct.getCorrelations(), fragProc);
			newReceive.setCorrelations(newCorrelations);
		}
		
		// TODO message exchange
		if (origAct.getMessageExchange() != null) {
			newReceive.setMessageExchange(origAct.getMessageExchange());
		}
		
		FragmentDuplicator.copyStandardElements(origAct, newReceive);
		
		return newReceive;
	}
	
	/**
	 * Copy the original invoke activity and return a new one.
	 * 
	 * @param origAct
	 * @param fragProc
	 * @param fragDefn
	 * @return
	 */
	public static Invoke copyInvokePBD(Invoke origAct, Process fragProc, Definition fragDefn) {
		
		if (origAct == null) {
			return null;
		}
		
		Invoke newInvoke = null;
		
		// To copy the invoke, (1) partner link (2) operation (3) portType (4)
		// variable: inputVariable, outputVariable (5) correlation (6)
		// compensationHandler (6) name (7) suppressJoinFailure (8)
		// standard-elements(targets/sources)
		newInvoke = BPELFactory.eINSTANCE.createInvoke();
		
		FragmentDuplicator.copyStandardAttributes(origAct, newInvoke);
		
		// inputVariable
		if (origAct.getInputVariable() != null) {
			String inputVarName = origAct.getInputVariable().getName();
			Variable inputVar = MyBPELUtils.resolveVariable(inputVarName, fragProc);
			if (inputVar == null) {
				throw new IllegalStateException("Variable " + inputVarName + " is not found in fragment process " + fragProc.getName());
			}
			newInvoke.setInputVariable(inputVar);
		}
		
		// outputVariable
		if (origAct.getOutputVariable() != null) {
			String outputVarName = origAct.getOutputVariable().getName();
			Variable outputVar = MyBPELUtils.resolveVariable(outputVarName, fragProc);
			if (outputVar == null) {
				throw new IllegalStateException("Variable " + outputVarName + " is not found in fragment process " + fragProc.getName());
			}
			
			newInvoke.setOutputVariable(origAct.getOutputVariable());
		}
		
		// correlations
		if (origAct.getCorrelations() != null) {
			
			Correlations newCorrelations = FragmentDuplicator.copyCorrelations(origAct.getCorrelations(), fragProc);
			newInvoke.setCorrelations(newCorrelations);
		}
		
		// compensationHandler
		if (origAct.getCompensationHandler() != null) {
			// TODO
			newInvoke.setCompensationHandler(origAct.getCompensationHandler());
		}
		
		FragmentDuplicator.copyStandardElements(origAct, newInvoke);
		
		return newInvoke;
	}
	
	/**
	 * Copy the original reply activity and return a new one.
	 * 
	 * @param origAct
	 * @return
	 */
	public static Reply copyReplyPBD(Reply origAct, Process fragProc, Definition fragDefn) {
		
		if (origAct == null) {
			return null;
		}
		
		Reply newReply = null;
		// To copy reply: (1) partnerLink (2) portType (3) variable (4)
		// faultName (5) messageExchange (6) correlations (7) toParts (8)
		// standard-elements(targets/sources) (9) standard-attributes
		
		newReply = BPELFactory.eINSTANCE.createReply();
		FragmentDuplicator.copyStandardAttributes(origAct, newReply);
		
		// variable
		if (origAct.getVariable() != null) {
			String varName = origAct.getVariable().getName();
			Variable var = MyBPELUtils.resolveVariable(varName, fragProc);
			if (var == null) {
				throw new IllegalStateException("Variable " + varName + " is not found in fragment process " + fragProc.getName());
			}
			newReply.setVariable(var);
		}
		
		// correlations
		if (origAct.getCorrelations() != null) {
			
			Correlations newCorrelations = FragmentDuplicator.copyCorrelations(origAct.getCorrelations(), fragProc);
			newReply.setCorrelations(newCorrelations);
		}
		
		// TODO message exchange
		if (origAct.getMessageExchange() != null) {
			newReply.setMessageExchange(origAct.getMessageExchange());
		}
		
		FragmentDuplicator.copyStandardElements(origAct, newReply);
		
		return newReply;
	}
}
