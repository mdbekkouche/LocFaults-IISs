package validation.system;

import ilog.concert.IloException;
import expression.logical.LogicalExpression;
import expression.variables.Variable;

/**
 * Defines the callback methods that are called while parsing the program to be verified.
 * These methods must be implemented by any validationSystem.
 * 
 * @author Olivier Ponsini
 *
 */
public interface ValidationSystemCallbacks {
	/**
	 * This methods is called to start the verification process.
	 * @throws IloException 
	 */
	public void validate() throws IloException;
	/**
	 * This method is called to check if the given constraint <pre>e</pre> is
	 * satisfiable in the current set of constraints.
	 * 
	 * @param e A constraint (i.e. a logical expression).
	 * @return  <code>true</code> if the given constraint <pre>e</pre> is satisfiable in
	 *          the current set of constraints; <code>false</code> otherwise.
	 */
	public boolean tryDecision(LogicalExpression le);
	/**
	 * Checks if the current csp is consistent.
	 * A fast and partial consistency check can be used (e.g. a filtering).
	 *       
	 * @return true if the current csp has a solution; false otherwise. 
	 * 
	 */
	public boolean isFeasible();
	/**
	 * Searches for a solution of the current CSP.
	 * @return true if the current csp has a solution; false otherwise. 
	 */
	public boolean solve();
	/**
	 * This method is called to solve the current validation system on the 
	 * postcondition.
	 * 
	 * @param e The postcondition to verify.
	 * @return <code>true</code> if the current validation system has a solution; 
	 *         <code>false</code> otherwise.
	 */
	public boolean checkPostcond(LogicalExpression le);

	/**
	 * This method is called to solve the current validation system on an assertion.
	 * 
	 * @param e The postcondition to verify.
	 * @return <code>true</code> if the current validation system has a solution; 
	 *         <code>false</code> otherwise.
	 */
	public boolean checkAssertion(LogicalExpression le, String message);
	/**
	 * This methods is called when a new constraint is parsed.
	 * @param le The constraint to add to the current validation system.
	 */
	public void addConstraint(LogicalExpression le);
	/**
	 * This methods is called when a new variable is added.
	 * This is used when dynamically calling functions.
	 * @param n The variable to add to the current validation system.
	 */
	public void addVar(Variable v);
	/**
	 * This method is called to save the current validation system state to allow backtracking.
	 */
	public void save();
	/**
	 * This method is called to restore the previous validation system state to allow backtracking.
	 */
	public void restore();
	

}
