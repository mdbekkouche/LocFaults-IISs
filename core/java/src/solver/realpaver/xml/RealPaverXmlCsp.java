package solver.realpaver.xml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import expression.logical.LogicalExpression;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;
import solver.Solver.SolverEnum;
import solver.realpaver.RealPaver;
import solver.realpaver.RealPaverExpressionVisitor;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.system.ExpressionCtrStore;
import validation.system.VariableVarStore;
import validation.system.xml.ValidationCSP;
import validation.system.xml.ValidationSystem;

/**
 * This class uses Real Paver to solve CSPs over reals.
 * Real Paver is called via the command line.
 * 
 * @author Olivier Ponsini
 *
 */
public class RealPaverXmlCsp extends ValidationCSP {

	/**
	 * Real Paver input file name. This file contains the CSP and solving options
	 * for Real paver command line call.
	 */
	private String inputFileName;
	/**
	 * Suffix added to the Real Paver input file name.
	 * This helps tracing the CSPs generated for Real Paver during 
	 * the CPBPV executable path exploration. 
	 */
	private int fileNum;
	private String optionHeader;
	private BufferedWriter inputFile;
    private RealPaverExpressionVisitor visitor;
    private RealPaver realPaver;
    
    /**
	 * Constructs a concrete linear CSP on reals handled by z3.
	 */ 
	public RealPaverXmlCsp(String name, ValidationSystem vs) {
		super(name, SolverEnum.REAL_PAVER, vs);
		visitor = new RealPaverExpressionVisitor(this);
		realPaver = (RealPaver)cspSolver;
		inputFileName = Validation.pgmFileName + ".rp";
		fileNum = 1;
		optionHeader = null;
	}

	public void setOptionHeader(String header) {
		optionHeader = header;
	}
	
	public void setDomainsToCurrentSolution() {
		setDomains(solution());
	}
	
	public void setDomains(DomainBox domains) {
		for (VariableDomain d: domains) {
			setDomain(d);
		}
	}

	public void setDomains(Solution vars) {
		for (Variable v: vars) {
			setDomain(v.domain());
		}
	}

	public void setDomain(VariableDomain domain) {
		Variable v = ((VariableVarStore)varBlock).get(domain.name());
		if (v!= null) {
			domain.setVariable(v);
			v.setDomain(domain);
		}
		else {
			System.err.println("Warning (setDomain): variable " 
					+ domain.variable() 
					+ " does not exist in the current CSP!");
		}
	}

	/**
	 * Updates domains in the current CSP to the given domains and reset the other domains.
	 * This does not modify any concrete variable domain.
	 * 
	 * A CSP domain present in <pre>domains</pre> is updated to the range in <pre>domains</pre>.
	 * A CSP domain not present in <pre>domains</pre> is reset to its type full domain.
	 * 
	 * @param domain The new domains for the domains' associated variables.
	 */
	public void resetDomains(DomainBox domains) {
		VariableDomain newDomain;
		for (Variable v: (VariableVarStore)varBlock) {
			newDomain = domains.get(v.name());
			if (newDomain != null) {
				v.setDomain(newDomain.minValue(), newDomain.maxValue());
			}
			else {
				v.resetDomain();
			}
		}		
	}

	/**
	 * Creates the stores of constraints and variables that will be handled by the concrete 
	 * solver.
	 * 
	 * This method is called by a mother class constructor.
	 *  
	 * @param s The solver associated with this CSP. It must be of type 
	 *          {@link solver.Z3Solver}.    
	 */	
	@Override
	protected void initCSP() {
		constr = new ExpressionCtrStore();
		varBlock = new VariableVarStore();
		arrayVarBlock = null;
		visitor = new RealPaverExpressionVisitor(this);
	}	

    private void write(Variable v) throws IOException {
		switch(v.type()) {
		case BOOL:
			inputFile.write("\tint ");
			inputFile.write(v.name());
			inputFile.write(" in [");
			if (v.domain() == null) { //Full type range
				inputFile.write("0, 1");
			}
			else {
				inputFile.write(v.constantBoolean()?"1, 1":"0, 0");
			}
   			inputFile.write("]");
   			break;
		case INT:
			inputFile.write("\tint ");
			inputFile.write(v.name());
			inputFile.write(" in ");
   			if (v.domain() == null) { //Full type range
				inputFile.write(Integer.toString(Validation.INTEGER_MIN_BOUND));
				inputFile.write(", ");
				inputFile.write(Integer.toString(Validation.INTEGER_MAX_BOUND));        				
			}
			else {
				writeNumericInterval(v.domain());
			}
   			break;
		case FLOAT:
		case DOUBLE:
			inputFile.write("\treal ");
			inputFile.write(v.name());
			inputFile.write(" in ");
   			if (v.domain() == null) { //Full type range
   	   			inputFile.write("[-oo, +oo]");
			}
			else {
				writeNumericInterval(v.domain());
			}
   			break;
		default:
			System.err.println("Error (RealPaver): unsupported type (" 
					+ v.type()
					+ ")!");
			System.exit(-1);
		}
    }

