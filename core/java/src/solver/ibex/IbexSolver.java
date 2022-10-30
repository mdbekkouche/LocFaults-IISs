package solver.ibex;

import expression.logical.LogicalExpression;
import expression.variables.Variable;
import solver.ConcreteSolver;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.system.ExpressionCtrStore;
import validation.system.VariableVarStore;

/**
 * The IBEX concrete solver.
 * This class defines native methods to access the IBEX C++ library.
 * These methods are part of the <pre>libjni_ibex.so</pre> library.
 * 
 * TODO: Preliminary work only: the solutions found by IBEX have still to be taken into account...
 * 
 * @author Olivier Ponsini
 *
 */
public class IbexSolver implements ConcreteSolver {

    static {
    	System.loadLibrary("jni_ibex");
    }

    /**
     * The pointer to the IBEX paver environment converted to a long integer.
     */
    private long paver_env;
    /**
     * The pointer to the IBEX paver space converted into a long integer.
     * This will be built from paver_env with the paver_space_factory.
     */
    private long paver_space;
    /**
     * The pointer to the IBEX paver space factory converted into a long integer.
     */
    private long paver_space_factory;
    /**
     * The pointer to the IBEX paver converted into a long integer.
     */
    private long paver;
   /**
     * The pointer to the IBEX contractors built from the CPBPV constraints converted into 
     * a long integer.
     */
    private long ctc_vector;
    /**
     * The variables handled by the CSP backed by IBEX.
     */
    private VariableVarStore vars;
    /**
     * The constraints of the CSP backed by IBEX.
     */
    private ExpressionCtrStore constraints;
    
    /**
     * Sets the variables and constraints to be handled by this solver.
     * 
     * @param vars The CSP variables.
     * @param constraints The CSP constraints.
     */
    public void setCSP(VariableVarStore vars, ExpressionCtrStore constraints) {
    	this.vars = vars;
    	this.constraints = constraints;
    }
    
    @Override
    public String toString() {
    	return "Gilles Chabert's Ibex 1.16";
    }

    /** 
     * Ensures that IBEX resources will be freed if this object is ever garbage-collected
     * before a clean call to {@link #stop()}.
     */
    public void finalize() {
    	ibexRelease(ctc_vector, paver, paver_space, paver_space_factory, paver_env);

    }

	/**
	 * Explores the solution of the CSP over the variables domains.
	 * No modification to the CSP or call to {@link #stopSearch()} should intervene between 
	 * a previous call to {@link #startSearch()} and a call to next(). If this occurs, 
	 * call {@link #startSearch()} again.
	 * 
	 * @return Not yet implemented !
	 */
	public boolean next() {
		ibexExplore(paver_space, paver);
		return false;
	}

	/**
	 * Creates the CSP on the paver side.
	 * Should always be called before {@link #next()} when the CSP has been modified or released.
	 * If a concrete CSP already existed, it is discarded (its memory resources are freed) and
	 * replaced by the current one.
	 */
	public void startSearch() {
		//Discard previous CSP in case it has not already been done
		ibexRelease(ctc_vector, paver, paver_space, paver_space_factory, paver_env);
		//Creates the paver environment.
		paver_env = ibexCreateEnv();
		//Adds variables to the paver environment.
		for (Variable var: vars) {
			ibexAddVar(paver_env, var.name());
		}

		//Creates the factory. Maybe this could be done in IbexInit() and the variables 
		//domain could be added along with the variables. This was not done like this in Quimper
		//but could be tried...
		paver_space_factory = ibexCreateSpaceFactory(paver_env);		
		//Adding the domains can only be done on the space factory. 
		for (Variable var: vars) {
			ibexSetVarDomain(paver_space_factory, var.name(), var.type().ordinal());
		}
		
		paver_space = ibexCreateSpace(paver_space_factory);
		ctc_vector = ibexCreateCTCVector();
		
		IbexExpressionVisitor visitor = new IbexExpressionVisitor(this);
		for (LogicalExpression c: constraints) {
			//Adds the constraint to IBEX
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("Adding ibex constraint " + c);
			}
			c.structAccept(visitor);
		}
		
