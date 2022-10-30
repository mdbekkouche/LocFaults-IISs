package solver.java;

import java.util.HashMap;

import expression.variables.ArrayVariable;
import expression.variables.SymbolTable;
import expression.variables.Variable;


public class JavaArrayVarBlock {

	// values associated to variables names
	private HashMap<ArrayVariable,Number[]> value;

	public JavaArrayVarBlock() {
		value = new HashMap<ArrayVariable,Number[]>();
	}
	
	public JavaArrayVarBlock(SymbolTable st){
		value = new HashMap<ArrayVariable,Number[]>();
		HashMap<String,Variable > stVal = st.getAllVariables();
		for (Variable v : stVal.values())  {
			if (v instanceof ArrayVariable)
				value.put((ArrayVariable)v,null);
		}
	}
	
	/** to set a constant value to a variable
	 */
	public void setConstantValue(ArrayVariable v, Number[] val){
		value.put(v,val);
	}
	
	public Number[] getValue(ArrayVariable v) {
		return value.get(v);
	}
	
	public String toString() {
		String s = "[";
		for (ArrayVariable v : value.keySet()) {
			s += v.name() + ": " + value.get(v) + ",";
		}
		s = s.substring(0,s.length()-1);
		s+="]";
		return s;
	}

}
