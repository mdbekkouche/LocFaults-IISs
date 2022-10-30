package expression.variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.Set;

import expression.variables.Variable.Use;

import validation.Validation.VerboseLevel;
import validation.util.Type;

/** class to store variables and to manage SSA renaming 
 * contains all the symbols found in the program (and the specification)
 * 
 * Parameters, local and global variables are stored in the same table
 * but they have different prefix : parameters and local variables are prefixed with the method name 
 * while global variables are prefixed with the class name.
 * 
 * When looking for a variable (method getPrefixed), one first looks if the variable prefixed by the 
 * class name exists and then returns the variable prefixed with the method name
 * 
 * Method name always ends with "_0"
 * Variables are renamed each time a call is made to the function (method oneMoreCall() in FunctionCallNode)
 * 
 * 
 * TODO : use 2 tables of symbols for local and global variables
 * 
 * @author helen
 *
 */

//TODO : have a symbol table for each Java block

public class SymbolTable implements Cloneable {

	// Variables indexed by their complete name
	private HashMap<String, Variable> var;

	// NameSSA variables indexed by their root
	private HashMap<String, NameSSA> name;

	// save stack
	private Stack<HashMap<String, ?>> save;

	public SymbolTable() {
		var = new HashMap<String, Variable>();
		name = new HashMap<String, NameSSA>();
		save = new Stack<HashMap<String, ?>>();
	}
	
	/**
	 * Empties this symbol table.
	 */
	public void reset() {
		var.clear();
		name.clear();
	}
	
	/** returns true if the table contains a variable with root name n */
	public boolean containsKey(String n){
		return name.containsKey(n);
	}
	
	
	/////////////////////////////////////////////////////////////
	// methods to add variables
	// they are duplicated to handle both scalar and array variables
	
	/** create and add a new scalar variable with renaming 0 */
	public Variable addNewVar(String rootName, Type t, Use u)  {
		NameSSA ssa = new NameSSA(rootName);
		name.put(rootName, ssa);
		Variable v = new Variable(ssa.current(), t, u);
		var.put(ssa.current(), v);
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println("SymbolTable.addNewVar: " + v.name());
		}
		// to add the old value of the variable (i.e. before function call)
		v.setOld(v);
		return v;
	}
	/** create and add a new array variable with renaming 0 */
	public ArrayVariable addNewArrayVar(String rootName, Type t, Use u, int l)  {
		NameSSA ssa = new NameSSA(rootName);
		name.put(rootName, ssa);
		ArrayVariable v = new ArrayVariable(ssa.current(), t, l, u);
		var.put(ssa.current(), v);
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println("SymbolTable.addNewArrayVar: " + v.name());
		}
		// to add the old value of the variable (i.e. before function call)
		v.setOld(v);
		return v;
	}
	
	/** create and add a new scalar variable with renaming 0 */
	public Variable addNewPrefixedVar(String prefix, String rootName, Type t, Use u)  {
		//System.out.println("symbol table addNewPrefixedVar " + prefix + " " + n);
		return addNewVar(prefix + "_" + rootName, t, u);
	}
	
	/** create and add a new array variable with renaming 0 */
	public ArrayVariable addNewPrefixedArrayVar(String prefix, String rootName, Type t, Use u, int l)  {
		//System.out.println("symbol table addNewPrefixedVar " + prefix + " " + n);
		return addNewArrayVar(prefix + "_" + rootName, t, u,l);
	}
	
	/** create and add a new variable with renaming 0 
	 * the variable name is prefixed with a method name*/
	public Variable addNewPrefixedVar(String prefix, String rootName, Type t)  {
		return addNewPrefixedVar(prefix, rootName, t, Use.LOCAL);
	}
	
	/** create and add a new variable with renaming 0 
	 * the variable name is prefixed with a method name*/
	public ArrayVariable addNewPrefixedArrayVar(String prefix, String rootName, Type t,int l)  {
		return addNewPrefixedArrayVar(prefix, rootName, t, Use.LOCAL,l);
	}
	
	/** add a new renaming of a scalar variable 
	 * 
	 * @return <code>null</code> if no variable with the given name exists in this symbol table.
	 */
	public Variable addVar(String n)  {
		//System.out.println("SymbolTable addVar " + n);
		NameSSA ssa = name.get(n);
		if (ssa != null) {
			Variable lastRenaming = var.get(ssa.current());
			ssa.next();
			Variable v;
			if (lastRenaming instanceof ArrayVariable)
				v= new ArrayVariable(ssa.current(), lastRenaming.type, ((ArrayVariable)lastRenaming).length(),lastRenaming.use);
			else
				v= new Variable(ssa.current(), lastRenaming.type, lastRenaming.use);
			v.setOld(lastRenaming.oldValue());
			var.put(ssa.current(), v);
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println("SymbolTable.addVar: " + v.name());
			}
			return v;
		}
		else {
			return null;
		}		
	}
	

	
	/** add a new renaming of a variable
	 */
	public Variable addPrefixedVar(String methodName, String className, String n)  {
		Variable v = addVar(className + "_" + n);
		if (v == null)
			v = addVar(methodName + "_" + n);
		return v;
	}
	

	/////////////////////////////////////////////////////////////
	// methods to get variables

	
	/** 
	 * Returns the variable associated to the current renaming
	 * of variable with root name n or null if such a variable does not exist in the table. 
	 */
	public Variable get(String rootName) {
//		System.out.println("symbolTable get "+n);
		NameSSA ssa = name.get(rootName);	
//		System.out.println("ssa " + ssa);
		if (ssa != null) 
			return var.get(ssa.current());
		else
			return null;
	}
	
	/** 
	 * Returns the variable associated to the current renaming
	 * of variable with root name n or null if such a variable does not exist in the table. 
	 */
	public ArrayVariable getArray(String rootName) {
		return (ArrayVariable)get(rootName);
	}

	/** 
	 * Returns the variable with given root name and given SSA number. 
	 */
	public Variable get(String rootName, int ssaNum) {
		return var.get(rootName + "_" + ssaNum);
	}
	
	/** 
	 * Returns the variable with given root name and given SSA number. 
	 */
	public ArrayVariable getArray(String rootName, int ssaNum) {
		return (ArrayVariable)get(rootName,ssaNum);
	}
	
	/** 
	 * Returns the variable associated to the given rootName and given SSA number.
	 * 
	 * The variable can be prefixed with the method name if it is local
	 * or by the class name if it is global.
	 * 
	 */
	public Variable getPrefixed(String methodName, String className, String unprefixedName, int ssaNum) {
		// Search for a global variable first
		Variable v = get(className + "_" + unprefixedName, ssaNum);
		if (v == null) // This is not a global variable but a local one
			v = get(methodName + "_" + unprefixedName, ssaNum);		
		return v;
	}

	/** 
	 * Returns the variable associated to the given rootName and given SSA number.
	 * 
	 * The variable can be prefixed with the method name if it is local
	 * or by the class name if it is global.
	 * 
	 */
	public ArrayVariable getPrefixedArray(String methodName, String className, String unprefixedName, int ssaNum) {
		return (ArrayVariable)getPrefixed(methodName, className, unprefixedName,ssaNum);
	}
	
	/** returns the variable associated to the current renaming
	 * of variable with root name n 
	 * the variable can be prefixed with the method name if it is local
	 * or by the class name if it is global
	 * 
	 */
	public Variable getPrefixed(String methodName, String className, String unprefixedName) {
//		System.out.println("symbolTable getPrefixed " + methodName + " " + className + " " + n);
		
		// Search for a global variable first
		Variable v = get(className + "_" + unprefixedName);
		if (v == null) // This is not a global variable but a local one
			v = get(methodName + "_" + unprefixedName);
		
		return v;
	}
	
	/** returns the variable associated to the current renaming
	 * of variable with root name n 
	 * the variable can be prefixed with the method name if it is local
	 * or by the class name if it is global
	 * 
	 */
	public ArrayVariable getPrefixedArray(String methodName, String className, String unprefixedName) {
		return (ArrayVariable)getPrefixed(methodName, className, unprefixedName);

	}
	
	/** save and restore SSAname, to have the right renaming of
	 * variables
	 */
