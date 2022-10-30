package solver.fplib;

import java.util.Stack;

import org.w3c.dom.Node;

import exception.AnalyzeException;

import expression.Expression;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.variables.Variable;
import expression.variables.VariableDomain;

import validation.Validation;
import validation.Validation.VerboseLevel;

import validation.solution.Solution;

import validation.util.ChildIterator;


import validation.visitor.JavaBlockVisitAndValidate;

/**
 * This validation system manages a CSP on floats using <code>fplib</code>.  
 *  
 * @author Olivier Ponsini
 *
 */
public class FplibShaveResValidationSystem extends FplibValidationSystem {
		
	/**
	 * Flags the method to use for shaving.
	 * True means Fplib's 3B consistency; False means our own shaving strategy, based on 
	 * Fplib's 2B consistency.
	 */
	private boolean kb;
	/**
	 * Domain storing the union of all computed domains with the shaving method 
	 * based on the 3B consistency.
	 */
	private VariableDomain res;

	/**
	 * Constructs a system with a CSP on floats for fplib.
	 * This system will try to shave the domain of the method return value.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public FplibShaveResValidationSystem(String name) {
		super(name);
	}

	/**
	 * Replaces adding the postcondition by adding a constraint 
	 * <pre>JMLResult = method return expression</pre>. 
	 * 
	 * @param jmlResult The JMLResult variable.
	 * @param returnExpr The method return expression.
	 */
	public LogicalExpression updatePostcond(Variable jmlResult, Expression returnExpr) {
		return new Assignment(jmlResult, returnExpr);	
	}

