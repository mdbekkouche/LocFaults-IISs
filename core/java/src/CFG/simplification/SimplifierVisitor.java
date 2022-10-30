package CFG.simplification;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeSet;

import validation.Validation.VerboseLevel;

import expression.Expression;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.NondetAssignment;
import expression.variables.Variable;
import expression.variables.Variable.Use;

import CFG.AssertEndWhile;
import CFG.AssertNode;
import CFG.BlockNode;
import CFG.CFG;
import CFG.CFGNode;
import CFG.CFGVisitException;
import CFG.CFGVisitor;
import CFG.EnsuresNode;
import CFG.FunctionCallNode;
import CFG.IfNode;
import CFG.RequiresNode;
import CFG.RootNode;
import CFG.WhileNode;
import CFG.OnTheFlyWhileNode;

/** 
 * make a bottom-up visit of the CFG to simplify the CFG :
 *     - remove empty nodes  
 *     - mark as deleted all nodes whose variables do not depend from 
 *       variables of the postcondition
 * 
 * @author helen
 *
 */


public class SimplifierVisitor implements CFGVisitor{
	
	// the cfg which is being visited
	private CFG cfg;

	// an expression visitor to collect the variables
	private VariableCollector vc ;
	
	// list of variables which depend from the postcondition
	TreeSet<Variable> usefulVar;
	
	Stack<TreeSet<Variable>> savedUsefulVar;
	
//	/**
//	 * The field declarations (i.e. globals) of the class to analyze.
//	 * It is processed when the simplified method is the method to
//	 * prove. 
//	 */
//	private BlockNode fieldDeclarations;
	
	// number of CFG nodes that have been removed
	int removedNodes;
	
	// number of assignments that have been removed
	int removedAssign;
	
	public SimplifierVisitor(CFG cfg) {
		this(cfg, new TreeSet<Variable>());
	}
	
	public SimplifierVisitor(CFG cfg, TreeSet<Variable> uv) {
		this.cfg = cfg;
		cfg.initBottomUpAccess();
		usefulVar = uv ;
		vc = new VariableCollector();
		savedUsefulVar = new Stack<TreeSet<Variable>>();
		//this.fieldDeclarations = fieldDeclarations;
		removedNodes=0;
		removedAssign=0;
	}

	public void visit(EnsuresNode n) throws CFGVisitException, IloException {
		this.addToUsefulVar(n.getCondition());
		acceptPrevious(n);
	}

	public void visit(RequiresNode n) throws CFGVisitException {
		// The RequiresNode is used as the root node of the CFG to stop exploration.
		
		// TODO: We add all the variables in the requires clause. 
		// However, part of the clause, or even the clause itself, may be removed if some of its sub-expressions
		// refer to variables not used for the program verification.
		// Coarsely, we could check that at least one variable in the clause is in usefulScalarVar.
		// For more accuracy, we would have to examine each sub-expression and remove it if it refers to unused variables...
		this.addToUsefulVar(n.getCondition());
	}
 
	@Override
	public void visit(RootNode n) throws CFGVisitException {
		System.err.println("Error (SimplifierVisitor): root node reached!");
		System.exit(-1);
	}

	public void visit(AssertNode n) throws CFGVisitException, IloException {
		this.addToUsefulVar(n.getCondition());
		acceptPrevious(n);
	}
	

	public void visit(AssertEndWhile n) throws CFGVisitException, IloException {
		this.addToUsefulVar(n.getCondition());
		acceptPrevious(n);		
	}

	public void visit(IfNode n) throws CFGVisitException, IloException {
		// the if node has been reached from bottom one more time
		cfg.bottomUpMark(n.getIdent());
		// if it has been reached from both left and right child, exploration continues on the father
		if (cfg.isBottomUpMarked(n.getIdent())) {
			// if then and else parts are the same, remove the if node
			if (n.getLeft() == n.getRight() &&  n.isRemovable()) {
				remove(n, "because if and then parts are empty");
			}
			else {
				this.usefulVar.addAll(this.savedUsefulVar.pop());
				this.addToUsefulVar(n.getCondition());
			}
			acceptPrevious(n);
		}
		else { //First visit
			this.savedUsefulVar.push((TreeSet<Variable>)usefulVar.clone());
		}
	}

	public void visit(WhileNode n) throws CFGVisitException, IloException {
		// the while node has been reached from bottom one more time
		cfg.bottomUpMark(n.getIdent());
		// if it has been reached from both left and right child, exploration continues on the father
		if (cfg.isBottomUpMarked(n.getIdent())) {
			// if then and else parts are the same, remove the if node
			if (n.getLeft() == n.getRight() &&  n.isRemovable()) {
				remove(n, "because if and then parts are empty");
			}
			else {
				this.usefulVar.addAll(this.savedUsefulVar.pop());
				this.addToUsefulVar(n.getCondition());
			}
			acceptPrevious(n);
		}
		else { //First visit
			this.savedUsefulVar.push((TreeSet<Variable>)usefulVar.clone());
		}
	}
		
