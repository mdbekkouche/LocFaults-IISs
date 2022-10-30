package expression.logical;


import java.util.Map;

import expression.Expression;
import expression.ExpressionVisitor;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

/** class to represent deterministic assignments
 * 
 * @author helen
 *
 */

public class Assignment extends AbstractLogicalExpression {
	/**
	 * The line number in the source code from where this assignment statement starts.  
	 */
	protected int startLine;
	
	protected Variable variable; // the variable 
	protected Expression rhs; // the expression assigned to the variable
	
	public Assignment(Variable va, Expression exp) {
		variable = va;
		rhs = exp;
		linear = rhs.isLinear();
	}

	public Assignment(Variable va, Expression exp, int startLine) {
		this(va, exp);
		this.startLine = startLine;
	}
	
	// to be used in NondetAssignment
	protected Assignment(Variable va) {
		variable = va;
		linear = true;
	}

	/** 
	 * @return the left hand side of the assignment (i.e. the variable)
	 */
	public Variable lhs(){
		return variable;
	}

	/** 
	 * @return the right hand side of the assignment (i.e. the expression)
	 */
	public Expression rhs(){
		return rhs;
	}
    
	/**
	 * @return The line number in the source code from where this assignment statement starts.  
	 */
	public int startLine() {
		return this.startLine;
	}
	
    @Override
    public  String toString() {
		String result = "";
		if (rhs!=null) result = variable.toString() + " := " + rhs.toString();
		return result ;
	}

	@Override
    public boolean equals(Object o){
		return (o instanceof Assignment) 
			&& variable.equals(((Assignment)o).variable) 
			&& rhs.equals(((Assignment)o).rhs);
	}

	@Override
    public int hashCode() {
		return (variable.hashCode() + rhs.hashCode())%Integer.MAX_VALUE;
	}

	/* lydie */
	@Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}
	
	// make a substitution of the expression only
	@Override
    public Assignment substitute(Variable var, Expression val) {
		return new Assignment(variable, rhs.substitute(var, val));
	}

	@Override
    public Assignment computeLastRenaming(SymbolTable var) {
		return new Assignment(variable, rhs.computeLastRenaming(var));
	}
	
	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		Variable a1 =  (Variable)variable.setPrefixInFunction(functionName, prefix);
		Expression a2 = rhs.setPrefixInFunction(functionName, prefix);
		return new Assignment(a1, a2);
	}
	
	// make a substitution of the expression only
	@Override
    public Assignment substituteAll(Map<Variable, Expression> substitutions) {
		return new Assignment(variable, rhs.substituteAll(substitutions));
	}
	

}
