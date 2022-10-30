package java2CFGTranslator;

import java.util.Stack;

import expression.Expression;



/** class for storing expressions which are explored during the visit of the JDT AST */

public class ExpressionStack {

	// expression stacks
	Stack<Expression> stack;
	
	public ExpressionStack() {
		stack = new Stack<Expression>();
	}
	
	public void push(Expression e){
		stack.push(e);
		//System.out.println("Pile expressions apres push" + stack);
	}
	
	public Expression pop(){
		return stack.pop();
	}
	

}
