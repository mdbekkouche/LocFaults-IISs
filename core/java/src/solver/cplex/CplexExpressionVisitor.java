package solver.cplex;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import validation.Validation;
import validation.util.OpCode;
import validation.util.Type;
import expression.Expression;
import expression.ExpressionVisitor;
import expression.MethodCall;
import expression.ParenExpression;
import expression.logical.AndExpression;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.Comparator;
import expression.logical.InfEqualExpression;
import expression.logical.InfExpression;
import expression.logical.JMLAllDiff;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NondetAssignment;
import expression.logical.NotExpression;
import expression.logical.OrExpression;
import expression.logical.SupEqualExpression;
import expression.logical.SupExpression;
import expression.numeric.BinaryExpression;
import expression.numeric.DivExpression;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.numeric.NumericExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;


/**
 * Expression visitor that creates CPLEX constraints Ilo Concert Cplex modeling.
 * An error state is managed internally that must be reset ({@link #resetFailStatus()} 
 * before any visit of an expression (except for the first one).
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 * @author lydie
 *
 */
public class CplexExpressionVisitor implements ExpressionVisitor {

	
//	static final int SUBST=1;
//	static final int PRECOND=2;
	
	protected CplexIntVarBlock intVar; // integer variables
	protected CplexIntArrayVarBlock intArrayVar; // integer arrays
	protected boolean fail; // to stop
	protected String failReason; // Reason for stopping
//	private int status; // give information about the parsing we are doing

	private boolean substitute;
//	private boolean lastPostCond; // to know if we must link last array renaming 
	// and last variables representing array indexes
	private String toSubstitute; // name of variable to substitute
	private NumericExpression value; // value to substitute
	
	protected IloCplex solver;
	protected IloConstraint cplexTrue;
	protected IloConstraint cplexFalse;
	protected IloIntExpr cplexCstInt0;
	protected IloIntExpr cplexCstInt1;
	
	public CplexExpressionVisitor(CplexIntVarBlock i,CplexIntArrayVarBlock t) {
		intVar = i;
		fail=false;
		substitute=false;
		intArrayVar = t;
		solver = intVar.getIloCplex();
		try {
			cplexCstInt0 = solver.constant((int)0);
			cplexCstInt1 = solver.constant((int)1);
			cplexTrue = solver.eq(cplexCstInt0, cplexCstInt0);
			cplexFalse = solver.not(cplexTrue);
		} catch (IloException e) {
			System.err.println("Error while creating constraint in "
					+ "Ilog CPLEX for boolean value "
					+ "!");
			e.printStackTrace();
			System.exit(8);			
		}
	}	
	
	// constructor used when adding a precondition
	// to be able to set constant values
	public CplexExpressionVisitor(HashMap<String,ArrayElement> arrayElt, CplexIntVarBlock i,CplexIntArrayVarBlock t) {
		this(i,t);
		substitute=false;
	}

//	/** 
//	 * returns a list of constraints to be added in the concrete solver
//	* when the constraint is a and operator, returns the list of terms
//	*/
//	public Object visit(Constraint constraint, Object data) {
//		if (fail)
//			return null;
//		ArrayList<IloConstraint> a = new ArrayList<IloConstraint>();
//		Object o = constraint.getExpression().structAccept(this, data);
//		//It is either an IloConstraint or an ArrayList<IloConstraint>
//		if (o instanceof ArrayList) {
//			ArrayList and = (ArrayList)o;
//			for (Object c : and)
//				a.add((IloConstraint)c);
//		}
//		else 
//			a.add((IloConstraint)o);
//		return a;
//	}
	
	public void resetFailStatus() {
		fail = false;
		failReason = "Uknown reason.";
	}

