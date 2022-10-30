package expression.logical;


import java.util.Map;

import expression.Expression;
import expression.ExpressionVisitor;
import expression.numeric.NumericExpression;
//import expression.variables.ArraySymbolTable;
import expression.variables.ArrayVariable;
import expression.variables.SymbolTable;
import expression.variables.Variable;

public class ArrayAssignment extends Assignment {
	private ArrayVariable previousTab; // variable before SSA renaming
	private NumericExpression index; // index

//	public ArrayAssignment(ArrayVariable va, NumericExpression i, NumericExpression v) {
//		this(va,null,i,v);
//	}
	
	public ArrayAssignment(
				ArrayVariable va, 
				ArrayVariable prevVa, 
				NumericExpression i, 
				Expression v) 
	{
		super(va,v);
		previousTab = prevVa;
		index = i;
		linear = v.isLinear();
	}

	public ArrayAssignment(
			ArrayVariable va, 
			ArrayVariable prevVa, 
			NumericExpression i, 
			Expression v,
			int startLine) 
	{
		this(va, prevVa, i, v);
		this.startLine = startLine;
	}

	public ArrayVariable array(){
    	return (ArrayVariable)variable;
    }
    
	public ArrayVariable previousArray(){
    	return previousTab;
    }
	
    public NumericExpression index() {
    	return index;
    }
    
    @Override
    public  String toString() {
		String result = "";
		if (rhs!=null) result = variable.toString() + 
		"[" + index.toString() + "] := " + rhs.toString();
		return result ;
	}

    @Override
    public boolean equals(Object o){
		return (o instanceof ArrayAssignment) 
			&& super.equals(o) && previousTab.equals(((ArrayAssignment)o).previousTab) 
			&& index.equals(((ArrayAssignment)o).index) ;
	}

    @Override
    public int hashCode() {
		return (super.hashCode() + previousTab.hashCode() + index.hashCode())%Integer.MAX_VALUE;
	}

    @Override
    public Object structAccept(ExpressionVisitor visitor) {
		return visitor.visit(this);
	}

    @Override
    public ArrayAssignment substitute(Variable var, Expression val) {
		//index and values are NumericExpressions so are their substitutes
		NumericExpression i = (NumericExpression)index.substitute(var, val);
		Expression v = rhs.substitute(var, val);
		return new ArrayAssignment((ArrayVariable)variable, previousTab,i, v);
	}

    @Override
    public ArrayAssignment computeLastRenaming(SymbolTable var) {
		//index and values are NumericExpressions so are their last renamings
		NumericExpression i = index.computeLastRenaming(var);
		Expression val = rhs.computeLastRenaming(var);
		ArrayVariable lastTab = (ArrayVariable)var.get(variable.root());
		//TODO: is it previousTab ????
		return new ArrayAssignment(lastTab,previousTab, i, val);
	}

	@Override
	public Expression setPrefixInFunction(String functionName, String prefix) {
		NumericExpression i = (NumericExpression)index.setPrefixInFunction(functionName, prefix);
		Expression v = rhs.setPrefixInFunction(functionName, prefix);
		return new ArrayAssignment((ArrayVariable)variable, previousTab,i, v);
	}

	@Override
	public ArrayAssignment substituteAll(Map<Variable, Expression> substitutions) {
		NumericExpression i = (NumericExpression)index.substituteAll(substitutions);
		Expression v = rhs.substituteAll(substitutions);
		return new ArrayAssignment((ArrayVariable)variable,previousTab, i, v);
	}	
    
}
