package CFG;

import java.util.ArrayList;

import ilog.concert.IloException;
import validation.Validation.VerboseLevel;

/** class to represent a CFG node 
 * 
 */
public abstract class CFGNode implements Comparable<CFGNode>, Cloneable {
	
	/**
	 * The line number in the source file where starts the 
	 * statement corresponding to this node. 
	 * A value of 0 means it has not been set yet.
	 */
	public int startLine = 0;
	

	/**
	* The start position in the source file is where starts the  
	* statement corresponding to this node.  
	* A value of 0 means it has not been set yet.
	*/
	public int startPosition = 0;
	
	/**
	* The length of the node
	* A value of 0 means it has not been set yet.
	*/
	public int length = 0;
	
	/**
	 * Identifying instructions, typically used for instruction in programs with loops.
	 */
	public String IDLine = "";
	
	
	// node identifier
	protected int nodeIdent; 

	// node key : it is equal to nodeIdent in the main program
	//            but it is prefixed by the name and the number of call
	//            in a function (this is done by a call to RenamerVisitor)
	protected String key;
	
	// first child
	protected CFGNode left;
	// second child, null if the node is not conditional
	protected CFGNode right;
	// fathers, at most 2 fathers because program only contains if else 
	protected CFGNode leftFather;
	protected CFGNode rightFather;
	
	// constructors
	
	/** to build an initial node 
	 * @param ident : ident number
	 * @param post : the name of the function and its number of call 
	 */
	public CFGNode(int ident, String post) {
		nodeIdent = ident;
		setKey(post);
		left = null;
		right = null;
		leftFather = null;
		rightFather = null;
	}
	
	public CFGNode(int nodeident, String key, CFGNode l, CFGNode r, CFGNode lf, CFGNode rf) {
		this.nodeIdent=nodeident;
		this.key = key;
		this.left=l;
		this.right = r;
		this.leftFather = lf;
		this.rightFather = rf;
	}
	
	public abstract Object clone();
	
	public static CFGNode clone(CFGNode n) {
		if (n==null)
			return n;
		if (n instanceof ConditionalNode) 
			return ConditionalNode.clone((ConditionalNode)n);
		if (n instanceof FunctionCallNode)
			return new FunctionCallNode((FunctionCallNode)n);
		return new BlockNode((BlockNode)n);
	}

	public int getIdent() {
		return nodeIdent;
	}
	
	/**
	 * 
	 * @return: the key of the node
	 * it is either nodeIdent (in the main method) or 
	 * functName_#call_nodeIdent (in a call to functName)
	 */
	public String getKey() {
		return key;
	}
	
//	/** to obtain the method name from the key
//	 * 
//	 * @return : the method name
//	 */
//	public String getMethod() {
//		int i = key.indexOf("]");
//		return key.substring(i);
//	}
	
	/**
	 * to set the key of the node according to a prefix
	 * @param prefix: gives the name of the function and its number of calls
	 */
	public void setKey(String post) {
		key = "[" + nodeIdent + "]" + post;
	}
	
	public int compareTo(CFGNode n) {
		return this.nodeIdent-n.nodeIdent;
	}
	
	// methods to link the nodes
	public void setLeft(CFGNode l){
		left = l;
	}
	
	public void setRight(CFGNode r){
		right = r;
	}
	
	public void setLeftFather(CFGNode f){
		leftFather = f;
	}
	
	public void setRightFather(CFGNode f){
		rightFather = f;
	}
	
	// methods to get left and right childs
	public CFGNode getLeft() {
		return left;
	}
	
	public CFGNode getRight() {
		return right;
	}
	
	// methods to get left and right fathers
	public CFGNode getLeftFather() {
		return leftFather;
	}
	
	public CFGNode getRightFather() {
		return rightFather;
	}
	
	// method to determine final nodes
	protected boolean isFinal() {
		return left==null && right==null;
	}
	
	// true if the node is conditional
	public boolean isConditional() {
		return this  instanceof IfNode || this  instanceof WhileNode;
	}
	
	// to know if the node has at least one father
	public boolean hasAFather() {
		return hasRightFather() || hasLeftFather();
	}
	
	// true if the node is a junction node
	public boolean isJunction() {
		return hasRightFather() && hasLeftFather();	
	}
	
	// to know if there exists a right father
	public boolean hasRightFather() {
		return rightFather!=null;
	}
	
	// to know if there exists a left father
	public boolean hasLeftFather() {
		return leftFather!=null;
	}
	
