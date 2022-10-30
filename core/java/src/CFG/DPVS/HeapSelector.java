package 	CFG.DPVS;


import java.util.Comparator;
import java.util.PriorityQueue;


import expression.variables.Variable;

/**
 * 
 * 
 * @author ponsini
 *
 */
public class HeapSelector implements VariableSelector {
			
	/**
	 * Java PriorityQueue is backed by a decreasing priority heap.
	 */
	private PriorityQueue<Variable> heap; 
	
	public HeapSelector(Comparator<Variable> cmp) {
		heap = new PriorityQueue<Variable>(11, cmp);
	}

	private HeapSelector() {
		//Nothing to do
	}
	
	@Override
	public boolean isEmpty() {
		return heap.isEmpty();
	}

	@Override
	public Variable pop() {
		return heap.poll();
	}

	@Override
	public void push(Variable v) {
		heap.offer(v);
	}
	
	@Override
	public HeapSelector clone() {
		HeapSelector clone = new HeapSelector();
		clone.heap = new PriorityQueue<Variable>(heap);
		return clone;
	}

}
