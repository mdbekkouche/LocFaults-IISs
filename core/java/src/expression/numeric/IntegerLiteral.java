package expression.numeric;


import java.util.Map;

import expression.Expression;
import expression.ExpressionVisitor;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;
import validation.Validation.VerboseLevel;
import validation.util.Type;

public class IntegerLiteral extends AbstractNumericExpression {
	/**
	 * The string representation of this literal.
	 * This stores the string representation as written in the source program.
	 */
	private String literal;	

	private IntegerLiteral() {
		linear = true;
		type = Type.INT;
	}
    
    public IntegerLiteral(int n) {
    	this();
    	literal = null;
    	constantNumber = new Integer(n);
    }

    public IntegerLiteral(String n) {
    	this();
    	constantNumber = new Integer(n);
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
    public boolean equals(Object o){
    	return (o instanceof IntegerLiteral) 
    		&& (constantNumber.intValue() == ((IntegerLiteral)o).constantNumber.intValue());
    }
    
	@Override
	public int hashCode() {
		return constantNumber.intValue();
	}
		
    /* lydie */
	@Override
	public Object structAccept(ExpressionVisitor visitor) {
		
		return visitor.visit(this);
	}

	@Override
	public IntegerLiteral substitute(Variable var, NumericExpression val) {
		return this;
	}
	
	@Override
	public IntegerLiteral computeLastRenaming(SymbolTable var) {
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
