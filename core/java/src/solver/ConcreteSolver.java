package solver;

public interface ConcreteSolver {

	/** to enable a search process */
	public void startSearch();
	
	/** to stop the search */
	public void stopSearch();

	/** true if current CSP a a next solution */
	public boolean next() ; 
	
	public String toString() ;
}
