package CFG.simplification;

import ilog.concert.IloException;
import validation.Validation;
import CFG.SetOfCFG;
import java2CFGTranslator.CFGBuilder;
import java2CFGTranslator.Java2CFGException;

public class SimplifierLauncher {

	/**
	 * @param args
	 * @throws IloException 
	 */
	public static void main(String[] args) throws IloException {
		try {
			if (args.length<2)
				System.err.println("Give a file name to convert and a method to verify");
			else {
				System.out.println("Starting conversion of file " + args[0]);
				CFGBuilder c = new CFGBuilder(args[0],args[1]);
				SetOfCFG cfgs = c.convert();
				System.out.println(cfgs);
				Simplifier si = new Simplifier();
				si.simplify(cfgs);
				System.out.println("Simplified CFGs");
				System.out.println(cfgs);
			}
		} catch (Java2CFGException e) {
			System.err.println(e.getMessage());
		}

	}

}
