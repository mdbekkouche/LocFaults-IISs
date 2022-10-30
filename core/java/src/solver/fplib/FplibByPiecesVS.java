package solver.fplib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import solver.fluctuat.FluctuatPiecewise;
import solver.fluctuat.Fluctuat.DebugResult;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.system.CtrDeepStore;
import validation.util.Shaving;
import validation.util.Shaving.ShaveInfo;
import validation.visitor.JavaVisitByPieces;
import exception.AnalyzeException;
import expression.Expression;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.variables.ConcreteVariable;
import expression.variables.DomainBox;
import expression.variables.Variable;

/**
 *  
 * @author Olivier Ponsini
 *
 */
public class FplibByPiecesVS extends FplibValidationSystem {
		
	private Map<String, ShaveInfo> shavedVarsInfo;
	private DomainBox fluctuatDomains;
	private double twoBpercent;
	private double kBpercent;
	private FluctuatPiecewise fluctuatAnalysis;
	private String fluctuatFctName;

	/**
	 * Constructs a system with a CSP on floats for fplib.
	 * This system will try to shave the domain of the method return value.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public FplibByPiecesVS(String name) {
		super(name);
		// Not great to do it here, but we want a RealPaverByPiecesVarStore and not the standard VariableVarStore...
		floatCSP.varBlock = new FplibVarDeepStore();
		// Idem for constraint store
		floatCSP.constr = new CtrDeepStore();
		fluctuatFctName = "main";
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
		
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println(floatCSP.toString());
			//floatCSP.constraintBlock().print();
			//floatCSP.varBlock.print();
		}
		
		floatCSP.startSearch();
		//floatCSP.setFplibDomains(fluctuatDomains);
		foundSolution = ((FplibSolver)floatCSP.cspSolver).kB(3, kBpercent, twoBpercent);
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

		// Union the domains of the shaved variables
		ShaveInfo shaveInfo;
		for (Variable v: lastPCSolutionRef) {
			shaveInfo = this.shavedVarsInfo.get(v.name());
			if (shaveInfo != null) {
				shaveInfo.unionDomain.union(v.domain());
			}
		}
		
		//Notifier à l'IHM la solution result
		displaySolution(lastPCSolutionRef);
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("###########################\n");
		}

		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Shaved domains after this path: ");
			for (ShaveInfo varInfo: shavedVarsInfo.values()) {
				System.out.println(varInfo.unionDomain);
			}
		}

		return foundSolution;
	}

	@Override
	public boolean solve(Solution sol) {
		floatCSP.varBlock.save();
		//Update variable domains to values computed by Fluctuat
		setVarLastRenamingToFluctuatValue();

		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println(floatCSP.toString());
			//floatCSP.constraintBlock().print();
			//floatCSP.varBlock.print();
		}
		
		floatCSP.startSearch();
		//floatCSP.setFplibDomains(fluctuatDomains);
		Boolean foundSolution = ((FplibSolver)floatCSP.cspSolver).kB(3, kBpercent, twoBpercent);
		// Build a solution where only the last renaming of each variable is kept
		// and associated with the root name of the variable
		if (foundSolution) {
			Solution s = floatCSP.solution();
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println("Solution:\n" + s);
			}
			Variable v;
			for (String varName: var.getCurrentVarNames()) {
				v = s.get(varName);
				sol.add(new Variable(v.root(), v.type(), v.use(), 
						             v.domain().minValue(), v.domain().maxValue()));
			}
		}
		floatCSP.stopSearch();
		floatCSP.varBlock.restore();
		return foundSolution;
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

	public void shave(String fluctuatFilename, double kBpercent, double twoBpercent, Node javaBlock) 
		throws AnalyzeException 
	{
		Solution sol= new Solution();
		fluctuatAnalysis = new FluctuatPiecewise(fluctuatFctName);
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\nStarting shaving with 3B and Fluctuat piecewise");
			System.out.println("........................");
		}

		this.kBpercent = kBpercent;
		this.twoBpercent = twoBpercent;
		
		Shaving.FluctuatFileInfo fluctuatInfo = Shaving.loadFluctuatFile(fluctuatFilename);
		this.fluctuatDomains = fluctuatInfo.fluctuatDomains;
		this.shavedVarsInfo = fluctuatInfo.shavedVarsInfo;

		//Analyze first piece of CFG with Fplib
		floatCSP.setDomains(this.fluctuatDomains);
		JavaVisitByPieces.validate(this, sol, javaBlock, fluctuatAnalysis);
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("........................");
			System.out.println("Final shaved domains");
			for (ShaveInfo varInfo: shavedVarsInfo.values()) {
				System.out.println(varInfo.unionDomain);
			}
		}
	}	
	
	/* 
	 * This method is called when a junction point has been reached and the analysis must be 
	 * restarted from this point.
	 * If <code>sol!=null</code>, reset the CSP and restarts analysis with the domains computed at the junction point; 
	 * otherwise, it was not a real junction (only one executable path reaches it), analysis just goes on with current CSP. 
	 * 
	 */
	@Override
	public void reset(Solution sol) {
		if (sol != null) {
			// Empties the constraint store, the variable store and the symbol table
			floatCSP.constraintBlock().reset();
			floatCSP.varBlock.reset();
			var.reset();

			Variable v; 
			DebugResult fluctuatVar;
			for (Variable solVar: sol) {
				v = this.addNewVar(solVar.name(), solVar.type(), solVar.use());
				// Variable added to symbol table and the one in the variable store differ because
				// Fplib uses ConcreteVariables instead of just Variables
				v = (Variable) floatCSP.varBlock.get(v.name());
				v.setDomain(solVar.domain().minValue(), solVar.domain().maxValue());
				
				// Check whether it is the META variable JMLResult and whether the new domain improves over the one of Fluctuat
				if (solVar.name().equals("JMLResult")) {
					fluctuatVar = fluctuatAnalysis.fluctuat.getVariableValues(fluctuatFctName);
					if (fluctuatVar.maxFloatDomain > solVar.domain().maxValue().doubleValue() 
					    || 
					    fluctuatVar.minFloatDomain < solVar.domain().minValue().doubleValue()) 
					{
					    fluctuatAnalysis.fluctuat.setVariableValues(
								fluctuatFctName, 
								solVar.domain().minValue().doubleValue(), 
								solVar.domain().maxValue().doubleValue());
					}
				}
				else {
					fluctuatVar = fluctuatAnalysis.fluctuat.getVariableValues(solVar.name());
					if (fluctuatVar.maxFloatDomain > solVar.domain().maxValue().doubleValue() 
					    || 
					    fluctuatVar.minFloatDomain < solVar.domain().minValue().doubleValue()) 
					{
					    fluctuatAnalysis.fluctuat.setVariableValues(
								solVar.name(), 
								solVar.domain().minValue().doubleValue(), 
								solVar.domain().maxValue().doubleValue());
					}
				}
			}
		}
	}

