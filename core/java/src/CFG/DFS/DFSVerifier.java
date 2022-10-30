package CFG.DFS;

import ilog.concert.IloException;

import java.util.ArrayList;

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
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.logical.NotExpression;
import expression.variables.Variable;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.system.ValidationSystemCallbacks;

/**
 * Implements a DFS visit of the graph. 
 * 
 * @author helen and Olivier Ponsini
 *
 */
public class DFSVerifier implements CFGVisitor {
			
	private ValidationSystemCallbacks validationSystem;
	
	public DFSVerifier(ValidationSystemCallbacks vs) {
		this.validationSystem = vs;
	}
	
	//--------------------------------------------------------
	// Methods that depend on the validation system
	//--------------------------------------------------------
		
	// to test a condition
	protected boolean isSatisfiable(LogicalExpression le) {
		return this.validationSystem.tryDecision(le);
	}
	
	// to test a condition on the ensure node : statistics on paths and execution times have to be
	// stored into the solver
	protected boolean ensureSatisfied(LogicalExpression le) {
		return this.validationSystem.checkPostcond(le);
	}
	
	// to test a condition on the ensure node : statistics on paths and execution times have to be
	// stored into the solver
	protected boolean assertSatisfied(LogicalExpression le, String message) {
		save();
		boolean isSatisfied = this.validationSystem.checkAssertion(le, message);
		restore();
		if (isSatisfied) {
			addConstraint(le);
		}
		return isSatisfied;
	}

	// to add a constraint into the solver
	protected void addConstraint(LogicalExpression le) {
		this.validationSystem.addConstraint(le);
	}
	
	// to handle backtrack and add or remove constraints in the solver
	protected void save() {
		this.validationSystem.save();
	}
	
	protected void restore() {
		this.validationSystem.restore();
	}

	//--------------------------------------------------------
	// Methods that visit the CFG
	//--------------------------------------------------------

	@Override
	public void visit(RootNode n) throws CFGVisitException, IloException {
		CFGNode firstNode = n.getLeft();
		if (firstNode != null)
			firstNode.accept(this);
		else
			System.err.println("Error (DFSVerifier): empty CFG!");
	}

	/** test the condition
	 * throws an exception if it is not verified
	 * this is the last node, but if it is in a function call,
	 * its left node is the place where to return in the caller method
	 * 
	 * it is necessary to first explore the caller method before
	 * backtracking into the called method
	 * @throws IloException 
	 */
	public void visit(EnsuresNode n) throws CFGVisitException, DFSException, IloException {
		//System.out.println("Visiting node n:" + n.getIdent());
		boolean satisfied;
		LogicalExpression le = n.getCondition();
		if (n.isMainEnsure()) { //Entry point method ensure
			satisfied = ensureSatisfied(le);
			if (!satisfied) {
				throw new DFSException("Ensures \n" + le + "\n" + "is violated!", false);
			}
			// this is the last node, nothing else to visit in this CFG
		}
		else {
			satisfied = assertSatisfied(le, "called method ensure "); 
			if (!satisfied) {
				throw new DFSException("Ensures \n" + le + "\n" + "is violated!", false);
			}		
			else { //Continue after call site.
				//The call site is the FunctionCallNode that started the visit of
				//the called method. The call site node stores in the left child 
				//of the ensure node, at each call, the node to continue with.
//				System.out.println("ensures node in DFS visitor " + n);
				n.getLeft().accept(this);
			}
		}
	}


	/** test the condition
	 * throws an exception if it is not verified
	 * visit the  left node
	 * @throws IloException 
	 */
	public void visit(RequiresNode n) throws CFGVisitException, IloException {
		//System.out.println("Visiting node n:" + n.getIdent());
		LogicalExpression le = n.getCondition();
		//System.out.println("Requires expression : " +le);
		if (isSatisfiable(le)) {
			System.out.println("Requires is satisfiable!");
		}
		else { 
			throw new DFSException("Requires cannot be satisfied!", true);
		}
		n.getLeft().accept(this);
	}
	
	/** test the assertion
	 * throws an exception if it is not verified
	 * return left node
	 * @throws IloException 
	 */
	public void visit(AssertNode n) throws CFGVisitException, IloException {
		//System.out.println("Visiting node n:" + n.getIdent());
		LogicalExpression le = n.getCondition();
		if (!assertSatisfied(le, "user assertion ")) {
			throw new DFSException("User assertion is violated!", false);
		}
		n.getLeft().accept(this);
	}
	
	/** test the assertion
	 * throws an exception if it is not verified
	 * return left node
	 * @throws IloException 
	 */
	public void visit(AssertEndWhile n) throws CFGVisitException, IloException {
		//System.out.println("Visiting node n:" + n.getIdent());
		LogicalExpression le = n.getCondition();
		if (!assertSatisfied(le, "LOOP unwinding assertion ")) {
			throw new DFSException(
					"Maximum unfolding number (" + Validation.maxUnfoldings +") reached for loop number " + n.getIdent() + "\n", 
					true);
		}
		n.getLeft().accept(this);
	}

