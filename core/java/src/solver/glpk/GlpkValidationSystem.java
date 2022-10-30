package solver.glpk;

import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.variables.Variable;
import expression.variables.Variable.Use;

import solver.glpk.GlpkSolver;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.solution.ValidationStatus;
import validation.system.xml.SimpleArrayValidationSystem;
import validation.system.xml.ValidationCSP;
import validation.util.Type;

/**
 * This validation system manages a CSP on rationals using <code>GLPK</code>. 
 * 
 *  A limited form of arrays, where all indices must be statically known, is allowed.
 *  
 * @author Olivier Ponsini
 *
 */
public class GlpkValidationSystem extends SimpleArrayValidationSystem {
	
	/**
	 * CSP over rationals.
	 */
	protected ValidationCSP rationalCSP; 

	/**
	 * Constructs a validation system with a CSP on rationals for GLPK.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public GlpkValidationSystem(String name) {
		super(name + " using GLPK.");
	}
	
	/** 
	 * Creates the CSP on rationals.
	 * This method is called by the super class.
	 */
	@Override
	protected void setCSP() {
		status = new ValidationStatus();
		rationalCSP = new GlpkValidationCSP("GLPK (rational CSP)", this);
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
		return addNewVar(n, t, Use.LOCAL);
	}

	// add a new variable for the new SSA renaming of n
	@Override
	public Variable addVar(String n) {
		Variable v = var.addVar(n);
		rationalCSP.addVar(v);
		return v;
	}

	@Override
	// helen
	public void addVar(Variable v) {
		rationalCSP.addVar(v);
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
			rationalCSP.constraintBlock().add(validPC, rationalCSP.visitor);
		//We do not handle the non enumerable 'forall' that was put aside
		if (this.postcondForAll != null) {
			System.err.println("Error (addPostcond): GLPK does not handle non enumerable quantifiers!");
			System.exit(40);
		}
	}

	@Override
	public void addPrecond(LogicalExpression c) {
		if (c != null)
			rationalCSP.addConstraint(c);
		//We do not handle the non enumerable 'forall' that was put aside
		if (this.precondForAll != null) {
			System.err.println("Error (addPrecond): GLPK does not handle non enumerable quantifiers!");
			System.exit(40);
		}
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
	public boolean solve(Solution result) {
		if (VerboseLevel.TERSE.mayPrint()) {	
			System.out.println("Solving real CSP...");
			if (VerboseLevel.VERBOSE.mayPrint()) {	
				System.out.println(rationalCSP.toString());
			}
		}
		return solveCSP(result);
	}
	
	/**
	 * Calls the concrete solver to find a solution to the current constraint system on rationals.
	 * 
	 * @param result A recipient for the solution if one is found.
	 * 
	 * @return <code>true</code> if a solution was found; <code>false</code> otherwise.
	 */
	private boolean solveCSP(Solution result) {
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
	
	/**
	 * This method solves the current linear problem several times 
	 * changing the objective function so as to obtain the min and 
	 * max solution values for each variable of the problem.
	 */
	public void findVariablesExtrema() {
		GlpkSolver solver = (GlpkSolver)rationalCSP.cspSolver; 
		GlpkVarBlock vars = (GlpkVarBlock)rationalCSP.varBlock;
		int[] ind = new int[1];
		double[] val = new double[1];
		
		rationalCSP.startSearch();

		//Find min and max for each variable
		for (Variable var: vars) {
			int col = vars.concreteVar(var);
			ind[0] = col;
			val[0] = 1.0;
			
			//Find min
			solver.setObjective(0, true, 1, ind, val);
			if (solver.next()) {
				if (VerboseLevel.VERBOSE.mayPrint()) {	
					System.out.println("Min " + var.name() + "= " + solver.value(col));
				}
			}
			
			//Find max
			solver.setObjectiveDirection(false);
			if (solver.next()) {
				if (VerboseLevel.VERBOSE.mayPrint()) {	
					System.out.println(
						"Max " + var.name() + "= " + solver.value(col));
				}
			}
			
			//reset this objective coefficient
			val[0] = 0.0;
			solver.setObjective(0, true, 1, ind, val);			
		}
		
		rationalCSP.stopSearch();
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
	public boolean isFeasible() {
		return tryDecision(new LogicalLiteral(true));
	}

	@Override
	public boolean solve() {
		return solve(new Solution());
	}

}
