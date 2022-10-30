package solver.z3.cfg;

import ilog.concert.IloException;
import CFG.CFG;
import CFG.CFGVisitException;
import CFG.SetOfCFG;
import CFG.DFS.DFSVerifier;
import CFG.DFS.DFSException;
import CFG.simplification.ConstantPropagator;
import CFG.simplification.Simplifier;
import java2CFGTranslator.CFGBuilder;
import java2CFGTranslator.Java2CFGException;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.logical.NotExpression;
import expression.variables.ArrayVariable;
import expression.variables.Variable;
import solver.z3.xml.Z3ValidationSystem;
import validation.solution.Solution;
import validation.system.cfg.CfgCsp;
import validation.system.cfg.CfgSingleCspValidation;
import validation.Validation;
import validation.Validation.VerboseLevel;

/**
 * This validation system combines Z3 and a DFS traversal of the CFG to validate the 
 * first method defined in the analyzed program.
 * 
 * It is based on the (heavy) {@link Z3ValidationSystem} that inherited all the features of 
 * the first version of CPBPV which was XML based.
 * 
 * @author Olivier Ponsini
 *
 */
public class Z3CfgDfsValidation extends CfgSingleCspValidation {

	private DFSVerifier dfsVisitor;
	
	/**
	 * Stores the latest solution found when checking an assertion or the post-condition.
	 */
	private Solution counterexample;
	
	public Z3CfgDfsValidation(String name) throws IloException {
		if (buildCFG()) {
			simplifyCFG();
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
			System.out.println("Creating solver(s):");
		}
		return new Z3CfgCsp("Z3 (rational CSP)");		
	}

	@Override
	public void validate() throws IloException {
//		System.out.println("dans Z3CfgDfsValidation " + method.getUsefulVar());
		for (Variable v: method.getUsefulVar()) {
			if (v instanceof ArrayVariable)
				this.csp.addArrayVar((ArrayVariable)v);
			else
				this.csp.addVar(v);
		}
		try {
			// add constraints of the static fields declaration
			// they exist
			// NB : the block can be null if the field declaration node has been simplified
			if (program.hasFieldDeclaration()) {
				for (Assignment a: program.getFieldDeclaration().getBlock()) {
					addConstraint(a);
				}
			}
			// visit the method and validate it
			this.method.firstNode().accept(this.dfsVisitor);

			System.out.println("...............................");
			System.out.println("Program is conform with its specification!");
		} catch(DFSException e) {
			System.out.println(e.getMessage());
			// e.abort is set for unwinding assertions.
			if (!e.abort) {
				System.out.println("Program is NOT conform with some specification case!");
				// Should the counterexample be written to a file?
				if (Validation.counterExampleFileName != null) {
					this.counterexample.writeToFile(Validation.counterExampleFileName);
				}
			}
			else {
				if (VerboseLevel.TERSE.mayPrint()) {
					System.out.println(this.printStatus());
				}
				// This means an ABORT in regression tests. Maybe we could handle unwinding assertions as
				// a new kind of test outcome, e.g. UKNOWN...
				System.exit(-1);
			}
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
		
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println("CSP:\n" + csp.toString());
		}
		
		csp.startSearch();
		boolean foundSolution = csp.next();
		csp.stopSearch();
		return foundSolution;
	}
	
	@Override
	public boolean checkAssertion(LogicalExpression le, String message) {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Asserts " + message + ": " + le);
		}

		csp.addConstraint(new NotExpression(le));
		
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println("CSP:\n" + csp.toString());
		}

		csp.startSearch();
		boolean foundSolution = csp.next();
		if (foundSolution) {
			// Store the counterexample found and notify it to the user interface
			this.counterexample = csp.solution();
			this.counterexample.setTime(csp.getElapsedTime());		
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Assertion is violated!");
				System.out.println(this.counterexample.toString());
				System.out.println("************************\n\n");
			}
		}
		else {
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Assertion is verified!");
				System.out.println("************************\n\n");
			}
		}
		
		csp.stopSearch();
		return !foundSolution;
	}
	
	@Override
	public boolean checkPostcond(LogicalExpression postcond) {
		Boolean foundSolution;

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\n###########################");
			System.out.println("End of path #" + this.pathNumber() + " reached");
		}

		csp.addConstraint(new NotExpression(postcond));

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Solving rational CSP...");
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(csp.toString());
			}
		}
		
		csp.startSearch();
		foundSolution = csp.next();
		if (foundSolution) {
			// Store the counterexample found and notify it to the user interface
			this.counterexample = csp.solution();
			this.counterexample.setTime(csp.getElapsedTime());		
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println(this.counterexample.toString());
			}
		}
		else { //Create empty solution
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println(Solution.noSolutionMessage(csp.getElapsedTime()));
			}
		}
		
		csp.stopSearch();
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("###########################\n");
		}

		this.addPath();	
		
		return !foundSolution;
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
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Current path is feasible(" + csp.getElapsedTime() + " ms)");
				if (VerboseLevel.VERBOSE.mayPrint()) {
					Solution sol = csp.solution();
					sol.setTime(csp.getElapsedTime());		
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
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("A counterexample has been found (" + csp.getElapsedTime() + " ms)");
				Solution sol = csp.solution();
				sol.setTime(csp.getElapsedTime());		
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

