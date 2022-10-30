package validation.system;

import expression.variables.ArrayVariable;

/** 
 * Interface to stores of array variables (ArrayVariable).
 * 
 * A store of array variables stores the arrays as handled by the concrete solver through the 
 * <pre>ConcreteType</pre> (e.g. IloIntVar[]). However, the arrays are added to the store 
 * using the CPBPV type <pre>ArrayVariable</pre>. 
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public interface ArrayVariableStore<ConcreteType> extends VarStore<ConcreteType> {
	
	/** add variable */
	public void add(ArrayVariable array);

}
