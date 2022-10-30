package solver.glpk_fplib;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import solver.fplib.FplibSolver;
import solver.fplib.FplibValidationCSP;
import solver.glpk.GlpkSolver;
import solver.glpk.GlpkValidationCSP;
import solver.glpk.GlpkVarBlock;
import validation.Validation.VerboseLevel;
import validation.system.xml.SimpleArrayValidationSystem;
import validation.solution.Solution;
import validation.solution.ValidationStatus;
import validation.util.Type;
import expression.Expression;
import expression.logical.EqualExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;
import expression.variables.Variable.Use;

/**
 * This validation system manages a CSP on rationals using <code>GLPK</code> for the
 * specification part and a CSP on floats using <code>Fplib</code> for the Java method
 * part.  
 *  
 * @author Olivier Ponsini
 *
 */
public class GlpkFplibValidationSystem extends SimpleArrayValidationSystem {
	/**
	 * The CSP over reals (specification part).
	 */
	protected GlpkValidationCSP realCSP; 
	/**
	 * The CSP over floats (method body part).
	 */
	protected FplibValidationCSP floatCSP;
	/**
	 * The solver over floats. 
	 * Convenient shortcut.
	 */
	private FplibSolver fplibSolver;
	/**
	 * The solver over reals. 
	 * Convenient shortcut.
	 */
	private GlpkSolver glpkSolver;

	/**
	 * Constructs a validation system with a CSP on rationals (GLPK) for 
	 * the specification and a CSP over floats (Fplib) for the method body.
	 * 
	 * @param name The name of the file containing the XML description of the Java 
	 *             program to be verified.
	 */
	public GlpkFplibValidationSystem(String name) {
		super(name + " using GLPK (specification) + Fplib (method).");
		fplibSolver = (FplibSolver)floatCSP.cspSolver;
		glpkSolver = (GlpkSolver)realCSP.cspSolver;
	}
	
	/** 
	 * Creates the CSPs over rationals and floats.
	 * This method is called by the super class.
	 */
	@Override
	protected void setCSP() {
		status = new ValidationStatus();
		realCSP = new GlpkValidationCSP("GLPK (rational CSP)", this);
		floatCSP = new FplibValidationCSP("Fplib (float CSP)", this);
		status.addStatus(realCSP.getStatus());
		status.addStatus(floatCSP.getStatus());
	}
	
	@Override
	public String printStatus() {
		return status.toString();
	}

	@Override
	public Variable addNewVar(String n, Type t, Use u) {
		Variable v = var.addNewVar(n, t, u);
		floatCSP.addVar(v);
		//For GLPK, we only add variables needed for the specification part 
		if (!isParsingMethodBody)
			realCSP.addVar(v);
		return v;
	}

	@Override
	public Variable addNewVar(String n, Type t) {
		return addNewVar(n, t, Use.LOCAL);
	}

	// add a new variable for the new SSA renaming of n
	@Override
	public Variable addVar(String n) {
		Variable v = var.addVar(n);
		floatCSP.addVar(v);
		//For Fplib, we only add variables needed for the specification part 
		if (!isParsingMethodBody)
			realCSP.addVar(v);
		return v;
	}
	
	@Override
	// helen
	public void addVar(Variable v) {
		floatCSP.addVar(v);
		//For Fplib, we only add variables needed for the specification part 
		if (!isParsingMethodBody)
			realCSP.addVar(v);
	}

	@Override
	public void addConstraint(LogicalExpression c) {
		if (isParsingMethodBody)
			floatCSP.addConstraint(c);
		else //Specification
			realCSP.addConstraint(c);
	}

	/**
	 * Adds the post-condition to the constraint problems 
	 * managed by this validation system.
	 * 
	 * @param v A variable to be substituted.
	 * @param expr The expression to substitute to the variable.
	 */
	//Method is overriden because here we do not want to substitute
	//the return expression to the special variable JMLResult; on the 
	//contrary, we add a constraint that links this variable with the 
	//return expression
	@Override
	public LogicalExpression updatePostcond(Variable v, Expression expr) {
		return new EqualExpression(v, expr);
	}