	// to know if there exists a right child
	public boolean hasRight() {
		return right!=null;
	}
	
	// to know if there exists a left child
	public boolean hasLeft() {
		return left!=null;
	}
	
	// method to accept a visitor to visit the CFG (cf class CFGVisitor)
	public abstract void accept(CFGVisitor v) throws CFGVisitException, IloException;
	
	// method to accept a visitor to visit the CFG (cf class CFGClonerVisitor)
	public abstract CFGNode accept(CFGClonerVisitor v) throws CFGVisitException;
	

	// to know if the node is empty (i.e contains no instruction block)
	public abstract boolean isEmpty();

	/** to know if a node can be removed
	 * a node can't be removed if both itself and its child have a right father 
	 * (a node can only have 2 fathers)
	 * 
	 * @return true if 
	 *    the node has at least a father and
	 *    the node has no right father or if its left child has no father
	 */
	public boolean isRemovable(){
		return  hasAFather() && (rightFather==null || left.rightFather==null) ;
	}
	
	/**
	 * Removes this node from the CFG it belongs to.
	 * 
	 * We assume the node is removable (<code>this.isRemovable() == true</code>) and 
	 * that the node that should replace a branching node (which is a node with two children) 
	 * is the left child of the branching node which is its left father.
	 * 
	 * @param message
	 */
	public void remove(String message) {
		// Link this node's child to this node's left father

		if (this.isLeftChildOf(leftFather)) {
			leftFather.left = left;
		}
		else {
			leftFather.right = left;
		}
		
		if (this.isLeftFatherOf(left)) {
			left.leftFather = leftFather;
			// Link this node's child to this node's right father, if need be
			if (rightFather != null) {
				// This node has a right father so its child does not have a right father 
				// (otherwise this node would not be removable).
				left.rightFather = rightFather;
				if (this.isLeftChildOf(rightFather)) {
					rightFather.left = left;
				}
				else {
					rightFather.right = left;
				}			
			}
			else {
				// In case of a branching node, left.rightFather may be not null and must be reset 
				// even if this node has no right father.
				if (right != null) 
					left.rightFather = null;				
			}
		}
		else{				
			// We can assume here that this node has no right father (otherwise this node would not have been removable).
			left.rightFather = leftFather;
		}	
			
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Node " + nodeIdent + " has been removed " + message);
		}
		
		// If we have removed a branching node, while it was not possible before, now 
		// the corresponding junction node may be removed.
		if (right != null && left.isEmpty() && left.isRemovable()) {
			left.remove("because the branching node corresponding to this junction node has been removed.");
		}

	}
	
	public boolean isLeftChildOf(CFGNode father) {
		return father != null && father.left == this;
	}

	public boolean isRightChildOf(CFGNode father) {
		return father != null && father.right == this;
	}
	
	public boolean isLeftFatherOf(CFGNode child){
		return child!=null && child.leftFather == this;
	}

	public boolean isRightFatherOf(CFGNode child){
		return child!=null && child.rightFather == this;
	}
	
	
	/** textual printing */
	
	// to print the fathers
	protected String fathers() {
		String s = "";
		if (leftFather!=null)
			s += "Left father: " +leftFather.nodeIdent;
		else
			s += "Left father empty";
		if (rightFather!=null)
			s += "\nRight father: " + rightFather.nodeIdent;
		else
			s += "\nRight father empty";
		return s;
	}
	
	// to print the child
	protected String child() {
		String s = "";
		if (! isFinal()) {
			s += "Left child: " + left.nodeIdent + "\n";
			if (right!=null)
				s += "Right child: " + right.nodeIdent;
			else 
				s+="Right child empty";
		}
		else 
			s +="FINAL node";
			
		return s;
	}
		
	// to print  node information
	public  String toString(){
		String s = "Ident: " + nodeIdent;
		s+="\nKey: " + key;
		s+="\nStart line in source code: " + startLine;
		s+="\nStart position in the source code: " + startPosition;
		s+="\nlength of the node: " + length;
		// print fathers
		s += "\n" + fathers();
		// print childs
		s += "\n" + child();
		
		return s;
	}

	/** useful for debugging
	 to print the subgraph that starts with this node
	 PRECOND : the subgraph has less than 300 nodes */
	public String printSubgraph() {
		CFG c = new CFG("CFG starting at node " + key);
		c.setNodeNumber(300);
		c.setFirstNode(this);
   		return c.toString();
	}
	
}
