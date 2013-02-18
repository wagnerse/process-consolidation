package org.bpel4chor.mergechoreography.test;

import org.bpel4chor.mergechoreography.util.ChoreoMergeUtil;
import org.eclipse.bpel.model.BPELFactory;
import org.eclipse.bpel.model.CatchAll;
import org.eclipse.bpel.model.Empty;
import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Scope;
import org.eclipse.bpel.model.TerminationHandler;
import org.eclipse.bpel.model.Variable;

public class BPELStructureTest {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Scope scope = BPELFactory.eINSTANCE.createScope();
		scope.setFaultHandlers(BPELFactory.eINSTANCE.createFaultHandler());
		CatchAll catchAll = BPELFactory.eINSTANCE.createCatchAll();
		TerminationHandler terminationHandler = BPELFactory.eINSTANCE.createTerminationHandler();
		// scope.setTerminationHandler(BPELFactory.eINSTANCE.createTerminationHandler());
		Empty empty1 = BPELFactory.eINSTANCE.createEmpty();
		Empty empty2 = BPELFactory.eINSTANCE.createEmpty();
		catchAll.setActivity(empty1);
		terminationHandler.setActivity(empty2);
		scope.getFaultHandlers().setCatchAll(catchAll);
		scope.setTerminationHandler(terminationHandler);
		scope.setVariables(BPELFactory.eINSTANCE.createVariables());
		
		Variable variable1 = BPELFactory.eINSTANCE.createVariable();
		Variable variable2 = BPELFactory.eINSTANCE.createVariable();
		Variable variable3 = BPELFactory.eINSTANCE.createVariable();
		variable1.setName("pepe1");
		variable2.setName("pepe2");
		variable3.setName("pepe3");
		scope.getVariables().getChildren().add(variable2);
		scope.getVariables().getChildren().add(variable3);
		
		Process process = BPELFactory.eINSTANCE.createProcess();
		process.setVariables(BPELFactory.eINSTANCE.createVariables());
		process.getVariables().getChildren().add(variable1);
		process.setActivity(scope);
		System.out.println(ChoreoMergeUtil.resolveVariable("pepe1", empty1));
		System.out.println(ChoreoMergeUtil.resolveVariable("pepe1", empty2));
		System.out.println(ChoreoMergeUtil.resolveVariable("pepe2", empty1));
		
		// System.out.println("" +
		// empty1.eContainer().eContainer().eContainer().eContainer());
		// System.out.println("" + empty2.eContainer().eContainer());
		
	}
	
}
