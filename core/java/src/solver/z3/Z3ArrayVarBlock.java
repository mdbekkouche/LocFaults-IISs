package solver.z3;

import validation.Validation.VerboseLevel;
import validation.system.AbstractArrayVariableStore;
import expression.variables.ArrayVariable;
import expression.variables.ConcreteArrayVariable;

/** 
 * Stores arrays of a Java program as z3 arrays.
 * 
 * Each time the value of an element of the array named <pre>A_i</pre> is modified, 
 * this creates a new array <pre>A_(i+1)</pre> in this store whose elements are identical 
 * to those in <pre>A_i</pre> except for the modified element. 
 * 
 * While new arrays are added to this store, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm. 
 * 
 * We keep track of the names and types of the arrays.
 * 
 * @author Olivier Ponsini
 */
public class Z3ArrayVarBlock extends AbstractArrayVariableStore<ConcreteArrayVariable<Long>> {
	
	private Z3Solver z3Solver;
	
	/**
	 * The constructor of a store of concrete arrays.
	 * 
	 * @param solver The concrete solver associated with this store of integer 
	 *               array variables. 
	 * @param format The format of integers: the number of bits encoding the integers.
	 */
	public Z3ArrayVarBlock(Z3Solver solver) {
		z3Solver = solver;
	}

	//---------------------------------
	//ArrayVarBlock interface methods
	//---------------------------------
	
	/**
	 * Adds a concrete array to this store.
	 * 
	 * @param name The name of the array, including its SSA renaming number.
	 * @param t Type of the array elements.
	 * @param length The number of elements of this array.
	 */
	@Override
	public void add(ArrayVariable array) {
		add(array.name(), 
			new ConcreteArrayVariable<Long>(array, 
			    							z3Solver.mkArray(array.name(), 
											 	   			 array.type(),
														   	 array.length())));
	}
		
	/** 
	 * Builds the string to print all the variables' values.
	 */
	@Override
	public void print() {
		if (VerboseLevel.TERSE.mayPrint()) {
			for (ConcreteArrayVariable<Long> v: elts) {
				System.out.print(z3Solver.z3AstToString(v.concreteArray()) + "\n");
			}
		}
	}

	/**
	 * Restores the previous array store.
	 * All variables added since the last call to {@link #save} are removed from this
	 * store.
	 */
	@Override
	public void restore() {
		int idxLastVarToRemove = ((Integer)save.pop()).intValue();
		
		for (int i = elts.size()-1; i >= idxLastVarToRemove; i--) {
			remove(get(i).name(), i);
		}
	}

}
