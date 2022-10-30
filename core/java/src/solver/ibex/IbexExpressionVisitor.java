package solver.ibex;

import expression.Expression;
import expression.ExpressionVisitor;
import expression.ParenExpression;
import expression.MethodCall;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.Comparator;
import expression.logical.JMLAllDiff;
import expression.logical.InfEqualExpression;
import expression.logical.NondetAssignment;
import expression.logical.SupEqualExpression;
import expression.logical.SupExpression;
import expression.logical.InfExpression;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NotExpression;
import expression.logical.AndExpression;
import expression.logical.OrExpression;
import expression.numeric.BinaryExpression;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;
import exception.AnalyzeException;

import validation.util.OpCode;

/**
 * Creates IBEX constraints from CPBPV ones.
 * This class relies on native methods to interface with the IBEX C++ paver. 
 * These methods are part of the <pre>libjni_ibex.so</pre> library.
 * 
 * @author Olivier Ponsini
 *
 */
public class IbexExpressionVisitor implements ExpressionVisitor {

	static {
    	System.loadLibrary("jni_ibex");
    }
	
	/**
	 * Operators' code as defined in IbexToken.h.
	 * The order of the enumeration corresponds to the code value in IBEX.
	 */
	protected enum IbexOpCode {
		LT,  
		LEQ, 
		EQU, 
		GEQ, 
		GT,
		M_EQU,
		ADD, 
		SUB,
		MUL,
		DIV,
		MIN,
		MAX,
		ARCTAN2,
		M_ADD,
		M_SUB,
		M_MUL,
		M_SCAL,
		M_VEC, 
		V_DOT,
		SIGN,
		MINUS, 
		SQR, 
		SQRT, 
		LOG, 
		EXP, 
		COS, 
		SIN, 
		TAN, 
		ARCCOS, 
		ARCSIN, 
		ARCTAN, 
		COSH, 
		SINH, 
		TANH, 
		ARCCOSH, 
		ARCSINH, 
		ARCTANH, 
		ABS, 
		M_MINUS, 
		M_TRANS,
		INF,
		MID, 
		SUP, 
		M_INF, 
		M_MID, 
		M_SUP;
		
		/**
		 * Returns the IBEX operator code corresponding to the given CPBPV operator code.
		 * 
		 * @param op Operator's code
		 * @return The IBEX operator code corresponding to the given CPBPV operator code.
		 * @throws AnalyzeException
		 */
		public static int convertFromCPBPVOp(OpCode op) 
			throws AnalyzeException
		{
			switch (op) {
			case ADD:
				return IbexOpCode.ADD.ordinal();
			case SUB:
				return IbexOpCode.SUB.ordinal();
			case MUL:
				return IbexOpCode.MUL.ordinal();
			case DIV:
				return IbexOpCode.DIV.ordinal();
			case GT:
				return IbexOpCode.GT.ordinal();
			case GEQ:
				return IbexOpCode.GEQ.ordinal(); 
			case LT:
				return IbexOpCode.LT.ordinal();
			case LEQ:
				return IbexOpCode.LEQ.ordinal();
			case EQU:
				return IbexOpCode.EQU.ordinal();
			default:
				throw new AnalyzeException("Unknown operator (" + op + ")");
			}
		}
	}
	
	/**
     * The IBEX solver java interface.
     */
    private IbexSolver solver;
 
    public IbexExpressionVisitor(IbexSolver solver) {
    	this.solver = solver;
    }
    
	//--------------------------------------------------------------
	// Build new constraints.
	// The new constraints are added to the paver environment handled
	// in the C++ interface part.
	//--------------------------------------------------------------
	
	public Object visit(Assignment assignment) {
		long leftExpr = (Long)assignment.lhs().structAccept(this);
		long rightExpr = (Long)assignment.rhs().structAccept(this);
		solver.newCmpOp(IbexOpCode.EQU.ordinal(), leftExpr, rightExpr);
		return null;
	}

	public Object visit(AndExpression expression) {
		expression.arg1().structAccept(this);
		expression.arg2().structAccept(this);			
		return null;
	}
				
