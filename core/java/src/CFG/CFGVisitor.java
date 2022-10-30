package CFG;

import ilog.concert.IloException;


/** class to visit a CFG
 * 
 * Can be used to simulate the behavior of the program when input data are provided or to 
 * perform a formal verification.
 * 
 * Each visit method makes an action on the expression of the CFG node and 
 * returns the next node to visit
 * 
 * @author helen
 *
 */
public interface CFGVisitor {
	
	public void visit(RootNode n) throws CFGVisitException, IloException;

	public void visit(EnsuresNode n) throws CFGVisitException, IloException;
	
	public void visit(RequiresNode n) throws CFGVisitException, IloException;
	
	public void visit(AssertNode n) throws CFGVisitException, IloException;
	
	public void visit(AssertEndWhile n) throws CFGVisitException, IloException;
	
	public void visit(IfNode n) throws CFGVisitException, IloException;
	
	public void visit(WhileNode n) throws CFGVisitException, IloException;
	
	public void visit(BlockNode n) throws CFGVisitException, IloException;

	public void visit(FunctionCallNode n) throws CFGVisitException, IloException;

	public void visit(OnTheFlyWhileNode whileNode) throws CFGVisitException;

}

