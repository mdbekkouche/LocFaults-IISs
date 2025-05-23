package expression.logical;


import java.util.Map;

import expression.Expression;
import expression.numeric.NumericExpression;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;
import validation.util.OpCode;

public class SupEqualExpression extends Comparator {

    public SupEqualExpression(Expression a1, Expression a2) {
    	super(a1, a2);
    }

    @Override
    public Boolean computeConstantBoolean() {
		if (arg1.isConstant() && arg2.isConstant()) {
			//We assume well-typed Java expressions, so two numeric expressions are compared
			return ((NumericExpression)arg1).constantNumber().doubleValue() 
				>= ((NumericExpression)arg2).constantNumber().doubleValue();	
		}
		else {
			return null;
		}
    }
    
    @Override
    public OpCode opCode() {
		return OpCode.GEQ;
	}

    @Override
    public String toString() {
    	return "( " + arg1.toString() + " >= " + arg2.toString() + " )";
    }

    @Override
    public boolean equals(Object o){
    	return (o instanceof SupEqualExpression) 
    		&& arg1.equals(((SupEqualExpression)o).arg1) 
    		&& arg2.equals(((SupEqualExpression)o).arg2);
    }

    @Override
    public SupEqualExpression substitute(Variable var, Expression val) {
		Expression a1 = arg1.substitute(var, val);
		Expression a2 = arg2.substitute(var, val);
		return new SupEqualExpression(a1, a2);
	}
	
    @Override
    public SupEqualExpression computeLastRenaming(SymbolTable var) {
		Expression a1 = arg1.computeLastRenaming(var);
		Expression a2 = arg2.computeLastRenaming(var);
		return new SupEqualExpression(a1, a2);
	}
  
	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		Expression a1 = arg1.setPrefixInFunction(functionName, prefix);
		Expression a2 =  arg2.setPrefixInFunction(functionName, prefix);
		return new SupEqualExpression(a1, a2);
	}
	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		LogicalExpression a1 = (LogicalExpression)arg1.substituteAll(substitutions);
		LogicalExpression a2 = (LogicalExpression)arg2.substituteAll(substitutions);
		return new SupEqualExpression(a1, a2);
	}
}
