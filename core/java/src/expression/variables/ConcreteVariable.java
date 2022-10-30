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
public class ConcreteVariable<ConcreteType> extends Variable {

	private ConcreteType concreteVar;
	
	//-----------------
	//Constructors
	//-----------------
	
	public ConcreteVariable(String name, Type type, ConcreteType concreteVar) {
		super(name, type);
		this.concreteVar = concreteVar;
	}
	
	public ConcreteVariable(Variable v, ConcreteType concreteVar) {
		super(v);
		this.concreteVar = concreteVar;
	}

	public ConcreteVariable(Variable v) {
		this(v, null);
	}
	
	//Copy constructor
	protected ConcreteVariable(ConcreteVariable<ConcreteType> v) {
		this(v, v.concreteVar);
	}
	
	//-----------------
	//Accessors
	//-----------------

	public ConcreteType concreteVar() {
		return concreteVar;
	}
	
	public void setConcreteVar(ConcreteType concreteVar) {
		this.concreteVar = concreteVar;
	}
	
	//-------------------
	//Cloneable interface
	//-------------------

	@Override
	public ConcreteVariable<ConcreteType> clone() {
		return new ConcreteVariable<ConcreteType>(this);
	}
	
}
