package validation.system.xml;

import java.util.ArrayList;
import java.util.HashMap;

import validation.solution.Solution;
import validation.system.ValidationSystemCallbacks;
import validation.util.LoopStatus;
import validation.util.Type;
import validation.visitor.EnsureCase;
import expression.Expression;
import expression.logical.LogicalExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.SymbolTable;
import expression.variables.Variable;
import expression.variables.Variable.Use;

/**
 * Defines the callback methods that are called while parsing the XML translation of 
 * th program to be verified.
 * These methods must be implemented by any XML validation system.
 * 
 * @author Olivier Ponsini
 *
 */
public interface ValidationSystemXmlCallbacks extends ValidationSystemCallbacks {

	/**
	 * Sets a flag meaning the method body is currently being parsed.
	 * This means parsing of the JML method annotations and method signature 
	 * is finished.
	 */
	public void setParsingMethodBody();

	/**
	 * @return The value of the flag meaning the method body is currently being parsed.
	 */
	public boolean isParsingMethodBody();
		
	/**
	 * @return The isParsingPrecond flag.
	 */
	public boolean isParsingPrecond();

	/**
	 * @param isParsingPrecond The isParsingPrecond flag to set.
	 */
	public void setParsingPrecond(boolean isParsingPrecond);

	/**
	 * @return Status of all the analyzed program loops.
	 */
	public LoopStatus loops();
				
	/**
	 * @return The arrayElt hash map.
	 */
	public HashMap<String, ArrayElement> getArrayElt();

	/**
	 * Sets the analyzed method return value's type.
	 * @param t The type of the analyzed method return value.
	 */
	public void setReturnType(Type t);

	/**
	 * @return The type of the analyzed method return value.
	 */
	public Type returnType();
	
	/**
	 * @return <code>true</code> if the analyzed method return type is <code>void</code>; 
	 * 		   <code>false</code> otherwise.
	 */
	public boolean isVoid();
			
	/** 
	 * Set the <code>postcond</code> attribute to the expression contained in the 
	 * given JML "ensures" case. 
	 * The "for all" expressions must be grouped together and all the quantified 
	 * expressions in the post-condition must have uniquely named quantified variables.
	 */
	public void setPostcond(EnsureCase s);
	
	/** 
	 * Get the <code>postcond</code> attribute. 
	 * Depending on the chosen validation strategy, by cases or not, the postcondition
	 * may be the negation of one conjunct of the original postcond or the original 
	 * postcond itself, respectively.
	 */
	public LogicalExpression getPostcond();

	/**
	 * Adds the given negated post-condition expression to this validation system 
	 * constraints. 
	 * A new constraint is built from the expression. For this, we separate the "for all"
	 * quantified expressions from the rest of the post-condition.
	 * 
	 * @param pc The post-condition expression.
	 */
	public void addPostcond(LogicalExpression pc);
		
	/**
	 * Substitutes <code>expr</code> to <code>v</code> in the post-condition and 
	 * adds the latter to the constraints problems managed by this validation system.
	 * 
	 * @param v A variable to be substituted.
	 * @param expr The expression to substitute to the variable.
	 * 
	 * @return The updated postcondition.
	 */
	public LogicalExpression updatePostcond(Variable v, Expression expr);
	
	/** 
	 * If the method is <code>void</code>, postCond is added when the return 
	 * statement is reached.
	 * The post-condition is expressed in term of last SSA renamings of 
	 * variables.
	 * 
	 * @return The updated postcondition.
	 */
	public LogicalExpression updatePostcond();
		
	/**
	 * Adds the given expression as a pre-condition for this validation system.
	 * The expression is expected to be in Conjunctive Normal Form and the "for all" 
	 * expressions must be grouped together in a single clause. 
	 * Moreover, the quantified expressions in the pre-condition must have uniquely 
	 * named quantified variables.
	 * 
	 * @param expr The pre-condition expression.
	 */
	public void addCNFPrecond(LogicalExpression expr);

