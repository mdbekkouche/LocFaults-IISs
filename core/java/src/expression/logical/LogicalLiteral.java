package expression.logical;


import java.util.Map;

import expression.Expression;
import expression.ExpressionVisitor;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;


/**
 * Class of logical values (<code>true</code> and <code>false</code>).
 * 
 * @author Olivier Ponsini
 *
 */
public class LogicalLiteral extends AbstractLogicalExpression {
	
	public LogicalLiteral(boolean val) {
    	linear = true;
		constantBoolean = val;
	}
	
	@Override
    public boolean equals(Object o) {
		if(o instanceof LogicalLiteral) {
			return constantBoolean == ((LogicalLiteral)o).constantBoolean();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return constantBoolean.hashCode();
	}

	@Override
    public String toString() {
		return constantBoolean.toString();
	}

	@Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
    public LogicalLiteral substitute(Variable var, Expression val) {
		return this;
	}
	
	@Override
    public LogicalLiteral computeLastRenaming(SymbolTable var) {
		return this;
	}

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		return this;
	}

	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		return this;
	}

}
