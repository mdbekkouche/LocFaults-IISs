package validation.system;

import expression.variables.Variable;

/** 
 * Interface to stores of scalar variables (Variable).
 * 
 * A store of scalar variables stores the variables as handled by the concrete solver through the 
 * <pre>ConcreteType</pre> (e.g. IloIntVar). However, the variables are added to the store 
 * using the CPBPV type <pre>Variable</pre>. 
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public interface VariableStore<ConcreteType> extends VarStore<ConcreteType> {
	
	/** add variable */
	public ConcreteType add(Variable var);

}