	/**
	 * Adds the given negated post-condition expression to this validation system constraints. 
	 * A new constraint is built from the expression. For this, we separate the "for all"
	 * quantified expressions from the rest of the post-condition.
	 * 
	 * @param pc The post-condition expression.
	 */
	@Override
	public void addPostcond(LogicalExpression pc) {
		LogicalExpression validPC = parseNegatedClausePostcond(pc);
		// Parsing the postcondition may return an empty constraint (e.g. an non enumerable
		// quantifier)
		if (validPC != null) {
			//We add the post-condition to the Fplib CSP instead of the GLPK CSP because Fplib
			//handles strict inequalities and the not equal operator. BUT FOR THIS TO BE SOUND,
			//the post-condition MUST NOT contain arithmetic expressions (otherwise they will 
			//be evaluated over floats instead of over reals). It is always possible to get rid of
			//arithmetic expressions in the post-condition by adding new variables set to these
			//expressions in the pre-condition.
			//floatCSP.constraints().add(validPC, floatCSP.varBlock, null);
			floatCSP.addConstraint(validPC);
		}
		//We do not handle the non enumerable 'forall' that was put aside
		if (this.postcondForAll != null) {
			System.err.println("Error (addPostcond): GLPK does not handle non enumerable quantifiers!");
			System.exit(40);
		}
	}

	@Override
	public void addPrecond(LogicalExpression c) {
		if (c != null)
			realCSP.addConstraint(c);
		//We do not handle the non enumerable 'forall' that was put aside
		if (this.precondForAll != null) {
			System.err.println("Error (addPrecond): GLPK does not handle non enumerable quantifiers!");
			System.exit(40);
		}
	}
	
	/**
	 * States whether the precondition has been already filtered.
	 * This is only to be used in tryDecision.
	 * Java has no way to limit the scope of a member to a method.
	 */
	private Boolean preConditionFiltered = false;
	/**
	 * This is the box obtained after filtering the variable domains 
	 * with GLPK according to the pre condition.
	 * This is only to be used in tryDecision.
	 * Java has no way to limit the scope of a member to a method.
	 */
	private DomainBox preConditionBox = null;
	
	@Override
	public boolean tryDecision(LogicalExpression c) {
		//This method is only called when parsing the method body, 
		//hence only the solver over floats (Fplib) is concerned, but we
		//also want to benefit from the domain reductions the 'requires' 
		//clause allows. Hence, we call glpk over reals. 

		boolean hasSolution = false;
		
		//Filtering inputs according the precondition with GLPK only has to be done once 
		//and preConditionFiltered is a flag to state whether it has already been done
		if (!preConditionFiltered) {
			realCSP.startSearch();
			preConditionBox = new DomainBox();
			preConditionBox = glpkFilterDomains(glpkSolver, (GlpkVarBlock)realCSP.varBlock, null);
			realCSP.stopSearch();
			preConditionFiltered = true;
		}
		
		//We go on if the precondition is satisfiable (GLPK filtering did 
		//not report an inconsistent CSP)
		if (preConditionBox != null) {
			floatCSP.save();
			floatCSP.addConstraint(c);
			floatCSP.startSearch();
			floatCSP.setFplibDomains(preConditionBox);
			hasSolution = floatCSP.next();
			floatCSP.stopSearch();
			floatCSP.restore();
		}
		
		return hasSolution;
	}
	
	@Override
	public boolean checkPostcond(LogicalExpression returnAssignment) {
		boolean foundSolution;
		
		addPostcond(postcond);
		floatCSP.addConstraint(returnAssignment);
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("\n###########################");
			System.out.println("Return statement reached for path " + casePathNumber());
		}
		
		
		//Get a reference on the lastPostcondSolution attribute, empty it and update it
		//through solve().
		Solution lastPCSolutionRef = getLastPostcondSolution();
		lastPCSolutionRef.reset();
		foundSolution = solve(lastPCSolutionRef);
		
