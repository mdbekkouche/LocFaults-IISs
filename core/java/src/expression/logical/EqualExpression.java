package expression.logical;


import java.util.Map;

import expression.Expression;
import expression.numeric.NumericExpression;
//import expression.variables.ArraySymbolTable;
import expression.variables.SymbolTable;
import expression.variables.Variable;
import validation.util.OpCode;
import validation.util.Type;

/* classe pour l'expression de relation == */
public class EqualExpression extends Comparator {

    public EqualExpression(Expression a1, Expression a2) {
    	super(a1, a2);
    }

    @Override
    public Boolean computeConstantBoolean() {
		if (arg1.isConstant() && arg2.isConstant()) {
			//We assume well-typed Java expressions, so either two logical expressions or
			//two numeric expressions are compared
			if (arg1.type() == Type.BOOL) { //Two logical expressions are compared
				return ((LogicalExpression)arg1).constantBoolean() 
					== ((LogicalExpression)arg2).constantBoolean();
			}
			else { //Two numeric expressions are compared
				return ((NumericExpression)arg1).constantNumber().doubleValue() 
				== ((NumericExpression)arg2).constantNumber().doubleValue();	
			}
		}
		else {
			return null;
		}
    }

    @Override
    public OpCode opCode() {
		return OpCode.EQU;
	}

    @Override
   public String toString() {
    	return "( " + arg1.toString() + " == " + arg2.toString() + " )";
    }

   @Override
   public boolean equals(Object o){
    	return (o instanceof EqualExpression) 
    		&& arg1.equals(((EqualExpression)o).arg1) 
    		&& arg2.equals(((EqualExpression)o).arg2);
    }
    
    @Override
    public EqualExpression substitute(Variable var, Expression val) {
		Expression a1 = arg1.substitute(var,val);
		Expression a2 = arg2.substitute(var,val);
		return new EqualExpression(a1, a2);
	}
	
    @Override
    public EqualExpression computeLastRenaming(SymbolTable var) {
		Expression a1 = arg1.computeLastRenaming(var);
		Expression a2 = arg2.computeLastRenaming(var);
		return new EqualExpression(a1, a2);
	}
    
	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		Expression a1 =arg1.setPrefixInFunction(functionName, prefix);
		Expression a2 =arg2.setPrefixInFunction(functionName, prefix);
		return new EqualExpression(a1, a2);
	}
	
	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		Expression a1 = arg1.substituteAll(substitutions);
		Expression a2 = arg2.substituteAll(substitutions);
		return new EqualExpression(a1, a2);
	}
}
