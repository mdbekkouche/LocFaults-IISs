package expression.variables;

import validation.util.Type;

/**
 * This class extends {@link Variable} by also storing the concrete variable, i.e. the variable
 * managed by the concrete solver. 
 * 
 * @author Olivier Ponsini
 *
 * @param <ConcreteType> Type used to encapsulate the concrete variables. 
 */
public class ConcreteArrayVariable<ConcreteType> extends ArrayVariable {

	private ConcreteType concreteArray;
	
	//-----------------
	//Constructors
	//-----------------

	public ConcreteArrayVariable(String name, Type type, int length, ConcreteType cv) {
		super(name, type, length);
		this.concreteArray = cv;
	}

	public ConcreteArrayVariable(ArrayVariable v, ConcreteType cv) {
		super(v);
		this.concreteArray = cv;
	}

	public ConcreteArrayVariable(ArrayVariable v) {
		this(v, null);
	}

	//copy constructor
	public ConcreteArrayVariable(ConcreteArrayVariable<ConcreteType> cv) {
		this(cv, cv.concreteArray);
	}
	
	//-----------------
	//Accessors
	//-----------------

	public ConcreteType concreteArray() {
		return concreteArray;
	}
	
	public void setConcreteArray(ConcreteType concreteVar) {
		this.concreteArray = concreteVar;
	}
	//-------------------
	//Cloneable interface
	//-------------------

	@Override
	public ConcreteArrayVariable<ConcreteType> clone() {
		return new ConcreteArrayVariable<ConcreteType>(this);
	}

}
