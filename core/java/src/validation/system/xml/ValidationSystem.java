package validation.system.xml;

import java.util.ArrayList;
import java.util.HashMap;

import solver.fplib.FplibByPiecesVS;
import solver.fplib.FplibNativeShaving2BVS;
import solver.fplib.FplibShaveResValidationSystem;
import solver.fplib.FplibShaving2BVS;
import solver.fplib.FplibValidationSystem;
import solver.glpk.GlpkValidationSystem;
import solver.glpk_fplib.GlpkFplibValidationSystem;
import solver.ibex.IbexValidationSystem;
import solver.ilocp.IloCPValidationSystem;
import solver.realpaver.xml.RealPaverByPiecesVS;
import solver.realpaver.xml.RealPaverShavingVS;
import solver.realpaver.xml.RealPaverXmlResultShaving;
import solver.realpaver.xml.RealPaverXmlValidation;
import solver.z3.xml.Z3ValidationSystem;
import validation.Validation;
import validation.util.Type;

import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.solution.ValidationStatus;
import validation.strategies.xml.RealPaverFplib;

import validation.util.DecisionPath;
import validation.util.LoopStatus;
import validation.util.Strategy;

import validation.visitor.EnsureCase;

import expression.Expression;
import expression.logical.LogicalExpression;

import expression.logical.OrExpression;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.AndExpression;
import expression.variables.ArrayElement;
//import expression.variables.ArraySymbolTable;
import expression.variables.ArrayVariable;
import expression.variables.SymbolTable;
import expression.variables.Variable;
import expression.variables.Variable.Use;


/** To store and manage constraint systems to validate on the fly 
 *  contains a CSP built from a specifcation part and a path through the program
*/

/** manage 3 ValidationCSP : 
 * - linear constraints
 * - boolean constraints
 * - complete system which contains all the constraints
 * 
 */

/** this is an abstract class, need to define abstract methods to
 * set the concrete CSP
 */ 

public abstract class ValidationSystem implements ValidationSystemXmlCallbacks {
		
	/**
	 * Creates the validation system containing the differents CSPs, according to 
	 * the given combination of solvers.
	 *  
	 * @param name The name of the validation system.
	 */
	public static ValidationSystem createXmlValidationSystem(String name) {
		switch(Validation.solverCombination) {
		case REAL_PAVER_FPLIB:
			return new RealPaverFplib(name);
		case REAL_PAVER:
			//Conformity validation or result domain shaving
			if (!Validation.shaving) {
				return new RealPaverXmlValidation(name);
			}
			else {
				if (Validation.shave3B) {
					return new RealPaverXmlResultShaving(name);
				}
				else if (Validation.piecewise) {
					return new RealPaverByPiecesVS(name);
				}
				else { // 2B shaving
					return new RealPaverShavingVS(name);
				}
			}
		case GLPK_FPLIB:
			return new GlpkFplibValidationSystem(name);
		case GLPK:
			return new GlpkValidationSystem(name);
		case FPLIB:
			//Conformity validation or domain shaving
			if (!Validation.shaving)
				return new FplibValidationSystem(name);
			else {
				if (Validation.shave3B)
					return new FplibShaveResValidationSystem(name);
				else if (Validation.piecewise) {
					return new FplibByPiecesVS(name);
				}
				else {  // 2B shaving
					if (Validation.native2BShaving)
						return new FplibNativeShaving2BVS(name);
					else
						return new FplibShaving2BVS(name);
				}
			}
		case Z3:
			return new Z3ValidationSystem(name);
		case IBEX_PAVER:
			return new IbexValidationSystem(name);
		case ILOG_CPLEX_CP_OPTIMIZER:
			return new IloCPValidationSystem(name);
		default: 
			System.err.println("The combination of solvers " + Validation.solverCombination 
					           + " is not available!\nGoing on with default combination.");
			return new IloCPValidationSystem(name);
		}
	}

	//----------------------------------------------------------
	// Attribute members
	//----------------------------------------------------------
	
	protected String name;
				
//	//	 abstract array variables
//	protected ArraySymbolTable arrayVar;
	
	//	 abstract variables
	protected SymbolTable var;
	
	// decisions taken in the current path
	private DecisionPath decision;
	
	// number of the currently explored path
	protected int totalPathNumber;
	// number of the currently explored path
	protected int casePathNumber;
	