	public Object visit(Comparator expression) {
		long leftExpr = (Long)expression.arg1().structAccept(this);
		long rightExpr = (Long)expression.arg2().structAccept(this);
		try {
			solver.newCmpOp(
					IbexOpCode.convertFromCPBPVOp(expression.opCode()), 
					leftExpr, 
					rightExpr);
		} catch (AnalyzeException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void negateComparator(Comparator cmp) {
		Expression a1 = cmp.arg1();
		Expression a2 = cmp.arg2();
		switch (cmp.opCode()) {
		case EQU:
			new SupExpression(a1,a2).structAccept(this);
			new InfExpression(a1,a2).structAccept(this);
			//Creates an union of these last two constraints
			solver.newUnion();
			break;
		case LT:
			 new SupEqualExpression(a1,a2).structAccept(this);
			 break;
		case GT:
			new InfEqualExpression(a1,a2).structAccept(this);
			break;
		case LEQ:
			 new SupExpression(a1,a2).structAccept(this);
			 break;
		case GEQ: 
			 new InfExpression(a1,a2).structAccept(this);
			 break;
		default:
			System.err.println("Error (negateComparator): unknown operator (" + cmp.opCode() + ")!");
			break;
		}
	}

	public Object visit(NotExpression expression) {
		LogicalExpression a1 = expression.arg1();
		if (a1 instanceof NotExpression) {
			((NotExpression)a1).arg1().structAccept(this);
		}
		else if (a1.isComparison()) {
			negateComparator((Comparator)a1);
		}
		else if (a1 instanceof ParenExpression) {
			((ParenExpression)a1).arg1().structAccept(this);			
		}
		else if (a1 instanceof OrExpression) {
			OrExpression o = (OrExpression)a1;
			new NotExpression(o.arg1()).structAccept(this);
			new NotExpression(o.arg2()).structAccept(this);
		} 
		else if (a1 instanceof AndExpression) {
			System.err.println("Error: Negating a logical AND operator "
					+ "should create an OR operator which is not yet supported with IBEX!");
		} 
		else if (a1 instanceof JMLForAllExpression) {
			System.err.println("Error: Negating a ForAll quantifier "
					+ "should create an Exist quantifier which is not yet supported with IBEX!");
		}  
		else if (a1 instanceof JMLExistExpression) {
			System.err.println("Error: Negating an Exist quantifier "
					+ "should create a ForAll quantifier which is not yet supported with IBEX!");
		}
		else {
			System.err.println("Error: Negating this expression: " 
					+ a1.getClass()
					+ " is not yet supported with IBEX!");			
		}
		return null;
	}

	//--------------------------------------------------------------
	// Transform expressions.
	// Pointers to ibex::Expr are handled as long int's wrapped into 
	// Longs. 
	// We use long int's for compatibility with 64-bit platform. 
	//--------------------------------------------------------------

	public Object visit(ParenExpression expression) {
		return expression.arg1().structAccept(this);
	}

	public Object visit(IntegerLiteral literal) {
		return solver.newCst(literal.constantNumber().doubleValue());
	}

	public Object visit(FloatLiteral literal) {
		return solver.newCst(literal.constantNumber().doubleValue());
	}

	public Object visit(DoubleLiteral literal) {
		return solver.newCst(literal.constantNumber().doubleValue());
	}

	public Object visit(LogicalLiteral literal) {
		long leftExpr = solver.newCst(0);
		long rightExpr = solver.newCst(1);
		int opCode;
		if (literal.constantBoolean()) {
			opCode = IbexOpCode.GT.ordinal();
		}
		else {
			opCode = IbexOpCode.LT.ordinal();			
		}
		solver.newCmpOp(opCode, leftExpr, rightExpr);
		return null;
	}

	public Object visit(Variable variable) {
		return solver.getSymbol(variable.name());
	}

	public Object visit(BinaryExpression expression) {
		long leftExpr = (Long)expression.arg1().structAccept(this);
		long rightExpr = (Long)expression.arg2().structAccept(this);
		try {
			return IbexSolver.ibexNewOp(
						IbexOpCode.convertFromCPBPVOp(expression.opCode()), 
						leftExpr, 
						rightExpr);
		} catch (AnalyzeException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object visit(ArrayAssignment assignment) {
		System.err.println("Error: arrays are not yet implemented with IBEX paver!");
		return null;
	}

	public Object visit(ArrayVariable variable) {
		System.err.println("Error: arrays are not yet implemented with IBEX paver!");
		return null;
	}

	public Object visit(ArrayElement tab) {
		System.err.println("Error: arrays are not yet implemented with IBEX paver!");
		return null;
	}

	public Object visit(OrExpression expression) {
		System.err.println("Error: logical operator OR not yet implemented with IBEX paver!");			
		return null;
	}

	public Object visit(JMLImpliesExpression expression) {
		System.err.println("Error: the IMPLY logical operator is not yet implemented with IBEX paver!");
		return null;
	}

	public Object visit(JMLForAllExpression expression) {
		System.err.println("Error: the ForAll quantifier is not yet implemented with IBEX paver!");
		return null;
	}

	public Object visit(JMLExistExpression expression) {
		System.err.println("Error: the Exist quantifier is not yet implemented with IBEX paver!");
		return null;
	}

	public Object visit(JMLAllDiff expression) {
		System.err.println("Error: the AllDiff constraint is not yet implemented with IBEX paver!");
		return null;
	}

	public Object visit(MethodCall mc) {
		System.err.println("Error : Method calls not allowed with IBEX !");
		System.err.println(mc);
		return null;
	}

	@Override
	public Object visit(NondetAssignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(JMLOld expression) {
		return expression.oldValue().structAccept(this);
	}
}