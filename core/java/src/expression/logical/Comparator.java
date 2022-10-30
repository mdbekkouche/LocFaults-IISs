package expression.logical;

import expression.Expression;
import expression.ExpressionVisitor;
import validation.util.OpCode;

public abstract class Comparator extends AbstractLogicalExpression {

	Expression arg1;
	Expression arg2;
	
	public Comparator(Expression a1, Expression a2) {
		arg1 = a1;
		arg2 = a2;
		linear = a1.isLinear() && a2.isLinear();		
		constantBoolean = computeConstantBoolean();
	}
	
	public Expression arg1() {
		return arg1;
	}

	public Expression arg2() {
		return arg2;
	}
	
	@Override
    public boolean isComparison() {
		return true;
	}	
		
    @Override
    public int hashCode() {
		return (arg1.hashCode() + arg2.hashCode())%Integer.MAX_VALUE;
	}
	
    @Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	public abstract OpCode opCode();
	
	/**
	 * This is called by this class' constructor to compute 
	 * this logical comparison's value when it is constant.
	 * Should be overridden by all derived classes.
	 * 
	 * @return the value of this logical expression when constant.
	 */
	public abstract Boolean computeConstantBoolean();

}