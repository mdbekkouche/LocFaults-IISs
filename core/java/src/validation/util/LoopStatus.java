package validation.util;


import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;
import java.lang.Cloneable;


/** class to store unfolding status of loops in the program */

public class LoopStatus {
	
	class LoopInformation implements Cloneable {
		int maxUnfold; // current max unfolding
		int lastUnfold; // last possible max unfolding
		boolean lastReached;
		int currentUnfold; // current unfolding
		int ident;
		// true if it is possible to enter one more time into the loop
		boolean moreUnfold; 
//		boolean running;
		int exploredPath;
				
		LoopInformation(int n) {
			maxUnfold = -1;
			lastUnfold = 0;
			currentUnfold = 0;
			ident = n;
			moreUnfold = true;
			lastReached = false;
//			running = true;
		}
		
		LoopInformation(int n,int nbPath) {
			this(n);
			exploredPath=nbPath;
		}
	
		void setMaxUnfold(int m) {
			maxUnfold = m;
		}
	
		void updateMaxUnfold() {
			maxUnfold++;
		}
			
		void updateReached() {
			lastReached=true;
		}
		
		void setLastUnfold(int m) {
			lastUnfold = m;
		}
		
		public void noMoreUnfold() {
			moreUnfold=false;
		}

		boolean lastReached() {
			if (!lastReached)
				return maxUnfold > lastUnfold;
			return false;
		}

		public String toString() {
			return "Status for loop # " + ident +
			"\nMaximum number of unfoldings : " + maxUnfold +
			", Current unfolding number :" + currentUnfold;
		}
		
		/**
		 * Clones this LoopInformation object.
		 * 
		 * @return A shallow copy of this LoopInformation object.
		 */
		@Override
		protected Object clone() {
			Object o = null;
			try {
				o = super.clone();
			} catch(CloneNotSupportedException e) {
				e.printStackTrace();
				System.exit(20);
			}
			return o;
		}
	}
	
	
	// loop informations
	private HashMap<Integer,LoopInformation> loopInfo; 
	
	// stack for loop nesting
	private Stack<Integer> loop;

	// save
	private Stack<Object> save;
	
	public LoopStatus() {
		loopInfo = new HashMap<Integer,LoopInformation>();
		loop = new Stack<Integer>();
		save = new Stack<Object>();
	}
	
	/** to add  a new loop entry with key n */
	public void start(int n,int nbPath) {
		Integer nn = new Integer(n);
		loop.push(nn);
		if (loopInfo.isEmpty() || (!loopInfo.isEmpty()&&!loopInfo.containsKey(nn)))
			loopInfo.put(nn,new LoopInformation(nn,nbPath));
	}
	
	/**
	 * Reset the status of all loops.
	 */
	public void reset() {
		loopInfo.clear();
		loop.clear();
		save.clear();
	}
	
//	public boolean running(int n) {
//		Integer nn = new Integer(n);
//		if (!loopInfo.containsKey(nn)) 
//			return false;
//		else return loopInfo.get(nn).running;
//	}
	
	/** gets the current unfolding number for loop n */
	public int currentUnfold() {
		return loopInfo.get(loop.peek()).currentUnfold;
	}
	
	/** gets the maximum unfolding number for loop n */
	public int maxUnfold() {
		return loopInfo.get(loop.peek()).maxUnfold;
	}
	
	/** add one to the current unfolding number for loop n */
	public void addUnfold() {
		loopInfo.get(loop.peek()).currentUnfold++;
	}
	
	/** set maximum unfold number for loop n */
	public void setMaxUnfold(int m) {
		loopInfo.get(loop.peek()).setMaxUnfold(m);
	}
	
	/** set last unfold number for loop n */
	public void setLastUnfold(int m) {
		loopInfo.get(loop.peek()).setLastUnfold(m);
	}
	
	/** reset current unfold number  */
	public void resetCurrentUnfold() {
		loopInfo.get(loop.peek()).currentUnfold=0;
	}

	
	/** update the maximum number of unfoldings
	 * used when need to enter one more time into the loop */
	public void updateMaxUnfold() {
		loopInfo.get(loop.peek()).updateMaxUnfold();
	}

	public void updateReached() {
		loopInfo.get(loop.peek()).updateReached();
	}
	
	/** to know if the maximum number of loop unfolding has been reached */
	public boolean lastReached() {
		if (loop.empty()) return false;
		return loopInfo.get(loop.peek()).lastReached();
	}
	
	/** to know if it is possible to enter one more time into the loop */
	public boolean moreUnfold() {
		if (loop.empty()) return false;
		return loopInfo.get(loop.peek()).moreUnfold;
	}
	
	/** to know if it is possible to enter one more time into the loop */
	public void noMoreUnfold() {
		if (!loop.empty()) 
			loopInfo.get(loop.peek()).noMoreUnfold();
	}

	/** to know if the loop currently executed is n */
	public boolean isCurrent(int n) {
		if (loop.empty()) return false;
		return loopInfo.get(loop.peek()).ident==n;
	}

	/** to know the identifier of the loop currently executed  */
	public int current() {
		if (!loop.empty()) return loopInfo.get(loop.peek()).ident;
		return -1;
	}
	
	/** to remove the current loop  */
	public void end() {
//		loopInfo.get(loop.peek()).running=false;
		resetCurrentUnfold();
		loop.pop();
	}

	public int getPath() {
		return loopInfo.get(loop.peek()).exploredPath;
	}
	
	public void setPath(int n) {
		 loopInfo.get(loop.peek()).exploredPath=n;
	}

	
	//--------------------------
	// methods to handle backtrak
	
	/** save the decisions of the current block */
	public void save() {
		save.push(loop.clone());
		save.push(loopInfoClone());
	}

	/** restore the previous block */
	public void restore() {
		loopInfo = (HashMap<Integer,LoopInformation>)save.pop();
		loop = (Stack<Integer>)save.pop();
	}
	
	/**
	 * Creates a deep copy of the {@link #loopInfo} map.
	 * 
	 * @return A new map containing the same entries as the loopInfo map, but the values
	 *         are clone copies of the original map ones.
	 */
	private HashMap<Integer,LoopInformation> loopInfoClone() {
		HashMap<Integer,LoopInformation> liMap = new HashMap<Integer,LoopInformation>();
		for(Entry<Integer, LoopInformation> entry: loopInfo.entrySet()) {
			liMap.put(entry.getKey(), (LoopInformation)entry.getValue().clone());
		}
		return liMap;
	}

	public String toString() {
		String s = "\nCurrent status of program loops \n----------------\n";
		s+=loopInfo.toString();
		return s;
	}
}
