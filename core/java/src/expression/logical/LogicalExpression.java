package expression.logical;

import expression.Expression;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;

/**
 * The logical (boolean) expressions.
 * They are made of logical and numeric sub-expressions. 
 * For this reason, it is not as interesting to overload 
 * {@link expression.Expression#substitute(expression.variables.Variable, Expression)} 
 * as it is done in {@link expression.numeric.NumericExpression}.
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
public interface LogicalExpression extends Expression { 
	/**
	 * @return whether this logical expression is a comparison expression or not.
	 * 
	 */
	public boolean isComparison();
	
	/**
	 * Returns the value of this expression as a boolean.
	 * The returned value is only meaningful if the expression is constant 
	 * (which can be checked by calling {@link #isConstant()}. 
	 * 
	 * @return The boolean value of this expression.
	 */
	public boolean constantBoolean();

	/* (non-Javadoc)
     * @see expression.Expression#computeLastRenaming(expression.variables.SymbolTable, expression.variables.ArraySymbolTable)
     * 
 	 * Overriden to take advantage of covariance inheritance.
     */
	@Override
	public LogicalExpression computeLastRenaming(SymbolTable vars);
	
}