	/** test the condition
	 * return the left child if it is true (then part)
	 * return the right child if it is false (else part)
	 * @throws IloException 
	 */
	public void visit(IfNode n) throws CFGVisitException, IloException {
		//System.out.println("Visiting node n:" + n.getIdent());
		LogicalExpression c = n.getCondition();
//		System.out.println("if number " + n.getIfNumber());
		
		// save solver context before taking then branch
		save();
		boolean enterThen = isSatisfiable(c);
		if (enterThen) {
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println("Condition " + c + " is satisfied, taking if part");
    		}
			n.getLeft().accept(this);
		}
		// restore solver context to explore else branch
		restore();

		if (isSatisfiable(new NotExpression(c))) {
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println("NEGATION of condition " + c + " is satisfied, taking else part");
    		}
 			n.getRight().accept(this);
		}
		else {
			// Could neither enter 'then' part nor 'else' part!
			if (!enterThen) {
				throw new DFSException(
						"Condition of 'if' statement number " + n.getIdent() + " is not satisfiable nor its negation!\n", 
						true);
			}
		}
	}


	public void visit(WhileNode n) throws CFGVisitException, IloException {
		//System.out.println("Visiting node n:" + n.getIdent());
		LogicalExpression cond = n.getCondition();
//		System.out.println("while " + cond);

		// save solver context before trying to exit (or not enter) the loop
		save();
		boolean exitWhile = isSatisfiable(new NotExpression(cond));
		if (exitWhile) {
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println("NEGATION of condition " + cond + " is satisfied");
    			System.out.println("Skipping while block " + n.whileNumber());
    		}
			n.getRight().accept(this);
		}
		// restore solver context and try to enter the loop
		restore();
		if (isSatisfiable(cond)) {
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println("Condition " + cond + " is satisfied");
    			System.out.println("Entering while block " + n.whileNumber());
    		}
			// visit n as a block node : visit the block and continue on the left
			n.getLeft().accept(this);
		}
		else {
			// Could not enter nor exit while loop!
			if (!exitWhile) {
				throw new DFSException(
						"Condition of 'while' statement number " + n.getIdent() + " is not satisfiable nor its negation!\n", 
						true);
			}
		}
	}
	
	// execute all statements in a block
	private void executeBlock(BlockNode n) {
		ArrayList<Assignment> block  = n.getBlock();
		for (Assignment a : block) {
			//System.out.println("current assignment " + a );
			addConstraint(a);
		}
	}

	/** 
	 * execute all assignments of the block
	 * the next node to visit is the left child
	 * @throws IloException 
	 */
	public void visit(BlockNode n) throws CFGVisitException, IloException {
		//System.out.println("Visiting node n:" + n.getIdent());
		executeBlock(n);
		n.getLeft().accept(this);
	}
	
	
	
	// VERSION qui remplace le FunctionCallNode par le CFG
//	public void visit(FunctionCallNode n) throws CFGVisitException {
//		if(VerboseLevel.TERSE.mayPrint()) {
//			System.out.println("\nFunction call to " + n.getName());
//		}
//		
//		CFG newCFG = n.duplicateCFG();
//		
//		// add new variables
//		for (Variable v : newCFG.getUsefulScalarVar()){
//			validationSystem.addVar(v);
//		}
//		
//		// to know where to continue after the function call
//		newCFG.setContinueWith(n.getLeft());
//
//		System.out.println("CFG dans fonction call " + newCFG);
//		
//		// replace the FunctionCallNode with the CFG itself
//		CFGNode nodeBefore = n.getLeftFather();
//		CFGNode firstNode = n.getParameterPassing();
//		nodeBefore.setLeft(firstNode);
//		firstNode.setLeftFather(nodeBefore);
//		CFGNode global = n.getLocalFromGlobal();
//		firstNode.setLeft(global);
//		global.setLeftFather(firstNode);
//		global.setLeft(newCFG.firstNode());
//		newCFG.firstNode().setLeftFather(global);
//		
//		firstNode.accept(this);
//	}
	
	/** 
	 * execute the node that make parameter passing
	 * then execute the CFG of the function,
	 * last, continue on the next node
	 * @throws IloException 
	 */
	public void visit(FunctionCallNode n) throws CFGVisitException, IloException {
		if(VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\nFunction call to " + n.getName());
		}
				
		// make a copy and a renaming of the CFG of the function
		// if it has already been called, we are in a backtrack,
		// and it is not necessary to duplicate it
		if ( !n.hasBeenCalled()){
			CFG newCFG = n.duplicateCFG();
			n.setCFG(newCFG);
		}		
		
		
		// add variables of this CFG
		for (Variable v : n.getCFG().getUsefulVar()){
			validationSystem.addVar(v);
		}
		
		// parameter passing
		executeBlock(n.getParameterPassing());

		// assignment of local var used to represent global var 
		executeBlock(n.getLocalFromGlobal());	

		// should execute :
		//   - the CFG of the function followed by  
		//   - the node after the function call node
		// to know where to continue after the function call
		n.getCFG().setContinueWith(n.getLeft());
		n.getCFG().firstNode().accept(this);
		
		if(VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\nEND of function call to " + n.getName()+ "\n");
		}

	}

	@Override
	public void visit(OnTheFlyWhileNode whileNode) throws CFGVisitException{
		// TODO Auto-generated method stub
		
	}
	
}