	/* 
	 * Assertions are ignored in shaving mode.
	 */
	@Override
	public boolean checkAssertion(LogicalExpression assertion, String message) {
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Warning: Assertion is ignored in shaving mode!");
		}
		return true;
	}

	//TODO: find best tradeoff between time/paths cut tuning kB parameters or only using 2B filtering
	//TODO: Domains filtered could be used in the remaining analysis instead of being discarded here and recomputed later...
	//      This would imply saving/restoring domains at each tryDecision. Currently only the variables are saved, not their domains...
	@Override
	public boolean tryDecision(LogicalExpression c) {
		Boolean foundSolution = true;

		floatCSP.save();
		floatCSP.addConstraint(c);
		
		if (VerboseLevel.VERBOSE.mayPrint()) {
			floatCSP.constraintBlock().print();
			floatCSP.varBlock.print();
		}
		
		//floatCSP.setDomains(fluctuatDomains);
		floatCSP.startSearch();
		//floatCSP.setFplibDomains(fluctuatDomains);
		foundSolution = ((FplibSolver)floatCSP.cspSolver).kB(3, 0.5, 0.5);
		//foundSolution = ((FplibSolver)floatCSP.cspSolver).twoB(0.5);
		floatCSP.stopSearch();

		floatCSP.restore();
		
		return foundSolution;
	}

	/* 
	 * The given LogicalExpression is not the postcondition but the assignment of 
	 * the return expression to the JMLResult variable.
	 * 
	 * @see validation.strategies.xml.GenericStrategy#checkPostcond(expression.logical.LogicalExpression)
	 */
	@Override
	public boolean checkPostcond(LogicalExpression returnAssignment) {
		boolean foundSolution;
		
		floatCSP.addConstraint(returnAssignment);
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\n###########################");
			System.out.println("Return statement reached for path " + casePathNumber());
		}
		
		//Get a reference on the lastPostcondSolution attribute, empty it and update it
		//through solve().
		Solution lastPCSolutionRef = getLastPostcondSolution();
		lastPCSolutionRef.reset();
		foundSolution = solve(lastPCSolutionRef);
		
		//Notifier à l'IHM la solution result
		displaySolution(lastPCSolutionRef);
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("###########################\n");
		}
		return foundSolution;
	}

	@Override
	public boolean solve(Solution sol) {
		if (this.kb)
			return solve3B();
		else
			return solveRes(sol);
	}
	
	@Override
	public void displaySolution(Solution sol) {
		//Do not display anything, this is done in solve.
	}

	//--------------------------------------------------------
	// Shaving strategy for JMLResult domain only 
	// based on Fplib's 2B filtering 
	// w/o propagation to other domains
	//--------------------------------------------------------

	
	/**
	 * If the given rd domain can be shaved, returns true and rd is set to 
	 * the new left or right bound (according to rightSide). 
	 * Otherwise, it returns false and rd is set to a smaller domain that can
	 * be tried for shaving.
	 *  
	 * @param rd
	 * @param feasible
	 * @param rightSide
	 * @return
	 */
	private boolean newBound(VariableDomain rd, boolean shave, boolean rightSide) {
		double min = rd.minValue().doubleValue();
		double max = rd.maxValue().doubleValue();
		
		if (!shave) { //CSP is feasible on rd domain
			//Domain cannot be shaved as is, can we try a smaller one ?
			if (min < max) {
				double middle = (min + max) / 2.0;
				if (rightSide) {
					if (middle == min)
						middle = Math.nextUp(min);
					if (middle < max) {
						rd.setMinValue(middle);
					}
					else {
						rd.setMinValue(max);
					}
				}
				else { //left side shaving
					if (middle == max)
						middle = Math.nextAfter(max, Double.NEGATIVE_INFINITY);
					if (middle > min) {
						rd.setMaxValue(middle);
					}
					else {
						rd.setMaxValue(min);
					}				
				}
				return false;
			}
			else { //No smaller domain can be tried 
				return true;
			}
		}
		else { //CSP infeasible on rd domain
			//Domain can be shaved, set rd to the new bound 
			if (rightSide) {
				double minPrevious = Math.nextAfter(min, Double.NEGATIVE_INFINITY);
				rd.setMinValue(minPrevious);
				rd.setMaxValue(minPrevious);
			}
			else { //left side shaving
				double maxNext = Math.nextUp(max);
				rd.setMinValue(maxNext);
				rd.setMaxValue(maxNext);				
			}
			return true;
		}
	}
	
	/** 
	 * Shaves the method return value domain.
	 * 
	 * The method tries to shorten method return value domain by checking if a 
	 * small interval near the two domain's bounds can be removed. 
	 * It starts with an interval half the size of the initial domain and 
	 * iteratively divides it by two until a small enough interval 
	 * is found if it exists.
	 * 
	 * @param fileName Name of the checked Java program file. 
	 * @param next First node of the program.
	 *
	 * @throws AnalyzeException 
	 */
	public void shaveRes(String fileName, Node next) throws AnalyzeException  {
		Solution sol= new Solution();
		Stack<ChildIterator> exitNodes = new Stack<ChildIterator>();
		Variable jmlResult = new Variable("JMLResult_0", this.returnType());
		VariableDomain rd;
		boolean shave;
		
		this.kb = false;

		if (VerboseLevel.QUIET.mayPrint()) {
			System.out.println("\nStarting shaving the return value domain of " + fileName);
			System.out.println("........................");
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Shaving right side...");
			}
		}
		rd = new VariableDomain(jmlResult, Validation.res_domain.minValue(), Validation.res_domain.maxValue());		
		Number oldBound = rd.minValue();
		while (oldBound.doubleValue() != rd.maxValue().doubleValue()) {
			shave = false;
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("oldBound = " + oldBound + " rd max = " + rd.maxValue() 
					+ ". Still shaving rigth side...");
			}
			while (!newBound(rd, shave, true)) {
				save();
				
				((FplibValidationCSP)floatCSP).setDomain(rd);
				Node n = next.cloneNode(true);
				JavaBlockVisitAndValidate.validateBlock(n, this, sol, exitNodes);
				shave = sol.isEmpty();
				if (!shave)
					oldBound = rd.minValue();
				sol.reset();
				restore();
				exitNodes.clear();
				this.loop.reset();
			}
			rd.setMinValue(oldBound);
		}

		Number rightBound = rd.maxValue();
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Shaving left side...");
		}
		rd = new VariableDomain(jmlResult, Validation.res_domain.minValue(), Validation.res_domain.maxValue());		
		oldBound = rd.maxValue();
		while (oldBound.doubleValue() != rd.minValue().doubleValue()) {
			shave = false;
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("oldBound = " + oldBound + " rd min = " + rd.minValue() 
					+ ". Still shaving left side...");
			}
			while (!newBound(rd, shave, false)) {
				save();
				
				((FplibValidationCSP)floatCSP).setDomain(rd);
				Node n = next.cloneNode(true);
				JavaBlockVisitAndValidate.validateBlock(n, this, sol, exitNodes);
				shave = sol.isEmpty();
				if (!shave)
					oldBound = rd.maxValue();
				sol.reset();
				restore();
				exitNodes.clear();
				this.loop.reset();
			}
			rd.setMaxValue(oldBound);
		}

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("New computed left bound = " + rd.minValue());
			System.out.println("New computed right bound = " + rightBound);
			System.out.println(printStatus());
		}
	}
	
	/**
	 * Calls Fplib's 2B filtering to decide if a solution exists. 
	 * 
	 * @param variables
	 * @return
	 */
	public boolean solveRes(Solution sol) {
		Boolean foundSolution = true;

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("2B on float CSP...");
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(floatCSP.toString());
			}
		}

		floatCSP.startSearch();
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println("Initial result domain: " + floatCSP.solution().get("JMLResult_0"));
		}
		foundSolution = ((FplibSolver)floatCSP.cspSolver).twoB(0);
		floatCSP.stopSearch();
		
		if (foundSolution)
			sol.copy(floatCSP.solution());
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("2B consistent: " + foundSolution);
		}
		return foundSolution;
	}
	
	
	//--------------------------------------------------------
	// Shaving strategy based on Fplib's 3B filtering
	//--------------------------------------------------------

	/**
	 * Return value domain shaving with 3B consistency.
	 * 
	 * The idea is to start with a given domain for the program return value (e.g. obtained from fluctuat).
	 * Then, for each execution path in the program, we build the corresponding constraint system in Fplib.
	 * We filter with 3B these CSP and build the union of the domains obtained for the return value on all 
	 * these CSP.
	 *  
	 * @param fileName
	 * @param next
	 * @throws AnalyzeException
	 */
	public void shave3B(String fileName, Node next) throws AnalyzeException  {
		Solution sol= new Solution();
		Stack<ChildIterator> exitNodes = new Stack<ChildIterator>();

		this.kb = true;
		
		if (VerboseLevel.QUIET.mayPrint()) {
			System.out.println("\nStarting shaving with 3B the return value domain of " + fileName);
			System.out.println("........................");
		}

		this.res = new VariableDomain(new Variable("JMLResult_0", this.returnType()), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
	
		Variable jmlResult = new Variable("JMLResult_0", this.returnType());
		VariableDomain rd = new VariableDomain(jmlResult, Validation.res_domain.minValue(), Validation.res_domain.maxValue());	
		((FplibValidationCSP)floatCSP).setDomain(rd);
		((FplibValidationCSP)floatCSP).varBlock.print();
		JavaBlockVisitAndValidate.validateBlock(next, this, sol, exitNodes);

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Final result domain = " + res);
		}
	}
		
	/**
	 * This method is called at the end of an execution path on a return statement. 
	 * It tries to shave the CSP variable domains (including the return value domain) 
	 * using Fplib's 3B filtering. The computed return value domain is then "unioned" with
	 * the domains computed for this return value in the other execution paths.  
	 *  
	 * @return
	 */
	public boolean solve3B() {
		Boolean foundSolution = true;

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("3B on float CSP...");
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(floatCSP.toString());
			}
		}

		floatCSP.startSearch();
		if (Validation.verboseLevel == VerboseLevel.DEBUG) {
			System.out.println("Initial result domain: " + floatCSP.solution().get("JMLResult_0").domain());
		}
		foundSolution = ((FplibSolver)floatCSP.cspSolver).kB(3, Validation.fplibkBprecision, Validation.fplib2Bprecision);
		floatCSP.stopSearch();
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("3B consistent: " + foundSolution);
			System.out.println("Old result domain: " + this.res);
		}
		if (foundSolution) {
			VariableDomain cd = floatCSP.solution().get("JMLResult_0").domain();
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Computed result domain: " + cd);
			}
			//Union between res and cd
			if (cd.minValue().doubleValue() < res.minValue().doubleValue())
				res.setMinValue(cd.minValue());
			if (cd.maxValue().doubleValue() > res.maxValue().doubleValue())
				res.setMaxValue(cd.maxValue());		
		}
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("New result domain: " + res);
		}
		return foundSolution;
	}
		
}
