package solver.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import expression.Expression;
import expression.ExpressionVisitor;
import expression.MethodCall;
import expression.ParenExpression;
import expression.logical.AndExpression;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.Comparator;
import expression.logical.InfEqualExpression;
import expression.logical.JMLAllDiff;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NondetAssignment;
import expression.logical.NotExpression;
import expression.logical.OrExpression;
import expression.numeric.BinaryExpression;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;

import validation.util.OpCode;
import validation.util.Type;


/**
 * This class implements a visitor, as in the visitor design pattern,
 * for {@link constraint.Constraint abstract constraints}.
 * This visitor builds a Java expression from an Expression
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 * @author Lydie
 *
 */
public class JavaExpressionVisitor implements ExpressionVisitor {

	/**
	 * The store of integer variables handled by the solver.
	 */
	private JavaVarBlock scalarVar;
	/**
	 * The store of integer array variables handled by the solver.
	 */
	private JavaArrayVarBlock arrayVar;

	
	
	/* random generator used to set values in non deterministic assignments
	 * 
	 */
	private Random r ;

	
	//------------
	//Constructors
	//------------
	
	/**
	 * Constructs a visitor for constraints with the given store of integer 
	 * variables already handled by the solver.
	 * 
	 * @param intVar A store of integer variables already handled by the solver.
	 */
	public JavaExpressionVisitor(JavaVarBlock intVar) {
		this.scalarVar = intVar;
		r = new Random(System.currentTimeMillis());
	}
	
	/**
	 * Constructs a visitor for constraints with the given stores of integer 
	 * variables and integer array variables already handled by the solver.
     *
	 * @param intVar A store of integer variables already handled by the 
	 *               solver.
	 * @param intArrayVar A store of integer array variables already 
	 *                    handled by the solver.
	 */
	public JavaExpressionVisitor(JavaVarBlock intVar, JavaArrayVarBlock intArrayVar) {
		this.scalarVar = intVar;
		this.arrayVar = intArrayVar;
		r = new Random(System.currentTimeMillis());
	}	
	
	
	// to dynamically add variables
	public void addVar(Variable v) {
		scalarVar.add(v);
	}
	
	//-----------------------------------
	//ExpressionVisitor interface methods
	//-----------------------------------
	
	/**
	 * This method visits an assignment expression that will result 
	 * in an equality constraint.
	 * @param assignement The assignment expression to visit.
	 * 
	 * @return A concrete equality constraint of type <code>IloConstraint</code>.
	 */
	public Object visit(Assignment assign) {
		Variable v = assign.lhs();
		Number val = (Number)assign.rhs().structAccept(this);
		scalarVar.setConstantValue(v, val);
//		System.out.println(scalarVar);
		return null;
	}
	

	/**
	 * 
	 * @param t: type of the variable
	 * @return: a random value of type t
	 */
	private Number randomValue(Type t) {
		switch (t) {
		case BOOL:
			return r.nextInt(2);
		case INT:
			return r.nextInt();
		case FLOAT:
			return r.nextFloat();
		case DOUBLE:
			return r.nextFloat();
		default:
			return null;
		}
	}
	
	
	/**
	 * This method visits a non deterministic assignement
	 *  expression that will result in an equality constraint with a
	 *  random value.
	 * @param assignement The assignment expression to visit.
	 * 
	 * @return A concrete equality constraint of type <code>IloConstraint</code>.
	 */
	public Object visit(NondetAssignment assign) {
		Variable v = assign.lhs();
		Number val = randomValue(v.type());
		scalarVar.setConstantValue(v, val);
//		System.out.println("valeur de assign " + val);
//		System.out.println(scalarVar);
		return null;
	}
	
	/**
	 * This method visits a parenthesised expression.
	 * Shouldn't this node be removed from the expression tree ?
	 * @param parenExpression The parenthesised expression to be visited.
	 * 
	 * @return The method can return either:
     * <ul>
     * <li><code>null</code>;</li>
     * <li>a single concrete constraint of type {@link ilog.concert.IloConstraint};</li>
     * <li>a set of concrete constraints of type <code>ArrayList&lt;IloConstraint&gt;</code>;</li>
     * </ul> 
	 */
	public Object visit(ParenExpression parenExpression) {
		return parenExpression.arg1().structAccept(this);
	}


