package expression;


import java.util.Map;

import validation.util.Type;
import expression.logical.LogicalExpression;
import expression.numeric.NumericExpression;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

public class ParenExpression implements NumericExpression, LogicalExpression {
    
    public Expression arg1;

    public ParenExpression(Expression a1) {
    	arg1 = a1;
    }

    public Expression arg1() {
    	return arg1;
    }
   
    @Override
	public boolean isLinear() {
		return arg1.isLinear();
	}

	@Override
	public Type type() {
		return arg1.type();
	}

    @Override
    public boolean isConstant() {
		return arg1.isConstant();
	}

    @Override
	public boolean isComparison() {
		if (type() == Type.BOOL)
			return ((LogicalExpression)arg1).isComparison();
		else
			return false;
	}

    @Override
	public boolean constantBoolean() {
		if (type() == Type.BOOL)
			return ((LogicalExpression)arg1).constantBoolean();
		else
			return false;
	}

	@Override
    public Number constantNumber() {
		if (type() != Type.BOOL)
			return ((NumericExpression)arg1).constantNumber();
		else
			return null;
	}

    @Override
    public boolean equals(Object o) {
    	return ((o instanceof ParenExpression) && arg1.equals(((ParenExpression)o).arg1))
    			|| arg1.equals(o);
    }

    @Override
    public int hashCode() {
	   	return arg1.hashCode();
	}
	   
    @Override
    public String toString() {
    	return "( " + arg1.toString()  + " )";
    }
        
    /* lydie */   
    @Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

    @Override
	public Expression substitute(Variable var, Expression val) {
		return new ParenExpression(arg1.substitute(var, val));
	}
    
	@Override
    public ParenExpression substitute(Variable var, NumericExpression val) {
		return new ParenExpression(arg1.substitute(var, val));
	}
	
    @Override
    public ParenExpression computeLastRenaming(SymbolTable var){
		return new ParenExpression(arg1.computeLastRenaming(var));
    }

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		return new ParenExpression(arg1.setPrefixInFunction(functionName, prefix));
	}

	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		return new ParenExpression(arg1.substituteAll(substitutions));
	}

}
