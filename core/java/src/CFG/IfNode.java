package CFG;

import ilog.concert.IloException;
import expression.logical.LogicalExpression;


// to represent an if node

public class IfNode extends ConditionalNode {

	
	// number of this if statement in the program
	// the number is set when the node is discovered during DFS 
	int ifNumber;
	
	public IfNode(int ifNumber,int ident,String functionName, LogicalExpression cond) {
		super(ident, functionName);
		condition = cond;
		this.ifNumber = ifNumber;
	}

	public IfNode(int ifNumber,int ident,String functionName) {
		super(ident,functionName);
		this.ifNumber = ifNumber;
	}
	
	public IfNode(IfNode n) {
		super(n);
		this.ifNumber = n.ifNumber;
	}
	
	public Object clone() {
		return new IfNode(this);
	}
	
	public int getIfNumber() {
		return ifNumber;
	}
	
	public String toString() {
		return "If number " + ifNumber + "\n" 
		 +  super.toString();
	}

	public void accept(CFGVisitor v) throws CFGVisitException, IloException {
		v.visit(this);
	}
	
	public IfNode accept(CFGClonerVisitor v) throws CFGVisitException {
		return v.visit(this);
	}
	
//	public void accept(MarkedDFSVisitor v) throws CFGVisitException {
//		if (!v.isMarked(nodeIdent)) {
//			v.mark(nodeIdent);
//			v.visit(this);
//		}
//	}

}
