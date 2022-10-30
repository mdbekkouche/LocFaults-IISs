package solver.fplib;

import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.variables.Variable;
import expression.variables.Variable.Use;

import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.solution.ValidationStatus;
import validation.system.xml.SimpleArrayValidationSystem;
import validation.util.Type;

/**
 * This validation system manages a CSP on floats using <code>fplib</code>.  
 *  
 * @author Olivier Ponsini
 *
 */
public class FplibValidationSystem extends SimpleArrayValidationSystem {
	
	/**
	 * The CSP over floats (specification part).
	 */
	protected FplibValidationCSP floatCSP; 

	/**
	 * Constructs a validation system with a CSP on floats for fplib.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public FplibValidationSystem(String name) {
		super(name + " using fplib.");
	}
	
	/** 
	 * Creates the CSP on floats.
	 * This method is called by the super class.
	 */
	@Override
	protected void setCSP() {
		status = new ValidationStatus();
		floatCSP = new FplibValidationCSP("FPLIB (float CSP)", this);
		status.addStatus(floatCSP.getStatus());
	}
	
	@Override
	public Variable addNewVar(String n, Type t, Use u) {
		Variable v = var.addNewVar(n, t, u);
		floatCSP.addVar(v);
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
		floatCSP.addVar(v);
		return v;
	}

	// helen
	@Override
	public void addVar(Variable v) {
		floatCSP.addVar(v);
	}
	
	@Override
	public void addConstraint(LogicalExpression c) {
		floatCSP.addConstraint(c);
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
		// Parsing the postcondition may return an empty constraint (e.g. an non enumerable
		// quantifier)
		if (validPC != null)
			floatCSP.addConstraint(validPC);
		//We do not handle the non enumerable 'forall' that was put aside
		if (this.postcondForAll != null) {
			System.err.println("Error (addPostcond): fplib does not handle non enumerable quantifiers!");
			System.exit(40);
		}
	}

	@Override
	public void addPrecond(LogicalExpression c) {
		if (c != null)
			floatCSP.addConstraint(c);
		//We do not handle the non enumerable 'forall' that was put aside
		if (this.precondForAll != null) {
			System.err.println("Error (addPrecond): fplib does not handle non enumerable quantifiers!");
			System.exit(40);
		}
	}

	@Override
	public boolean tryDecision(LogicalExpression c) {
		floatCSP.save();
		floatCSP.addConstraint(c);
		
		if (Validation.verboseLevel == VerboseLevel.DEBUG) {
			System.out.println(floatCSP.toString());
		}
		
		floatCSP.startSearch();
		
		boolean hasSolution = floatCSP.next();
		
		floatCSP.stopSearch();
		floatCSP.restore();
		
		return hasSolution;
	}

	@Override
	public boolean solve(Solution result) {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Solving float CSP...");
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(floatCSP.toString());
			}
		}
		return solveCSP(result);				
	}
	
	/**
	 * Calls the concrete solver to find a solution to the current constraint system on floats.
	 * 
	 * @param result A recipient for the solution if one is found.
	 * 
	 * @return <code>true</code> if a solution was found; <code>false</code> otherwise.
	 */
	private boolean solveCSP(Solution result) {
		boolean foundSolution;
		
		floatCSP.startSearch();
		foundSolution = floatCSP.next();
		if (foundSolution) {
			result.copy(floatCSP.solution());
		}
		floatCSP.stopSearch();
		result.setTime(floatCSP.getElapsedTime());
		return foundSolution;
	}

	@Override
	protected void saveCSP() {
		floatCSP.save();
	}

	@Override
	protected void restoreCSP() {
		floatCSP.restore();
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
