package CFG.BottomUp;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import validation.util.Type;

import expression.logical.AndExpression;
import expression.logical.Assignment;
import expression.variables.Variable;
import expression.variables.Variable.Use;

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

/** 
 * The class visits a CFG in a bottom-up way in order to compute 
 *    anc[u]: the conditional nodes which are ancestors of u
 *    left[u,v]: is true when v is reachable from the left of 
 *        ancestor u
 *        
 * During the visit, an assertion node is transformed into a block node with 
 * the assertion assigned to a uniquely defined boolean variable. 
 * This variable is combined with the post-condition in an AND expression.
 * Thus, after this visitor, the CFG does not contain assertion nodes anymore!
 * 
 * @author helen
 *
 */
public class DPVSInformationVisitor implements CFGVisitor {

	/** map CFGNode keys to the conditional nodes they depend
	 * (cf anc[u] in DPVS algorithm)
	 */
	private HashMap<String, TreeSet<CFGNode>> conditionalAncestors;


	/** fromLeft[u,v] is true when condition of ancestor v has to be
	 * true to reach u (i.e. u is in the left branch of v) 
	 */
//	private boolean[][] fromLeft;
	private HashMap<String,TreeSet<String>> leftDependencies;
		
	/** nodes where variables are defined 
	 */
	private HashMap<String, ArrayList<CFGNode>> variableDefinitions;

	
	// mark if the current node has already been visited
	// useful to do not rename many time the same node after a conditional node
	private boolean[] marked;

	/**
	 * The CFG of the method that this visitor visits.
	 */
	CFG method;
	/**
	 * Stores if this visitor visits the verified method (<code>true</code>) or a 
	 * method called from the verified method (<code>false</code>).
	 */
	boolean isMethodToProve;
	/**
	 * Stores the post-condition's node which is needed when visiting an assertion.
	 * In a bottom-up traversal of the CFG, the post-condition's node,
	 * which is the last node of the graph, is always visited before any other node and
	 * in particular before assertion nodes.
	 * Note: we could get rid of this attribute considering that the post-condition's node
	 * is always (guaranteed?) the last node of a given CFG (i.e., here method.last()).
	 */
	private EnsuresNode postCondNode;
	/**
	 * Counts the assertions encountered while visiting {@link #method} CFG.
	 * The CFG traversal is bottom-up, so last assert in source code is given number 1 and first assert 
	 * in the code is given the greatest number.
	 */
	int assertionCounter = 1;
	
	public DPVSInformationVisitor(int numberOfNode, CFG method, boolean isMethodToProve) {
		this.method = method;
		this.isMethodToProve = isMethodToProve;
		marked = new boolean[numberOfNode];
//		fromLeft = new boolean[numberOfNode][numberOfNode];
		leftDependencies = new HashMap<String, TreeSet<String>>();
		conditionalAncestors = new HashMap<String, TreeSet<CFGNode>>();
		variableDefinitions = new HashMap<String, ArrayList<CFGNode>>();
	}
	
	////////////////////////////////////////////////////
	// tool methods
	////////////////////////////////////////////////////
	
