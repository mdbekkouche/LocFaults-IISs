package solver.glpk;

import solver.ConcreteSolver;
import validation.Validation;
import validation.solution.SolverStatus;
import validation.util.Type;
import exception.VariableValueException;

/**
 * The GLPK concrete solver.
 * This class defines native methods to access the GLPK C library.
 * These methods are part of the <code>libjni_glpk.so</code> library.
 * 
 * @author Olivier Ponsini
 *
 */
public class GlpkSolver implements ConcreteSolver {

    static {
    	System.loadLibrary("jni_glpk");
    }
        
    /**
     * The pointer to the GLPK linear problem converted to a long int.
     * Initialized to "NULL".
     */
    private long lp = 0L;
   
    public GlpkSolver() {
    	//Creates the solver problem if it does not exist or erase the previous one
    	lp = glpkInit(
			lp,
			Validation.verboseLevel.compareTo(Validation.VerboseLevel.VERBOSE) >= 0);
    }
	    
    @Override
    public String toString() {
    	return "GLPK " + glpkVersion();
    }

    /** 
     * Ensures that GLPK resources will be freed if this object is ever garbage-collected
     * before a clean call to {@link #stop()}.
     */
    public void finalize() {
		glpkRelease(lp);
    }

	/**
	 * Explores the solution of the CSP over the variables domains.
	 * This method will not iterate over the possible solutions.
	 * 
	 * @return True if an unbounded solution could be found; false otherwise.
	 */
	public boolean next() {
		return glpkSolve(lp);
	}

	/**
	 * Explores the solution of the CSP over the variables domains. 
	 * This method will not iterate over the possible solutions.
	 * 
	 * The method also updates the given solver status.
	 * 
	 * @return True if an unbounded (feasible or optimal) solution could be found; false otherwise.
	 */
	public boolean next(SolverStatus status) {
		status.moreSolve();
		status.setCurrentTime();
		boolean next = glpkSolve(lp);
		status.setElapsedTime();
		if (!next)
			status.moreFail();
		return next;
	}

	/**
	 * Creates the CSP on the solver side.
	 * Should always be called before {@link #next()} when the CSP has been modified or released.
	 * If a concrete CSP already existed, it is discarded (its memory resources are freed) and
	 * replaced by the current one.
	 */
	public void startSearch() {
		//Nothing to be done
	}

	public void stopSearch() {
		//nothing to be done
	}
	
	public void release() {
		glpkRelease(lp);
		//Set lp to "NULL"
		lp = 0L;	
	}
	
	public void setVarBounds(int col, boolean boundedMin, double min, 
							 boolean boundedMax, double max) 
	{
		glpkSetVarBounds(lp, col, boundedMin, min, boundedMax, max);
	}
	
	public int addVar(String name, Type type) {
		switch(type) {
		case FLOAT:
		case DOUBLE:
			return glpkAddVar(lp, name);
		default:
			System.err.println(type + " variables are not supported in this GLPK validation system!");
			System.err.println("Variable " + name + " handled as a rational!");
			return glpkAddVar(lp, name);
		}
	}
	
	public void addCtr(double bound, int opCode, int nb, int[] indices,	double[] values) {
		glpkAddCtr(lp, bound, opCode, nb, indices, values);
	}
	
	public void delCols(int[] cols) {
		glpkDelCols(lp, cols.length, cols);
	}
	
	public void delRows(int[] rows) {
		glpkDelRows(lp, rows.length, rows);
	}
	
    public void setObjective(
    		double constant, 
    		boolean minimize, 
    		int nb, 
    		int[] indices, 
    		double[] values) 
    {
    	glpkSetObjective(lp, constant, minimize, nb, indices, values);
    }

    public void setObjectiveDirection(boolean minimize) {
    	glpkSetObjectiveDirection(lp, minimize);
    }
    
	public Number value(int col, Type varType) throws VariableValueException {
		switch(varType) {
		case FLOAT:
			return new Float((float)glpkValue(lp, col));
		case DOUBLE:
			return new Double(glpkValue(lp, col));
		default:
			throw new VariableValueException(
					"unused type (" 
					+ varType 
					+ ") for a variable with GLPK!"); 
		}
	}

