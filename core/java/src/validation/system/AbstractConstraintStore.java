package validation.system;

import validation.Validation.VerboseLevel;

/** 
 * Stores the constraints generated from a Java method.
 * 
 * While new constraints are added to the store of constraints, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm. 
 * 
 * @author Olivier Ponsini
 */
public abstract class AbstractConstraintStore<T> extends AbstractStore<T> implements ConstraintStore<T> {

	/**
	 * Adds an element to this store.
	 * 
	 * @param elt The element to be added to this store.
	 * 
	 * @return <code>True</code> if the element could be added; <code>false</code> otherwise.
	 */
	public boolean add(T elt) {
		return elts.add(elt);
	}

	/**
	 * Convert a concrete constraint into a String for printing.
	 * 
	 * @param ctr A constraint as handled by the concrete solver.
	 * @return The String representation of the given concrete constraint.
	 */
	public String ctrToString(T ctr) {
		return ctr.toString();
	}
			

	/**
	 * Restores the previous constraint store.
	 * All constraints added since the last call to {@link #save} are removed from this
	 * constraint store.
	 */
	@Override
	public void restore() {
		int idxLastConstrToRemove = ((Integer)save.pop()).intValue();
		
		for (int i = elts.size()-1; i >= idxLastConstrToRemove; i--) {
			elts.remove(i);
		}
	}
	
	/** 
	 * Prints all the elements.
	 */
	@Override
	public void print() {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.print(this.toString());
		}
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (T c: elts) {
			s.append(ctrToString(c));
			s.append("\n");
		}
		return s.toString();
	}

}
