package solver.fplib;

import solver.ConcreteSolver;
import validation.Validation;
import validation.util.Type;
import exception.VariableValueException;
import expression.variables.DomainBox;
import expression.variables.Variable;

/**
 * The fplib concrete solver.
 * This class defines native methods to access the fplib C++ library.
 * These methods are part of the <code>libjni_fplib.so</code> library.
 * 
 * Here is the typical usage sequence:
 * <ol>
 *   <li>First, a concrete CSP must be created ({@link #init()}),</li>
 *   <li>then variables are added to this csp ({@link #addVar(long, String, Type)});</li>
 *   <li>Second, a model must be created from the previous concrete CSP ({@link #fplibCreateModel(long)}),</li>
 *   <li>then constraints are added to this model ({@link #fplibAddCtr(long, long)});</li>
 *   <li>Third, the model must be extracted and an Fplib's solver created using {@link #createSolver(long)};</li>
 *   <li>Fourth, at least a first domain box must be pushed to the solver:
 *   <ul>
 *     <li>from the current variable domains by just calling {@link #push()}, or</li> 
 *     <li>from a box modified from the current one by calling {@link #fplibSetVarBounds(long, double, double)}
 *         (taking care to only RESTRICT existing domains) and then {@link #push()}.</li>
 *   </ul></li>
 * </ol>
 * 
 * @author Olivier Ponsini
 *
 */
public class FplibSolver implements ConcreteSolver {

    static {
    	System.loadLibrary("jni_fplib");
    }

    /**
     * The pointer to the fplib solver converted to a long int.
     * Initialized to "NULL".
     */
    private long solver = 0L;
    /**
     * This marks if {@link #next()} is called for the first time.
     */
    private boolean firstNext = true;
    
    @Override
    public String toString() {
    	return "Claude Michel's Fplib v-070111";
    }

    /** 
     * Ensures that Fplib resources will be freed if this object is ever garbage-collected
     * before a clean call to {@link #stop()}.
     */
    public void finalize() {
		fplibRelease(solver);
    }

	/**
	 * Explores the solution of the CSP over the variables domains.
	 * No modification to the CSP should intervene between 
	 * successive calls to this method. If this occurs, 
	 * call {@link #release()} and build the concrete CSP again.
	 * 
	 * When called for the first time, this method will {@link #push()} the current variable state; 
	 * for the next calls it will use the states available in Fplib's stack if any. 
	 * 
	 */
	public boolean next() {
		//If this is the first call to next, we push the current variable domains.
		//Otherwise, the previous call to fplibNext has taken care of the stack.
		if (firstNext) {
			firstNext = false;
			fplibPush(solver);
		}
		return fplibNext(solver);
	}

	/**
	 */
	public void startSearch() {
		//Nothing to be done.
	}
		
	/**
	 */
	public void stopSearch() {
		//Nothing to be done.
	}

	public void release() {
		fplibRelease(solver);
		//We also set the pointers to "null" to avoid double free
		solver = 0L;
		firstNext = true;
	}
	
	/**
	 * Releases the current concrete solver (and dependences) and 
	 * creates a new empty concrete CSP.
	 * @return A pointer to an empty Fplib CSP.
	 */
	public long init() {
		//Discard previous CSP in case it has not already been done
		stopSearch();
		//Creates the solver environment.
		return fplibInit(Validation.float_rnd.ordinal());	
	}

	/**
	 * Creates the Fplib solver based on the given model.
	 * 
	 * When the solver is created, a first box based on the current variable domains
	 * is also pushed. We pop it (since it is usually made up of free domains) and
	 * requires the user to take care of pushing its own box or using methods like {@link #next()} that 
	 * takes care of it.
	 * 
	 * @param model
	 */
	public void createSolver(long model) {
		solver = fplibCreateSolver(model);
		fplibPop(solver);
	}
	
	public long addVar(long csp, String name, Type t) {
		if (t==Type.INT)
			return fplibAddBoundedVar(
						csp,
						name, 
						t.ordinal(),
						Validation.INTEGER_MIN_BOUND,
						Validation.INTEGER_MAX_BOUND); 
		else	
			return fplibAddVar(csp, name, t.ordinal());
	}

	public long addVar(long csp, Variable v) {
		if (v.domain() == null) {
			return addVar(csp, v.name(), v.type());
		}
		else {
			return fplibAddBoundedVar(
						csp,
						v.name(), 
						v.type().ordinal(),
						v.domain().minValue().doubleValue(),
						v.domain().maxValue().doubleValue()); 
		}
	}
	
	public static Number infValue(long var, Type varType) throws VariableValueException {
		switch(varType) {
		case INT:
			return new Integer(fplibInfValueInt(var));
		case FLOAT:
			return new Float(fplibInfValueFloat(var));
		case DOUBLE:
			return new Double(fplibInfValueDouble(var));
		default:
			throw new VariableValueException("unused type (" + varType + ") for a variable!"); 
		}
	}

