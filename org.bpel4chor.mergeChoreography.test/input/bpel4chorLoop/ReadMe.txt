The scenarios for Loop Consolidation is listed in "scenarios.pptx"

TO RUN LOOP CONSOLIDATION:
1) Modify the constant "mergeOutputPath" present in Constants.java in 
	org.bpel4chor.mergechoreography.test.util package.
2) Open ChoreographyMergerLoopTest.java present in 
	org.bpel4chor.mergechoreography.test.looputil package.
3) Modify the number present in [] in static String scenario = scenarios[0] to the 
	desired scenarios. The number representing the scenarios is given in the comment
	part of "scenarios" variable.
4) Run the java class and obtain the merged output file in the path given in 
	"mergeOutputPath" constant.
	
TO CHECK THE MERGED FILE
1) Execute the previous steps to obtain the merged file for the desired scenario.
2) Run CheckLoopMergeResultTest.java present in 
	org.bpel4chor.mergechoreography.test.looputil package.
3) The class checks the merged file created with the expected merged file present in 
	checkFiles folder.