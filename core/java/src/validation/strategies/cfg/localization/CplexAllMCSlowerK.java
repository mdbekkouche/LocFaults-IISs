/**
 * 
 */
package validation.strategies.cfg.localization;

import ilog.concert.IloAddable;
import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;

import CFG.ConditionalNode;

import solver.ConcreteSolver;
import solver.cplex.Cplex;
import validation.strategies.cfg.localization.DFSdevieVisitor.CtrInfo;
import validation.system.cfg.CfgCsp;

/**
 * we calculate MCS of size <= k
 * @author mdbekkouche
 *
 */
public class CplexAllMCSlowerK {

	protected IloCplex solver; 
	protected HashMap<String, CtrInfo> ctrInfos;
	protected int NumberFaultyCond;
	public IloIntVar[] x;
	public IloIntVar[] nonx;
	private BufferedWriter conflictOutput;
	public ArrayList<String> SuspInsts1 = new ArrayList<String>();
	public double timeMCS;
	public ArrayList<ArrayList<String>> arrListMCSes;
	
	private void output(String s) {
		try {
			conflictOutput.write(s);
		} catch (IOException e) {
			System.err.println("Warning :Unable to write to output file!\n");
			e.printStackTrace();
		}
	}
	
	/*
     * L'implementation de l'algorithme de Liffiton.
     */
    private int AddYVars(IloConstraint[] ctrs) throws IloException{	   
    	
        x = solver.boolVarArray(ctrs.length);
        nonx = solver.boolVarArray(ctrs.length);
        int cpt=0;
        
        
    	for (int c=0;c<=ctrs.length-1;c++){
    		solver.add(solver.ifThen(solver.eq(x[c],1),ctrs[c]));   		
    		int Line = ctrInfos.get(ctrs[c].getName()).startLine;
    		if (Line==-1 || Line==-2){
    			solver.add(solver.eq(x[c],1));
    			cpt++;
    		}
        }
    	return cpt;	
    }
    
    private IloConstraint AtMost(int k) throws IloException{
    	int lengthx=x.length;
		return solver.le(solver.diff(lengthx,solver.sum(x)),k);   	
    }
    
