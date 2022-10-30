package validation.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import validation.Validation.VerboseLevel;
import validation.system.ExpressionCtrStore;
import expression.ExpressionVisitor;
import expression.logical.LogicalExpression;

/**
 * Quick hack to guarantee that constraints are really all saved and restored.
 * Almost everything inherited is unused or overridden, so the inheritage hierarchy should be
 * modified... 
 * 
 * @author Olivier Ponsini
 *
 */
public class CtrDeepStore extends ExpressionCtrStore {

	protected ArrayList<LogicalExpression> elts;
	
	protected Stack<ArrayList<LogicalExpression>> save;
	
	public CtrDeepStore() {
		elts = new ArrayList<LogicalExpression>();
		save = new Stack<ArrayList<LogicalExpression>>();
	}
	
	@Override
	public boolean add(LogicalExpression ctr, ExpressionVisitor visitor) {
		return elts.add(ctr);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (LogicalExpression c: elts) {
			s.append(c);
			s.append("\n");
		}
		return s.toString();
	}

	@Override
	public void print() {
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.print(this.toString());
		}		
	}

	@Override
	public void reset() {
		elts.clear();
	}

	public void reset(ArrayList<LogicalExpression> ctrs) {
		elts = ctrs;
	}

	
	@Override
	public void restore() {
		elts = save.pop();
	}

	@Override
	public void save() {
		save.push((ArrayList<LogicalExpression>)elts.clone());
	}

	@Override
	public int size() {
		return elts.size();
	}

	@Override
	public Iterator<LogicalExpression> iterator() {
		return elts.iterator();
	}

	public ArrayList<LogicalExpression> getAllConstraints() {
		return elts;
	}
	
}