	/**
	 * This method visits an integer variable.
	 * If the variable is already in the store of integer 
	 * variables handled by the concrete solver returns it; otherwise, 
	 * adds a new variable to the store and returns it.
	 * 
	 * This is where substitution of the JML <pre>\result</pre> variable occurs 
	 * if {@link #substitute} is true.
	 * 
	 * @param variable The integer variable to be visited.
	 * @return If no substitution occurs, the method returns a concrete integer 
	 *         variable, i.e. as handled by the concrete
	 *         solver, of type <code>IloIntVar</code>, corresponding to the
	 *         program integer variable <code>variable</code>.
	 *         Otherwise, if substitution occurs, the method may also return a concrete 
	 *         integer expression of type {@link ilog.concert.IloIntExpr}, which 
	 *         is a supertype of <code>IloIntVar</code>.
	 */
	public Object visit(Variable variable) {
		return scalarVar.getValue(variable);
	}

	/**
	 * This method visits an integer literal expression that will result 
	 * in a concrete integer constant expression.
	 * 
	 * @param literal The integer literal expression to visit.
	 * @return A concrete integer constraint of type <code>IloIntExpr</code>.
	 */
	public Object visit(IntegerLiteral literal) {
		return literal.constantNumber();
	}

	/**
	 * This method visits a float literal expression that will result 
	 * in a concrete float constant expression.
	 * 
	 * @param literal The float literal expression to visit.
	 * @return A concrete float constraint of type <code>IloNumExpr</code>.
	 */
	public Object visit(FloatLiteral literal) {
		return literal.constantNumber();
	}
	/**
	 * This method visits a double literal expression that will result 
	 * in a concrete double constant expression.
	 * 
	 * @param literal The double literal expression to visit.
	 * @return A concrete double constraint of type <code>IloNumExpr</code>.
	 */
	public Object visit(DoubleLiteral literal) {
		return literal.constantNumber();
	}

	/**
	 * This method visits a logical literal expression that will result 
	 * in a concrete logical expression.
	 * 
	 * @param literal The logical literal expression to visit.
	 * @return A concrete constraint of type <code>IloConstraint</code>.
	 */
	public Object visit(LogicalLiteral literal) {
		if(literal.constantBoolean())
			return new Integer(1);
		return new Integer(0);
	}

	/**
	 * This method visits an array assignement expression that will result 
	 * in several concrete constraints.
	 * 
	 * When an element at index <pre>i</pre> of an array <pre>A_k</pre> is assigned, 
	 * the array variable gets a new renaming number <pre>A_(k+1)</pre>. This new array 
	 * is such that for all <pre>j != i</pre>, <pre>A_(k+1)[j] = A_k[j]</pre>, and
	 * <pre>A_(k+1)[i]</pre> takes the value assigned.
	 * The constraints generated reflect the values of the elements of <pre>A_(k+1)</pre>.
	 * @param eltArrayAssign The array assignment expression to visit.
	 *                       It includes an array <code>t</code>, an index 
	 *                       <code>idx</code>, and a value <code>val</code> such that
	 *                       <code>t[idx]=val</code>.
	 * 
	 * @return A (maybe empty) set of concrete integer constraints of type 
	 *         <code>ArrayList&lt;IloConstraint&gt;</code>.
	 */
	public Object visit(ArrayAssignment eltArrayAssign) {
		System.err.println("Java visitor for arrays is not yet implemented");
		return null;
	}

	/**
	 * This method visits an integer array variable, this will result 
	 * in an array of concrete integer variables.
	 * @param array The array variable to visit.
	 * 
	 * @return The array of concrete integer variables (of Type <code>IloIntVar[]</code>)
	 *         corresponding to the array variable <code>array</code>.
	 */
	public Object visit(ArrayVariable array) {
		System.err.println("Java visitor for arrays is not yet implemented");
		return arrayVar.getValue(array);
	}

	
	/**
	 * This method visits an integer array element expression that will result 
	 * in a concrete expression.
	 * @param elt The array element expression to visit.
	 *            It includes an array <code>t</code>, and an index 
	 *            <code>idx</code>.
	 * 
	 * @return Either a concrete integer variable of type <code>IloIntVar</code>
	 *         if the index value of the element could be statically evaluated; 
	 *         or a concrete expression <code>element</code> of type 
	 *         <code>IloIntExpr</code>; or <code>null</code> if something went wrong.
	 */
	public Object visit(ArrayElement elt) {
		System.err.println("Java visitor for arrays is not yet implemented");
		return null;
	}
	

