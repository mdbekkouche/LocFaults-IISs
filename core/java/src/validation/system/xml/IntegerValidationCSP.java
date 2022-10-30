package validation.system.xml;

import java.util.HashMap;
import java.util.Map;

import solver.Solver.SolverEnum;

import expression.logical.LogicalExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;


/** 
 * A CSP for validation 
 * 
 * @author helen
 */
public abstract class IntegerValidationCSP extends ValidationCSP {		
	/** 
	 * A validation CSP for integers.
	 * @param n name of the CSP
	 * @param solver the solver type associated with this CSP
	 * @param vs the validation system owning this CSP
	 */
	public IntegerValidationCSP(String n, SolverEnum solver, ValidationSystem vs) {
		super(n, solver, vs);
	}
				
	/** 
	 * Adds a constraint that is in the JML <code>requires</code> statement.
	 * Allows to set array elements as constants.
	 * 
	 * If not overriden, this method simply calls {@link #addConstraint(Constraint)}.
	 * 
	 * @param c The precondition to be added.
	 * @param arrayElts The list of array elements appearing in the precondition.
	 * @param csp The CSP containing the variables appearing in the precondition.
	 */
	public void addConstraint(LogicalExpression c, HashMap<String, ArrayElement> arrayElts) {
		addConstraint(c);
	}

	/** 
	 * Adds a constraint that can be removed.
	 * Depending on the solver (e.g. with CPLEX), many concrete constraints may have 
	 * been added for the single abstract constraint <pre>c</pre>. 
	 * 
	 * Obsolete : preferred mecanism is with save/restore 
	 */
	public void addRemovable(LogicalExpression c) {
		constr.add(c, visitor);
	}
				
	/**
	 * Maps an array name with the values found by the solver.
	 * Each array of concrete variables in this block gives an entry in the map 
	 * that associates its name with an array of Numbers whose value is the one 
	 * of the concrete variable or <code>null</code> if no value was set by the 
	 * solver.
	 * 
	 * @return A map of array names and arrays of values.
	 */
	public abstract Map<ArrayVariable, Number[]> getArraysValues();

	public abstract void add(Map<ArrayVariable, Number[]> arrays);

}