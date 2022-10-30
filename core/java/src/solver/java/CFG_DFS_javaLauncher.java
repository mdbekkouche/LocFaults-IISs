package solver.java;

import ilog.concert.IloException;
import validation.Validation;
import validation.Validation.VerboseLevel;

/**
 * class to evaluate a java program and verify its pre and post condition
 * according to input values
 * 
 * @author helen
 *
 */
public class CFG_DFS_javaLauncher {

	/**
	 * @param args
	 * @throws IloException 
	 */
	public static void main(String[] args) throws IloException {
		if (args.length<2)
			System.err.println("Give a program name and a method to execute");
		else {

			// build a validation
			Validation.pgmFileName=args[0];
			Validation.pgmMethodName = args[1];
			if (args.length==3)	{	
				if (args[2].equals("-v"))
					Validation.verboseLevel=VerboseLevel.VERBOSE;
				else
					Validation.maxUnfoldings =	new Integer(args[2]);
			}
			if (args.length==4)	{	
				if (args[2].equals("-v"))
					Validation.verboseLevel=VerboseLevel.VERBOSE;
				Validation.maxUnfoldings =	new Integer(args[3]);
			}
			CFG_DFS_javaInterpreter e = new CFG_DFS_javaInterpreter();
			e.validate();
		}
	}
}

