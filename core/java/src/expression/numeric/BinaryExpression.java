package expression.numeric;

import expression.ExpressionVisitor;
import validation.util.OpCode;
import validation.util.Type;

public abstract class BinaryExpression extends AbstractNumericExpression {

	protected NumericExpression arg1;
	protected NumericExpression arg2;

	public BinaryExpression(NumericExpression a1, NumericExpression a2) {
		arg1 = a1;
	  	arg2 = a2;
	  	linear = arg1.isLinear() && arg2.isLinear();
	  	type = Type.mostGeneralType(arg1.type(), arg2.type());
		constantNumber = computeConstantNumber();
	}

	/**
	 * This is called by this class' constructor to compute 
	 * this arithmetic operation's value when it is constant.
	 * Should be overridden by all derived classes.
	 * 
	 * @return the value as a <code>Number</code> of this arithmetic expression when constant, <code>null</code> otherwise.
	 */
	public abstract Number computeConstantNumber();
	
	public NumericExpression arg1() {
		return arg1;
	}
	  
	public NumericExpression arg2() {
		return arg2;
	}
	
	@Override
	public int hashCode() {
		return (arg1.hashCode() + arg2.hashCode())%Integer.MAX_VALUE;
	}
	
	/**
	 * @return this arithmetic operator's code.
	 */
	public abstract OpCode opCode();
		    	    
	@Override
	public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

}