    private void writeNumericInterval(VariableDomain d) throws IOException {    	
		if (d.minValue() == null) {
			inputFile.write("[-oo");
		}			
		else {
	    	String minBound = d.minValue().toString();
			if (minBound.equals("-Infinity")) {
				inputFile.write("[-oo");
			}
			else if (minBound.equals("Infinity")) { //[+oo, +oo]
				inputFile.write("[+oo");			
			}
			else {
				inputFile.write("[");
				inputFile.write(minBound);
			}
		}
		
		inputFile.write(", ");
   		
		if (d.maxValue() == null) {
			inputFile.write("+oo]");			
		}
		else {
		   	String maxBound = d.maxValue().toString();
			if (maxBound.equals("Infinity")) {
				inputFile.write("+oo]");
   			}
			else if (maxBound.equals("-Infinity")) { //[-oo, -oo]
				inputFile.write("-oo]");			
			}
			else {
				inputFile.write(maxBound);       				
				inputFile.write("]");
			}
		}
    }
    
    private void writeVars(Iterable<Variable> vars) throws IOException {
		boolean firstVar = true;
		inputFile.write("Variables");
		inputFile.newLine();
		for (Variable v: vars) {
			if (!firstVar) {
				inputFile.write(",");
				inputFile.newLine();
			}
			else {
				firstVar = false;
			}
    		write(v);
		}
		inputFile.write(";");
		inputFile.newLine();
    }
    
    private void writeCtrs(Iterable<LogicalExpression> ctrs) throws IOException {
		boolean firstVar = true;
		String rpCtr;
		inputFile.write("Constraints");
		inputFile.newLine();
		for (LogicalExpression ctr: ctrs) {
			rpCtr = (String)ctr.structAccept(visitor);
			if (rpCtr.isEmpty()) {
				continue;
			}
			if (!firstVar) {
				inputFile.write(",");
				inputFile.newLine();
			}
			else {
				firstVar = false;
			}
			inputFile.write("\t");
			inputFile.write(rpCtr);
		}
		inputFile.write(";");
		inputFile.newLine();    	
    }

    /**
     * Writes optional header to the RealPaver input file.
     * Here, several options controlling the Real Paver solving strategy can be set. 
     *  
     * @throws IOException
     */
    private void writeHeader() throws IOException {
    	if (optionHeader != null) {
    		inputFile.write(optionHeader);
    		inputFile.newLine();
    		if (VerboseLevel.VERBOSE.mayPrint()) {
    			System.out.println("Real Paver options:\n" + optionHeader);
    		}
    	}
    }

    /** 
     * Creates the Real Paver input file. 
     */
	@Override
	public void startSearch() {
		try {
			inputFile = new BufferedWriter(new FileWriter(inputFileName + fileNum));
		} catch(IOException e) {
			System.err.println("Error (RealPaver): cannot open file for writing (" 
					+ inputFileName
					+ ")!");
			e.printStackTrace();
			System.exit(-1);
		}
    	try {
    		writeHeader();
    		writeVars((VariableVarStore)varBlock);
    		writeCtrs((ExpressionCtrStore)constr);
    		inputFile.close();
		} catch(IOException e) {
			System.err.println("Error (RealPaver): cannot write to file (" 
					+ inputFile.toString()
					+ ")!");
			e.printStackTrace();
			System.exit(-1);
		}
		realPaver.setInputFileName(inputFileName + fileNum);
		//Next CSP will have a different suffix number
		fileNum++;
		realPaver.startSearch();
	}
	
	/** to stop the search */
	@Override
	public void stopSearch(){
		realPaver.stopSearch();
	}

	/** true if current CSP as a next solution */
	@Override
	public boolean next() {
		status.moreSolve();
		status.setCurrentTime();
		boolean next = realPaver.next();
		status.setElapsedTime();
		if (!next)
			status.moreFail();
		return next;
	}
	
	@Override
	public Solution solution() {
		Solution sol = realPaver.solution();
		sol.setTime(this.getElapsedTime());
		return sol;
	}	

	@Override
	public DomainBox domainBox() {
		return solution().toDomainBox();
	}	
	
}