	protected void visitAssignments(ArrayList<Assignment> block) {
		// the set of assignments is treated from the last one to the first one
		// to correctly handle SSA renaming
		for (int i = block.size()-1; i>=0; i--) {
			Assignment ass = block.get(i);
			Variable assignedVar = ass.lhs();
			if (assignedVar.use() == Use.META) {
				// We may assume here the assignment comes from a return statement.
				// Removing it requires to ensure that all calls to this function
				// are from removed statements. As this verification can not be done 
				// in one simplification pass, we keep the return statement.
				this.addToUsefulVar(ass.rhs());
				this.usefulVar.add(assignedVar);
			}
			else if (usefulVar.contains(assignedVar)) { 
//				System.out.println("assignment " + ass + " is useful");
				if (! (ass instanceof NondetAssignment)){
					this.addToUsefulVar(ass.rhs());
					if (ass instanceof ArrayAssignment) {
						ArrayAssignment arrAss = (ArrayAssignment)ass;
						if (!usefulVar.contains(arrAss.previousArray()))
								usefulVar.add(arrAss.previousArray());
					}
				}
			}
			else {
	    		if (VerboseLevel.VERBOSE.mayPrint()) {
	    			System.out.println("Assignment " + ass + " has been removed");
	    		}
				block.remove(ass);
				removedAssign++;
			}
		}
	}

	public void visit(BlockNode n) throws CFGVisitException, IloException {
		ArrayList<Assignment> initialBlock = null;
		if (VerboseLevel.VERBOSE.mayPrint()) {
			initialBlock = (ArrayList<Assignment>) n.getBlock().clone();
		}
		// remove all assignments which do not depend from the post-condition
		visitAssignments(n.getBlock());
		// remove the block if it is empty or do not depend from the post-condition
		if (n.isEmpty() && n.isRemovable()) {
			if (initialBlock != null) {  // Verbose mode on
				remove(n, "because block " + initialBlock + " is not useful");
			}
			else { // Verbose mode off
				remove(n, "");
			}
		}
		acceptPrevious(n);
	}
	
	
	// assignments for parameter passing are always useful
	private void visitParameterPassing(ArrayList<Assignment> param){
		for (int i = 0; i<param.size(); i++) {
			Assignment ass = (Assignment)param.get(i);
			this.addToUsefulVar(ass.rhs());
			usefulVar.add(ass.lhs());
		}
	}
	
	// assignments of global variables from their representation
	// in the function are always useful
	private void visitGlobalFromLocal(ArrayList<Assignment> link){
		for (int i = 0; i<link.size(); i++) {
			Assignment ass = (Assignment)link.get(i);
			this.addToUsefulVar(ass.rhs());
			usefulVar.add(ass.lhs());
		}
	}
	
	//global variables used in the function are always useful
	private void visitLocalFromGlobal(ArrayList<Assignment> link){
		for (int i = 0; i<link.size(); i++) {
			Assignment ass = (Assignment)link.get(i);
			this.addToUsefulVar(ass.rhs());
			usefulVar.add(ass.lhs());
		}
	}
	
	public void visit(FunctionCallNode n) throws CFGVisitException, IloException {
		// add variables in parameter passing assignments 
		visitParameterPassing(n.getParameterPassing().getBlock());
		// add global variables which are used in the function
		visitLocalFromGlobal(n.getLocalFromGlobal().getBlock());
		CFG c = n.getCFG();
		// continue by visiting the CFG associated to the function
		String functionName = c.name();
		//System.out.println("Function call to " + functionName);
		if (n.isFirstCall()) {
			System.out.println("Simplifying CFG " + functionName);
			System.out.println("Initial CFG has " + c.getNodeNumber() + " nodes");
			// create a simplifier visitor from the current one to simplify the function
			SimplifierVisitor sv = new SimplifierVisitor(c, usefulVar);
			n.getCFG().last().accept(sv);
			System.out.println(Simplifier.removedNodeMessage(sv.removedNodes));
			System.out.println("End of simplification of method "+functionName);
		}
		// propagate above the functionCall node
		acceptPrevious(n);
	}
	
	
    public void addToUsefulVar(Expression exp) {
		usefulVar.addAll((TreeSet<Variable>)exp.structAccept(vc));
	}
	
	//// methods to find next node to explore
	
	// to continue CFG exploration
	private void acceptPrevious(CFGNode n) throws CFGVisitException, IloException {
		TreeSet<Variable> savedVar;
		CFGNode rightFather = n.getRightFather();
		if (rightFather != null) {
			savedVar = (TreeSet<Variable>)this.usefulVar.clone();
			n.getLeftFather().accept(this);
			this.usefulVar = savedVar;
			rightFather.accept(this);
		}
		else {
			n.getLeftFather().accept(this);
		}
	}
	
//	// to continue CFG exploration
//	private void acceptPrevious(CFGNode n) throws CFGVisitException{
//		CFGNode leftFather = findLeftFather(n);
//		CFGNode rightFather = findRightFather(n);
//		n.setLeftFather(leftFather);
//		if (leftFather!=null) {
//			leftFather.setLeft(n);
//			leftFather.accept(this);
//		}
//		n.setRightFather(rightFather);
//		if (rightFather!=null) {
//			rightFather.setRight(n);
//			rightFather.accept(this);
//		}
//	}
		
	
	// tools
	
	private void remove(CFGNode node, String message) {
		node.remove(message);
		this.removedNodes++;
	}
	
	// return true if at least one variable in var is also in usefulVar
	private boolean isUseful(TreeSet<Variable> var) {
		boolean useful = false;
		for (Variable v : var) {
			if (usefulVar.contains(v)){
				useful = true;
				break;
			}
		}
		return useful;
	}

	@Override
	public void visit(OnTheFlyWhileNode whileNode) {
		// TODO Auto-generated method stub
		
	}
	
}
