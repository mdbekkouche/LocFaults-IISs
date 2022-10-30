package java2CFGTranslator;

/** to store positions of JML annotations inside the Java program
 * 
 * @author Olivier Ponsini
 *
 */

public class AssertPosition implements Comparable {
	private int line;
	private int column;
	
	public AssertPosition(int line, int column) {
		this.line = line;
		this.column = column;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}
	
	public int compareTo(Object o) throws ClassCastException 	{
		AssertPosition p = (AssertPosition)o;
		if(line > p.line)
			return 1;
		else if(line < p.line)
			return -1;
		else { //lines are equal
			if(column > p.column)
				return 1;
			else if(column < p.column)
				return -1;
			else  //equality
				return 0;
		}
	}

	public int compareTo(int line, int column) 	{
		if(this.line > line)
			return 1;
		else if(this.line < line)
			return -1;
		else { //lines are equal
			if(this.column > column)
				return 1;
			else if(this.column < column)
				return -1;
			else  //equality
				return 0;
		}
	}
	
	public boolean equals(Object o) {
		if(o instanceof AssertPosition) {
			return (this.line == ((AssertPosition)o).line)
				   && (this.column == ((AssertPosition)o).column);
		}
		return false;
	}
	
	public String toString() {
		return "l:" + line + "c:" + column;
	}
}