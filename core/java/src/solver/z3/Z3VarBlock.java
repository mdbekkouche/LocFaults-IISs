package solver.z3;

import validation.Validation.VerboseLevel;
import validation.system.AbstractVariableStore;
import expression.variables.ConcreteVariable;
import expression.variables.Variable;

/** 
 * Stores variables of a Java block.
 * 
 * The variables are stored as z3_ast C++ pointers converted to 64 bit long integers 
 * (for 64 bit architectures support). 
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
public class Z3VarBlock extends AbstractVariableStore<ConcreteVariable<Long>> {

	private Z3Solver z3Solver;
	
	public Z3VarBlock(Z3Solver solver) {
		z3Solver = solver;
	}
	
	Long concreteVar(String varName) {
		//System.out.println("VAR=" + varName);
		return get(varName).concreteVar();
	}
	
	/**
	 * Adds a variable to this store of variable.
	 * 
	 * @param v The variable to add.
	 */
	@Override
	public ConcreteVariable<Long> add(Variable v) {
		ConcreteVariable<Long> cv = new ConcreteVariable<Long>(v, z3Solver.mkVar(v.name(), v.type()));
		add(v.name(), cv);
		return cv;
	}
	
	/** 
	 * Prints all the variables' values.
	 */
	@Override
	public void print() {
		if (VerboseLevel.TERSE.mayPrint()) {
			for (ConcreteVariable<Long> v: this)
				System.out.println(z3Solver.z3AstToString(v.concreteVar()));
		}
	}
	
	public String toString() {
		String s="";
		for (ConcreteVariable<Long> v: this)
			s+=z3Solver.z3AstToString(v.concreteVar())+" ";
		return s;
	}
	

	//--------------------------------------------------------
	//VarBlock interface methods to handle backtracking
	//--------------------------------------------------------
	

	/**
	 * Restores the previous variable store.
	 * All variables added since the last call to {@link #save} are removed from this
	 * store.
	 */
	@Override
	public void restore() {
		int idxLastVarToRemove = ((Integer)save.pop()).intValue();
		
		for (int i = size()-1; i >= idxLastVarToRemove; i--) {
			remove(get(i).name(), i);
		}
	}
	
}