	/**
	 * Adds the given expression as an assumption for this validation system.
	 * The expression is expected to be in disjunctive normal form and the "for all" 
	 * expressions must be grouped together in a single clause. 
	 * Moreover, the quantified expressions must have uniquely named quantified variables.
	 * 
	 * @param expr a constraint expression.
	 */
	public void addDNFAssumption(LogicalExpression expr);
	
	/**
	 * Adds a new integer variable for the new SSA renaming of n.
	 */ 
	public Variable addVar(String n);
		
	/** 
	 * Adds a new variable that is not a method parameter.
	 */ 
	public Variable addNewVar(String n, Type t);

	/**
	 * Adds a new variable.
	 */ 
	public Variable addNewVar(String n, Type t, Use u);

    /**
     * @param n Name of a scalar variable.
     * @return <code>true</code> if a scalar variable of the given name exists in the 
     *         current symbol table; <code>false</code> otherwise.
     */
    public boolean containsVar(String n);

	/**
	 * @param n Name of a scalar variable.
	 * @return the variable associated to the current renaming
	 *         of variable with root name n.
	 */
	public Variable getVar(String n);
	
   /**
	 * Adds a new integer array variable for the new SSA renaming of n.
	 */ 
	public ArrayVariable addArrayVar(String n);

	/**
	 * Adds a new integer array variable that is not a method parameter.
	 */ 
	public ArrayVariable addNewArrayVar(String n, int l, Type t);

	/**
	 * Adds a new integer array variable.
	 */ 
	public ArrayVariable addNewArrayVar(String n, int l, Type t, Use u);
	
    /**
     * @param n Name of an array variable.
     * @return <code>true</code> if an array variable of the given name exists in the 
     *         current symbol table; <code>false</code> otherwise.
     */
    public boolean containsArrayVar(String n);

	/**
	 * @param n Name of an array variable.
	 * @return the array variable associated to the current renaming
	 *         of array variable with root name n.
	 */
	public ArrayVariable getArrayVar(String n);

	/**
	 * Increments the path counter.
	 */
	public void addPath();

	/**
	 * @return The number of path explored for the current "ensure case" of 
	 *          the post-condition.
	 */
	public int casePathNumber();

	/**
	 * Reset the path counter. 
	 */
	public void resetPath();

	/** 
	 * Adds a decision to the current path. 
	 */
	public void addDecision(LogicalExpression e);
	
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
	public boolean solve(Solution result);	
	
	/**
	 * @return The solution computed by the last call to 
	 *         {@link ValidationSystemCallbacks.checkPostcond}.
	 */
	public Solution getLastPostcondSolution();

	/**
	 * @return A String representation of all the decision taken on the current path.
	 */
	public String printDecision();

	/**
	 * @return A String representation of all the variables in the symbol table.
	 */
	public String printVariableValues();
	
	/**
	 * @return A String representation of this validation system status.
	 */
	public String printStatus();

	/**
	 * @return A String representation of the given solution.
	 */
	public void displaySolution(Solution sol);
			
	/**
 	 * Prepare CSP for analysis of the next piece of CFG:
 	 *  - Remove all constraints from the constraint store
 	 *    (Improvement would be to compute variables not modified in if or else branches and keep their associated constraints 
	 *     coming from statements before the if statement)
	 *  - Reset all Variables to SSA renaming 0 and set their domains to the values given as parameter.
	 *
	 * @param sol Contains variables with their root name
	 */
	public void reset(Solution sol);
	
	public SymbolTable copySymbolTable();
	
	public ArrayList<?> getConstraints();

	public HashMap<String, ?> copyVariableMap();

	public void setSymbolTable(SymbolTable savedSymbolTable);

	public void setConstraints(ArrayList<?> savedCtr);

	public void setVariableMap(HashMap<String, ?> savedVar);
	
}
