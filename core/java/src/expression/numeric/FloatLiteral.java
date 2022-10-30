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
public class FloatLiteral extends AbstractNumericExpression {
    
	/**
	 * The string representation of this literal.
	 * This stores the string representation as written in the source program.
	 */
	private String literal;
	
	private FloatLiteral() {
		linear = true;
		type = Type.FLOAT;
	}
	
	/**
	 * Constructs a float literal from  a Number.
	 * The {@link Float#floatValue()} of the given number <pre>n</pre> is used.
	 * 
	 * @param n The value of the literal to construct.
	 */
	public FloatLiteral(float n) {
		this();
	    literal = null;
	    constantNumber = new Float(n);
	}

	/**
	 * Constructs a float literal from a string representation of a float value.
	 * 
	 * @param n A valid string representation of a float value.
	 */
	public FloatLiteral(String n) {
		this();
	    constantNumber = new Float(n);
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
    	return (o instanceof FloatLiteral) 
    		&& (constantNumber.floatValue() == ((FloatLiteral)o).constantNumber.floatValue());
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
	public FloatLiteral substitute(Variable var, NumericExpression val) {
		return this;
	}

	@Override
	public FloatLiteral computeLastRenaming(SymbolTable var) {
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
