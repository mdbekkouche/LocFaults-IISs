package expression.numeric;


import java.util.Map;

import expression.Expression;
import expression.ExpressionVisitor;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;
import validation.Validation.VerboseLevel;
import validation.util.Type;

/**
 * Represents <code>float</code> type literals.
 * 
 * @author Olivier Ponsini
 *
 */
public class DoubleLiteral extends AbstractNumericExpression {   
	/**
	 * The string representation of this literal.
	 * This stores the string representation as written in the source program if
	 * it is different from the string representation of its actual value stored in 
	 * constantNumber.
	 */
	private String literal;	

	private DoubleLiteral() {
		linear = true;
		type = Type.DOUBLE;
	}

	/**
	 * Constructs a double literal from a Number.
	 * The {@link Double#doubleValue()} of the given number <pre>n</pre> is used.
	 * 
	 * @param n The value of the literal to construct.
	 */
	public DoubleLiteral(double n) {
	    this();
	    literal = null;
	    constantNumber = new Double(n);
	}

	/**
	 * Constructs a double literal from a string representation of a double value.
	 * 
	 * @param n A valid string representation of a double value.
	 */
	public DoubleLiteral(String n) {
		this();
	    constantNumber = new Double(n);
    	literal = n;
	}
	
    /**
     * @return the string representation of this numeric literal as written in the source code.
     */
    public String literal() {
    	if (literal != null)
    		return literal;
    	else
    		return constantNumber.toString();
    }

    /**
     * Returns both the String representation of this constant's value and the exact 
     * string (between parentheses) that was used to create it, if they are different.   
     */
	@Override
	public String toString() {
		if (literal != null && VerboseLevel.DEBUG.mayPrint()) 
			return constantNumber + "(" + literal + ")";
		else
			return constantNumber.toString();
    }

    @Override
    public boolean equals(Object o) {
    	return (o instanceof DoubleLiteral) 
    		&& (constantNumber.doubleValue() == ((DoubleLiteral)o).constantNumber.doubleValue());
    }

	@Override
	public int hashCode() {
		return constantNumber.intValue();
	}

	@Override
	public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public DoubleLiteral substitute(Variable var, NumericExpression val) {
		return this;
	}

	@Override
	public DoubleLiteral computeLastRenaming(SymbolTable var) {
		return this;
	}

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		return this;
	}
	
	@Override
	public NumericExpression substituteAll(Map<Variable, Expression> substitutions) {
		return this;
	}
}
