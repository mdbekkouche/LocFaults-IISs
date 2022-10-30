package expression.numeric;


import java.util.Map;

import expression.Expression;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

import validation.util.OpCode;

public class TimeExpression extends BinaryExpression {

    public TimeExpression(NumericExpression a1, NumericExpression a2) {
    	super(a1, a2);
    	linear = computeLinear();
    }

    private boolean computeLinear() {
    	if (arg1.isConstant()) 
    		return arg2.isLinear();
      	if (arg2.isConstant()) 
      		return arg1.isLinear();
        return false;
    }

    @Override
	public Number computeConstantNumber() {
		if (arg1.isConstant() && arg2.isConstant()) {
			switch(type) {
			case INT:
				return new Integer(
						arg1.constantNumber().intValue()
						* arg2.constantNumber().intValue());
			case FLOAT:
				return new Float(
						arg1.constantNumber().floatValue()
						* arg2.constantNumber().floatValue());
			case DOUBLE:
				return new Double(
						arg1.constantNumber().doubleValue()
						* arg2.constantNumber().doubleValue());
			default:
				System.err.println("Error (TimeExpression): wrong type (" + type + ")!");
				return null;
			}
		}
		else if ((arg1.isConstant() && (arg1.constantNumber().doubleValue() == 0))
				|| (arg2.isConstant() && (arg2.constantNumber().doubleValue() == 0)))
			return new Integer(0);
		else
			return null;
	}

	@Override
	public OpCode opCode() {
		return OpCode.MUL;
	}
    
	@Override
	public TimeExpression substitute(Variable var, NumericExpression val) {
		return new TimeExpression(arg1.substitute(var, val), arg2.substitute(var, val));
	}
    
	@Override
	public TimeExpression computeLastRenaming(SymbolTable var) {
		return new TimeExpression(
				arg1.computeLastRenaming(var),
				arg2.computeLastRenaming(var));
	}

	@Override
	public String toString() {
	return "( " + arg1.toString() + " * " + arg2.toString() + " )";
    }
    
	@Override
	public boolean equals(Object o){
    	return (o instanceof TimeExpression) 
    		&& arg1.equals(((TimeExpression)o).arg1) 
    		&& arg2.equals(((TimeExpression)o).arg2);
    }
	
	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		return new TimeExpression((NumericExpression)arg1.setPrefixInFunction(functionName, prefix),
				(NumericExpression)arg2.setPrefixInFunction(functionName, prefix));
	}

	@Override
	public NumericExpression substituteAll(Map<Variable, Expression> substitutions) {
		return new TimeExpression((NumericExpression)arg1.substituteAll(substitutions),	
					              (NumericExpression)arg2.substituteAll(substitutions));
	}
	
}