	// IntegerElementVariables
	// list of the integer array elements that have been created
	// to be able to make the link between the IntegerArrayVariable
	// and the set of IntegerElementVariable it represents
	// useful to set IntegerElementVariable as constants
	protected HashMap<String,ArrayElement> arrayElt;
	// true during precondition parsing
	// use to set IntegerArrayElements as constant when 
	// the precondition sets constants values
	// (see BubleSort Mantovani)
	protected boolean isParsingPrecond;
	/**
	 * True when exploring the method body paths.
	 */
	protected boolean isParsingMethodBody;
	
	/**
	 * Contains all the "for all" quantified expressions from the precondition.
	 * These expressions cannot be added to the constraints as is, they will need specific
	 * treatment when reaching the end of a branch: for each solution of the main csp, a new csp will
	 * be built to solve these "for all" expressions.
	 * 
	 * @see #addCNFPrecond(Expression)
	 * @see #solveCompleteCSP(Solution)
	 */
	protected JMLForAllExpression precondForAll;
	/**
	 * Contains all the "for all" quantified expressions from the negation of the postcondition.
	 * These expressions cannot be added to the constraints as is, they will need specific
	 * treatment when reaching the end of a branch: for each solution of the main csp, a new csp will
	 * be built to solve these "for all" expressions.
	 * 
	 * @see #addPostcond(Expression)
	 * @see #solveCompleteCSP(Solution)
	 */
	protected JMLForAllExpression postcondForAll;
	
	// status for program loop unfolding 
	protected LoopStatus loop;
	
	// logical expressions of the specification part
	// currently validated
	// will be removed and reparsed at the end of the path
	// when JMLResult is known
	protected LogicalExpression postcond;
		
	// return type of the method
	protected Type returnType;
	
	// status of the validation
	protected ValidationStatus status;
	
	/**
	 * Stores the solution computed by the last call to 
	 * {@link #checkPostcond(LogicalExpression)}.
	 * It is initially empty.
	 */
	protected Solution lastPostcondSolution;
	
