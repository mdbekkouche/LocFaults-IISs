package validation.strategies.cfg.localization;

import ilog.concert.IloAddable;
import ilog.concert.IloConstraint;
import ilog.concert.IloCumulFunctionExpr;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloObjective;
import ilog.cp.IloCP;
import ilog.cp.IloCP.ConflictStatus;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import solver.cplex.Cplex;
import solver.ilocp.IloCPSolver;
import validation.strategies.cfg.localization.DFSdevieVisitor.CtrInfo;
import validation.system.cfg.CfgCsp;

public class CPOPTIMIZERAllMCSlowerK {
	protected IloCP solver;
	protected HashMap<String, CtrInfo> ctrInfos;
	private double timeMCS;
	public IloIntVar[] x;
	public IloIntVar[] nonx;
	public ArrayList<ArrayList<String>> arrListMCSes;
	
	private ArrayList<Integer> suspInstsLiffiton = new ArrayList<Integer>();
	private ArrayList<Integer> suspInstsDF = new ArrayList<Integer>();
	private ArrayList<Integer> suspInstsQE = new ArrayList<Integer>();
	private ArrayList<Integer> suspInstsCR = new ArrayList<Integer>();
	
	public ArrayList<String> SuspInsts1 = new ArrayList<String>();
	
	private double timeIIS_DF;
	private double timeIIS_QE;
	private double timeIIS_CR;
	
	private int NbIISCal;
		
	public int getNbIISCal() {
		return NbIISCal;
	}

	public void setNbIISCal(int nbIISCal) {
		NbIISCal = nbIISCal;
	}

	public ArrayList<Integer> getSuspInstsLiffiton() {
		return suspInstsLiffiton;
	}
	
	public ArrayList<Integer> getSuspInstsDF() {
		return suspInstsDF;
	}
	
	public ArrayList<Integer> getSuspInstsQE() {
		return suspInstsQE;
	}
	
	public ArrayList<Integer> getSuspInstsCR() {
		return suspInstsCR;
	}
	
	public double getTimeIIS_DF() {
		return timeIIS_DF;
	}
	
	public double getTimeIIS_QE() {
		return timeIIS_QE;
	}
	
	public double getTimeMCS() {
		return timeMCS;
	}
	
	public double getTimeIIS_CR() {
		return timeIIS_CR;
	}
	
