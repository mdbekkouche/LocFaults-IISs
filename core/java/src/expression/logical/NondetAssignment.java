package expression.logical;


import java.util.Map;

import expression.Expression;
import expression.ExpressionVisitor;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

/** to represent non deterministic assignments of the form 
 *      v = nondet_in()
 * 
 * @author helen
 *
 */

public class NondetAssignment extends Assignment {

	/** constructor used for non deterministic assignments
	 * 
	 * @param va
	 */
	public NondetAssignment(Variable va) {
		super(va);
	}

	public NondetAssignment(Variable va, int startLine) {
		this(va);
		this.startLine = startLine;
	}

	@Override
    public  String toString() {
		return  variable.toString() + " == nondet_in()";
	}

	@Override
    public boolean equals(Object o){
		return (o instanceof NondetAssignment) 
			&& variable.equals(((NondetAssignment)o).variable) ;
	}

	@Override
    public int hashCode() {
		return variable.hashCode();
	}

	/* lydie */
	@Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	// make a substitution of the expression only
	@Override
    public Assignment substitute(Variable var, Expression val) {
		return this;
	}

	@Override
    public Assignment computeLastRenaming(SymbolTable var) {
		return this;
	}
	
	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		Variable a1 =  (Variable)variable.setPrefixInFunction(functionName, prefix);
		return new NondetAssignment(a1);
	}
	
	// make a substitution of the expression only
	@Override
    public Assignment substituteAll(Map<Variable, Expression> substitutions) {
		return this;
	}
	
}
