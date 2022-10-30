package solver.fplib;

import expression.variables.ConcreteVariable;
import expression.variables.Variable;
import validation.system.AbstractVariableStore;

/** 
 * Stores variables of a Java block.
 * 
 * Variables are added when found in java parameters and declaration statements.
 * While new variables are added to this store, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm. 
 * 
 * The variables are stored as C++ pointers to fplib <code>Constraint</code> converted 
 * to 64 bit long integers (for 64 bit architectures support).
 * 
 * We keep track of the variables by their name and type.
 * 
 * @author Olivier Ponsini
 */
public class FplibVarBlock extends AbstractVariableStore<ConcreteVariable<Long>> {
			
	/**
	 * Adds a concrete variable to this store of concrete variables.
	 * 
	 * @param v The variable to add.	 
	 */
	public void add(ConcreteVariable<Long> v) {
		add(v.name(), v);
	}

	/**
	 * Adds a variable to this store of concrete variables.
	 * 
	 * @param v The variable to add.	 
	 */
	@Override
	public ConcreteVariable<Long> add(Variable v) {
		ConcreteVariable<Long> cv = new ConcreteVariable<Long>(v);
		add(v.name(), cv);
		return cv;
	}
	
	/** 
	 * Prints all the variables' values.
	 * It requires the variables exist in the fplib current model.
	 */
	@Override
	public void print() {
		for (ConcreteVariable<Long> fplibVar: elts) {
			if (fplibVar.concreteVar() != null) {
				FplibSolver.fplibVarDisplay(fplibVar.concreteVar());
				System.out.println();
			}
		}
	}

	//--------------------------------------------------------
	//VarBlock interface methods to handle backtracking
	//--------------------------------------------------------
	

	/**
	 * Restores the previous variable store.
	 * All variables added since the last call to {@link #save} are removed from this
	 * store.
	 */
	public void restore() {
		int idxLastVarToRemove = ((Integer)save.pop()).intValue();
		
		for (int i = elts.size()-1; i >= idxLastVarToRemove; i--) {
			remove(get(i).name(), i);
		}
	}
	
}
