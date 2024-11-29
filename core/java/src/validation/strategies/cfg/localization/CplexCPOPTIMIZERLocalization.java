package validation.strategies.cfg.localization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import CFG.CFG;
import CFG.SetOfCFG;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import java2CFGTranslator.CFGBuilder;
import java2CFGTranslator.Java2CFGException;
import solver.cplex.cfg.CplexCfgCsp;
import solver.ilocp.cfg.IloCPCfgCsp;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.solution.ValidationStatus;
import validation.strategies.cfg.localization.DFSdevieVisitor.CtrInfo;

public class CplexCPOPTIMIZERLocalization {
	
	public int numberCondition;
	protected SetOfCFG program;
	protected CFG method;
	/**
	 * Status of the localization.
	 */ 
	protected ValidationStatus status;
	/**
	 * CSP associated to this strategy.
	 */
	protected CplexCfgCsp csp;
	
	protected IloCPCfgCsp cspcp;
	protected IloCplex solver;
	protected DFSdevieVisitorExtented pv;
	/**
	 * Maps Cplex constraint names to constraint information (source code line number, 
	 * associated preference (hard or soft constraint, original expression).
	 */
	protected HashMap<String, CtrInfo> ctrInfos; 
	
	
	public CplexCPOPTIMIZERLocalization(int numberFaultyCond) throws IloException {		
		status = new ValidationStatus();

		// Set program and method attributes
		
		long time = Calendar.getInstance().getTimeInMillis();
		buildCFG();			
		//simplifyCFG();
		long time1 = Calendar.getInstance().getTimeInMillis();

		try {
			HashMap<String, String> inputs = readCounterexample(Validation.counterExampleFileName);
			System.out.println("\nFrom the counterexample, LocFaults calculates MCS and IIS by exploring the graph in DFS from top to bottom and by deviating at most '"+numberFaultyCond+"' conditional statements.");
			//System.out.println("Solver: CPLEX");
			long sumtimeDFSMCSIIS = 0;
			double sumtimeDFS = 0;
			pv = new DFSdevieVisitorExtented();
			for (int k=0;k<=numberFaultyCond;k++){
				System.out.println("/***************************************************************/");
				System.out.println("By deviating '"+k+"' condition(s), we obtain:\n");
							
				csp = new CplexCfgCsp("CPLEX", pv);
				
				cspcp = new IloCPCfgCsp("CP OPTIMIZER");	
				
				//csp = createCSP();
				//solver = ((Cplex)csp.solver).getIloCplex();			
				pv.setCSP(csp,cspcp);
				csp.save();
				cspcp.save();
				long timeDFSMCSIIS = Calendar.getInstance().getTimeInMillis();
				ctrInfos = pv.visitPath(method, program.getFieldDeclaration().getBlock(),inputs,0,k,"CPLEX");			
				long timeDFSMCSIIS1 = Calendar.getInstance().getTimeInMillis();
				System.out.println();
				long timeDFSMCSIISF=timeDFSMCSIIS1-timeDFSMCSIIS;
				double timeDFS = Printresults(timeDFSMCSIISF);
				sumtimeDFSMCSIIS=sumtimeDFSMCSIIS+timeDFSMCSIISF;
				sumtimeDFS = timeDFS + sumtimeDFS;
			}
			long timeCFGBuilding=time1-time;
			PrintFinalresults(timeCFGBuilding,sumtimeDFS,sumtimeDFSMCSIIS);
			
		}catch (IOException e) {
			System.err.println("Error while trying to read counterexample file (" + Validation.counterExampleFileName + ")!");
			e.printStackTrace();
			System.exit(-1);
		}	
	}
	
	private double Printresults(long timeDFSMCSIIS) {
		
		System.out.println("The resulats:"); 
		double timeMCS = pv.getTimeMCS();
		double timeIISDF = pv.getTimeIISDF();
		double timeIISQE = pv.getTimeIISQE();
		double timeIISCR = pv.getTimeIISCR();
        double timeDFS = timeDFSMCSIIS - (timeMCS+timeIISDF+timeIISQE+timeIISCR);
		System.out.println("1. Elapsed time during DFS exploration: "+(timeDFS )/1000.0);
		System.out.println("2. Elapsed time during MCS calculation: "+(timeMCS)/1000.0);
		System.out.println("3. Elapsed time during IIS isolation using Deletion Filter: "+(timeIISDF)/1000.0);
		System.out.println("4. Elapsed time during IIS isolation using QuickExplain: "+(timeIISQE)/1000.0);
		System.out.println("5. Elapsed time during IIS isolation using Conflict Refiner: "+(timeIISCR)/1000.0);
		/*System.out.println("3. MCS found:");
		System.out.println("Assignments");   	
		ArrayList<ArrayList<Integer>>  AllMCSProgramAss = new ArrayList<ArrayList<Integer>>();  	
		for (int i=0;i<pv.allMCSProgramAss.size();i++){
    		for (int j=0;j<pv.allMCSProgramAss.get(i).size();j++){
    			ArrayList<Integer> arrListMCS = new ArrayList<Integer>();
    			boolean b=false;
    			for (int k=0;k<pv.allMCSProgramAss.get(i).get(j).size();k++){
    				int Line = ctrInfos.get(pv.allMCSProgramAss.get(i).get(j).get(k)).startLine;    	            		  
	            	  String CE = ctrInfos.get(pv.allMCSProgramAss.get(i).get(j).get(k)).pgmCtr.toString();
	            	  if (b==false){
	            		  
	            		  if (Line !=-1 && Line != -2 && Line != 0){
	            			  b=true;
	            			  arrListMCS.add(Line);			  
	            		  }	            		    
	            	  }
	            	  else{
	            		  
	            		  if (Line != -1 && Line != -2 && Line != 0){
	            			  arrListMCS.add(Line);
	            		  }	            		   
	            	  }
    			}
    			if (b==true){
    				int k=0;
    				while (k<AllMCSProgramAss.size() && !arrListMCS.containsAll(AllMCSProgramAss.get(k))){					
    					k++;
    				}					
    				if (k==AllMCSProgramAss.size())	
    			     AllMCSProgramAss.add(arrListMCS);	
    			  //System.out.println("}");
    			}
    		}
    	}
    	
    	for (int i=0;i<AllMCSProgramAss.size();i++){
    		boolean b=false;
    		
    		for (int j=0;j<AllMCSProgramAss.get(i).size();j++){
    			if (b==false){	            		
            			  b=true;
            			  System.out.print("{line "+AllMCSProgramAss.get(i).get(j)); 	            		    
            	  }
            	  else{
            			  System.out.print(",line "+AllMCSProgramAss.get(i).get(j)); 	            		   
            	  }
    		}
    		System.out.println("}");	    			
    	}
    	
    	
    	System.out.println("Conditions");
    	for (int i=0;i<pv.allMCSProgramCond.size();i++){
    		boolean b=false;
    		
    		for (int j=0;j<pv.allMCSProgramCond.get(i).size();j++){
    			if (b==false){	            		
            			  b=true;
            			  System.out.print("{line "+pv.allMCSProgramCond.get(i).get(j)); 	            		    
            	  }
            	  else{
            			  System.out.print(",line "+pv.allMCSProgramCond.get(i).get(j)); 	            		   
            	  }
    		}
    		System.out.println("}");
    			
    	}*/
    	/*System.out.println();
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
    	System.out.println();*/
		return timeDFS;
	}
	