	public CPOPTIMIZERAllMCSlowerK(CfgCsp csp, HashMap<String, CtrInfo> ctrInfos1, int numberFaultyCond, ArrayList<String> suspInsts) throws IloException{
		NbIISCal=0;
		for(int j=0;j<suspInsts.size();j++){
			SuspInsts1.add(suspInsts.get(j));
		}
		
		solver=((IloCPSolver)csp.solver).solver();
		
		//solver.setParameter(IloCP.IntParam., 1000);
		
		//solver.setParameter(IloCP.IntParam.SearchType, IloCP.ParameterValues.); // Search strategy
		
		ctrInfos = ctrInfos1;
		ArrayList<IloAddable> ctrsList = new ArrayList<IloAddable>();
		Iterator<?> iter = solver.iterator();
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
		timeMCS=time1-time;
		System.out.println("\nRuntime of the method that compute MCS: "+(time1-time)/1000.0);
		
		CalculateDegreeSuspicion(arrListMCSes,ctrs);
		
		/**********************/
		
		/*********/
		
		System.out.println("   IIS in CSP_a using Deletion Filter:");
		time = Calendar.getInstance().getTimeInMillis();
		ArrayList<String> IISs= DeletionFilter(ctrs);
		time1 = Calendar.getInstance().getTimeInMillis();
		timeIIS_DF = time1-time;
		System.out.println("\nRuntime of the method that compute IIS using Deletion Filter: "+(time1-time)/1000.0);
		
		/********/
		
		System.out.println("   IIS in CSP_a using QuickExplain:");
		time = Calendar.getInstance().getTimeInMillis();
		IloConstraint[] B = OptainHardCtrs(ctrs);
		IloConstraint[] C = OptainSoftCtrs(ctrs);
		System.out.println("Length of the set of soft constraints : "+ C.length);
		System.out.print("{CE");
		IloConstraint[] IISQE= QuickExplain(B,B,C);
		System.out.println(",POST}");
		time1 = Calendar.getInstance().getTimeInMillis();
		timeIIS_QE = time1-time;
		System.out.println("\nRuntime of the method that compute IIS using QuickExplain: "+(time1-time)/1000.0);
		
		if (IISQE.length != 0) {
			NbIISCal++;
		}
		
		/*******/
		System.out.println("   IIS in CSP_a using the conflict refiner implementation of CP Optimizer:");
		time = Calendar.getInstance().getTimeInMillis();
		IloConstraint[] IISCR= ConflictRefiner(ctrs);
		time1 = Calendar.getInstance().getTimeInMillis();
		timeIIS_CR = time1-time;
		System.out.println("\nRuntime of the method that compute IIS using the conflict refiner implementation of CP Optimizer: "+(time1-time)/1000.0);
		/*******/
		
		
		iter = solver.iterator();
		List<IloConstraint> list =new ArrayList<IloConstraint>();
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof IloConstraint) {
				list.add((IloConstraint) next);
			}
		}
		int cmp=0;
		while (cmp!=list.size()) {
			IloConstraint next = list.get(cmp);
			solver.remove((IloConstraint)next);
			cmp++;
		}
    	/*********************/
		 solver.add(ctrs_copie);
		}
		
	
	}
	
	private IloConstraint[] ConflictRefiner(IloConstraint[] ctrs) throws IloException {
		/**********************/
		Iterator<?> iter = solver.iterator();
		List<IloConstraint> list =new ArrayList<IloConstraint>();
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof IloConstraint) {
				list.add((IloConstraint) next);
			}
		}
		int cmp=0;
		while (cmp!=list.size()) {
			IloConstraint next = list.get(cmp);
			solver.remove((IloConstraint)next);
			cmp++;
		}
		/**********************/
		solver.add(ctrs);
		
		double prefs[] = new double[ctrs.length];
		for (int i = 0; i < ctrs.length; i++) {
			int Line = ctrInfos.get(ctrs[i].getName()).startLine;
    		if (Line!=-1 && Line!=-2)
    			prefs[i]=1;
    		else 
    			prefs[i]=0;
		}
		//Arrays.fill(prefs, 1);
		System.out.print("{CE");
		
		if (solver.refineConflict(ctrs, prefs)) {
            for (int i = 0; i < ctrs.length; i++) {
            	ConflictStatus cStat = solver.getConflict(ctrs[i]);
            	if (cStat == ConflictStatus.ConflictMember || cStat == ConflictStatus.ConflictPossibleMember) {
            		int Line = ctrInfos.get(ctrs[i].getName()).startLine;
            		if (Line!=-1 && Line!=-2) {
            			System.out.print(",line "+Line);
            			if (!suspInstsCR.contains(Line)) {
    						suspInstsCR.add(Line);
    					}
            		}	
            	}
			}
        }
		System.out.println(",POST}");
		return null;
	}
	
	private IloConstraint[] QuickExplain(IloConstraint[] b, IloConstraint[] b2, IloConstraint[] c) throws IloException {
		/**********************/
		Iterator<?> iter = solver.iterator();
		List<IloConstraint> list =new ArrayList<IloConstraint>();
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof IloConstraint) {
				list.add((IloConstraint) next);
			}
		}
		int cmp=0;
		while (cmp!=list.size()) {
			IloConstraint next = list.get(cmp);
			solver.remove((IloConstraint)next);
			cmp++;
		}
		/**********************/
		solver.add(b);

		if (b2.length!=0 && !solver.solve()) {
			return new IloConstraint[0];
		}
		if (c.length==1) {
			int Line = ctrInfos.get(c[0].getName()).startLine;
			System.out.print(",line "+Line);
			if (!suspInstsQE.contains(Line)) {
				suspInstsQE.add(Line);
			}
			return new IloConstraint[] {c[0]};  
		}
		int midpoint = c.length / 2;

        // Create the first half of the array
        IloConstraint[] c1 = java.util.Arrays.copyOfRange(c, 0, midpoint);

        // Create the second half of the array
        IloConstraint[] c2 = java.util.Arrays.copyOfRange(c, midpoint, c.length);
        
		// Créez un nouveau tableau pour contenir la concaténation de c1 et c2
        IloConstraint[] bc1 = new IloConstraint[b.length + c1.length];

        // Copiez les éléments de c1 dans c3
        System.arraycopy(b, 0, bc1, 0, b.length);

        // Copiez les éléments de c2 dans c3
        System.arraycopy(c1, 0, bc1, b.length, c1.length);
		
		
		IloConstraint[] b3 = QuickExplain(bc1,c1,c2);
		// Créez un nouveau tableau pour contenir la concaténation de c1 et c2
        IloConstraint[] bb3 = new IloConstraint[b.length + b3.length];

        // Copiez les éléments de c1 dans c3
        System.arraycopy(b, 0, bb3, 0, b.length);

        // Copiez les éléments de c2 dans c3
        System.arraycopy(b3, 0, bb3, b.length, b3.length);
        
		IloConstraint[] b4 = QuickExplain(bb3,b3,c1);
		
		// Créez un nouveau tableau pour contenir la concaténation de c1 et c2
        IloConstraint[] b4b3 = new IloConstraint[b4.length + b3.length];

        // Copiez les éléments de c1 dans c3
        System.arraycopy(b4, 0, b4b3, 0, b4.length);

        // Copiez les éléments de c2 dans c3
        System.arraycopy(b3, 0, b4b3, b4.length, b3.length);
        
        return b4b3;
	}
	
	private IloConstraint[] OptainSoftCtrs(IloConstraint[] ctrs) {
		ArrayList<IloConstraint> softConstraints = new ArrayList<IloConstraint>();
		for (int j=0;j<ctrs.length;j++){
			int Line = ctrInfos.get(ctrs[j].getName()).startLine;
			if (Line != -1 && Line != -2) {
				softConstraints.add(ctrs[j]);
			}
		}
		IloConstraint[] C = new IloConstraint[softConstraints.size()];
		for (int i = 0; i < softConstraints.size(); i++) {
			C[i] = softConstraints.get(i);
		}
		
		return C;
	}

	private IloConstraint[] OptainHardCtrs(IloConstraint[] ctrs) {
		ArrayList<IloConstraint> hardConstraints = new ArrayList<IloConstraint>();
		for (int j=0;j<ctrs.length;j++){
			int Line = ctrInfos.get(ctrs[j].getName()).startLine;
			if (Line == -1 || Line == -2) {
				hardConstraints.add(ctrs[j]);
			}
		}
		IloConstraint[] H = new IloConstraint[hardConstraints.size()];
		for (int i = 0; i < hardConstraints.size(); i++) {
			H[i] = hardConstraints.get(i);
		}
		
		return H;
	}

	
	private ArrayList<String> DeletionFilter(IloConstraint[] ctrs) throws IloException {
		
		
		/**********************/
		Iterator<?> iter = solver.iterator();
		List<IloConstraint> list =new ArrayList<IloConstraint>();
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof IloConstraint) {
				list.add((IloConstraint) next);
			}
		}
		int cmp=0;
		while (cmp!=list.size()) {
			IloConstraint next = list.get(cmp);
			solver.remove((IloConstraint)next);
			cmp++;
		}
    	/*********************/
		solver.add(ctrs);
		int i=0;
		System.out.print("{CE");i++;
		for (int j=0;j<ctrs.length;j++){
			int Line = ctrInfos.get(ctrs[j].getName()).startLine;
			if (Line!=-1 && Line!=-2) {
				solver.remove(ctrs[j]);
				if (solver.solve()) {
					
					solver.add(ctrs[j]);
					if (i==0) {
						System.out.print("{line "+Line/*+"("+ctrInfos.get(ctrs[c].getName()).pgmCtr.toString()+")"+"."+IdenInstWhile*/); 
					} else {
						System.out.print(",line "+Line/*+"("+ctrInfos.get(ctrs[c].getName()).pgmCtr.toString()+")"+"."+IdenInstWhile*/);
					}
					if (!suspInstsDF.contains(Line)) {
						suspInstsDF.add(Line);
					}
					i++;
				} 
			} 
		}
		System.out.println(",POST}");
		/*
		int i=0;
		iter = solver.iterator();
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof IloConstraint) {
			  int Line = ctrInfos.get(((IloConstraint) next).getName()).startLine;
			  String CE = ctrInfos.get(((IloConstraint) next).getName()).pgmCtr.toString();
			  if (i==0) {
				  if (Line !=-1 && Line != -2){
		  			  System.out.print("{line "+Line);   	            			  
		  		  }else if (Line == -1) {
		  			  System.out.print("{"+CE); 
		  		  }else {
		  			  System.out.print("{POST");
		  		  }
			  } else {
				  if (Line != -1 && Line != -2){
        			  System.out.print(",line "+Line);
        		  }
        		  else if (Line == -1) {
        			  System.out.print(","+CE); 
        		  }
        		  else {
        			  System.out.print(",POST");
        		  }
			  }
			}
			i++;
		}
		System.out.println("}");
		*/
		return null;
	}

	private void CalculateDegreeSuspicion(ArrayList<ArrayList<String>> arrListMCSes, IloConstraint[] ctrs) {
		//System.out.println("Le calcule du degré de suspicion (MIVc) de chaque instruction :");
		
		int NumberFaultyCtrs=0;
		for (int i=0;i<ctrs.length;i++){
			double degre=0.000;
			for (int j=0;j<arrListMCSes.size();j++){
				
				 int k=0;
				 while (k<arrListMCSes.get(j).size() && !arrListMCSes.get(j).get(k).equals(ctrs[i].getName())){
					 k++;
				 }
				 if (k!=arrListMCSes.get(j).size() && arrListMCSes.get(j).get(k).equals(ctrs[i].getName())){
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
	
	private ArrayList<ArrayList<String>> MCSesLiffiton(IloConstraint[] ctrs) throws IloException{
		int i=0;
		
	 	/**********************/
		Iterator<?> iter = solver.iterator();
		List<IloConstraint> list =new ArrayList<IloConstraint>();
		while (iter.hasNext()) {
			Object next = iter.next();
			if (next instanceof IloConstraint) {
				list.add((IloConstraint) next);
			}
		}
		int cmp=0;
		while (cmp!=list.size()) {
			IloConstraint next = list.get(cmp);
			solver.remove((IloConstraint)next);
			cmp++;
		}
    	/*********************/
		
		int nbCtrsCEPOST=AddYVars(ctrs);
		
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
    	            			  System.out.print("{line "+Line/*+"("+ctrInfos.get(ctrs[c].getName()).pgmCtr.toString()+")"+"."+IdenInstWhile*/);  
    	            			  if (!suspInstsLiffiton.contains(Line)) {
    	            				  suspInstsLiffiton.add(Line);
    	            			  }
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
    	            			  System.out.print(",line "+Line/*+"("+ctrInfos.get(ctrs[c].getName()).pgmCtr.toString()+")"/*+"."+IdenInstWhile*/);
    	            			  if (!suspInstsLiffiton.contains(Line)) {
    	            				  suspInstsLiffiton.add(Line);
    	            			  }
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

}

	
