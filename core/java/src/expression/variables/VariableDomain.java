package expression.variables;

import validation.Validation;
import validation.util.Type;

/** 
 * Class to represent a variable's domain.
 *  
 * @author Olivier Ponsini
 * @author Helen Collavizza
 */
public class VariableDomain implements Cloneable {
	/**
	 * The variable to which this domain is associated.
	 */
	private Variable var;
	/**
	 * Lower bound of this variable domain, if associated variable is numeric.
	 * <code>null<code> means -infinity over reals (which is distinct from 
	 * float infinity).
	 */
	private Number min;
	/**
	 * Upper bound of this variable domain, if associated variable is numeric.
	 * <code>null<code> means +infinity over reals (which is distinct from 
	 * float infinity).
	 */
	private Number max;
	/**
	 * States if the lower bound is open. Default is <code>false</code>.
	 */
	private boolean openMin;
	/**
	 * States if the upper bound is open. Default is <code>false</code>.
	 */
	private boolean openMax;
	/**
	 * Boolean variable value, if associated variable is boolean.
	 * If associated variable is boolean and constant, this field contains its value; 
	 * otherwise it is set to <code>null</code>.
	 */
	private Boolean constantBoolean;
	
	public VariableDomain(Variable v, Number mi, Number ma) {
		var = v;
		min = mi;
		max = ma;
	}

	/**
	 * Creates a new singleton variable domain.
	 * @param v
	 * @param o Must either be a {@link Number} or a {@link Boolean} according to 
	 *          the type of parameter <pre>v</pre>.
	 */
	public VariableDomain(Variable v, Object o) {
		var = v;
		if (v.type == Type.BOOL) {
			constantBoolean = (Boolean)o;			
		}
		else {
			min = (Number)o;
			max = (Number)o;
		}
	}

	protected VariableDomain(VariableDomain d) {
		var = d.var;
		min = d.min;
		max = d.max;
		openMin = d.openMin;
		openMax = d.openMax;
		constantBoolean = d.constantBoolean;
	}
	
	@Override
	public VariableDomain clone() {
		return new VariableDomain(this);
	}
		
	public String name() {
		return var.name();
	}

	public Variable variable() {
		return var;
	}

	public Type type() {
		return var.type();
	}

	public void setVariable(Variable v) {
		var = v;
	}
	
	public void setMinValue(Number min) {
		this.min = min;
	}

	public void setMaxValue(Number max) {
		this.max = max;
	}

	public void setOpenMin(boolean b) {
		openMin = b;
	}
	
	public void setOpenMax(boolean b) {
		openMax = b;
	}
	
	public void setBooleanValue(Boolean b) {
		this.constantBoolean = b;
	}
	
	public Number minValue() {
		return min;
	}

	public Number maxValue() {
		return max;
	}
	
	public boolean isOpenMin() {
		return openMin;
	}

	public boolean isOpenMax() {
		return openMax;
	}
	
	public Boolean booleanValue() {
		return constantBoolean;
	}
	
	public boolean isParameter() {
		return var.isParameter();
	}
	
	public boolean isSingleton() {	
		return (constantBoolean != null) 
		|| ((min != null) && (max != null) && (max.doubleValue() == min.doubleValue()));
	}

	/**
	 * The computed interval is only correct for discrete types; in particular, 
	 * it won't work if {@link #min} or {@link #max} are set to <code>null</code>.
	 *  
	 * @return A String representation of the numeric interval stored in this 
	 * variable's domain.
	 */
	private String numericInterval() {
		String s;
		if (min.equals(max)) 
			s = min.toString();
		else 
			s = "[" + min + " , " + max + "]";
		return s;
	}
	
	public String toString() {
		String s = var.name();
		switch(var.type()) {
		case BOOL:
			s += "{ false, true } : ";
			if (constantBoolean == null) {
				s += "{ false, true } = ";
			}
			else {
				s += constantBoolean;
			}
			break;
		case INT:
			s += "[" + Validation.INTEGER_MIN_BOUND + ":" 
			     + Validation.INTEGER_MAX_BOUND + "] = ";
			s += numericInterval();
			break;
		case FLOAT:
			s += "[" + -Float.MAX_VALUE + ":" + Float.MAX_VALUE + "] = ";
			s += numericInterval();
			break;
		case DOUBLE:
			s += "[" + -Double.MAX_VALUE + ":" + Double.MAX_VALUE + "] = ";
			s += numericInterval();
			break;
		default:
			s += "[UKNOWN TYPE]";
			break;
		}		
		return s;
	}
	
	/**
	 * Returns the single value contained in this domain.
	 * This is only meaningful if {@link #isSingleton()} is <code>true</code>.
	 *  
	 * @return It is either a Boolean or a Number according to the domain's type.
	 */
	public Object singletonValue() {
		if (var.type == Type.BOOL) {
			return constantBoolean;
		}
		else {
			return min;
		}
	}

	/**
	 * Set this domain to the union of it and the given domain. 
	 * 
	 * @param domain
	 */
	public void union(VariableDomain domain) {
		if (min.doubleValue() > domain.minValue().doubleValue())
			min = domain.minValue();
		if (max.doubleValue() < domain.maxValue().doubleValue())
			max = domain.maxValue();						
	}
}
