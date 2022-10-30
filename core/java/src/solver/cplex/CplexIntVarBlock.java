package solver.cplex;

import ilog.concert.IloAddable;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import expression.variables.Variable;
import validation.system.AbstractVariableStore;

/** a class to store integer variables of a Java block
 * as IloCplex integer variables
 * allow to perform backtrak on the blocks
 * the variables are added when found in IntegerExpression
 * we create variables in the linear CSP each time it appears 
 * into a linear expression
 * 
 * @author helen
 *
 */
public class CplexIntVarBlock extends AbstractVariableStore<IloIntVar> {
	
	// the solver
	private Cplex solver; 
	
	public CplexIntVarBlock(Cplex s) {
		this.solver=s;
	}

	//--------------------------
	// methods to handle variables
	
	/** add integer variable */
	@Override
	public IloIntVar add(Variable v) {
		IloIntVar cv = solver.createConcreteVar(v);
		add(v.name(), cv);
		return cv;
	}
	
	/** restore the previous block */
	@Override
	public void restore() {
		// restoring variables
		int iVar = ((Integer)save.pop()).intValue();
		for (int i = size()-1; i>iVar-1; i--) {
			remove(get(i).getName(), i);
		}
	}

	/** returns the Cplex variable of name "name"
	 * used when parsing IntegerExpression to build expressions
	 * with concrete Cplex syntax of variables
	 * if the variable is not known, then create a new variable
	 */
	public IloIntVar getCplexVar(Variable v) {
		Integer index = varIndexes.get(v.name());
		if (index == null) {
			return add(v);
		}
		else {
			return get(index);
		}
	}

	/** 
	 * Returns the concrete solver.
	 */
	public IloCplex getIloCplex() {
		return solver.getIloCplex();
	}
	
}
