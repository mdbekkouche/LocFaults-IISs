package validation.strategies.cfg.localization;

import java.util.HashMap;
import java.util.List;

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
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.numeric.NumericExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;

/**
 * Given all variable values, this visitor computes the value of an expression. 
 * 
 * @author Olivier Ponsini
 *
 */
public class RhsExpressionComputer implements ExpressionVisitor {

	/**
	 * Maps variables' name with their value.
	 */
	protected HashMap<String, Expression> knownValues;

	public Expression compute(Expression rhs, HashMap<String, Expression> varValues) {
		knownValues = varValues;
		return (Expression)rhs.structAccept(this);
	}
	

	/**************************************************/
	/****        EXPRESSION VISITOR               *****/
	/**************************************************/

	@Override
	public Object visit(ParenExpression expression) {
		return expression.arg1.structAccept(this);
	}

	@Override
	public Object visit(Variable variable) {
		return knownValues.get(variable.name());
	}
	
	/* (non-Javadoc)
	 * @see expression.ExpressionVisitor#visit(expression.variables.ArrayElement)
	 * 
	 * Array elements are stored in <code>constants</code> with the array's name 
	 * followed by '[index value]'.
	 */
	@Override
	public Object visit(ArrayElement elt) {
		// Simplifying index expression
		IntegerLiteral index = (IntegerLiteral)elt.index().structAccept(this);
		return knownValues.get(elt.array().name() + "[" + index.literal() + "]");
	}
	
	@Override
	public Object visit(IntegerLiteral literal) {
		return literal;
	}

	@Override
	public Object visit(FloatLiteral literal) {
		return literal;
	}

	@Override
	public Object visit(DoubleLiteral literal) {
		return literal;
	}

	@Override
	public Object visit(LogicalLiteral literal) {
		return literal;
	}
	
	@Override
	public Object visit(BinaryExpression expression) {
		NumericExpression arg1 = (NumericExpression)expression.arg1().structAccept(this);
		NumericExpression arg2 = (NumericExpression)expression.arg2().structAccept(this);
		switch (expression.opCode()) {
		case ADD:
			return visitAdd(arg1, arg2, expression.type());
		case SUB:
			return visitSub(arg1, arg2, expression.type());
		case MUL:
			return visitMul(arg1, arg2, expression.type());
		case DIV:
			return visitDiv(arg1, arg2, expression.type());
		default:
			System.err.println("Error (ConstantPropagator): unknown binary expression: " + expression);
			return expression;
		}
	}

	private NumericExpression visitAdd(NumericExpression arg1, NumericExpression arg2, Type type) {
		switch(type) {
		case INT:
			return new IntegerLiteral(
					arg1.constantNumber().intValue() + arg2.constantNumber().intValue());
		case FLOAT:
			return new FloatLiteral(
					arg1.constantNumber().floatValue() + arg2.constantNumber().floatValue());
		case DOUBLE:	
			return new DoubleLiteral(
					arg1.constantNumber().doubleValue() + arg2.constantNumber().doubleValue());
		default:
			System.err.println("Error (ConstantPropagator - Plus): wrong type (" + type + ")!");
			return null;
		}
	}
	
	private NumericExpression visitSub(NumericExpression arg1, NumericExpression arg2, Type type) {
		switch(type) {
		case INT:
			return new IntegerLiteral(
					arg1.constantNumber().intValue() - arg2.constantNumber().intValue());
		case FLOAT:
			return new FloatLiteral(
					arg1.constantNumber().floatValue() - arg2.constantNumber().floatValue());
		case DOUBLE:	
			return new DoubleLiteral(
					arg1.constantNumber().doubleValue() - arg2.constantNumber().doubleValue());
		default:
			System.err.println("Error (ConstantPropagator - Minus): wrong type (" + type + ")!");
			return null;
		}
	}

	private NumericExpression visitMul(NumericExpression arg1, NumericExpression arg2, Type type) {
		switch(type) {
		case INT:
			return new IntegerLiteral(
					arg1.constantNumber().intValue() * arg2.constantNumber().intValue());
		case FLOAT:
			return new FloatLiteral(
					arg1.constantNumber().floatValue() * arg2.constantNumber().floatValue());
		case DOUBLE:	
			return new DoubleLiteral(
					arg1.constantNumber().doubleValue() * arg2.constantNumber().doubleValue());
		default:
			System.err.println("Error (ConstantPropagator - Time): wrong type (" + type + ")!");
			return null;
		}
	}

