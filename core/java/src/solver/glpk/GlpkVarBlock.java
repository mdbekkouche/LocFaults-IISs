package solver.glpk;

import expression.variables.ConcreteVariable;
import expression.variables.Variable;

import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.system.AbstractVariableStore;

/** 
 * Stores variables of a Java block.
 * 
 * Variables are added when found in java parameters and declaration statements.
 * While new variables are added to this store, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm. 
 * 
 * The variables are stored as GLPK column indices in the matrix of the LP problem.
 * We also keep track of the variables by their name and type.
 * 
 * @author Olivier Ponsini
 */
public class GlpkVarBlock extends AbstractVariableStore<ConcreteVariable<Integer>> {

	GlpkSolver glpkSolver;
		
	public GlpkVarBlock(GlpkSolver solver) {
		glpkSolver = solver;
	}
	
	public int concreteVar(Variable var) {
		return get(var.name()).concreteVar();
	}
		
	/**
	 * Adds a variable to this store of variables.
	 * 
	 * @param v The variable to add.	 
	 */
	@Override
	public ConcreteVariable<Integer> add(Variable v) {
		int col = glpkSolver.addVar(v.name(), v.type());
		ConcreteVariable<Integer> cv = new ConcreteVariable<Integer>(v, col);
		add(v.name(), cv);
		if (Validation.verboseLevel == VerboseLevel.DEBUG) {		
			System.out.println("GLPK: added variable " + v + " in column " + col);
		}
		return cv;
	}
	
	/** 
	 * Prints all the variables' values.
	 * It requires the variables exist in the GPLK current model.
	 */
	@Override
	public void print() {
		if (VerboseLevel.TERSE.mayPrint()) {
			for (ConcreteVariable<Integer> v: this)
				System.out.println(v.name() + ": " + glpkSolver.value(v.concreteVar()));
		}
	}

	//--------------------------------------------------------
	//VarBlock interface methods to handle backtracking
	//--------------------------------------------------------
	

	/**
	 * Restores the previous variable store.
	 * All variables added since the last call to {@link #save} are removed from this
	 * store and from the GLPK matrix.
	 */
	@Override
	public void restore() {
		int idxLastVarToRemove = ((Integer)save.pop()).intValue();
		int[] cols = new int[size() - idxLastVarToRemove];
		ConcreteVariable<Integer> var;
		
		for (int i = size()-1; i >= idxLastVarToRemove; i--) {
			var = get(i);
			cols[i - idxLastVarToRemove] = var.concreteVar();
			remove(var.name(), i);
		}
		
		if (cols.length > 0) {
			glpkSolver.delCols(cols);
		}
	}
	
}
