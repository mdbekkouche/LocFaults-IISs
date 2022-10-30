package CFG.DFS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.TreeSet;

import CFG.AssertEndWhile;
import CFG.AssertNode;
import CFG.BlockNode;
import CFG.CFG;
import CFG.CFGClonerVisitor;
import CFG.CFGNode;
import CFG.CFGVisitException;
import CFG.ConditionalNode;
import CFG.EnsuresNode;
import CFG.FunctionCallNode;
import CFG.IfNode;
import CFG.OnTheFlyWhileNode;
import CFG.RequiresNode;
import CFG.WhileNode;
import CFG.simplification.VariableCollector;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.variables.ArrayVariable;
import expression.variables.Variable;


/** 
 * class to rename all variables used in the CFG 
 * according to a number of call
 * 
 * make a deep copy of the CFG
 * use a DFS visit that stops on junction nodes
 * 
 * each variable fct_var_s where s is the SSA number is renamed fct_i_var_s
 * where i is the number of the function call
 * this is useful when a same function is called many times in the same program
 * 
 * @author helen
 *
 */
public class ClonerAndRenamerVisitor implements CFGClonerVisitor {
	
	// useful variables in the CFG
	private  TreeSet<Variable> usefulVar;
		
	// name of the called function
	private String functionName;
	
	// name of the current prefix of the function
	private String prefix;
	
	// mark if the current node has already been renamed
	// useful to do not rename many time the same node after a conditional node
	private boolean[] marked;

	// the last node of the CFG
	// this is necessary because the new last node must be assigned
	// the the last node attribute of the CFG (for function calls)
	private CFGNode last;
	
	// the node from where one comes
	private CFGNode previous;
	
	// the stack of junction nodes
	private Stack<CFGNode> junctionNode ;
	
	public ClonerAndRenamerVisitor(String fctName, int numberOfNode, int callNumber, TreeSet<Variable> u,TreeSet<ArrayVariable> au) {
		marked = new boolean[numberOfNode];
		functionName = fctName;
		usefulVar = u;
		prefix = functionName + "_" + callNumber ;
		last=null;
		junctionNode = new Stack<CFGNode>();
	}
	
	public ClonerAndRenamerVisitor(String fctName, int numberOfNode, int callNumber) {
		marked = new boolean[numberOfNode];
		functionName = fctName;
		usefulVar = new TreeSet<Variable>();
		prefix = functionName + "_" + callNumber ;
		last=null;
		junctionNode = new Stack<CFGNode>();
	}
	
	public CFGNode last() {
		return last;
	}
	
	public Collection<Variable> getUsefulVar() {
		return usefulVar;
	}
	
	
	/**
	 * finish the visit of a CFGNode <-> explore the nodes below the current
	 * junction node 
	 * 
	 * this is necessary for a DFS visit that stops on junction nodes
	 * 
	 * @param n
	 * @return the node with updated value of left and right child
	 * @throws CFGVisitException
	 */
	private CFGNode endVisit(CFGNode n) throws CFGVisitException {
		n.setLeft(n.getLeft().accept(this));
		if (n.hasRight()) {
			previous = n;
			n.setRight(n.getRight().accept(this));
		}
		return n;
	}
	
	/**
	 * visit a condition
	 * @param cond
	 * @return cond where variables have been renamed
	 */
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
	
	/**
	 * duplicate a conditional node
	 * @param n
	 * @return the clone of n
	 */
	private ConditionalNode duplicate(ConditionalNode n){
		ConditionalNode res = (ConditionalNode)n.clone();
		res.setCondition(visitCondition(n.getCondition()));
		res.setKey(prefix);
		return res;
	}
	
	/**
	 * duplicate a block node
	 * @param n
	 * @return the clone of n
	 */
	private BlockNode duplicate(BlockNode n){
		BlockNode res = (BlockNode)n.clone();
		res.setKey(prefix);
		visitBlock(res.getBlock());
		return res;
	}
	
	/**
	 * to rename and clone the EnsuresNode
	 * it is the last node of the CFG
	 */
	public EnsuresNode visit(EnsuresNode n) throws CFGVisitException {
		if (!n.isJunction()) {
			EnsuresNode res = (EnsuresNode)duplicate(n);
			res.setLeftFather(previous);
			// this is the last node of the CFG being visited
			last = res;
			return res;
		}
		else {
			if (!marked[n.getIdent()]) {
				marked[n.getIdent()]=true;
				EnsuresNode res = (EnsuresNode)duplicate(n);
				res.setLeftFather(previous);
				// this is the last node of the CFG being visited
				last = res;
				junctionNode.push(res);
				return res;
			}
			else {
				EnsuresNode next = (EnsuresNode) junctionNode.pop();
				next.setRightFather(previous);
				return next;
			}
		}
	}

	/** to rename and clone a node which is either :
	 *   - a RequiresNode
	 *   - an AssertNode
	 *   - an AssertEndWhile node
	 * @param n : the node
	 * @return : the renamed node
	 * @throws CFGVisitException
	 */
	private ConditionalNode visitRequireOrAssertNode(ConditionalNode n )throws CFGVisitException {
		if (!n.isJunction()) {
			ConditionalNode res = duplicate(n);
			res.setLeftFather(previous);
			previous = res;
			return (ConditionalNode) endVisit(res);
		}
		else {
			if (!marked[n.getIdent()]){
				marked[n.getIdent()]=true;
				ConditionalNode res = duplicate(n);				
				res.setLeftFather(previous);
				junctionNode.push(res);
				return res;
			}
			else {
				ConditionalNode next = (ConditionalNode) junctionNode.pop();
				next.setRightFather(previous);
				previous = next;
				return (ConditionalNode) endVisit(next);
			}
		}
	}
	