	public  ValidationSystem(String name) {
		this.name = name;
		decision = new DecisionPath();
		casePathNumber = 1;
		totalPathNumber = 0;
		loop = new LoopStatus();
		var = new SymbolTable();
//		arrayVar = new ArraySymbolTable();
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Creating solver(s):");
		}
		setCSP();
		arrayElt = new HashMap<String,ArrayElement>();
		isParsingPrecond = false;
		precondForAll = null;
		postcondForAll = null;
		lastPostcondSolution = new Solution();
	}
	
	/**
	 * Sets isParsingMethodBody flag to true and isParsingPrecond flag to false.
	 */
	@Override
	public void setParsingMethodBody() {
		this.isParsingPrecond = false;
		this.isParsingMethodBody = true;
	}
	
	/**
	 * @return The isParsingMethodBody flag.
	 */
	@Override
	public boolean isParsingMethodBody() {
		return isParsingMethodBody;
	}	

	/**
	 * @return The isParsingPrecond flag.
	 */
	@Override
	public boolean isParsingPrecond() {
		return isParsingPrecond;
	}

	/**
	 * @param isParsingPrecond The isParsingPrecond flag to set.
	 */
	@Override
	public void setParsingPrecond(boolean isParsingPrecond) {
		this.isParsingPrecond = isParsingPrecond;
	}

	@Override
	public LoopStatus loops() {
		return loop;
	}

	/**
	 * @return The arrayElt hash map.
	 */
	@Override
	public HashMap<String, ArrayElement> getArrayElt() {
		return arrayElt;
	}
	
	// mehtods to handle the return type
	////////////////////////////////////
	
	@Override
	public void setReturnType(Type t) {
		returnType = t;
	}

	@Override
	public Type returnType() {
		return returnType;
	}
	
	@Override
	public boolean isVoid(){
		return returnType == Type.VOID;
	}
	
	
	// ------------------------------------------------------
	// Dummy implementation of ValidationSystemCallbacks
	// to ensure compatibility with validation systems older 
	// than this interface.
	// ------------------------------------------------------
	
	@Override
	public boolean checkPostcond(LogicalExpression updatedPostcond) {
		boolean foundSolution;
		
		addPostcond(updatedPostcond);
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\n###########################");
			System.out.println("Return statement reached for path " + casePathNumber());
		}
		
		//Get the reference on lastPostcondSolution and reset it before
		//populating it by a call to solve. The call to solve updates directly 
		//the lastPostcondSolution attribute.
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
	public boolean checkAssertion(LogicalExpression assertion, String message) {
		Solution sol = new Solution();
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Verifying assertion: " + assertion);
		}
        //constSyst.addConstraint(Strategy.negate(assertion));
        //An assertion is treated as a post-condition, except that we will keep it in
        //the constraint system if it is a true assertion. This also implies that an assertion 
        //must not contain "and" connectives with quantified expressions as arguments
        //(conjunction in assertions can simply translate into several assertions).
        addPostcond(Strategy.negateCase(assertion));
        if(solve(sol)) { //CSP has a solution
    		//Notifier à l'IHM la solution result
        	displaySolution(sol);
    		if (VerboseLevel.QUIET.mayPrint()) {
    			if (VerboseLevel.TERSE.mayPrint()) {
    				System.out.println("Assertion is violated!");
    				if (VerboseLevel.VERBOSE.mayPrint()) {
    					System.out.println(
    						"Current elapsed time = " 
    						+ (System.currentTimeMillis() - validation.ValidationLauncher.startTime())/1000);
    				}
    			}
    			System.out.println(message);
    		}
        	return false;
        }
        else { //CSP has no solution
    		//Notifier à l'IHM la solution result
        	displaySolution(sol);
    		if (VerboseLevel.TERSE.mayPrint()) {
    			System.out.println("Assertion is verified!");
    			if (VerboseLevel.VERBOSE.mayPrint()) {
    				System.out.println(
    						"Current elapsed time = " 
    						+ (System.currentTimeMillis() - validation.ValidationLauncher.startTime())/1000);
    			}
    		}
        	return true;
       }
	}
	
	@Override
	public void validate() {
		//Does nothing...
	}

	// ------------------------------------------------------
		
	/** 
	 * Set the <code>postcond</code> attribute to the expression contained in the 
	 * given JML "ensures" case. 
	 * The "for all" expressions must be grouped together and all the quantified 
	 * expressions in the post-condition must have uniquely named quantified variables.
	 */
	@Override
	public void setPostcond(EnsureCase s) {
		postcond = s.exp;
	}

	/** 
	 * Get the <code>postcond</code> attribute. 
	 * Depending on the chosen validation strategy, by cases or not, the postcondition
	 * may be the negation of one conjunct of the original postcond or the original 
	 * postcond itself, respectively.
	 */
	@Override
	public LogicalExpression getPostcond() {
		return postcond;
	}
			
	/**
	 * Substitutes <code>expr</code> to <code>v</code> in the post-condition and adds the latter 
	 * to the constraints problems managed by this validation system.
	 * 
	 * @param v A variable to be substituted.
	 * @param expr The expression to substitute to the variable.
	 */
	@Override
	public LogicalExpression updatePostcond(Variable v, Expression expr) {
		return (LogicalExpression)postcond.substitute(v, expr);
	}
	
	/** for void methods
	 * if the method is void, postCond is added when the return 
	 * statement is reached
	 * the post-condition is expressed in term of last SSA renamings of 
	 * variables
	 */
	@Override
	public LogicalExpression updatePostcond() {
		return (LogicalExpression)postcond.computeLastRenaming(var);
	}
	
	@Override
	public Solution getLastPostcondSolution() {
		return this.lastPostcondSolution;
	}
	
