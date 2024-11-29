package validation.strategies.cfg.localization;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import validation.Validation;
import validation.system.cfg.CfgCsp;
import validation.util.Type;
import expression.Expression;
import expression.logical.AndExpression;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NotExpression;
import expression.numeric.IntegerLiteral;
import expression.variables.ArrayVariable;
import expression.variables.Variable;
import ilog.concert.IloException;
import CFG.AssertEndWhile;
import CFG.AssertNode;
import CFG.BlockNode;
import CFG.CFG;
import CFG.CFGNode;
import CFG.CFGVisitException;
import CFG.CFGVisitor; 
import CFG.ConditionalNode;
import CFG.EnsuresNode;
import CFG.FunctionCallNode;
import CFG.IfNode;
import CFG.OnTheFlyWhileNode;
import CFG.RequiresNode;
import CFG.RootNode;
import CFG.WhileNode;

import solver.Solver;
import solver.ilocp.cfg.IloCPCfgCsp;

public class DFSdevieVisitor extends RhsExpressionComputer  implements CFGVisitor {

	/**
	 * Values coming from the counterexample (the values ​​of the propagation of the counterexample).
	 */
	protected HashMap<String, String> ceValues;
	
	/**
	 * To store the informations about constraints.
	 */
	protected HashMap<String, CtrInfo> ctrInfos;
	
	public int nbAssignments;
	
	/**
	 * To store the constraints which correspond to instructions of the program.
	 */
	protected CfgCsp csp;
	
	//private CfgCsp cspcp;
	
	public ArrayList<LogicalExpression> arrListInsts = new ArrayList<LogicalExpression>(); 
	
	/**
	 * A number used as a unique identifier to name the constraint in 
	 * the concrete solver.
	 */
	protected int ctrId;
	
	public ArrayList<String> arrListInstsIden = new ArrayList<String>();
	
	/**
	 * All informations used for a constraint.
	 */
	
	public class CtrInfo {
		/**
		 * Source code line number associated with a constraint.
		 * Line number is 0 for a constraint not directly associated to a program statement 
		 * (e.g. specifications or assignments due to DSA form).
		 */
		public int startLine;
		/**
		 * Original expression of a constraint in the source code.
		 */
		public LogicalExpression pgmCtr;
		
		protected CtrInfo(int line, LogicalExpression ctr) {
			startLine = line;
			pgmCtr = ctr;
		}
	}
	
	public int nbNodes;
	
	protected int k;
	
	protected int kc;
	
	protected int nbFaultyCond;
	
	protected String solverName;
	
	ArrayList<String> suspInsts = new ArrayList<String>(); 
	
	protected ArrayList<Integer> suspInstsDF = new ArrayList<Integer>();
	protected ArrayList<Integer> suspInstsQE = new ArrayList<Integer>();
	protected ArrayList<Integer> suspInstsCR = new ArrayList<Integer>();
	
	ArrayList<ArrayList<ArrayList<String>>>  allMCSProgramAss;
	
	ArrayList<ArrayList<Integer>>  allMCSProgramCond;
	
	protected int nbIISCal;
	
	public int getNbIISCal() {
		return this.nbIISCal;
	}

	public void setnbIISCal(int nbIISCal) {
		this.nbIISCal = nbIISCal;
	}

	public ArrayList<Integer> getSuspInstsDF() {
		return suspInstsDF;
	}

	public void setSuspInstsDF(ArrayList<Integer> suspInstsDF) {
		this.suspInstsDF = suspInstsDF;
	}

	public ArrayList<Integer> getSuspInstsQE() {
		return suspInstsQE;
	}

	public void setSuspInstsQE(ArrayList<Integer> suspInstsQE) {
		this.suspInstsQE = suspInstsQE;
	}

	public ArrayList<Integer> getSuspInstsCR() {
		return suspInstsCR;
	}

	public void setSuspInstsCR(ArrayList<Integer> suspInstsCR) {
		this.suspInstsCR = suspInstsCR;
	}
	
	public class TypeCondDeviated{
		public ConditionalNode cond;
		public String branch;
	}
	protected ArrayList<TypeCondDeviated> condDeviated;
	
	protected ArrayList<LogicalExpression> condDeviatedExp;
	
