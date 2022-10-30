package expression.numeric;


import java.util.Map;

import validation.util.OpCode;
import expression.Expression;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

public class DivExpression extends BinaryExpression {
	
    public DivExpression(NumericExpression a1, NumericExpression a2) {
    	super(a1, a2);
    	linear = a1.isLinear() && a2.isConstant(); //handles cases like x / 3
    }
    
    @Override
	public Number computeConstantNumber() {
		if (arg1.isConstant() && arg2.isConstant()) {
			switch(type) {
			case INT:
				return new Integer(
						arg1.constantNumber().intValue()
						/ arg2.constantNumber().intValue());
			case FLOAT:
				return new Float(
						arg1.constantNumber().floatValue()
						/ arg2.constantNumber().floatValue());
			case DOUBLE:
				return new Double(
						arg1.constantNumber().doubleValue()
						/ arg2.constantNumber().doubleValue());
			default:
				System.err.println("Error (DivExpression): wrong type (" + type + ")!");
				return null;
			}
		}
		else {
			return null;
		}
	}

	@Override
	public OpCode opCode() {
		return OpCode.DIV;
	}

	@Override
	public DivExpression substitute(Variable var, NumericExpression val) {
		return new DivExpression(arg1.substitute(var, val),	arg2.substitute(var, val));
	}
	
	@Override
	public DivExpression computeLastRenaming(SymbolTable var) {
		return new DivExpression(
				arg1.computeLastRenaming(var),
				arg2.computeLastRenaming(var));
	}

	@Override
    public String toString() {
	return "( " + arg1.toString() + " / " + arg2.toString() + " )";
    }
    
    @Override
    public boolean equals(Object o){
    	return (o instanceof DivExpression) 
    		&& arg1.equals(((DivExpression)o).arg1) 
    		&& arg2.equals(((DivExpression)o).arg2);
    }

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		return new DivExpression((NumericExpression)arg1.setPrefixInFunction(functionName, prefix),
				(NumericExpression)arg2.setPrefixInFunction(functionName, prefix));
	}

	@Override
	public NumericExpression substituteAll(Map<Variable, Expression> substitutions) {
		return new DivExpression((NumericExpression)arg1.substituteAll(substitutions), 
								 (NumericExpression)arg2.substituteAll(substitutions));
	}

}