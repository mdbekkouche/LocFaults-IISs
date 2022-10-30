package solver.fplib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.Map.Entry;

import expression.variables.ConcreteVariable;
import expression.variables.Variable;

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
public class FplibVarDeepStore extends FplibVarBlock {
	
	protected HashMap<String, ConcreteVariable<Long>> elts;

	protected Stack<HashMap<String, ConcreteVariable<Long>>> save;
			
	public FplibVarDeepStore() {
		elts = new LinkedHashMap<String, ConcreteVariable<Long>>();
		save = new Stack<HashMap<String, ConcreteVariable<Long>>>();
	}

	/**
	 * Adds a concrete variable to this store of concrete variables.
	 * 
	 * @param v The variable to add.	 
	 */
	public void add(ConcreteVariable<Long> v) {
		elts.put(v.name(), v);
	}

	/**
	 * Adds a variable to this store of concrete variables.
	 * 
	 * @param v The variable to add.	 
	 */
	@Override
	public ConcreteVariable<Long> add(Variable v) {
		ConcreteVariable<Long> cv = new ConcreteVariable<Long>(v);
		elts.put(v.name(), cv);
		return cv;
	}
	
	/** 
	 * Prints all the variables' values.
	 * It requires the variables exist in the fplib current model.
	 */
	@Override
	public void print() {
		for (ConcreteVariable<Long> fplibVar: elts.values()) {
			if (fplibVar.concreteVar() != null) {
				FplibSolver.fplibVarDisplay(fplibVar.concreteVar());
				System.out.println();
			}
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (ConcreteVariable<Long> fplibVar: elts.values()) {
			sb.append(fplibVar.toString() + ": ");
			sb.append(fplibVar.domain() + "\n");
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
		elts = (HashMap<String, ConcreteVariable<Long>>) save.pop();
	}

	@Override
	public ConcreteVariable<Long> get(String name) {
		return elts.get(name);
	}

	@Override
	public void reset() {
		elts.clear();
	}

	public void reset(HashMap<String, ConcreteVariable<Long>> savedVar) {
		elts = savedVar;
	}

	@Override
	public void save() {
		// Shallow copy of the hash map
		HashMap<String, ConcreteVariable<Long>> copy = (HashMap<String, ConcreteVariable<Long>>)elts.clone();
		// Deep copy of the variables in the map
		for (Entry<String, ConcreteVariable<Long>> e: copy.entrySet()) {
			e.setValue(e.getValue().clone());
		}
		save.push(copy);
	}

	
	public HashMap<String, ConcreteVariable<Long>> copyVariableMap() {
		// Shallow copy of the hash map
		HashMap<String, ConcreteVariable<Long>> copy = (HashMap<String, ConcreteVariable<Long>>)elts.clone();
		// Deep copy of the variables in the map
		for (Entry<String, ConcreteVariable<Long>> e: copy.entrySet()) {
			e.setValue(e.getValue().clone());
		}
		return copy;
	}

	@Override
	public int size() {
		return elts.size();
	}

	@Override
	public Iterator<ConcreteVariable<Long>> iterator() {
		return elts.values().iterator();
	}
	
}
