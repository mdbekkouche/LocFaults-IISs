package expression.logical;

import validation.util.OpCode;

public abstract class LogicalOperator extends AbstractLogicalExpression {

	LogicalExpression arg1;
	LogicalExpression arg2;

	protected LogicalOperator(LogicalExpression a1, LogicalExpression a2) {
		arg1 = a1;
		arg2 = a2;
		constantBoolean = computeConstantBoolean();
	}
	
	// used to build the JML spec as a list of subcases
	// class Strategy
	public LogicalExpression arg1() {
		return arg1;
	}

	public LogicalExpression arg2() {
		return arg2;
	}
		
	public abstract OpCode opCode();
	
	/**
	 * This is called by this class' constructor to compute 
	 * this logical operation's value when it is constant.
	 * Should be overridden by all derived classes.
	 * 
	 * @return the value of this logical expression when constant.
	 */
	public abstract Boolean computeConstantBoolean();

	@Override
    public int hashCode() {
		return (arg1.hashCode() + arg2.hashCode())%Integer.MAX_VALUE;
	}

}
