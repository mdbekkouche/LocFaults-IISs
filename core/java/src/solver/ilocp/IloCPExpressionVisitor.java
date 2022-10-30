package solver.ilocp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.ClassCastException;
import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.cp.IloCP;

import expression.ExpressionVisitor;
import expression.ParenExpression;
import expression.MethodCall;
import expression.logical.AndExpression;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.Comparator;
import expression.logical.JMLAllDiff;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NondetAssignment;
import expression.logical.NotExpression;
import expression.logical.OrExpression;
import expression.numeric.BinaryExpression;
import expression.numeric.NumericExpression;
import expression.numeric.IntegerLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.DoubleLiteral;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;

/**
 * This class implements a visitor, as in the visitor design pattern,
 * for {@link constraint.Constraint abstract constraints}.
 * This visitor builds the set of concrete constraints, i.e. as handled by 
 * the concrete solver (Ilog CP Optimizer), from the abstract constraint 
 * extracted from the Java program.
 * More precisely, this visitor goes through the expression tree contained in the 
 * abstract constraint and builds the corresponding set of constraints in the 
 * concrete syntax of the Ilog CP Optimizer syntax (using Ilog Concert API).
 * 
 * The <code>visit</code> methods can return either:
 * <ul>
 * <li><code>null</code>;</li>
 * <li>a single concrete constraint of type {@link ilog.concert.IloConstraint};</li>
 * <li>a set of concrete constraints of type <code>ArrayList&lt;IloConstraint&gt;</code>;</li>
 * <li>a concrete integer variable of type {@link ilog.concert.IloIntVar};</li>
 * <li>a concrete integer expression of type {@link ilog.concert.IloIntExpr}.</li>
 * </ul> 
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 * @author Lydie
 *
 */
public class IloCPExpressionVisitor implements ExpressionVisitor {
	/**
	 * A reference to the Ilog CP optimizer solver.
	 */
	private IloCP concreteSolver;
	/**
	 * The store of integer variables handled by the solver.
	 */
	private IloCPIntVarBlock intVar;
	/**
	 * The store of integer array variables handled by the solver.
	 */
	private IloCPIntArrayVarBlock intArrayVar;
	/**
	 * When set to true, perform the substitution of the JML 
	 * <code>\result</code> variable. 
	 */
	private boolean substitute;
	/**
	 * The name of the variable to substitute.
	 */
	private String toSubstitute;
	/**
	 * The value to be substituted.
	 */
	private NumericExpression substitutedValue;
	
	
	//------------
	//Constructors
	//------------
	
	/**
	 * Constructs a visitor for constraints with the given store of integer 
	 * variables already handled by the solver.
	 * 
	 * @param solver A reference to the Ilog CP Optimizer solver.
	 * @param intVar A store of integer variables already handled by the solver.
	 */
	public IloCPExpressionVisitor(IloCP solver, IloCPIntVarBlock intVar) {
		concreteSolver = solver;
		this.intVar = intVar;
		substitute = false;
	}
	
	/**
	 * Constructs a visitor for constraints with the given stores of integer 
	 * variables and integer array variables already handled by the solver.
     *
	 * @param solver A reference to the Ilog CP Optimizer solver.
	 * @param intVar A store of integer variables already handled by the 
	 *               solver.
	 * @param intArrayVar A store of integer array variables already 
	 *                    handled by the solver.
	 */
	public IloCPExpressionVisitor(IloCP solver, 
			                      IloCPIntVarBlock intVar,
			                      IloCPIntArrayVarBlock intArrayVar) {
		this(solver, intVar);
		this.intArrayVar = intArrayVar;
	}
	
	/**
	 * Constructor to be used when adding the post-condition.
	 * Makes the link between the <pre>JMLResult</pre> variable and 
	 * the latest renaming of the variable associated with the Java 
	 * return value. 
	 * 
	 * @param solver A reference to the Ilog CP Optimizer solver.
	 * @param intVar A store of integer variables already handled by the 
	 *               solver.
	 * @param intArrayVar A store of integer array variables already 
	 *                    handled by the solver.
	 * @param name Name of the variable to be substituted.
	 * @param returnValue The return value of the current branch of the 
	 *                    Java program verified.
	 */
	public IloCPExpressionVisitor(IloCP solver,
			                      IloCPIntVarBlock intVar,
			                      IloCPIntArrayVarBlock intArrayVar,
			                      String name, 
			                      NumericExpression returnValue) {
		this(solver, intVar, intArrayVar);
		substitute = true;
		toSubstitute = name;
		substitutedValue = returnValue;
	}
	
	
	//-----------------------------------
	//ExpressionVisitor interface methods
	//-----------------------------------
	