//	// make the link between specification (JMLResult) and program (javaResult)
//	public void linkResult(Variable v){
//		// important de mettre le linéaire d'abord car sinon 
//		// la contrainte élément du complet fait que l'on ne peut pas réduire l'indice
//		Variable jmlResult = var.get("JMLResult");
//		Expression subst = postCond.substitute(jmlResult, v);
//		Constraint c = new Constraint(subst);
//		if (c.linear()) {
//			linearCSP.addConstraint(c,completeCSP);
//		}
//		else {
//			addBoolConstraint((LogicalExpression)subst);
//		}
//		completeCSP.addConstraint(c);
//	}	
		
	/**
	 * Adds the given expression as a pre-condition for this validation system.
	 * The expression is expected to be in Conjunctive Normal Form and the "for all" expressions must be 
	 * grouped together in a single clause. 
	 * Moreover, the quantified expressions in the pre-condition must have uniquely named quantified 
	 * variables.
	 * 
	 * @param expr The pre-condition expression.
	 */
	@Override
	public void addCNFPrecond(LogicalExpression expr) {
		if(expr instanceof AndExpression) {
			AndExpression and = (AndExpression)expr;
			addCNFPrecond(and.arg1());
			addCNFPrecond(and.arg2());
		}
		else {
			addPrecond(parseClausePrecondForAll(expr, true));
		}
	}

	/**
	 * Adds the given expression as an assertion for this validation system.
	 * The expression is expected to be in disjunctive normal form and the "for all" expressions must 
	 * be grouped together in a single clause. 
	 * Moreover, the quantified expressions must have uniquely named quantified variables.
	 * 
	 * @param expr The pre-condition expression.
	 */
	@Override
	public void addDNFAssumption(LogicalExpression expr) {
		addConstraint(parseClausePrecondForAll(expr, false));
	}
	
	/**
	 * Parses the given expression as a clause for this validation system.
	 * The expression is expected to be a disjunction of predicates and quantified expressions.
	 * The "for all" expressions must be grouped together. 
	 * Moreover, the quantified expressions must have uniquely named quantified variables.
	 * Quantified expressions will be expanded if enumerable, and non enumerable "for all" expressions
	 * are removed from the returned expression and stored in {@link #precondForAll} member for
	 * subsequent treatment.
	 *  
	 * @param expr The clause expression.
	 * @param precond Specifies whether we are dealing with the precond, and we should then 
	 *                keep a non enumerable forall in the {@link #precondForAll} member, or we are dealing
	 *                with an assertion and we should not overwrite {@link #precondForAll}. 
	 * @return The given expression where enumerable quantified expressions are expanded and non 
	 *         enumerable "for all" are removed.
	 */
	protected LogicalExpression parseClausePrecondForAll(LogicalExpression expr, boolean precond) {
		if(expr instanceof OrExpression) {
			OrExpression or =(OrExpression)expr;
			LogicalExpression e1 = parseClausePrecondForAll(or.arg1(), precond);
			LogicalExpression e2 = parseClausePrecondForAll(or.arg2(), precond);
			if(e1 != null) {
				if(e2 != null)
					return new OrExpression(e1, e2);
				else
					return e1;
			}
			else {
				return e2;
			}
		}
		else if(expr instanceof JMLExistExpression) {
			JMLExistExpression jmlExist = (JMLExistExpression)expr;
			
			if(jmlExist.enumeration() != null) {
				return jmlExist;
			}
			else {
				return new AndExpression(jmlExist.boundExpression(), jmlExist.condition());
			}
		}
		else if(expr instanceof JMLForAllExpression) {
			JMLForAllExpression jmlForAll = (JMLForAllExpression)expr;
			
			if(jmlForAll.enumeration() != null) { 
				return jmlForAll;
			}
			else {
				if (precond)
					this.precondForAll = jmlForAll;
				return null;
			}
		}
		else {
			return expr;
		}
	}

	/**
	 * Parses the given expression as a negated post-condition case for this validation system.
	 * The expression is expected to be a conjunction of predicates and quantified expressions.
	 * The "for all" expressions must be grouped together. 
	 * Moreover, the quantified expressions must have uniquely named quantified variables.
	 * Quantified expressions will be expanded if enumerable, and non enumerable "for all" expressions
	 * are removed from the returned expression and stored in {@link #postcondForAll} member for
	 * subsequent treatment.
	 *  
	 * @param expr The negated post-condition case expression.
	 * @return The given expression where enumerable quantified expressions are expanded and non 
	 *         enumerable "for all" are removed.
	 */
	protected LogicalExpression parseNegatedClausePostcond(LogicalExpression expr) {
		if(expr instanceof AndExpression) {
			AndExpression and =(AndExpression)expr;
			LogicalExpression e1 = parseNegatedClausePostcond(and.arg1());
			LogicalExpression e2 = parseNegatedClausePostcond(and.arg2());
			if(e1 != null) {
				if(e2 != null)
					return new AndExpression(e1, e2);
				else
					return e1;
			}
			else {
				return e2;
			}
		}
		else if(expr instanceof JMLExistExpression) {
			JMLExistExpression jmlExist = (JMLExistExpression)expr;
			
			if(jmlExist.enumeration() != null) {
				return jmlExist;
			}
			else {
				return new AndExpression(jmlExist.boundExpression(), jmlExist.condition());
			}
		}
		else if(expr instanceof JMLForAllExpression) {
			JMLForAllExpression jmlForAll = (JMLForAllExpression)expr;
			
			if(jmlForAll.enumeration() != null) { 
				return jmlForAll;
			}
			else {
				//TODO: Take into account ForAll expressions that may have been left by JML assertions
				//in the code.
				this.postcondForAll = jmlForAll;
				return null;
			}
		}
		else {
			return expr;
		}
	}

	protected abstract void addPrecond(LogicalExpression expr);
		
	/** add a decision to the current path */
	@Override
	public void addDecision(LogicalExpression e){
		decision.addDecision(e);
	}
	
	// methods to add variables
	///////////////////////////	
			
	// add a new variable that is local to the method 
	@Override
	public Variable addNewVar(String n, Type t) {
			return addNewVar(n, t, Use.LOCAL);
	}

	@Override
	public boolean containsVar(String n){
		return var.containsKey(n);
	}
	
	@Override
	public boolean containsArrayVar(String n){
		return var.containsKey(n);
	}

	@Override
	public void addPath() {
		totalPathNumber++;
		casePathNumber++;
	}
				
	/** returns the variable associated to the current renaming
	 * of variable with root name n */
	@Override
	public Variable getVar(String n){
		return var.get(n);
	}
	
	/** returns the array variable associated to the current renaming
	 * of variable with root name n */
	@Override
	public ArrayVariable getArrayVar(String n){
		return var.getArray(n);
	}

	@Override
	public int casePathNumber(){
		return casePathNumber;
	}
	
	@Override
	public String printDecision() {
		return decision.toString();
	}

	@Override
	public String printVariableValues(){
		return var.variableValues();
	}
	
	@Override
	public String printStatus() {
		StringBuilder s = new StringBuilder("###########################\n\n");
		s.append("Solving information for program ");
		s.append(name);
		s.append("\n----------------------\n");
		s.append(status.toString());
		s.append(totalPathNumber);
		s.append(" feasible paths were explored during this validation\n");
		s.append("\nTotal resolution time : ");
		s.append(status.getTotalTime());
		s.append("s\n###########################");
		return s.toString();
	}

	@Override
	public void displaySolution(Solution sol) {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println(sol.toString());
		}
	}

	@Override
	public void resetPath(){
		casePathNumber = 1;
	}

	//-----------------------------------
	// methods to manage backtrak
	////////////////////////////
	
	/** restore the current state of the CSPs
	 */
	@Override
	public void restore(){
		restoreCSP();
		decision.restore();
//		loop.restore();
		var.restore();
//		arrayVar.restore();
	}
	
	protected abstract void restoreCSP();
			
	/** save the current state of the CSPs
	 */
	@Override
	public void save(){
		saveCSP();
		decision.save();
//		loop.save();
		var.save();
//		arrayVar.save();
	}
	
	protected abstract void saveCSP();
		
	/** 
	 * Defines concrete real and/or integer and/or boolean CSPs.
	 * Must be implemented in child classes
	 */
	protected abstract void setCSP();
	
	@Override
	public void reset(Solution sol) {
		System.err.println("Error: unimplemented reset method in ValidationSystem!");
		System.exit(-1);
	}

	@Override
	public SymbolTable copySymbolTable() {
		return (SymbolTable)var.copy();
	}
	
	@Override
	public ArrayList<?> getConstraints() {
		System.err.println("Error: unimplemented getConstraints method in ValidationSystem!");
		System.exit(-1);
		return null;
	}

	@Override
	public HashMap<String, ?> copyVariableMap() {
		System.err.println("Error: unimplemented copyVariableMap method in ValidationSystem!");
		System.exit(-1);
		return null;
	}
	
	@Override
	public void setConstraints(ArrayList<?> savedCtr) {
		System.err.println("Error: unimplemented setConstraints method in ValidationSystem!");
		System.exit(-1);
	}


	@Override
	public void setSymbolTable(SymbolTable savedSymbolTable) {
		var.setSymbols(savedSymbolTable);
	}


	@Override
	public void setVariableMap(HashMap<String, ?> savedVar) {
		System.err.println("Error: unimplemented setVariableMap method in ValidationSystem!");
		System.exit(-1);
	}

}
