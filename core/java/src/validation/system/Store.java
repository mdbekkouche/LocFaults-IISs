package validation.system;

/** 
 * Common interface to constraint and variable stores.
 * Elements of type <pre>T</pre> are stored. 
 * This interface allows to iterate over the stored elements.
 * 
 *  This interface is extended by ConstraintStore and VarStore interfaces for storing
 *  constraints and variables (array or scalar variables) respectively.
 *  
 *  Abstract implementations of all these interfaces exist.
 * 
 * @author Olivier Ponsini
 *
 */
public interface Store<T> extends Iterable<T> {

	/** Gets the number of elements in this store. */
	public int size();
	
	/** Prints the elements. */
	public void print() ;

	//----------------------------
	// methods to handle backtrack
	
	/** Saves the current store. */
	public void save() ;

	/** Restores the previous store. */
	public void restore();

	/**
	 * Empties this constraint store.
	 */
	public void reset();

}
