package solver.realpaver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import expression.variables.Variable;

import solver.ConcreteSolver;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.util.Type;

/**
 * 
 * @author Olivier Ponsini
 *
 */
public class RealPaver implements ConcreteSolver {
	
	private String[] cmd; 
	
	private String inputFileName;
	
	private Solution sol;
	
	public RealPaver() {
		sol = new Solution();
		cmd = new String[2];
		cmd[0] = "./lib/RealPaver/realpaver";
		//cmd[1] is set with the input file name in next()		
	}
	
	public void setInputFileName(String fileName) {
		inputFileName = fileName;
	}
	
	public Solution solution() {
		return sol;
	}
	    
    @Override
    public String toString() {
    	return "Real Paver v-0.4";
    }
 
    /**
	 * Does Nothing.
	 */
    @Override
	public void startSearch() {
		//Nothing to be done.
    }

    private void decodeSolution(InputStream input) throws IOException {
		Scanner scan;
		Double min, max;
		String line, name, bound;
		boolean readSol = false;
		
		BufferedReader out = new BufferedReader(new InputStreamReader(input));
		while((line = out.readLine()) != null) {
	    	if (Validation.verboseLevel == VerboseLevel.DEBUG) {
	    		System.out.println(line);
	    	}
	    	if (line.startsWith("OUTER BOX") || line.startsWith("INNER BOX")) {
	    		readSol = true;
	    	}
	    	else if (readSol) {
		    	if (line.isEmpty()) {
		    		readSol = false;
		    	}
		    	else {
		    		scan = new Scanner(line).useDelimiter("\\s+[\\[\\]]?|[\\[\\]]"); 
		    		name = scan.next();		//Read variable's name

		    		//"in" or "=" keywords
		    		if (scan.next().charAt(0) == '=') {
		    			bound = scan.next();
		    			if (bound.equals("-inf")) {
		    				min = Double.NEGATIVE_INFINITY;
		    			}
		    			else if (bound.equals("inf")) {
		    				min = Double.POSITIVE_INFINITY;		    				
		    			}
		    			else {
		    				min =  new Double(bound);
		    			}
		    			sol.add(new Variable(name, Type.DOUBLE, min, min));		    			
		    		}
		    		else {
		    			bound = scan.next();	//Read lower bound
		    			if (bound.endsWith("o")) {
		    				min = Double.NEGATIVE_INFINITY;
		    			}
		    			else {
		    				min = new Double(bound);
		    			}
	    		
		    			scan.next();			//Skip "," separator

		    			bound = scan.next();	//Read upper bound
		    			if (bound.endsWith("o")) {
		    				max = Double.POSITIVE_INFINITY;
		    			}
		    			else {
		    				max = new Double(bound);
		    			}
		    			sol.add(new Variable(name, Type.DOUBLE, min, max));
		    		}
		    	}
	    	}
		}
		out.close();
    }
    
	/**
	 * Explores the solution of the CSP over the variables domains.
	 */
    @Override
	public boolean next() {
    	try {
    		cmd[1] = inputFileName;
    		if (VerboseLevel.DEBUG.mayPrint()) {
    			System.out.println("Real Paver input file: " + inputFileName);
    		}
    		Process p = Runtime.getRuntime().exec(cmd);
    		p.waitFor();
    		sol.reset();
    		decodeSolution(p.getInputStream());
    		p.destroy();
    	} catch(IOException e) {
    		System.err.println("Error (RealPaver): IOException when executing Real Paver binary!");
    		e.printStackTrace();
    		System.exit(-1);
    	} catch(InterruptedException e) {
    		System.err.println("Error (RealPaver): Main thread interrupted while executing Real Paver binary!");
    		e.printStackTrace();
    		System.exit(-1);
    	}
		return !sol.isEmpty();
	}
		
	/**
	 * Does nothing.
	 */
    @Override
	public void stopSearch() {
		//Nothing to be done.
	}
	 
}
