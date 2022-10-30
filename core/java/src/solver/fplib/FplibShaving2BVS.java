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
public class FplibShaving2BVS extends FplibValidationSystem {
		
	private Map<String, ShaveInfo> shavedVarsInfo;
	private DomainBox fluctuatDomains;
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
	public FplibShaving2BVS(String name) {
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
		
		floatCSP.setDomains(fluctuatDomains);
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
			return solve2B();
		
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
		
		Shaving.FluctuatFileInfo fluctuatInfo = Shaving.loadFluctuatFile(fluctuatFilename);
		this.fluctuatDomains = fluctuatInfo.fluctuatDomains;
		this.shavedVarsInfo = fluctuatInfo.shavedVarsInfo;

		JavaBlockVisitAndValidate.validateBlock(javaBlock, this, sol, exitNodes);
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("........................");
			System.out.println("Final shaved domains");
			for (ShaveInfo varInfo: shavedVarsInfo.values()) {
				System.out.println(varInfo.unionDomain);
			}
		}
	}	
		
	private boolean shaveSide2B(Side side, Variable shavedVar, double splitSize) {
		FplibSolver fplibSolver = (FplibSolver)floatCSP.cspSolver;
		VariableDomain shavingDomain;
		
		fplibSolver.push();
		shavingDomain = Shaving.splitDomain(side, shavedVar.domain(), splitSize);
		
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("shaving domain: " + shavingDomain);
		}
		
		floatCSP.setFplibDomain(shavingDomain);
		boolean consistent = fplibSolver.twoB(twoBpercent);
		fplibSolver.pop();
		if (!consistent) {
			Shaving.updateShavedDomain(side, shavedVar.domain(), shavingDomain);
			floatCSP.setFplibDomain(shavedVar.domain());
		}
		return !consistent;
	}
		
	private void doShavedDomainsUnion() {
		VariableDomain pathDomain;
		VariableDomain unionDomain;
		Variable var;
		
		for (ShaveInfo varInfo: shavedVarsInfo.values()) {
			unionDomain = varInfo.unionDomain;
			var = ((FplibVarBlock)floatCSP.varBlock).get(unionDomain.name());
			if (var != null) { //Variable to shave exists in current path
				pathDomain = var.domain();
				if (pathDomain.minValue().doubleValue() < unionDomain.minValue().doubleValue())
					unionDomain.setMinValue(pathDomain.minValue());
				if (pathDomain.maxValue().doubleValue() > unionDomain.maxValue().doubleValue())
					unionDomain.setMaxValue(pathDomain.maxValue());
			}
		}
	}
	
	public boolean solve2B() {
		FplibSolver fplibSolver = (FplibSolver)floatCSP.cspSolver;
		Queue<Variable> varQ = new ArrayDeque<Variable>(shavedVarsInfo.size());
		Variable shavedVar;
		boolean shaved = false;
		double splitSize;
		long splitFactor;
		boolean consistent = true;

		floatCSP.setDomains(fluctuatDomains);
		floatCSP.startSearch();
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("2B shaving on float CSP...");
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println(floatCSP.toString());
				floatCSP.varBlock.print();	
			}
		}
		
		//Ideally we should start with domains filtered when trying decisions along this path.
		//Since it is not done, or when there is no control statements in the function, we could
		//do a first 2B...
		//consistent = fplibSolver.twoB(twoBpercent);
		//floatCSP.setDomainsToFplibDomains();
		//if (VerboseLevel.DEBUG.mayPrint()) {
			//System.out.println("After first 2B:");
			//floatCSP.varBlock.print();	
		//}
		
		//varQ.addAll(shavedVarsInfo.keySet());
		for (String varName: shavedVarsInfo.keySet()) {
			shavedVar = ((FplibVarBlock)floatCSP.varBlock).get(varName);

			if (shavedVar != null) {
				if (shavedVar.domain() == null) {
					shavedVar.setDomain(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
				}
				varQ.add(shavedVar);
			}
		}

		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println("Variables to be shaved:");
			System.out.println(shavedVarsInfo);			
		}
		
		while (!varQ.isEmpty() && consistent) {
			shavedVar = varQ.remove();
			
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("Candidate variable for shaving: " + shavedVar.domain());
			}
			
			splitFactor = shavedVarsInfo.get(shavedVar.name()).splitFactor;
			splitSize = Shaving.splitSize(shavedVar, splitFactor);
			if (splitSize > this.intervalMinSize && 1.0/splitFactor > this.intervalMinRatio) {
				shaved = shaveSide2B(Side.LEFT, shavedVar, splitSize);
				shaved |= shaveSide2B(Side.RIGHT, shavedVar, splitSize);

				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println("Variable after shaving: " + shavedVar.domain());
				}

				if (shaved) {
					consistent = fplibSolver.twoB(twoBpercent);
					if (consistent) {
						floatCSP.setDomainsToCurrentSolution();
					}
				}
				else {
					//splitFactor could be increased faster to speed up shaving (but potentially loose in precision...)
					shavedVarsInfo.get(shavedVar.name()).splitFactor *= 2;
					if (VerboseLevel.DEBUG.mayPrint()) {
						System.out.println("New split Factor=" + shavedVarsInfo.get(shavedVar.name()).splitFactor);
					}
				}
				varQ.add(shavedVar);
			}
		}
		
		if (consistent) {
			doShavedDomainsUnion();
		}
				
		if (VerboseLevel.VERBOSE.mayPrint()) {
			Variable var;
			System.out.println("This path shaved domains: ");
			for (String varName: shavedVarsInfo.keySet()) {
				var = ((FplibVarBlock)floatCSP.varBlock).get(varName);
				if (var != null) { //Shaved var exists in current path
					System.out.println(var.domain());
				}
			}
			System.out.println("Shaved domains after this path: ");
			for (ShaveInfo varInfo: shavedVarsInfo.values()) {
				System.out.println(varInfo.unionDomain);
			}
		}
		
		//Reset splitFactor
		for (ShaveInfo varInfo: shavedVarsInfo.values()) {
			varInfo.splitFactor = 2;
		}
		//Domains should be reset to their value before last decision
		floatCSP.resetDomains(fluctuatDomains);
		floatCSP.stopSearch();

		return consistent;
	}

	//--------------------------------------------------------
	// Shaving strategy for a queue of variables
	// based on Fplib's 2B filtering 
	// with propagation to other domains
	// Shaving is done on the whole program
	//--------------------------------------------------------

	public void shave2Bpgm(double twoBpercent, double intervalMinSize, Node javaBlock) throws AnalyzeException {
		Variable shavedVar;
		boolean shaved;
		Queue<Variable> varQ = initialVarQ();
		Queue<Variable> visitedVarQ = new ArrayDeque<Variable>(varQ.size());
		Variable jmlResult = ((FplibVarBlock)floatCSP.varBlock).get("JMLResult_0");
		jmlResult.setDomain(Validation.res_domain.minValue(), Validation.res_domain.maxValue());
		
		while (!varQ.isEmpty()) {
			shavedVar = varQ.remove();
			shaved = shaveSide(shavedVar, Side.LEFT, intervalMinSize, javaBlock);
			shaved |= shaveSide(shavedVar, Side.RIGHT, intervalMinSize, javaBlock);
			//Note that it could be interesting to trigger 2B filtering and 
			//variable adding depending on the size of the reduction instead of 
			//just on whether a reduction occurred or not (e.g., if domain reduction 
			//is too small, don't bother reducing other domains that may be impacted 
			//by current shaved domain).
			if (shaved) {
				floatCSP.startSearch();
				((FplibSolver)floatCSP.cspSolver).twoB(twoBpercent);
				floatCSP.stopSearch();
				((FplibValidationCSP)floatCSP).setDomainsToCurrentSolution();
				varQ.addAll(visitedVarQ);
				visitedVarQ.clear();
			}
			visitedVarQ.add(shavedVar);
		}
		System.out.println("domains:");
		floatCSP.varBlock.print();
	}
	
	public boolean shaveSide(Variable v, Side side, double minSize, Node javaBlock) throws AnalyzeException {
		Solution sol= new Solution();
		Stack<ChildIterator> exitNodes = new Stack<ChildIterator>();
		VariableDomain dv = v.domain().clone();
		VariableDomain d = v.domain();
		boolean shaved = false;
		double inf, sup, mid;
		
		initialSideDomain(d, side);
		while (!shaved && ((d.maxValue().doubleValue() - d.minValue().doubleValue()) > minSize)) {
			floatCSP.constraintBlock().save();
			this.var.save();
			JavaBlockVisitAndValidate.validateBlock(javaBlock, this, sol, exitNodes);
			floatCSP.constraintBlock().restore();
			this.var.restore();
			exitNodes.clear();
			this.loop.reset();

			if (sol.isEmpty()) {
				shaved = true;
			}
			else {
				inf = d.minValue().doubleValue();
				sup = d.maxValue().doubleValue();
				mid = (sup + inf) / 2;
				if (side == Side.LEFT) {
					if (mid > inf) {
						d.setMaxValue(mid);
					}
					else {
						d.setMaxValue(inf);
					}
				}
				else {
					if (mid < sup) {
						d.setMinValue(mid);
					}
					else {
						d.setMinValue(sup);
					}
				}
			}
			sol.reset();
		}
		if (shaved) {
			if (side == Side.LEFT) {
				d.setMinValue(d.maxValue());
				d.setMaxValue(dv.maxValue());
			}
			else {
				d.setMaxValue(d.minValue());
				d.setMinValue(dv.minValue());
			}
		}
		else {
			d.setMinValue(dv.minValue());
			d.setMaxValue(dv.maxValue());
		}
		return shaved;
	}
	
	private Queue<Variable> initialVarQ() {
		ArrayDeque<Variable> varQ = new ArrayDeque<Variable>(floatCSP.varBlock.size());
		//for (Variable v: (FplibVarBlock)floatCSP.varBlock) {
		//	varQ.add(v);
		//}
		varQ.add(((FplibVarBlock)floatCSP.varBlock).get("JMLResult_0"));
		return varQ;
	}
	
	private void initialSideDomain(VariableDomain d, Side side) {
		double inf = d.minValue().doubleValue();
		double sup = d.maxValue().doubleValue();
		double quarter = (sup - inf) / 4;
		if (side == Side.LEFT) {
			d.setMaxValue(inf + quarter);
		}
		else {
			d.setMinValue(sup - quarter);
		}
	}
	
}
