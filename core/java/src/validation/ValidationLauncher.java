package validation;

import ilog.concert.IloException;
import exception.AnalyzeException;
import expression.variables.VariableDomain;
import solver.Solver.SolverCombination;
import validation.Validation.VerboseLevel;
/** 
 * La classe de base qui créé une preuve et la lance 
 *
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 */

public class ValidationLauncher {

	/**
	 * Holds the starting time of the application in milliseconds.
	 * The format is the one used by {@link System.currentTimeMillis()}.
	 */
	private static long startTime;

	/**
	 * @return The starting time of the application in milliseconds. 
	 *         See {@link System.currentTimeMillis()} for the format.
	 */
	public static long startTime() {
		return startTime;
	}
	
	// mode d'emploi
	public static void help() {
		System.out.println("Usage:");
		System.out.println("\tcpbpv [-help|-version] [-q|-v] [-solvers <combination>]");
		System.out.println("\t      [-int_format <number>] [-max_unfoldings <number>]");
		System.out.println("\t      [-arrays_length <number>] [-writeCE <file>]");
		System.out.println("\t      [-real_precision <number>] [-float_round <rnd_mode>]");
		System.out.println("\t      [-kB_precision <number>] [-2B_precision <number>]");
		System.out.println("\t      [-shaving_precision <number> <number>]");
		System.out.println("\t      [-cover] [-locate <file>]");
		System.out.println("\t      [-softinputs] [-softpost]");
		System.out.println("\t      [-shaving [-3B  <number> <number>|-2B <file>]");
		System.out.println("\t      [-native] [-piecewise]] <file>[@<method>]\n");		
		System.out.println("Verify formally the <method> from the Java program described in");
		System.out.println("the XML or Java file <file>. If <method> is not given, the first");
		System.out.println("one found in the <file> will be used.");

		System.out.println("Options:");
		System.out.println("\t-help\t\tPrint this help message and exit.");
		System.out.println("\t-version\tPrint the version number and exit.");
		System.out.println("\t-q\t\tPrint minimal information on stdout (not silent yet).");
		System.out.println("\t-v\t\tPrint verbose information on stdout. It includes");
		System.out.println("\t\t\tmessages from the calls to the solvers.");
		System.out.println("\t-V\t\tPrint debug information on stdout, more than you would");
		System.out.println("\t\t\tusually need.");
		System.out.println("\t-solvers\tUse the combination of solvers denoted by <combination>.");
		System.out.println("\t\t\tPossible values are: ILOG_CPLEX_CP_OPTIMIZER, CPLEX,");
		System.out.println("\t\t\tCP_OPTIMIZER, IBEX_PAVER, Z3, FPLIB, GLPK, GLPK_FPLIB,");
		System.out.println("\t\t\tREAL_PAVER, REAL_PAVER_FPLIB; default is");
		System.out.println("\t\t\t" + Validation.solverCombination + ".");
		System.out.println("\t-int_format\tUse integers coded on <number> bits; default is " 
			+ Validation.integerFormat() + ".");
		System.out.println("\t-max_unfoldings\tLoops will be unfolded up to <number> times included;");
		System.out.println("\t\t\tdefault is " + Validation.maxUnfoldings + ".");
		System.out.println("\t-writeCE\tIf found, write the counterexample to <file>.");

		System.out.println("\nModes:");
		System.out.println("\t-cover\t\tGenerate path coverage tests instead of verification.");
		System.out.println("\t-shaving\tTries to reduce given domains using shaving strategies.");
		System.out.println("\t\t\tEither option -2B, -3B or -piecewise must be used in");
		System.out.println("\t\t\tshaving mode (see solvers' specific options).");
		System.out.println("\t-locate\t\tGenerate faulty path constraints from a counterexample");
		System.out.println("\t\t\tgiven in a file containing input variables' value.");
	
		System.out.println("\t-softinputs\tIn localization mode, set counterexample constraints");
		System.out.println("\t\t\tas soft constraints.");
		System.out.println("\t-softpost\tIn localization mode, set post-condition constraint");
		System.out.println("\t\t\tas a soft constraint.");
		
		
		System.out.println("\nSolvers' specific options:");
		
		System.out.println("\n  IBEX:");
		System.out.println("\t-real_precision\tPrecision on reals will be set to <number>;");
		System.out.println("\t\t\tdefault is " + Validation.real_precision + ".");
		
		System.out.println("\n  FPLIB:");		
		System.out.println("\t-float_rnd\tFloats rounding mode.");
		System.out.println("\t\t\tPossible values are: UP, DOWN, NEAR, or ZERO;");
		System.out.println("\t\t\tdefault is " + Validation.float_rnd + ".");
		System.out.println("\t-2B_precision\t2B filtering precision in percent of floats;");
		System.out.println("\t\t\tlower is better. Default is " + Validation.fplib2Bprecision + ".");
		System.out.println("\t-kB_precision\tkB splitting precision in percent of floats;");
		System.out.println("\t\t\tlower is better. Default is " + Validation.fplibkBprecision + ".");
		System.out.println("\t-2B\t\tIn shaving mode only, use Fplib's 2B filtering for");
		System.out.println("\t\t\tshaving JMLResult's domain.");
		System.out.println("\t\t\t<file> contains initial domains and variables to be");
		System.out.println("\t\t\tshaved.");
		System.out.println("\t-shaving_precision");
		System.out.println("\t\t\tIn 2B shaving mode, this sets the absolute and");
		System.out.println("\t\t\trelative (in percent) shaving precision (i.e. minimal");
		System.out.println("\t\t\tsplit domain size); default is resp. " 
				+ Validation.shavingAbsolutePrecision + " and " + Validation.shavingRelativePrecision*100 + ".");
		System.out.println("\t-native\t\tIn 2B shaving mode, use the C++ implementation");
		System.out.println("\t\t\tinstead of the Java one.");
		System.out.println("\t-3B\t\tIn shaving mode only, use Fplib's 3B filtering for");
		System.out.println("\t\t\tshaving JMLResult's domain.");
		System.out.println("\t\t\tFirst given number is the lower bound and second number");
		System.out.println("\t\t\tis the upper bound of JMLResult's domain.");
		System.out.println("\t-piecewise\tIn 2B shaving mode only, merge execution paths to avoid");
		System.out.println("\t\t\tcombinatorial explosion of analyzed paths.");
		System.out.println("\t\t\t<file> contains initial domains and variables to be");
		System.out.println("\t\t\tshaved.");

		
		System.out.println("\n  Realpaver:");		
		System.out.println("\t-2B\t\tIn shaving mode only, use Realpaver's local");
		System.out.println("\t\t\tconsistencies for shaving JMLResult's domain.");
		System.out.println("\t\t\t<file> contains initial domains and variables to be");
		System.out.println("\t\t\t shaved.");
		System.out.println("\t-shaving_precision");
		System.out.println("\t\t\tIn 2B shaving mode, this sets the absolute and");
		System.out.println("\t\t\trelative (in percent) shaving precision (i.e. minimal");
		System.out.println("\t\t\tsplit domain size); default is resp. " + Validation.shavingAbsolutePrecision + " and " + Validation.shavingRelativePrecision*100 + ".");
		System.out.println("\t-3B\t\tIn shaving mode only, use Realpaver's internal paving");
		System.out.println("\t\t\tfor shaving JMLResult's domain.");
		System.out.println("\t\t\tFirst given number is the lower bound and second number");
		System.out.println("\t\t\tis the upper bound of JMLResult's domain.");
		System.out.println("\t-piecewise\tIn 2B shaving mode only, merge execution paths to avoid");
		System.out.println("\t\t\tcombinatorial explosion of analyzed paths.");
		System.out.println("\t\t\t<file> contains initial domains and variables to be");
		System.out.println("\t\t\tshaved.");

		
		System.out.println("\n  Z3:");
		System.out.println("\tOnly solver to work with .java files.");
	}
	
	
	/**
	 * Returns the version number string.
	 * This is a string built from the Subversion tag. The tag is inserted in the source 
	 * file through svn keyword substitution mechanism.
	 * 
	 * @return A string "version-XX" where XX is the tag number of the system under 
	 *         Subversion.
	 */
	public static String version() {
		String svnURL = "$HeadURL: http://subversion.renater.fr/cpbpv/CPBPV/Branches/CBEL/core/java/src/validation/ValidationLauncher.java $";
		int startPos = svnURL.indexOf("version-");
		int endPos = svnURL.indexOf('/', startPos);
		try {
			return svnURL.substring(startPos, endPos);
		} catch(IndexOutOfBoundsException e) {
			return "Version number could not be determined!";
		}
	}

	
    private static void parseCmdLine(String[] args) {
        int i;
        String arg;
        boolean fileFound = false;

        i = 0;
        while(i < args.length) {
            arg = args[i++];

            if (arg.equals("-q")) {
            	Validation.verboseLevel = VerboseLevel.QUIET;
            }
            else if (arg.equals("-v")) {
            	Validation.verboseLevel = VerboseLevel.VERBOSE;
            }
            else if (arg.equals("-V")) {
            	Validation.verboseLevel = VerboseLevel.DEBUG;
            }
            else if (arg.equals("-help")) {
                help();
                System.exit(0);
            }
            else if (arg.equals("-version")) {
                System.out.println(version());
                System.exit(0);
            }
            else if (arg.equals("-solvers")) {
                if (i < args.length) {
                	try {
                		Validation.solverCombination = SolverCombination.valueOf(args[i++]);
                	} catch (IllegalArgumentException e) {
                        System.err.println("Unknown combination (" + args[i-1] + ")!");
                        System.err.println("Default combination (" + Validation.solverCombination
                        		+ ") will be used.");               		
                	}
                }
                else {
                    System.err.println("-solvers option requires the name of a combination!");
                    System.err.println("Default combination (" + Validation.solverCombination
                    		+ ") will be used.");
                }
            }
            else if (arg.equals("-int_format")) {
                if (i < args.length)
                	Validation.setIntegerFormat(Integer.parseInt(args[i++]));
                else {
                    System.err.println("-int_format option requires a number of bits!");
                    System.err.println("Default integer format (" + Validation.integerFormat() 
                    		           + ") will be used.");
                }
            }
            else if (arg.equals("-max_unfoldings")) {
                if (i < args.length)
                	Validation.maxUnfoldings = Integer.parseInt(args[i++]);
                else {
                    System.err.println("-max_unfoldings option requires a number of times!");
                    System.err.println("Default maximum value (" + Validation.maxUnfoldings 
                    		           + ") will be used.");
                }
            }
            else if (arg.equals("-arrays_length")) {
                if (i < args.length)
                	Validation.maxArrayLength = Integer.parseInt(args[i++]);
                else {
                    System.err.println("-arrays_length option requires a number for the length of the arrays!");
                    System.err.println("Default value (" + Validation.maxArrayLength 
                    		           + ") will be used.");
                }
            }
            else if (arg.equals("-real_precision")) {
                if (i < args.length)
                	Validation.real_precision = Double.parseDouble(args[i++]);
                else {
                    System.err.println("-real_precision option requires a number!");
                    System.err.println("Default precision on reals (" + Validation.real_precision 
                    		           + ") will be used.");
                }
            }
            else if (arg.equals("-2B_precision")) {
                if (i < args.length)
                	Validation.fplib2Bprecision = Double.parseDouble(args[i++]);
                else {
                    System.err.println("-fplib2Bprecision option requires a number!");
                    System.err.println("Default precision for 2B (" + Validation.fplib2Bprecision 
                    		           + ") will be used.");
                }
            }
            else if (arg.equals("-kB_precision")) {
                if (i < args.length)
                	Validation.fplibkBprecision = Double.parseDouble(args[i++]);
                else {
                    System.err.println("-fplibkBprecision option requires a number!");
                    System.err.println("Default precision for kB (" + Validation.fplibkBprecision 
                    		           + ") will be used.");
                }
            }
            else if (arg.equals("-shaving_precision")) {
                if (i < (args.length - 1)) {
                	Validation.shavingAbsolutePrecision = Double.parseDouble(args[i++]);
                	Validation.shavingRelativePrecision = Double.parseDouble(args[i++])/100;
               }
                else {
                    System.err.println("-shaving_precision option requires two numbers!");
                    System.err.println("Ignoring option.");
                }
            }
            else if (arg.equals("-float_rnd")) {
                if (i < args.length)
                	Validation.float_rnd = validation.util.RoundingMode.valueOf(args[i++]);
                else {
                    System.err.println("-float_rnd option requires a valid rounding mode!");
                    System.err.println("Default mode (" + Validation.float_rnd
                    		+ ") will be used.");
                }
            }
            else if (arg.equals("-2B")) {
                if (i < args.length) {
                	Validation.shavingFileName = args[i++];
                	Validation.res_domain = null;
                	Validation.shave3B = false;
               }
                else {
                    System.err.println("-2B option requires a file name!");
                    System.err.println("Ignoring option.");
                }
            }
            else if (arg.equals("-3B")) {
                if (i < (args.length - 1)) {
                	double lb = Double.parseDouble(args[i++]);
                	double ub = Double.parseDouble(args[i++]);
                	Validation.res_domain = new VariableDomain(null, lb, ub);
                	Validation.shave3B = true;
                	Validation.shavingFileName = null;
               }
                else {
                    System.err.println("-3B option requires two numbers!");
                    System.err.println("Ignoring option.");
                }
            }
           else if (arg.equals("-shaving")) {
            	Validation.shaving = true;
            }
            else if (arg.equals("-cover")) {
            	Validation.pathCoverage = true;
            }
            else if (arg.equals("-locate")) {
                if (i < args.length) {
                	Validation.locate = true;
                	Validation.counterExampleFileName = args[i++];
                }
                else {
                    System.err.println("-locate option requires a file name!");
                    System.err.println("Ignoring option.");
                }
            }
            else if (arg.equals("-NbFaultyCond")){
            	if (i < args.length){
            		Validation.NumberFaultyCond = Integer.parseInt(args[i++]);
            	}
            	else {
                    System.err.println("-NbFaultyCond option requires a number!");
                    System.err.println("Ignoring option.");
                }
            }
            else if (arg.equals("-softinputs")) {
            	Validation.softInputs = true;
            }
            else if (arg.equals("-softpost")) {
            	Validation.softPost = true;
            }
           else if (arg.equals("-writeCE")) {
                if (i < args.length) {
                	Validation.counterExampleFileName = args[i++];
                }
                else {
                    System.err.println("-writeCE option requires a file name!");
                    System.err.println("Ignoring option.");
                }
            }
            else if (arg.equals("-dpvs")) {
            	Validation.dpvs = true;
            }
            else if (arg.equals("-native")) {
            	Validation.native2BShaving = true;
            }
            else if (arg.equals("-piecewise")) {
            	Validation.piecewise = true;
            }
           else { //We should be reading the file name
            	if(!fileFound) {
            		fileFound = true;
            		int methodPos = arg.indexOf('@');
            		if (methodPos == -1) {
            			//No method name given
            			Validation.pgmFileName = arg;
            		}
            		else {
            			//A method name has been given
            			Validation.pgmFileName = arg.substring(0, methodPos);
            			Validation.pgmMethodName = arg.substring(methodPos + 1);
            		}
            	}
            	else { //Exits the while loop since there is a conflict on the file name
            		System.err.println("A file name (" + Validation.pgmFileName + ") was already found, this "
            				+ "one (" + arg + ") cannot be used. Rest of the line is ignored.");
            		break;
            	}
            }
        }
        if(!fileFound) {
        	System.err.println("An input file is required. Command usage is following...\n\n");
      		help();
        	System.exit(-1);
        }
        if (Validation.shaving && (Validation.res_domain == null) && (Validation.shavingFileName == null)) {
           	System.err.println("In Shaving mode with Fplib, either -2B or -3B option must be used. Command usage is following...\n\n");
   			help();
           	System.exit(-1);      	
        }
    }

	// TODO : traiter les erreurs avec des exceptions
	public static void main(String[] args) throws IloException {
		parseCmdLine(args);
		try {
			startTime = System.currentTimeMillis();
			Validation.verify();
			if (VerboseLevel.TERSE.mayPrint()) {
				long endTime = System.currentTimeMillis();
				System.out.println("Total elapsed time: " + (endTime-startTime)/1000.0 + " s.");
			}
		} catch (AnalyzeException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (StackOverflowError e) {
			System.err.println("Stack overflow " + e);
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.err.println(e.getCause());
		}
	}
}