    private ArrayList<ArrayList<String>> MCSesLiffiton(IloConstraint[] ctrs) throws IloException{
    	int i=0;
    	
    	/**********************/
    	Iterator<?> iter = solver.getModel().iterator();
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof IloConstraint) {
				solver.remove((IloConstraint)next);
			}
			else if (next instanceof IloObjective){
				solver.remove((IloObjective)next);	
			}
			
		}
    	/**********************/
      	
    	solver.exportModel("modelCAMUS-EX.lp");
    	int nbCtrsCEPOST=AddYVars(ctrs);
    	solver.exportModel("modelCAMUS-EX1.lp");

    	arrListMCSes = new ArrayList<ArrayList<String>>();
    	
    	int CardMinMCS=1;
    	int Nb=0;
    	int k=1;
    	IloIntExpr blocking = null ;
    	/*
    	 * On calculera les MCS de taille <=k.
    	 */
    	while (solver.solve() && k!=CardMinMCS+4){ 
    	    IloConstraint ct=AtMost(k);
    		solver.add(ct);
    		while (solver.solve()){
    			blocking=null;
                i=0;
                
                ArrayList<String> arrListMCS = new ArrayList<String>();
    			for (int c=0;c<=ctrs.length-1;c++){
    	              if (solver.getValue(x[c])==0){
    	            	  i++;
    	            	  
    	            	  int Line = ctrInfos.get(ctrs[c].getName()).startLine;
    	            	  
    	            	  String CE = ctrInfos.get(ctrs[c].getName()).pgmCtr.toString();
    	            	  if (i==1){
    	            		  blocking=x[c];
    	            		  if (Line !=-1 && Line != -2){
    	            			  System.out.print("{line "+Line/*"("+ctrInfos.get(ctrs[c].getName()).pgmCtr.toString()+")"+*/);   	            			  
    	            		  }else if (Line == -1) {
    	            			  System.out.print("{"+CE); 
    	            		  }else {
    	            			  System.out.print("{POST");
    	            		  }
    	            		  arrListMCS.add(ctrs[c].getName());  
    	            	  }
    	            	  else{
    	            		  blocking=solver.sum(blocking, x[c]);
    	            		  if (Line != -1 && Line != -2){
    	            			  System.out.print(",line "+Line/*"("+ctrInfos.get(ctrs[c].getName()).pgmCtr.toString()+")"+*/);
    	            		  }
    	            		  else if (Line == -1) {
    	            			  System.out.print(","+CE); 
    	            		  }
    	            		  else {
    	            			  System.out.print(",POST");
    	            		  }
    	            		  arrListMCS.add(ctrs[c].getName()); 
    	            	  }
    	            	  
    	              }
    	            }
    			System.out.println("}");
    			solver.add(solver.ge(blocking,1)); arrListMCSes.add(arrListMCS);
    		}
    		Nb++;
      		if (Nb==1){
      			CardMinMCS=k;
      		}
    		solver.remove(ct);
    		k++;
    	}
		return arrListMCSes;
	}
    
    /**
     * @param numberFaultyCond 
     * @param stackFaultyCond 
     * @param suspInsts 
     ***/

	
	public CplexAllMCSlowerK(CfgCsp csp, HashMap<String, CtrInfo> ctrInfos1, int numberFaultyCond, ArrayList<String> suspInsts) throws IloException{	
        
		for(int j=0;j<suspInsts.size();j++){
			SuspInsts1.add(suspInsts.get(j));
		}
		
		solver = ((Cplex)csp.solver).getIloCplex();
		
		ctrInfos = ctrInfos1;
		NumberFaultyCond=numberFaultyCond;
		ArrayList<IloAddable> ctrsList = new ArrayList<IloAddable>();
		Iterator<?> iter = solver.getModel().iterator();
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof IloConstraint) {
				ctrsList.add((IloConstraint)next);
			}
			else {
				System.out.println("Warning: Constraint " + next + " has been skipped for conflict refinement!");
			}
		}
		
		// CPLEX needs an array of constraints so we convert the ArrayList into a std array
		IloConstraint[] ctrs = new IloConstraint[ctrsList.size()];
		IloConstraint[] ctrs_copie = new IloConstraint[ctrsList.size()];
		ctrsList.toArray(ctrs);
		ctrsList.toArray(ctrs_copie);
		
		System.out.println("------------------------");
		System.out.println("2. CSP_a:");
		for (int i=0;i<=ctrs.length-1;i++){
        	CtrInfo ctrInfo=ctrInfos.get(ctrs[i].getName()); 
        	System.out.print(ctrInfo.pgmCtr.toString());
        	System.out.print(" --> ");
        	System.out.println("line "+ctrInfo.startLine);
        }
		
		System.out.println("");
		
		
		/**
		 *   The implementation of the algorithm of Liffiton. 
		 */
		if (solver.solve()){			
		    System.out.println("The system is feasible");
			System.out.println("------------------------");
		}
		else{
			System.out.println("The system is infeasible");
			System.out.println("------------------------");
		long time = Calendar.getInstance().getTimeInMillis();
		
		System.out.println("3. MCS in CSP_a:");
		ArrayList<ArrayList<String>> arrListMCSes=MCSesLiffiton(ctrs); 
		long time1 = Calendar.getInstance().getTimeInMillis();
		timeMCS=(time1-time)/1000.0;
		System.out.println("\nRuntime of the method that compute MCS: "+(time1-time)/1000.0);
		
		
		CalculateDegreeSuspicion(arrListMCSes,ctrs);
		
		/**********************/
		iter = solver.getModel().iterator();
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof IloConstraint) {
				solver.remove((IloConstraint)next);
			}
			else if (next instanceof IloObjective){
				solver.remove((IloObjective)next);
			}
				
		}
		/**********************/
		solver.add(ctrs_copie);
		}
		
	}
	
	private void CalculateDegreeSuspicion(ArrayList<ArrayList<String>> arrListMCSes, IloConstraint[] ctrs) {
		//System.out.println("Le calcule du degré de suspicion (MIVc) de chaque instruction :");
		
		int NumberFaultyCtrs=0;
		for (int i=0;i<ctrs.length;i++){
			double degre=0.000;
			
			for (int j=0;j<arrListMCSes.size();j++){
				
				 int k=0;
				 while (k<arrListMCSes.get(j).size() && arrListMCSes.get(j).get(k)!=ctrs[i].getName()){
					 k++;
				 }
				 if (k!=arrListMCSes.get(j).size() && arrListMCSes.get(j).get(k)==ctrs[i].getName()){
					 double valeur = Math.pow(arrListMCSes.get(j).size(), -1);
					 degre=degre+valeur; 
				 }
				
			}
			if (degre!=0.000){
				
				int Line = ctrInfos.get(ctrs[i].getName()).startLine;
				String CE = ctrInfos.get(ctrs[i].getName()).pgmCtr.toString();
	    	
	    	    String line=Integer.toString(Line);
	    	    int lengthSuspInsts1=SuspInsts1.size();
				if (Line != -1 && Line != -2){
					if (Line != 0){
					 NumberFaultyCtrs++;
					 int j=0;
					 while (j<lengthSuspInsts1 && Integer.parseInt(SuspInsts1.get(j))!=Line){
						 j++;  
					 }
					 if (j==lengthSuspInsts1){
						 SuspInsts1.add(line);
					 }
					}
					System.out.println("MIVcard(ctrs,line "+Line+")="+degre);
				}
				else if (Line == -1){
					System.out.println("MIVcard(ctrs,"+CE+")="+degre);
				}else{
					NumberFaultyCtrs++;
					System.out.println("MIVcard(ctrs,POST)="+degre);
				}
			}
		}
		//double ratio=(NumberFaultyCtrs*100.000)/23.000;
		System.out.println("\nThe number of instructions suspected: "+NumberFaultyCtrs);
		//System.out.println("Ratio instructions suspectes / instructions du programme:"+ratio+"%");
		
		
	}
	
}
