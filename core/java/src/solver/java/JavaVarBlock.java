package solver.java;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Map.Entry;

import expression.variables.Variable;

import validation.Validation.VerboseLevel;
import validation.system.VariableStore;


public class JavaVarBlock implements VariableStore<Entry<Variable, Number>> {

	// values associated to variables names
	private HashMap<Variable,Number> value;

	public JavaVarBlock() {
		value = new HashMap<Variable,Number>();
	}
	
	// to build a var block from a SymbolTables
	// 
	public JavaVarBlock(expression.variables.SymbolTable st){
		value = new HashMap<Variable,Number>();
		HashMap<String,Variable > stVal = st.getAllVariables();
		for (Variable v : stVal.values())  
			value.put(v,null);
	}
	
	// to build a var block from a TreeSet that has
	// been built after CFG simplification
	// 
	public JavaVarBlock(Collection<Variable> ts){
		value = new HashMap<Variable,Number>();
		for (Variable v : ts)  
			value.put(v,null);
	}
	
//	/** to set a constant value to a variable
//	 */
//	private void setConstantValue(String name, Number val){
//		value.put(name,val);
//	}
	
	/** to set a constant value to a variable
	 */
	public void setConstantValue(Variable v, Number val){
		value.put(v,val);
	}

	
	public Number getValue(Variable v) {
		return value.get(v);
	}
	
	public Set<Variable> vars() {
		return value.keySet();
	}
	
	@Override
	public String toString() {
		String s = "[";
		for (Variable v : value.keySet()) {
			s += v.name() + ": " + value.get(v) + ",";
		}
		s = s.substring(0,s.length()-1);
		s+="]";
		return s;
	}

	@Override
	public Entry<Variable, Number> add(Variable v) {
		if (!value.containsKey(v)) {
			value.put(v, null);
			return new AbstractMap.SimpleEntry<Variable, Number>(v, null);
		}
		else {
			return new AbstractMap.SimpleEntry<Variable, Number>(v, value.get(v));
		}
	}

	@Override
	public void restore() {
		// TODO Auto-generated method stub
	}

	@Override
	public Iterator<Entry<Variable, Number>> iterator() {
		return value.entrySet().iterator();
	}

	@Override
	public Entry<Variable, Number> get(String n) {
		for (Entry<Variable, Number> e: value.entrySet()) {
			if (e.getKey().name().equals(n)) {
				return e;
			}
		}
		return null;
	}

	@Override
	public void print() {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println(this.toString());
		}
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub		
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void reset() {
		value.clear();
	}

}
