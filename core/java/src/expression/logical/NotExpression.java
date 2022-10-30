package expression.logical;


import java.util.Map;

import validation.util.OpCode;
import expression.Expression;
import expression.ExpressionVisitor;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

/* classe pour l'expression logique not*/
public class NotExpression extends LogicalOperator {

	public NotExpression(LogicalExpression a1) {
		super(a1, null);
		linear = a1.isLinear();
	}
	
    @Override
	public Boolean computeConstantBoolean() {
		if (arg1.isConstant())
			return !arg1.constantBoolean();
		else
			return null;
    }

	@Override
	public OpCode opCode() {
		return OpCode.NOT;
	}

	@Override
    public String toString() {
		return  "!( " + arg1.toString()+ " )";
	}

	@Override
    public boolean equals(Object o){
		return (o instanceof NotExpression) &&  arg1.equals(((NotExpression)o).arg1) ;
	}

	@Override
    public int hashCode() {
		return arg1.hashCode();
	}

	/* lydie */
	@Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
    public NotExpression substitute(Variable var, Expression val) {
		return new NotExpression((LogicalExpression)arg1.substitute(var, val));
	}

	@Override
    public NotExpression computeLastRenaming(SymbolTable var) {
		return new NotExpression(arg1.computeLastRenaming(var));
	}

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		LogicalExpression a1 = (LogicalExpression)arg1.setPrefixInFunction(functionName, prefix);
		return new NotExpression(a1);
	}

	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		return new NotExpression((LogicalExpression)arg1.substituteAll(substitutions));
	}
}