		//Notifier à l'IHM la solution result
		displaySolution(lastPCSolutionRef);
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("###########################\n");
		}
		return foundSolution;
	}

	/**
	 * 
	 * @param result Ignored.
	 * 
	 * @return Always returns false.
	 */
	public boolean solve(Solution result) {
		boolean foundSolution;
		
		if (VerboseLevel.VERBOSE.mayPrint()) {
			System.out.println("Specification:");
			System.out.println(realCSP.constraintBlock().toString());
			System.out.println("Method:");
			System.out.println(floatCSP.constraintBlock().toString());
		}

		//Creates the GLPK and Fplib CSPs
		realCSP.startSearch();
		floatCSP.startSearch();

		//kBFixPoint();
		foundSolution = filterAndCut(1e-9, result);		

		floatCSP.stopSearch();
		realCSP.stopSearch();

		return foundSolution;
	}

	public boolean filterAndCut(double minDomainSize, Solution counterExample) {
		GlpkVarBlock glpkVarBlock = (GlpkVarBlock)realCSP.varBlock;
		DomainBox box = new DomainBox();
		Stack<DomainBox> boxes = new Stack<DomainBox>();
		Collection<DomainBox> newBoxes;		
		DomainBox newBox;

		//We manage a stack of boxes and the corresponding stack of solver's states
		//for Fplib
		boxes.push(box);
		fplibSolver.push();
		
		int i = 0;
		int nb_reduc;
		while (!boxes.empty()) {
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("Nb explored boxes = " + i++);
			}
			
			//Take a box and restore the corresponding Fplib solver's state
			box = boxes.pop();
			fplibSolver.pop();
			
			if (VerboseLevel.DEBUG.mayPrint()) {
				System.out.println("Box=" + box);
			}
			
			newBox = glpkFilterDomains(glpkSolver, glpkVarBlock, box);
			
			if (newBox != null) { //CSP has a feasible solution with GLPK
				
				if (newBox != box) //Some filtering occured
					nb_reduc = box.reduceDomains(newBox);
				else
					nb_reduc = 0;
				
				if (VerboseLevel.DEBUG.mayPrint()) {
					System.out.println("Box after GLPK Filtering=" + box);
				}
				
				newBox = fplib2BFilterDomains(fplibSolver, box);
				
				if (newBox != null) { //CSP was 2B consistent with Fplib
					nb_reduc += box.reduceDomains(newBox);
					if (VerboseLevel.VERBOSE.mayPrint()) {
						if (VerboseLevel.DEBUG.mayPrint()) {
							System.out.println("Box after Fplib Filtering=" + box);
						}
						System.out.println("nb reductions=" + nb_reduc);
					}

					//UNCOMMENT to add k-test strategy
					//Collection<DomainBox> kBoxes = kTests(box, 10);
					
					//Cut the current box in several sub-boxes
					//Collection<DomainBox> newBoxes = cutBox(box, minDomainSize);
					newBoxes = cutBoxAndIterate(box, 0);
					
					if (newBoxes.size() == 1) {
						//Current box could not be cut
						if (VerboseLevel.DEBUG.mayPrint()) {
							System.out.println("Possible solution=" + newBoxes.iterator().next());
						}
						counterExample.setSolution(newBoxes.iterator().next());
						return true;
					}
					else { //Current box has been cut
						//We add all the new boxes to the stack of boxes left to explore
						boxes.addAll(newBoxes);
						//We also save, as many times as the number of boxes, the current
						//Fplib solver's state in its internal stack so that we can restore it
						//when we will explore the newly added boxes
						for (int nbbox=0; nbbox<newBoxes.size(); nbbox++) {
							fplibSolver.push();
						}
						
						//UNCOMMENT to add k-test strategy
						//boxes.addAll(kBoxes);
						//for (int nbbox=0; nbbox<kBoxes.size(); nbbox++) {
							//FplibSolver.fplibPush();
						//}
						
					}
				}
				else { //CSP was NOT 2B consistent with Fplib
					if (VerboseLevel.VERBOSE.mayPrint()) {
						System.out.println("No Fplib Solution!");
					}
				}
			}
			else { //CSP had NO feasible solution with GLPK
				if (VerboseLevel.VERBOSE.mayPrint()) {
					System.out.println("No GLPK Solution!");
				}
			}
		}
		//No solution found
		return false;
	}
	
	Collection<DomainBox> cutBox(DomainBox box, double minDomainSize) {
		VariableDomain cutDomain;
		double cutDomainSize;
		
		//We look for a domain to cut in their insertion order in the box' domain list.
		//We skip too small domains.
		//TODO: How to treat unbounded domains
		Iterator<VariableDomain> iter = box.getDomains().iterator();
		do {
			cutDomain = iter.next();
			cutDomainSize = Math.abs(
					cutDomain.maxValue().doubleValue() 
					- cutDomain.minValue().doubleValue());
		} while ((cutDomainSize <= minDomainSize) && iter.hasNext());
		
		//If no suitable domain could be found (they all are too small),
		//we return the current box.
		if (cutDomainSize <= minDomainSize) {
			ArrayList<DomainBox> boxes = new ArrayList<DomainBox>(1);
			boxes.add(box);
			return boxes;
		}

		//Otherwise, we compute the double at the middle of the selected domain, 
		//and build the two half domains based on this value and the next greater double value.
		//Doing this, we miss some reals, but we are interested in the floating-point values 
		//(not the ones in the Reals) that can lead the program body to violate the specification.
		double half = 
			(cutDomain.minValue().doubleValue() + cutDomain.maxValue().doubleValue()) / 2.0;
		VariableDomain halfDomain1 = new VariableDomain(cutDomain.variable(),
														cutDomain.minValue(),
														half);
		VariableDomain halfDomain2 = new VariableDomain(cutDomain.variable(),
														Math.nextUp(half),
														cutDomain.maxValue());
		
		//System.out.println(
		//		"Cutting according to " + cutDomain.name() 
		//		+ " d1=" + halfDomain1 
		//		+ " d2=" + halfDomain2);
		
		//Create the box with the first half domain
		DomainBox halfBox = new DomainBox();
		//This preserves the insertion order of box in halfBox
		halfBox.add(box);
		//Replace the value of the existing cut domain in halfBox by the first half domain.
		//Removing it and reinserting it puts it last in the insertion order so that it 
		//will be the last one to be tried the next time we look for a domain to cut.
		//Insertion and removal are constant time (LinkedHashTable).
		halfBox.remove(halfDomain1);
		halfBox.add(halfDomain1);
		
		//re-use the existing box as the second half box 
		box.remove(halfDomain2);
		box.add(halfDomain2);
		
		//Return the two boxes with the two half domains.
		ArrayList<DomainBox> boxes = new ArrayList<DomainBox>(2);
		boxes.add(halfBox);
		boxes.add(box);
		return boxes;
	}

	Collection<DomainBox> cutBoxAndIterate(DomainBox box, double minDomainSize) {
		VariableDomain cutDomain;
		double cutDomainSize;
		
		//We look for a domain to cut in their insertion order in the box' domain list.
		//We skip too small domains.
		//TODO: How to treat unbounded domains
		Iterator<VariableDomain> iter = box.getDomains().iterator();
		do {
			cutDomain = iter.next();
			//This should always be >= 0
			cutDomainSize = cutDomain.maxValue().doubleValue() 
					        - cutDomain.minValue().doubleValue();
		} while ((cutDomainSize <= minDomainSize) && iter.hasNext());
		
		//If no suitable domain could be found (they all are too small),
		//we return the current box.
		if (cutDomainSize <= minDomainSize) {
			ArrayList<DomainBox> boxes = new ArrayList<DomainBox>(1);
			boxes.add(box);
			return boxes;
		}
		
		//Otherwise, we partition cutDomain in several sub-domains and create as many 
		//corresponding new sub-boxes
		return partition3(box, cutDomain);
	}
	
	
	/**
	 * Tries to build 3 boxes, by partioning one variable domain <pre>[a,b]</pre> into 
	 * three new sub-domains:
	 *   - a degnerated one made of the middle of the variable domain
	 *   - a second one going from <pre>a</pre> to the double just before the middle
	 *   - a third one going from the double value just after the middle to <pre>b</pre>
	 * 
	 * Doing this, we miss some reals, but we are interested in the floating-point values
	 * (not the ones in the Reals) that can lead the program body to violate the specification.
	 * 
	 * @param box The box to be cut. It is modified by this method.
	 * @param cutDomain The domain to be partitioned. 
	 *                  Should contain at least two distinct floats.
	 * 
	 * @return
	 */
	Collection<DomainBox> partition3(DomainBox box, VariableDomain cutDomain) {
		VariableDomain newDomain;
		DomainBox newBox;
		ArrayList<DomainBox> boxes = new ArrayList<DomainBox>(3);	
		Variable cutVar = cutDomain.variable();
		String cutVarName = cutDomain.name();
		double min = cutDomain.minValue().doubleValue();
		double max = cutDomain.maxValue().doubleValue();
		double middle = (min + max) / 2.0;
		
		double middlePrevious = Math.nextAfter(middle, Double.NEGATIVE_INFINITY);
		if (min <= middlePrevious) {
			newDomain = new VariableDomain(cutVar, min, middlePrevious);
			//Create the box with the first half domain
			newBox = new DomainBox();
			//This preserves the insertion order of box in halfBox
			newBox.add(box);
			//Replace the value of the existing cut domain in halfBox by the 
			//first half domain.
			//Removing it and reinserting it puts it last in the insertion order so that it 
			//will be the last one to be tried the next time we look for a domain to cut.
			//Insertion and removal are constant time (LinkedHashTable).
			newBox.remove(cutVarName);
			newBox.add(newDomain);
			//Add this new box to the list of returned setDomainsboxes
			boxes.add(newBox);
		}
		
		double middleNext = Math.nextUp(middle);
		if (max >= middleNext) {
			newDomain = new VariableDomain(cutVar, middleNext, max);
			newBox = new DomainBox();
			newBox.add(box);
			newBox.remove(cutVarName);
			newBox.add(newDomain);
			boxes.add(newBox);
		}
		
		newDomain = new VariableDomain(cutVar, middle, middle);
		//re-use the existing box for the degenerated domain of the middle value 
		box.remove(cutVarName);
		box.add(newDomain);
		//We add the box with the degenerated domain last so that it will be the first 
		//one poped from the stack of boxes
		boxes.add(box);
		
		return boxes;
	}
	
	/**
	 * Tries to Build 5 boxes, by splitting one variable domain <pre>[a,b]</pre> into 
	 * five new sub-domains:
	 * <ol>
	 *   <li>a degnerated one made of the max bound of the variable domain [b, b]</li>
	 *   <li>a degnerated one made of the min bound of the variable domain [a, a]</li>
	 *   <li>a degnerated one made of the middle of the variable domain [middle, middle]</li>
	 *   <li>another one going from <pre>middle+</pre> (next double after middle) to <pre>b-</pre> (
	 *     previous double before b)</li>
	 *   <li>a last one <pre>[a+, middle-]</pre></li>
	 * </ol>  
	 *   
	 *   Doing this, we miss some reals, but we are interested in the floating-point values
	 *   (not the ones in the Reals) that can lead the program body to violate the specification.
	 *   
	 *   Be aware that values that are not numbers (infinity, NaN) in domains are not properly
	 *   handled.
	 *   
	 * @param box The current box to be cut. It is modified by this method.
	 * @param cutDomain The domain to be partitioned. 
	 *                  Should contain at least two distinct floats.
	 *                  
	 * @return An ordered list of boxes partitioning the given box.
	 */
	Collection<DomainBox> partition5(DomainBox box, VariableDomain cutDomain) {
		VariableDomain newDomain;
		DomainBox newBox;
		ArrayList<DomainBox> boxes = new ArrayList<DomainBox>(5);		
		Variable cutVar = cutDomain.variable();
		String cutVarName = cutDomain.name();
		
		//The domain contains at least two distinct floats (its bounds)
		double min = cutDomain.minValue().doubleValue();
		double max = cutDomain.maxValue().doubleValue();
		double minNext = Math.nextUp(min);
		double maxPrevious = Math.nextAfter(max, Double.NEGATIVE_INFINITY);
		
		
		//We add the boxes with the degenerated domains last so that they will be the first 
		//ones poped from the stack of boxes (in the order : max, min, and middle).
		if (minNext <= maxPrevious) {
			double middle;
			if (minNext == maxPrevious) { //Domain contains only three floats
				middle = minNext;
			}
			else { //Domain contains at least four distinct floats
				middle = (minNext + maxPrevious) / 2.0;
				double middlePrevious = Math.nextAfter(middle, Double.NEGATIVE_INFINITY);
				double middleNext = Math.nextUp(middle);

				//Trying to create a box with sub-domain [a+, middle-]
				if (minNext <= middlePrevious) {
					newDomain = new VariableDomain(cutVar, minNext, middlePrevious);
					newBox = new DomainBox();
					//This preserves the insertion order of box in newBox
					newBox.add(box);
					//Replace the value of the existing cut domain in newBox by the 
					//new smaller domain.
					newBox.remove(cutVarName);
					newBox.add(newDomain);
					//Add this new box to the list of returned boxes
					boxes.add(newBox);
				}
				
				//Trying to create a box with sub-domain [middle+, b-]
				if (middleNext <= maxPrevious) {
					newDomain = new VariableDomain(cutVar, middleNext, maxPrevious);
					newBox = new DomainBox();
					newBox.add(box);
					newBox.remove(cutVarName);
					newBox.add(newDomain);
					boxes.add(newBox);
				}
			}
			
			//Creating a box with degenerated domain [middle, middle]
			newDomain = new VariableDomain(cutVar, middle, middle);
			newBox = new DomainBox();
			//Copy the existing box while preserving insertion order
			newBox.add(box);
			//Replace the domain to be cut by a new smaller domain 
			//Removing it and reinserting it puts it last in the insertion order 
			//so that it will be the last one to be tried the next time we look for a 
			//domain to cut.
			//Insertion and removal are constant time (LinkedHashTable).
			newBox.remove(cutVarName);
			newBox.add(newDomain);
			boxes.add(newBox);

		}
				
		//And now the two degenerated domains corresponding to the domain's bounds
		newDomain = new VariableDomain(cutVar, min, min);
		newBox = new DomainBox();
		//Copy the existing box
		newBox.add(box);
		//Replace the domain to be cut by a new smaller domain 
		newBox.remove(cutVarName);
		newBox.add(newDomain);
		boxes.add(newBox);

		newDomain = new VariableDomain(cutVar, max, max);
		//re-use the existing box for the degenerated domain of the max value 
		box.remove(cutVarName);
		box.add(newDomain);
		boxes.add(box);
		
		return boxes;
	}


	/**
	 * @param box
	 * @param k should be >=1.
	 * @return
	 */
	Collection<DomainBox> kTests(DomainBox box, int k) {
		ArrayList<DomainBox> testCases = new ArrayList<DomainBox>(k);
		HashMap<VariableDomain, double[]> valueMap = new HashMap<VariableDomain, double[]>();
		double[] values;
		
		//System.out.println("KTests box=" + box);
		
		for (VariableDomain domain: box.getDomains()) {
			//As for now, an input variable domain is a domain associated with a parameter
			//whose name does not start with the special prefix "_CPBPV_" reserved to variables
			//only used in the JML specification
			if (domain.isParameter() && !domain.name().startsWith("_CPBPV_")) {
				//System.out.println("DOMAIN=" + domain.name());
				double min = domain.minValue().doubleValue();
				double max = domain.maxValue().doubleValue();
				double increment = (max - min) / (double)k;
				double nextVal = min;
				values = new double[k];
				
				int i = 0;
				do {
					values[i++] = nextVal;
					nextVal += increment;
				} while ((i < k) && (nextVal <= max));
		
				while (i < k) {
					values[i++] = max;
				}
				
				valueMap.put(domain, values);
			}
		}

		DomainBox newBox;
		VariableDomain oldDomain, newDomain;
		double value;
		for (int i=0; i<k; i++) {
			newBox = new DomainBox();
			newBox.add(box);
			for (Entry<VariableDomain, double[]> entry: valueMap.entrySet()) {
				oldDomain = entry.getKey();
				value = entry.getValue()[i];
				newDomain = new VariableDomain(oldDomain.variable(), value, value);
				newBox.add(newDomain);
			}
			testCases.add(newBox);
		}
		
		return testCases;
	}
	
	/**
	 * Iterates reducing variables' domains with GLPK on the specification then with
	 * Fplib (strong kB-consistency) on the program body until no domain is reduced anymore.
	 */
	public void kBFixPoint() {
		GlpkVarBlock glpkVarBlock = (GlpkVarBlock)realCSP.varBlock;
		DomainBox domains = new DomainBox();
		
		int nb_reduc;
		int nb_iteration = 0;
		do {
			nb_reduc = domains.reduceDomains(glpkFilterDomains(glpkSolver,	glpkVarBlock, domains));
			//System.out.println("Box (GLPK):\n" + domains);
			nb_reduc += domains.reduceDomains(fplibKBFilterDomains(fplibSolver, domains));
			//System.out.println("Box (Fplib):\n" + domains);
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("nb reductions = " + nb_reduc 
									+ " nb iterations = " + ++nb_iteration);
			}
		} while (nb_reduc > 0);
	}
	
	/**
	 * This method solves with GLPK the constraints from the specification part 
	 * several times changing the objective function so as to obtain the min and 
	 * max solution values for each variable of the problem.
	 * Extremum values are returned in a DomainBox object.
	 * 
	 * @return either:
	 * 	      	<ul>
	 * 				<li><pre>domains</pre> if there is no CSP;</li>
	 * 				<li><code>null</code> if the CSP has no feasible solution;</li>
	 * 				<li>a box with newly created domains if filtering was successful.</li>
	 * 			</ul>
	 */
	public DomainBox glpkFilterDomains(
			GlpkSolver glpkSolver, 
			GlpkVarBlock glpkVars, 
			DomainBox domains) 
	{
		//If there's no precondition and the post-condition could not be handled by GLPK,
		//we have nothing to filter!
		if (realCSP.constraintBlock().size() == 0) {
			return domains;
		}
		
		//Set the new variable domains for the GLPK solver
		realCSP.setDomains(domains);

		Double min, max;
		int[] ind = new int[1];
		double[] val = new double[1];		
		DomainBox newDomains = new DomainBox();
		boolean hasSolution = false;
		//Find min and max for each variable with GLPK
		//and create the concrete variables in Fplib
		for (Variable var: glpkVars) {
			int col = glpkVars.concreteVar(var);
			ind[0] = col;
			val[0] = 1.0;
			
			//Find min
			glpkSolver.setObjective(0, true, 1, ind, val);
			if (glpkSolver.next(realCSP.getStatus())) {
				//TODO: on pourrait aussi appeler glp_get_obj_value pour obtenir la valeur
				//de la fonction objectif (pourrait être légèrement moins coûteux selon 
				//Claude).
				min = glpkSolver.value(col);
				hasSolution = true;
			}
			else {
				if (!hasSolution) {	
					//The first calls to next() didn't allow us to determine if the csp has a solution
					//we'll find out by checking GLPK status through solutionFound()
					if (glpkSolver.solutionFound()) {
						hasSolution = true;	
					}
					else { //This CSP has no solution
						return null;
					}
				}
				//If we get up there, the CSP has an unbounded solution
				min = Double.NEGATIVE_INFINITY;
			}
		
			//Find max
			glpkSolver.setObjectiveDirection(false);
			if (glpkSolver.next(realCSP.getStatus())) {
				max = glpkSolver.value(col);
			}
			else {
				max = Double.POSITIVE_INFINITY;
			}
		
			//reset this objective coefficient
			val[0] = 0.0;
			glpkSolver.setObjective(0, true, 1, ind, val);
		
			//Add this variable's extrema to the list of extrema
			newDomains.add(new VariableDomain(var, min, max));
			//Set this variable's extrema as this variable's new bounds in GLPK
			//so that it can be used in computing the other variables' extrema
			glpkSolver.setVarBounds(col, 
					                (min != Double.NEGATIVE_INFINITY), 
					                min, 
					                (max != Double.POSITIVE_INFINITY), 
					                max);
		}		
		return newDomains;
	}
	
	/** 
	 * 
	 * @return A newly created DomainBox.
	 */
	public DomainBox fplibKBFilterDomains(FplibSolver fplibSolver, DomainBox domains) 
	{	
		floatCSP.setFplibDomains(domains);
		fplibSolver.kB(floatCSP.varBlock.size(), 0.01, 0);
		DomainBox newDomains = floatCSP.domainBox();
		return newDomains;
	}
	/** 
	 * 
	 * @return A newly created DomainBox.
	 */
	public DomainBox fplib2BFilterDomains(FplibSolver fplibSolver, DomainBox domains) 
	{	
		floatCSP.setFplibDomains(domains);
		floatCSP.getStatus().moreSolve();
		floatCSP.getStatus().setCurrentTime();
		if (fplibSolver.twoB(0.0)) {
			floatCSP.getStatus().setElapsedTime();
			return floatCSP.domainBox();
		}
		else {
			floatCSP.getStatus().setElapsedTime();
			floatCSP.getStatus().moreFail();
			return null;
		}
	}

	//TODO: save/restore mechanism is used when parsing the method body
	//      to keep the current set of constraints. As the constraints from 
	//      the specification won't change, we should not need to save/restore
	//      them in the floatCSP (=>suppress the calls to floatCSP methods).
	
	@Override
	protected void saveCSP() {
		realCSP.save();
		floatCSP.save();
	}

	@Override
	protected void restoreCSP() {
		realCSP.restore();
		floatCSP.restore();
	}

	@Override
	public boolean isFeasible() {
		return tryDecision(new LogicalLiteral(true));
	}

	@Override
	public boolean solve() {
		return solve(new Solution());
	}

}
