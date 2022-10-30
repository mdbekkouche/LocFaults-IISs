package validation.system;

/** 
 * Convenient type to denote stores of array variables extending AbstractVarStore and implementing
 * ArrayVariableStore.
 * 
 * @author Olivier Ponsini
 */
public abstract class AbstractArrayVariableStore<ConcreteType> 
	extends AbstractVarStore<ConcreteType> 
	implements ArrayVariableStore<ConcreteType> 
{
		//Left blank intentionally
}
