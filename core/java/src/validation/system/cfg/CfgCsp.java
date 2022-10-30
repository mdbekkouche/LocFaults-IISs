package validation.system.cfg;

import solver.ConcreteSolver;
import validation.Validation;
import validation.solution.Solution;
import validation.solution.SolverStatus;
import validation.system.ArrayVariableStore;
import validation.system.ConstraintStore;
import validation.system.VariableStore;
import expression.ExpressionVisitor;
import expression.logical.LogicalExpression;
import expression.variables.ArrayVariable;
import expression.variables.DomainBox;
import expression.variables.Variable;

/**
 * Derived classes must set in their constructors the following attributes:
 * {@link #solver}, {@link #vars}, {@link #arrayVars}, {@link #ctrs}, and 
 * {@link #visitor}.
 * 
 * @author Olivier Ponsini
 *
 * @param <SolverG>
 * @param <VarBlockG>
 * @param <ArrayVarBlockG>
 * @param <ConstraintBlockG>
 * @param <ExpressionVisitorG>
 */
public abstract class CfgCsp<SolverG extends ConcreteSolver,
							 VarBlockG extends VariableStore,
							 ArrayVarBlockG extends ArrayVariableStore,
							 ConstraintBlockG extends ConstraintStore,
							 ExpressionVisitorG extends ExpressionVisitor> 
{
	/** The solver's status. */
	protected SolverStatus status;
	/** The CSP's name */
	protected String name;

	//Following attributes must be set by derived classes
	
	/** The CSP's solver. */
	public SolverG solver;
	protected VarBlockG vars;
	protected ArrayVarBlockG arrayVars;
	protected ConstraintBlockG ctrs; 
	/**
	 * The visitor to visit this CSP constraint expressions.
	 * It does not have to be created for each added constraint and can be stored here.
	 */
	protected ExpressionVisitorG visitor;
	
				
	public CfgCsp(String name) {
		this.name = name;
		status = new SolverStatus(name);
	}

	/** to enable a search process */
	public void startSearch() {
		solver.startSearch();
	}
	
	/** to stop the search */
	public void stopSearch(){
		solver.stopSearch();
	}

	/** true if current CSP as a next solution */
	public boolean next() {
		status.moreSolve();
		status.setCurrentTime();
		boolean next = solver.next();
		status.setElapsedTime();
		if (!next)
			status.moreFail();
		return next;
	}
	
	/** to know the current elapsed time */
	public long getElapsedTime() {
		return status.getElapsedTime();
	}
	
	/** to return the status */
	public SolverStatus getStatus() {
		return status;
	}

	public void addVar(Variable v) {
		vars.add(v);
	}
	
	public void addArrayVar(ArrayVariable v) {
		arrayVars.add(v);
	}
	
	/** 
	 * Adds a constraint to this CSP.
	 */
	public boolean addConstraint(LogicalExpression c) {
		return ctrs.add(c, visitor);
	}

	/** 
	 * Adds a constraint to this CSP.
	 */
	public boolean addConstraint(LogicalExpression c, String name) {
		return ctrs.add(c, visitor, name);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(name);
		s.append("\nConcrete solver is ");
		s.append(solver.toString());
		s.append("\n\nConstraints:\n");
		s.append(ctrs.toString());
		s.append("\n\nVariables:\n");
		s.append(vars.toString());
		return s.toString();
	}
	
	/** Saves current CSP. */
	public abstract void save();
	
	/** Restores current CSP. */
	public abstract void restore();
	
	public abstract Solution solution();
	
	public abstract DomainBox domainBox();
}
