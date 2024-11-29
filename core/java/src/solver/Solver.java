package solver;

import solver.cplex.Cplex;
import solver.fplib.FplibSolver;
import solver.glpk.GlpkSolver;
import solver.ibex.IbexSolver;
import solver.ilocp.IloCPSolver;
import solver.realpaver.RealPaver;
import solver.z3.Z3Solver;
import validation.Validation.VerboseLevel;

/**
 * This class provides some utility methods and a factory method to create concrete solvers.
 *  
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 *
 */
public class Solver {
    /**
     * This enumerates the names of the known solvers. 
     */
    public static enum SolverEnum {
    	/**
    	 * The Ilog CPLEX solver for linear mixed integer problems.
    	 */
    	CPLEX,
    	/**
    	 * The Ilog CP Optimizer solver for non linear constraint problems.
    	 */
    	ILOG_CP_OPTIMIZER,
    	/**
    	 * The IBEX paver on continuous domains. Solves constraint problems over reals.
    	 */
    	IBEX_PAVER,
    	/**
    	 * The z3 SMT solver. Solves linear constraint problems over reals (among others).
    	 */
    	Z3,
    	/**
    	 * Claude Michel's fplib solver. Solves constraint problems over floats.
    	 */
    	FPLIB,
    	/**
    	 * GNU Linear Programming Kit. Solves linear constraint problems over rationals 
    	 * or mixed integers.
    	 */
    	GLPK,
    	/**
    	 * The Real Paver solver. Exact solver over the reals.
    	 */
    	REAL_PAVER    	
   } 
    /**
     * This enumerates all the combination of solvers available in the system. 
     */
    public static enum SolverCombination {
    	/**
    	 * Combines <pre>Ilog Cplex</pre> for linear constraints and <pre>Ilog Jsolver</pre>
    	 * for boolean abstraction and non linear constraints.
    	 */
    	CPLEX_JSOLVER, 
    	/**
    	 * Combines <pre>Ilog Cplex</pre> for linear constraints and <pre>Ilog CP Optimizer</pre> 
    	 * for non linear constraints.
    	 */
    	ILOG_CPLEX_CP_OPTIMIZER,
    	/**
    	 * This uses exclusively the <pre>IBEX</pre> paver for constraints over reals.
    	 */
    	IBEX_PAVER,
    	/**
    	 * This uses exclusively the <pre>z3</pre> SMT solver for linear constraints.
    	 */
    	Z3,
    	/**
    	 * This uses exclusively the <pre>fplib</pre> solver for constraints over floats.
    	 */
    	FPLIB,
    	/**
    	 * This uses exclusively the <pre>GLPK</pre> solver for constraints over floats.
    	 */
    	GLPK,
    	/**
    	 * Combines <pre>GLPK</pre> for linear constraints over rationals on the specification
    	 * and <pre>Fplib</pre> for linear constraints over floats on the analyzed method body.
    	 */
    	GLPK_FPLIB,
       	/**
    	 * This interprets the java program.
    	 */
     	JAVA,
    	/**
    	 * This uses exclusively the <pre>Real Paver</pre> solver for constraints over 
    	 * reals.
    	 */
    	REAL_PAVER,     	
    	/**
    	 * Combines <pre>Real Paver</pre> and <pre>Fplib</pre> solvers for searching 
    	 * maximal error between real and float results.
    	 */
    	REAL_PAVER_FPLIB, 
    	/**
    	 * This uses exclusively ILOG CPLEX solver.
    	 */   	
    	CPLEX, 
    	/**
    	 * This uses exclusively ILOG CP Optimizer solver.
    	 */   	   	
    	CP_OPTIMIZER,    
    	/**
    	 * This uses exclusively ILOG CPLEX and ILOG CP Optimizer solvers
    	 */
    	CPLEXCP_OPTIMIZER
  }
	
	//Factory
	public static ConcreteSolver createSolver(SolverEnum solver) {
		ConcreteSolver s = createConcreteSolver(solver);
		/*if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\t" + s.toString());
		}*/
		return s;
	}
	
	// methods to handle concrete solver
	
	/** to set the concrete solver */
	private static ConcreteSolver createConcreteSolver(SolverEnum solver) {
		switch (solver) {
		case REAL_PAVER		  : return new RealPaver();
		case GLPK			  : return new GlpkSolver();
		case FPLIB			  : return new FplibSolver();
		case Z3               : return new Z3Solver();  
		case IBEX_PAVER       : return new IbexSolver();  
		case CPLEX            : return new Cplex();
		case ILOG_CP_OPTIMIZER: return new IloCPSolver();
		default:
			System.err.println("Error (createConcreteSolver): unknown solver (" + solver + ")!");
			System.exit(80);
			return null;
		}
	}
	
}