	/**
	 * This method visits an assignement expression that will result 
	 * in an equality constraint.
	 * @param assignement The assignement expression to visit.
	 * 
	 * @return A concrete equality constraint of type <code>IloConstraint</code>.
	 */
	public IloConstraint visit(Assignment assign) {
		//TODO handle cast exception
		IloIntExpr lhs = (IloIntExpr)assign.lhs().structAccept(this);
		IloIntExpr rhs = (IloIntExpr)assign.rhs().structAccept(this);
		//This never raises an exception, how do we know that all went well and that
		//we don't need to call error(1). Is it if null is returned ? This is not 
		//documented by Ilog as far as I could see.
		return concreteSolver.eq(lhs,rhs);
	}

	@Override
	public Object visit(NondetAssignment assignment) {
		// Nothing to be done except maybe create the assigned variable if it has not
		// already been created
		// Uncomment next statement to enforce a runtime check of the variable existence
		//intVar.get(assignment.lhs().name());
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
	public IloIntVar visit(Variable variable) {
		//If substitution must occur and this is the variable to be substituted,
		//then we substitute 'value' to this variable.
		if (substitute && variable.root().equals(toSubstitute))
			return (IloIntVar)substitutedValue.structAccept(this);
		else
			return intVar.get(variable.name()).concreteVar();
	}

	/**
	 * This method visits an integer literal expression that will result 
	 * in a concrete integer constant expression.
	 * 
	 * @param literal The integer literal expression to visit.
	 * @return A concrete integer constraint of type <code>IloIntExpr</code>.
	 */
	public IloNumExpr visit(IntegerLiteral literal) {
		try {
			return concreteSolver.constant((int)literal.constantNumber().intValue());
		} catch (IloException e) {
			System.err.println("Error when creating Ilog CP Optimizer constant!");
			e.printStackTrace();
			System.exit(7);
			return null;
		}
	}

	/**
	 * This method visits a float literal expression that will result 
	 * in a concrete float constant expression.
	 * 
	 * @param literal The float literal expression to visit.
	 * @return A concrete float constraint of type <code>IloNumExpr</code>.
	 */
	public IloNumExpr visit(FloatLiteral literal) {
		try {
			return concreteSolver.constant(literal.constantNumber().floatValue());
		} catch (IloException e) {
			System.err.println("Error when creating Ilog CP Optimizer constant!");
			e.printStackTrace();
			System.exit(7);
			return null;
		}
	}
	/**
	 * This method visits a double literal expression that will result 
	 * in a concrete double constant expression.
	 * 
	 * @param literal The double literal expression to visit.
	 * @return A concrete double constraint of type <code>IloNumExpr</code>.
	 */
	public IloNumExpr visit(DoubleLiteral literal) {
		try {
			return concreteSolver.constant(literal.constantNumber().doubleValue());
		} catch (IloException e) {
			System.err.println("Error when creating Ilog CP Optimizer constant!");
			e.printStackTrace();
			System.exit(7);
			return null;
		}
	}

	/**
	 * This method visits a logical literal expression that will result 
	 * in a concrete logical expression.
	 * 
	 * @param literal The logical literal expression to visit.
	 * @return A concrete constraint of type <code>IloConstraint</code>.
	 */
	public IloConstraint visit(LogicalLiteral literal) {
		IloConstraint constraint = null;
		try {
			if(literal.constantBoolean())
				constraint = concreteSolver.trueConstraint();
			else
				constraint = concreteSolver.falseConstraint();
		} catch(IloException e) {
			System.err.println("Error while creating constraint in "
					+ "Ilog CP Optimizer for boolean value " + literal.constantBoolean()
					+ "!");
			e.printStackTrace();
			System.exit(8);			
		}
		return constraint;
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
	public ArrayList<IloConstraint> visit(ArrayAssignment eltArrayAssign) {
		ArrayList<IloConstraint> constraints = new ArrayList<IloConstraint>();
		//eltArrayAssign contains both A_k and A_(k+1) arrays of concrete variables.
		IloIntVar[] newArray = 
			intArrayVar.get(eltArrayAssign.array().name()).concreteArray();
		IloIntVar[] oldArray = 
			intArrayVar.get(eltArrayAssign.previousArray().name()).concreteArray();
		NumericExpression idx = eltArrayAssign.index();
		IloIntExpr concreteVal = (IloIntExpr)eltArrayAssign.rhs().structAccept(this);

		//Partial evaluation optimization: constraints are simpler if we know the 
		//index value. Shouldn't we report an array out of bounds if case be ?
		if (idx.isConstant()) {
			for(int i = 0; i < newArray.length; i++) {
				//We assume idx holds an integer value: we rely on the Java compiler for that
				if (i == idx.constantNumber().intValue())
					constraints.add(concreteSolver.eq(newArray[i], concreteVal));
				else 
					constraints.add(concreteSolver.eq(newArray[i], oldArray[i]));
			}
		}
		else {
			IloIntExpr concreteIdx = (IloIntExpr)idx.structAccept(this);
			for(int i = 0; i < newArray.length; i++) {
				try {
					constraints.add(
							concreteSolver.ifThenElse(
									concreteSolver.eq(concreteIdx, i), 
									concreteSolver.eq(newArray[i], concreteVal),
									concreteSolver.eq(newArray[i], oldArray[i])));
				} catch(IloException e) {
					System.err.println("Error while adding ifThenElse constraint in "
							+ "Ilog CP Optimizer for array " + eltArrayAssign.array().name()
							+ " at index " + i + "!");
					e.printStackTrace();
					System.exit(9);
				}
			}
		}
		return constraints;
	}

	/**
	 * This method visits an integer array variable, this will result 
	 * in an array of concrete integer variables.
	 * @param array The array variable to visit.
	 * 
	 * @return The array of concrete integer variables (of Type <code>IloIntVar[]</code>)
	 *         corresponding to the array variable <code>array</code>.
	 */
	public IloIntVar[] visit(ArrayVariable array) {
		return intArrayVar.get(array.name()).concreteArray();
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
	public IloIntExpr visit(ArrayElement elt) {
		IloIntVar[] array = intArrayVar.get(elt.array().name()).concreteArray();
		NumericExpression idx = elt.index();
		//Partial evaluation if the index value is known. 
		//Shouldn't we report an array out of bounds if case be ?
		if(idx.isConstant()) {
			//We assume idx holds an integer value: we rely on the Java compiler for that
			int idxValue = idx.constantNumber().intValue();
			if((0 <= idxValue) && (idxValue < array.length))
				return array[idxValue];
		}
		//idx.constant() is false, or idxValue is out of array bounds
		try {
			return concreteSolver.element(array, (IloIntExpr)idx.structAccept(this));
		} catch(IloException e) {
			System.err.println("Error while creating element expression in "
					+ "Ilog CP Optimizer for elt " + elt + "!");
			e.printStackTrace();
			System.exit(10);
		}
		return null;
	}

	/**
	 * Returns the Ilog CP Optimizer method names corresponding to the four 
	 * common arithmetic binary operators on integers (<pre>+, -, *, /</pre>).
	 * 
	 * @param expression An expression denoting one of the four arithmetic binary operators.
	 * @return The name of the concrete solver method corresponding to the operator 
	 *         given as parameter.
	 */
	private String IloCPIntSyntax(BinaryExpression expression) {
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
			System.err.println("Error (IloCPIntSyntax): unknown operator (" + expression.opCode() + ")!");
			return "";
		}
	}  	
	
	/**
	 * Returns the Ilog CP Optimizer method names corresponding to the five 
	 * common arithmetic comparison operators on integers (<pre>=, <, >, <=, >=</pre>).
	 * 
	 * @param expression An expression denoting one of the five arithmetic comparison
	 *                   operators (see above).
	 * @return The name of the concrete solver method corresponding to the operator 
	 *         given as parameter.
	 */
	protected String IloCPCompareSyntax(Comparator expression) {
		switch (expression.opCode()) {
		case GT:
			return "gt";
		case GEQ:
			return "ge";
		case LT:
			return "lt";
		case LEQ:
			return "le";
		case EQU:
			return "eq";
		default:
			System.err.println("Error (IloCPCompareSyntax): unknown operator (" + expression.opCode() + ")!");
			return "";
		}
	}

	/**
	 * Invokes method <code>name</code> on the concrete solver (Ilog CP Optimizer) 
	 * using java reflection and returns the concrete expression resulting from 
	 * the invocation.
	 * 
	 * This method should only be called to invoke methods implemented by 
	 * the concrete solver that return expressions of type 
	 * <code>IloIntExpression</code>.
	 * 
	 * More precisely, this method will call 
	 * <code>concreteSolver.name(IloIntExpr, IloIntExpr)</code>.
	 * 
	 * @param name The name of the method to invoke on the concrete solver.
	 * @param args The arguments of the method to invoke. 
	 * @return The result of the invocation of method <code>name</code> with 
	 *         arguments <code>args</code> on the concrete solver, which will be
	 *         an <code>IloIntExpr</code> expression or <code>null</code> if something 
	 *         went wrong.
	 */
	private IloIntExpr invokeIloIntExpr(String name, Object[] args) {
		IloIntExpr exp = null;
		try {
			Class<?> solverClass = Class.forName("ilog.cp.IloCP");
			Class argClass = Class.forName("ilog.concert.IloIntExpr");
			Class[] param = {argClass, argClass};
			Method m = solverClass.getMethod(name, param);
			exp = (IloIntExpr)m.invoke(concreteSolver, args);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return exp;
	}
	
	/**
	 * Invokes method <code>name</code> on the concrete solver (Ilog CP Optimizer) 
	 * using java reflection and returns the concrete constraint resulting from 
	 * the invocation.
	 * 
	 * This method should only be called to invoke methods implemented by 
	 * the concrete solver that return expressions of type 
	 * <code>IloConstraint</code>.
	 * 
	 * More precisely, this method will call 
	 * <code>concreteSolver.name(IloIntExpr, IloIntExpr)</code> if 
	 * <code>ExprNotConstr</code> is <code>true</code>; otherwise, it will call
	 * <code>concreteSolver.name(IloConstraint, IloConstraint)</code>.
	 * 
	 * @param name The name of the method to invoke on the concrete solver.
	 * @param args The arguments of the method to invoke. 
	 * @return The result of the invocation of method <code>name</code> with 
	 *         arguments <code>args</code> on the concrete solver, which will be
	 *         an <code>IloConstraint</code> constraint or <code>null</code> if 
	 *         something went wrong.
	 */
	protected IloConstraint invokeIloConstraint(String name, 
												Object[] args, 
												boolean ExprNotConstr) {
		String className = (ExprNotConstr) ? "ilog.concert.IloIntExpr"
										   : "ilog.concert.IloConstraint";
		IloConstraint constr = null;
		try {
			Class<?> solverClass = Class.forName("ilog.cp.IloCP");
			Class argClass = Class.forName(className);
			Class[] param = {argClass, argClass};
			Method m = solverClass.getMethod(name, param);
			constr = (IloConstraint)m.invoke(concreteSolver, args);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return constr;
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
	public IloIntExpr visit(BinaryExpression expression) {
		// TODO : les parenthèses
		IloIntExpr[] args = new IloIntExpr[2];
		args[0] = (IloIntExpr)expression.arg1().structAccept(this);
		args[1] = (IloIntExpr)expression.arg2().structAccept(this);
		return invokeIloIntExpr(IloCPIntSyntax(expression), args);
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
	public IloConstraint visit(AndExpression expression) {
		IloConstraint[] args = new IloConstraint[2];
		args[0] = (IloConstraint)expression.arg1().structAccept(this);
		args[1] = (IloConstraint)expression.arg2().structAccept(this);
		if ((args[0] == null) || (args[1] == null))
			return null;
		else {
			try {
				return concreteSolver.and(args[0], args[1]);
			} catch(IloException e) {
				System.err.println("Error while creating an 'and' constraint in "
						+ "Ilog CP Optimizer for expression " + expression + "!");			
				e.printStackTrace();
				System.exit(12);
				return null;
			}
		}
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
	public IloConstraint visit(OrExpression expression) {
		IloConstraint[] args = new IloConstraint[2];
		args[0] = (IloConstraint)expression.arg1().structAccept(this);
		args[1] = (IloConstraint)expression.arg2().structAccept(this);
		if (args[0] == null) 
			return args[1];
		else if (args[1] == null) 
			return args[0];
		else {
			try {
				return concreteSolver.or(args[0], args[1]);
			} catch(IloException e) {
				System.err.println("Error while creating an 'or' constraint in "
						+ "Ilog CP Optimizer for expression " + expression + "!");			
				e.printStackTrace();
				System.exit(12);
				return null;
			}
		}
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
	public IloConstraint visit(JMLImpliesExpression expression) {
		try {
			IloConstraint c1 = (IloConstraint)expression.arg1().structAccept(this);
			IloConstraint c2 = (IloConstraint)expression.arg2().structAccept(this);
			return concreteSolver.imply(c1,c2);
		} catch(IloException e) {
			System.err.println("Error while creating an 'imply' constraint in "
					+ "Ilog CP Optimizer for expression " + expression + "!");			
			e.printStackTrace();
			System.exit(12);
		} catch(ClassCastException e) {
			System.err.println("Error while creating the 'imply' constraint arguments in "
					+ "Ilog CP Optimizer for expression " + expression + "!");			
			e.printStackTrace();
			System.exit(12);
		}
		return null;
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
	public IloConstraint visit(NotExpression expression) {
		try {
			return concreteSolver.not(
						(IloConstraint)(expression.arg1()).structAccept(this));
		} catch(IloException e) {
			System.err.println("Error while creating a 'not' constraint in "
					+ "Ilog CP Optimizer for expression " + expression + "!");			
			e.printStackTrace();
			System.exit(11);
		}
		return null;
	}

	/**
	 * This method visits an arithmetic comparison expression that will result 
	 * in a concrete constraint.
	 * 
	 * @param expression The arithmetic comparison expression to visit.
	 * @return A concrete constraint of type <code>IloConstraint</code> modeling the
	 *         same comparison as <code>expression</code>.
	 */
	public IloIntExpr visit(Comparator expression) {
		IloIntExpr[] args = new IloIntExpr[2];
		args[0] = (IloIntExpr)expression.arg1().structAccept(this);
		args[1] = (IloIntExpr)expression.arg2().structAccept(this);
		return invokeIloConstraint(IloCPCompareSyntax(expression), args, true);
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
	public IloConstraint visit(JMLForAllExpression jmlForAll) {
		IloConstraint result = null;
		
		LogicalExpression expr = jmlForAll.condition();
		Variable varIndex = jmlForAll.index();
		List<IntegerLiteral> varIndexValues = jmlForAll.enumeration();
		
		if(varIndexValues != null) {
			result = (IloConstraint)(expr.substitute(varIndex, varIndexValues.get(0))).structAccept(this);	
			for(int i = 1; i < varIndexValues.size(); i++) {
				try {
					result = concreteSolver.and(
							(IloConstraint)expr.substitute(varIndex, varIndexValues.get(i)).structAccept(this), 
							result);
				} catch(IloException e) {
					System.err.println("Error while creating the 'and' constraint "
							+ "in Ilog CP Optimizer for JML ForAll expression " 
							+ jmlForAll + "!");			
					e.printStackTrace();
					System.exit(13);
				}
			}
		}
		else {  //This ForAll is not enumerable
			System.err.println("CP Error : Not Enumerable ForALL !");
			System.err.println(jmlForAll);
			System.exit(-1);
		}
		return result;
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
	public IloConstraint visit(JMLExistExpression jmlExist) {
		IloConstraint result = null;
		
		LogicalExpression expr = jmlExist.condition();
		Variable varIndex = jmlExist.index();
		List<IntegerLiteral> varIndexValues = jmlExist.enumeration();
		
		if(varIndexValues != null) {
			result = (IloConstraint)expr.substitute(varIndex, varIndexValues.get(0)).structAccept(this);			
			for(int i = 1; i < varIndexValues.size(); i++) {
				try {
					result = concreteSolver.or(
							(IloConstraint)expr.substitute(varIndex, varIndexValues.get(i)).structAccept(this), 
							result);
				} catch(IloException e) {
					System.err.println("Error while creating the 'or' constraint "
							+ "in Ilog CP Optimizer for JML Exist expression " 
							+ jmlExist + "!");			
					e.printStackTrace();
					System.exit(14);
				}
			}
		}
		else {
			System.err.println("CP Error : Not Enumerable Exist !");
			System.err.println(jmlExist);
			System.exit(-1);			
		}
		return result;
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
	public IloConstraint visit(JMLAllDiff expression) {
		try {
			return concreteSolver.allDiff((IloIntVar[])expression.array().structAccept(this));
		} catch(IloException e) {
			System.err.println("Error while creating the 'allDiff' constraint "
			           + "in Ilog CP Optimizer for expression " + expression + "!");			
			e.printStackTrace();
			System.exit(15);
		}
		return null;
	}

	public Object visit(MethodCall mc) {
		System.err.println("Error : Method calls not allowed with CP !");
		System.err.println(mc);
		System.exit(16);
		return null;
	}

	@Override
	public Object visit(JMLOld expression) {
		return this.visit(expression.oldValue());
	}
}