	public static Number supValue(long var, Type varType) throws VariableValueException {
		switch(varType) {
		case INT:
			return new Integer(fplibSupValueInt(var));
		case FLOAT:
			return new Float(fplibSupValueFloat(var));
		case DOUBLE:
			return new Double(fplibSupValueDouble(var));
		default:
			throw new VariableValueException("unused type (" + varType + ") for a variable!"); 
		}		
	}
    
	public boolean kB(int k, double kB_percent, double twoB_percent) {
		return fplibKB(solver, k, kB_percent, twoB_percent);
	}

	public boolean twoB(double twoB_percent) {
		return fplib2B(solver, twoB_percent);
	}
 
	public void push() {
		fplibPush(solver);
	}
    
	public void pop() {
		fplibPop(solver);
	}
	
	public boolean shave(double twoBpercent, double intervalMinSize, double intervalMinRatio) {
		return fplibShave(this.solver, twoBpercent, intervalMinSize, intervalMinRatio);
	}

	//--------------------------------------------------------------
	// NATIVE METHODS
	//--------------------------------------------------------------

	/**
	 * This method creates the solver environment 
	 * where to store variables and constraints and sets the rounding mode.
	 */
	public static native long fplibInit(int rndMode);
	/**
	 * Releases the memory resources allocated for the solver.
	 */ 
    public static native void fplibRelease(long solver);
    /**
     * This method explores the search space for a solution to the CSP.
     * 
     * @return <code>True</code> if a solution has been found; <code>False</code> otherwise.
     */
    public static native boolean fplibNext(long solver);
    /**
     * Filters variable domains according to (strong ?) k-consistency.
     * Taking <pre>k=#variables</pre> should give for each variable the min and 
     * max values participating in a solution of the CSP.
     * 
     * @param k number of variables considered for consistency checking.
     * @param kB_percent Permet de stopper le filtrage par kB pour <pre>k>2</pre>
     *                   dès que les réductions obtenues sur les domaines sont inférieures 
     *                   au pourcentage donné. 
     *                   Une valeur habituelle est 5 (pour cent). La valeur peut-être 0
     *                   auquel cas le filtrage continue tant qu'une réduction, aussi 
     *                   infime soit-elle, est effectuée.
     * @param twoB_percent Permet de stopper le filtrage par 2B dès que les réductions 
     *                     obtenues sur les domaines sont inférieures au pourcentage donné. 
     *                     Une valeur habituelle est 5 (pour cent). La valeur peut-être 0
     *                     auquel cas le filtrage continue tant qu'une réduction, aussi 
     *                     infime soit-elle, est effectuée.
     * 
     * @return Voir avec Claude Michel pour la valeur retournée.
     */
    public static native boolean fplibKB(long solver, int k, double kB_percent, double twoB_percent);
    /**
     * Filters variable domains according to 2B-consistency.
     * 
     * @param twoB_percent Permet de stopper le filtrage par 2B dès que les réductions 
     *                     obtenues sur les domaines sont inférieures au pourcentage donné. 
     *                     Une valeur habituelle est 5 (pour cent). La valeur peut-être 0
     *                     auquel cas le filtrage continue tant qu'une réduction, aussi 
     *                     infime soit-elle, est effectuée.
     * 
     * @return Voir avec Claude Michel pour la valeur retournée.
     */
    public static native boolean fplib2B(long solver, double twoB_percent);
    /**
     * This method should be called after adding all variables to the CSP and before 
     * adding constraints.
     */ 
    public static native long fplibCreateModel(long csp);
    /**
     * Extracts the model and creates the solver.
     * 
     * This method should be called after adding all constraints to the CSP and before 
     * calling any filtering or solving algorithm.
     */ 
    public static native long fplibCreateSolver(long model);
    /**
     * Pushes the current solver's state (including variable domains) on an internal stack.
     */
    public static native void fplibPush(long solver);
    /**
     * Restores a previously saved solver's state (including variable domains) 
     * from an internal stack.
     */    
    public static native void fplibPop(long solver);
   /**
     * Sets the domain bounds of the given variable.
     * This must be used after a model has been created and extracted 
     * (see {@link #startSearch()} and {@link #startSearch(DomainBox)}).
     * Be careful to only set bounds already included in the current variable domain.
     * 
     * @param var the concrete Fplib variable (a pointer to it casted to a long).
     * @param min Variable's domain minimum value.
     * @param max Variable's domain maximum value.
     */  
    public static native void fplibSetVarBounds(long var, double min, double max);
    /**
     * Adds a variable in the environment and set its bounds.
     * 
     * @param name Name of the variable we want to add to the solver's environment.
     * @param type Type of the variable we want to add to the solver's environment.
     * @param min Variable's domain minimum value.
     * @param max Variable's domain maximum value.
     * 
     * @return A pointer to the concrete variable added (Fpc_Variable *).
     */  
    public static native long fplibAddBoundedVar(
    		long csp,
    		String name, 
    		int type, 
    		double min, 
    		double max);
    /**
     * Adds an unbounded variable in the environment.
     * 
     * @param name Name of the variable we want to add to the solver's environment.
     * @param type Type of the variable we want to add to the solver's environment.
     * 
     * @return A pointer to the concrete variable added (Fpc_Variable *).
     */  
    public static native long fplibAddVar(long csp, String name, int type);
    /**
     * Creates a constraint from a variable in the environment.
     * 
     * @param var A pointer (Fpc_Variable *) to a concrete variable.
      * 
     * @return A pointer to the concrete constraint created (Constraint *).
     */  
    public static native long fplibMkVar(long var);
    /**
     * Adds a constraint in the solver's environment.
     * 
     * @param model An Fplib model handling the variables of the given constraint.
     * @param ctr The concrete constraint (Constraint *) to be added to the current solver's model.
     */  
    public static native void fplibAddCtr(long model, long ctr);
    /**
     * This method returns the inf bound value of a fplib integer variable in the current model. 
     * @param var A pointer to a <b>valid</b> concrete <b>integer</b> variable (Fpc_Variable *) 
     *            in the current model.
     * 
     *  @return The inf bound value of the given concrete integer variable.
     */
    public static native int fplibInfValueInt(long var);
    /**
     * This method returns the sup bound value of a fplib integer variable in the current model. 
     * @param var A pointer to a <b>valid</b> concrete <b>integer</b> variable (Fpc_Variable *)
     *            in the current model.
     * 
     *  @return The sup bound value of the given concrete integer variable.
     */
    public static native int fplibSupValueInt(long var);
    /**
     * This method returns the inf bound value of a fplib float variable in the current model. 
     * @param var A pointer to a <b>valid</b> concrete <b>float</b> variable (Fpc_Variable *)
     *            in the current model.
     * 
     *  @return The inf bound value of the given concrete float variable.
     */
    public static native float fplibInfValueFloat(long var);
    /**
     * This method returns the sup bound value of a fplib float variable in the current model. 
     * @param var A pointer to a <b>valid</b> concrete <b>float</b> variable (Fpc_Variable *)
     *            in the current model.
     * 
     *  @return The sup bound value of the given concrete float variable.
     */
    public static native float fplibSupValueFloat(long var);
    /**
     * This method returns the inf bound value of a fplib double variable in the current model. 
     * @param var A pointer to a <b>valid</b> concrete <b>double</b> variable (Fpc_Variable *)
     *            in the current model.
     * 
     *  @return The inf bound value of the given concrete double variable.
     */
    public static native double fplibInfValueDouble(long var);
    /**
     * This method returns the sup bound value of a fplib double variable in the current model. 
     * @param var A pointer to a <b>valid</b> concrete <b>double</b> variable (Fpc_Variable *)
     *            in the current model.
     * 
     *  @return The sup bound value of the given concrete double variable.
     */
    public static native double fplibSupValueDouble(long var);
    /**
     * Display on standard output a string representation of the given concrete variable.
     * 
     * @param var A pointer to a concrete variable <b>valid</b> (Fpc_Variable *) in the current model.
     */
    public static native void fplibVarDisplay(long var);
	/**
	 * Creates a new expression corresponding to operator <pre>op</pre>.
	 * 
	 * @param op The operator code as defined in {@link validation.util.OpCode}.
	 * @param lhs The operator's first argument as fplib  constraint pointer (Constraint *).
	 * @param rhs The operator's second argument as fplib constraint pointer (Constraint *). 
	 *            May be null (i.e. <code>== 0</code>) in case of a unary operator.
	 * 
	 * @return A new expression built from the operator and its arguments.
	 */
	public static native long fplibMkOp(int op, long lhs, long rhs);
	/**
	 * Creates a new expression corresponding to a call to method <pre>methodName</pre>.
	 * 
	 * @return A new expression built from the method and its parameters.
	 */
	public static native long fplibMkMethodCall(String methodName, long[] params);
	/**
	 * Creates a new assignment constraint, which is different in fplib from an equality comparison.
	 * 
	 * @param var The pointer to the concrete variable (Fpc_Variable *) assigned.
	 * @param rhs The pointer to a concrete constraint (Constraint *) assigned to the given variable
	 * 
	 * @return A pointer to a new concrete constraint (Constraint *) <code>var = rhs</code>.
	 */
	public static native long fplibMkAssign(long var, long rhs);
	/**
     * Creates a concrete integer constant. 
     *  
     * @param cst The constant value.
     *             
     * @return A pointer (Constraint *) to the fplib element created for this constant.
     */  
	public static native long fplibMkIntCst(int cst);
    /**
     * Creates a concrete float constant. 
     *  
     * @param cst The constant value.
     *             
     * @return A pointer (Constraint *) to the fplib element created for this constant.
     */  
	public static native long fplibMkFloatCst(float cst);
    /**
     * Creates a concrete double constant. 
     *  
     * @param cst The constant value.
     *             
     * @return A pointer (Constraint *) to the fplib element created for this constant.
     */  
	public static native long fplibMkDoubleCst(double cst);

	public static native void initShaving(String fluctuatFilename);

	public static native boolean fplibShave(long solver, double twoBpercent, double intervalMinSize, double intervalMinRatio);

	public static native void displayShavedDomains();
 
}