	// to mark the node and continue exploration on the fathers
	private void accept(CFGNode n) throws CFGVisitException{
		//System.out.println("DPVSAncestorVisitor: visiting node " + n.getIdent());
		if (!marked[n.getIdent()]) {
			//mark the node
			marked[n.getIdent()]=true;
			// visit left father
			try {
				n.getLeftFather().accept(this);
			} catch (IloException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// visit right father
			if (n.hasRightFather()) {
				try {
					n.getRightFather().accept(this);
				} catch (IloException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// add the ancestors of the left and right fathers of n
			// into the set of ancestors of n
			addAncestors(n);
		}
	}
	
	/**
	 * return true if i is in the left branch of j
	 * @param i
	 * @param j
	 * @return
	 */
	private boolean isInLeftBranch(CFGNode ni, CFGNode nj) {
		String i = ni.getKey();
		String j = nj.getKey();
		return leftDependencies.containsKey(j)&&leftDependencies.get(j).contains(i);
	}
	
	/** 
	 * add ni in the dependencies of nj
	 *    ni is in the left branch of nj
	 * @param ni
	 * @param nj
	 */
	private void addLeftDependency(CFGNode ni, CFGNode nj){
		//System.out.println("adding " + ni.getKey() + " in left of " + nj.getKey());
		if (leftDependencies.containsKey(nj.getKey()))
			leftDependencies.get(nj.getKey()).add(ni.getKey());
		else {
			TreeSet<String> l = new TreeSet<String>();
			l.add(ni.getKey());
			leftDependencies.put(nj.getKey(),l);
		}
	}
	
	/**
	 * add the non direct ancestors of one father of n into the list of ancestors of n
	 * also manage leftDependencies
	 * 
	 * NB: similar to listOut.addAll(listIn) but also manage leftDependencies
	 * 
	 * @param n: the current node
	 * @param father: the father of n from which the ancestors are added
	 * @param listOut: the list to build. It contains the ancestors 
	 *        of n, the direct one and the ancestors from its father
	 * 
	 * PRECOND: n only has a left father, or one of its father has no ancestors
	 */
	private void addAncestorsInList(CFGNode n, CFGNode father, TreeSet<CFGNode> listOut ){
		if (conditionalAncestors.containsKey(father.getKey())) {
			TreeSet<CFGNode> listIn = conditionalAncestors.get(father.getKey());
			Iterator<CFGNode> iter = listIn.iterator();
			while (iter.hasNext()) {
				CFGNode next = iter.next();
				listOut.add(CFGNode.clone(next));
				if (isInLeftBranch(father,next))
					addLeftDependency(n,next);
				//			fromLeft[n.getIdent()][next.getIdent()]=
				//				fromLeft[n.getLeftFather().getIdent()][next.getIdent()];
			}
		}
	}
	
	
	/** 
	 * add the ancestors of the fathers of n
	 *
	 * if a node "an" is an ancestor of "n", and if "n" is reachable both from 
	 * the left branch and the right branch of "an", this means 
	 * that n does not depend on "an".
	 * 
	 * @param nodeIdent
	 * @param n
	 */
	private void addAncestors(CFGNode n){
		CFGNode leftFather = n.getLeftFather();
		
		// conditional ancestors of n
		TreeSet<CFGNode> list = 
			conditionalAncestors.containsKey(n.getKey())?
					conditionalAncestors.get(n.getKey()):new TreeSet<CFGNode>();

		// n only has a left father : add the ancestor of it left father
		if (!n.hasRightFather()) {
			//if (conditionalAncestors.get(leftFather.getKey())!=null)
				//System.out.println(leftFather.getKey());
				addAncestorsInList(n,leftFather, list);
		}
		else {
			CFGNode rightFather = n.getRightFather();
			TreeSet<CFGNode> leftAncestors = conditionalAncestors.get(leftFather.getKey());
			TreeSet<CFGNode> rightAncestors = conditionalAncestors.get(rightFather.getKey());
			
			// add the ancestors of the left and the right fathers
			// only if they are different
			if (leftAncestors.isEmpty()) {
				addAncestorsInList(n,rightFather,list);
			}
			else {
				if (rightAncestors.isEmpty())
					addAncestorsInList(n,leftFather,list);
				else {
					// there exist some ancestors for the left and the right fathers
					// only add those that do not belong to both right and left
					Iterator<CFGNode> iter = leftAncestors.iterator();
					while (iter.hasNext()) {
						CFGNode next = iter.next();
						if (!rightAncestors.contains(next)){
							list.add(CFGNode.clone(next));
							if (isInLeftBranch(leftFather,next))
								addLeftDependency(n, next);
//							fromLeft[n.getIdent()][next.getIdent()]=
//								fromLeft[leftFather.getIdent()][next.getIdent()];
						}
						else {
							// if the same node belongs to the left and the right
							// ancestors of n, it is added only if it is reachable from
							// the same branch of the ancestor
							if (isInLeftBranch(leftFather, next) && isInLeftBranch(rightFather, next)
									|| (!isInLeftBranch(leftFather, next) && !isInLeftBranch(rightFather, next))) {
//							if (fromLeft[leftFather.getIdent()][next.getIdent()]==
//								fromLeft[rightFather.getIdent()][next.getIdent()]) {
								list.add(CFGNode.clone(next));
								if (isInLeftBranch(leftFather, next) && isInLeftBranch(rightFather, next))
									addLeftDependency(n, next);
//								fromLeft[n.getIdent()][next.getIdent()]=
//									fromLeft[leftFather.getIdent()][next.getIdent()];
							}
						}
					}
					iter = rightAncestors.iterator();
					while (iter.hasNext()) {
						CFGNode next = iter.next();
						if (!leftAncestors.contains(next)){
							list.add(CFGNode.clone(next));
							if (isInLeftBranch(rightFather,next))
								addLeftDependency(n, next);
//							fromLeft[n.getIdent()][next.getIdent()]=
//								fromLeft[leftFather.getIdent()][next.getIdent()];
						}
//						else {
//							if (isInLeftBranch(leftFather, next) && isInLeftBranch(rightFather, next)
//									|| (!isInLeftBranch(leftFather, next) && !isInLeftBranch(rightFather, next))) {
////							if (fromLeft[leftFather.getIdent()][next.getIdent()]==
////								fromLeft[rightFather.getIdent()][next.getIdent()]) {
//								list.add(next);
//								if (isInLeftBranch(leftFather, next) && isInLeftBranch(rightFather, next))
//									addLeftDependency(n, next);
////								fromLeft[n.getIdent()][next.getIdent()]=
////									fromLeft[leftFather.getIdent()][next.getIdent()];
//
//							}
//						}
					}
				}

			}
		}
		conditionalAncestors.put(n.getKey(), list);
	}
	
	// add n to the ancestors of node number nodeIdent
	private void addOneAncestor(String nodeIdent,CFGNode n){
		TreeSet<CFGNode> list = 
			conditionalAncestors.containsKey(nodeIdent)?
					conditionalAncestors.get(nodeIdent):new TreeSet<CFGNode>();
		// if n is conditional, add node n to the ancestors 
		// of nodeIdent
		if (n.isConditional()) {
			list.add(ConditionalNode.clone((ConditionalNode)n));
		}
		conditionalAncestors.put(nodeIdent, list);
	}


	public HashMap<String, TreeSet<CFGNode>> getAncestors() {
		return conditionalAncestors;
	}
	
	
	public HashMap<String,TreeSet<String>> getLeftDependencies() {
		return leftDependencies;
//		HashMap<String,ArrayList<String>> inLeftBranch = new HashMap<String, ArrayList<String>>();
//		for (int i=0;i<fromLeft.length;i++)
//			for (int j=0;j<fromLeft.length;j++) {
////				if (fromLeft[i][j]) {
////					ArrayList<Integer> list = 
////						inLeftBranch.containsKey(j)?
////								inLeftBranch.get(j):new ArrayList<Integer>();
////					list.add(i);
////					inLeftBranch.put(j, list);
////				}
//					
//			}
//		return inLeftBranch;
	}
	
	public HashMap<String, ArrayList<CFGNode>> getVarDefinitions() {
		return variableDefinitions;
	}
	                 
	////////////////////////////////////////////////////
	// visit methods
	////////////////////////////////////////////////////

	@Override
	public void visit(EnsuresNode n) throws CFGVisitException {
		postCondNode = n;
		accept(n);
	}

	@Override
	public void visit(RequiresNode n) throws CFGVisitException {
		if (!marked[n.getIdent()]) {
			marked[n.getIdent()]=true;
		}	
	}

	/* (non-Javadoc)
	 * @see CFG.CFGVisitor#visit(CFG.AssertNode)
	 * 
	 * The assertion node is transformed into a block node with one assignment of the assertion to a uniquely 
	 * defined boolean variable. 
	 * This variable is combined with the post-condition in an AND expression.
	 * 
	 */
	@Override
	public void visit(AssertNode assertNode) throws CFGVisitException {
		// Assertions in called methods are ignored
		if (!this.isMethodToProve) {
			accept(assertNode);
		}
		else {
			// The newly created variable must have a unique name.
			// All program variables are already in DSA form and end with '_num' so they won't be confused
			// with the name given here. However, there may be an ambiguity in case of recursive calls of the 
			// verified method, but recursive calls seems not to be handled. 
			Variable assertionVar = new Variable(method.name() + "_Assertion" + this.assertionCounter++, Type.BOOL, Use.LOCAL);
			method.getUsefulVar().add(assertionVar);
			postCondNode.setCondition(new AndExpression(assertionVar, postCondNode.getCondition()));
			
			// Creates a new block node and substitutes it to the assertion node 
			ArrayList<Assignment> assignments = new ArrayList<Assignment>(1);
			assignments.add(new Assignment(assertionVar, assertNode.getCondition()));
			BlockNode block = new BlockNode(assertNode.getIdent(), this.method.name(), assignments);
			
			// Link assertion's child (always left) to new block's left child
			// An assertion node can be the left or the right father of its child.
			CFGNode assertNodeChild = assertNode.getLeft();
			block.setLeft(assertNodeChild);
			if (assertNodeChild.getRightFather() == assertNode) {
				assertNodeChild.setRightFather(block);
			}
			else {
				assertNodeChild.setLeftFather(block);
			} 
			
			// Link assertion's father(s) to new block's father(s)
			CFGNode assertNodeLeftFather = assertNode.getLeftFather();
			block.setLeftFather(assertNodeLeftFather);
			if (assertNode.hasRightFather()) {
				CFGNode assertNodeRightFather = assertNode.getRightFather();
				block.setRightFather(assertNodeRightFather);
				assertNodeLeftFather.setLeft(block);
				assertNodeRightFather.setLeft(block);
			}
			else {
				if (assertNodeLeftFather.getLeft() == assertNode) {
					assertNodeLeftFather.setLeft(block);
				}
				else {
					assertNodeLeftFather.setRight(block);
				}
			}
			// Collect needed information for DPVS on the newly created block
			visit(block);
		}
	}

	@Override
	public void visit(AssertEndWhile n) throws CFGVisitException {
		accept(n);		
	}

	@Override
	public void visit(IfNode n) throws CFGVisitException {
		// add n to the ancestors of its child
		// and mark the branch from which the child 
		// can be reached from n
		addOneAncestor(n.getLeft().getKey(), n);
		addLeftDependency(n.getLeft(),n);
		if (n.hasRight()) {
			addOneAncestor(n.getRight().getKey(), n);
		}
		// explore node above n
		accept(n);
	}

	@Override
	public void visit(WhileNode n) throws CFGVisitException {
		addOneAncestor(n.getLeft().getKey(), n);
		addLeftDependency(n.getLeft(), n);
		if (n.hasRight()) {
			addOneAncestor(n.getRight().getKey(), n);
		}
		// explore node above n
		accept(n);
		
//		accept(n);
//		// add n and its ancestors to the ancestors of its child
//		addOneAncestor(n.getLeft().getKey(), n);
//		if (n.hasRight())
//			addOneAncestor(n.getRight().getKey(), n);			
	}

	// associate block n with all variables defined in n
	private void visitBlock(BlockNode n){
		ArrayList<Assignment> b = n.getBlock();
		for (int i=0;i<b.size();i++){
			Assignment ass = b.get(i);
			String name = ass.lhs().name();
			ArrayList<CFGNode> list = 
				variableDefinitions.containsKey(name)?
						variableDefinitions.get(name):new ArrayList<CFGNode>();
			list.add(new BlockNode(n));
			variableDefinitions.put(name, list);
		}
	}
	
	@Override
	public void visit(BlockNode n) throws CFGVisitException {
		accept(n);
		addOneAncestor(n.getKey(), n.getLeftFather());
		visitBlock(n);
		if (n.hasRight())
			addOneAncestor(n.getKey(), n.getRightFather());	
	}

	@Override
	// continue the visit on the previous nodes
	// the nodes in the CFG of function n will be visited on the fly  
	// by DPVS algorithm when a FunctionCallNode is reached
	public void visit(FunctionCallNode n) throws CFGVisitException {
		accept(n);
		addOneAncestor(n.getKey(), n.getLeftFather());
	}

	@Override
	public void visit(OnTheFlyWhileNode whileNode) throws CFGVisitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(RootNode n) throws CFGVisitException {
		System.err.println("Error (DPVSInformationVisitor): reached root node!");
		System.exit(-1);
	}
	
}
