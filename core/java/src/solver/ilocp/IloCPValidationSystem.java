package solver.ilocp;

import java.util.Map;

import expression.AbstractExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NotExpression;
import expression.logical.OrExpression;
import expression.variables.ArrayVariable;
import expression.variables.Variable;
import expression.variables.Variable.Use;

import solver.cplex.CplexIntegerValidationCSP;
import validation.solution.Solution;
import validation.solution.ValidationStatus;

import validation.system.xml.IntegerValidationCSP;
import validation.system.xml.ValidationSystem;

import validation.util.Type;

import validation.Validation.VerboseLevel;

/** 
 * This validation system manages a linear CSP using Ilog Cplex and a non linear
 * CSP using Ilog CP Optimizer. No boolean abstraction is made, so there is no 
 * boolean CSP.
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public class IloCPValidationSystem extends ValidationSystem {

	// constraint systems to store and solve program and specification constraints
	public IntegerValidationCSP completeCSP; // all constraints
		
	public IntegerValidationCSP linearCSP; // linear constraints 

	/**
	 * Constructs a validation system with a linear and a non linear CSP.
	 * The respective solvers associated with the CSPs are Ilog Cplex and 
	 * Ilog CP Optimizer.
	 * 
	 * @param name The name of the file containing the XML description of the Java program
	 *             to be verified.
	 */
	public IloCPValidationSystem(String name) {
		super(name + " using Ilog Cplex for linear constraints and Ilog CP Optimizer "
			  + "for non linear constraints.");
	}
	
	/**
	 * This methods creates the CSPs of this validation system. 
	 * For this validation system, there are two CSPs:
	 * <ul>
	 * <li>a linear CSP handled by Ilog Cplex;</li>
	 * <li>a non linear CSP handled by Ilog CP Optimizer.</li>
	 * </ul>
	 * 
	 * This method is called by the mother class constructor.
	 * 
	 * @param n Not used...
	 */
	@Override
	protected void setCSP() {
		status = new ValidationStatus();
		linearCSP = new CplexIntegerValidationCSP("CPLEX (linear)", this);
		completeCSP = new IloCPIntegerValidationCSP("CP Optimizer (complete)", this);
		status.addStatus(completeCSP.getStatus());
		status.addStatus(linearCSP.getStatus());
	}
	
	@Override
	public Variable addNewVar(String n, Type t, Use u) {
		Variable v = var.addNewVar(n, t, u);
		completeCSP.addVar(v);
		return v;
	}

	//helen
	@Override
	public void addVar(Variable v) {
		completeCSP.addVar(v);
	}
	
	// add a new integer variable for the new SSA renaming of n
	@Override
	public Variable addVar(String n) {
		Variable v = var.addVar(n);
		completeCSP.addVar(v);
		return v;
	}
	
	// add a new integer array variable for the new SSA renaming of n
	@Override
	public ArrayVariable addArrayVar(String n) {
		ArrayVariable v = (ArrayVariable) var.addVar(n);
		completeCSP.addArrayVar(v);
		return v;
	}

	// add a new integer array variable
	@Override
	public ArrayVariable addNewArrayVar(String n, int l, Type t) {
		return addNewArrayVar(n, l, t, Use.LOCAL);
	}

	// add a new integer array variable
	@Override
	public ArrayVariable addNewArrayVar(String n, int l, Type t,Use u) {
		//Variable Use information is lost
		ArrayVariable v = var.addNewArrayVar(n, t,u,l);
		completeCSP.addArrayVar(v);
		return v;
	}

	@Override
	public void addConstraint(LogicalExpression c) {
		completeCSP.addConstraint(c);
		linearCSP.addConstraint(c);
	}

	/**
	 * Adds the given negated post-condition expression to this validation system constraints. 
	 * A new constraint is built from the expression. For this, we separate the "for all"
	 * quantified expressions from the rest of the post-condition.
	 * 
	 * @param pc The post-condition expression.
	 */
	@Override
	public void addPostcond(LogicalExpression pc) {
		LogicalExpression validPC = parseNegatedClausePostcond(pc);
		// Parsing the postcondition may return an empty constraint (e.g. an non enumerable
		// quantifier)
		if (validPC != null) {
			completeCSP.addRemovable(validPC);
			if(validPC.isLinear()) {
				linearCSP.addConstraint(validPC);
			}
		}
	}
	
	@Override
	public void addPrecond(LogicalExpression c) {
		if (c != null) {
			completeCSP.addConstraint(c);
			if (c.isLinear()) {
				linearCSP.addConstraint(c, arrayElt);
			}
		}
	}
		
	/**
	 * Checks if the given constraint <pre>c</pre> is satisfiable in the linear CSP.
	 * If the given constraint is not linear, returns true.
	 * The constraint is not added to the CSPs.
	 */ 
	@Override
	public boolean tryDecision(LogicalExpression c) {
		boolean hasSolution = true;
		// try to add constraint into linear CSP
		if (c.isLinear()) {
			linearCSP.save();
			linearCSP.addConstraint(c);
			hasSolution = linearCSP.next();
			linearCSP.restore();
		}
		return hasSolution;
	}

