package CFG.DPVS;

import java.util.Stack;

import expression.variables.Variable;

public class StackSelector implements VariableSelector {
	
	private Stack<Variable> stack = new Stack<Variable>();

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public Variable pop() {
		return stack.pop();
	}

	@Override
	public void push(Variable v) {
		stack.push(v);
	}

	@Override
	public StackSelector clone() {
		StackSelector clone = new StackSelector();
		clone.stack = (Stack<Variable>)this.stack.clone();
		return clone;
	}
	
}
