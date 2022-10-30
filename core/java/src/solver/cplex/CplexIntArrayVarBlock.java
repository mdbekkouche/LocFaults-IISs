package solver.cplex;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import expression.variables.ArrayVariable;
import expression.variables.ConcreteArrayVariable;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;

import validation.Validation;
import validation.system.AbstractArrayVariableStore;
import validation.system.xml.IntegerValidationCSP;
import validation.util.Type;

/** a class to store integer array variables of a Java block
 * as IlcSolver integer array variables
 * allow to perform backtrak on the blocks
 * @author helen
 *
 */
public class CplexIntArrayVarBlock extends AbstractArrayVariableStore<ConcreteArrayVariable<IloIntVar>> {
	// int variables
	// array t, with renaming r, at index i is called t_r_i

	// the concrete solver
	private IloCplex s; 
	
	public CplexIntArrayVarBlock(Cplex s) {
		this.s = s.solver;
	}

	//--------------------------
	// methods to handle variables
	
	
	@Override
	public void add(ArrayVariable array) {
		try {
			IloIntVar elt = s.intVar(Validation.INTEGER_MIN_BOUND,
												 Validation.INTEGER_MAX_BOUND,
												 array.name());
			add(array.name(), new ConcreteArrayVariable<IloIntVar>(array, elt));
		} catch (IloException e) {
			System.err.println("Error when adding var in CPLEX constraint system!");
			e.printStackTrace();
			System.exit(7);
		}
	}
	
	/** 
	 * Adds the values of integer arrays from <code>arrays</code> to the given CSP. 
	 * For each array in <code>arrays</code>, a new array with the same name is created and 
	 * filled with the values of the array elements. This means that new concrete integer 
	 * variables are added to the constraint system.
	 *
	 * @param integerArrays A mapping from array names to array element values.
	 * @param csp The constraint problem to which add the new arrays of concrete variables.
	 */
	public void add(Map<ArrayVariable, Number[]> arrays, IntegerValidationCSP csp) {
		if(csp instanceof CplexIntegerValidationCSP) {
			for(Entry<ArrayVariable, Number[]> entry : arrays.entrySet()) {
				add(entry.getValue(), entry.getKey(), (CplexConstraintBlock)csp.constraintBlock());
			}
		}
	}
	
	/**
	 * Adds an array, named <code>arrayName</code>, of concrete variables to the 
	 * constraint system <code>csp</code>. These concrete variables are initialized 
	 * with the values provided in <code>array</code>. A <code>null</code> value, 
	 * means that the concrete variable can take any value over the integers' domain.
	 * 
	 * @param values An array of, possibly <code>null</code>, Integer objects. These are the values
	 *        of the array variables to add to the csp.
	 * @param arrayName The name of the array to add to the csp.
	 * @param csp A constraint system where to add an array of variables.
	 */
	private void add(Number[] values, ArrayVariable array, CplexConstraintBlock constraints) 
	{
		IloIntVar arrayElt;
		String eltName;
		
		for(int i=0; i<values.length; i++) {
			if(values[i] != null) {
				try {
					eltName = array.name() + "_" + i;
					arrayElt = s.intVar(Validation.INTEGER_MIN_BOUND,
							 			Validation.INTEGER_MAX_BOUND,
							 			eltName);
					add(eltName, 
						new ConcreteArrayVariable<IloIntVar>(
								new ArrayVariable(eltName, Type.INT, 0), arrayElt));
					constraints.add(s.eq(arrayElt, values[i].intValue()));
				} catch(IloException e) {
					System.err.println("Error when copying array element " 
							+ array.name() 
							+ "[" + i + "]" 
							+ " in Ilog CPLEX constraint system." );
					e.printStackTrace();
					System.exit(17);
				}
			}
		}
	}

	/**
	 * Maps an array name with the values found by the solver.
	 * Each array of concrete variables in this block gives an entry in the map that associates its 
	 * name with an array of Integers whose value is the one of the concrete variable or 
	 * <code>null</code> if no value was set by the solver.
	 * 
	 * @return A map of array names and arrays of values.
	 */
	public Map<ArrayVariable, Number[]> getArraysValues() {
		Map<ArrayVariable, Number[]> map = new HashMap<ArrayVariable, Number[]>();
		for(ConcreteArrayVariable<IloIntVar> array : elts) {
			Integer[] dest = new Integer[1];
				try {
					dest[0] = new Integer((int)s.getValue(array.concreteArray()));
				} catch(Exception e) {
					if(e instanceof IloException) {
						dest[0] = null;
					}
					else {
						System.err.println("Cplex getArraysValues method: Unexepected exception caught!");
						e.printStackTrace();
						System.exit(21);
					}
				}
			map.put(array, dest);
		}
		return map;
	}
			
	// return the name associated to the last renaming
	// of array with roof n, at index i
	// for example, if n=tab and i=8 then we must find
	// var of name tab_k_i where k is the biggest number 
	// such that tab_k_i is a variable
	
	// required to make the link between the successive renamings
	
	//TODO: stocker dans une structure de donnée
	// où on connait pour chaque indice le renomage précédent
	protected IloIntVar getLastVar(String n, int i){
		int maxRenaming = -1;

		for (String ss : this.varIndexes.keySet()) {
			int first = ss.indexOf('_');
			if (ss.substring(0,first).equals(n)){
				int second = ss.indexOf('_',first+1);
				String index = ss.substring(second+1);
				if (index.equals(i+"")) {
					String renaming = ss.substring(first+1, second);
					int ren = (new Integer(renaming)).intValue();
					if (ren>maxRenaming)
						maxRenaming=ren;
			    }	
     		}
		}
		String name;
		if (maxRenaming==-1) {
			name = n + '_' + 0 + '[' + i + ']';
			add(new ArrayVariable(name, Type.INT, 0));
		}
		else {
			name = n + '_' + maxRenaming + '[' + i + ']';
		}
		return get(name).concreteArray();
	}

	protected IloIntVar getNextVar(String n, int i){
		int maxRenaming = -1;

		for (String ss : varIndexes.keySet()) {
			int first = ss.indexOf('_');
			if (ss.substring(0,first).equals(n)){
				int second = ss.indexOf('_',first+1);
				String index = ss.substring(second+1);
				if (index.equals(i+"")) {
					String renaming = ss.substring(first+1, second);
					int ren = (new Integer(renaming)).intValue();
					if (ren>maxRenaming)
						maxRenaming=ren;
			    }	
     		}
		}
		String	name;
		if (maxRenaming==-1) 
			name = n + '_' + 0 + '[' + i + ']';
		else
			name = n + '_' + (maxRenaming+1) + '[' + i + ']';
		add(new ArrayVariable(name, Type.INT, 0));
		return get(name).concreteArray();
	}
	
	//--------------------------
	// methods to handle backtrak
	
	/** restore the previous block */
	@Override
	public void restore() {
		int idxLastVarToRemove = ((Integer)save.pop()).intValue();
		
		for (int i = elts.size()-1; i >= idxLastVarToRemove; i--) {
			remove(elts.get(i).name(), i);
		}
	}

}