	private void PrintFinalresults(long timeCFGBuilding,double sumtimeDFS, long sumtimeDFSMCSIIS) {
		System.out.println("/***************************************************************/");
		System.out.println("The final resulats:");
		System.out.println("1. The pretreatment(CFG building) time: "+(timeCFGBuilding)/1000.0);
		System.out.println("2. Total elapsed time during DFS exploration: "+(sumtimeDFS)/1000.0);
    	System.out.println();
    	double globalTimeMCS = pv.getGlobalTimeMCS();
    	double globalTimeIIS_DF = pv.getGlobalTimeIIS_DF();
    	double globalTimeIIS_QE = pv.getGlobalTimeIIS_QE();
    	double globalTimeIIS_CR = pv.getGlobalTimeIIS_CR();
    	System.out.println("3. The time required to calculate the MCSs:"+globalTimeMCS/1000.0);
    	System.out.println("4. The time required for Deletion Filter:"+globalTimeIIS_DF/1000.0);
    	System.out.println("5. The time required for QuickExplain:"+globalTimeIIS_QE/1000.0);
    	System.out.println("6. The time required for the conflict refiner implementation:"+globalTimeIIS_CR/1000.0);
    	System.out.println("7. Total elapsed time during DFS exploration and MCS calculation: "+(sumtimeDFSMCSIIS-(globalTimeIIS_DF+globalTimeIIS_QE+globalTimeIIS_CR))/1000.0);
    	System.out.println("8. Total elapsed time during DFS exploration and IIS calculation using Deletion Filter: "+(sumtimeDFSMCSIIS-(globalTimeMCS+globalTimeIIS_QE+globalTimeIIS_CR))/1000.0);
    	System.out.println("9. Total elapsed time during DFS exploration and IIS calculation using QuickExplain: "+(sumtimeDFSMCSIIS-(globalTimeIIS_DF+globalTimeMCS+globalTimeIIS_CR))/1000.0);
    	System.out.println("10. Total elapsed time during DFS exploration and IIS calculation using conflict refiner: "+(sumtimeDFSMCSIIS-(globalTimeIIS_DF+globalTimeIIS_QE+globalTimeMCS))/1000.0);
    	System.out.println("11. The number of paths that resulted in an IIS with at least one soft constraint: "+pv.getNbIISCal());
    	System.out.println("12. Suspicious instructions (using MCSs):"+pv.suspInsts);
    /*	int lengthSuspInsts=pv.suspInsts.size();
    	for (int i=0;i<lengthSuspInsts;i++){
    		String elts = pv.suspInsts.get(i);
    		if (i!=lengthSuspInsts-1){
    			System.out.print(elts+",");
    		}else{
    			System.out.print(elts);
    		}
    	}
    	System.out.println();*/
    	//System.out.println("12. The number of suspicious instructions:"+lengthSuspInsts);
    	System.out.println("13. Suspicious instructions (using Deletion Filter):"+CplexLocalization.removeDuplicatesAndZeros(pv.getSuspInstsDF()));
    	System.out.println("14. Suspicious instructions (using QuickExplain):"+CplexLocalization.removeDuplicatesAndZeros(pv.getSuspInstsQE()));
    	System.out.println("15. Suspicious instructions (using Conflict Refiner):"+CplexLocalization.removeDuplicatesAndZeros(pv.getSuspInstsCR()));
    	//System.out.println("14. The number of suspicious instructions:"+);
    	
    	System.out.println();
	}
	
	/**
	 * Builds the CFG from the source code file.
	 */
	private void buildCFG() {
		status.cfgBuildingTime = System.currentTimeMillis();
		try {
			this.program = new CFGBuilder(Validation.pgmFileName, Validation.pgmMethodName, 
										  Validation.maxUnfoldings, Validation.maxArrayLength).convert();
			System.out.println("The size of the constructed CFG: "+this.program.getNbrInstructions());
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
