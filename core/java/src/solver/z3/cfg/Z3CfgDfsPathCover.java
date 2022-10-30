package solver.z3.cfg;

import ilog.concert.IloException;
import CFG.CFG;
import CFG.CFGVisitException;
import CFG.DFS.DFSVerifier;
import CFG.DFS.DFSException;
import java2CFGTranslator.CFGBuilder;
import java2CFGTranslator.Java2CFGException;
import expression.AbstractExpression;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.variables.Variable;
import validation.solution.Solution;
import validation.system.cfg.CfgCsp;
import validation.system.cfg.CfgSingleCspValidation;
import validation.Validation;
import validation.Validation.VerboseLevel;

/**
 * This validation system combines Z3 and a DFS traversal of the CFG to validate the 
 * first method defined in the analyzed program.
 * 
 * @author Olivier Ponsini
 *
 */
public class Z3CfgDfsPathCover extends CfgSingleCspValidation {
	
	private DFSVerifier dfsVisitor;
	
	public Z3CfgDfsPathCover(String name) {
		try {
			this.method = Validation.pgmMethod(new CFGBuilder(Validation.pgmFileName,Validation.pgmMethodName, Validation.maxUnfoldings,Validation.maxArrayLength).convert());
		} catch(Java2CFGException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
		if (this.method != null) {
			if (Validation.verboseLevel == VerboseLevel.DEBUG) {
				System.out.println("\nInitial CFG\n" + method);
			}
			this.dfsVisitor = new DFSVerifier(this);
		}
		else {
			System.err.println("Error: method " 
					+ Validation.pgmMethodName 
					+ " is not defined in " 
					+ Validation.pgmFileName 
					+ "!");
			System.exit(-1);
		}
	}
	
	@Override
	protected CfgCsp createCSP() {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Creating solver:");
		}
		return new Z3CfgCsp("Z3");		
	}

	@Override
	public void validate() throws IloException {
		for (Variable v: method.getUsefulVar()) {
			this.csp.addVar(v);
		}
		try {
			this.method.firstNode().accept(this.dfsVisitor);
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Program reachable paths are fully covered!");
			}
		} catch(DFSException e) {
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Program reachable paths are NOT fully covered!");
			}
			e.printStackTrace();
		} catch(CFGVisitException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println(this.printStatus());
		}
	}

	@Override
	public boolean tryDecision(LogicalExpression le) {
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Decision: " + le);
		}
		csp.addConstraint(le);
		csp.startSearch();
		boolean foundSolution = csp.next();
		csp.stopSearch();
		return foundSolution;
	}
	
	@Override
	public boolean checkAssertion(LogicalExpression le, String message) {
		//Assertions are not checked...
		return true;
	}
	
	@Override
	public boolean checkPostcond(LogicalExpression postcond) {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\n###########################");
			System.out.println("End of path #" + this.pathNumber() + " reached");
			System.out.println("Solving CSP...");
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(csp.toString());
			}
		}
		
		csp.startSearch();
		if (csp.next()) {
			Solution sol = csp.solution();
			csp.stopSearch();
			//Notifier à l'IHM la solution
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("\n---------\n");
				System.out.println("Test input values:");
			}
			Variable solVar;
			for (Variable v: method.parameters()) {
				solVar = sol.get(v.name());
				if (VerboseLevel.TERSE.mayPrint()) {
					System.out.println(solVar.domain().toString() + "\n");
				}
				//We add the input values that allow to reach the current branch to
				//check the postcond. This is just one test, no guarantee on the validity 
				//of the postcondition if not found false.
				addConstraint(
						new Assignment(solVar, 
						               AbstractExpression.createLiteral(solVar.domain())));				
			}
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println(sol.get(method.name() + "_0_Result_0").domain().toString() + "\n");
				System.out.println("\nResolution time : " + sol.getTime()/1000.0 + " s\n");
			}

			//We now check whether the postcondition is true or false for this test case.
			addConstraint(postcond);
			csp.startSearch();
			boolean checkedPC = csp.next();
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Postcondition is " + checkedPC);
			}
			csp.stopSearch();
			
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("\n---------\n");
			}
		}
		else { //Create empty solution
			System.err.println("Error: reached a path for which no input values could be found!");
			csp.stopSearch();
		}
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("###########################\n");
		}

		this.addPath();	
		
		//We want to explore all reachable paths
		return true;
	}
	
	@Override
	/**
	 * 
	 * @return true if the current csp has a solution
	 * 
	 * TODO: this function must only do an incomplete and fast
	 *       consistency check (e.g. a filtering)
	 * 
	 */
	public boolean isFeasible() {
		Boolean foundSolution;
		Solution sol;

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\n###########################");
			System.out.println("Checking if the current path is feasible ...");
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(csp.toString());
			}
		}
		
		csp.startSearch();
		foundSolution = csp.next();
		if (foundSolution) {
			sol = csp.solution();
			sol.setTime(csp.getElapsedTime());		
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Current path is feasible(" + csp.getElapsedTime() + " ms)");
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println(sol.toString());
				}
    		}
		}
		else { //Create empty solution
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Path has been cut (" + csp.getElapsedTime() + " ms)");
			}
			//Validation.println(Solution.noSolutionMessage(csp.getElapsedTime()));
		}
		
		csp.stopSearch();
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("###########################\n");
		}
		
		return foundSolution;
	}
	
	@Override
	/**
	 * @return true if the current csp has a solution
	 */
	public boolean solve() {
		Boolean foundSolution;
		Solution sol;

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\n###########################");
			System.out.println("End of path "+ pathNumber++);
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(csp.toString());
			}
		}
		
		csp.startSearch();
		foundSolution = csp.next();
		if (foundSolution) {
			sol = csp.solution();
			sol.setTime(csp.getElapsedTime());		
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("A counterexample has been found (" + csp.getElapsedTime() + " ms)");
				System.out.println(sol.toString());
			}
		}
		else { 
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Program is correct on this path (" + csp.getElapsedTime() + " ms)");
			}
		}
		
		csp.stopSearch();
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("###########################\n");
		}
		
		return foundSolution;
	}
}