	/**
	 * Invokes method <code>name</code> on the concrete solver Java
	 * 
	 * @param operator The operator's code.
	 * @param args The arguments of the method to invoke. 
	 * @return The result of the invocation of method <code>name</code> with 
	 *         arguments <code>args</code> on the concrete solver, which will be
	 *         an <code>IloIntExpr</code> expression or <code>null</code> if something 
	 *         went wrong.
	 */
	private Object invokeJavaIntegerExpr(OpCode operator, Number[] args) {
		int res = 0;
//		System.out.println(args[0]+ " " + args[1]);
		switch (operator) {
		case ADD:
			res= ((Integer)args[0] + (Integer)args[1]);
			break;
		case SUB:
			res= ((Integer)args[0] - (Integer)args[1]);
			break;
		case MUL:
			res= ((Integer)args[0] * (Integer)args[1]);
			break;
		case DIV:
			res= ((Integer)args[0] / (Integer)args[1]);
			break;
		default:
			System.err.println("Error (invokeJavaIntegerExpr): unknown operator (" + operator + ")!");
			break;
		}

		return new Integer(res);
	}
	
	/**
	 * Invokes method <code>name</code> on the concrete solver Java
	 * 
	 * @param operator The operator's code.
	 * @param args The arguments of the method to invoke. 
	 * @return The result of the invocation of method <code>name</code> with 
	 *         arguments <code>args</code> on the concrete solver, which will be
	 *         an <code>IloIntExpr</code> expression or <code>null</code> if something 
	 *         went wrong.
	 */
	private Object invokeJavaDoubleExpr(OpCode operator, Number[] args) {
		double res = 0;
		
		switch (operator) {
		case ADD:
			res= ((Double)args[0] + (Double)args[1]);
			break;
		case SUB:
			res= ((Double)args[0] - (Double)args[1]);
			break;
		case MUL:
			res= ((Double)args[0] * (Double)args[1]);
			break;
		case DIV:
			res= ((Double)args[0] / (Double)args[1]);
			break;
		default:
			System.err.println("Error (invokeJavaDoubleExpr): unknown operator (" + operator + ")!");
			break;
		}
		
		return new Double(res);
	}
	
	
	/**
	 * Invokes method <code>name</code> on the concrete solver Java
	 * 
	 * @param operator The operator's code.
	 * @param args The arguments of the method to invoke. 
	 * @return The result of the invocation of method <code>name</code> with 
	 *         arguments <code>args</code> on the concrete solver, which will be
	 *         an <code>IloIntExpr</code> expression or <code>null</code> if something 
	 *         went wrong.
	 */
	private Object invokeJavaFloatExpr(OpCode operator, Number[] args) {
		double res = 0;
		
		switch (operator) {
		case ADD:
			res= ((Float)args[0] + (Float)args[1]);
			break;
		case SUB:
			res= ((Float)args[0] - (Float)args[1]);
			break;
		case MUL:
			res= ((Float)args[0] * (Float)args[1]);
			break;
		case DIV:
			res= ((Float)args[0] / (Float)args[1]);
			break;
		default:
			System.err.println("Error (invokeJavaFloatExpr): unknown operator (" + operator + ")!");
			break;
		}
		
		return new Float(res);
	}
	
