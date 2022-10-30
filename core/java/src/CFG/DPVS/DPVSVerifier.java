package CFG.DPVS;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.system.ValidationSystemCallbacks;
import CFG.BlockNode;
import CFG.CFG;
import CFG.CFGNode;
import CFG.CFGVisitException;
import CFG.ConditionalNode;
import CFG.EnsuresNode;
import CFG.FunctionCallNode;
import CFG.RequiresNode;
import CFG.SetOfCFG;
import CFG.BottomUp.DPVSInformationVisitor;
import CFG.simplification.VariableCollector;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.logical.NotExpression;
import expression.variables.Variable;

/**
 * This class implements the DPVS algorithm. 
 * 
 * @author helen
 *
 */
public class DPVSVerifier  {
	
	/** the validation system
	 */
	private ValidationSystemCallbacks vs;
	
	/** the method to prove */
	private CFG method;
			
	/** map variable names to the list of nodes where they are defined
	 * (cf du[x] in DPVS algorithm)
	 */
	private HashMap<String, ArrayList<CFGNode>> variableDefinitions;
	
	/** map CFGNode identifiers to the conditional nodes they depend
	 * (cf anc[u] in DPVS algorithm)
	 */
	private HashMap<String, TreeSet<CFGNode>> conditionalAncestors;
		
	/** leftDependencies(key(i)) is the list of the keys of the CFG 
	 *  nodes which are in the left branch of i (deep levels)
	 */
	private HashMap<String,TreeSet<String>> leftDependencies;

	private BlockNode pgmFieldsInitializations;
	
	SetOfCFG program;
		
	/** colors */
	private enum Colors {
	    BLANK, // not visited
	    RED,   // condition must be true (from left)
	    BLUE,  // condition must be false (from righ)
	}
	
