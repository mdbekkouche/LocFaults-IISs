package solver.cplex;

import java.util.HashMap;
import java.util.Map;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import expression.logical.LogicalExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;
import solver.Solver.SolverEnum;
import validation.solution.Solution;
import validation.system.xml.IntegerValidationCSP;
import validation.system.xml.ValidationSystem;
import validation.util.Type;

/** 
 * A concrete class for integer validation CSP
 * with CPLEX concrete solver.
 * 
 * @author helen
 *
 */
public class CplexIntegerValidationCSP extends IntegerValidationCSP
{
			
	public CplexIntegerValidationCSP(String n, ValidationSystem vs) {
		super(n, SolverEnum.CPLEX, vs);
	}
	
	@Override
	protected void initCSP() {
		Cplex js = (Cplex)cspSolver;
		constr = new CplexConstraintBlock(js);		
		varBlock = new CplexIntVarBlock(js);
		arrayVarBlock=new CplexIntArrayVarBlock(js);
		visitor = new CplexExpressionVisitor((CplexIntVarBlock)varBlock, (CplexIntArrayVarBlock)arrayVarBlock);
	}

	public IloCplex.Status getCplexStatus() {
		return ((CplexIntegerValidationCSP)varBlock).getCplexStatus();
	}

	/** 
	 * Adds a constraint that is in the JML <code>requires</code> statement.
	 * Allows to set array elements as constants.
	 * 
	 * @param c The precondition to be added.
	 * @param arrayElts The list of array elements appearing in the precondition.
	 * @param csp The CSP containing the variables appearing in the precondition.
	 */
	@Override
	public void addConstraint(LogicalExpression c, HashMap<String, ArrayElement> arrayElts)
	{
		constr.add(c, new CplexExpressionVisitor(arrayElts, 
												(CplexIntVarBlock)varBlock, 
												(CplexIntArrayVarBlock)arrayVarBlock));
	}

	/* returns a solution */
	@Override
	public Solution solution() {
		Solution sol = new Solution(this.getElapsedTime());
		Variable var;
		Integer val;
		
		try {
			for(IloIntVar iloVar: (CplexIntVarBlock)varBlock){
				val = new Integer((int)((Cplex)cspSolver).solver.getValue(iloVar));
				var = new Variable(iloVar.getName(), Type.INT, val, val);
				sol.add(var);
			}
			if (arrayVarBlock != null) {
				for(IloIntVar iloVar: (CplexIntVarBlock)arrayVarBlock){
					val = new Integer((int)((Cplex)cspSolver).solver.getValue(iloVar));
					var = new Variable(iloVar.getName(), Type.INT, val, val);
					sol.add(var);
				}
			}		
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		return sol;
	}

	/* returns a solution */
	@Override
	public DomainBox domainBox() {
		DomainBox box = new DomainBox();
		Variable var;
		Integer val;
		
		try {
			for(IloIntVar iloVar: (CplexIntVarBlock)varBlock){
				val = new Integer((int)((Cplex)cspSolver).solver.getValue(iloVar));
				var = new Variable(iloVar.getName(), Type.INT);
				box.add(new VariableDomain(var, val, val));
			}
			if (arrayVarBlock != null) {
				for(IloIntVar iloVar: (CplexIntVarBlock)arrayVarBlock){
					val = new Integer((int)((Cplex)cspSolver).solver.getValue(iloVar));
					var = new Variable(iloVar.getName(), Type.INT);
					box.add(new VariableDomain(var, val, val));
				}
			}		
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		return box;
	}

	@Override
	public void add(Map<ArrayVariable, Number[]> arrays) {
		((CplexIntArrayVarBlock)arrayVarBlock).add(arrays, this);
	}

	@Override
	public Map<ArrayVariable, Number[]> getArraysValues() {
		return ((CplexIntArrayVarBlock)arrayVarBlock).getArraysValues();
	}
	 
}
