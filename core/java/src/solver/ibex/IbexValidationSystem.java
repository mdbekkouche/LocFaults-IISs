package solver.ibex;

import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.variables.ArrayVariable;
import expression.variables.Variable;
import expression.variables.Variable.Use;

import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.solution.ValidationStatus;
import validation.system.xml.ValidationCSP;
import validation.system.xml.ValidationSystem;
import validation.util.Type;

/**
 * This validation system manages a CSP on reals using IBEX. 
 * Integers are not yet fully supported: they can only be used as constant values 
 * and are solved in the CSP on reals being interpreted as singleton intervals 
 * 
 * TODO: Preliminary work only: the solutions found by IBEX have still to be taken into account...
 *   
 * @author Olivier Ponsini
 *
 */
public class IbexValidationSystem extends ValidationSystem {
	
	/**
	 * Constraints on reals.
	 */
	protected ValidationCSP realCSP; 

	/**
	 * Constructs a validation system with a CSP on reals for IBEX.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public IbexValidationSystem(String name) {
		super(name + " using IBEX.");
	}
	
	/** 
	 * Creates the CSP on reals.
	 * This method is called by the super class.
	 */
	@Override
	protected void setCSP() {
		status = new ValidationStatus();
		realCSP = new IbexRealValidationCSP("IBEX (real CSP)", this);
		status.addStatus(realCSP.getStatus());
	}
	
	@Override
	public Variable addNewVar(String n, Type t, Use u) {
		Variable v = var.addNewVar(n, t, u);
		realCSP.addVar(v);
		return v;
	}

	@Override
	public Variable addNewVar(String n, Type t) {
		return addNewVar(n, t, Use.LOCAL);
	}

	// add a new variable for the new SSA renaming of n
	@Override
	public Variable addVar(String n) {
		Variable v = var.addVar(n);
		realCSP.addVar(v);
		return v;
	}
	
	@Override
	// helen
	public void addVar(Variable v) {
		realCSP.addVar(v);
	}

	@Override
	public void addConstraint(LogicalExpression c) {
		realCSP.addConstraint(c);
	}

	/**
	 * Adds the given negated post-condition expression to this validation system constraints. 
	 * A new constraint is built from the expression. For this, we separate the "for all"
	 * quantified expressions from the rest of the post-condition.
	 * 
	 * @param pc The post-condition expression.
	 */
	@Override
	public void addPostcond(LogicalExpression pc) {
		LogicalExpression validPC = parseNegatedClausePostcond(pc);
		realCSP.addConstraint(validPC);
	}

	@Override
	public void addPrecond(LogicalExpression c) {
		if (c != null)
			realCSP.addConstraint(c);
	}

	@Override
	public boolean tryDecision(LogicalExpression c) {
		return true;
	}

//	@Override
//	public SolvingCode tryAndAddDecision(Constraint co) {
//		floatCSP.addConstraint(co);
//		return SolvingCode.NOT_SOLVED;
//	}

	@Override
	protected void saveCSP() {
		realCSP.save();
	}

	@Override
	protected void restoreCSP() {
		realCSP.restore();
	}

	@Override
	public ArrayVariable addArrayVar(String n) {
		System.err.println("Error (addArrayVar): arrays are not yet implemented with IBEX paver!");
		System.exit(70);
		return null;
	}

	@Override
	public ArrayVariable addNewArrayVar(String n, int l, Type t) {
		System.err.println("Error (addNewArrayVar): arrays are not yet implemented with IBEX paver!");
		System.exit(71);
		return null;
	}

	@Override
	public ArrayVariable addNewArrayVar(String n, int l, Type t, Use u) {
		System.err.println("Error (addNewArrayVar): arrays are not yet implemented with IBEX paver!");
		System.exit(71);
		return null;
	}

	@Override
	public boolean solve(Solution result) {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Solving real CSP...");
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(realCSP.toString());
			}
		}
		return solveRealCSP(result);				
	}
	
	/**
	 * Calls the concrete solver to find a solution to the current constraint system on reals.
	 * 
	 * @param result A recipient for the solution if one is found.
	 * 
	 * @return <code>true</code> if a solution was found; <code>false</code> otherwise.
	 */
	private boolean solveRealCSP(Solution result) {
		boolean foundSolution;
		
		realCSP.startSearch();
		foundSolution = realCSP.next();
		if (foundSolution) {
			result.copy(realCSP.solution());
		}
		realCSP.stopSearch();
		result.setTime(realCSP.getElapsedTime());
		return foundSolution;
	}

	@Override
	public boolean isFeasible() {
		return tryDecision(new LogicalLiteral(true));
	}

	@Override
	public boolean solve() {
		return solve(new Solution());
	}

	
}
