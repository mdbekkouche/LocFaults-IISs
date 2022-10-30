package CFG;

import ilog.concert.IloException;
import expression.Expression;
import expression.logical.LogicalExpression;

// to represent a while node which has been statically unwound

public class WhileNode extends ConditionalNode {
	
	
	// number of this if statement in the program
	// the number is set when the node is discovered during DFS 
	private int whileNumber;

	//TODO: indispensable ?
	public WhileNode(int whileNumber,int ident,String functionName) {
		super(ident, functionName);
		this.whileNumber = whileNumber;
	}

	public WhileNode(int whileNumber, int ident, String functionName, LogicalExpression cond) {
		super(ident, functionName);
		condition = cond;
		this.whileNumber = whileNumber;
	}
	
	public WhileNode(WhileNode n) {
		super(n);
		this.whileNumber = n.whileNumber;
	}

	public Object clone() {
		return new WhileNode(this);
	}
	
	public String toString() {
		return "While number " + whileNumber + "\n" + 
			super.toString();
	}
	
	public void setCondition(Expression cond){
		condition = cond;
	}

	public LogicalExpression getCondition(){
		return (LogicalExpression)condition;
	}
	
	public int whileNumber() {
		return whileNumber;
	}
	
	public void accept(CFGVisitor v) throws CFGVisitException, IloException {
		v.visit(this);
	}
	
	public WhileNode accept(CFGClonerVisitor v) throws CFGVisitException {
		return v.visit(this);
	}
	
//	public void accept(MarkedDFSVisitor v) throws CFGVisitException {
//		if (!v.isMarked(nodeIdent)) {
//			v.mark(nodeIdent);
//			v.visit(this);
//		}
//	}
	
}
