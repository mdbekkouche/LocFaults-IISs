package validation.system.xml;


import expression.ExpressionVisitor;
import expression.logical.LogicalExpression;
import expression.variables.ArrayVariable;
import expression.variables.DomainBox;
import expression.variables.Variable;

import solver.ConcreteSolver;
import solver.Solver;
import solver.Solver.SolverEnum;
import validation.Validation;
import validation.solution.Solution;
import validation.solution.SolverStatus;
import validation.system.ArrayVariableStore;
import validation.system.ConstraintStore;
import validation.system.VariableStore;

/** 
 * A CSP for validation. 
 * 
 * Takes as inputs ad hoc types Constraint, Expression, Variable
 * used to parse the xml intermediate form.
 * Define operations to manage saving and solving CSP.
 * 
 * Store this information in a concrete solver.
 * 
 * To create a concrete Validation CSP, derive class
 * {@link RealValidationSystem}, {@link IntegerValidationCSP}...
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 */
public abstract class ValidationCSP {
    
	/** Link to the ValidationSystem owning this ValidationCSP. */
	public ValidationSystem validationSystem;	
	/** The CSP's solver. */
	public ConcreteSolver cspSolver;
	/** The solver's status. */
	protected SolverStatus status;
	/** The CSP's name */
	protected String name;
	/** The constraints handled by this CSP of the currently explored path. */
	public ConstraintStore constr;
	/** The CSP's variables. */
	public VariableStore varBlock;
	/** The CSP's array variables. */
	public ArrayVariableStore arrayVarBlock;
	/**
	 * The visitor to visit this CSP constraint expressions.
	 * It does not have to be created for each added constraint and can be stored here.
	 */
	public ExpressionVisitor visitor;
	
	/** 
	 * A validation CSP with a name, a solver, and a validationSystem. 
	 */ 
	public ValidationCSP(String n, SolverEnum solverType, ValidationSystem vs) {
		validationSystem = vs;
		name = n;
		status = new SolverStatus(n);
		cspSolver = Solver.createSolver(solverType);
		initCSP();
	}
			
	/** 
	 * This method must be used by derived classes to initialize all this class' members. 
	 */
	protected abstract void initCSP() ;

	/** 
	 * Returns a solution of this CSP
	 * PRECOND : search has been done
	 */
	public abstract Solution solution();

	public abstract DomainBox domainBox();
	
	public ConstraintStore constraintBlock() {
		return constr;
	}
	
	/** to enable a search process */
	public void startSearch() {
		cspSolver.startSearch();
	}
	
	/** to stop the search */
	public void stopSearch(){
		cspSolver.stopSearch();
	}

	/** true if current CSP as a next solution */
	public boolean next() {
		status.moreSolve();
		status.setCurrentTime();
		boolean next = cspSolver.next();
		status.setElapsedTime();
		if (!next)
			status.moreFail();
		return next;
	}
	
	/** to know the current elapsed time */
	public long getElapsedTime() {
		return status.getElapsedTime();
	}
	
	/** to know the current  time */
	public double getTime() {
		return status.getTime();
	}

	/** 
	 * adds a variable to the variable block of this CSP.
	 */
	public void addVar(Variable v) {
		varBlock.add(v);
	}
	
	/**
	 * Adds an array variable to the array variable block of this CSP.
	 * @param array TODO
	 */	
	public  void addArrayVar(ArrayVariable array) {
		if (arrayVarBlock != null)
			arrayVarBlock.add(array);
		else
			System.err.println("Error: arrays does not seem to be supported with this solver!");
	}

	/** to return the status */
	public SolverStatus getStatus() {
		return status;
	}

	/** 
	 * Adds a constraint to this CSP.
	 */
	public boolean addConstraint(LogicalExpression c) {
		return constr.add(c, this.visitor);
	}

	// methods to manage backtrak
	
	/** Saves current CSP. */
	public void save() {
		constr.save();
		varBlock.save();
		if (arrayVarBlock!=null)
			arrayVarBlock.save();
	}
	
	/** Restores current CSP. */
	public void restore() {
		constr.restore();
		varBlock.restore();
		if (arrayVarBlock!=null)
			arrayVarBlock.restore();
	}

	public String toString() {
		switch (Validation.verboseLevel) {
		case DEBUG:
		case VERBOSE:
			return name + "\nConcrete solver is " 
					+ cspSolver.toString() + "\n"
					+ "\nConstraints:\n" + constr.toString()
					+ "\nVariables:\n" + varBlock.toString();
		case TERSE:
			return name + "\nConcrete solver is "
					+ cspSolver.toString() + "\n";
		default:
			return "";
		}
	}

}
