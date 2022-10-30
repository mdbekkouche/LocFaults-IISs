package expression.numeric;


import java.util.Map;

import expression.Expression;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

import validation.util.OpCode;

public class MinusExpression extends BinaryExpression {

    public MinusExpression(NumericExpression a1, NumericExpression a2) {
    	super(a1, a2);
    }

    @Override
	public Number computeConstantNumber() {
		if (arg1.isConstant() && arg2.isConstant()) {
			switch(type) {
			case INT:
				return new Integer(
						arg1.constantNumber().intValue()
						- arg2.constantNumber().intValue());
			case FLOAT:
				return new Float(
						arg1.constantNumber().floatValue()
						- arg2.constantNumber().floatValue());
			case DOUBLE:
				return new Double(
						arg1.constantNumber().doubleValue()
						- arg2.constantNumber().doubleValue());
			default:
				System.err.println("Error (MinusExpression): wrong type (" + type + ")!");
				return null;
			}
		}
		else {
			return null;
		}
	}

    @Override
	public OpCode opCode() {
		return OpCode.SUB;
	}

	@Override
	public MinusExpression substitute(Variable var, NumericExpression val) {
		return new MinusExpression(arg1.substitute(var, val), arg2.substitute(var, val));
	}
	
	@Override
	public MinusExpression computeLastRenaming(SymbolTable var) {
		return new MinusExpression(
				arg1.computeLastRenaming(var),
				arg2.computeLastRenaming(var));
	}

	@Override
    public String toString() {
	return "( " + arg1.toString() + " - "+ arg2.toString() + " )";
    }
    
    @Override
    public boolean equals(Object o){
    	return (o instanceof MinusExpression) 
    		&& arg1.equals(((MinusExpression)o).arg1) 
    		&& arg2.equals(((MinusExpression)o).arg2);
    }

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		return new MinusExpression((NumericExpression)arg1.setPrefixInFunction(functionName, prefix),
				(NumericExpression)arg2.setPrefixInFunction(functionName, prefix));
	}
	
	@Override
	public NumericExpression substituteAll(Map<Variable, Expression> substitutions) {
		return new MinusExpression((NumericExpression)arg1.substituteAll(substitutions),
								   (NumericExpression)arg2.substituteAll(substitutions));
	}
	    
}
