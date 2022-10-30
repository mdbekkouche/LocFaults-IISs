package CFG.DFS;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import CFG.AssertEndWhile;
import CFG.AssertNode;
import CFG.BlockNode;
import CFG.CFGNode;
import CFG.CFGVisitException;
import CFG.CFGVisitor;
import CFG.EnsuresNode;
import CFG.FunctionCallNode;
import CFG.IfNode;
import CFG.OnTheFlyWhileNode;
import CFG.RequiresNode;
import CFG.RootNode;
import CFG.WhileNode;
import CFG.simplification.VariableCollector;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.variables.Variable;


/** 
 * class to rename all variables used in the CFG 
 * according to a number of call
 * 
 * each variable fct_var_s where s is the SSA number is renamed fct_i_var_s
 *  where i is the number of the function call
 * this is useful when a same function is called many times in the same program
 * 
 * @author helen
 *
 */
public class RenamerVisitor implements CFGVisitor {
	
	// useful variables in the CFG
	private  TreeSet<Variable> usefulVar;
	
	// name of the called function
	private String functionName;
	
	// name of the current prefix of the function
	private String prefix;
	
	// mark if the current node has already been renamed
	// useful to do not rename many time the same node after a conditional node
	private boolean[] marked;

	
	public RenamerVisitor(String fctName, int numberOfNode, int callNumber, TreeSet<Variable> u) {
		marked = new boolean[numberOfNode];
		functionName = fctName;
		usefulVar = u;
		prefix = functionName + "_" + callNumber ;
	}
	
	public Collection<Variable> getUsefulVar() {
		return usefulVar;
	}
	
	private LogicalExpression visitCondition(LogicalExpression cond) {
		cond = (LogicalExpression)cond.setPrefixInFunction(functionName, prefix+"_");
		VariableCollector vb = new VariableCollector();
		TreeSet<Variable> ts = (TreeSet<Variable>)cond.structAccept(vb);
		for (Variable v : ts){
			if (!usefulVar.contains(v))
				usefulVar.add(v);
		}
		return cond;
	}
	

	@Override
	public void visit(RootNode n) throws CFGVisitException, IloException {
		CFGNode firstNode = n.getLeft();
		if (firstNode != null)
			firstNode.accept(this);
		else
			System.err.println("Error (RenamerVisitor): empty CFG!");
	}

	
	public void visit(EnsuresNode n) throws CFGVisitException {
		if (!marked[n.getIdent()]) {
			marked[n.getIdent()]=true;
			n.setKey(prefix);
			n.setCondition(visitCondition( n.getCondition()));
		}
	}


	public void visit(RequiresNode n) throws CFGVisitException, IloException {
		if (!marked[n.getIdent()]) {
			marked[n.getIdent()]=true;
			n.setKey(prefix);
			n.setCondition(visitCondition( n.getCondition()));		
			n.getLeft().accept(this);
		}
	}


	public void visit(AssertNode n) throws CFGVisitException, IloException {
		if (!marked[n.getIdent()]) {
			marked[n.getIdent()]=true;
			n.setKey(prefix);
			n.setCondition(visitCondition( n.getCondition()));
			n.getLeft().accept(this);
		}
	}


	public void visit(AssertEndWhile n) throws CFGVisitException, IloException {
		if (!marked[n.getIdent()]) {
			marked[n.getIdent()]=true;
			n.setKey(prefix);
			n.setCondition(visitCondition( n.getCondition()));
			n.getLeft().accept(this);
		}
	}


	public void visit(IfNode n) throws CFGVisitException, IloException {
		if (!marked[n.getIdent()]) {
			marked[n.getIdent()]=true;
			n.setKey(prefix);
			n.setCondition(visitCondition( n.getCondition()));
			n.getLeft().accept(this);
			n.getRight().accept(this);
		}
	}


	public void visit(WhileNode n) throws CFGVisitException, IloException {
		if (!marked[n.getIdent()]) {
			marked[n.getIdent()]=true;
			n.setKey(prefix);
			n.setCondition(visitCondition( n.getCondition()));
			n.getLeft().accept(this);
			n.getRight().accept(this);
		}
	}

	public void visitBlock(ArrayList<Assignment> b){
		for (int i=0;i<b.size();i++){
			Assignment ass = b.get(i);
			ass = (Assignment) ass.setPrefixInFunction(functionName, prefix+"_");

			//RHS variables should already exist or have been added to useful scalar 
			//variables before while renaming, so we only add the LHS
			
//			VariableCollector vb = new VariableCollector();
//			TreeSet<Variable> ts = (TreeSet<Variable>)ass.structAccept(vb);
//			for (Variable v : ts){
//				if (!usefulScalarVar.contains(v))
//					usefulScalarVar.add(v);
//			}
			if (!usefulVar.contains(ass.lhs()))
				usefulVar.add(ass.lhs());
			if (ass instanceof ArrayAssignment) {
				ArrayAssignment arrAss = (ArrayAssignment)ass;
				if (!usefulVar.contains(arrAss.previousArray()))
						usefulVar.add(arrAss.previousArray());
			}
			b.set(i, ass);
		}
	}
	
	public void visit(BlockNode n) throws CFGVisitException, IloException {
		if (!marked[n.getIdent()]) {
			marked[n.getIdent()]=true;
			n.setKey(prefix);
			visitBlock(n.getBlock());
			n.getLeft().accept(this);
		}
	}


	public void visit(FunctionCallNode n) throws CFGVisitException {
		// TODO Auto-generated method stub
		// not called unless there are recursive function calls
	}

	@Override
	public void visit(OnTheFlyWhileNode whileNode) {
		// TODO Auto-generated method stub
		
	}
	
//	/** method to rename the link node of a function call
//	 * only assigned variables must be renamed 
//	 * (because assigned variables are the variables of the called method, while variables
//	 * in the right hand side are formal parameters)
//	 * 
//	 * this is not a visit method, because the link node in a function call has no child
//	*/
//	public void renameLink(FunctionCallNode fct){
//		BlockNode link = fct.getParameterPassing();
//		visitBlock(link.getBlock());
//	}

	
	/**
	 * return the name of the variable when it is used in a function 
	 * which is called many times 
	 * the variable name is thus of the form :
	 *     fct_i_var_j
	 *        where i is the function call number and j the SSA renaming
	 *        @param : name : the name of the variable
	 */
//	public String nextNameInFunctionCall(String name) {
//		int first = name.indexOf('_');
//		int second = name.indexOf('_', first+1);
//		int third = name.indexOf('_', second+1);
//		if (third==-1) {
//			System.err.println("problem when getting the name of a variable in a function call");
//			System.exit(0);
//		}
//		String fct = name.substring(0, first);
//		if (fct.equals(functionName)) {
//			int numberCall = new Integer(name.substring(first+1,second));
//			numberCall++;  
//			return name.substring(0, first) + '_' + numberCall  + '_' + name.substring(second+1);
//		}
//		return name;
//	}

}
