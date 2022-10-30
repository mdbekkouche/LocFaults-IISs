package validation.system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.Map.Entry;

import expression.variables.Variable;

import validation.Validation.VerboseLevel;
import validation.system.VariableVarStore;

/** 
 * Stores variables of a Java block as objects of type Variable.
 * 
 * Quick hack to guarantee to fully save/restore all the variables.
 * Almost everything inherited is unused or overridden, so the inheritage hierarchy should be
 * modified... 
  
 * 
 * @author Olivier Ponsini
 */
public class VariableVarDeepStore extends VariableVarStore {
	
	protected HashMap<String, Variable> elts;

	protected Stack<HashMap<String, Variable>> save;
	

	public VariableVarDeepStore() {
		elts = new LinkedHashMap<String, Variable>();
		save = new Stack<HashMap<String, Variable>>();
	}

	/**
	 * Adds a variable to this store of variable.
	 * 
	 * @param v The variable to add.	 
	 */
	@Override
	public Variable add(Variable v) {
		elts.put(v.name(), v);
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
		elts = (HashMap<String, Variable>) save.pop();
	}

	@Override
	public Variable get(String name) {
		return elts.get(name);
	}

	@Override
	public void reset() {
		elts.clear();
	}

	public void reset(HashMap<String, Variable> savedVar) {
		elts = savedVar;
	}
	
	@Override
	public void save() {
		// Shallow copy of the hash map
		HashMap<String, Variable> copy = (HashMap<String, Variable>)elts.clone();
		// Deep copy of the variables in the map
		for (Entry<String, Variable> e: copy.entrySet()) {
			e.setValue(e.getValue().clone());
		}
		save.push(copy);
	}
	
	public HashMap<String, Variable> copyVariableMap() {
		HashMap<String, Variable> copy = (LinkedHashMap<String, Variable>)elts.clone();
		// Deep copy of the variables in the map
		for (Entry<String, Variable> e: copy.entrySet()) {
			e.setValue(e.getValue().clone());
		}
		return copy;
	}

	@Override
	public int size() {
		return elts.size();
	}

	@Override
	public Iterator<Variable> iterator() {
		return elts.values().iterator();
	}

}
