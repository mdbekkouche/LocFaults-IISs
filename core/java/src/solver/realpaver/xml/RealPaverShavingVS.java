package solver.realpaver.xml;

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

import validation.Validation.VerboseLevel;

import validation.solution.Solution;
import validation.system.VariableVarStore;

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
public class RealPaverShavingVS extends RealPaverXmlValidation {
	
	private Map<String, ShaveInfo> shavedVarsInfo;
	private DomainBox fluctuatDomains;
	private double intervalMinSize;
	private double intervalMinRatio;
	/**
	 * Constructs a system with a CSP on reals for Real Paver.
	 * This system will try to shave the domains as specified in a configuration file.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public RealPaverShavingVS(String name) {
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

	//TODO: Domains filtered could be used in the remaining analysis instead of being discarded here and recomputed later...
	//      This would imply saving/restoring domains at each tryDecision. Currently only the variables are saved, not their domains...
	@Override
	public boolean tryDecision(LogicalExpression c) {
		csp.save();
		csp.addConstraint(c);
		
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println(csp.toString());
		}

		csp.resetDomains(this.fluctuatDomains);
		csp.setOptionHeader("Output mode = hull;\nBisection mode = paving;\nConsistency local = HC4;");			
		csp.startSearch();
		
		boolean hasSolution = csp.next();
		
		csp.stopSearch();
		csp.restore();
		
		return hasSolution;
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
		
		csp.addConstraint(returnAssignment);
		
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
		Queue<Variable> varQ = new ArrayDeque<Variable>(shavedVarsInfo.size());
		Variable shavedVar;
		boolean shaved = false;
		double splitSize;
		long splitFactor;
		boolean consistent = true;

		csp.resetDomains(fluctuatDomains);
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Shaving on real CSP...");
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println(csp.toString());
			}
		}
		
		//Ideally we should start with domains filtered when trying decisions along this path.
		//Since it is not done, or when there is no control statements in the function, we could
		//do a first filtering...
		
		for (String varName: shavedVarsInfo.keySet()) {
			shavedVar = ((VariableVarStore)csp.varBlock).get(varName);
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
				shaved = shaveSide(Side.LEFT, shavedVar, splitSize);
				shaved |= shaveSide(Side.RIGHT, shavedVar, splitSize);

				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println("Variable after shaving: " + shavedVar.domain());
				}

				if (shaved) {
					csp.setOptionHeader("Output mode = hull;\nBisection none;\nConsistency local = BC5;");			
					csp.startSearch();
					consistent = csp.next();
					csp.stopSearch();
					if (consistent) {
						csp.setDomainsToCurrentSolution();
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
				var = ((VariableVarStore)csp.varBlock).get(varName);
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
		csp.resetDomains(fluctuatDomains);

		return consistent;
	}
	
	@Override
	public void displaySolution(Solution sol) {
		//Do not display anything, this is done in solve.
	}

	//--------------------------------------------------------
	// Shaving strategy for a queue of variables 
	// based on Real Paver local filterings 
	// with propagation to other domains
	// Shaving is done in each execution path
	//--------------------------------------------------------

	public void shave(String fluctuatFilename, double intervalMinSize, double intervalMinRatio, Node javaBlock) 
		throws AnalyzeException 
	{
		Solution sol= new Solution();
		Stack<ChildIterator> exitNodes = new Stack<ChildIterator>();
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\nStarting shaving with local consistencies in each path");
			System.out.println("........................");
		}

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
	
	/**
	 * This version of splitDomain differs from the one in class Shaving to circumvent a "feature" of Realpaver 0.4
	 * that assimilates all values above 1.797693134862283e+308 to Double.POSITIVE_INFINITY and 
	 * below -1.797693134862283e+308 to Double.NEGATIVE_INFINITY.
	 * Because of this behavior the shaving algorithm would not terminate when trying to shave smaller intervals than
	 * [1.7976931348622828e+308, +oo] on right (resp. [-oo, -1.7976931348622828e+308] on left).
	 */
	public static VariableDomain splitDomain(Side side, VariableDomain domain, double splitSize) {
		VariableDomain splitDomain = domain.clone();
		double inf = domain.minValue().doubleValue();
		double sup = domain.maxValue().doubleValue();
		
		if (side == Side.LEFT) {
			if (inf + splitSize == Double.NEGATIVE_INFINITY) {
				splitDomain.setMaxValue(-1.7976931348622828e+308);
			}
			else {
				splitDomain.setMaxValue(inf + splitSize);
			}
		}
		else {
			if (sup - splitSize == Double.POSITIVE_INFINITY) {
				splitDomain.setMinValue(1.7976931348622828e+308);
			}
			else {
				splitDomain.setMinValue(sup - splitSize);
			}
		}
		
		return splitDomain;
	}
	
	private boolean shaveSide(Side side, Variable shavedVar, double splitSize) {
		VariableDomain shavingDomain, initialDomain;
		
		initialDomain = shavedVar.domain();
		shavingDomain = splitDomain(side, initialDomain, splitSize);
		
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("shaving domain: " + shavingDomain);
		}
		
		shavedVar.setDomain(shavingDomain);
		csp.setOptionHeader("Output mode = hull;\nBisection none;\nConsistency local = BC5;");		
		csp.startSearch();
		boolean consistent = csp.next();
		csp.stopSearch();
		shavedVar.setDomain(initialDomain);
		if (!consistent) {
			Shaving.updateShavedDomain(side, initialDomain, shavingDomain);
		}
		return !consistent;
	}

	private void doShavedDomainsUnion() {
		VariableDomain pathDomain;
		VariableDomain unionDomain;
		Variable var;
		
		for (ShaveInfo varInfo: shavedVarsInfo.values()) {
			unionDomain = varInfo.unionDomain;
			var = ((VariableVarStore)csp.varBlock).get(unionDomain.name());
			if (var != null) { //Variable to shave exists in current path
				pathDomain = var.domain();
				if (pathDomain.minValue().doubleValue() < unionDomain.minValue().doubleValue())
					unionDomain.setMinValue(pathDomain.minValue());
				if (pathDomain.maxValue().doubleValue() > unionDomain.maxValue().doubleValue())
					unionDomain.setMaxValue(pathDomain.maxValue());
			}
		}
	}
		
}
