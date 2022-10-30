package expression.numeric;

import expression.Expression;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

/**
 * The numeric expressions.
 * They are made of numeric sub-expressions only.
 *
 * Due to compatibility with XML based validation systems, we compute the value of 
 * an expression if it is constant. Note that this value is computed once for all, it is not 
 * recomputed if sub-expressions change.
 * This is a deprecated feature, the recommended way of doing this is to decouple partial 
 * evaluation from constraint generation. Partial evaluation can be done on the CFG by calling a
 * dedicated method.
 *
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public interface NumericExpression extends Expression {
	/**
	 * Returns the value of this expression as a Number.
	 * The returned value is only meaningful if the expression is constant 
	 * (which can be checked by calling {@link #isConstant()}. 
	 * 
	 * @return The value of this expression as a Number. May return 
	 *         <code>null</code> if the expression is not constant.
	 */
	public Number constantNumber();	
		
	/**
	 * Substitutes <pre>val</pre> to <pre>var</pre> in a copy of this 
	 * numeric expression and returns the modified copy. 
	 * 
	 * This is interesting to overload {@link expression.Expression#substitute(Variable, Expression)} 
	 * because we know that variables are numeric in a numeric expression. 
	 * Note that we cannot assume this in a logical expression. 
	 * 
	 * @param var The variable to be replaced.
	 * @param val The replacement numeric expression.
	 * @return A new expression where <pre>var</pre>
	 *         has been replaced by <pre>val</pre>.
	 * @see {@link expression.numeric.AbstractNumericExpression}
	 */
	public NumericExpression substitute(Variable var, NumericExpression val);

	
	/* (non-Javadoc)
     * @see expression.Expression#computeLastRenaming(expression.variables.SymbolTable, expression.variables.ArraySymbolTable)
     * 
 	 * Overriden to take advantage of covariance inheritance.
     */
	@Override
	public NumericExpression computeLastRenaming(SymbolTable vars);

}

