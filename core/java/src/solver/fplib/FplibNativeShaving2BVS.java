package solver.fplib;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.w3c.dom.Node;

import exception.AnalyzeException;

import expression.Expression;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;

import validation.Validation;
import validation.Validation.VerboseLevel;

import validation.solution.Solution;

import validation.util.ChildIterator;
import validation.util.Shaving;
import validation.util.Shaving.ShaveInfo;
import validation.util.Shaving.Side;


import validation.visitor.JavaBlockVisitAndValidate;

/**
 *  
 * @author Olivier Ponsini
 *
 */
public class FplibNativeShaving2BVS extends FplibValidationSystem {
		
	private Map<String, ShaveInfo> shavedVarsInfo;
	//private DomainBox fluctuatDomains;
	private double twoBpercent;
	private double intervalMinSize;
	private double intervalMinRatio;
	/**
	 * Constructs a system with a CSP on floats for fplib.
	 * This system will try to shave the domain of the method return value.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public FplibNativeShaving2BVS(String name) {
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
		floatCSP.startSearch();

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("2B shaving on float CSP...");
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println(floatCSP.toString());
				floatCSP.varBlock.print();	
			}
		}

		boolean consistent = ((FplibSolver)floatCSP.cspSolver).shave(this.twoBpercent, this.intervalMinSize, this.intervalMinRatio);

		floatCSP.stopSearch();
			
		return consistent;
	}
	
	@Override
	public void displaySolution(Solution sol) {
		//Do not display anything, this is done in solve2B.
	}

	//--------------------------------------------------------
	// Shaving strategy for a queue of variables 
	// based on Fplib's 2B filtering 
	// with propagation to other domains
	// Shaving is done in each execution path
	//--------------------------------------------------------

	public void shave2B(String fluctuatFilename, double twoBpercent, double intervalMinSize, double intervalMinRatio, Node javaBlock) 
		throws AnalyzeException 
	{
		Solution sol= new Solution();
		Stack<ChildIterator> exitNodes = new Stack<ChildIterator>();
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\nStarting shaving with 2B in each path");
			System.out.println("........................");
		}

		this.twoBpercent = twoBpercent;
		this.intervalMinSize = intervalMinSize;
		this.intervalMinRatio = intervalMinRatio;
				
		//Shaving.FluctuatFileInfo fluctuatInfo = Shaving.loadFluctuatFile(fluctuatFilename);
		//this.fluctuatDomains = fluctuatInfo.fluctuatDomains;
		//this.shavedVarsInfo = fluctuatInfo.shavedVarsInfo;
		FplibSolver.initShaving(fluctuatFilename);
		
		JavaBlockVisitAndValidate.validateBlock(javaBlock, this, sol, exitNodes);
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("........................");
			System.out.println("Final shaved domains");
			FplibSolver.displayShavedDomains();
		}
	}	
	
}
