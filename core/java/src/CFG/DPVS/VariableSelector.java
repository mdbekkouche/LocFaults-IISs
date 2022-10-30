package CFG.DPVS;

import expression.variables.Variable;

/**
 * Handles the list of variable waiting to be treated by DPVS strategy.
 * Cloning may perform a shallow copy of the data structure.
 * 
 * @author ponsini
 *
 */
public interface VariableSelector extends Cloneable {
	
	/**
	 * Removes the next variable to be treated by DPVS from the waiting list and returns it. 
	 * @return The next variable that DPVS should treat.
	 */
	public Variable pop(); 
	
	/**
	 * Adds a variable to the list of variables waiting to be treated by DPVS. 
	 * @param v A variable to add to the DPVS waiting list.
	 */
	public void push(Variable v);
	
	/**
	 * @return <code>true</code> if there is no more variable to be treated by DPVS.
	 */
	public boolean isEmpty();

	public VariableSelector clone();

}
