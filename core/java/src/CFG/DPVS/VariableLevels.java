package CFG.DPVS;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.Map.Entry;

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
import CFG.SetOfCFG;
import CFG.WhileNode;

import expression.logical.Assignment;
import expression.variables.Variable;

public class VariableLevels implements Comparator<Variable>, CFGVisitor {
	
	private HashMap<Variable, int[]> levels;
	
	private Stack<Integer> nodeLevels;

	private HashSet<CFGNode> marked;
	
	private int curLevel;
	
	public VariableLevels(SetOfCFG pgm) throws IloException {
		levels = new HashMap<Variable, int[]>();
		nodeLevels = new Stack<Integer>();
		marked = new HashSet<CFGNode>();
		
		this.visitAssignments(pgm.getFieldDeclaration().getBlock());
		
		for(CFG cfg: pgm.methods()) {
			try {
				cfg.firstNode().accept(this);
			} catch (CFGVisitException e) {
				e.printStackTrace();
			}
		}
		
		//System.out.println("Variable levels:\n");
		//for (Entry<Variable, int[]> e: levels.entrySet()) {
			//System.out.println(e.getKey() + ": " + (e.getValue()[0]/e.getValue()[1]));
		//}
	}
	
	@Override
	public int compare(Variable v1, Variable v2) {
		int[] info1 = levels.get(v1);
		int[] info2 = levels.get(v2);
		
		return (info1[0]/info1[1]) - (info2[0]/info2[1]); 
	}
	
	/**************************************************/
	/****             CFG VISITOR                 *****/
	/**************************************************/
	
	private boolean checkJunction(CFGNode n) {
		if (n.isJunction()) {
			if (marked.add(n)) { //First visit of the junction node
				nodeLevels.push(curLevel);
				return false;
			}
			else { //Second (and last) visit of this junction node
				marked.remove(n);
				int level = nodeLevels.pop();
				if (curLevel < level) {
					curLevel = level;
				}
				return true;
			}
		}
		else {
			return true;
		}
	}
	
	private void updateLevels(Variable v, int level) {
		int[] info = levels.get(v);
		if (info == null) {
			levels.put(v, new int[] {level, 1});
		}
		else {
			info[0] += level;
			info[1]++;
		}
	}
		
	@Override
	public void visit(RootNode n) throws CFGVisitException, IloException {
		CFGNode firstNode = n.getLeft();
		if (firstNode != null)
			firstNode.accept(this);
		else
			System.err.println("Error (VariableLevels): empty CFG!");
	}

	public void visit(EnsuresNode n) throws CFGVisitException {
		//End of CFG visit
	}

	public void visit(RequiresNode n) throws CFGVisitException, IloException {
		// Cannot be a junction node
		n.getLeft().accept(this);
	}
 

	public void visit(AssertNode n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			n.getLeft().accept(this);
		}
	}
	
	public void visit(AssertEndWhile n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			n.getLeft().accept(this);
		}
	}

	private void visitBranchingNode(ConditionalNode branchingNode) throws CFGVisitException, IloException {
		if (checkJunction(branchingNode)) {
			curLevel++;
			int level = curLevel;
			branchingNode.getLeft().accept(this);
			curLevel = level;
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
		for (Assignment ass: block) {
			updateLevels(ass.lhs(), curLevel);
		}
	}

	public void visit(BlockNode n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			curLevel++;
			visitAssignments(n.getBlock());
			n.getLeft().accept(this);
		}
	}	
	
	public void visit(FunctionCallNode n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			curLevel++;
			// add variables in parameter passing assignments 
			visitAssignments(n.getParameterPassing().getBlock());
			// add global variables which are used in the function
			visitAssignments(n.getLocalFromGlobal().getBlock());
			
			//TODO : we should take care of the max level of the called function
			
			n.getLeft().accept(this);
		}
	}
	
	@Override
	public void visit(OnTheFlyWhileNode whileNode) {
		// TODO Auto-generated method stub
		
	}

}
