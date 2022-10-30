package solver.ibex;

import solver.Solver.SolverEnum;
import validation.solution.Solution;
import validation.system.ExpressionCtrStore;
import validation.system.VariableVarStore;
import validation.system.xml.ValidationCSP;
import validation.system.xml.ValidationSystem;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;

/**
 * This class uses IBEX to solve CSPs over reals.
 * 
 * @author Olivier Ponsini
 *
 */
public class IbexRealValidationCSP extends ValidationCSP {
	/**
	 * Constructs a concrete CSP on reals handled by IBEX.
	 */ 
	public IbexRealValidationCSP(String name, ValidationSystem vs) {
		super(name, SolverEnum.IBEX_PAVER, vs);
		((IbexSolver)cspSolver).setCSP(
				(VariableVarStore)this.varBlock, 
				(ExpressionCtrStore)this.constraintBlock());
	}

	/**
	 * Creates:
	 *   - the store of constraints that will be handled by the concrete solver;
	 *   - the store of variables that will be handled by the concrete solver.
	 *   
	 * This method is called by a mother class constructor.
	 *  
	 */	
	@Override
	protected void initCSP() {
		constr = new ExpressionCtrStore();
		varBlock = new VariableVarStore();
		arrayVarBlock = null;
		visitor = new IbexExpressionVisitor((IbexSolver)cspSolver);
	}

	@Override
	public Solution solution() {
		Variable var;
		double min, max;
		Solution sol = new Solution(this.getElapsedTime());
		for (Variable v: (VariableVarStore)varBlock) {
			min = ((IbexSolver)cspSolver).getLB(v.name());
			max = ((IbexSolver)cspSolver).getUB(v.name());
			var = v.clone();
			var.setDomain(min, max);
			sol.add(var);
		}
		return sol;
	}	

	@Override
	public DomainBox domainBox() {
		double min, max;
		DomainBox box = new DomainBox();
		for (Variable v: (VariableVarStore)varBlock) {
			min = ((IbexSolver)cspSolver).getLB(v.name());
			max = ((IbexSolver)cspSolver).getUB(v.name());
			box.add(new VariableDomain(v, min, max));
		}
		return box;
	}	
}
