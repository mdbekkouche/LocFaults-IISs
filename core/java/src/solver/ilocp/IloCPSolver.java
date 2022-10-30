package solver.ilocp;

import java.io.OutputStream;

import ilog.cp.IloCP;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;

import solver.ConcreteSolver;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.util.Type;

/**
 * This class encapsulates the Ilog CP Optimizer solver as a 
 * {@link solver.ConcreteSolver}.
 * 
 * <code>ConcreteSolvers</code> are used by {@link validation.system.validationCSP} objects 
 * containing the constraint store to get to the concrete solver.
 *  
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 */
public class IloCPSolver implements ConcreteSolver {
	
	/**
	 * The Ilog CP Optimizer solver.
	 */
	protected IloCP concreteSolver;

	/**
	 * Creates an instance of the Ilog CP Optimizer solver.
	 */
	public IloCPSolver() {
		concreteSolver = new IloCP();
	    //If the verbosity level is too quiet to print solver messages, set output
	    //to a dummy OutputStream discarding writes. 
		if(Validation.verboseLevel.compareTo(VerboseLevel.VERBOSE) < 0) {
			concreteSolver.setOut(new OutputStream(){public void write(int b){}});	
			concreteSolver.setWarning(new OutputStream(){public void write(int b){}});	
		}
	}
	
	//--------------------------------
	//ConcreteSolver interface methods
	//--------------------------------
	
	
	/**
	 * This method calls the solver to get a new solution to the CSP
	 * handled by the solver.
	 * 
	 * Before calling <code>next</code> for the first time, a call to 
	 * {@link #startSearch} is mandatory.
	 * After calling <code>next</code> for the last time, a call to 
	 * {@link #stopSearch} is advised.
	 * 
	 * @return <code>true</code> if the solver found a solution; 
	 *         <code>false</code> otherwise. 
	 */
	public boolean next() {
		try {
			return concreteSolver.next();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Initializes the solver before calling {@link #next} to search a solution
	 * to the CSP handled by the solver.
	 */
	public void startSearch() {
		try {
			concreteSolver.startNewSearch();
		} catch (IloException e) {
			System.err.println("Ilog CP Optimizer solver could not be initialised!");
			e.printStackTrace();
			System.exit(19);
		}
	}

	/**
	 * This method ends the search freeing resources used during the search. It does
	 * not free the memory resources associated with the CSP handled by the solver, 
	 * only the search resources.  
	 */
	public void stopSearch() {
		try {
			concreteSolver.endSearch();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns whether the CSP handled by the solver has a solution by initiating 
	 * a search process.
	 * 
	 * @return <code>true</code> if the solver found a solution; 
	 *         <code>false</code> otherwise. 
	 */
	public boolean hasSolution() {
		boolean has = false;
		startSearch();
		has = next();
		stopSearch();
		return has;
	}
	
	
	/**
	 * Accessor to the Ilog CP Optimizer solver object.
	 * 
	 * @return The concrete <code>IloCP</code> solver.
	 */
	public  IloCP solver() {
		return concreteSolver;
	}

	public String toString() {
		try {
			return "Ilog CP Optimizer " + concreteSolver.getVersion();
		} catch(IloException e) {
			e.printStackTrace();
			return "CP Error!";
		}
	}
	
	/**
	 * Adds the value of <code>concreteVar</code> variable to the list of solution 
	 * variable values <pre>solution</pre>. The value added is part of the solution 
	 * found by the solver. Some variables may not have any value in this solution 
	 * if they were not extracted from the model by the solver algorithm.
	 * 
	 * @param concreteVar The variable for which we want the value in the current 
	 *                    solver solution.
	 * @return TODO
	 */
	public Number getValue(IloIntVar concreteVar) {
		return new Integer((int)concreteSolver.getValue(concreteVar));
	}
	
	/**
	 * Creates a concrete IloCP variable that can be used in constraints handled by
	 * CP solver.
	 * Boolean variables in CP are integer variables with domain <pre>[0,1]</pre>.
	 * 
	 * @param name Name of the variable.
	 * @param type Type of the variable. Only Boolean and integer types are supported.
	 * @param min Lower bound of an integer variable domain. Ignored for boolean variables.
	 * @param max Upper bound of an integer variable domain. Ignored for boolean variables.
	 * @return A variable of the given name, type, and domain that can be used in CP
	 *         concrete constraints. 
	 */
	public IloIntVar createVar(String name, Type type, int min, int max) {
		try {
			switch(type) {
			case BOOL:
				return concreteSolver.boolVar(name);
			case INT:
				return concreteSolver.intVar(min, max, name);
			default:
				System.err.println("Error (createVar): CP does not support this type ("
						+ type 
						+ ") for a variable!");
				System.exit(18);
				return null;
			}
		} catch (IloException e) {
			System.err.println("Error when adding var in Ilog CP Optimizer constraint "
					           + "system!");
			e.printStackTrace();
			System.exit(18);
			return null;
		}
	}

}