	protected boolean corrige;

	public CFG method;
	
	protected double timeMCS;
	protected double timeIISDF;
	protected double timeIISQE;
	protected double timeIISCR;
	
	protected double globalTimeIIS_DF;
	protected double globalTimeIIS_QE;
	protected double globalTimeIIS_CR;
	
	protected double globalTimeMCS;
	
	private boolean currentCspIsLinear=true;
	
	public double getGlobalTimeIIS_DF() {
		return globalTimeIIS_DF;
	}
	
	public double getGlobalTimeIIS_QE() {
		return globalTimeIIS_QE;
	}
	
	public double getGlobalTimeMCS() {
		return globalTimeMCS;
	}
	
	public double getGlobalTimeIIS_CR() {
		return globalTimeIIS_CR;
	}
	
	public double getTimeMCS() {
		return timeMCS;
	}
	
	public double getTimeIISDF() {
		return timeIISDF;
	}
	
	public double getTimeIISQE() {
		return timeIISQE;
	}
	
	public double getTimeIISCR() {
		return timeIISCR;
	}

	public DFSdevieVisitor() {
		this.csp = null;
	}
	
	public void setCSP(CfgCsp csp) {
		this.csp = csp;
	}
	
	
	
	/**
	 * Explore the paths from the counterexample values given in <code>inputs</code> by a bounded DFS exploration.
	 * 
	 * @param method
	 * @param globalInitializations
	 * @param inputs 
	 * @param inputs
	 * @throws IOException 
	 * @throws CFGVisitException 
	 */
	public HashMap<String, CtrInfo> visitPath(CFG method, ArrayList<Assignment> globalInitializations,HashMap<String, String> inputs,int numberFaultyCondc, int numberFaultyCond,String s) 
	{
		 
		solverName=s;
		allMCSProgramAss = new ArrayList<ArrayList<ArrayList<String>>>();
		allMCSProgramCond = new ArrayList<ArrayList<Integer>>();
		condDeviated=new ArrayList<TypeCondDeviated>();		
	    condDeviatedExp=new ArrayList<LogicalExpression>();
	    nbAssignments = 0;
		nbNodes = 0;
		knownValues = new HashMap<String, Expression>();
		ctrInfos = new HashMap<String, CtrInfo>();
		ctrId= 1;
		timeMCS=0;
		timeIISDF=0;
		timeIISQE=0;
		timeIISCR=0;
		k=numberFaultyCond;
		kc=numberFaultyCondc;
		nbFaultyCond=numberFaultyCond;
		this.ceValues = inputs;
		// Add global variables initializations if any
		if (!globalInitializations.isEmpty()) {
			visitAssignments(globalInitializations);
		}
		
		// Add method parameters from the counterexample to the CSP
		for (Variable param : method.parameters()) {
			paramAssignments(param, method.startLine);
		}
		
		try {
			method.firstNode().accept(this);
		} catch (Exception e) {
			System.err.println("Error (PathVisitor): CFG visit terminated abnormally!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		return this.ctrInfos;
	}
	
	/**
	 * Handles method parameters. These are inputs and should be found in the counterexample values.
	 * 
	 * @param v
	 * @param startLine
	 */
	private void paramAssignments(Variable v, int startLine) {
		if (v instanceof ArrayVariable) { // Deal with arrays
			ArrayVariable array = (ArrayVariable)v;
			for (int i=0; i<array.length(); i++) {
				addCEAssignment(new Variable(array.name() + "[" + i + "]", array.type(), array.use()), -1);
			}
		}
		else { // Deal with scalar variables
			addCEAssignment(v, -1);
		}
	}
	
	/**
	 * Adds a constraint to the CSP and stores associated information (line number, preference).
	 * 
	 * @param ctr
	 * @param startLine
	 * @param pref
	 */
	private void addCtrAndPref(LogicalExpression ctr, int startLine) {		
			/**
			 * Adding instruction (ctr) in the table path instructions
			 */
			arrListInsts.add(ctr);
			/******/
			String ctrName = Integer.toString(ctrId++);
			/**
			 * Adding ID of "ctr" in arrListInstsIden
			 */
			arrListInstsIden.add(ctrName);
			/**
			 * Adding the constraint that corresponds to the instruction to the CSP under construction.
			 */
			csp.addConstraint(ctr, ctrName);
			ctrInfos.put(ctrName, new CtrInfo(startLine, ctr));
			
	}
	
	/**
	 * Adds an assignment constraint for an input variable. The variable's value comes from the counterexample.
	 *  
	 * @param v
	 * @param startLine
	 */
	private void addCEAssignment(Variable v, int startLine) {
		Expression value = Type.getLiteralFromString(ceValues.get(v.name()), v.type());
		if (value != null) {
			knownValues.put(v.name(), value);
			csp.addVar(v);
			addCtrAndPref(new Assignment(v, value), startLine);
		}
		else {
			System.err.println("Error (PathVisitor): incomplete counterexample, variable " + v + " is missing!");
			System.err.println(Thread.currentThread().getStackTrace());
			System.exit(-1);
		}
	}
	
	@Override
	public void visit(RootNode n) throws CFGVisitException, IloException {
		CFGNode firstNode = n.getLeft();
		if (firstNode != null)
			firstNode.accept(this);
		else
			System.err.println("Error (ConstantPropagator): empty CFG!");
	}

	@Override
	public void visit(EnsuresNode n) throws CFGVisitException, IloException {
		nbNodes++;
		
		int size=this.condDeviated.size();
		
		if (size==0 && n.marked==-1){
			
			n.marked=1;
			
			// We explored the path of counterexample.		
			addCtrAndPref(n.getCondition(), -2);
			
			PrintAddMCSCondition();
			
			// we calculate MCS in the full path of counterexample
			if (solverName=="CPLEX"){
				
				CplexAllMCSlowerK allmcslowerk = new CplexAllMCSlowerK(csp,ctrInfos,k,suspInsts);
			
				// We add MCS computed
				allMCSProgramAss.add(allmcslowerk.arrListMCSes);
				suspInsts.clear();
				int lengthSuspInsts1=allmcslowerk.SuspInsts1.size();
				for (int i=0;i<lengthSuspInsts1;i++){
					String elts = allmcslowerk.SuspInsts1.get(i);
					suspInsts.add(elts);
				}
				suspInstsDF.addAll(allmcslowerk.getSuspInstsDF());
				suspInstsQE.addAll(allmcslowerk.getSuspInstsQE());
				suspInstsCR.addAll(allmcslowerk.getSuspInstsCR());
				
				// For summing the MCS computation time during exploration
				timeMCS=timeMCS+allmcslowerk.getTimeMCS();
				timeIISDF=timeIISDF+allmcslowerk.getTimeIIS_DF();
				timeIISQE=timeIISQE+allmcslowerk.getTimeIIS_QE();
				timeIISCR=timeIISCR+allmcslowerk.getTimeIIS_CR();
				globalTimeIIS_DF = globalTimeIIS_DF + allmcslowerk.getTimeIIS_DF();
				globalTimeIIS_QE = globalTimeIIS_QE + allmcslowerk.getTimeIIS_QE();
				globalTimeIIS_CR = globalTimeIIS_CR + allmcslowerk.getTimeIIS_CR();
				globalTimeMCS = globalTimeMCS + allmcslowerk.getTimeMCS();
				nbIISCal = nbIISCal + allmcslowerk.getNbIISCal();
				
			}
			else if (solverName=="CP OPTIMIZER"){
				CPOPTIMIZERAllMCSlowerK allmcslowerk = new CPOPTIMIZERAllMCSlowerK(csp,ctrInfos,k,suspInsts);
				// We add MCS computed
				allMCSProgramAss.add(allmcslowerk.arrListMCSes);
			    
				suspInsts.clear();
				int lengthSuspInsts1=allmcslowerk.SuspInsts1.size();
				for (int i=0;i<lengthSuspInsts1;i++){
					String elts = allmcslowerk.SuspInsts1.get(i);
					suspInsts.add(elts);
				}
				suspInstsDF.addAll(allmcslowerk.getSuspInstsDF());
				suspInstsQE.addAll(allmcslowerk.getSuspInstsQE());
				suspInstsCR.addAll(allmcslowerk.getSuspInstsCR());
				// For summing the MCS computation time during exploration
				timeMCS=timeMCS+allmcslowerk.getTimeMCS();
				timeIISDF=timeIISDF+allmcslowerk.getTimeIIS_DF();
				timeIISQE=timeIISQE+allmcslowerk.getTimeIIS_QE();
				timeIISCR=timeIISCR+allmcslowerk.getTimeIIS_CR();
				globalTimeIIS_DF = globalTimeIIS_DF + allmcslowerk.getTimeIIS_DF();
				globalTimeIIS_QE = globalTimeIIS_QE + allmcslowerk.getTimeIIS_QE();
				globalTimeIIS_CR = globalTimeIIS_CR + allmcslowerk.getTimeIIS_CR();
				globalTimeMCS = globalTimeMCS + allmcslowerk.getTimeMCS();
				
				nbIISCal = nbIISCal + allmcslowerk.getNbIISCal();
			}
		}
	}

	@Override
	public void visit(RequiresNode n) throws CFGVisitException, IloException {
		nbNodes++;		
		// Cannot be a junction node
		n.getLeft().accept(this);
	}

	@Override
	public void visit(AssertNode n) throws CFGVisitException, IloException {
		LogicalLiteral cond = (LogicalLiteral)n.getCondition().structAccept(this);
		if (!cond.constantBoolean()) {  // this is the first failing assertion on this path
			// Add it to the CSP and cut rest of the path
			addCtrAndPref(n.getCondition(), n.startLine);
		}
		else {  // Ignore the assertion and go on visiting the faulty path
			n.getLeft().accept(this);
		}
	}

	@Override
	public void visit(AssertEndWhile n) throws CFGVisitException, IloException {
		n.getLeft().accept(this);
	}
   
	
	private void visitBranchingNode(ConditionalNode branchingNode, int kc) throws CFGVisitException, IloException {
		nbNodes++;
		LogicalLiteral cond = (LogicalLiteral)branchingNode.getCondition().structAccept(this);
		
		// We save the context of the CSP
		csp.save();
		// We save the number of the number of conditions deviatedOn sauvgarde le nombre de conditions à dévier à partir de la condition.
		//deque.push(k);
		TypeCondDeviated cd = new TypeCondDeviated();
		if (cond.constantBoolean()) {
			// Follow 'Then' branch
			if (/*this.k==1*/this.kc==nbFaultyCond-1 && (this.kc+1<branchingNode.marked || branchingNode.marked==-1)){
				corrige=Correct(branchingNode.getRight());				
				if (corrige){
					
					// we mark the node
					branchingNode.marked=this.kc+1;
					
					NotExpression Exp = new NotExpression(branchingNode.getCondition());
					cd.cond=branchingNode;
					cd.branch="If";
					this.condDeviated.add(cd);
					this.condDeviatedExp.add(Exp);
					
					//Add and print MCS of condition computed. 
					allMCSProgramCond.add(PrintAddMCSCondition());
					
					AddNegationsConditions();
					
					if (solverName=="CPLEX"){
						CplexAllMCSlowerK allmcslowerk = new CplexAllMCSlowerK(csp,ctrInfos,k,suspInsts);					
					
						this.condDeviated.remove(cd);
						this.condDeviatedExp.remove(Exp);
					
						allMCSProgramAss.add(allmcslowerk.arrListMCSes);
						suspInsts.clear();
					     int lengthSuspInsts1=allmcslowerk.SuspInsts1.size();
				         for (int i=0;i<lengthSuspInsts1;i++){
				         String elts = allmcslowerk.SuspInsts1.get(i);
				     	 suspInsts.add(elts);
			    	    }
				        suspInstsDF.addAll(allmcslowerk.getSuspInstsDF());
						suspInstsQE.addAll(allmcslowerk.getSuspInstsQE());
						suspInstsCR.addAll(allmcslowerk.getSuspInstsCR()); 
						// For summing the MCS computation time during exploration
						timeMCS=timeMCS+allmcslowerk.getTimeMCS();
						timeIISDF=timeIISDF+allmcslowerk.getTimeIIS_DF();
						timeIISQE=timeIISQE+allmcslowerk.getTimeIIS_QE();
						timeIISCR=timeIISCR+allmcslowerk.getTimeIIS_CR();
						globalTimeIIS_DF = globalTimeIIS_DF + allmcslowerk.getTimeIIS_DF();
						globalTimeIIS_QE = globalTimeIIS_QE + allmcslowerk.getTimeIIS_QE();
						globalTimeIIS_CR = globalTimeIIS_CR + allmcslowerk.getTimeIIS_CR();
						globalTimeMCS = globalTimeMCS + allmcslowerk.getTimeMCS();
						nbIISCal = nbIISCal + allmcslowerk.getNbIISCal();
					}else if (solverName=="CP OPTIMIZER"){
						CPOPTIMIZERAllMCSlowerK allmcslowerk = new CPOPTIMIZERAllMCSlowerK(csp,ctrInfos,k,suspInsts);
						this.condDeviated.remove(cd);
						this.condDeviatedExp.remove(Exp);
					
						allMCSProgramAss.add(allmcslowerk.arrListMCSes);
						suspInsts.clear();
					     int lengthSuspInsts1=allmcslowerk.SuspInsts1.size();
				         for (int i=0;i<lengthSuspInsts1;i++){
				         String elts = allmcslowerk.SuspInsts1.get(i);
				     	 suspInsts.add(elts);
			    	    }
				        suspInstsDF.addAll(allmcslowerk.getSuspInstsDF());
						suspInstsQE.addAll(allmcslowerk.getSuspInstsQE());
						suspInstsCR.addAll(allmcslowerk.getSuspInstsCR());
						// For summing the MCS computation time during exploration
				        timeMCS=timeMCS+allmcslowerk.getTimeMCS();
						timeIISDF=timeIISDF+allmcslowerk.getTimeIIS_DF();
						timeIISQE=timeIISQE+allmcslowerk.getTimeIIS_QE();
						timeIISCR=timeIISCR+allmcslowerk.getTimeIIS_CR();
						globalTimeIIS_DF = globalTimeIIS_DF + allmcslowerk.getTimeIIS_DF();
						globalTimeIIS_QE = globalTimeIIS_QE + allmcslowerk.getTimeIIS_QE();
						globalTimeIIS_CR = globalTimeIIS_CR + allmcslowerk.getTimeIIS_CR();
						globalTimeMCS = globalTimeMCS + allmcslowerk.getTimeMCS();
						
						nbIISCal = nbIISCal + allmcslowerk.getNbIISCal();
					}
					
				}
			}else if (/*this.k>1*/this.kc<nbFaultyCond-1 && (this.kc+1<branchingNode.marked || branchingNode.marked==-1)){
				this.kc=kc+1;
				cd.cond=branchingNode;
				cd.branch="If";
				this.condDeviated.add(cd);
				this.condDeviatedExp.add(new NotExpression(branchingNode.getCondition()));
				branchingNode.getRight().accept(this);
			}
			this.kc=kc;
			this.condDeviated.remove(cd);
			this.condDeviatedExp.remove(new NotExpression(branchingNode.getCondition()));
			csp.restore();
			branchingNode.getLeft().accept(this);
			
		}
		else {
			// Follow 'Else' branch
			if (/*this.k==1*/this.kc==nbFaultyCond-1 && (this.kc+1<branchingNode.marked || branchingNode.marked==-1)){
				corrige=Correct(branchingNode.getLeft());				
				if (corrige){
					
				    // we mark the node
					branchingNode.marked=this.kc+1;
					
					cd.cond=branchingNode;
					cd.branch="Else";
					this.condDeviated.add(cd);
					this.condDeviatedExp.add(branchingNode.getCondition());					
					
					// Add and print MCS of condition computed. 
					allMCSProgramCond.add(PrintAddMCSCondition());
					
					AddNegationsConditions(); 
					if (solverName=="CPLEX"){					
						CplexAllMCSlowerK allmcslowerk = new CplexAllMCSlowerK(csp,ctrInfos,k,suspInsts);
						
						this.condDeviated.remove(cd);
						this.condDeviatedExp.remove(branchingNode.getCondition());
					
						allMCSProgramAss.add(allmcslowerk.arrListMCSes);
						suspInsts.clear();
					    int lengthSuspInsts1=allmcslowerk.SuspInsts1.size();
				        for (int i=0;i<lengthSuspInsts1;i++){
				       	  String elts = allmcslowerk.SuspInsts1.get(i);
				    	  suspInsts.add(elts);
			    	    }
				        suspInstsDF.addAll(allmcslowerk.getSuspInstsDF());
						suspInstsQE.addAll(allmcslowerk.getSuspInstsQE());
						suspInstsCR.addAll(allmcslowerk.getSuspInstsCR());
						// For summing the MCS computation time during exploration
						timeMCS=timeMCS+allmcslowerk.getTimeMCS();
						timeIISDF=timeIISDF+allmcslowerk.getTimeIIS_DF();
						timeIISQE=timeIISQE+allmcslowerk.getTimeIIS_QE();
						timeIISCR=timeIISCR+allmcslowerk.getTimeIIS_CR();
						globalTimeIIS_DF = globalTimeIIS_DF + allmcslowerk.getTimeIIS_DF();
						globalTimeIIS_QE = globalTimeIIS_QE + allmcslowerk.getTimeIIS_QE();
						globalTimeIIS_CR = globalTimeIIS_CR + allmcslowerk.getTimeIIS_CR();
						globalTimeMCS = globalTimeMCS + allmcslowerk.getTimeMCS();
						
						nbIISCal = nbIISCal + allmcslowerk.getNbIISCal();
					}else if (solverName=="CP OPTIMIZER"){
						CPOPTIMIZERAllMCSlowerK allmcslowerk = new CPOPTIMIZERAllMCSlowerK(csp,ctrInfos,k,suspInsts);
						this.condDeviated.remove(cd);
						this.condDeviatedExp.remove(branchingNode.getCondition());
					
						allMCSProgramAss.add(allmcslowerk.arrListMCSes);
						suspInsts.clear();
					    int lengthSuspInsts1=allmcslowerk.SuspInsts1.size();
				        for (int i=0;i<lengthSuspInsts1;i++){
				       	 String elts = allmcslowerk.SuspInsts1.get(i);
				    	 suspInsts.add(elts);
			    	    }
				        suspInstsDF.addAll(allmcslowerk.getSuspInstsDF());
						suspInstsQE.addAll(allmcslowerk.getSuspInstsQE());
						suspInstsCR.addAll(allmcslowerk.getSuspInstsCR());
						// For summing the MCS computation time during exploration
				        timeMCS=timeMCS+allmcslowerk.getTimeMCS();
						timeIISDF=timeIISDF+allmcslowerk.getTimeIIS_DF();
						timeIISQE=timeIISQE+allmcslowerk.getTimeIIS_QE();
						timeIISCR=timeIISCR+allmcslowerk.getTimeIIS_CR();
						globalTimeIIS_DF = globalTimeIIS_DF + allmcslowerk.getTimeIIS_DF();
						globalTimeIIS_QE = globalTimeIIS_QE + allmcslowerk.getTimeIIS_QE();
						globalTimeIIS_CR = globalTimeIIS_CR + allmcslowerk.getTimeIIS_CR();
						globalTimeMCS = globalTimeMCS + allmcslowerk.getTimeMCS();
						
						nbIISCal = nbIISCal + allmcslowerk.getNbIISCal();
					}
				}
			}else if (/*this.k>1*/this.kc<nbFaultyCond-1 && (this.kc+1<branchingNode.marked || branchingNode.marked==-1)){
				this.kc=kc+1;
				cd.cond=branchingNode;
				cd.branch="Else";
				this.condDeviated.add(cd);
				this.condDeviatedExp.add(branchingNode.getCondition());
				branchingNode.getLeft().accept(this);
			}					
			this.kc=kc;
			this.condDeviated.remove(cd);
			this.condDeviatedExp.remove(branchingNode.getCondition());
			csp.restore();
			branchingNode.getRight().accept(this);			
		}
	}
	
	
	
	protected ArrayList<Integer> PrintAddMCSCondition() {
		ArrayList<Integer> arrListMCS = new ArrayList<Integer>();		
		int size=this.condDeviated.size();
		System.out.println("\nSolver: "+solverName);
		if (size!=0){
			System.out.println("1. CSP_d:");
		}
		else{
			System.out.println("1. CSP_d: empty set");
		}
		Iterator<TypeCondDeviated> iter=condDeviated.iterator();
		while (iter.hasNext()){
			TypeCondDeviated next=iter.next();
			
			System.out.println("line "+Integer.toString(next.cond.startLine)+"("+next.branch+")"+" : "+next.cond.getCondition());
			int line = next.cond.startLine;
			arrListMCS.add(line);
			if (!suspInsts.contains(Integer.toString(line))) {
				suspInsts.add(Integer.toString(line));
				suspInstsDF.add(line);
				suspInstsQE.add(line);
				suspInstsCR.add(line);
			}	
		}
		return arrListMCS;
	}

	private void AddNegationsConditions() {
		Iterator<LogicalExpression> iter1=this.condDeviatedExp.iterator();
		//LogicalExpression Exp = this.CondDeviatedExp.get(0);
		//iter1.next();
		while (iter1.hasNext()){
			//Exp = new AndExpression(Exp,iter1.next());
			//addCtrAndPref(Exp, -2);
			addCtrAndPref(iter1.next(), -2);
		}
		//addCtrAndPref(Exp, -2);
		//addCtrAndPref(this.CondDeviatedExp.get(this.CondDeviatedExp.size()-1), -2);
	}
	
	@Override
	public void visit(IfNode n) throws CFGVisitException, IloException {	
		visitBranchingNode(n,this.kc);
	}

	@Override
	public void visit(WhileNode n) throws CFGVisitException, IloException {
		visitBranchingNode(n,this.kc);		
	}
	
	private void visitAssignments(ArrayList<Assignment> block) {
		String ceValue;
		Expression rhs;
		IntegerLiteral index;
		Assignment ass;
		ArrayAssignment arrayAss;
		// the set of assignments is treated from the first one to the last one
		// to correctly handle SSA renaming
		ListIterator<Assignment> blockItr = block.listIterator();
		while (blockItr.hasNext()) {
			ass = blockItr.next();
			rhs = ass.rhs();
			if (rhs == null) { // non-det assignment are in the counterexample
				addCEAssignment(ass.lhs(), ass.startLine());
			}
			else {
				if (ass.rhs() instanceof ArrayVariable) { // array copy
					ArrayVariable arrayDest = (ArrayVariable)ass.lhs();
					for (int i=0; i<arrayDest.length(); i++) {
						knownValues.put(arrayDest.name() + '[' + i + ']', 
								        knownValues.get(((ArrayVariable)ass.rhs()).name() + '[' + i + ']'));
					}
					csp.addArrayVar(arrayDest);
					addCtrAndPref(ass, ass.startLine());
				}
				else {
					// We first try to see if the value of this variable is part of the counterexample.
					// This is necessarily the case for non-det assignments, but we may also have
					// all the program variable values in the counterexample (and not just the inputs)
					ceValue = ceValues.get(ass.lhs().name());
					//if (ceValue != null) {  
						//rhs = Type.getLiteralFromString(ceValue, ass.lhs().type());
					//}
					//else {  // This non-input variable is not in the counterexample
						// Propagate constants and simplify rhs if possible
						rhs = (Expression)rhs.structAccept(this);
					//}

					if (ass instanceof ArrayAssignment) {
						arrayAss = (ArrayAssignment)ass;
						index = (IntegerLiteral)arrayAss.index().structAccept(this);
						for (int i=0; i<arrayAss.array().length(); i++) {
							if (i == index.constantNumber().intValue())
								knownValues.put(arrayAss.array().name() + '[' + i + ']', rhs);
							else
								knownValues.put(arrayAss.array().name() + '[' + i + ']', 
										        knownValues.get(arrayAss.previousArray().name() + '[' + i + ']'));
						}
						csp.addArrayVar(arrayAss.array());
						addCtrAndPref(arrayAss, arrayAss.startLine());
					}
					else {  // not an array assignment
						csp.addVar(ass.lhs());
						knownValues.put(ass.lhs().name(), rhs);
						addCtrAndPref(ass, ass.startLine());
					}
				}
			}
		}
		nbAssignments += block.size();
	}

	
	@Override
	public void visit(BlockNode n) throws CFGVisitException, IloException {
		nbNodes++;
		visitAssignments(n.getBlock());
		n.getLeft().accept(this);
	}

	@Override
	public void visit(FunctionCallNode n) throws CFGVisitException, IloException {
		nbNodes++;
		// add variables in parameter passing assignments 
		visitAssignments(n.getParameterPassing().getBlock());
		// add global variables which are used in the function
		visitAssignments(n.getLocalFromGlobal().getBlock());

		// visit the callee
		n.getCFG().firstNode().accept(this);
		
		// go on with the visit of the caller
		n.getLeft().accept(this);
	}

	@Override
	public void visit(OnTheFlyWhileNode whileNode) throws CFGVisitException {
		
	}
	
	private boolean Correct(CFGNode n) throws CFGVisitException, IloException{
		if (n instanceof EnsuresNode){
			LogicalLiteral cond = (LogicalLiteral)((ConditionalNode) n).getCondition().structAccept(this);
			return cond.constantBoolean();
			
		}else if (n instanceof IfNode || n instanceof WhileNode) {
			LogicalLiteral cond = (LogicalLiteral)((ConditionalNode) n).getCondition().structAccept(this);
			if (cond.constantBoolean()) {
				// Follow 'Then' branch
				return Correct(n.getLeft());
			}
			else {
				// Follow 'Else' branch
				return Correct(n.getRight());
			}
		}else if (n instanceof AssertEndWhile){
			return Correct(n.getLeft());
		}
		else if (n instanceof BlockNode) {
			propagate(((BlockNode) n).getBlock());
			return Correct(n.getLeft());
		}
		return corrige;
		
		
	}
	
	private void propagate(ArrayList<Assignment> block){
		String ceValue;
		Expression rhs;
		IntegerLiteral index;
		Assignment ass;
		ArrayAssignment arrayAss;
		// the set of assignments is treated from the first one to the last one
		// to correctly handle SSA renaming
		ListIterator<Assignment> blockItr = block.listIterator();
		while (blockItr.hasNext()) {
			ass = blockItr.next();
			rhs = ass.rhs();
			if (rhs == null) { // non-det assignment are in the counterexample
				addCEAss(ass.lhs(), ass.startLine());
			}
			else {
				if (ass.rhs() instanceof ArrayVariable) { // array copy
					ArrayVariable arrayDest = (ArrayVariable)ass.lhs();
					for (int i=0; i<arrayDest.length(); i++) {
						knownValues.put(arrayDest.name() + '[' + i + ']', 
								        knownValues.get(((ArrayVariable)ass.rhs()).name() + '[' + i + ']'));
					}
				}
				else {
					// We first try to see if the value of this variable is part of the counterexample.
					// This is necessarily the case for non-det assignments, but we may also have
					// all the program variable values in the counterexample (and not just the inputs)
					ceValue = ceValues.get(ass.lhs().name());
					//if (ceValue != null) {  
						//rhs = Type.getLiteralFromString(ceValue, ass.lhs().type());
					//}
					//else {  // This non-input variable is not in the counterexample
						// Propagate constants and simplify rhs if possible
						rhs = (Expression)rhs.structAccept(this);
					//}

					if (ass instanceof ArrayAssignment) {
						arrayAss = (ArrayAssignment)ass;
						index = (IntegerLiteral)arrayAss.index().structAccept(this);
						for (int i=0; i<arrayAss.array().length(); i++) {
							if (i == index.constantNumber().intValue())
								knownValues.put(arrayAss.array().name() + '[' + i + ']', rhs);
							else
								knownValues.put(arrayAss.array().name() + '[' + i + ']', 
										        knownValues.get(arrayAss.previousArray().name() + '[' + i + ']'));
						}
					}
					else {  // not an array assignment
						knownValues.put(ass.lhs().name(), rhs);
					}
				}
			}
		}
		nbAssignments += block.size();
		
	}
	
	private void addCEAss(Variable v, int startLine) {
		Expression value = Type.getLiteralFromString(ceValues.get(v.name()), v.type());
		if (value != null) {
			knownValues.put(v.name(), value);
		}
		else {
			System.err.println("Error (PathVisitor): incomplete counterexample, variable " + v + " is missing!");
			System.err.println(Thread.currentThread().getStackTrace());
			System.exit(-1);
		}
	}

}