	public DPVSVerifier(ValidationSystemCallbacks vs, SetOfCFG program, String methodName) throws IloException {

		this.vs = vs;
		this.program = program;
		
		// method to verify
		this.method = program.getMethod(methodName);
		this.pgmFieldsInitializations = program.getFieldDeclaration();
		// building the information
		DPVSInformationVisitor av = new DPVSInformationVisitor(program.size(), this.method, true);
		try {
			method.last().accept(av);
		} catch (CFGVisitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		conditionalAncestors=av.getAncestors();
		variableDefinitions=av.getVarDefinitions();
		leftDependencies = av.getLeftDependencies();

	}
	
	//--------------------------------------------------------
	// methods to print the information which are used as input of DPVS
	//--------------------------------------------------------

	// print the list of nodes where variables are defined
	private void printVariableDefinitions() {
		System.out.println("Variable definitions");
		for (String s : variableDefinitions.keySet()) {
			ArrayList<CFGNode> list = variableDefinitions.get(s);
			String res = s + ": [";
			for (int i=0;i<list.size();i++) {
				res += list.get(i).getKey() + " ";
			}
			System.out.println(res+"]");
		}
		System.out.println("\n");
	}
	
	// print the conditional ancestors of each node
	private void printAncestors() {
		System.out.println("Conditional ancestors of nodes");
		for (String s : conditionalAncestors.keySet()) {
			TreeSet<CFGNode> list = conditionalAncestors.get(s);
			Iterator<CFGNode> iter = list.iterator();
			String res = "Node " + s + ": [";
			while (iter.hasNext()) {
				CFGNode n = iter.next();
				res += n.getKey() + " ";
			}
			System.out.println(res+"]");
		}
		System.out.println("\n");
	}
	
	// print the dependencies between nodes
	private void printNodeDependencies() {
		System.out.println("node dependencies ");
		for (String n : leftDependencies.keySet()) {
			System.out.println("Descendants in the left branch of node " + n);
			System.out.println(leftDependencies.get(n));
		}
		System.out.println();
	}
	
	// to print all the static information required for DPVS algorithm
	public void printDPVSStaticInformation() {
		printVariableDefinitions();
		printAncestors();
		printNodeDependencies();
	}
	
	//--------------------------------------------------------
	// tools methods for DPVS algorithm
	//--------------------------------------------------------

	/** collect the variables of e which are not marked
	 * and add them into the stack
	 * 
	 * @param e: expression
	 * @param variableStack: the variable stack
	 * @param marked: the set of marked variables
	 * 
	 * POSTCOND: the variables which have been pushed into the stack
	 *           are marked 
	 */
	private void addVarInStack(LogicalExpression e, VariableSelector varSelector, TreeSet<Variable> marked) {
		VariableCollector vb = new VariableCollector();
		TreeSet<Variable> varsInDefinition = (TreeSet<Variable>) vb.visit(e);
		for (Variable v : varsInDefinition) {
			// We skip JML iteration variables and the first SSA version of global variables and parameters:
			//   - quantified variables (in JML \forall and \exists expressions) have no definition (for DPVS, that is) 
			//   - parameters_0 have no definition
			//   - globals_0 have a unique definition added as constraints before 
			//     starting the DPVS algorithm
			if (!v.isQuantified() && !((v.isParameter() || v.isGlobal()) && v.ssaNumber() == 0)) {
				if (VerboseLevel.DEBUG.mayPrint()) {
					System.out.println("Adding var to DPVS list: " + v);
				}
				if (marked.add(v)) {
					// v was not already marked
					varSelector.push(v);
				}
			}
		}
	}
	
	/** 
	 * return the assignment that defines variable x in node n
	 * @param x: the variable
	 * @param n: the node
	 * @return: the assignment
	 */
	private Assignment getDefinition(Variable x, CFGNode n) {
		ArrayList<Assignment> block = ((BlockNode)n).getBlock();
		for (Assignment a : block) {
			if (a.lhs().equals(x))
				return a;
		}
		System.err.println("DPVSVerifier: variable " + x + " not found in block " + n);
		System.exit(0);
		return null;
	}
	
	/** 
	 * method to handle one conditional ancestor of a node
	 * 
	 * @return : false if ancestor anc is incompatible with 
	 *           the nodes that have already been explored
	 * 
	 */
	private boolean treatAncestor(CFGNode n, CFGNode anc, TreeSet<Variable> marked, VariableSelector varSelector, HashMap<String,Colors> colors)
	{	
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.print("[ancestor "+ anc.getKey() + " of " + n.getKey()+"] "); 
		}
				
		// no branch has been explored
		if (isBlank(anc, colors)) {
			// add the variables of the condition of the ancestor
			LogicalExpression cond = ((ConditionalNode)anc).getCondition();
			addVarInStack(cond, varSelector, marked);
			// cond must be true to reach n coming from anc
			if (isLeftDependent(n, anc)) {
				vs.addConstraint(cond);
	    		if (VerboseLevel.VERBOSE.mayPrint()) {
	    			System.out.println("adding condition\n    " + cond);
	    		}
				colors.put(anc.getKey(), Colors.RED);
			}
			// cond must be false to reach n coming from anc			
			else {
				LogicalExpression negCond = new NotExpression(cond);
				vs.addConstraint(negCond);
	    		if (VerboseLevel.VERBOSE.mayPrint()) {
	    			System.out.println("adding condition\n    " + negCond);
	    		}
				colors.put(anc.getKey(), Colors.BLUE);
			}
		}
		// during the current exploration, one branch of anc
		// has already been selected
		else {
			// the branch of anc that has already been selected
			// is the opposite of the branch required to reach n from anc
			// thus the path is impossible
			if ((isLeftDependent(n, anc) && isBlue(anc, colors)) 
				|| (!isLeftDependent(n, anc) && isRed(anc, colors))) 
			{
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println("Impossible path for DVPS: the other branch is already selected");
				}
				return false;
			}
			// the branch of anc that has already been selected
			// is the same as the one required to reach n from anc
			// thus, it is not added
			else {
	    		if (VerboseLevel.VERBOSE.mayPrint()) {
	    			System.out.println("This ancestor has already been added");
	    		}
			}
		}
		return true;
	}
	
	/**
	 * private function to pass the effective parameters to a function
	 * @param f
	 * @param marked
	 * @param varSelector
	 */
	private void parameterPassing(FunctionCallNode f, TreeSet<Variable> marked, VariableSelector varSelector)
	{		
		BlockNode param = f.getParameterPassing();
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("[parameter passing]");
		}
		for (Assignment a : param.getBlock()){
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println("adding definition: " + a);
    		}
			vs.addConstraint(a);
			addVarInStack(a, varSelector, marked);
		}
	}

	/**
	 * private function to pass the effective parameters to a function
	 * @param f
	 * @param marked
	 * @param varSelector
	 */
	private void mapGlobalsToLocals(FunctionCallNode f, TreeSet<Variable> marked, VariableSelector varSelector)
	{	
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("[Mapping globals to locals]");
		}
		for (Assignment a : f.getLocalFromGlobal().getBlock()){
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println("adding definition: " + a);
    		}
			vs.addConstraint(a);
			addVarInStack(a, varSelector, marked);
		}
	}
	
	/** private method to manage function calls
	 * add information related to f in variableDefinitions, 
	 * conditionalAncestors and leftDependencies
	 * @throws IloException 
	 */
	private void makeFunctionCall(FunctionCallNode f, TreeSet<Variable> marked, VariableSelector varSelector) throws IloException
	{
		CFG newCFG;
		if (!f.hasBeenCalled()) {
			// rename all variables of the CFG for one more call 
			newCFG = f.duplicateCFG();	
			f.setCFG(newCFG);	
		}
		
		newCFG = f.getCFG();
		// add all the variables
		// NB: the effective parameters of the function 
		//     have been added in newVars but should not be added in 
		//     validationSystem
		for (Variable v : newCFG.getUsefulVar()){
			vs.addVar(v);
		}
		
		System.out.println("Call to " + f.getName());
		//		System.out.println("CFG " + f.getCFG());

		// make effective parameter passing
		parameterPassing(f, marked, varSelector);

		// Map global variables to local copies
		mapGlobalsToLocals(f, marked, varSelector);

		// compute information on the function nodes
		DPVSInformationVisitor iv = new DPVSInformationVisitor(newCFG.getNodeNumber(), newCFG, false);
		try {
			newCFG.last().accept(iv);
		} catch (CFGVisitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
		// add information on the function node into the current information 
		conditionalAncestors.putAll(iv.getAncestors());
		//		System.out.println("var def in function call " + iv.getVarDefinitions());
		variableDefinitions.putAll(iv.getVarDefinitions());
		leftDependencies.putAll(iv.getLeftDependencies());
		
		if (Validation.verboseLevel==VerboseLevel.DEBUG) {
			System.out.println("Static information after call to " + f.getName());
			printDPVSStaticInformation();
		}
	}

	
	//--------------------------------------------------------
	// DPVS algorithm
	//--------------------------------------------------------

	
	/** private method for validation
	 * PRECOND: the variable stack is not empty
	 * @throws IloException 
	 */
	private boolean validate(TreeSet<Variable> marked, VariableSelector varSelector, HashMap<String,Colors> colors) throws IloException{

		// select one variable
		Variable x = varSelector.pop();
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Treatment of variable " + x);
		}

		// loops on the node where variable x is defined
		ArrayList<CFGNode> nodesOfX = variableDefinitions.get(x.name());
		
		//System.out.println("noeuds de " + x.name() + " " + nodesOfX);
		
		//To match LeVinh's DPVS, we try the definitions in the reverse order of the ArrayList  
		//for (CFGNode n : nodesOfX) {
		int i;
		CFGNode n;
		for(i=nodesOfX.size()-1; i>=0; i--) {
			n = nodesOfX.get(i);
			
			// true if the negation of the current branch has already been explored
			boolean cut = false;

			// save the current context
			HashMap<String,Colors>  savedColors = new HashMap<String, Colors>(colors);
			TreeSet<Variable> savedMarked = new TreeSet<Variable>(marked);
			VariableSelector savedVarStack = (VariableSelector) varSelector.clone();
			vs.save();

			// add the definition of x
			Assignment definitionOfX = getDefinition(x, n);
			vs.addConstraint(definitionOfX);
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println("[node " + n.getKey() + "] adding definition \n   " + definitionOfX);
    		}
    		
    		//System.out.println("Heuristic Variable: " + x + " Definition: " + definitionOfX);

    		// push the variables involved in the definition of x
			addVarInStack(definitionOfX, varSelector, marked);

			
			// loop to add the conditions of the ancestors of x 
			// (i.e. conditional nodes that x depend)
			// if the ancestor is not compatible, then the branch is cut
			TreeSet<CFGNode> listOfAncestors = conditionalAncestors.get(n.getKey());
			if (listOfAncestors != null) {
				Iterator<CFGNode> iter = listOfAncestors.iterator();
				while (!cut && iter.hasNext()) {
					CFGNode anc = iter.next();
					cut = !treatAncestor(n, anc, marked, varSelector, colors);
				}
			}

			
			// if the father of n is a function call node,
			// and the function call has not been made for
			// this node, make the function call
			// (the function call must be done only one time
			// for the whole node)
			// is the branch has been cut, it is not necessary 
			// to call the function
			if (!cut && (n.getLeftFather() instanceof FunctionCallNode)){
				FunctionCallNode fct = (FunctionCallNode)n.getLeftFather();
				makeFunctionCall(fct, marked, varSelector);
//				if (fct.firstCallForNode(n.getIdent())) {
//					fct.setCallerNode(n.getIdent());
//					makeFunctionCall(fct,marked,variableStack);
//				}
			}

			// if the branch is not cut 
			if (!cut) {
				// end of a path, make a complete solve
				if (varSelector.isEmpty()) {
					// there is a counterexample
					if (vs.solve()) 
						return false;
				}
				else {
					// current path is feasible, continue the exploration
					// (i.e. recursive call)
					if (vs.isFeasible()){
						boolean result = validate(marked, varSelector, colors);
						// stop if there is a counterexample
						if (!result)
							return result;
					}
				}
			}
			else 
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println("Path has been cut, backtracking...");
				}
			
			// to restore the initial context before exploring another definition
			colors = savedColors;
			varSelector = savedVarStack;
			marked = savedMarked;
			vs.restore();
		}
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println("End of treatment of variable " + x);
		}
		return true;
	}
	
	/**
	 * main method for validation using DPVS algorithm
	 * @throws IloException 
	 */
	public boolean validate() throws IloException {

		if (Validation.verboseLevel==VerboseLevel.VERBOSE) {
				System.out.println("Static information for main program");
				printDPVSStaticInformation();
		}
		
		// initializations
		//////////////////

        //	set of marked variables
		TreeSet<Variable> marked = new TreeSet<Variable>();
		
		// variable stack selector 
		//VariableSelector variableSelector = new StackSelector();
		
		//variable heap selector
		VariableSelector variableSelector = new HeapSelector(new VariableLevels(program));
		
		// colors to know the condition of reachability of a 
		// conditional node
		HashMap<String,Colors> colors= new HashMap<String,Colors>();

		// add global variable initializations
		if (this.pgmFieldsInitializations != null) {
			for (Assignment ass: this.pgmFieldsInitializations.getBlock()) {
				this.vs.addConstraint(ass);
			}
		}
		
		// add the negation of the postcondition into the constraint system
		LogicalExpression postCond = ((EnsuresNode)method.last()).getCondition();
		LogicalExpression negPostCond = new NotExpression(postCond);
		vs.addConstraint(negPostCond);

		// initialize the stack with the variables of the postcondition
		addVarInStack(negPostCond, variableSelector, marked);
		
		// add the precondition in the constraint system
		LogicalExpression preCond = ((RequiresNode)method.firstNode()).getCondition();
		vs.addConstraint(preCond);

		
		// call to the validation method
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\n###########################");
			System.out.println("Starting program verification using DPVS"); 
			System.out.println("###########################\n");
		}
		
		if (variableSelector.isEmpty()) {
			// The post-condition only refers to parameters and/or global variables not modified
			// in the program to be checked: no need to call DPVS (e.g. UselessRequires.java in regression tests).
			return !vs.solve();
		}
		else {
			return validate(marked, variableSelector, colors);
		}		
	}
	
	
	///////////////////////////////////////////////////
	//      tools methods
	///////////////////////////////////////////////////
	
	// private methods to handle colors of nodes
	private boolean isBlank(CFGNode n,HashMap<String,Colors> colors) {
		return !colors.containsKey(n.getKey());
	}
	
	private boolean isRed(CFGNode n,HashMap<String,Colors> colors) {
		return colors.get(n.getKey())==Colors.RED;
	}
	
	private boolean isBlue(CFGNode n,HashMap<String,Colors> colors) {
		return colors.get(n.getKey())==Colors.BLUE;
	}
	
	// private method for node dependencies
	private boolean isLeftDependent(CFGNode n, CFGNode anc){
		return leftDependencies.containsKey(anc.getKey()) && 
		leftDependencies.get(anc.getKey()).contains(n.getKey());
	}
}