//	// try to add co in the CSP and return fail code
//	// POSTCOND : if co is possible, it has been added into the CSP
//	// ATTENTION : les appels imbriqués linear.save et constSyst.save ne marchent pas
//	@Override
//	public SolvingCode tryAndAddDecision(Constraint co) {
//		SolvingCode failCode = SolvingCode.NOT_SOLVED;
//
//		// try to add constraint into linear CSP
//		if (co.linear()){
//			failCode = SolvingCode.NOT_FAIL_LINEAR;
//			linearCSP.save();
//			linearCSP.addConstraint(co, completeCSP);
//			// system is linear and has a solution
//			if (!linearCSP.next()) {
//				failCode=SolvingCode.FAIL_LINEAR;
//				linearCSP.restore();
//			}
//			else {
//				//The way save/restore works implies each save has its restore at the same level
//				linearCSP.restore();
//				linearCSP.addConstraint(co, completeCSP);				
//			}
//		}
//		return failCode;
//	}

	@Override
	protected void saveCSP() {
		completeCSP.save();
		linearCSP.save();
	}

	@Override
	protected void restoreCSP() {
		completeCSP.restore();
		linearCSP.restore();
	}
	
	/** 
	 * Solves the system.
	 * If either the linear or the boolean systems exist and have no solution,
	 * then the complete system has no solution.
	 * 
	 * @param result If a solution exists, it will be stored in this parameter.
	 * 
	 * @return <code>true</code> if a solution to this CSP exists; 
	 *         <code>false</code> otherwise.
	 */	
	@Override
	public boolean solve(Solution result) {
		Boolean foundSolution = true;
		
		//TODO : le système linéaire ne contient pas
		// les JMLResult!= donc inutile d'asseyer de résoudre le système
		// linéaire
		linearCSP.startSearch();
		if (!linearCSP.next()) {
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Linear CSP is inconsistent.");
			}
			result.setTime(linearCSP.getElapsedTime());
			foundSolution = false;
		}
		linearCSP.stopSearch();
		
		//Linear CSP had a solution.
		if (foundSolution) {
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Solving complete CSP...");
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println(completeCSP.toString());
				}
			}
			foundSolution = solveCompleteCSP(result);
		}
		
		return foundSolution;
	}
	
	/**
	 * Calls the concrete solver to find a solution to the current complete constraint system.
	 * 
	 * @param result A recipient for the solution if one is found.
	 * @return <code>true</code> if a solution was found; <code>false</code> otherwise.
	 */
	private boolean solveCompleteCSP(Solution result) {
		boolean foundSolution = false;
		completeCSP.startSearch();
		
		//This decides whether a second CSP will be necessary or not
		if(precondForAll != null || postcondForAll != null) {
			IntegerValidationCSP secondCompleteCSP = 
				new IloCPIntegerValidationCSP("CP Optimizer second complete CSP", this);
			IntegerValidationCSP secondLinearCSP = 
				new CplexIntegerValidationCSP("CPLEX second linear CSP", this); 
			
			//Add concrete variables for the quantified ones and get the constraint to add
			LogicalExpression expr = populateSecondCSPIntVar(secondCompleteCSP);
			populateSecondCSPIntVar(secondLinearCSP);
			
			//Not intuitive, but we have to start the loop
			boolean foundSecondarySolution = true;
			while(foundSecondarySolution && completeCSP.next()) {
				foundSecondarySolution = solveSecondCSP(secondCompleteCSP, 
														secondLinearCSP, 
														expr, 
														completeCSP.solution());
			}
			//A solution to the "global" CSP exists, if a solution exists for the first CSP (completeCSP)
			//AND no solution exists for the second CSP 
			foundSolution = !foundSecondarySolution;
			if(foundSolution) {
				result.copy(completeCSP.solution());
			}
			completeCSP.getStatus().moreSolve(secondCompleteCSP.getStatus().getSolve());
			completeCSP.getStatus().moreFail(secondCompleteCSP.getStatus().getFail());			
		}
		else {
			if(completeCSP.next()) {
				foundSolution = true;
				result.copy(completeCSP.solution());
			}
		}
		completeCSP.stopSearch();
		result.setTime(completeCSP.getElapsedTime());
		return foundSolution;
	}

	/**
	 * Adds to the given csp concrete variables needed for the non enumerable quantified "for all"
	 * expressions coming from the pre-condition and negated post-condition. It also returns the 
	 * expression that must be added to the csp.  
	 * 
	 * @param csp The constraint system to which adding the pre-condition and negated post-condition 
	 *            non enumerable "for all" expressions of this validation system.
	 * @return The expression to add as the a constraint in the given constraint system.
	 */
	private LogicalExpression populateSecondCSPIntVar(IntegerValidationCSP csp) {
		LogicalExpression expr;
		if(precondForAll != null) {
			expr = parseForAll(precondForAll, csp);
			if(postcondForAll != null)
				expr = new OrExpression(
						new NotExpression(expr), 
						new NotExpression(parseForAll(postcondForAll, csp)));
		}
		else {
			expr = new NotExpression(parseForAll(postcondForAll, csp));
		}
		return expr;
	}

	/**
	 * Parses the given "for all" expression, adding as necessary new concrete variables for the 
	 * contained quantified variables.
	 * The "for all" expression may contain another "for all" expression in its main condition (but not 
	 * in its bound condition).
	 * 
	 * @param jmlForAll A JML ForAll expression.
	 * @param csp The constraint system where to add the constraint built from the "for all" expression.
     *
	 * @return The expression to add as a constraint in the given constraint system.
	 */
	private static LogicalExpression parseForAll(JMLForAllExpression jmlForAll, IntegerValidationCSP csp) {
		csp.addVar(jmlForAll.index());
		if(jmlForAll.condition() instanceof JMLForAllExpression) {
			return new OrExpression(new NotExpression(jmlForAll.boundExpression()),
									parseForAll((JMLForAllExpression)jmlForAll.condition(), csp));
		}
		else {
			return new OrExpression(new NotExpression(jmlForAll.boundExpression()), 
									jmlForAll.condition());
		}
	}

	/**
	 * Calls the concrete solvers (linear and complete) to solve the second CSP containing the 
	 * constraint <code>constraintExpr</code> updated by the solution values <code>solValues<code>
	 * coming from the solving of the first CSP.
	 * 
	 * @param secondCompleteCSP A complete CSP where to solve <code>constraintExpr</code>. 
	 * @param secondLinearCSP A linear CSP where to solve <code>constraintExpr</code>. 
	 * @param constraintExpr The constraint to be solved in the second CSPs.
	 * @param solValues The solution values coming from the first CSP.
	 * @return <code>true</code> if a solution was found to the second CSP; <code>false</code> 
	 *         otherwise.
	 */
	private boolean solveSecondCSP(IntegerValidationCSP secondCompleteCSP,
								   IntegerValidationCSP secondLinearCSP,
								   LogicalExpression constraintExpr,
			  					   Solution solValues) 
	{
		boolean foundSolution = false;
		boolean foundLinearSolution = false;
		
		//We use the solution values of the complete CSP to update 
		//the constraint of the second CSP
		for(Variable solVar : solValues) {
			constraintExpr = (LogicalExpression)constraintExpr.substitute(
					solVar,
					AbstractExpression.createLiteral(solVar.domain()));
		}
		
		Map<ArrayVariable, Number[]> arrays = completeCSP.getArraysValues(); 
			
		if(constraintExpr.isLinear()) {
			secondLinearCSP.save();
			secondLinearCSP.add(arrays);			
			secondLinearCSP.addConstraint(constraintExpr);
			secondLinearCSP.startSearch();
			foundLinearSolution = secondLinearCSP.next();
			secondLinearCSP.restore();		
			secondLinearCSP.stopSearch();
		}
		else {
			foundLinearSolution = true;
		}
		
		if(foundLinearSolution) {
			secondCompleteCSP.save();		
			secondCompleteCSP.add(arrays);
			secondCompleteCSP.addConstraint(constraintExpr);
			secondCompleteCSP.startSearch();
			foundSolution = secondCompleteCSP.next();
			if(foundSolution) {
				String s = "Solution found in secondary CSP:\n" 
					+ secondCompleteCSP.solution()
					+ "with solution from primary CSP:\n"
					+ solValues;
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println(s);
				}
			}
			secondCompleteCSP.stopSearch();
			secondCompleteCSP.restore();
		}
		return foundSolution;
	}

	@Override
	public boolean isFeasible() {
		return tryDecision(new LogicalLiteral(true));
	}

	@Override
	public boolean solve() {
		return solve(new Solution());
	}

}
