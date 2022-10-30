package expression.logical;


import java.util.Map;

import expression.Expression;
import expression.ExpressionVisitor;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

/** class to represent a JML forall expression 
 * current restriction :
 * assume we have bounded expressions
 * */
public class JMLExistExpression extends Iterator {

	public JMLExistExpression(Variable index, LogicalExpression bound, LogicalExpression condition) {
		super(index, bound, condition);
	}

	//Equal modulo index renaming
	@Override
    public boolean equals(Object o){
		if (o instanceof JMLExistExpression) {
			JMLExistExpression exist = (JMLExistExpression)o;
			return 
				this.bound.substitute(this.index, exist.index).equals(exist.bound)
				&&
				this.condition.substitute(this.index, exist.index).equals(exist.condition);				
		}
		else {
			return false;
		}
	}

	@Override
    public int hashCode() {
		return this.getClass().hashCode();
	}

	@Override
    public String toString() {
		return "( Exist " + super.toString() + " )";
	}

	/* lydie */
	@Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
    public JMLExistExpression substitute(Variable var, Expression val) {
		return new JMLExistExpression(index,
				                      (LogicalExpression)bound.substitute(var, val),
				                      (LogicalExpression)condition.substitute(var, val));
	}
	
	@Override
    public JMLExistExpression computeLastRenaming(SymbolTable varr)
    {
		return new JMLExistExpression(index,
				                      bound.computeLastRenaming(varr),
				                      condition.computeLastRenaming(varr));
    }

	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		return new JMLExistExpression(index,
                (LogicalExpression)bound.substituteAll(substitutions),
                (LogicalExpression)condition.substituteAll(substitutions));

	}

}
