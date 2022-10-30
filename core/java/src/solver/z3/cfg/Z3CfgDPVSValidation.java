package solver.z3.cfg;

import ilog.concert.IloException;
import java2CFGTranslator.CFGBuilder;
import java2CFGTranslator.Java2CFGException;

import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.system.cfg.CfgSingleCspValidation;
import CFG.CFG;
import CFG.SetOfCFG;
import CFG.DPVS.DPVSVerifier;
import CFG.simplification.ConstantPropagator;
import CFG.simplification.Simplifier;
import expression.logical.LogicalExpression;
import expression.logical.NotExpression;
import expression.variables.ArrayVariable;
import expression.variables.Variable;

/**
 * This validation system combines Z3 and DPVS heuristic to validate a method defined in the analyzed program.
 * 
 * @author Olivier Ponsini
 *
 */
public class Z3CfgDPVSValidation extends CfgSingleCspValidation {

	// the verifier
	private DPVSVerifier verif;
	
	/**
	 * Stores the latest solution found when checking the post-condition (which embeds all the user assertions too).
	 */
	private Solution counterexample;
	
	public Z3CfgDPVSValidation(String name) throws IloException {
		if (buildCFG()) {
			simplifyCFG();
			verif = new DPVSVerifier(this, program, method.name());
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
	protected Z3CfgCsp createCSP() {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Creating solver(s):");
		}
		return new Z3CfgCsp("Z3 (rational CSP)");		
	}

	@Override
	public void validate() {
		for (Variable v: method.getUsefulVar()) {
			if (v instanceof ArrayVariable)
				this.csp.addArrayVar((ArrayVariable)v);
			else
				this.csp.addVar(v);
		}
		
		try {
			if (verif.validate()) {
				if (VerboseLevel.TERSE.mayPrint()) {
					System.out.println("\n###########################");
				}
				System.out.println("Program is conform with its specification!");
			}
			else {
				if (VerboseLevel.TERSE.mayPrint()) {
					System.out.println("\n###########################");
				}
				System.out.println("Program is NOT conform with some specification case!");
				// Should the counterexample be written to a file?
				if (Validation.counterExampleFileName != null) {
					this.counterexample.writeToFile(Validation.counterExampleFileName);
				}
			}
		} catch (IloException e) {
			// TODO Auto-generated catch block
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
	
	/* (non-Javadoc)
	 * @see validation.system.ValidationSystemCallbacks#checkAssertion(expression.logical.LogicalExpression, java.lang.String)
	 * 
	 *  After the call to DPVSInformationVisitor, which builds the data structures needed by DPVS, 
	 *  the CFG does not contain assertion nodes anymore. They have been replaced by assertion to boolean 
	 *  variables and merged with the post-condition (@see DPVSInformationVisitor#visit(AssertNode assertNode)).
	 *  So this method should never be called!
	 */
	@Override
	public boolean checkAssertion(LogicalExpression le, String message) {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Asserts " + message + ": " + le);
		}
		csp.addConstraint(new NotExpression(le));
		csp.startSearch();
		boolean foundSolution = csp.next();
	
		if (VerboseLevel.TERSE.mayPrint()) {
			if (foundSolution) {
				System.out.println("Assertion is violated!");
				System.out.println(csp.solution().toString());
			}
			else {
				System.out.println("Assertion is verified!");
			}
			System.out.println("************************\n\n");
		}

		csp.stopSearch();

		return !foundSolution;
	}
	
	/* (non-Javadoc)
	 * @see validation.system.ValidationSystemCallbacks#checkPostcond(expression.logical.LogicalExpression)
	 *
	 * This method should never be called with DPVS heuristics (@see DPVSVerifier).
	 */
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
			//Notifier à l'IHM la solution
			if (VerboseLevel.TERSE.mayPrint()) {
				Solution sol = csp.solution();
				sol.setTime(csp.getElapsedTime());		
				System.out.println(sol.toString());
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

		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("\n###########################");
			System.out.println("Checking if the current path is feasible ...");
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println(csp.toString());
			}
		}	
		
		csp.startSearch();
		foundSolution = csp.next();
		long time = csp.getElapsedTime();
		if (foundSolution) {
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println("Current path is feasible (" + time + " ms)");
    			if (VerboseLevel.DEBUG.mayPrint()) {  
    				Solution sol = csp.solution();
    				sol.setTime(time);		
    				System.out.println(sol.toString());
    			}
    		}
		}
		else { 
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println("Path " + pathNumber + " has been cut or is correct (" + time + " ms)");
    		}
		}
		
		csp.stopSearch();
		if (VerboseLevel.VERBOSE.mayPrint()) {
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
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println(csp.toString());
			}
		}
		
		csp.startSearch();
		foundSolution = csp.next();
		long time = csp.getElapsedTime();
		this.counterexample = csp.solution();
		this.counterexample.setTime(time);
		if (foundSolution) {
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("A counterexample has been found (" + time + " ms)");		
				System.out.println(this.counterexample.toString());
				System.out.println("###########################\n");
			}
		}
		else { 
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Program is correct on path " + (pathNumber-1) + " (" + csp.getElapsedTime() + " ms)");
				System.out.println("###########################\n");
			}
		}
		
		csp.stopSearch();
		
		return foundSolution;
	}
	
}

