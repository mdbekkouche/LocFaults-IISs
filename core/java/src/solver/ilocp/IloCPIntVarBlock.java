package solver.ilocp;

import ilog.concert.IloIntVar;

import expression.variables.ConcreteVariable;
import expression.variables.Variable;
import validation.Validation;
import validation.system.AbstractVariableStore;

/** 
 * Stores integer variables of a Java block as IloCP integer variables.
 * 
 * Variables are added when found in expressions, whether linear or non linear.
 * While new variables are added to this store, the class can handle 
 * saving and restoring the current store, to be used for instance in 
 * a backtracking algorithm. 
 * 
 * We keep track of the variables by their name.
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 */
public class IloCPIntVarBlock extends AbstractVariableStore<ConcreteVariable<IloIntVar>> {
	/**
	 * The concrete solver: Ilog CP Optimizer.
	 */
	private IloCPSolver solver; 
	
	/**
	 * The constructor of a store of integer variables.
	 * 
	 * @param solver The concrete solver associated with this store of integer variables. 
	 * @param format The format of integers: the number of bits encoding the integers.
	 */
	public IloCPIntVarBlock(IloCPSolver solver) {
		this.solver=solver;
	}

	
	//---------------------------------
	//VarBlock interface methods
	//---------------------------------
	
	/** 
	 * Adds an integer variable to this store. 
	 * 
	 * @param v The integer variable to be added. We will extract its name.
	 */
	@Override
	public ConcreteVariable<IloIntVar> add(Variable v) {
		ConcreteVariable<IloIntVar> cv = new ConcreteVariable<IloIntVar>(v, 
                														solver.createVar(
                																v.name(),
                																v.type(),
                																Validation.INTEGER_MIN_BOUND, 
                																Validation.INTEGER_MAX_BOUND));
		add(v.name(), cv);
		return cv;
	}

	/**
	 * Restores the previous integer variable store.
	 * All variables added since the last call to {@link #save} are removed from this
	 * store. They arre not actually removed from the concrete CSP model (this is not yet a problem 
	 * since we only handle parameters and local variables whose scope is the one of the method.
	 */
	@Override
	public void restore() {
		int idxLastVarToRemove = ((Integer)save.pop()).intValue();
		
		for (int i = size()-1; i >= idxLastVarToRemove; i--) {
			remove(get(i).name(), i);
		}
	}
		
}