package validation.util;


import java.util.ArrayList;
import java.util.Stack;

import expression.logical.LogicalExpression;


/** class to store the decisions of a path in the program */

public class DecisionPath {
	
	// decisions
	private ArrayList<LogicalExpression> decision; 

	// save
	private Stack<Integer> save;
	
	
	public DecisionPath() {
		decision = new ArrayList<LogicalExpression>();
		save=new Stack<Integer>();
	}
	
	/** add a decision */
	public void addDecision(LogicalExpression c){
		decision.add(c);
	}
	
	//--------------------------
	// methods to handle backtrak
	
	/** save the decisions of the current block */
	public void save() {
		save.push(new Integer(decision.size()));
	}

	/** restore the previous block */
	public void restore() {
		int lastIndex = (Integer)save.pop();
		for (int i = decision.size()-1; i>lastIndex-1; i--) {
			decision.remove(i);
		}
	}
		
	public String toString() {
//		String s = "Path number " + nbPath;
		String s = "\nDecisions taken on the path\n----------------\n";
		for (LogicalExpression e : decision) 
			s += e.toString() + "\n";
		return s;
	}
}
