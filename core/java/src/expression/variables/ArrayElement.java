package expression.variables;


import java.util.Map;

import expression.Expression;
import expression.ExpressionVisitor;
import expression.logical.LogicalExpression;
import expression.numeric.AbstractNumericExpression;
import expression.numeric.NumericExpression;

/**
 * Represents an access to an array variable.
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public class ArrayElement extends AbstractNumericExpression	implements LogicalExpression {
    ArrayVariable tab; // the array
    //This should be an integer expression...
    NumericExpression index; //the index of the access
    Boolean constantBoolean = null;

    public ArrayElement(ArrayVariable t, NumericExpression i) {
    	tab = t;
    	index = i;
    	linear = index.isLinear();
    	type = t.type();
    }

    public ArrayVariable array(){
    	return tab;
    }
    
    public NumericExpression index() {
    	return index;
    }
     
	public void setConstantNumber(Number n) {
		constantNumber = n;
	}
   
	@Override
	public boolean isConstant() {
		return (constantNumber != null) || (constantBoolean != null);
	}

	@Override
	public boolean isComparison() {
		return false;
	}

	@Override
	public boolean constantBoolean() {
		return constantBoolean;
	}

	@Override
    public String toString() {
    	String s= tab.toString() + "[" + index.toString() + "]";
    	if (isConstant()) 
    		s += " constant value: " + constantNumber();
    	return s;
    }

	@Override
	public boolean equals(Object o){
    	return (o instanceof ArrayElement) 
    		&& tab.equals(((ArrayElement)o).tab)
    		&& index.equals(((ArrayElement)o).index);
    }
    
	@Override
	public int hashCode() {
		return (tab.hashCode() + index.hashCode())%Integer.MAX_VALUE;
	}
   
    /* lydie */
	@Override
	public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

   /* (non-Javadoc)
    * @see expression.numeric.NumericExpression#substitute(expression.variables.Variable, expression.numeric.NumericExpression)
    * 
    * No need to override AbstractNumericExpression default implementation of 
    * expression.substitute(Variable, Expression) as it would also be an error here to try to substitute
    * a logical expression as the index must be a numeric expression.
    */
	@Override
    public ArrayElement substitute(Variable var, NumericExpression val) {
		//index is a NumericExpression and so is its substitution
		return new ArrayElement(tab, index.substitute(var,val));
	}

	@Override
	public ArrayElement computeLastRenaming(SymbolTable var) {
		//index is a NumericExpression and so is its last renaming
		return new ArrayElement(tab.computeLastRenaming(var),
				                index.computeLastRenaming(var));
	}

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		return new ArrayElement(tab, (NumericExpression)index.setPrefixInFunction(functionName, prefix));
	}

	@Override
	public NumericExpression substituteAll(Map<Variable, Expression> substitutions) {
		return new ArrayElement(tab, (NumericExpression)index.substituteAll(substitutions));
	}

}
