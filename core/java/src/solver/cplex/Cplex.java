package solver.cplex;

import java.io.OutputStream;

import expression.variables.Variable;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;

import solver.ConcreteSolver;
import validation.Validation;
import validation.Validation.VerboseLevel;

public class Cplex implements ConcreteSolver {
	
	protected IloCplex solver; // the concrete solver

	public Cplex() {
		try {
			solver = new IloCplex();

			// put emphasis on feasability
		    //solver.setParam(IloCplex.IntParam.Reduce, 1);
		    //solver.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
		    solver.setParam(IloCplex.IntParam.MIPEmphasis, IloCplex.MIPEmphasis.Feasibility);

		    //If the verbosity level is too quiet to print solver messages, set output
		    //to a dummy OutputStream discarding writes. 
			if(Validation.verboseLevel.compareTo(VerboseLevel.VERBOSE) < 0) {
				solver.setOut(new OutputStream(){public void write(int b){}});
				solver.setWarning(new OutputStream(){public void write(int b){}});
			}
		} catch (IloException e) {
			System.err.println("Error when creating Ilog CPLEX solver!" );
			e.printStackTrace();
			System.exit(5);
		} 
	}
	
	public IloIntVar createConcreteVar(Variable v) {
		try {
			switch (v.type()) {
			case BOOL:
				return solver.intVar(0, 1, v.name());
			case INT:
				return solver.intVar(Validation.INTEGER_MIN_BOUND,
						 			 Validation.INTEGER_MAX_BOUND,
						 			 v.name());
			default:
				System.err.println("Error (Cplex): unsupported variable type (" + v.type() + ")!");
				System.exit(-1);
			}
		} catch (IloException e) {
			System.err.println("Error (Cplex): unable to create variable (" + v + ")!");
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	//TODO : vérifier la sémantique du solve !
	// si solve est faux, il peut y avoir quand même une solution
//	A Boolean value reporting whether a feasible solution has been found. 
//	This solution is not necessarily optimal. If false  is returned, 
//	a feasible solution may still be present, but IloCplex has not 
//	been able to prove its feasibility.
	public boolean next() {
		boolean next = false;
		try {
			next = solver.solve();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return next;
	}


	public void startSearch() {
	}


	public void stopSearch() {
		//This does not just stop the current search...
		//solver.end();
	}
	
	public  IloCplex getIloCplex() {
		return solver;
	}
	
	public IloCplex.Status getCplexStatus() {
		try {
			return getIloCplex().getStatus();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String toString() {
		try {
			return "CPLEX " + solver.getVersion();
		} catch(IloException e) {
			e.printStackTrace();
			return "CPLEX Error!";
		}
	}
}
