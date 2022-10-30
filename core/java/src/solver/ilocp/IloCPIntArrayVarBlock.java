package solver.ilocp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import expression.variables.ArrayVariable;
import expression.variables.ConcreteArrayVariable;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;

import validation.Validation;
import validation.system.AbstractArrayVariableStore;
import validation.system.xml.IntegerValidationCSP;

/** 
 * Stores integer arrays of a Java program as arrays of IloCP 
 * integer variables.
 * 
 * Each time the value of an element of the array named <pre>A_i</pre> is modified, 
 * this creates a new array <pre>A_(i+1)</pre> in this store whose elements are identical 
 * to those in <pre>A_i</pre> except for the modified element. 
 * 
 * While new arrays are added to this store, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm. 
 * 
 * We keep track of the variables by their name.
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 */
public class IloCPIntArrayVarBlock extends AbstractArrayVariableStore<ConcreteArrayVariable<IloIntVar[]>> {
	/**
	 * The concrete solver: Ilog CP Optimizer.
	 */
	private IloCPSolver solver; 

	/**
	 * The constructor of a store of arrays of concrete integer variables.
	 * 
	 * @param solver The concrete solver associated with this store of integer 
	 *               array variables. 
	 * @param format The format of integers: the number of bits encoding the integers.
	 */
	public IloCPIntArrayVarBlock(IloCPSolver solver) {
		this.solver = solver;
	}

	//---------------------------------
	//ArrayVarBlock interface methods
	//---------------------------------
	
	/**
	 * Adds an array of concrete integer variables to this store.
	 * 
	 * The name of the concrete integer variable at index <pre>i</pre> will be
	 * <pre>name[idx]</pre>/
	 * 
	 * @param name The name of the array, including its SSA renaming number.
	 * @param length The number of elements of this array.
	 */
	@Override
	public void add(ArrayVariable array) {
		//We could have used:
		//IloIntVar[] array = concreteSolver.intVarArray(length, vMin, vMax, name);
		//But then the names of each integer variable is not documented by Ilog. 
		//Though some testings showed that the names are as expected, this might 
		//be subject to change from Ilog.
		IloIntVar[] cpArray = new IloIntVar[array.length()];
		for(int i=0; i < array.length(); i++) {
			cpArray[i] = solver.createVar(array.name() + "[" + i + "]",
										  array.type(),
										  Validation.INTEGER_MIN_BOUND, 
										  Validation.INTEGER_MAX_BOUND);
		}
		add(array.name(), new ConcreteArrayVariable<IloIntVar[]>(array, cpArray));
	}
	
	/** 
	 * Adds the values for arrays from <code>arrays</code> to the given CSP. 
	 * For each array in <code>arrays</code>, a new array with the same name is created and 
	 * filled with the values of the array elements. This means that new concrete  
	 * variables are added to the constraint system.
	 *
	 * @param arrays A mapping from array names to array element values.
	 * @param csp The constraint problem to which add the new arrays of concrete variables.
	 */
	public void add(Map<ArrayVariable, Number[]> integerArrays, IntegerValidationCSP csp) 
	{
		for(Entry<ArrayVariable, Number[]> entry : integerArrays.entrySet()) {
			add(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Adds an array, named <code>arrayName</code>, of concrete variables to the constraint system 
	 * <code>csp</code>. These concrete variables are initialized with the values provided in 
	 * <code>array</code>. A <code>null</code> value, means that the concrete variable can take any
	 * value over the integers' domain.
	 * @param values An array of, possibly <code>null</code>, Integer objects. These are the values
	 *        of the array variables to add to the csp.
	 * @param arrayName The name of the array to add to the csp.
	 * @param csp A constraint system where to add an array of variables.
	 */
	private void add(ArrayVariable array, Number[] values) {
		IloIntVar[] dest = new IloIntVar[values.length];
		for(int i=0; i<values.length; i++) {
			if(values[i] != null) {
				int val = values[i].intValue();
				dest[i] = solver.createVar(array.name() + "[" + i + "]", array.type(), val, val);
			}
			else {
				dest[i] = solver.createVar(array.name() + "[" + i + "]",
						array.type(),
						Validation.INTEGER_MIN_BOUND, 
						Validation.INTEGER_MAX_BOUND);
			}	
		}
		add(array.name(), new ConcreteArrayVariable<IloIntVar[]>(array, dest));
	}

	/**
	 * Maps an array with the values found by the solver.
	 * Each array of concrete variables in this block gives an entry in the map 
	 * that associates it with an array of Numbers whose value is the one of the 
	 * concrete variable or <code>null</code> if no value was set by the solver.
	 * 
	 * @return A map of array names and arrays of values.
	 */
	public Map<ArrayVariable, Number[]> getArraysValues() {
		Map<ArrayVariable, Number[]> map = new HashMap<ArrayVariable, Number[]>();
		
		for(ConcreteArrayVariable<IloIntVar[]> array : elts) {
			IloIntVar[] src = array.concreteArray();
			Integer[] dest = new Integer[src.length];
			for(int i=0; i<src.length; i++) {
				try {
					dest[i] = new Integer((int)solver.concreteSolver.getValue(src[i]));
				} catch(Exception e) {
					if(e instanceof IloException) {
						dest[i] = null;
					}
					else {
						System.err.println("IloCP getArraysValues method: Unexpected exception caught!");
						e.printStackTrace();
						System.exit(21);
					}
				}
			}
			map.put(array, dest);
		}
		return map;
	}
		
	//--------------------------------------------------------
	//VarBlock interface methods to handle backtracking
	//--------------------------------------------------------

	/**
	 * Restores the previous array store.
	 * All variables added since the last call to {@link #save} are removed from this
	 * store.
	 */
	@Override
	public void restore() {
		int idxLastVarToRemove = ((Integer)save.pop()).intValue();
		
		for (int i = elts.size()-1; i >= idxLastVarToRemove; i--) {
			remove(elts.get(i).name(), i);
		}
	}
		
}
