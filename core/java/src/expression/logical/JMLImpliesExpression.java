package expression.logical;


import java.util.Map;

import expression.Expression;
import expression.ExpressionVisitor;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

import validation.util.OpCode;

/** class to represent a JMLImplies expression 
 * */
public class JMLImpliesExpression extends LogicalOperator {

	public JMLImpliesExpression(LogicalExpression a1, LogicalExpression a2) {
		super(a1, a2);
		linear=a1.isLinear() && a2.isLinear();
	}
	
	@Override
	public Boolean computeConstantBoolean() {
		if (arg1.isConstant() && arg2.isConstant())
			return !arg1.constantBoolean() || arg2.constantBoolean();
		else if ((arg1.isConstant() && !arg1.constantBoolean())
				|| (arg2.isConstant() && arg2.constantBoolean()))
			return Boolean.TRUE;
		else
			return null;
	}

	@Override
	public OpCode opCode() {
		return OpCode.IMPLIES;
	}

	@Override
    public String toString() {
		return  "( " + arg1.toString() + " ==> " + arg2.toString() + " )";
	}

	@Override
    public boolean equals(Object o){
		return (o instanceof JMLImpliesExpression) 
			&& arg1.equals(((JMLImpliesExpression)o).arg1) 
			&& arg2.equals(((JMLImpliesExpression)o).arg2);
	}

	/* lydie */
	@Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
    public JMLImpliesExpression substitute(Variable var, Expression val) {
		LogicalExpression a1 = (LogicalExpression)arg1.substitute(var, val);
		LogicalExpression a2 = (LogicalExpression)arg2.substitute(var, val);
		return new JMLImpliesExpression(a1, a2);
	}

	@Override
    public JMLImpliesExpression computeLastRenaming(SymbolTable var) {
		LogicalExpression a1 = arg1.computeLastRenaming(var);
		LogicalExpression a2 = arg2.computeLastRenaming(var);
		return new JMLImpliesExpression(a1, a2);
	}

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		LogicalExpression a1 = (LogicalExpression)arg1.setPrefixInFunction(functionName, prefix);
		LogicalExpression a2 = (LogicalExpression)arg2.setPrefixInFunction(functionName, prefix);
		return new JMLImpliesExpression(a1, a2);
	}

	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		LogicalExpression a1 = (LogicalExpression)arg1.substituteAll(substitutions);
		LogicalExpression a2 = (LogicalExpression)arg2.substituteAll(substitutions);
		return new JMLImpliesExpression(a1, a2);
	}
}