		//Creates the paver with the precision given on the command line or the 
		//default precision. The paver space has to be built beforehand.
		paver = ibexCreatePaver(paver_space, ctc_vector, Validation.real_precision);
	}

	/**
	 * Releases the memory resources allocated to the IBEX paver in the C++ interface.
	 * This method should be called as soon as the current CSP is not needed anymore.
	 */
	public void stopSearch() {
		ibexRelease(ctc_vector, paver, paver_space, paver_space_factory, paver_env);
	}
	
	public void newCmpOp(int cmp, long leftExpr, long rightExpr) {
		ibexNewCmpOp(paver_space, ctc_vector, cmp, leftExpr, rightExpr);
	}
	
	public void newUnion() {
		ibexNewUnion(paver_space, ctc_vector);
	}
	
	public long newCst(double value) {
		return ibexNewCst(paver_env, value);
	}
	
	public long getSymbol(String varName) {
		return ibexGetSymbol(paver_env, varName);
	}

	public double getLB(String varName) {
		return ibexGetLB(paver_space, varName);
	}

	public double getUB(String varName) {
		return ibexGetUB(paver_space, varName);
	}
	
	//--------------------------------------------------------------
	// NATIVE METHODS
	//--------------------------------------------------------------

	/**
	 * This method should always be called first. It creates the paver environment 
	 * where to store variables and constraints.
	 */
	public static native long ibexCreateEnv();
    /**
     * This method should be called after initialization and before creation 
     * of the paver space.
     */ 
    public static native long ibexCreateSpaceFactory(long env);
   /**
     * This method should be called after the creation of the space factory.
     */ 
    public static native long ibexCreateSpace(long space_factory);
    /**
     * This method should be called before visiting the constraints to add to the CSP.
     */ 
    public static native long ibexCreateCTCVector();
	/**
	 * Releases the memory resources allocated for the paver.
	 */ 
    public static native void ibexRelease(long ctcs, 
    									   long paver, 
    									   long space, 
    									   long space_factory, 
    									   long env);
    /**
     * This method explores the search space for solutions to the CSP. 
     */
    public static native void ibexExplore(long space, long paver);
    /**
     * Adds a variable in the environment. At this level, we do not keep track of its type, 
     * and hence domain: the domain information will be added through the paver space factory.
     * 
     * @param name Name of the variable we want to add to the paver environment.
     */  
    public static native void ibexAddVar(long env, String name);
    /**
     * Adds domain information to the variables present in the environment.
     * This method requires the space factory has been created.
     * @param name Name of the variable for which we want to set the domain.
     * @param type Represents the type of the variable in the Java source program. 
     *             The mapping between values and types depends on the order of the types in the 
     *             Java enumeration {@link validation.util.Type} (starting from 0).
     */ 
    public static native void ibexSetVarDomain(long space_factory, String name, int type);
    /**
     * Creates a paver instance.
     * The paver space and environment should exist before calling this method.
     * All the constraints in the environment are associated with a HC4Revise contractor, 
     * and all the resulting contractors are handled through a Propagation contractor 
     * (a kind of AC3 propagation algorithm).
     *
     * A Precision contractor is also added whose ceil is defined by the precision parameter.
     * The bisection algorithm is provided by a RoundRobin whose precision is the same as the 
     * Precision contractor.
     * 
     * @param precision The value used for the precision contractor (smaller domains are rejected)
     *                  and the bisection algorithm.
     */ 
    public static native long ibexCreatePaver(long space, long ctcs, double precision);
	/**
	 * Creates a new ibex constraint from a comparison expression and adds it to 
	 * the current paver list of contractors.
	 * 
	 * @param opCode The IBEX operator code corresponding to the enum {@link IbexOpCode}
	 *               items order.
	 * @param leftExpr A pointer to an IBEX expression that will be the first (left-hand side)
	 *                 argument of the comparison operator.
	 * @param rightExpr A pointer to an IBEX expression that will be the second (right-hand side)
	 *                  argument of the comparison operator.
	 */ 
	public static native void ibexNewCmpOp(long space, long ctcs, int opCode, long leftExpr, long rightExpr);		
	/**
	 * Creates an union from the last two constraints added.
	 */
	public static native void ibexNewUnion(long space, long ctcs);		
	/**
	 * Creates a new constant with the given value and returns a pointer to it.
	 * 
	 * @param value The value of the constant to build.
	 * 
	 * @return A pointer (cast into a long int) to the newly built IBEX constant expression.
	 */ 
	public static native long ibexNewCst(long paver_env, double value);
	/**
	 * Returns the IBEX symbol of the given name.
	 * 
	 * @return A pointer (cast into a long int) to the IBEX symbol of the given name.
	 */ 
	public static native long ibexGetSymbol(long env, String varName);
	/**
	 * Creates a new ibex expression and returns it as a pointer cast into a 'long int'.
	 *
	 * @param opCode The IBEX operator code corresponding to the enum {@link IbexOpCode}
	 *               items order.
	 * @param leftExpr A pointer to an IBEX expression that will be the first (left-hand side)
	 *                 argument of the operator.
	 * @param rightExpr A pointer to an IBEX expression that will be the second (right-hand side)
	 *                  argument of the operator; <pre>null</pre> in case of a unary operator.
	 * 
	 * @return A pointer (cast into a long int) to the newly built IBEX operator expression.
	 */ 
	public static native long ibexNewOp(int opCode, long leftExpr, long rightExpr);
	/**
	 * @param space A pointer to an IBEX Space
	 * @param varName Name of an IBEX entity.
	 * @return the lower bound of the given variable entity domain.
	 */
	public static native double ibexGetLB(long space, String varName);
	/**
	 * @param space A pointer to an IBEX Space
	 * @param varName Name of an IBEX entity.
	 * @return the upper bound of the given variable entity domain.
	 */	
	public static native double ibexGetUB(long space, String varName);
	
	

}
