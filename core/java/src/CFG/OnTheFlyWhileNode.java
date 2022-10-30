package CFG;

import expression.Expression;
import expression.logical.LogicalExpression;

/** to represent a while node 
 * this node contains the condition, the body of the while statement
 * and information on the number of loop unfolding 
 * 
 * This kind of while node must be unwound on the fly
*/

public class OnTheFlyWhileNode extends ConditionalNode {
	
	// number of this if statement in the program
	// the number is set when the node is discovered during DFS 
	private int whileNumber;
	
	// maximum number of unwinding
	private int maxUnwound;
	
	// current number of unwinding for this while 
	private int currentUnwound;
	
	/** CFG associated with the body of the loop */
	private CFG body;

	public OnTheFlyWhileNode(int whileNumber,int ident, String functionName, CFG body, LogicalExpression cond,int maxUnw) {
		super(ident,functionName);
		this.body = body;
		this.condition = cond;
		this.whileNumber = whileNumber;
		this.currentUnwound = 0;
		this.maxUnwound = maxUnw;
	}
	
	public OnTheFlyWhileNode(OnTheFlyWhileNode n) {
		super(n);
		this.whileNumber = n.whileNumber;
		this.body = n.body;
		this.currentUnwound = n.currentUnwound;
		this.maxUnwound = n.maxUnwound;
	}

	public Object clone() {
		return new OnTheFlyWhileNode(this);
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
	
	private void unwind() {
		
	}
	
	public void accept(CFGVisitor v) throws CFGVisitException {
		v.visit(this);
	}
	
	public OnTheFlyWhileNode accept(CFGClonerVisitor v) throws CFGVisitException {
		return v.visit(this);
	}
	
//	public void accept(MarkedDFSVisitor v) throws CFGVisitException {
//		if (!v.isMarked(nodeIdent)) {
//			v.mark(nodeIdent);
//			v.visit(this);
//		}
//	}
	
}