	/**
	 * This method visits an arithmetic binary expression that will result 
	 * in a concrete expression.
	 * 
	 * @param expression The arithmetic expression to visit.
	 * @return A concrete expression of type <code>IloIntExpr</code> modeling the
	 *         same operation as <code>expression</code>, or <code>null</code> if 
	 *         something went wrong building the constraint.
	 */
	public Object visit(BinaryExpression expression) {
//		System.out.println("JavaExpressionVisitor Binary " + expression);
		// TODO : les parenthèses
		Number[] args = new Number[2];
		args[0] = (Number)expression.arg1().structAccept(this);
		args[1] = (Number)expression.arg2().structAccept(this);
//		System.out.println("args dans binary " + args[0] + " " + args[1]);
		if (args[0] instanceof Integer)
			return invokeJavaIntegerExpr(expression.opCode(), args);
		else if (args[0] instanceof Double)
			return invokeJavaDoubleExpr(expression.opCode(), args);
		System.out.println(expression.opCode() + " " +  args[0] + " " + args[1]);
		return invokeJavaFloatExpr(expression.opCode(), args);
	}

	/**
	 * Visits a method call.
	 * 
	 * Only calls to static methods are allowed.
	 *
	 */
	public Object visit(MethodCall mc) {
		try {
			Class<?> methodClass = Class.forName(mc.qualification());
			Class<?>[] paramClasses = new Class<?>[mc.parameters().size()];
			Object[] paramValues = new Number[mc.parameters().size()];
			int i=0;
			for (Expression param: mc.parameters()) {
				paramClasses[i] = Type.getPrimitiveClass(param.type()); 
				paramValues[i] = (Number)param.structAccept(this);
				i++;	
			}
			Method m = methodClass.getMethod(mc.unqualifiedName(), paramClasses);
			//invoking a static method
			return m.invoke(null, paramValues);
		} catch (ClassNotFoundException e) {
			System.err.println("Error (JavaExpressionVisitor): the class (" + mc.qualification() + ") does not exist!");
			e.printStackTrace();
			System.exit(-1);
		}catch (SecurityException e) {
			System.err.println("Error (JavaExpressionVisitor): A security manager refused access to method: " + mc.methodName() + "!");
			e.printStackTrace();
			System.exit(-1);
		} catch (NoSuchMethodException e) {
			System.err.println("Error (JavaExpressionVisitor): the method (" + mc.methodName() + ") does not exist!");
			e.printStackTrace();
			System.exit(-1);
		} catch (IllegalArgumentException e) {
			System.err.println("Error (JavaExpressionVisitor): arguments in method call (" + mc + ") does not match method definition!");
			e.printStackTrace();
			System.exit(-1);
		} catch (IllegalAccessException e) {
			System.err.println("Error (JavaExpressionVisitor): access to method (" + mc + ") is illegal!");
			e.printStackTrace();
			System.exit(-1);
		} catch (InvocationTargetException e) {
			System.err.println("Error (JavaExpressionVisitor): the called method (" + mc + ") threw an exception!");
			e.printStackTrace();
			System.exit(-1);
		}
		//Should never be reached...
		return null;
	}
	
	/**
	 * This method visits a logical AND expression that will result 
	 * in a concrete constraint.
	 * 
	 * @param expression The logical expression to visit.
	 * @return A concrete constraint of type <code>IloConstraint</code> modeling the
	 *         logical AND, or <code>null</code> if 
	 *         something went wrong building the constraint.
	 */
	public Object visit(AndExpression expression) {
		Integer[] args = new Integer[2];
		args[0] = (Integer)expression.arg1().structAccept(this);
		args[1] = (Integer)expression.arg2().structAccept(this);
		if (((Integer)args[0]).intValue()==1 && ((Integer)args[1]).intValue()==1)
			return new Integer(1);
		else
			return new Integer(0);
	}

	/**
	 * This method visits a logical OR expression that will result 
	 * in a concrete constraint.
	 * 
	 * @param expression The logical expression to visit.
	 * @return A concrete constraint of type <code>IloConstraint</code> modeling the
	 *         logical OR, or <code>null</code> if 
	 *         something went wrong building the constraint.
	 */
	public Object visit(OrExpression expression) {
		Integer[] args = new Integer[2];
		args[0] = (Integer)expression.arg1().structAccept(this);
		args[1] = (Integer)expression.arg2().structAccept(this);
		if (((Integer)args[0]).intValue()==0 && ((Integer)args[1]).intValue()==0)
			return new Integer(0);
		else
			return new Integer(1);
	}
		