	public void setVarLastRenamingToFluctuatValue() {
		DebugResult fluctuatVar;
		Variable cspVar;
		for (String varName: var.getCurrentVarNames()) {
			if (varName.equals("JMLResult_0")) {
				fluctuatVar = fluctuatAnalysis.fluctuat.getVariableValues(fluctuatFctName);				
			}
			else {
				fluctuatVar = fluctuatAnalysis.fluctuat.getVariableValues(Variable.root(varName));
			}
			cspVar = ((FplibVarDeepStore)floatCSP.varBlock).get(varName);
			cspVar.setDomain(fluctuatVar.minFloatDomain, fluctuatVar.maxFloatDomain);
		}
	}

	@Override
	public ArrayList<LogicalExpression> getConstraints() {
		return ((CtrDeepStore)floatCSP.constr).getAllConstraints();
	}

	@Override
	public HashMap<String, ConcreteVariable<Long>> copyVariableMap() {
		return ((FplibVarDeepStore)floatCSP.varBlock).copyVariableMap();
	}

	@Override
	public void setConstraints(ArrayList<?> ctrs) {
		((CtrDeepStore)floatCSP.constr).reset((ArrayList<LogicalExpression>)ctrs);
	}

	@Override
	public void setVariableMap(HashMap<String, ?> savedVar) {
		((FplibVarDeepStore)floatCSP.varBlock).reset((HashMap<String, ConcreteVariable<Long>>)savedVar);
	}

}
