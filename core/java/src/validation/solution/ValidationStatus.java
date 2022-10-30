package validation.solution;

import java.util.ArrayList;

/** a class to store information on solving 
 * for the global validation system
 * @author helen
 */
public class ValidationStatus {
	
	private ArrayList<SolverStatus> statuses;
	/**
	 * Time needed to parse the program and build the corresponding CFG.
	 */
	public long cfgBuildingTime;
	/**
	 * Time needed to simplify the CFG.
	 */
	public long cfgSimplificationTime;
			
	public ValidationStatus() {
		statuses = new ArrayList<SolverStatus>();
	}
		
	public void addStatus(String name) {
		statuses.add(new SolverStatus(name));
	}

	public void addStatus(SolverStatus status) {
		statuses.add(status);
	}
	
	/** get total resolution time */
	public double getTotalTime() {
		double time = 0;
		for (SolverStatus status: statuses) {
			time += status.getTime();
		}
		return time;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("--\nTime for parsing and CFG construction: ");
		s.append(cfgBuildingTime/1000.0);
		s.append(" s.\nTime for CFG simplification: ");
		s.append(cfgSimplificationTime/1000.0);
		s.append(" s.\n--\n");
		for (SolverStatus status: statuses) {
			if (status.getSolve() != 0) {
				s.append(status.toString());
				s.append("\n-----------\n");
			}
		}
		return s.toString();		
	}
 }