//	TODO : trouver quelque chose de plus économe que le clone
	public void save() {
		save.push((HashMap)name.clone());
		// to have right renaming for each variable
		for (NameSSA n : name.values()) {
			n.save();
		}
		save.push((HashMap)var.clone());
	}
	
	public void restore() {
		var = (HashMap<String,Variable>)save.pop();
		name = (HashMap<String,NameSSA>)save.pop();
		for (NameSSA n : name.values()) {
			n.restore();
		}
	}
	
	/** 
	 *  to make copies of symbol tables to get correct SSA renamings when parsing if statements
	 */
	public Object clone() {
		SymbolTable st = new SymbolTable();
		st.var = (HashMap<String, Variable>) var.clone();
		st.name = new HashMap<String, NameSSA>();
		for (String n : name.keySet()) {
			st.name.put(n, (NameSSA) name.get(n).clone());
		}
		st.save = (Stack<HashMap<String, ?>>) save.clone();
		return st;
	} 	

	/** 
	 *  Copies only var and name
	 */
	public SymbolTable copy() {
		SymbolTable st = new SymbolTable();
		st.var = (HashMap<String, Variable>) var.clone();
		st.name = new HashMap<String, NameSSA>();
		for (String n : name.keySet()) {
			st.name.put(n, (NameSSA) name.get(n).clone());
		}
		return st;
	} 	

	public void setSymbols(SymbolTable st) {
		var = st.var;
		name = st.name;
	}
	
	/** return the set of names of program variables */
	public Set<String> getNames() {
		//		ArrayList<String> names = new ArrayList<String>();
		//		for (NameSSA n : name.values())
		//			names.add(n.current());
		return name.keySet();
	}
		
	/** returns the current SSA names of program variables */
	public ArrayList<String> getCurrentVarNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (NameSSA n : name.values())
			names.add(n.current());
		return names;
	}
	
	/** returns the names of all SSA variables of program  
	 * used in java2CFG.evaluator
	 * */
	public ArrayList<String> getAllVarNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (NameSSA n : name.values()){
			String root = n.name();
			for (int i=0;i<=n.cpt;i++)
				names.add(root+"_"+i);
		}
		return names;
	}
	
	public HashMap<String, Variable> getAllVariables() {
		return var;
	}
	
	/** return value of variables */
	public String variableValues() {
		if (name.values().size()==0)
			return "Symbol table is empty";
		String s = "Variable values\n";
		for (NameSSA n : name.values()) {
			Variable v = var.get(n.current());
			if (v.isConstant())
				s += n.current() + ":" + v.constantNumber();
			else 
				s += n.current() + ":" + " unknown";
			s+=" ";
		}
		return s;
	}
	
	public String toString() {
		return var.toString();
	}

	public void addRenamings(String rootName, int min, int max, SymbolTable otherTable) {
		//Set the new current maximum SSA number for the variable rootName in this table
		NameSSA ssaNumber = name.get(rootName + "_" + (min-1));
		if (ssaNumber != null) {
			ssaNumber.setNumber(max);
		}
		else {
			name.put(rootName, new NameSSA(rootName, max));
		}
		
		//Add variables from the otherTable for the renamings between min+1 and max
		for (int i=min; i<=max; i++) {
			var.put(rootName + "_" + i, otherTable.get(rootName, i));
		}
	}
}