	public IloConstraint visit(Assignment assignment) {
       	if (fail)
			return null;
       	
       	Variable var = assignment.lhs();
		IloIntExpr cvar = intVar.getCplexVar(var);
		IloIntExpr val = (IloIntExpr)assignment.rhs().structAccept(this);

		//val may be an array element and thus be null with fail set to true
		if(fail)
			return null;

		IloConstraint c = null;
		try {
			if (var.type() == Type.BOOL) {
				c = solver.ifThen((IloConstraint)val, solver.eq(cvar, this.cplexCstInt1));
				c = solver.and(c, solver.ifThen(solver.not((IloConstraint)val), solver.eq(cvar, this.cplexCstInt0)));
			}
			else {
				c = solver.eq(cvar, val);
			}
		} catch (IloException e) {
			error(1);
			e.printStackTrace();
		}
		
		return c;
	}
	
	@Override
	public Object visit(NondetAssignment assignment) {
		// Nothing to be done except maybe create the assigned variable if it has not
		// already been created
		intVar.getCplexVar(assignment.lhs());
		return null;
	}

	
	public Object visit(ParenExpression parenExpression) {
		if (fail)
			return null;
		return parenExpression.arg1().structAccept(this);
	}

	public IloNumExpr visit(Variable v) {
		if (fail)
			return null;
		else if (substitute && v.root().equals(toSubstitute))
			return (IloNumExpr)value.structAccept(this);
		else if (v.type() == Type.BOOL) {
			try {
				return solver.eq(intVar.getCplexVar(v), this.cplexCstInt1);
			} catch (IloException e) {
				System.err.println("Error (CplexExpressionVisitor): unable to create logical constraint from boolean variable!");
				e.printStackTrace();
				System.exit(-1);
				return null;
			}
		}
		else
			return intVar.getCplexVar(v);
	}

	public IloIntExpr visit(IntegerLiteral literal) {
		if (fail)
			return null;
		try {
			return solver.constant((int)literal.constantNumber().intValue());
		} catch (IloException e) {
			System.err.println("Error when creating cplex int constant!");
			e.printStackTrace();
			System.exit(6);
		}
		return null;
	}

	// adds a constant and returns the IlcNumExpr
	private IloNumExpr createConstant(double val) {
		try {
			return solver.constant(val);
		} catch (IloException e) {
			System.err.println("Error when creating cplex double constant!");
			e.printStackTrace();
			System.exit(6);
		}
		return null;
	}	

	public IloNumExpr visit(FloatLiteral literal) {
		if (fail)
			return null;
		return createConstant(literal.constantNumber().floatValue());
	}

	public IloNumExpr visit(DoubleLiteral literal) {
		if (fail)
			return null;
		return createConstant(literal.constantNumber().doubleValue());
	}

	public IloConstraint visit(LogicalLiteral literal) {
		if (fail)
			return null;

			//There is no predefined true and false constraints in CPLEX
			if(literal.constantBoolean())
				return this.cplexTrue;
			else
				return this.cplexFalse;
	}
	
