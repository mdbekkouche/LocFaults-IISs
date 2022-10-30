package CFG;

/**
 * to represent a function call node
 * 
 * @author helen
 * 
 */

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import validation.util.Type;
import CFG.DFS.ClonerAndRenamerVisitor;
import CFG.DFS.RenamerVisitor;
import expression.Expression;
import expression.logical.Assignment;
import expression.variables.SymbolTable;
import expression.variables.Variable;
import expression.variables.Variable.Use;

public class FunctionCallNode extends CFGNode {

	/** CFG associated with the function*/
	private CFG cfg;

	/** number of call of the method*/
	private int callNumber;
	
	/** flag to know if this FunctionCallNode has already been
	 * called and thus its CFG has already been renamed
	 */
	private boolean hasBeenCalled;

	/** effective parameters */
	private ArrayList<Expression> effectiveParam;

	/** node that contains assignments from effective parameters to formal parameters*/
	private BlockNode parameterLink;

	/** node that contains assignments from global variables to renaming 0 of the variables 
	 * which are used in the function to represent global variables*/
	private BlockNode localFromGlobal;

	/** 
	 * the node from which the function has been called
	 * This is used to make the call to the function only one time for a whole
	 * node (and not for each definition in the block)
	 */
	private int callerNodeForDPVS;
	
	/**
	 * the node that follows this function call
	 */
	private CFGNode nodeAfterCall;

	//TODO : require to keep a table of all FunctionCallNode in Java2CFGVisitor
	//       is it necessary ? that works if we simplify the CFG when calling the function for the first time
	// to know if it is the last call to the function
	// used to simplify the CFG of the function only when it is its last call
	// (current simplifierVisitor makes a bottomUp visit)
	//	private boolean lastCall;

	public FunctionCallNode(int nodeIdent, CFG cfg, ArrayList<Expression> effectiveParam, int n)
	{
		super(nodeIdent, cfg.name());
		this.cfg = cfg;
		this.effectiveParam = effectiveParam;
		this.callNumber = n;
		this.callerNodeForDPVS = -1;
		makeParameterNode(cfg.parameters());
		this.hasBeenCalled = false;
	}

	public FunctionCallNode(FunctionCallNode n) {
		super(n.nodeIdent,n.key,n.left,n.right,n.leftFather,n.rightFather);
		callNumber = n.callNumber;
		effectiveParam = n.effectiveParam;
		parameterLink = n.parameterLink;
		localFromGlobal = n.localFromGlobal;
		this.callerNodeForDPVS = -1;
		this.hasBeenCalled = n.hasBeenCalled;
		this.nodeAfterCall = n.nodeAfterCall;
		this.cfg = n.cfg;
	}

	public Object clone() {
		return new FunctionCallNode(this);
	}
	
	/**
	 * 
	 * @param n: to set the current caller node
	 */
	public void setCallerNode(int n) {
		callerNodeForDPVS = n;
	}
	
	/**
	 * 
	 * @param n: to set the node where to continue after the call
	 */
	public void setAfterNode(CFGNode n) {
		nodeAfterCall = n;
	}
	
	public CFGNode getAfterNode() {
		return nodeAfterCall;
	}

	/** 
	 * @param n: the node
	 * @return: true when the current call to the function has
	 *          not been make from node n (used in DPVS)
	 */
	public boolean firstCallForNode(int n) {
		return callerNodeForDPVS!=n;
	}

	// to add a first node into the CFG to make the link between 
	// formal and effective parameters
	private void makeParameterNode(ArrayList<Variable> formalParam) {	
		parameterLink = new BlockNode(cfg.getNodeNumber() + 1, cfg.name());
		for (int index = 0; index < formalParam.size(); index++) {
			parameterLink.add(new Assignment(formalParam.get(index), effectiveParam.get(index)));
		}
	}

