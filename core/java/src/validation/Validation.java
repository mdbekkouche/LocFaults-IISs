package validation;

import ilog.concert.IloException;

import java.io.File;

import CFG.CFG;
import CFG.SetOfCFG;

import solver.Solver.SolverCombination;
import solver.cplex.cfg.CplexCfgDPVSValidation;
import solver.ilocp.cfg.IloCPCfgDPVSValidation;
import solver.java.CFG_DFS_javaInterpreter;
import solver.z3.cfg.Z3CfgDPVSValidation;
import solver.z3.cfg.Z3CfgDfsPathCover;
import solver.z3.cfg.Z3CfgDfsValidation;
import validation.strategies.cfg.localization.CPOPTIMIZERLocalization;
import validation.strategies.cfg.localization.CplexLocalization;
import validation.strategies.cfg.localization.Localization;
import validation.strategies.cfg.localization.Z3Localization;
import validation.system.ValidationSystemCallbacks;
import validation.visitor.XMLVisitAndValidate;
import exception.AnalyzeException;
import expression.variables.VariableDomain;

//TODO: Pourquoi garder la classe XMLVisitAndValidate à part et ne pas l'intégrer ici ?

/** 
 * This class is the high level entry point for the validation of a Java program
 * described in XML.
 * It links the XML file containing the Java program description with a validation system 
 * integrating constraint solvers and starts the validation process.
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 */
public class Validation {
	
	/**
	 * Defines the level of verbosity of output messages.
	 * Four levels are handled :
	 * <ul>
	 *   <li><code>QUIET</code>;</li>
	 *   <li><code>TERSE</code>;</li>
	 *   <li><code>VERBOSE</code>.</li>
	 *   <li><code>DEBUG</code>.</li>
	 *  </ul>
	 *  
	 *  Take care that the order of declaration of the elements is important: quieter 
	 *  levels should be declared before more verbose ones.
	 */
	public static enum VerboseLevel {
		/**
		 * Print minimal information during validation. This is not silent.
		 */
		QUIET,
		/**
		 * Print more information during validation than QUIET level, but less 
		 * than VERBOSE level. This is the default level.
		 */
		TERSE,
		/**
		 * Print information about each program path visited and each solver call, 
		 * including the solvers' output.
		 */
		VERBOSE,
		/**
		 * Print as much information as possible, probably more than one would want.
		 */
		DEBUG;
		
		/**
		 * @return <code>true</code> if this verbose level is smaller or equal to 
		 *         the application verbosity level.  
		 */
		public boolean mayPrint() {
			//Here we rely on the order in which the elements are declared in the 
			//VerboseLevel enumeration. 
			return this.compareTo(Validation.verboseLevel) <= 0;
		}
	}
	/**
	 * Name of the file containing description of the Java program.
	 * It may be a Java source code file or an XML representation of the Java program.
	 */
	public static String pgmFileName;
	/**
	 * Name of the method to validate. It must be defined in {@link #pgmFileName}.
	 */
	public static String pgmMethodName;
	/**
	 * Records the verbose level. Output messages should be 
	 * printed accordingly. Default is {@link #VerboseLevel.TERSE}.
	 */
	public static VerboseLevel verboseLevel = VerboseLevel.TERSE;
	/**
	 * The combination of solvers to use for validation.
	 * Default is {@link SolverCombination.#ILOG_CPLEX_CP_OPTIMIZER}.
	 */
	public static SolverCombination solverCombination = SolverCombination.ILOG_CPLEX_CP_OPTIMIZER;
	/**
	 * Maximum number (included) of loop unfoldings.
	 * Unfolding of loops is globally managed: it is the same 
	 * number for any loop in the program. Default is 10.
	 */
	public static int maxUnfoldings = 10;
	/**
	 * Maximum length for arrays. Default is 10.
	 */
	public static int maxArrayLength = 10;
	/**
	 * The integer format to be used in the validation systems, expressed as a 
	 * number of bits. Default is 8 bits.
	 */
	private static int integer_format = 8;
	/**
	 * The minimum possible value for integers.
	 * It depends on the integers' format.
	 */
	public static int INTEGER_MIN_BOUND = -128;
	/**
	 * The maximum possible value for integers.
	 * It depends on the integers' format. 
	 */
	public static int INTEGER_MAX_BOUND = 127;
	/**
	 * The precision used on reals: if the difference between two reals is below this threshold, 
	 * the two reals are considered the same. Default is 0.1.
	 * IBEX only.
	 */
	public static double real_precision = 0.1;
	/**
	 * The rounding mode to be used with floats in solvers able to deal with floats (i.e. fplib). 
	 * Default is {@link validation.util.RoundingMode.NEAR}.
	 * FPLIB only.
	 */
	public static validation.util.RoundingMode float_rnd = validation.util.RoundingMode.NEAR;
	/**
	 * Enables shaving mode and disable verification.
	 */
	public static boolean shaving = false;
	/**
	 * The initial domain of the method return value.
	 * If not <code>null</code>, CPBPV will not try to check the specification, but will 
	 * try instead to reduce this domain by shaving. Some solvers only.
	 */
	public static VariableDomain res_domain = null;
	/**
	 * Differentiates shaving strategy between Fplib 3B filtering and 
	 * our own shaving strategy based on Fplib 2B filtering.
	 * Default is to use our own strategy. 
	 */
	public static boolean shave3B = false;
	/**
	 * Uses the C++ JNI implementation of 2B shaving instead of the Java one.
	 */
	public static boolean native2BShaving = false;
	/**
	 * Enables piecewise analysis of programs: paths are merged before every
	 * second conditional statement to prevent combinatorial explosion of the
	 * number of paths to analyze. Used in collaboration with Fluctuat.
	 */
	public static boolean piecewise = false;	
	/**
	 * Enables path coverage test generation and disable verification.
	 */
	public static boolean pathCoverage = false;
	/**
	 * Enables faulty path generation and disable verification.
	 * <code>null</code> means disabled.
	 */
	public static boolean locate = false;
	/**
	 * In verification mode, write the counterexample to given file name.
	 * The file is in a format suitable to be used for faulty path 
	 * generation (@see #locateCounterExampleFileName).
	 * <code>null</code> means disable writing to a file.
	 */
	public static String counterExampleFileName = null;
	