	public RequiresNode visit(RequiresNode n) throws CFGVisitException {
		return (RequiresNode)visitRequireOrAssertNode(n);
	}

	public AssertNode visit(AssertNode n) throws CFGVisitException {
		return (AssertNode)visitRequireOrAssertNode(n);
	}

	public AssertEndWhile visit(AssertEndWhile n) throws CFGVisitException {
		return (AssertEndWhile)visitRequireOrAssertNode(n);
	}
	
	/**
	 * to visit a branch instruction
	 * 
	 * @param n : the node
	 * @return : the renamed node
	 * @throws CFGVisitException
	 */
	private ConditionalNode visitIfOrWhileNode(ConditionalNode n) throws CFGVisitException {

		if (!n.isJunction()) {
			ConditionalNode res = duplicate(n);
			res.setLeftFather(previous);
			previous = res;
			return (ConditionalNode) endVisit(res);
		}
		else {
			if (!marked[n.getIdent()]){
				marked[n.getIdent()]=true;
				ConditionalNode res = duplicate(n);
				res.setLeftFather(previous);
				junctionNode.push(res);
				return res;
			}
			else {
				ConditionalNode next = (ConditionalNode) junctionNode.pop();
				next.setRightFather(previous);
				previous = next;
				return (ConditionalNode) endVisit(next);
			}
		}
	}

	public IfNode visit(IfNode n) throws CFGVisitException {
		return (IfNode)visitIfOrWhileNode(n);
	}

	public WhileNode visit(WhileNode n) throws CFGVisitException {
		return (WhileNode)visitIfOrWhileNode(n);
	}

	/**
	 * visit a block
	 * @param b
	 * POST : all assignments of the block have been renamed
	 */
	public void visitBlock(ArrayList<Assignment> b){
		for (int i=0;i<b.size();i++){
			Assignment ass = b.get(i);
			ass = (Assignment) ass.setPrefixInFunction(functionName, prefix+"_");

			if (!usefulVar.contains(ass.lhs()))
				usefulVar.add(ass.lhs());
			b.set(i, ass);
		}
	}
//	
//	/**
//	 * visit a block
//	 * @param b
//	 * POST : all assignments of the block have been renamed
//	 */
//	public ArrayList<Assignment> visitBlock(ArrayList<Assignment> b){
//		ArrayList<Assignment> res = new ArrayList<Assignment>();
//		for (int i=0;i<b.size();i++){
//			Assignment ass = b.get(i);
//			ass = (Assignment) ass.setPrefixInFunction(functionName, prefix+"_");
//
//			if (!usefulScalarVar.contains(ass.lhs()))
//				usefulScalarVar.add(ass.lhs());
//			res.add(ass);
//		}
//		return res;
//	}
	
	/**
	 * to visit a block node
	 * 
	 */
	public BlockNode visit(BlockNode n) throws CFGVisitException {

		if (!n.isJunction()) {
			BlockNode res = duplicate(n);
			res.setLeftFather(previous);
			previous = res;
			return (BlockNode) endVisit(res);
		}
		else {
			if (!marked[n.getIdent()]) {
				marked[n.getIdent()]=true;
				BlockNode res = duplicate(n);
				res.setLeftFather(previous);
				junctionNode.push(res);
				return res;
			}
			else {
				BlockNode next = (BlockNode) junctionNode.pop();
				next.setRightFather(previous);
				previous = next;
				return (BlockNode) endVisit(next);
			}
		}
	}


	/* (non-Javadoc)
	 * @see CFG.CFGClonerVisitor#visit(CFG.FunctionCallNode)
	 * 
	 * We may visit a FunctionVallNode while cloning and renaming when a called method calls another method.
	 */
	public FunctionCallNode visit(FunctionCallNode fctCall) throws CFGVisitException {
		FunctionCallNode fctCallClone;
		
		if (!fctCall.isJunction()) {
			fctCallClone = (FunctionCallNode)fctCall.clone();
			fctCallClone.setLeftFather(previous);
			CFG CFGClone = fctCallClone.duplicateCFG();
			fctCallClone.setCFG(CFGClone);
			this.usefulVar.addAll(CFGClone.getUsefulVar());
			previous = fctCallClone;
			return (FunctionCallNode)endVisit(fctCallClone);
		}
		else {
			if (!marked[fctCall.getIdent()]) {
				marked[fctCall.getIdent()]=true;
				fctCallClone = (FunctionCallNode)fctCall.clone();
				fctCallClone.setLeftFather(previous);
				CFG CFGClone = fctCallClone.duplicateCFG();
				fctCallClone.setCFG(CFGClone);
				this.usefulVar.addAll(CFGClone.getUsefulVar());
				junctionNode.push(fctCallClone);
				return fctCallClone;
			}
			else {
				fctCallClone = (FunctionCallNode)junctionNode.pop();
				fctCallClone.setRightFather(previous);
				previous = fctCallClone;
				return (FunctionCallNode)endVisit(fctCallClone);
			}
		}
	}

	@Override
	public OnTheFlyWhileNode visit(OnTheFlyWhileNode whileNode) {
		// TODO Auto-generated method stub
		return null;
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
