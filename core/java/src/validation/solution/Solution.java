package validation.solution;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import validation.Validation;

import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;

/** class to represent a solution of the validation :
 * by default, the solution is empty
 * @author helen
 *
 */
public  class Solution implements Iterable<Variable> {
	
	// resolution time required to build this solution
	protected long time;
	
	// if solved, no need for searching other solutions
	protected boolean stopped;
	
	// the variables values
	protected LinkedHashMap<String, Variable> variables;
	
	public Solution(long time) {
		this.stopped = false;
		this.time = time;
		this.variables = new LinkedHashMap<String, Variable>();
	}
		
	public Solution() {
		this(0);
	}
	
	public boolean isEmpty() {
		return variables.isEmpty();
	}
			
	public void setTime(long t) {
		time = t;
	}

	public long getTime(){
		return time;
	}
	
	public void stop() {
		stopped = true;
	}
	
	public void start() {
		stopped = false;
	}
	
	public boolean stopped() {
		return stopped;
	}
	
	public void reset() {
		time = 0;
		stopped = false;
		variables.clear();
	}
	
	public Variable get(String name) {
		return variables.get(name);
	}

	public void add(Variable v) {
		variables.put(v.name(), v);
	}
	
	/**
	 * Adds to the current solution the given set of variables. 
	 * @param s A set of variables, disjoint from the current set {@link #variables}.
	 */
	public void addSolution(Collection<Variable> vars) {
		for (Variable v: vars) {
			variables.put(v.name(), v);
		}
	}

	public void setSolution(Collection<Variable> vars) {
		variables.clear();
		addSolution(vars);
	}

	public void setSolution(DomainBox domains) {
		variables.clear();
		Variable v;
		for (VariableDomain d: domains) {
			v = d.variable();
			v.setDomain(d);
			variables.put(v.name(), v);
		}
	}
		
	public void copy(Solution s) {
		variables = s.variables;
		time = s.time;
		stopped = s.stopped;
	}
	
	/**
	 * Converts a Solution into a DomainBox.
	 * 
	 * @return The list of domains of the variables in this solution as a DomainBox.
	 */
	public DomainBox toDomainBox() {
		DomainBox box = new DomainBox();
		for (Variable v: variables.values()) {
			box.add(v.domain());
		}
		return box;
	}

	public static String noSolutionMessage(long elapsedTime) {
		String s = "\n---------\n";
		s+= "No solution";
		s+= "\nResolution time : " + elapsedTime/1000.0 + "s\n";
		return s + "\n---------\n";
		
	}
	
	/** to be completed by child classes */
	@Override
	public String toString() {
		String s;
		if (isEmpty()) 
				s = noSolutionMessage(time);
		else {
			s = "\n---------\n";
			s+= "Counter-example found  \n";
			for (Variable v: variables.values()) {
				s += v.domain().toString() + "\n";
			}
			s+= "\nResolution time : " + time /1000.0 + " s\n";
			s += "\n---------\n";
		}
		return s;
	}

	@Override
	public Iterator<Variable> iterator() {
		return variables.values().iterator();
	}
	
	public void writeToFile(String fileName) {
		try {
			BufferedWriter counterexample = new BufferedWriter(new FileWriter(fileName));
			for (Variable v: this.variables.values()) {
				counterexample.write(v.name());
				counterexample.write(" ");
				counterexample.write(v.domain().singletonValue().toString());
				counterexample.newLine();
			}
			counterexample.close();
		} catch (IOException ioException) {
			System.err.println("Error (Solution.writeToFile): unable to open/write file " + Validation.counterExampleFileName + "!");
			ioException.printStackTrace();
		}
	}

}
