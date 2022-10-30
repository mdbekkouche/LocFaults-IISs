package solver.ilocp;

import ilog.concert.IloIntVar;

import java.util.Map;

import expression.variables.ArrayVariable;
import expression.variables.ConcreteArrayVariable;
import expression.variables.ConcreteVariable;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;

import solver.Solver.SolverEnum;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.system.xml.IntegerValidationCSP;
import validation.system.xml.ValidationSystem;
import validation.util.Type;

/** 
 * This class implements a concrete integer validation CSP handled by 
 * Ilog CP Optimizer.
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 */
public class IloCPIntegerValidationCSP extends IntegerValidationCSP {
			
	/**
	 * Constructs a concrete integer non linear CSP handled by Ilog CP
	 * Optimizer.
	 * 
	 * @param name The name of the CSP.
	 * @param format The integer format as a number of bits.
	 */
	public IloCPIntegerValidationCSP(String name, ValidationSystem vs) {
		super(name, SolverEnum.ILOG_CP_OPTIMIZER, vs);
	}
		
	/**
	 * Creates:
	 *   - the store of constraints that will be handled by the concrete solver;
	 *   - the store of integer array variables that will be handled by the concrete 
	 *     solver;
	 *   - the store of integer variables that will be handled by the concrete solver.
	 * 
	 * This method is called by a mother class constructor.
	 *  
	 * @param s The solver associated with this integer CSP. It must be of type 
	 *          {@link solver.IloCPSolver}.    
	 */	
	@Override
	protected void initCSP() {
		constr = new IloCPConstraintBlock(((IloCPSolver)this.cspSolver).solver());		
		varBlock = new IloCPIntVarBlock((IloCPSolver)this.cspSolver);
		arrayVarBlock = new IloCPIntArrayVarBlock(
				(IloCPSolver)this.cspSolver);
		visitor = new IloCPExpressionVisitor(
				((IloCPSolver)cspSolver).concreteSolver, 
				(IloCPIntVarBlock)varBlock, 
				(IloCPIntArrayVarBlock)arrayVarBlock);
	}

	@Override
	public void add(Map<ArrayVariable, Number[]> arrays) {
		((IloCPIntArrayVarBlock)arrayVarBlock).add(arrays, this);
	}

	@Override
	public Map<ArrayVariable, Number[]> getArraysValues() {
		return ((IloCPIntArrayVarBlock)arrayVarBlock).getArraysValues();
	}

	@Override
	public Solution solution() {	
		Solution sol = new Solution(this.getElapsedTime());
		Number val;
		Variable var;
	
		try {
			for(ConcreteVariable<IloIntVar> concreteVar: (IloCPIntVarBlock)varBlock) {
				try {
					val = ((IloCPSolver)cspSolver).getValue(concreteVar.concreteVar());
					var = concreteVar.clone();
					if (var.type() == Type.BOOL) {
						if (val.intValue() == 0) {
							var.setConstant(new Boolean(false));
						}
						else {
							var.setConstant(new Boolean(true));
						}
					}
					else {
						var.setConstant(val);
					}
					sol.add(var);
				} catch (Exception e) {
					if (e instanceof ilog.concert.IloException) {
						if (VerboseLevel.VERBOSE.mayPrint()) {
							System.out.println("Variable " 
									+ concreteVar.concreteVar() 
									+ " was not extracted by the solver"
									+ " and is not part of the solution.");
						}
					}
					else {
						throw e;
					}
				}
			}
			for(ConcreteArrayVariable<IloIntVar[]> concreteArrayVar: 
					(IloCPIntArrayVarBlock)arrayVarBlock) 
			{
				IloIntVar[] concreteArray = concreteArrayVar.concreteArray();
				for(int i = 0; i < concreteArray.length; i++) {
					try {
						//We know we're dealing with integer values.
						val = ((IloCPSolver)cspSolver).getValue(concreteArray[i]);
						sol.add(new Variable(concreteArray[i].getName(), Type.INT, val, val));
					} catch (Exception e) {
						if (e instanceof ilog.concert.IloException) {
							if (VerboseLevel.VERBOSE.mayPrint()) {
								System.out.println("Variable " 
										+ concreteArray[i]
										+ " was not extracted by the solver"
										+ " and is not part of the solution.");
							}
						}
						else {
							throw e;
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("addValue method: Unexepected exception caught!");
			e.printStackTrace();
			System.exit(21);
		}
		return sol;
	}
	
	@Override
	public DomainBox domainBox() {	
		DomainBox box = new DomainBox();
		Number val;
		Variable var;
	
		try {
			for(ConcreteVariable<IloIntVar> concreteVar: (IloCPIntVarBlock)varBlock) {
				try {
					//We know we're dealing with integer values.
					val = ((IloCPSolver)cspSolver).getValue(concreteVar.concreteVar());
					var = concreteVar.clone();
					if (var.type() == Type.BOOL) {
						if (val.intValue() == 0) {
							var.setConstant(new Boolean(false));
						}
						else {
							var.setConstant(new Boolean(true));
						}
					}
					else {
						var.setConstant(val);
					}
					box.add(var.domain());
				} catch (Exception e) {
					if (e instanceof ilog.concert.IloException) {
						if (VerboseLevel.VERBOSE.mayPrint()) {
							System.out.println("Variable " 
									+ concreteVar .concreteVar()
									+ " was not extracted by the solver"
									+ " and is not part of the solution.");
						}
					}
					else {
						throw e;
					}
				}
			}
			for(ConcreteArrayVariable<IloIntVar[]> concreteArrayVar: 
					(IloCPIntArrayVarBlock)arrayVarBlock) 
			{
				IloIntVar[] concreteArray = concreteArrayVar.concreteArray();
				for(int i = 0; i < concreteArray.length; i++) {
					try {
						//We know we're dealing with integer values.
						val = ((IloCPSolver)cspSolver).getValue(concreteArray[i]);
						box.add(new VariableDomain(
								  new Variable(concreteArray[i].getName(), Type.INT), val, val));
					} catch (Exception e) {
						if (e instanceof ilog.concert.IloException) {
							if (VerboseLevel.VERBOSE.mayPrint()) {
								System.out.println("Variable " 
										+ concreteArray[i]
										+ " was not extracted by the solver"
										+ " and is not part of the solution.");
							}
						}
						else {
							throw e;
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("addValue method: Unexepected exception caught!");
			e.printStackTrace();
			System.exit(21);
		}
		return box;
	}

}
