/**
 * 
 */
package validation.strategies.cfg.localization;

import ilog.concert.IloException;
import ilog.cp.IloCP;
import ilog.cplex.IloCplex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import CFG.CFG;
import CFG.SetOfCFG;
import java2CFGTranslator.CFGBuilder;
import java2CFGTranslator.Java2CFGException;

import solver.cplex.Cplex;
import solver.ilocp.cfg.IloCPCfgCsp;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.solution.ValidationStatus;
import validation.strategies.cfg.localization.DFSdevieVisitor.CtrInfo;

/**
 * The location of errors by using CPOPTIMIZER solver
 * @author mdbekkouche
 *
 */
public class CPOPTIMIZERLocalization {
	
	protected HashMap<String, CtrInfo> ctrInfos; 
	protected SetOfCFG program;
	protected CFG method;
	/**
	 * Status of the localization.
	 */ 
	protected ValidationStatus status;
	
	protected DFSdevieVisitor pv;
	protected IloCPCfgCsp csp;
	protected IloCP solver;
	public CPOPTIMIZERLocalization(int numberFaultyCond){
		status = new ValidationStatus();

		// Set program and method attributes
		
		long time = Calendar.getInstance().getTimeInMillis();
		buildCFG();			
		//simplifyCFG();
		long time1 = Calendar.getInstance().getTimeInMillis();
		
		HashMap<String, String> inputs = null;
		long sumtimeDFSMCS = 0;
		try {
			inputs = readCounterexample(Validation.counterExampleFileName);
			System.out.println("\nFrom the counterexample, LocFaults calculates MCS by exploring the graph in DFS from top to bottom and by deviating at most '"+numberFaultyCond+"' conditional statements.");
			System.out.println("Solver: CP OPTIMIZER");
		} catch (IOException e) {
			System.err.println("Error while trying to read counterexample file (" + Validation.counterExampleFileName + ")!");
			e.printStackTrace();
			System.exit(-1);
		}
		pv = new DFSdevieVisitor();
		for (int k=0;k<=numberFaultyCond;k++){
			
			System.out.println("By deviating '"+k+"' condition(s), we obtain:\n");
		    csp = new IloCPCfgCsp("CP Optimizer");			
		    pv.setCSP(csp);
		    csp.save();
		    long timeDFSMCS = Calendar.getInstance().getTimeInMillis();
		    ctrInfos = pv.visitPath(method, program.getFieldDeclaration().getBlock(),inputs,0,k,"CP Optimizer");			
		    long timeDFSMCS1 = Calendar.getInstance().getTimeInMillis();
		    System.out.println();
			long timeDFSMCSF=timeDFSMCS1-timeDFSMCS;
			Printresults(timeDFSMCSF);
			sumtimeDFSMCS=sumtimeDFSMCS+timeDFSMCSF;
		}
		long timeCFGBuilding=time1-time;
		PrintFinalresults(timeCFGBuilding,sumtimeDFSMCS);
	}
	
	private void Printresults(long timeDFSMCS) {
		
		System.out.println("The resulats:");
		System.out.println("1. Elapsed time during DFS exploration and MCS calculation: "+(timeDFSMCS)/1000.0);
		System.out.println("2. The sum of computation time of MCS isolations only: "+pv.timeMCS);
		System.out.println();
    	System.out.print("Suspicious instructions:");
    	int lengthSuspInsts=pv.suspInsts.size();
    	for (int i=0;i<lengthSuspInsts;i++){
    		String elts = pv.suspInsts.get(i);
    		if (i!=lengthSuspInsts-1){
    			System.out.print(elts+",");
    		}else{
    			System.out.print(elts);
    		}
    	}
    	System.out.println();
    	System.out.println("The number of suspicious instructions:"+lengthSuspInsts);
    	System.out.println();
	}
	
	private void PrintFinalresults(long timeCFGBuilding,long sumtimeDFSMCS) {
		System.out.println("/***************************************************************/");
		System.out.println("The final resulats:");
		System.out.println("1. The pretreatment(CFG building) time: "+(timeCFGBuilding)/1000.0);
		System.out.println("2. Elapsed time during DFS exploration and MCS calculation: "+(sumtimeDFSMCS)/1000.0);
	}
	
	public void localize()  {
		
	}
	/**
	 * Builds the CFG from the source code file.
	 */
	private void buildCFG() {
		status.cfgBuildingTime = System.currentTimeMillis();
		try {
			this.program = new CFGBuilder(Validation.pgmFileName, Validation.pgmMethodName, 
										  Validation.maxUnfoldings, Validation.maxArrayLength).convert();
		} catch(Java2CFGException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		status.cfgBuildingTime = System.currentTimeMillis() - status.cfgBuildingTime;

		if (this.program != null) {
			method = Validation.pgmMethod(program);
    		if (VerboseLevel.DEBUG.mayPrint()) {
    			System.out.println("\nInitial CFGs\n" +program);
    		}
		}
		else {
			System.err.println("Error: method " 
					+ Validation.pgmMethodName 
					+ " is not defined in " 
					+ Validation.pgmFileName 
					+ "!");
			System.exit(-1);
		}
	}
	/**
	 * Reads the counterexample from the file with name <code>fileName</code>.
	 * 
	 * Each line of this file contains either a variable name and the 
	 * value of this variable. The file must at least contain the values of all the
	 * verified method parameters and non-det assignments. 
	 * The variable name is the variable fully qualified name (with prefix and SSA number). 
	 * 
	 * 
	 * @param fileName Name of the file containing the counterexample.
	 * @return A mapping of variable names and values.
	 * @throws IOException 
	 */
	protected HashMap<String, String> readCounterexample(String fileName) 
		throws IOException 
	{
		BufferedReader counterexample = new BufferedReader(new FileReader(fileName));
		HashMap<String, String> inputs = new HashMap<String, String>();
		String line;
		String[] input;
		
		while ((line = counterexample.readLine()) != null) {
	    	if (Validation.verboseLevel == VerboseLevel.DEBUG) {
	    		System.out.println(line);
	    	}
	    	// Skip empty lines
	    	if (!line.isEmpty()) {
	    		input = line.split(" ");
	    		inputs.put(input[0], input[1]);
	    	}
		}
	    return inputs;
	}
}
