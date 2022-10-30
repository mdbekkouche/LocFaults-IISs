package validation.strategies.xml;

import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import expression.AbstractExpression;
import expression.Expression;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.variables.Variable;

/**
 * This strategy provides structural test coverage of all executable i-path in the 
 * XML analyzed program.
 * 
 *  All solvers having an XML validation system associated can be used.
 *  
 * @author Olivier Ponsini
 *
 */
public class CoverageStrategy extends GenericStrategy {

	public CoverageStrategy(String vsName) {
		super(vsName);
	}
	
	private boolean checkInstantiatedPostcond(Solution sol) {
		//We add all the variable values that allow to reach the current branch to
		//check the postcond. This is just one test, no guarantee on the validity of 
		//the postcondition if not found false.
		//TODO: Adding the input values would be enough...
		for(Variable solVar : sol) {
			addConstraint(new Assignment(solVar, 
										 AbstractExpression.createLiteral(solVar.domain())));
		}	
		addConstraint(vs.getPostcond());	
		return solve(new Solution());
	}
	
	/* 
	 * The given LogicalExpression is not the postcondition but the assignment of 
	 * the return expression to the JMLResult variable.
	 * 
	 * @see validation.strategies.xml.GenericStrategy#checkPostcond(expression.logical.LogicalExpression)
	 */
	@Override
	public boolean checkPostcond(LogicalExpression returnAssignment) {
		boolean foundSolution, checkedPC;
		
		addConstraint(returnAssignment);
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\n###########################");
			System.out.println("Return statement reached for path " + casePathNumber());
		}
	
		//Get a reference on the lastPostcondSolution attribute. 
		//Initially the attribute is empty, we are the only one to update it and we 
		//leave it empty, so we can always assume it is empty here.
		Solution lastPCSolutionRef = vs.getLastPostcondSolution();
		foundSolution = solve(lastPCSolutionRef);
		if (!foundSolution) {
			System.err.println("Error: reached a path for which no input values could be found!");
			checkedPC = false; 
		}
		else {
			Solution sol = new Solution();
			//We filter out variables that are neither parameters nor the return value
			for (Variable v: lastPCSolutionRef) {
				if (v.isParameter()) {
					sol.add(v);
				}
			}
			sol.add(lastPCSolutionRef.get("JMLResult_0"));
			sol.setTime(lastPCSolutionRef.getTime());
			//Notifier à l'IHM la solution result
			displaySolution(sol);
			checkedPC = checkInstantiatedPostcond(sol);
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Postcondition is " + checkedPC + "!");
			}
		}
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("###########################\n");
		}

		//We now need to empty the vs.lastPostcondSolution attribute so that path 
		//exploration continues.
		lastPCSolutionRef.reset();
		
		return checkedPC;
	}
	
	/* 
	 * Assertions are ignored during path coverage.
	 * 
	 * @see validation.strategies.xml.GenericStrategy#checkAssertion(expression.logical.LogicalExpression, java.lang.String)
	 */
	@Override
    public boolean checkAssertion(LogicalExpression le, String message) {
    	return true;
      }
      
	/* 
	 * Post-condition is ignored for path coverage, but we add the assignment of
	 * the return expression to the JMLResult variable.
	 * 
	 * @see validation.strategies.xml.GenericStrategy#postPostCond(expression.variables.Variable, expression.Expression)
	 */
	@Override
	public LogicalExpression updatePostcond(Variable v, Expression expr) {
		return new Assignment(v, expr);
	}
	
	/* 
	 * Post-condition is ignored for path coverage.
	 * 
	 * @see validation.strategies.xml.GenericStrategy#updatePostCond()
	 */
	@Override
	public LogicalExpression updatePostcond() {
		return new LogicalLiteral(true);
	}

	@Override
	public void displaySolution(Solution sol) {
		//Notifier à l'IHM la solution
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\n---------\n");
			System.out.println("Test input values:");
			for (Variable v: sol) {
				System.out.println(sol.get(v.name()).domain().toString() + "\n");
			}
			System.out.println("\nResolution time : " + sol.getTime()/1000.0 + " s\n");
			System.out.println("\n---------\n");
		}
	}
	
}
