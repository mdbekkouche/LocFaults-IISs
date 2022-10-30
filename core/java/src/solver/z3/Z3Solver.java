package solver.z3;

import solver.ConcreteSolver;
import validation.Validation;
import validation.util.Type;
import exception.VariableValueException;

/**
 * The z3 concrete solver.
 * For the moment, we do not handle several instances of the concrete z3 SMT solver.
 * 
 * This class defines native methods to access the z3 C library.
 * These methods are part of the <code>libjni_z3.so</code> library.
 * 
 * @author Olivier Ponsini
 *
 */
public class Z3Solver implements ConcreteSolver {

    static {
    	System.loadLibrary("jni_z3");
    }
    
    /**
     * Native Z3 context pointer converted to a long integer.
     * Contains a stack of CSP models.
     */
    private long z3_ctx;
    
    /**
     * Native Z3 model pointer converted to a long integer.
     */
    private long z3_model;
    
    /**
     * Constructs the z3 solver and creates an initial context.
     */
    public Z3Solver() {
    	//Creates the z3 context
    	z3_ctx = z3Init();
    }
 
    @Override
    public String toString() {
    	return "Microsoft Z3 SMT " + z3Version();
    }
    
    /** 
     * Ensures that IBEX resources will be freed if this object is ever garbage-collected
     * before a clean call to {@link #stop()}.
     */
    public void finalize() {
    	z3Release(z3_ctx);
    }

	/**
	 * Explores the solution of the CSP over the variables domains.
	 * 
	 * @return <code>true</code> if a solution to the current CSP has been found; 
	 *         <code>false</code> otherwise.
	 */
	public boolean next() {
		z3_model = z3Check(z3_ctx);
		if (z3_model != 0)
			return true;
		else
			return false;
	}

	/**
	 * Does nothing.
	 */
	public void startSearch() {
	}

	/**
	 * Frees the resources allocated for the model (the counter-example) 
	 * found for the CSP if it exists.
	 * Should be called before modifying the CSP and starting a new search, 
	 * but should not be called before the counter-example has been stored on the CPBPV side.
	 */
	public void stopSearch() {
		z3DelModel(z3_ctx, z3_model);
		//Set the model to "null"
		z3_model = 0;
	}
	
	public long z3MkOp(int op, long lhs, long rhs) {
		return z3MkOp(z3_ctx, op, lhs, rhs);
	}

    public long z3GetArrayVariable(long arrayAst, long indexAst) {
    	return z3GetArrayVariable(z3_ctx, arrayAst, indexAst);
    }

    public long mkVar(String name, Type t) {
		if (t==Type.INT)
			return z3MkIntVar(z3_ctx,
							  name, 
							  Validation.INTEGER_MIN_BOUND, 
							  Validation.INTEGER_MAX_BOUND); 
		else {	
			long l = z3MkVar(z3_ctx, name, t.ordinal());
			return l;
		}
	}

	public long mkArray(String name, Type t, int length) {
		if (t==Type.INT)
			return z3MkIntArray(z3_ctx,
								name, 
								length,
							  	Validation.INTEGER_MIN_BOUND, 
							  	Validation.INTEGER_MAX_BOUND); 
		else	
			return z3MkArray(z3_ctx, name, t.ordinal());
	}

    public long z3MkIfThenElse(long condAst, long thenAst, long elseAst) {
    	return z3MkIfThenElse(z3_ctx, condAst, thenAst, elseAst);
    }
    
    /**
	 * The interpretation on reals may loose precision, as the string representation from z3
	 * may be an exact fraction which this method converts into a floating number (a Java Double).
	 */  
	public Object getValue(long var, Type t) 
		throws VariableValueException 
	{
		return Type.getValueFromString(z3GetValue(z3_ctx, z3_model, var), t);
	}

	/**
	 * The interpretation on reals may loose precision, as the string representation from z3
	 * may be an exact fraction which this method converts into a floating number (a Java Double).
	 */  
	public Object getValue(long array, int index, Type t) 
		throws VariableValueException 
	{
		return Type.getValueFromString(z3GetArrayValue(z3_ctx, z3_model, array, index), t);
	}