	/**
	 * This method visits a logical unary not expression that will result 
	 * in a concrete constraint.
	 * 
	 * @param expression The logical not expression to visit.
	 * @return A concrete constraint of type <code>IloConstraint</code> modeling the
	 *         same operation as <code>expression</code>, or <code>null</code> if the 
	 *         solver could not build the constraint.
	 */
	public Object visit(NotExpression expression) {
		Integer val = (Integer)expression.arg1().structAccept(this);
		if (val.intValue()==1)
			return new Integer(0);
		return new Integer(1);
	}

	private Object invokeJavaComparator(OpCode op, Number[] args) {
		int compare = 0;
		if (args[0] instanceof Integer)
			compare = ((Integer)args[0]).compareTo((Integer)args[1]);
		else if (args[0] instanceof Double)
			compare = ((Double)args[0]).compareTo((Double)args[1]);
		else if (args[0] instanceof Float)
			compare = ((Float)args[0]).compareTo((Float)args[1]);

		boolean res=false;
		
		switch (op) {
		case GT:
			res= compare>0;
			break;
		case GEQ:
			 res= compare>=0; 
			 break;
		case LT:
			res= compare<0;
			break;
		case LEQ:
			res= compare<=0;
			break;
		case EQU:
			res=compare==0;
			break;
		default:
			System.err.println("Error (invokeJavaComparator): unsupported operator (" + op + ")!");
		}
		
		if (res)
			return new Integer(1);
		return new Integer(0);
	}
	
	/**
	 * This method visits an arithmetic comparison expression that will result 
	 * in a concrete constraint.
	 * 
	 * @param expression The arithmetic comparison expression to visit.
	 * @return A concrete constraint of type <code>IloConstraint</code> modeling the
	 *         same comparison as <code>expression</code>.
	 */
	public Object visit(Comparator expression) {
		Number[] args = new Number[2];
		args[0] = (Number)expression.arg1().structAccept(this);
		args[1] = (Number)expression.arg2().structAccept(this);
		return invokeJavaComparator(expression.opCode(), args);
	}
	
	/**
	 * This method visits a JML imply expression that will result 
	 * in a concrete constraint.
	 * @param expr The JML imply expression to visit.
	 * 
	 * @return A concrete constraint of type <code>IloConstraint</code> modeling the
	 *         imply <code>expression</code>, or <code>null</code> if the 
	 *         solver could not build the constraint.
	 */	
	public Object visit(JMLImpliesExpression expression) {

		Integer c1 = (Integer)expression.arg1().structAccept(this);
		Integer c2 = (Integer)expression.arg2().structAccept(this);
		if (c1.intValue()==1 && c2.intValue()==0)
			return new Integer(0);
		return new Integer(1);
	}

	/**
	 * This method visits a JML <pre>ForAll</pre> expression that will result 
	 * in a conjunction of concrete constraints.
	 * @param jmlForAll The JML <pre>ForAll</pre> expression to visit. It includes 
	 *                   an index variable, statically known min and max bounds for 
	 *                   the index and an expression parameterised by the index.
	 * 
	 * @return A concrete constraint of type <code>IloConstraint</code> modeling the
	 *         conjunction of the substitutions in the <pre>ForAll</pre> expression of
	 *         the index values to the index; or <code>null</code> if the 
	 *         solver could not build the constraint.
	 */	
	public Object visit(JMLForAllExpression jmlForAll) {
		System.err.println("JML quanrtifiers not yet implemented");
//		IloConstraint result = null;
//		
//		LogicalExpression expr = jmlForAll.condition();
//		Variable varIndex = jmlForAll.index();
//		List<IntegerLiteral> varIndexValues = jmlForAll.enumeration();
//		
//		if(varIndexValues != null) {
//			result = (IloConstraint)(expr.substitute(varIndex, varIndexValues.get(0))).structAccept(this, data);	
//			for(int i = 1; i < varIndexValues.size(); i++) {
//				try {
//					result = concreteSolver.and(
//							(IloConstraint)expr.substitute(varIndex, varIndexValues.get(i)).structAccept(this, data), 
//							result);
//				} catch(IloException e) {
//					System.err.println("Error while creating the 'and' constraint "
//							+ "in Ilog CP Optimizer for JML ForAll expression " 
//							+ jmlForAll + "!");			
//					e.printStackTrace();
//					System.exit(13);
//				}
//			}
//		}
//		else {  //This ForAll is not enumerable
//			System.err.println("CP Error : Not Enumerable ForALL !");
//			System.err.println(jmlForAll);
//			System.exit(-1);
//		}
//		return result;
		return null;
	}

//	public Object visit(JMLForAllExpression expression, Object data) {
//		LogicalExpression expr = expression.condition();
//		IntegerVariable varIndex = expression.index();
//		LogicalExpression bound = expression.boundExpression();
//		
//		//Ajout de la variable d'itération dans le CSP
//		this.intVar.addVar(varIndex);
//		//Contrainte sur les bornes de la variable d'itération
//		IloConstraint cBound = (IloConstraint)bound.structAccept(this, data);
//		//Contrainte de la condition du ForAll
//		IloConstraint cCond = (IloConstraint)expr.structAccept(this, data);
//		try {
//			return concreteSolver.not(concreteSolver.and(cBound, concreteSolver.not(cCond)));
//		} catch(IloException e) {
//			System.err.println("Error while creating the constraint "
//					+ "in Ilog CP Optimizer for JML ForAll expression " 
//					+ expression + "!");			
//			e.printStackTrace();
//			System.exit(13);
//			return null;
//		}
//	}
	
