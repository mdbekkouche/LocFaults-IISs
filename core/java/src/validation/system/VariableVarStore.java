package validation.system;

import expression.variables.Variable;

import validation.Validation.VerboseLevel;

/** 
 * Stores variables of a Java block as objects of type Variable.
 * 
 * Variables are added when found in java parameters and declaration statements.
 * While new variables are added to this store, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm.
 * 
 * We keep track of the variables by their name.
 * 
 * @author Olivier Ponsini
 */
public class VariableVarStore extends AbstractVariableStore<Variable> {
	
	/**
	 * Adds a variable to this store of variable.
	 * 
	 * @param v The variable to add.	 
	 */
	@Override
	public Variable add(Variable v) {
		add(v.name(), v);
		return v;
	}

	/** 
	 * Prints all the variables' values.
	 */
	@Override
	public void print() {
		if (VerboseLevel.TERSE.mayPrint()) {
			for (Variable v : this) {
				System.out.print(v.toString() + ": ");
				System.out.print(v.domain() + "\n");
			}
		}
	}

	/** 
	 * Builds a String with all the variables' values.
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Variable v : this) {
				sb.append(v.toString() + ": ");
				sb.append(v.domain() + "\n");
		}
		return sb.toString();
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
		
		for (int i = size()-1; i >= idxLastVarToRemove; i--) {
			remove(get(i).name(), i);
		}
	}
		
}