	public long z3MkDistinct(long[] elts) {
		return z3MkDistinct(z3_ctx, elts);
	}

    public long z3MkForAll(long[] boundedVars, long body) {
    	return z3MkForAll(z3_ctx, boundedVars, body);
    }

    public long z3MkExist(long[] boundedVars, long body) {
    	return z3MkExist(z3_ctx, boundedVars, body);
    }

	public long z3MkBoolCst(boolean cst) {
		return z3MkBoolCst(z3_ctx, cst);
	}

	public long z3MkIntCst(int cst) {
		return z3MkIntCst(z3_ctx, cst);
	}

	public long z3MkRealCst(String cst) {
		return z3MkRealCst(z3_ctx, cst);
	}

	public void z3PushContext() {
		z3PushContext(z3_ctx);
	}

	public void z3PopContext() {
		z3PopContext(z3_ctx);
	}
	
	public void z3AddCtr(long ctr) {
		z3AddCtr(z3_ctx, ctr);
	}

	public String z3AstToString(long ast) {
		return z3AstToString(z3_ctx, ast);
	}
	
	//--------------------------------------------------------------
	// NATIVE METHODS
	//--------------------------------------------------------------
	
	/**
	 * This method should always be called first. It creates the solver context 
	 * where to store variables and constraints.
	 */
	private static native long z3Init();
	/**
	 * Releases the memory resources allocated for the solver.
	 */ 
    private static native void z3Release(long ctx);
    /**
     * This method explores the search space for solutions to the CSP. 
     */
    private static native long z3Check(long ctx);
    /**
     * This method releases the z3 model built when searching for a CSP solution.
     */
    private static native void z3DelModel(long ctx, long model);
	/**
	 * @return the Z3 version number as a String.
	 */
	public static native String z3Version();
	/**
	 * Saves the concrete solver current context for future backtrack.
	 */
	static native void z3PushContext(long ctx);
	/**
	 * Restores a previously saved concrete solver context.
	 */
	static native void z3PopContext(long ctx);	
    /**
     * Adds a variable to the context.   
     *  
     * @param name Name of the variable we want to add to the solver context.
     * @param type Represents the type of the variable in the Java source program. 
     *             The mapping between values and types depends on the order of the types in the 
     *             Java enumeration {@link validation.util.Type} (starting from 0).
     *             
     * @return A pointer (converted to a long integer) to the z3_ast element created 
     *         by z3 for this variable.
     */  
	static native long z3MkVar(long ctx, String name, int type);
    /**
     * Adds an integer variable to the context and sets its domain between <code>min</code>
     * and <code>max</code>. This is done by adding constraints <code>name >= min</code> and
     * <code>name <= max</code>
     *  
     * @param name Name of the variable we want to add to the solver context.
     * @param min Variables's domain min bound.
     * @param min Variables's domain max bound.
     *             
     * @return A pointer (converted to a long integer) to the z3_ast element created 
     *         by z3 for this variable.
     */  
	static native long z3MkIntVar(long ctx, String name, int min, int max);
    /**
     * Adds an array to the context.
     * 
     * Z3 arrays are not bounded: they have no length.
     *  
     * @param name Name of the array we want to add to the solver context.
     * @param type Represents the type of the array elements in the Java source program. 
     *             The mapping between values and types depends on the order of the types in the 
     *             Java enumeration {@link validation.util.Type} (starting from 0).
     *             
     * @return A pointer (converted to a long integer) to the z3_ast element created 
     *         by z3 for this array.
     */  
	static native long z3MkArray(long ctx, String name, int type);
    /**
     * Adds an array of integers to the context and sets the array elements domain between 
     * <code>min</code> and <code>max</code>. This is done by adding constraints 
     * <code>name[i] >= min</code> and <code>name[i] <= max</code>
     *  
     * Z3 arrays are not bounded: they have no length, but here we need it to set the domain
     * constraints on each array's element.
     *  
     * @param name Name of the array we want to add to the solver context.
     * @param min Variables's domain min bound.
     * @param min Variables's domain max bound..
      *             
     * @return A pointer (converted to a long integer) to the z3_ast element created 
     *         by z3 for this array.
     */  
	static native long z3MkIntArray(long ctx, String name, int length, int min, int max);
	/**
     * Creates a concrete boolean constant. 
     *  
     * @param cst The constant value.
     *             
     * @return A pointer (converted to a long integer) to the z3_ast element created 
     *         by z3 for this constant.
     */  
	static native long z3MkBoolCst(long ctx, boolean cst);
    /**
     * Creates a concrete integer constant. 
     *  
     * @param cst The constant value.
     *             
     * @return A pointer (converted to a long integer) to the z3_ast element created 
     *         by z3 for this constant.
     */  
	static native long z3MkIntCst(long ctx, int cst);
    /**
     * Creates a concrete real constant. 
     *  
     * @param cst The constant value.
     *             
     * @return A pointer (converted to a long integer) to the z3_ast element created 
     *         by z3 for this constant.
     */  
	static native long z3MkRealCst(long ctx, String cst);
	/**
	 * Creates a new expression corresponding to operator <code>op</code>.
	 * 
	 * @param op The operator code as defined in {@link validation.util.OpCode}.
	 * @param lhs The operator's first argument as z3_ast pointer.
	 * @param rhs The operator's second argument as z3_ast pointer. May be null 
	 *            (<code>== 0</code>) in case of a unary operator.
	 * 
	 * @return A new expression built from the operator and its arguments.
	 */
	static native long z3MkOp(long ctx, int op, long lhs, long rhs);
	/**
	 * Adds a concrete constraint to the solver context.
	 * 
	 * @param ctr The concrete constraint to add to the concrete solver current context.
	 */
	static native void z3AddCtr(long ctx, long ctr);
	/**
	 * Returns a string representation of this z3_ast node.
	 * 
	 * @param ast A z3_ast node.
	 * @return A string representation of the given z3_ast node.
	 */
	static native String z3AstToString(long ctx, long ast);
	/**
     * This method returns the string representation of the value of a z3 variable 
     * in the current model. If the value could not be found, it returns an error message
     * that won't be parsed as a number and will raise an exception if tried so.
     */
    static native String z3GetValue(long ctx, long model, long z3VarAst);
    /**
     * This method returns the string representation of the value of a z3 array element
     * (z3ArrayAst[index]) in the current model. 
     * If the value could not be found, it returns an error message
     * that won't be parsed as a number and will raise an exception if tried so.
     */
    static native String z3GetArrayValue(long ctx, long model, long z3ArrayAst, int index);
    /**
     * This method returns the z3 element corresponding to <code>arrayAst[indexAst]</code>.
     * It is a <code>select</code> expression similar to the <code>element</code> constraint.
     */
    static native long z3GetArrayVariable(long ctx, long arrayAst, long indexAst);
    /**
     * This method returns the z3 element corresponding to an <code>if-then-else</code> constraint.
     */
    static native long z3MkIfThenElse(long ctx, long condAst, long thenAst, long elseAst);
    /**
     * This method returns the z3 distinct constraint on all the concrete variables in 
     * <code>elts</code>.
     */
    static native long z3MkDistinct(long ctx, long[] elts);
    /**
     * This builds a z3 ForAll quantifier.
     *  
     * @param boundedVars An array containing all the z3 variables bounded in the 
     *        quantified formula.
     * @param body The quantified formula.
     * 
     * @return A concrete z3 ForAll quantifier (a C++ pointer casted into a long).
     */
    static native long z3MkForAll(long ctx, long[] boundedVars, long body);
    /**
     * This builds a z3 Exist quantifier.
     *  
     * @param boundedVars An array containing all the z3 variables bounded in the 
     *        quantified formula.
     * @param body The quantified formula.
     * 
     * @return A concrete z3 Exist quantifier (a C++ pointer casted into a long).
     */
    static native long z3MkExist(long ctx, long[] boundedVars, long body);
}
