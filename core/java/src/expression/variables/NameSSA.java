package expression.variables;

import java.util.Stack;

/* Classe pour gérer le nom des variables avec renomage pour forme ssa
 */


public class NameSSA implements Cloneable {
	protected int cpt; // le compteur des renomages
	private String name;
	
	// stack to save and restore current renaming
	private Stack<Integer> save;

	/* créé une variable de nom n  */
	public NameSSA(String n) {
		this(n, 0);
	}

	public NameSSA(String n, int c) {
		name=n;
		cpt=c;
		save = new Stack<Integer>();
	}


	public String name() {
		return name;
	}

	public int currentNumber() {
		return cpt;
	}
	
	// renomage suivant
	public void next() {
		cpt++;
	}
	
	public void setNumber(int num) {
		cpt = num;
	}

	// nom de la variable courante
	public String current(){
		return (name + "_" + cpt);
	}

	// nom de la variable suivante
	public String nextVar(){
		return (name + "_" + (cpt+1));
	}


	public int hashcode() {
		return name.hashCode();
	}

	public Object clone() {
		return new NameSSA(name,cpt);
	}
	
	public void save() {
		save.push(new Integer(cpt));
	}

	public void restore() {
		cpt = save.pop().intValue();
	}
	
}