	/**
	 *  global variable name in function is global_functionName_#call_className_varName
	 * @param n : the name of the global variable in the function
	 * @return : the true name of the global variable
	 */
	private String getGlobalName(String n){
		//		System.out.println("dans getGlobalName " + n);
		int first = n.indexOf("_");
		int second = n.indexOf("_", first+1);
		int third = n.indexOf("_", second+1);
		return n.substring(third+1);
	}

	/** to make link between global variables of the program and their representation
	 * in the function
	 * 
	 * [DPVS only]
	 * 
	 * @param calleeGlobals : map that gives the last SSA renaming of each variable used to represent  
	 *            a global variable in the CFG of the function
	 * @param callerSymbols : current symbol table with the last renaming of global variables 
	 * 
	 */
	public void makeGlobalVariableNodes(HashMap<String, Integer> calleeGlobals, String callerName, SymbolTable callerSymbols, 
										HashMap<String, Integer> callerGlobalVars, boolean callerIsMethodToProve, 
										BlockNode globalFromLocalNode) 
	{
		String globalName, callerPrefixedName;
		Variable globalVar, newGlobalVar, firstLocalVar, lastLocalVar;
		Type t;
		int ssaInFunction;

		String calleeName = cfg.name();

		// node to execute before the CFG of the function in order to assign right value of global variables
		// to the variables used in the function to represent these global variables
		// Node number cfg.getNodeNumber() + 1 is used for parameters' node.
		localFromGlobal = new BlockNode(cfg.getNodeNumber() + 2, calleeName);

		// Assignments to execute after the CFG of the function in order to set the value 
		// of global variables modified by the execution of the function
		TreeSet<Variable> methodUsefulScalarVar = this.cfg.getUsefulVar();

		// treat each global variable
		for (String name : calleeGlobals.keySet()) {
			//			System.out.println("dans make " + name + s.getAllVarNames());

			// Local variables from global ones
			globalName = getGlobalName(name);
			if (callerIsMethodToProve) {
				callerPrefixedName = globalName;
			}
			else {
				callerPrefixedName = "global_" + callerName + "_" + globalName;
			}
			globalVar = callerSymbols.get(callerPrefixedName);
			// set the renaming of the global variable before function call (for \old)
			globalVar.setOld(globalVar);
			t = globalVar.type();
			firstLocalVar = new Variable(name + "_0", t, Use.GLOBAL);
			localFromGlobal.add(new Assignment(firstLocalVar, globalVar));

			// Global Variables from local ones
			ssaInFunction = calleeGlobals.get(name);
			if (ssaInFunction > 0) {
				newGlobalVar = callerSymbols.addVar(callerPrefixedName);
				lastLocalVar = new Variable(
						"global_" + calleeName + "_" + this.callNumber 
						+ "_" + globalName + "_" + ssaInFunction, 
						t, 
						Use.GLOBAL);
				callerGlobalVars.put(newGlobalVar.root(), newGlobalVar.ssaNumber());
				globalFromLocalNode.add(new Assignment(newGlobalVar, lastLocalVar));
				//The variable must be added to the CFG useful scalar variables, 
				//otherwise it will be created twice: 
				// 1. the simplifier will add it to the method to prove useful scalar variables 
				//    (which is used to create at start the solver variables) and 
				// 2. the processing of the function call node by DPVS will 
				//    create the variable again in the solver, on-the-fly  
				methodUsefulScalarVar.add(lastLocalVar);
			}
		}
	}

	public Type returnType() {
		return cfg.returnType();
	}

	@Override
	public void accept(CFGVisitor v) throws CFGVisitException, IloException {
		v.visit(this);
	}