	private NumericExpression visitDiv(NumericExpression num, NumericExpression denom, Type type) {
		switch(type) {
		case INT:
			return new IntegerLiteral(
					num.constantNumber().intValue()	/ denom.constantNumber().intValue());
		case FLOAT:
			return new FloatLiteral(
					num.constantNumber().floatValue() / denom.constantNumber().floatValue());
		case DOUBLE:
			return new DoubleLiteral(
					num.constantNumber().doubleValue() / denom.constantNumber().doubleValue());
		default:
			System.err.println("Error (ConstantPropagator - Div): wrong type (" + type + ")!");
			return null;
		}	
	}

	@Override
	public Object visit(Comparator expression) {
		Expression arg1 = (Expression)expression.arg1().structAccept(this);
		Expression arg2 = (Expression)expression.arg2().structAccept(this);
		return visitComparator(arg1, arg2, expression.opCode());
	}
	
	private LogicalExpression visitComparator(Expression arg1, Expression arg2, OpCode cmp) {
		// We assume comparisons are between NumericExpression or (exclusive) LogicalExpression
		if (arg1 instanceof NumericExpression) {
			double val1 = ((NumericExpression)arg1).constantNumber().doubleValue();
			double val2 = ((NumericExpression)arg2).constantNumber().doubleValue();
			switch (cmp) {
			case EQU:
				return new LogicalLiteral(val1 == val2);
			case LT:
				return new LogicalLiteral(val1 < val2);
			case LEQ:
				return new LogicalLiteral(val1 <= val2);
			case GT:
				return new LogicalLiteral(val1 > val2);
			case GEQ:
				return new LogicalLiteral(val1 >= val2);
			}
		}
		else { //It is a LogicalExpression, only equality is allowed
			boolean val1 = ((LogicalExpression)arg1).constantBoolean();
			boolean val2 = ((LogicalExpression)arg2).constantBoolean();
			if (cmp == OpCode.EQU) {
				return new LogicalLiteral(val1 == val2);
			}
		}
		// we should never reach this point...
		return null;
	}

	@Override
	public Object visit(AndExpression expression) {
		LogicalExpression arg1 = (LogicalExpression)expression.arg1().structAccept(this);
		LogicalExpression arg2 = (LogicalExpression)expression.arg2().structAccept(this);
		if (arg1.constantBoolean()) 
			return arg2;
		else 
			return arg1;
	}

	@Override
	public Object visit(OrExpression expression) {
		LogicalExpression arg1 = (LogicalExpression)expression.arg1().structAccept(this);
		LogicalExpression arg2 = (LogicalExpression)expression.arg2().structAccept(this);
		if (arg1.constantBoolean())
			return arg1;
		else 
			return arg2;
	}

	@Override
	public Object visit(NotExpression expression) {
		LogicalExpression arg1 = (LogicalExpression)expression.arg1().structAccept(this);
		if (arg1.constantBoolean()) 
			return new LogicalLiteral(false);
		else
			return new LogicalLiteral(true);
	}

	/* (non-Javadoc)
	 * @see expression.ExpressionVisitor#visit(expression.logical.JMLImpliesExpression)
	 * 
	 * We are potentially doing some work twice here since when evaluating '(A==>B)==>C' 
	 * from CplexLocalizationExpressionVisitor we will evaluate 'A' twice...
	 */
	@Override
	public Object visit(JMLImpliesExpression expression) {
		LogicalLiteral arg1 = (LogicalLiteral)expression.arg1().structAccept(this);
		if (arg1.constantBoolean())
			return expression.arg2().structAccept(this);
		else
			return new LogicalLiteral(true);
	}
	
	@Override
	public Object visit(MethodCall mc) {
		// TODO Auto-generated method stub
		return null;
	}

	
	// -----------------------------------------------------
	// Following expressions should never be visited...
	//------------------------------------------------------
	
	@Override
	public Object visit(JMLOld expression) {
		return null;
	}

	@Override
	public Object visit(JMLForAllExpression forAll) {		
		return null;
	}

	@Override
	public Object visit(JMLExistExpression exist) {
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		return null;
	}

	@Override
	public Object visit(JMLAllDiff expression) {
		return null;
	}

	@Override
	public Object visit(NondetAssignment assignment) {
		return null;
	}

	@Override
	public Object visit(ArrayAssignment assignment) {
		return null;
	}

	@Override
	public Object visit(ArrayVariable variable) {
		return null;
	}
	
	// ---------------------------------------------------------
	// END OF: Following expressions should never be visited...
	//----------------------------------------------------------

}
