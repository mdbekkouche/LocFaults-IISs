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
public class JMLForAllExpression extends Iterator {


	public JMLForAllExpression(Variable index, LogicalExpression bound, LogicalExpression condition) {
		super(index, bound, condition);
	}

	//Equal modulo index renaming
	@Override
    public boolean equals(Object o){
		if (o instanceof JMLForAllExpression) {
			JMLForAllExpression forAll = (JMLForAllExpression)o;
			return 
				this.bound.substitute(this.index, forAll.index).equals(forAll.bound)
				&&
				this.condition.substitute(this.index, forAll.index).equals(forAll.condition);				
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
		return "( ForAll " + super.toString() + " )";
	}
	
	/* lydie */
	@Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
    public JMLForAllExpression substitute(Variable var, Expression val) {
		return new JMLForAllExpression(index,
									   (LogicalExpression)bound.substitute(var, val),
									   (LogicalExpression)condition.substitute(var, val));
	}
	
	@Override
    public JMLForAllExpression computeLastRenaming(SymbolTable var)
    {
		return new JMLForAllExpression(index,
				                       bound.computeLastRenaming(var),
				                       condition.computeLastRenaming(var));
    }

	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		return new JMLForAllExpression(index,
                (LogicalExpression)bound.substituteAll(substitutions),
                (LogicalExpression)condition.substituteAll(substitutions));

	}
}