	public FunctionCallNode accept(CFGClonerVisitor v) throws CFGVisitException {
		return v.visit(this);
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public BlockNode getParameterPassing() {
		return parameterLink;
	}

	public BlockNode getLocalFromGlobal() {
		return localFromGlobal;
	}

	public CFG getCFG() {
		return cfg;
	}
	
	public void setCFG(CFG c) {
		this.cfg = c;
	}

	public String getName() {
		return "function " + cfg.name() + ", call " + callNumber;
	}

	public ArrayList<Expression> effectiveParam() {
		return effectiveParam;
	}

	public String toString(){
		return "Function call to " + cfg.name() + " (call #" + callNumber + ")\n" 
		+  super.toString() + "\n" + printParameters()
		+ "\n" + printGlobalVar() ; //+ "\nLAST" + cfg.last();
	}

	private String printParameters() {
		String s = "Parameter passing:\n    ";
		s+=parameterLink.getBlock().toString();
		return s;
	}

	private String printGlobalVar() {
		String s= "";
		if (localFromGlobal.getBlock().size()!=0) {
			s+= "Local from global assignments (executed before function call)\n    ";
			s+=localFromGlobal.getBlock();
		}
		return s;
	}

	////////////////////////////////////////////////////////
	// methods for handling renaming of the prefix of the function 
	// when the function is called more than one time

	/** return true if this FunctionCallNode is visited for the first time
	 */
	public boolean isFirstCall() {
		return callNumber==0;
	}
	
	public boolean hasBeenCalled() {
		return hasBeenCalled;
	}

	//	/** to set lastCall
	//	 */
	//	public void setLastCall() {
	//		lastCall=true;
	//	}

	//	/**
	//	 * @return : true if it is the last call of the method in the current class
	//	 * used in simplifierVisitor to simplify the CFG of the function only once
	//	 */
	//	public boolean isLastCall() {
	//		return lastCall;
	//	}

	/** method to rename the variables in the CFG associated with the function
	 *  according to the current call number of the method
	 * 
	 * @return : the set of new variables
	 * @throws IloException 
	 */
	public Collection<Variable> oneMoreCall() throws IloException {
		// visit the parameter passing block and the global variable passing blocks
		// to rename the function identifiers according to the current
		// function number of call
		TreeSet<Variable> oldVar = (TreeSet<Variable>)cfg.getUsefulVar().clone();
		RenamerVisitor rn = new RenamerVisitor(cfg.name(),cfg.getNodeNumber(),callNumber,oldVar);
		rn.visitBlock(parameterLink.getBlock());
		rn.visitBlock(localFromGlobal.getBlock());		

		try {
			cfg.firstNode().accept(rn);
		} catch (CFGVisitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return only new variables
		Collection<Variable> newVars = new TreeSet<Variable>();
		for (Variable v : rn.getUsefulVar()) {
			if (!cfg.getUsefulVar().contains(v)) {
				newVars.add(v);
			}
		}
		//		//System.out.println("new vars in function call node "+ newVars);

		//callNumber++;
		return newVars;
	}


	/**
	 * visit the parameter passing block and the global variable passing block
	 * to rename the function identifiers according to the current
	 * function number of call
     * then make a deep copy of the CFG where identifiers have been prefixed
	 *           by the name of the function and the number of its call
	 * 
	 * @return : a copy of the CFG where identifiers have been prefixed
	 *           by the name of the function and the number of its call
	 * 
	 */
	public CFG duplicateCFG() {
		ClonerAndRenamerVisitor rn = new ClonerAndRenamerVisitor(cfg.name(), cfg.getNodeNumber(), callNumber);
		//,(TreeSet<Variable>) cfg.getUsefulScalarVar().clone());

		// clone and rename the parameters
		BlockNode newBlock = new BlockNode(parameterLink);
		rn.visitBlock(newBlock.getBlock());
		parameterLink=newBlock;
//		System.out.println("param dans duplicate " + parameterLink);

		// clone and rename the global variable link
		newBlock = new BlockNode(localFromGlobal);
		rn.visitBlock(newBlock.getBlock());
		localFromGlobal = newBlock;
		
		// to know if this FunctionCallNode has already been called
		// and thus renamed
		hasBeenCalled = true;

		return new CFG(cfg, rn);
	}
}
