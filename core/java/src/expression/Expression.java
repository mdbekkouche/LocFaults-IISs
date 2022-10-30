package expression;

import java.util.Map;


import expression.variables.SymbolTable;
import expression.variables.Variable;
import validation.util.Type;

/**
 * This is the supertype of all expressions.
 * {@link #isConstant()} method is used by validation systems based on XML
 * parsing to do some partial evaluation of expressions. In new validation systems,
 * e.g. the ones based on CFG, partial evaluation should occur in a distinct 
 * preliminary stage (on the CFG) and not when creating the expressions. 
 *  
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public interface Expression {
    /**
     * @return whether the expression is linear or not.
     */
    public boolean isLinear();
    
	/**
	 * @return whether the expression is constant (computations on 
	 *         constant values) or not.
	 *         Litterals and array lengthes are constants.
	 */
	public boolean isConstant();
	
	/**
	 * @return the type of this expression.
	 */
	public Type type(); 
		
	/**
	 * Accepts the given visitor to visit this expression.
	 * 
	 * @param visitor
	 * @return
	 */
	public Object structAccept(ExpressionVisitor visitor);

	/**
	 * Substitutes <pre>val</pre> to <pre>var</pre> in a deep copy of this 
	 * expression and returns the modified copy.   
	 * 
	 * @param var The variable to be replaced.
	 * @param val The replacement expression.
	 * @return A new expression (deep copy of this one) where <pre>var</pre>
	 *         has been replaced by <pre>val</pre>.
	 */
	public Expression substitute(Variable var, Expression val);

	/**
	 * Substitutes <pre>val[i]</pre> to <pre>var[i]</pre> in a deep copy of this 
	 * expression and returns the modified copy.   
	 * @param substitutions TODO
	 * 
	 * @return A new expression (deep copy of this one) where <pre>var</pre>
	 *         has been replaced by <pre>val</pre>.
	 */
	public Expression substituteAll(Map<Variable, Expression> substitutions);

    /**
     * Builds a deep copy of this expression where all variables are replaced by
     * their current SSA variable.
     * 
     * @param vars Variables symbol table.
     * @return a deep copy of this expression where all variables are replaced by
     *         their current SSA variable.
     *         
     * NOTA: this is only useful for XML version. With a CFG, the postcondition
     *       has been built with the correct renaming
     */
    public Expression computeLastRenaming(SymbolTable vars);

    
    /**
     * to rename variables that occurs in a given function
     * 
     * @param functionName : the name of the function
     * @param prefix : the new prefix of the variable for the current function call
     * @return : the new name of the variable for this function call
     */
    public Expression setPrefixInFunction(String functionName, String prefix);
}
