package validation.system;

/** 
 * Common interface to store of variables.
 * 
 * A store of variables stores the variables as handled by the concrete solver through the 
 * <pre>ConcreteType</pre> (e.g. IloIntVar).
 * 
 * Store of variables maintain the link between program variables and solver variables 
 * via variables' name. This interface allows to retrieve a solver variable thanks to its name.
 *  
 *  This interface is extended by VariableStore and ArrayVariableStore interfaces for storing
 *  scalar and array variables respectively.
 *  
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public interface VarStore<ConcreteType> extends Store<ConcreteType> {
	
	/**
	 * @param n Name of the variable.
	 * @return The concrete variable as handled by the solver which name is
	 *         given in parameter.
	 */
	public ConcreteType get(String name);

}
