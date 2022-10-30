package validation.solution;

/** a class to store and compute 
 * information about resolution in a solver */

public class SolverStatus {
	
	// local variable to set the time before
	private long before;
	
	// name of the system
	private String name;
	
	// current solving time 
	private long time;
	
	// number of solving
	private int solve;

	// number of fails
	private int fail;

	public SolverStatus(String n) {
		name=n;
		before=0;
		time=0;
		solve=0;
		fail=0;
	}
	
	public void setCurrentTime() {
		before = System.currentTimeMillis();
	}
	
	public long getElapsedTime() {
		return System.currentTimeMillis() - before;
	}
	
	public void setElapsedTime() {
		time+=getElapsedTime();
	}
	
	public double getTime() {
		return time/1000.0;
	}
	
	public void moreFail() {
		fail++;
	}

	public void moreFail(int i) {
		fail += i;
	}
	
	public int getFail() {
		return fail;
	}
	
	public void moreSolve() {
		solve++;
	}

	public void moreSolve(int i) {
		solve += i;
	}
	
	public int getSolve() {
		return solve;
	}
	
	public String toString(){
		String s= name + " CSP :\n";
		s+="  Number of solve " + getSolve() +"\n";
		s+="  Number of fail " + getFail() +"\n";
		s+="  Solving time " + getTime() +"s";
		return s;
	}
}
