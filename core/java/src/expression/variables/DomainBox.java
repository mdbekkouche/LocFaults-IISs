package expression.variables;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

/** a class to store the value of a solution (its variables with
 * their domains
 * @author helen
 */
public class DomainBox implements Iterable<VariableDomain> {

	private LinkedHashMap<String, VariableDomain> variableDomains; 
	
	public DomainBox() {
		//Creates a hash map whose iteration order is the insertion one.
		variableDomains = new LinkedHashMap<String, VariableDomain>();
	}	

	public Collection<VariableDomain> getDomains() {
		return variableDomains.values();
	}
		
	public void add(DomainBox val) {
		for (VariableDomain sv : val.getDomains()) {
			variableDomains.put(sv.name(), sv);
		}
	}

	public void add(VariableDomain sv) {
		variableDomains.put(sv.name(), sv);
	}

	public void remove(VariableDomain sv) {
		variableDomains.remove(sv.name());
	}

	public void remove(String name) {
		variableDomains.remove(name);
	}
	
	public VariableDomain get(String name) {
		return variableDomains.get(name);
	}
	
	public boolean isEmpty() {
		return variableDomains.isEmpty();
	}
	
	@Override
	public Iterator<VariableDomain> iterator() {
		return variableDomains.values().iterator();
	}
	
	/**
	 * Reduces the domains of the variables of this solution.
	 * 
	 * Each solved variable of this solution is viewed as a domain for 
	 * the corresponding variable (min and max values being the bounds of the domain).
	 * This method reduces these domains according to those given as parameters:
	 * <ul>
	 * <li>If a domain does not exist yet, it is created with the parameter values;</li>
	 * <li>If the parameter domain for a given variable is smaller than the current domain 
	 *     for the same variable of this object, the current domain is updated with the 
	 *     parameter domain's values.</li>
	 * </ul>     
	 * 
	 * @param filteredDomains New domains considered for possible reduction of the current ones.
	 *                        Should not be null;
	 * 
	 * @return the number of added and reduced domains.
	 */
	public int reduceDomains(DomainBox filteredDomains) {
		int nb_reduc=0;
		boolean reduc;
		Number min, max;
		
		for (VariableDomain filtered_sv: filteredDomains.getDomains()) {
			VariableDomain sv = variableDomains.get(filtered_sv.name());
			if (sv == null) {  
				//La variable n'a pas encore de domaine, nous le créons 
				variableDomains.put(filtered_sv.name(), filtered_sv);
				nb_reduc++;
			}
			else {
				reduc = false;
		
				//Nous vérifions si une réduction de domaine a eu lieu sur chaque borne
				min = sv.minValue();
				if (filtered_sv.minValue().doubleValue() > sv.minValue().doubleValue()) {
					//sv.setMinValue(filtered_sv.minValue());
					min = filtered_sv.minValue();
					reduc = true;
				}

				max = sv.maxValue();
				if (filtered_sv.maxValue().doubleValue() < sv.maxValue().doubleValue()) {
					//sv.setMaxValue(filtered_sv.maxValue());
					max = filtered_sv.maxValue();
					reduc = true;
				}
				
				if (reduc) {
					variableDomains.put(filtered_sv.name(), new VariableDomain(filtered_sv.variable(), min, max));
					nb_reduc++;
				}
			}
		}
		return nb_reduc;
	}
	
	@Override
	public String toString() {
		String s="";
		for(VariableDomain sv: variableDomains.values()){
			s+=sv.toString()+"\n";
		}
		return s;
	}

}
