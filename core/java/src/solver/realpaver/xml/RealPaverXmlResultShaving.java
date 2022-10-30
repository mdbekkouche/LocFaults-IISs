package solver.realpaver.xml;

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
 * This validation system shaves the return value of the analyzed method using 
 * Real Paver.  
 *  
 * @author Olivier Ponsini
 *
 */
public class RealPaverXmlResultShaving extends RealPaverXmlValidation {
	/**
	 * Domain storing the union of all computed domains along each executable path.
	 */
	private VariableDomain res;
	
	/**
	 * Constructs a system with a CSP on reals for Real Paver.
	 * This system will try to shave the domain of the method return value.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public RealPaverXmlResultShaving(String name) {
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
	 * @see validation.system.xml.ValidationSystem#checkAssertion(expression.logical.LogicalExpression, java.lang.String)
	 */
	@Override
	public boolean checkAssertion(LogicalExpression assertion, String message) {
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Warning: Assertion is ignored in shaving mode!");
		}
		return true;
	}

	
	@Override
	public boolean tryDecision(LogicalExpression c) {
		csp.save();
		csp.addConstraint(c);
		
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println(csp.toString());
		}

		csp.setOptionHeader("Output\n\tmode = hull;\nBisection\n\t mode = paving;\nConsistency\n\tlocal = HC4;");			
		
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

	/**
	 * This method is called at the end of an execution path on a return statement. 
	 * It tries to shave the CSP variable domains (including the return value domain) 
	 * using Fplib's 3B filtering. The computed return value domain is then "unioned" with
	 * the domains computed for this return value in the other execution paths.  
	 *  
	 * @return
	 */
	@Override
	public boolean solve(Solution sol) {
		Boolean foundSolution = true;

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Filtering on real CSP...");
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println(csp.toString());
			}
		}
		
		//Set consistency and time limit (0.5 s)
		//csp.setOptionHeader("Output\n\tmode = hull;\nBisection\n\tmode = paving;\nConsistency\n\tstrong = 3B,\n\twidth = 1.0e-3;\nTime = 500;");
		csp.setOptionHeader("Output\n\tmode = hull;\nBisection\n\tmode = paving;\nConsistency\n\tlocal = BC5;\nTime = 500;");
		
		csp.startSearch();
		if (Validation.verboseLevel == VerboseLevel.DEBUG) {
			System.out.println("Initial result domain: " + ((validation.system.VariableVarStore)csp.varBlock).get("JMLResult_0").domain());
		}
		foundSolution = csp.next();
		csp.stopSearch();
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("3B consistent: " + foundSolution);
			System.out.println("Old result domain: " + this.res);
		}
		if (foundSolution) {
			VariableDomain cd = csp.solution().get("JMLResult_0").domain();
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
	
	@Override
	public void displaySolution(Solution sol) {
		//Do not display anything, this is done in solve3B and solveRes.
	}

	//--------------------------------------------------------
	// Shaving strategy based on 3B filtering
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
		
		if (VerboseLevel.QUIET.mayPrint()) {
			System.out.println("\nStarting shaving the return value domain of " + fileName);
			System.out.println("........................");
		}

		//((RealPaverXmlCsp)csp).setOptionHeader("Consistency\n\tstrong = 3B,\n\twidth = 1;");
		
		this.res = new VariableDomain(new Variable("JMLResult_0", this.returnType()), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
	
		Variable jmlResult = new Variable("JMLResult_0", this.returnType());
		VariableDomain rd = new VariableDomain(jmlResult, Validation.res_domain.minValue(), Validation.res_domain.maxValue());	
		csp.setDomain(rd);
		csp.varBlock.print();
		JavaBlockVisitAndValidate.validateBlock(next, this, sol, exitNodes);

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Final result domain = " + res);
		}
	}
	
}
