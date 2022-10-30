package validation.system.xml;

import expression.variables.ArrayVariable;
import expression.variables.Variable.Use;
import validation.util.Type;

/**
 * Provides a simple implementation of arrays as a collection of scalar variables 
 * conforming to a specific naming scheme.
 * 
 * The naming scheme for variables associated to an array is: <pre>'arrayName'_i'indexValue'</pre>.
 * 
 * @author Olivier Ponsini
 *
 */
public abstract class SimpleArrayValidationSystem extends ValidationSystem {
	
	public SimpleArrayValidationSystem(String n) {
		super(n);
	}
	
	/** 
	 * Support for arrays is limited to array accesses with a known constant index.
	 * The implementation represents an array <pre>a</pre> as a collection of variables
	 * <pre>a_i0</pre>...<pre>a_in</pre> where <pre>n</pre> is the array length.
	 * 
	 * This method is called when an array assignment occurs. We do not modify the SSA 
	 * renamings of arrays, so we just have to return the first such renaming created with 
	 * {@link #addNewArrayVar(String, int, Type)} when the array was first declared.
	 * 
	 * @return The ArrayVariable stored in the array symbol table for the array of the given name.
	 */
	@Override
	public ArrayVariable addArrayVar(String name) {
		return var.getArray(name);
	}

	/** 
	 * Support for arrays is limited to array accesses with a known constant index.
	 * The implementation represents an array <pre>a</pre> as a collection of variables
	 * <pre>a_i0</pre>...<pre>a_in</pre> where <pre>n</pre> is the array length.
	 * 
	 * This method is called when an array is declared (either as method parameter or as a variable).
	 * We add this array to the array symbol table to keep track of its type and length, but we 
	 * won't use the renaming features of ArraySymbolTable.
	 * We also create the corresponding collection of scalar variables.
	 * 
	 * @return 
	 */
	@Override
	public ArrayVariable addNewArrayVar(String name, int length, Type t) {
		return addNewArrayVar(name, length, t, Use.LOCAL);
	}

	/** 
	 * Support for arrays is limited to array accesses with a known constant index.
	 * The implementation represents an array <pre>a</pre> as a collection of variables
	 * <pre>a_i0</pre>...<pre>a_in</pre> where <pre>n</pre> is the array length.
	 * 
	 * This method is called when an array is declared (either as method parameter or as a variable).
	 * We add this array to the array symbol table to keep track of its type and length, but we 
	 * won't use the renaming features of ArraySymbolTable.
	 * We also create the corresponding collection of scalar variables.
	 * 
	 * @return 
	 */
	@Override
	public ArrayVariable addNewArrayVar(String name, int length, Type t, Use u) {
		for (int i=0; i<length; i++) {
			addNewVar(name + "_i" + i, t, u);
		}
		return var.addNewArrayVar(name,  t,u,length);
	}

}