	public double value(int col) {
		return glpkValue(lp, col);
	}

	public String rowToString(int row) {
		return glpkRowToString(lp, row);
	}
	
	public boolean solutionFound() {
		return glpkSolutionFound(lp);
	}
	
    //--------------------------------------------------------------
	// NATIVE METHODS
	//--------------------------------------------------------------

	/**
	 * @return The GLPK version number as a String.
	 */
	public static native String glpkVersion();
	/**
	 * This method creates the linear problem structure where to add variables and constraints.
	 * 
	 * @param verbose enables or disables terminal output.
	 */
	private static native long glpkInit(long lp, boolean verbose);
	/**
	 * Releases the memory resources allocated for the solver.
	 */ 
    private static native void glpkRelease(long lp);
    /**
     * This method explores the search space for a solution to the CSP.
     * 
     * @return <code>True</code> if a solution has been found; <code>False</code> otherwise.
     */
    private static native boolean glpkSolve(long lp);
    /**
     * Adds a rational variable to the LP matrix.
     * 
     * @param name Name of the variable we want to add to the solver's environment.
     * 
     * @return The column index of the variable in the LP matrix.
     */  
    public static native int glpkAddVar(long lp, String name);
    /**
     * Adds a rational variable to the LP matrix with given bounds.
     * 
     * @param name Name of the variable we want to add to the solver's environment.
     * 
     * @return The column index of the variable in the LP matrix.
     */  
    public static native int glpkAddBoundedVar(
    		long lp,
    		String name, 
    		boolean boundedMin, 
    		double min,
    		boolean boundedMax,
    		double max);
    /**
     * Sets the domain bounds of the variable at the given column.
     */  
    public static native void glpkSetVarBounds(
    		long lp,
    		int col, 
    		boolean boundedMin, 
    		double min,
    		boolean boundedMax,
    		double max);
    /**
     * Adds a constraint in the solver LP matrix.
     * 
     * @param bound the row bound value.
     * @param opCode the code (from opCode enum) of the relational operator of this constraint.
     * @param nb the number of coefficients to set in the LP matrix.
     * @param indices the indices of the columns where to set the coefficients. 
     *                Should never be <code>null</code>.
     * @param values the coefficients to be set in the given columns of the LP matrix.
     *               Should never be <code>null</code>.
     */               
    public static native void glpkAddCtr(
    		long lp,
    		double bound, 
    		int opCode, 
    		int nb, 
    		int[] indices, 
    		double[] values);
    /**
     * Sets the LP objective function.
     * 
     * @param constant The constant value of the objective function. 
     * @param minimize True if the direction is minimization; false if it is maximization.
     * @param nb the number of coefficients to set in the objective function.
     * @param indices the indices of the columns where to set the coefficients.
     *                Should never be <code>null</code>.
     * @param values the coefficients to be set in the given columns of the objective function.
     *                Should never be <code>null</code>.
     */  
    public static native void glpkSetObjective(
    		long lp,
    		double constant, 
    		boolean minimize, 
    		int nb, 
    		int[] indices, 
    		double[] values);
    /**
     * Sets the LP objective function direction.
     * 
     * @param minimize True if the direction is minimization; 
     *                 false if it is maximization.
     */  
    public static native void glpkSetObjectiveDirection(long lp, boolean minimize);
    /**
     * This method returns the value of a GLPK variable in the current solution. 
     * @param col The column index of the variable in the GLPK LP matrix.
     * 
     *  @return The value of the variable at the given column in the LP matrix.
     */
    public static native double glpkValue(long lp, int col); 
    
    public static native void glpkDelCols(long lp, int nb, int[] cols);
    
    public static native void glpkDelRows(long lp, int nb, int[] rows);
    
    /**
     * @param lp
     * @param row
     * @return A String representation of the given GLPK LP matrix row.
     */
    public static native String glpkRowToString(long lp, int row);
    
    /**
     * @param lp
     * @return True if the last solve had a solution (feasible, optimal, or unbounded); 
     *         False otherwise.
     */
    public static native boolean glpkSolutionFound(long lp);
   
}
