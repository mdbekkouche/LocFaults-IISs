package CFG;

import expression.Expression;
import expression.logical.LogicalExpression;


/**
 *  to represent an if node
 * @author helen
 *
 */

public abstract class ConditionalNode extends CFGNode {
	
	/**
	 * A node in the graph is marked, in the case where his deviation allows 
	 * to correct the program
	 */
	public int marked=-1; 
	
	// condition
	protected Expression condition;

	public ConditionalNode(int ident, String methodName, LogicalExpression cond) {
		super(ident,methodName);
		condition = cond;
	}
	
	public ConditionalNode(ConditionalNode n) {
		super(n.nodeIdent,n.key,n.left,n.right,n.leftFather,n.rightFather);
		condition = n.condition;
	}

	public ConditionalNode(int ident, String methodName) {
		super(ident, methodName);
	}
	
	public static ConditionalNode clone(ConditionalNode n){
		if (n instanceof IfNode)
			return new IfNode((IfNode)n);
		if (n instanceof WhileNode)
			return new WhileNode((WhileNode)n);
		if (n instanceof OnTheFlyWhileNode)
			return new OnTheFlyWhileNode((OnTheFlyWhileNode)n);
		if (n instanceof EnsuresNode)
			return new EnsuresNode((EnsuresNode)n);
		if (n instanceof RequiresNode)
			return new RequiresNode((RequiresNode)n);
		if (n instanceof AssertNode)
			return new AssertNode((AssertNode)n);
		return new AssertEndWhile((AssertEndWhile)n);
	}
	
	public void setCondition(Expression cond){
		condition = cond;
	}
	
	public LogicalExpression getCondition(){
		return (LogicalExpression)condition;
	}
	
	public boolean isEmpty() {
		return condition==null;
	}
	
	public String toString() {
		return  "Condition: " + condition + "\n" + 
		  super.toString();
	}
}