	/**
	 * This method visits a JML <pre>Exist</pre> expression that will result 
	 * in a disjunction of concrete constraints.
	 * @param jmlExist The JML <pre>Exist</pre> expression to visit. It includes an 
	 *                   index variable, statically known min and max bounds for the index 
	 *                   and an expression parameterised by the index.
	 * 
	 * @return A concrete constraint of type <code>IloConstraint</code> modeling the
	 *         disjunction of the substitutions in the <pre>Exist</pre> expression of
	 *         the index values to the index; or <code>null</code> if the 
	 *         solver could not build the constraint.
	 */	
	public Object visit(JMLExistExpression jmlExist) {
		System.err.println("JML quanrtifiers not yet implemented");
//		IloConstraint result = null;
//		
//		LogicalExpression expr = jmlExist.condition();
//		Variable varIndex = jmlExist.index();
//		List<IntegerLiteral> varIndexValues = jmlExist.enumeration();
//		
//		if(varIndexValues != null) {
//			result = (IloConstraint)expr.substitute(varIndex, varIndexValues.get(0)).structAccept(this, data);			
//			for(int i = 1; i < varIndexValues.size(); i++) {
//				try {
//					result = concreteSolver.or(
//							(IloConstraint)expr.substitute(varIndex, varIndexValues.get(i)).structAccept(this, data), 
//							result);
//				} catch(IloException e) {
//					System.err.println("Error while creating the 'or' constraint "
//							+ "in Ilog CP Optimizer for JML Exist expression " 
//							+ jmlExist + "!");			
//					e.printStackTrace();
//					System.exit(14);
//				}
//			}
//		}
//		else {
//			System.err.println("CP Error : Not Enumerable Exist !");
//			System.err.println(jmlExist);
//			System.exit(-1);			
//		}
//		return result;
		return null;
	}

	/**
	 * This method visits a JML <pre>AllDiff</pre> expression that will result 
	 * in a concrete <pre>allDiff</pre> constraint.
	 * 
	 * @param expression The JML <pre>AllDiff</pre> expression to visit. It includes 
	 *                   an array of integer variables.
	 * @return A concrete constraint of type <code>IloConstraint</code> modeling the
	 *         JML <pre>AllDiff</pre> expression; or <code>null</code> if the 
	 *         solver could not build the constraint.
	 */	
	public Object visit(JMLAllDiff expression) {
		System.err.println("JML quanrtifiers not yet implemented");
//		try {
//			return concreteSolver.allDiff((IloIntVar[])expression.array().structAccept(this, data));
//		} catch(IloException e) {
//			System.err.println("Error while creating the 'allDiff' constraint "
//			           + "in Ilog CP Optimizer for expression " + expression + "!");			
//			e.printStackTrace();
//			System.exit(15);
//		}
		return null;
	}

	@Override
	public Object visit(JMLOld expression) {
		return this.visit(expression.oldValue());
	}
	
}
