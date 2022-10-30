package solver.realpaver.xml;

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
 * This validation system manages a CSP on reals using <pre>Real Paver</pre>.  
 *  
 * @author Olivier Ponsini
 *
 */
public class RealPaverXmlValidation extends SimpleArrayValidationSystem {
	
	/**
	 * The CSP.
	 */
	protected RealPaverXmlCsp csp; 

	/**
	 * Constructs a validation system with a CSP on reals.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public RealPaverXmlValidation(String name) {
		super(name + " using Real Paver.");
	}
	
	/** 
	 * Creates the CSP on floats.
	 * This method is called by the super class.
	 */
	@Override
	protected void setCSP() {
		status = new ValidationStatus();
		csp = new RealPaverXmlCsp("Real Paver", this);
		status.addStatus(csp.getStatus());
	}
	
	@Override
	public Variable addNewVar(String n, Type t, Use u) {
		Variable v = var.addNewVar(n, t, u);
		csp.addVar(v);
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
		csp.addVar(v);
		return v;
	}

	// helen
	@Override
	public void addVar(Variable v) {
		csp.addVar(v);
	}
	
	@Override
	public void addConstraint(LogicalExpression c) {
		csp.addConstraint(c);
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
			csp.addConstraint(validPC);
		//We do not handle the non enumerable 'forall' that was put aside
		if (this.postcondForAll != null) {
			System.err.println("Error (addPostcond): Real Paver does not handle non enumerable quantifiers!");
			System.exit(40);
		}
	}

	@Override
	public void addPrecond(LogicalExpression c) {
		if (c != null)
			csp.addConstraint(c);
		//We do not handle the non enumerable 'forall' that was put aside
		if (this.precondForAll != null) {
			System.err.println("Error (addPrecond): Real Paver does not handle non enumerable quantifiers!");
			System.exit(40);
		}
	}

	@Override
	public boolean tryDecision(LogicalExpression c) {
		csp.save();
		csp.addConstraint(c);
		
		if (Validation.verboseLevel == VerboseLevel.DEBUG) {
			System.out.println(csp.toString());
		}
		
		csp.startSearch();
		
		boolean hasSolution = csp.next();
		
		csp.stopSearch();
		csp.restore();
		
		return hasSolution;
	}

	@Override
	public boolean solve(Solution result) {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Solving CSP...");
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(csp.toString());
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
		
		csp.startSearch();
		foundSolution = csp.next();
		if (foundSolution) {
			result.copy(csp.solution());
		}
		csp.stopSearch();
		result.setTime(csp.getElapsedTime());
		return foundSolution;
	}

	@Override
	protected void saveCSP() {
		csp.save();
	}

	@Override
	protected void restoreCSP() {
		csp.restore();
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
