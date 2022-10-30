package CFG;

import ilog.concert.IloException;

import java.util.ArrayList;

import java.util.HashSet;

import expression.logical.Assignment;

import validation.Validation;

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
import CFG.RequiresNode;
import CFG.SetOfCFG;
import CFG.WhileNode;
import CFG.OnTheFlyWhileNode;

/**
 * On ne propage pas les constantes éventuellement fixées dans la pré-condition... (A faire)  
 * @author Olivier
 *
 */
public class CodeDumper implements CFGVisitor {

	private HashSet<CFGNode> marked;

	private void dump(CFG cfg) throws IloException {
		try {			
			cfg.firstNode().accept(this);
		} catch (CFGVisitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void dump(SetOfCFG program) throws IloException {
		if (marked == null) {
			marked = new HashSet<CFGNode>();
		}
		else {
			marked.clear();
		}
		visitAssignments(program.getFieldDeclaration().getBlock());
		dump(program.getMethod(Validation.pgmMethodName));
	}

		
	/**************************************************/
	/****             CFG VISITOR                 *****/
	/**************************************************/
	
	private boolean checkJunction(CFGNode n) {
		if (n.isJunction()) 
			if (marked.add(n)) {
				System.out.println("}\nelse {");
				return false;
			}
			else {
				System.out.println("}");
				return true;
			}
		else 
			return true;
	}
	
	@Override
	public void visit(RootNode n) throws CFGVisitException, IloException {
		CFGNode firstNode = n.getLeft();
		if (firstNode != null)
			firstNode.accept(this);
		else
			System.err.println("Error (CodeDumper): empty CFG!");
	}

	public void visit(EnsuresNode n) throws CFGVisitException {
		if (checkJunction(n)) {
			System.out.println("/*@ ensure " + n.getCondition() + "@*/");
		}
		//End of CFG visit
	}

	public void visit(RequiresNode n) throws CFGVisitException, IloException {
		// Cannot be a junction node
		System.out.println("/*@ require " + n.getCondition() + "@*/");
		n.getLeft().accept(this);
	}
 

	public void visit(AssertNode n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			System.out.println("/*@ assert " + n.getCondition() + "@*/");
			n.getLeft().accept(this);
		}
	}
	

	public void visit(AssertEndWhile n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			System.out.println("/*@ assert " + n.getCondition() + "@*/");
			n.getLeft().accept(this);
		}
	}

	private void visitBranchingNode(ConditionalNode branchingNode) throws CFGVisitException, IloException {
		if (checkJunction(branchingNode)) {
			System.out.println("if (" + branchingNode.getCondition() + ") {");
			branchingNode.getLeft().accept(this);
			branchingNode.getRight().accept(this);
		}
	}
	
	public void visit(IfNode n) throws CFGVisitException, IloException {
		visitBranchingNode(n);
	}

	// WhileNode have been unfolded, they act like IfNode
	public void visit(WhileNode n) throws CFGVisitException, IloException {
		visitBranchingNode(n);
	}
		
	private void visitAssignments(ArrayList<Assignment> block){
		for (Assignment a: block) {
			System.out.println(a.lhs() + " = " + a.rhs() + ";");
		}
	}

	public void visit(BlockNode n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			visitAssignments(n.getBlock());
			n.getLeft().accept(this);
		}
	}	
	
	public void visit(FunctionCallNode n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			// add variables in parameter passing assignments 
			visitAssignments(n.getParameterPassing().getBlock());
			// add global variables which are used in the function
			try {
				visitAssignments(n.getLocalFromGlobal().getBlock());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			n.getLeft().accept(this);
		}
	}
	
	@Override
	public void visit(OnTheFlyWhileNode whileNode) {
		// TODO Auto-generated method stub
		
	}

}
