package solver.cplex.cfg;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import expression.variables.ArrayVariable;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;

import solver.Solver;
import solver.Solver.SolverEnum;
import solver.cplex.Cplex;
import solver.cplex.CplexConstraintBlock;
import solver.cplex.CplexExpressionVisitor;
import solver.cplex.CplexIntArrayVarBlock;
import solver.cplex.CplexIntVarBlock;

import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.strategies.cfg.localization.CplexLocalizationExpressionVisitor;
import validation.strategies.cfg.localization.RhsExpressionComputer;
import validation.system.cfg.CfgCsp;
import validation.util.Type;

/**
 * This class uses ILOG CPLEX.
 * 
 * @author Olivier Ponsini
 *
 */
public class CplexCfgCsp extends CfgCsp<Cplex,
									 CplexIntVarBlock,
									 CplexIntArrayVarBlock,
									 CplexConstraintBlock,
									 CplexExpressionVisitor> 
{
				
	public CplexCfgCsp(String name) {
		super(name);
		solver = (Cplex)Solver.createSolver(SolverEnum.CPLEX);
		ctrs = new CplexConstraintBlock(solver);
		vars = new CplexIntVarBlock(solver);
		arrayVars = new CplexIntArrayVarBlock(solver);
		visitor = new CplexExpressionVisitor(vars, arrayVars);
	}

	public CplexCfgCsp(String name, RhsExpressionComputer rhsComputer) {
		super(name);
		solver = (Cplex)Solver.createSolver(SolverEnum.CPLEX);
		ctrs = new CplexConstraintBlock(solver);
		vars = new CplexIntVarBlock(solver);
		arrayVars = new CplexIntArrayVarBlock(solver);
		visitor = new CplexLocalizationExpressionVisitor(vars, arrayVars, rhsComputer);
}

	// methods to manage backtrack
	
	/** Saves current CSP. */
	public void save() {
		ctrs.save();
		vars.save();
		arrayVars.save();
	
	}
	
	/** Restores current CSP. */
	public void restore() {
		ctrs.restore();
		vars.restore();
		arrayVars.restore();
	}
	
	public Solution solution() {
		Solution sol = new Solution(this.getElapsedTime());
		Variable var;

		for(IloIntVar v: vars) {
			var = new Variable(v.getName(), Type.INT);
			try {
				var.setConstant(solver.getIloCplex().getValue(v));
				sol.add(var);
			} catch (UnknownObjectException e) {
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println("Variable " + var + " is not part of the solution!");
				}
			} catch (IloException e) {
				if (VerboseLevel.VERBOSE.mayPrint()) {
					e.printStackTrace();
				}
			}
		}
		return sol;
	}	

	public DomainBox domainBox() {
		DomainBox box = new DomainBox();
		try {
			for(IloIntVar v: vars) {
				box.add(new VariableDomain(new Variable(v.getName(), Type.INT), solver.getIloCplex().getValue(v)));
			}
		} catch (IloException e) {
			if (VerboseLevel.VERBOSE.mayPrint()) {
				e.printStackTrace();
			}
		}
		return box;
	}	
	
	public void addArrayVar(ArrayVariable v) {
		for (int i = 0; i < v.length(); i++) 
			vars.add(new Variable(v.name() + '[' + i + ']', v.type(), v.use()));
	}

	
}
