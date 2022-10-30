package java2CFGTranslator;

import CFG.CFG;
import CFG.SetOfCFG;


/** launcher to build a CFG
 * 
 * Java files are in directory testFiles/ 
 * 
 * main takes the name of the program to be exported as argument 
 *     java CFGBuilderLauncher javaFiles/Foo.java
 * will export file javaFiles/Foo.java into file opsemFiles/Foo.ml
 * 
 * @version November 2010
 * @author Hélène Collavizza
 * from an original student work by Eric Le Duff and Sébastien Derrien
 * Polytech'Nice Sophia Antipolis
 */
public class CFGBuilderLauncher {

	public static void main(String[] args) {
		try {
			if (args.length<2)
				System.err.println("Give a file name to convert and a method to verify");
			else {
				CFGBuilder c=null;
				switch (args.length){
				case 2 : c= new CFGBuilder(args[0],args[1]);
				break;
				case 3 : int bound = new Integer(args[2]);
				// max length of arrays is supposed equals to max number of unwinding
				c= new CFGBuilder(args[0],args[1],bound,bound);
				}
				SetOfCFG result = c.convert();
				System.out.println(result);
			}
		} catch (Java2CFGException e) {
			System.err.println(e.getMessage());
		}

	}

}
