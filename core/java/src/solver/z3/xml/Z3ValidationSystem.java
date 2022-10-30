package solver.z3.xml;

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
 * This validation system manages a linear CSP on reals using z3. 
 *  
 * @author Olivier Ponsini
 *
 */
public class Z3ValidationSystem extends ValidationSystem {
	
	/**
	 * CSP over rationals.
	 */
	protected ValidationCSP rationalCSP; 

	/**
	 * Constructs a validation system with a linear CSP on reals for z3.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public Z3ValidationSystem(String name) {
		super(name + " using z3.");
	}
	
	/** 
	 * Creates the CSP on reals.
	 * This method is called by the super class.
	 */
	@Override
	protected void setCSP() {
		status = new ValidationStatus();
		rationalCSP = new Z3Csp("Z3 (rational CSP)", this);
		status.addStatus(rationalCSP.getStatus());
	}
	
	@Override
	public Variable addNewVar(String n, Type t, Use u) {
		Variable v = var.addNewVar(n, t, u);
		rationalCSP.addVar(v);
		return v;
	}

	@Override
	public Variable addNewVar(String n, Type t) {
		//Information about being an input variable is not used
		return addNewVar(n, t, Use.LOCAL);
	}

	// add a new variable for the new SSA renaming of n
	@Override
	public Variable addVar(String n) {
		Variable v = var.addVar(n);
		rationalCSP.addVar(v);
		return v;
	}
	
	// helen
	@Override
	public void addVar(Variable v) {
		rationalCSP.addVar(v);
	}

	@Override
	public ArrayVariable addArrayVar(String n) {
		ArrayVariable v = (ArrayVariable)var.addVar(n);
		rationalCSP.addArrayVar(v);
		return v;
	}

	@Override
	public ArrayVariable addNewArrayVar(String n, int l, Type t) {
		return addNewArrayVar(n, l, t, Use.LOCAL);
	}

	@Override
	public ArrayVariable addNewArrayVar(String n, int l, Type t, Use u) {
		//Variable Use information is lost
		ArrayVariable v = var.addNewArrayVar(n, t,u,l);
		rationalCSP.addArrayVar(v);
		return v;
	}

	@Override
	public void addConstraint(LogicalExpression c) {
		rationalCSP.addConstraint(c);
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
			rationalCSP.addConstraint(validPC);
		//We also add the non enumerable 'forall' that was put aside because z3 can directly 
		//handle it
		if (this.postcondForAll != null)
			rationalCSP.addConstraint(this.postcondForAll);
	}

	@Override
	public void addPrecond(LogicalExpression c) {
		if (c != null)
			rationalCSP.addConstraint(c);
		//We also add the non enumerable 'forall' that was put aside because z3 can directly 
		//handle it
		if (this.precondForAll != null)
			rationalCSP.addConstraint(this.precondForAll);
	}

	@Override
	public boolean tryDecision(LogicalExpression c) {
		rationalCSP.save();
		rationalCSP.addConstraint(c);
		
		rationalCSP.startSearch();

		boolean hasSolution = rationalCSP.next();

		rationalCSP.stopSearch();
		rationalCSP.restore();
		return hasSolution;
	}

	@Override
	protected void saveCSP() {
		rationalCSP.save();
	}

	@Override
	protected void restoreCSP() {
		rationalCSP.restore();
	}

	@Override
	public boolean solve(Solution result) {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Solving rational CSP...");
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(rationalCSP.toString());
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
		
		rationalCSP.startSearch();
		foundSolution = rationalCSP.next();
		if (foundSolution) {
			result.copy(rationalCSP.solution());
		}
		rationalCSP.stopSearch();
		result.setTime(rationalCSP.getElapsedTime());
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
