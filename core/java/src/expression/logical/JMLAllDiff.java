package expression.logical;


import java.util.Map;

import expression.Expression;
import expression.ExpressionVisitor;
//import expression.variables.ArraySymbolTable;
import expression.variables.ArrayVariable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

// to represent CSP constraint AllDiff : all values of the array
// must be different

public class JMLAllDiff extends AbstractLogicalExpression {
	private ArrayVariable tab; // variable 

	public JMLAllDiff(ArrayVariable va) {
		tab = va;
		linear = false;
	}
	
	public ArrayVariable array(){
    	return tab;
    }
    
	@Override
    public  String toString() {
		return "AllDiff(" + tab.name() + ")" ;
	}

	@Override
    public boolean equals(Object o){
		return (o instanceof JMLAllDiff) && tab.equals(((JMLAllDiff)o).tab);
	}

	@Override
    public int hashCode() {
		return tab.hashCode();
	}

	@Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
    public JMLAllDiff substitute(Variable var, Expression val) {
		// TODO : substitute with an array
		return this;
	}

	@Override
    public JMLAllDiff computeLastRenaming(SymbolTable intVar){
		return new JMLAllDiff(tab.computeLastRenaming(intVar));
	}

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression substituteAll(Map<Variable, Expression> substitutions) {
		// TODO Auto-generated method stub
		return this;
	}


	

		
}