	/**
	 *  Faulty conditions
	 */
	public static boolean NbFaultyCond = true;
	/**
	 * The number of faulty conditions that we consider
	 */
	public static int NumberFaultyCond = 0;
	
	/**
	 * In localization mode, set counterexample input values as soft constraints.
	 */
	public static boolean softInputs = false;
	/**
	 * In localization mode, set post-condition as a soft constraint.
	 */
	public static boolean softPost = false;
	/**
	 * Enables verification using DPVS algorithm.
	 */
	public static boolean dpvs = false;
	/**
	 * Precision in percent of number of floats of 2B filtering in Fplib.
	 */
	public static double fplib2Bprecision = 0.5;
	/**
	 * Precision in percent of number of floats of kB filtering in Fplib.
	 */
	public static double fplibkBprecision = 0.5;
	/**
	 * 2B shaving precision with Fplib.
	 * The shaving strategy will not use an interval smaller than this value to try to shave a domain.
	 */
	public static double shavingAbsolutePrecision = 1e-4;
	/**
	 * 2B shaving precision with Fplib.
	 * The shaving strategy will not use an interval whose size ratio with unsplit domain is smaller 
	 * than this value to try to shave a domain.
	 */
	public static double shavingRelativePrecision = 0;
	/**
	 * Name of the file containing the data needed for 2B shaving with Fplib.
	 */
	public static String shavingFileName;
	
	public static int integerFormat() {
		return integer_format;
	}
	
	public static void setIntegerFormat(int bits) {
		integer_format = bits;
		long val = (long)Math.pow(2, bits-1);
		INTEGER_MIN_BOUND = (int)-val;
		INTEGER_MAX_BOUND = (int)val-1;
	}
	
	/**
	 * Finds the CFG corresponding to the method to validate.
	 * 
	 * @param methods The set of method CFGs defined in file {@value #pgmFileName}.
	 * @return The method named {@value #pgmMethodName}.
	 */
	public static CFG pgmMethod(SetOfCFG methods) {
		//Validation.pgmMethodName should never be null at this point: if the method to prove 
		//was not specified on command line, it was set to the first method found in the class 
		//when converting the Java source code to CFG (by Java2CFGVisitor)
		 return methods.getMethod(Validation.pgmMethodName);
	}
	
	/**
	 * Parses the file <code>pgmFileName</code> and validates on the fly the Java 
	 * program it describes.
	 * 
	 * @throws AnalyzeException
	 * @throws IloException 
	 * @throws ConstraintException
	 */
	protected static void verify() throws AnalyzeException, IloException {
		if (pgmFileName.endsWith(".xml")) {
			XMLVisitAndValidate xvv = new XMLVisitAndValidate(new File(pgmFileName));
			//If pathCoverage or shaving is true, do not validate post condition by cases
			xvv.validate(!pathCoverage && !shaving);		
		} 
		else {
			if (pathCoverage) {  // mode = test generation for path coverage
				if (solverCombination != SolverCombination.Z3) {
					System.err.println("Error: Path coverage is only available with Z3!");
					System.exit(-1);
				}
				else {
					ValidationSystemCallbacks vs = new Z3CfgDfsPathCover("Z3");
					vs.validate();
				}
			}
			else if (locate) {  // mode = error localization
				System.out.println("DOING LOCALIZATION...");
				switch(solverCombination) {
				case CPLEX:
					CplexLocalization loc = new CplexLocalization(NumberFaultyCond);
					//Localization loc = new Localization();
					//loc.localize();
					break;
				case Z3:
					Z3Localization loc1 = new Z3Localization();
					loc1.localize();
					break;
				case CP_OPTIMIZER:
					CPOPTIMIZERLocalization loc2 = new CPOPTIMIZERLocalization(NumberFaultyCond);
					break;	
				default: 
					System.err.println("The combination of solvers " + Validation.solverCombination 
							           + " is not available!\nGoing on with Java interpreter.");
					System.exit(-1);
				}
				
			}
			else { // mode = validation
				ValidationSystemCallbacks vs;
				switch(solverCombination) {
				case Z3:
					if (dpvs) {  // DPVS heuristic
						vs = new Z3CfgDPVSValidation("Z3");
					}
					else {  // DFS (left first) heuristic
						vs = new Z3CfgDfsValidation("Z3");
					}
					break;
				case CP_OPTIMIZER:
					vs = new IloCPCfgDPVSValidation("CP Optimizer");
					break;
				case CPLEX:
					vs = new CplexCfgDPVSValidation("CPLEX");
					break;
				default: 
					System.err.println("The combination of solvers " + Validation.solverCombination 
							           + " is not available!\nGoing on with Java interpreter.");
				case JAVA:
					vs = new CFG_DFS_javaInterpreter();
					break;
				}
				vs.validate();
			}
		}
	}

}

