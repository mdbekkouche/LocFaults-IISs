package solver.fplib;

import java.util.ArrayList;

import validation.system.xml.ValidationCSP;

import validation.util.OpCode;

import expression.Expression;
import expression.ParenExpression;
import expression.MethodCall;
import expression.SimpleArrayExpressionVisitor;
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
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;

/**
 * Creates <code>fplib</code> constraints from CPBPV ones.
 * 
 * @author Olivier Ponsini
 *
 */
public class FplibExpressionVisitor extends SimpleArrayExpressionVisitor {
	
	public FplibExpressionVisitor(ValidationCSP csp) {
		super("FPLIB", csp);
	}

	//--------------------------------------------------------------
	// Build new constraints.
	// The new constraints are returned as pointers to the fplib 
	// concrete constraints.
	//--------------------------------------------------------------
	
	public Long visit(Assignment assignment) {
		long concreteVar = ((FplibVarBlock)csp.varBlock).get(assignment.lhs().name()).concreteVar();
		long rightExpr = (Long)assignment.rhs().structAccept(this);
		return FplibSolver.fplibMkAssign(concreteVar, rightExpr);
	}

	public ArrayList<Long> visit(AndExpression expression) {
		ArrayList<Long> ctrs = new ArrayList<Long>();
		Object res;

		// arg1 could itself be an AND operator and return an array of constraints
		res = expression.arg1().structAccept(this);
		if (res instanceof Long) {
			ctrs.add((Long)res);
		}
		else { // res must be an ArrayList<Long>
			ctrs.addAll((ArrayList<Long>)res);
		}

		// arg2 could itself be an AND operator and return an array of constraints
		res = expression.arg2().structAccept(this);			
		if (res instanceof Long) {
			ctrs.add((Long)res);
		}
		else { // res must be an ArrayList<Long>
			ctrs.addAll((ArrayList<Long>)res);
		}
		return ctrs;
	}
		
	public Long visit(Comparator expression) {
		long leftExpr = (Long)expression.arg1().structAccept(this);
		long rightExpr = (Long)expression.arg2().structAccept(this);
		return FplibSolver.fplibMkOp(
					expression.opCode().ordinal(), 
					leftExpr, 
					rightExpr);
	}

	private Long negateComparator(Comparator cmp) {
		Expression a1 = cmp.arg1();
		Expression a2 = cmp.arg2();
		switch (cmp.opCode()) {
		case EQU:
			long leftExpr = (Long)a1.structAccept(this);
			long rightExpr = (Long)a2.structAccept(this);
			return (Long)FplibSolver.fplibMkOp(
							OpCode.NEQ.ordinal(), 
							leftExpr, 
							rightExpr);
		case LT:
			 return (Long)new SupEqualExpression(a1,a2).structAccept(this);
		case GT: 
			 return (Long)new InfEqualExpression(a1,a2).structAccept(this);
		case LEQ: 
			 return (Long)new SupExpression(a1,a2).structAccept(this);
		case GEQ: 
			 return (Long)new InfExpression(a1,a2).structAccept(this);
		default:
			System.err.println("Error (negateComparator): unknown comparator (" + cmp + ")!");
			System.exit(43);
			return null;
		}
	}

	public Object visit(NotExpression expression) {
		LogicalExpression a1 = expression.arg1();
		if (a1 instanceof NotExpression) {
			return ((NotExpression)a1).arg1().structAccept(this);
		}
		else if (a1.isComparison()) {
			return negateComparator((Comparator)a1);
		}
		else if (a1 instanceof ParenExpression) {
			//We assume well-formed Java expressions: the not operator argument is a LogicalExpression
			LogicalExpression e = (LogicalExpression)((ParenExpression)a1).arg1();
			return new NotExpression(e).structAccept(this);			
		}
		else if (a1 instanceof OrExpression) {
			OrExpression o = (OrExpression)a1;
			AndExpression a = new AndExpression(new NotExpression(o.arg1()), 
												new NotExpression(o.arg2()));
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
		}  
		else if (a1 instanceof JMLExistExpression) {
			JMLForAllExpression e = (JMLForAllExpression)a1;
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
		}
		else if (a1 instanceof AndExpression) {
			System.err.println("Error: Negating a logical AND operator "
					+ "should create an OR operator which is not supported with fplib!");
			System.exit(44);
			return null;
		} 
		else {
			System.err.println("Error: Negating this expression: " 
					+ a1.getClass()
					+ " is not supported with fplib!");			
			System.exit(45);
			return null;
		}
	}
	
	public Long visit(IntegerLiteral literal) {
		return FplibSolver.fplibMkIntCst(literal.constantNumber().intValue());
	}

	public Long visit(FloatLiteral literal) {
		return FplibSolver.fplibMkFloatCst(literal.constantNumber().floatValue());
	}

	public Long visit(DoubleLiteral literal) {
		return FplibSolver.fplibMkDoubleCst(literal.constantNumber().doubleValue());
	}

	public Long visit(LogicalLiteral literal) {
		long leftExpr = FplibSolver.fplibMkIntCst(0);
		long rightExpr = FplibSolver.fplibMkIntCst(1);
		int opCode;
		if (literal.constantBoolean()) {
			opCode = OpCode.GT.ordinal();
		}
		else {
			opCode = OpCode.LT.ordinal();			
		}
		return FplibSolver.fplibMkOp(opCode, leftExpr, rightExpr);
	}

	public Long visit(Variable variable) {
		return FplibSolver.fplibMkVar(((FplibVarBlock)csp.varBlock).get(variable.name()).concreteVar());
	}

	public Long visit(BinaryExpression expression) {
		long leftExpr = (Long)expression.arg1().structAccept(this);
		long rightExpr = (Long)expression.arg2().structAccept(this);
		return FplibSolver.fplibMkOp(
						expression.opCode().ordinal(), 
						leftExpr, 
						rightExpr);
	}

	public Object visit(MethodCall mc) {
		long[] concreteParams = new long[mc.parameters().size()];
		int i = 0;
		for (Expression param: mc.parameters()) {
			concreteParams[i++] = ((Long)param.structAccept(this));
		}
		return FplibSolver.fplibMkMethodCall(mc.methodName(), concreteParams);
	}


	//-----------------------------------------
	// Unimplemented methods
	//-----------------------------------------

	public Object visit(JMLForAllExpression expression) {
		System.err.println("Error: the ForAll quantifier is not yet implemented with fplib!");
		System.exit(47);
		return null;
	}

	public Object visit(JMLExistExpression expression) {
		System.err.println("Error: the Exist quantifier is not yet implemented with fplib!");
		System.exit(47);
		return null;
	}

	public Object visit(ArrayVariable variable) {
		System.err.println("Error: array are not yet implemented with fplib!");
		System.exit(47);
		return null;
	}

	public Object visit(OrExpression expression) {		
		System.err.println("Error: logical operator OR not yet implemented with fplib solver!");	
		System.exit(41);
		return null;
	}

	public Object visit(JMLImpliesExpression expression) {
		System.err.println("Error: the IMPLY logical operator is not yet implemented with fplib!");
		System.exit(47);
		return null;
	}

	public Object visit(JMLAllDiff expression) {
		System.err.println("Error: the AllDiff constraint is not yet implemented with fplib!");
		System.exit(47);
		return null;
	}

	@Override
	public Object visit(NondetAssignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object visit(JMLOld expression) {
		return this.visit(expression.oldValue());
	}

}