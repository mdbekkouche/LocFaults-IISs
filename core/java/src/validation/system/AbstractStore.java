package validation.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import validation.Validation.VerboseLevel;

/** 
 * Abstract implementation of generic variable and constraint stores.
 * 
 * @author Olivier Ponsini
 */
public abstract class AbstractStore<T> implements Store<T> {
	
	/**
	 * The list of elements handled by this store.
	 */
	protected ArrayList<T> elts;
	/**
	 * This stack handles saving and restoring stores of variables.
	 * The typical usage of this store is to add new variables until
	 * it is needed to remove the last ones added. In order to do this, we just need to 
	 * record the index in the array of variables {@link #elts} from which we started
	 * to add new variables we may need to remove.
	 * 
	 * @see #save()
	 * @see #restore()
	 */
	protected Stack<Integer> save; 

	/**
	 * The constructor of a store of variables.
	 * 
	 */
	public AbstractStore() {
		elts = new ArrayList<T>();
		save = new Stack<Integer>();
	}

	@Override
	public int size() {
		return elts.size();
	}
	
	/**
	 * Returns the i+1th element of this store.
	 * 
	 * @param i The index of the element to retrieve; ranges from 0 to this.size()-1.
	 * 
	 * @return the i+1th element of this store.
	 */
	public T get(int i) {
		return elts.get(i);
	}
	
	/**
	 * @return An Iterator over the elements of this store.
	 */
	@Override
	public Iterator<T> iterator() {
		return this.elts.iterator();
	}
	
	/** 
	 * Prints all the elements.
	 */
	@Override
	public void print() {
		if (VerboseLevel.TERSE.mayPrint()) {
			for (T v : elts)
				System.out.print(v.toString() + "\n");
		}
	}
	
	/** 
	 * Saves this store to be restored later.
	 * Saving the store amounts to saving on a stack the index following the current last
	 * element in the store. Since we can only append new elements or restore a previous
	 * store, it is safe to just remember from which index the new elements were added.
	 */
	@Override
	public void save() {
		save.push(new Integer(elts.size()));
	}
	
	@Override
	public void reset() {
		elts.clear();
		save.clear();
	}
			
}