	public Object visit(ArrayAssignment tab) {
		if (fail)
			return null;

		NumericExpression index = tab.index();
		ArrayVariable t = tab.array();
		IloIntExpr val = (IloIntExpr)tab.rhs().structAccept(this);

		//val may be an array element and thus be null with fail set to true
		if(fail)
			return null;
		
		int indexVal = -1;
		if (index.isConstant()) {
			//We assume index is an integer value: we rely on the Java compiler for that
			indexVal = index.constantNumber().intValue();
		}
		else 
			indexVal = computeCplexIndexValue(index, toSubstitute, value);

		if (indexVal!=-1) {
			if (indexVal >= t.length() || indexVal<0){
				System.err.println("Array out of bound!");
//				throw new ExecutionException("Array out of bound exception");
			}
			// get the cplex var that corresponds to indexVal
			IloIntVar cplexTab= intArrayVar.getNextVar(t.root(),indexVal);
//			///			System.out.println("l'élément " +cplexTab[indexVal]);

			try {
				ArrayList<IloConstraint> constr = new ArrayList<IloConstraint>();
				// index "indexVal" takes value val
				constr.add(solver.eq(cplexTab,val));
////				System.out.println("array assign " + t.name() + " " + s.eq(cplexTab,val));
//				// other indexes are unchanged
////				for (int i=0;i<t.length();i++)
////					if (i!=indexVal)
////						constr.add(s.eq(cplexTab[i],cplexPrevTab[i]));
////				System.out.println("cplex visitor array assign") ;
////				System.out.println("index " + indexVal + " constr " +  constr);
				return constr; 
			} catch (IloException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		fail=true;
		failReason = "Access to an unbounded index of array!";
		return null;
	}
	
	public IloIntVar visit(ArrayElement tab) {
		if (fail)
			return null;

		NumericExpression index = tab.index();
		ArrayVariable t = (ArrayVariable)tab.array();
		int indexVal = -1;
		if (index.isConstant()) {
			//We assume index is an integer value: we rely on the Java compiler for that
			indexVal = index.constantNumber().intValue();
		}
		else 
			indexVal = computeCplexIndexValue(index, toSubstitute, value);
		if(indexVal != -1) {
			if(indexVal >= t.length() || indexVal<0) {	
				System.err.println("Array out of bound!");
				//throw new Exception("Array out of bound exception");
			}
			// if we are adding the postcondition, we need to associate 
			// last array renaming and last variable representing indexes
				return intArrayVar.getLastVar(t.root(),indexVal);
//			System.out.println("l'élément " +cplexTab[indexVal]);
		}
		fail=true;
		failReason = "Access to an unbounded index of array!";
		return null;
	}
		
	private int computeCplexIndexValue(NumericExpression index, 
			                           String toSubstitute, 
			                           NumericExpression var)
	{
		IloIntExpr ind = (IloIntExpr)index.structAccept(this);
		//ind may be an array element and thus be null with fail set to true
		if(fail)
			return -1;

		int val = -1;
		boolean bounded=false;
		IloIntVar i = null;
		try {
			i = solver.intVar(Validation.INTEGER_MIN_BOUND, Validation.INTEGER_MAX_BOUND);
			solver.add(solver.eq(i,ind));
			bounded = solver.solve();
		} catch (IloException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (bounded) {
			try {
				val = (int)solver.getValue(i);
			} catch (IloException e) {
				System.err.println("Unbounded!"); 
			}
			if (val!=-1) {
				bounded = false;
				try{
					IloConstraint diff = solver.not(solver.eq(ind, val));
					solver.add(diff);
					if (!solver.solve()) {
						bounded=true;
					}
					solver.remove(diff);
				} catch (IloException e) {
					System.err.println("Unbounded!"); 
				}
			}
			if (bounded) {
				return val;
			}
		}
		return -1;
	}


	// Cplex syntax of integer operators
	// PRECOND : expression is linear
	private String cplexIntSyntax(BinaryExpression expression) {
		switch (expression.opCode()) {
		case ADD:
			return "sum";
		case SUB:
			return "diff";
		case MUL:
			return "prod";
		case DIV:
			return "div";
		default:
			System.err.println("Error (CplexIntSyntax): unknown operator (" + expression.opCode() + ")!");
			return "";
		}
	}  	

	// invoke method "name" and returns a IloIntExpr
	// using java reflect
	private IloIntExpr invokeIloIntExpr(String name,Object[] args) {
		IloIntExpr exp = null;
		try {
			Class<?> sc = Class.forName("ilog.concert.IloModeler");
			Class<?> intExp = Class.forName("ilog.concert.IloIntExpr");
			Class<?>[] param = {intExp,intExp};
			Method m = sc.getDeclaredMethod(name, param);
			exp = (IloIntExpr)m.invoke(solver, args);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return exp;
	}
	
	public IloNumExpr visit(BinaryExpression expression) {
		if (fail)
			return null;
		// TODO : les parenthèses
		// it is a div expression with constant values
		if (expression instanceof DivExpression) {
			try {
				if(expression.isConstant()) {
					return solver.constant(expression.constantNumber().doubleValue());
				}
				else if (expression.arg2().isConstant()) {
					double coeff = 1 / (expression.arg2().constantNumber().doubleValue());
					return solver.prod((IloIntExpr)expression.arg1().structAccept(this), coeff);
				}
				else {
					fail = true;
					failReason = "Ilog CPLEX cannot handle a division expression with "
						+ "non constant values!";
					return null;
				}
			} catch (IloException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IloIntExpr[] args = new IloIntExpr[2];
		args[0] = (IloIntExpr)expression.arg1().structAccept(this);
		args[1] = (IloIntExpr)expression.arg2().structAccept(this);
		//args may be array elements and thus be null with fail set to true
		if(fail)
			return null;
		else
			return invokeIloIntExpr(cplexIntSyntax(expression),args);
	}
	
	public IloConstraint visit(OrExpression exp) {
		if (fail)
			return null;
		IloConstraint c = null;
		OrExpression o = (OrExpression)exp;
		try {
			IloConstraint a1 =  (IloConstraint)o.arg1().structAccept(this);
			IloConstraint a2 =  (IloConstraint)o.arg2().structAccept(this);
			// cas des JMLExist vides
			if (a1==null) c=a2;
			else if (a2==null) c=a1;
			else
				c = solver.or(a1,a2);
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
	}
	
	public IloConstraint visit(AndExpression exp) {
		if (fail)
			return null;
		return visiteAux(exp, null);
	}

	/**
	 * Builds a constraint which is the conjunction of the linear arguments of 
	 * <code>exp</code>.
	 *  
	 * @param exp
	 * @param c The constraint being built. On first call should be <code>null</code>.
	 * @return An <pre>and</pre> concrete constraint.
	 */
	private IloConstraint visiteAux(Expression exp, IloConstraint c) {
		if (fail)
			return null;

		IloConstraint result = c;

		if (exp instanceof AndExpression) {
			   result = visiteAux(((AndExpression)exp).arg1(), c);
			   result = visiteAux(((AndExpression)exp).arg2(), result);
		}
		else if (exp.isLinear()) {
			result = (IloConstraint)exp.structAccept(this);
			if (fail)
				return null;
			if(c != null) {
				try {	
					result = solver.and(result, c);
				} catch(IloException e) {
					System.err.println("Error while creating an 'and' constraint in "
							+ "Ilog Cplex for expression " + exp + "!");			
					e.printStackTrace();
					System.exit(7);
				}
			}
		}
		return result;
	}
			
	public IloConstraint visit(JMLImpliesExpression jmlImplies) {
		IloConstraint result = null;
		IloConstraint arg1 = (IloConstraint)(new NotExpression(jmlImplies.arg1())).structAccept(this);
		IloConstraint arg2 = (IloConstraint)jmlImplies.arg2().structAccept(this);
		
		if(fail)
			return null;
		
		try {
			result = solver.or(arg1, arg2);
		} catch(IloException e) {
			System.err.println("Error while creating an 'or' constraint in "
					+ "Ilog Cplex for expression " + jmlImplies + "!");			
			e.printStackTrace();
			System.exit(37);								
		}
		return result;
	}
	
	public IloConstraint visit(JMLForAllExpression jmlForAll) {
		IloConstraint result = null;

		LogicalExpression expr = jmlForAll.condition();
		Variable varIndex = jmlForAll.index();
		List<IntegerLiteral> val = jmlForAll.enumeration();

		if(val != null) {
			IntegerLiteral valIndex = val.get(0);
			result = (IloConstraint)expr.substitute(varIndex, valIndex).structAccept(this);
			//If fail is not set to true here, it won't be neither in the calls to structAccept 
			//in the for loop
			if (fail)
				return null;
			for (int i=1; i<val.size(); i++) {
				try {
					result = solver.and(
						(IloConstraint)expr.substitute(varIndex, val.get(i)).structAccept(this),
						result);
				} catch(IloException e) {
					System.err.println("Error while creating an 'and' constraint in "
							+ "Ilog Cplex for expression " + jmlForAll + "!");			
					e.printStackTrace();
					System.exit(7);					
				}
			}
		}
		else {
			System.err.println("CPLEX Error : Not Enumerable ForAll !");
			System.err.println(jmlForAll);
			System.exit(-1);			
		}
		return result;
	}
	

	public IloConstraint visit(JMLExistExpression jmlExist) {
		IloConstraint result = null;

		LogicalExpression expr = jmlExist.condition();
		Variable varIndex = jmlExist.index();
		List<IntegerLiteral> enumeration = jmlExist.enumeration();
		
		if(enumeration != null) {
			IntegerLiteral valIndex = enumeration.get(0);
			result = (IloConstraint)expr.substitute(varIndex, valIndex).structAccept(this);
			//If fail is not set to true here, it won't be neither in the calls to structAccept 
			//in the for loop
			if (fail)
				return null;
			for (int i=1; i<enumeration.size(); i++) {
				try {
					result = solver.or(
						(IloConstraint)expr.substitute(varIndex, enumeration.get(i)).structAccept(this),
						result);
				} catch(IloException e) {
					System.err.println("Error while creating an 'or' constraint in "
							+ "Ilog Cplex for expression " + jmlExist + "!");			
					e.printStackTrace();
					System.exit(7);					
				}
			}
		}
		else {
			System.err.println("CPLEX Error : Not Enumerable Exist !");
			System.err.println(jmlExist);
			System.exit(-1);			
		}
		return result;
	}
	
	public Object visit(JMLAllDiff expression) {
		System.err.println("Visiting JMLAllDiff in Cplex!");
		fail = true;
		failReason = "Visiting JMLAllDiff in Cplex!";
		return null;
	}

	// apply De morgan law at one level:
	// !!(exp) -> exp
	// !(exp1||exp2) -> !(exp1) & !(exp2)
	/**
	 * Builds a concrete <pre>not</pre> constraint.
	 * 
	 * It seems that CPlex does not handle the negation of logical <pre>or</pre> 
	 * and <pre>and</pre> constraints (it throws an exception if we try 
	 * <code>cplex.not(cplex.and(c1,c2))</code>); it expects a <code>CpxRange</code> 
	 * object with the <code>not</code> method (but this is not documented!). 
	 * So we apply De morgan laws to propagate the negation towards constraints free 
	 * of logical operators.
	 * <pre>!!(exp) -> exp</pre> and <pre>!(exp1|exp2) <-> !(exp1) & !(exp2)</pre>.
	 * We also have to treat the cases of JML <pre>Forall</pre> and <pre>Exists</pre> 
	 * expressions as they generate <pre>or</pre> and <pre>and</pre> constraints.
	 * 
	 * @param expression The <pre>not</pre> expression to visit.
	 */
	public Object visit(NotExpression expression) {
		if (fail)
			return null;
		
		LogicalExpression a1 = expression.arg1();
		if (a1 instanceof NotExpression) {
			return ((NotExpression)a1).arg1().structAccept(this);
		}
		else if (a1 instanceof OrExpression) {
			OrExpression o = (OrExpression)a1;
			AndExpression a = new AndExpression(new NotExpression(o.arg1()),
					new NotExpression(o.arg2()));
			return a.structAccept(this);
		} 
		else if (a1.isComparison() && (((Comparator)a1).opCode() != OpCode.EQU)) {
			return negateComparator((Comparator)a1);
		}
		else if (a1 instanceof AndExpression) {
			AndExpression a = (AndExpression)a1;
			OrExpression o = new OrExpression(new NotExpression(a.arg1()),
					new NotExpression(a.arg2()));
			return o.structAccept(this);
		} 
		else if (a1 instanceof JMLImpliesExpression) {
			JMLImpliesExpression e = (JMLImpliesExpression)a1;
			AndExpression a = new AndExpression(e.arg1(), new NotExpression(e.arg2()));
			return a.structAccept(this);
		}
		else if (a1 instanceof JMLForAllExpression) {
			JMLForAllExpression e = (JMLForAllExpression)a1;
			//If the bound is enumerable, it is not mandatory to negate it, but in the other case, 
			//we need to expand the implicit implication and negate it as a whole.
			if(e.enumeration() != null)
				return (new JMLExistExpression(
								e.index(), 
								e.boundExpression(), 
								new NotExpression(e.condition()))).structAccept(this);
			else //TODO: Normally, at this level, a non enumerable quantifier could already be 
				 //       in the form (var ; true ; body) so we could just take the negation of body 
				 //       (it would then be useless to create this new 'and' expression).
				return (new JMLExistExpression(
								e.index(), 
								new LogicalLiteral(true), 
								new AndExpression(
										e.boundExpression(), 
										new NotExpression(e.condition())))).structAccept(this);
				
//			LogicalExpression expr = e.condition();
//			IntegerVariable varIndex = e.index();	
//			ArrayList<IntegerLiteral> varIndexValues = e.enumerate();
//			LogicalExpression result = new NotExpression(
//					(LogicalExpression)expr.substitute(varIndex, varIndexValues.get(0)));
//			for(int i = 1; i < varIndexValues.size(); i++) {
//				result = new OrExpression(
//							new NotExpression(
//									(LogicalExpression)expr.substitute(varIndex,
//									        						   varIndexValues.get(i))),
//							result);
//			}
//			return result.structAccept(this, data);
		}  
		else if (a1 instanceof JMLExistExpression) {
			JMLExistExpression e = (JMLExistExpression)a1;
			//If the bound is enumerable, it is not mandatory to negate it, but in the other case, 
			//we need to expand the implicit implication and negate it as a whole.
			if(e.enumeration() != null)
				return (new JMLForAllExpression(
								e.index(), 
								e.boundExpression(), 
								new NotExpression(e.condition()))).structAccept(this);
			else //TODO: Normally, at this level, a non enumerable quantifier could already be 
				 //       in the form (var ; true ; body) so we could just take the negation of body 
				 //       (it would then be useless to create this new 'and' expression).
				return (new JMLForAllExpression(
								e.index(), 
								new LogicalLiteral(true), 
								new OrExpression(
										new NotExpression(e.boundExpression()), 
										new NotExpression(e.condition())))).structAccept(this);				
//			LogicalExpression expr = e.condition();
//			IntegerVariable varIndex = e.index();	
//			ArrayList<IntegerLiteral> varIndexValues = e.enumerate();
//			LogicalExpression result = new NotExpression(
//					(LogicalExpression)expr.substitute(varIndex, varIndexValues.get(0)));
//			for(int i = 1; i < varIndexValues.size(); i++) {
//				result = new AndExpression(
//							new NotExpression(
//									(LogicalExpression)expr.substitute(varIndex,
//									        						   varIndexValues.get(i))),
//							result);
//			}
//			return result.structAccept(this, data);
		}

//		return null;
		IloConstraint c = (IloConstraint)a1.structAccept(this);
		if(fail)
			return null;
		IloConstraint notc = null;
		try {
			notc = solver.not(c);
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return notc;
	}

	// visite a comparison expression (<, <=, >,>=) which has been found 
	// into a not epxression
	// returns the comparison operator to correspond to the negation
	// of a1
	private Object negateComparator(Comparator exp) {
		if (fail)
			return null;
		LogicalExpression l = null;
		Expression a1 = exp.arg1();
		Expression a2 = exp.arg2();
		
		if (exp instanceof InfExpression) 
			 l = new SupEqualExpression(a1,a2);
		if (exp instanceof SupExpression) 
			 l = new InfEqualExpression(a1,a2);
		if (exp instanceof InfEqualExpression) 
			 l = new SupExpression(a1,a2);
		if (exp instanceof SupEqualExpression) 
			 l = new InfExpression(a1,a2);
		return l.structAccept(this);
	}
	
	// Cplex syntax of comparator operators
	// PRECOND : the comparison operator is not strict 
	private String cplexCompareSyntax(Comparator e) {
		switch (e.opCode()) {
		case GEQ:
			return "ge";
		case LEQ:
			return "le";
		case EQU:
			return "eq";
		default:
			System.err.println("Error (jSolverCompareSyntax): unsupported operator (" + e.opCode() + ")!");
			return "";
		}
	}

	// when comparison is strict, add a delta variable
	//TODO : be sure that the delta could be added to the variable
	// without overflow
	private IloConstraint invokeStrictComparator(Comparator expression, IloIntExpr[] args) {
		if (fail)
			return null;
		IloConstraint c = null;
		try {
			if (expression.opCode() == OpCode.GT)
				c = solver.ge(args[0], solver.sum(args[1], 1));
			else 
				c = solver.le(args[0], solver.diff(args[1], 1));
		} catch (IloException e) {
			error(2);
			e.printStackTrace();
		}
		return c;
	}
	
	/** invoke method "name" and returns a IloConstraint
	* the method is either in class "IloIntExpr" (b=true)
	* or in class "IloConstraint" (b=false) 
	* using java reflect */
	private IloConstraint invokeIloConstraint(String name, Object[] args, boolean b) {
		if (fail)
			return null;
		String className = (b) ? "ilog.concert.IloNumExpr"
				: "ilog.concert.IloConstraint";
		IloConstraint exp = null;
		try {
			Class<?> sc = Class.forName("ilog.concert.IloModeler");
			Class<?> intExp = Class.forName(className);
			Class<?>[] param = {intExp,intExp};
			Method m = sc.getDeclaredMethod(name, param);
			exp = (IloConstraint)m.invoke(solver, args);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return exp;
	}
	
	public IloConstraint visit(Comparator expression) {
		if (fail)
			return null;
		IloIntExpr[] args = new IloIntExpr[2];
		args[0] = (IloIntExpr)expression.arg1().structAccept(this);
		args[1] = (IloIntExpr)expression.arg2().structAccept(this);
		if (fail)
			return null;		
		if ((expression.opCode() == OpCode.GT) || (expression.opCode() == OpCode.LT))
			return invokeStrictComparator(expression, args);
		else {
			//Beware that this is not sound (see Tests/XML/Basique/Disjunction.xml)
			//if ((expression.opCode() == OpCode.EQU) && precond) 
				//setConstantArrayElement(expression);
			return invokeIloConstraint(cplexCompareSyntax(expression), args, true);
		}
	}

//	private void setConstantArrayElement(Comparator expression){
//		// need to consider the equality as an assignment
//		Expression first = expression.arg1();
//		Expression second = expression.arg2();
//		if (first instanceof Variable) {
//			if (second.isConstant() && (second instanceof NumericExpression)) {
//				((Variable)first).setConstantNumber(((NumericExpression)second).constantNumber());
//			}
//		}
//		else if (first instanceof ArrayElement) {
//			ArrayElement tab = (ArrayElement)first;
//			NumericExpression index = tab.index();
//			if (index.isConstant() && second.isConstant() && (second instanceof NumericExpression)) {
//				String eltName = tab.array().name()+"_"+index.constantNumber();
//				if (intArrayElt.containsKey(eltName)) {
//					//System.out.println("known in NumericExprVisitor " + eltName);
//					//elt = intArrayElt.get(eltName);
//				}
//				else {
//					intArrayElt.put(eltName,tab);
//					//System.out.println("added in NumericExprVisitor " + eltName + " " +tab);
//				}
//				tab.setConstantNumber(((NumericExpression)second).constantNumber());
//				//System.out.println("tab " + tab);
//			}
//		}	
//	}
	
	protected void error(int n) {
		String s = "error when creating Cplex syntax";
		switch (n) {
		case 1 : s = " of assignment";
			break;
		case 2 : s = " of strict comparison operator";
		break;
		}
		System.err.println(s);
	}



	public Object visit(ArrayVariable variable) {
		fail = true;
		failReason = "Visiting an ArrayVariable in CPLEX!";
		return null;
	}

	public Object visit(MethodCall mc) {
		System.err.println("CPLEX Error : Method calls not allowed with CPLEX !");
		System.err.println(mc);
		fail = true;
		failReason = "Method calls not allowed with CPLEX !";
		return null;
	}

	@Override
	public Object visit(JMLOld expression) {
		return this.visit(expression.oldValue());
	}

}
