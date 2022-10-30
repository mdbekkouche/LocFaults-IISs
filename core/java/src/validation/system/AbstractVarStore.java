package validation.system;

import java.util.HashMap;

/** 
 * Stores variables of a Java block (either arrays or scalar variables).
 * 
 * Variables are added when found in java parameters and declaration statements.
 * While new variables are added to this store, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm. The type of the variables actually stored is generic type T: it 
 * can be the non-specific {@link validation.variable.Variable} type, but it is often the solver
 * concrete variable type.
 * 
 * We keep track of the variables by their name.
 * 
 * @author Olivier Ponsini
 */
public abstract class AbstractVarStore<T> extends AbstractStore<T> implements VarStore<T> {
	/**
	 * A map from the variable names to their index in this store.
	 * This allows quick retrieval of variables from their name. 
	 * Using a Map<String, T> instead of {@link #elts} + {@link #varIndexes} would be 
	 * handy, but unfortunately Java does not provide a Map implementation with easy 
	 * and quick removal of last inserted elements (which is needed for efficiently 
	 * implementing {@link #restore()}). 
	 */
	protected HashMap<String, Integer> varIndexes;

	/**
	 * The constructor of a store of variables.
	 * 
	 */
	public AbstractVarStore() {
		varIndexes = new HashMap<String, Integer>();
	}

	/**
	 * Adds a variable <pre>var</pre> with name <pre>name</pre> to this block.
	 * @param name
	 * @param var
	 */
	protected void add(String name, T var) {
		if (!elts.contains(var)) {
			elts.add(var);
			varIndexes.put(name, size()-1);
		}
	}

	/**
	 * Removes the variable named <pre>name</pre> whose index in this block list of variables is
	 * <pre>index</pre>.
	 * (index could be deduced from the variable's name, but this is often already known when 
	 * calling this method, so we do not recompute it.
	 * @param name
	 * @param index
	 */
	protected void remove(String name, int index) {
		elts.remove(index);
		varIndexes.remove(name);
	}
	
	@Override
	public T get(String name) {
		try {
			return elts.get(varIndexes.get(name));
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void reset() {
		varIndexes.clear();
		super.reset();
	}
			
}
