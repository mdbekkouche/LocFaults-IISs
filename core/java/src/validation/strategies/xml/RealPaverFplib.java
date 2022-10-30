package validation.strategies.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import java.util.ArrayList;

import solver.fplib.FplibSolver;
import solver.fplib.FplibValidationCSP;
import solver.realpaver.xml.RealPaverXmlCsp;
import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.system.VariableVarStore;
import validation.system.xml.SimpleArrayValidationSystem;
import validation.solution.Solution;
import validation.solution.ValidationStatus;
import validation.util.Type;
import expression.Expression;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.variables.DomainBox;
import expression.variables.Variable;
import expression.variables.VariableDomain;
import expression.variables.Variable.Use;

/**
 * This validation system manages a CSP on reals using <code>Real Paver</code> and 
 * a CSP on floats using <code>Fplib</code>. Both CSP are applied to a Java method body 
 * and we look for the error differenece between the method result on reals and the 
 * method result on floats.   
 *  
 * @author Olivier Ponsini
 *
 */
public class RealPaverFplib extends SimpleArrayValidationSystem {
	/**
	 * The CSP over reals.
	 */
	protected RealPaverXmlCsp realCsp; 
	/**
	 * The CSP over floats.
	 */
	protected FplibValidationCSP floatCsp;
	/**
	 * The solver over floats. 
	 * Convenient shortcut.
	 */
	private FplibSolver fplibSolver;

	/**
	 * Constructs a validation system with a CSP on rationals (GLPK) for 
	 * the specification and a CSP over floats (Fplib) for the method body.
	 * 
	 * @param name The name of the file containing the XML description of the Java 
	 *             program to be verified.
	 */
	public RealPaverFplib(String name) {
		super(name + " using GLPK (specification) + Fplib (method).");
		fplibSolver = (FplibSolver)floatCsp.cspSolver;
	}
	
	/** 
	 * Creates the CSPs over rationals and floats.
	 * This method is called by the super class.
	 */
	@Override
	protected void setCSP() {
		status = new ValidationStatus();
		realCsp = new RealPaverXmlCsp("Real Paver (real CSP)", this);
		floatCsp = new FplibValidationCSP("Fplib (float CSP)", this);
		status.addStatus(realCsp.getStatus());
		status.addStatus(floatCsp.getStatus());
	}
	
	@Override
	public String printStatus() {
		return status.toString();
	}

	@Override
	public Variable addNewVar(String n, Type t, Use u) {
		Variable v = var.addNewVar(n, t, u);
		floatCsp.addVar(v);
		realCsp.addVar(v);
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
		floatCsp.addVar(v);
		realCsp.addVar(v);
		return v;
	}
	
	// helen
	@Override
	public void addVar(Variable v) {
		floatCsp.addVar(v);
		realCsp.addVar(v);
	}

	@Override
	public void addConstraint(LogicalExpression c) {
		floatCsp.addConstraint(c);
		realCsp.addConstraint(c);
	}

	//Postcondition is ignored, but we add the assignment of return 
	//expression to JMLResult variable
	@Override
	public LogicalExpression updatePostcond(Variable v, Expression expr) {
		return new Assignment(v, expr);
	}

	@Override
	public void addPostcond(LogicalExpression pc) {
		System.err.println("Error: RealPaverFplib does not support assertions or void methods yet!");
		System.exit(-1);
	}

	@Override
	public void addPrecond(LogicalExpression c) {
		addConstraint(c);
	}
		
	@Override
	public boolean tryDecision(LogicalExpression c) {
		//We do not prune execution paths
		return true;
	}
	
	@Override
	public boolean checkPostcond(LogicalExpression le) {
		boolean foundSolution;
		
		addConstraint(le);

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

	@Override
	public boolean solve(Solution result) {
		boolean foundSolution;
		
		if (VerboseLevel.DEBUG.mayPrint()) {
			System.out.println("Real:");
			System.out.println(realCsp.constraintBlock().toString());
			System.out.println("Float:");
			System.out.println(floatCsp.constraintBlock().toString());	
		}

		floatCsp.startSearch();
		
		for (Variable v: (VariableVarStore)realCsp.varBlock) {
			v.setDomain((VariableDomain)null);
		}

		//kBFixPoint();
		foundSolution = filterAndCut(1e-2, result);		

		floatCsp.stopSearch();

		return foundSolution;
	}
	
	private double maxDifference(VariableDomain d1, VariableDomain d2) {
		System.out.println("d1=" + d1);
		System.out.println("d2=" + d2);
		double diff1 = Math.abs(d1.maxValue().doubleValue() - d2.minValue().doubleValue());
		double diff2 = Math.abs(d2.maxValue().doubleValue() - d1.minValue().doubleValue());
		return Math.max(diff1, diff2);
	}

	public boolean filterAndCut(double epsilon, Solution counterExample) {
		DomainBox box = new DomainBox();
		Stack<DomainBox> boxes = new Stack<DomainBox>();
		Collection<DomainBox> newBoxes;		
		DomainBox realBox, floatBox;

		//We manage a stack of boxes and the corresponding stack of solver's states
		//for Fplib
		boxes.push(box);
		fplibSolver.push();
		
		int i = 0;
		while (!boxes.empty()) {
			if (VerboseLevel.VERBOSE.mayPrint()) {
				System.out.println("Nb explored boxes = " + i++);
			}
			
			//Take a box and restore the corresponding Fplib solver's state
			box = boxes.pop();
			fplibSolver.pop();
			
			if (Validation.verboseLevel == VerboseLevel.DEBUG) {
				System.out.println("Box=" + box);
			}
			
			realBox = realFilter(box);
			
			if (!realBox.isEmpty()) { //CSP has a feasible solution over reals
												
				floatBox = fplib2BFilterDomains(box);
				
				if (floatBox != null) { //CSP was 2B consistent with Fplib

					if (Validation.verboseLevel == VerboseLevel.DEBUG) {
						System.out.println("Box after Fplib Filtering=" + floatBox);
					}

					double maxError = maxDifference(realBox.get("JMLResult_0"), 
													floatBox.get("JMLResult_0")); 
					if (maxError >= epsilon) {

						//Cut the current box in several sub-boxes
						box.reduceDomains(realBox);
						newBoxes = cutBoxAndIterate(box, 0);
					
						if (floatBox.get("JMLResult_0").isSingleton() || (newBoxes.size() == 1)) {
							//Current box could not be cut or float result is already a constant
							if (Validation.verboseLevel == VerboseLevel.DEBUG) {
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
						}
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
					System.out.println("No real Solution!");
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
	public DomainBox realFilter(DomainBox domains) 
	{		
		//Set the new variable domains for the real solver
		realCsp.setDomains(domains);
		realCsp.startSearch();
		realCsp.next();
		realCsp.stopSearch();
		return realCsp.domainBox();
	}
	
	/** 
	 * 
	 * @return A newly created DomainBox.
	 */
	public DomainBox fplibKBFilterDomains(DomainBox domains) 
	{	
		floatCsp.setFplibDomains(domains);
		fplibSolver.kB(floatCsp.varBlock.size(), 0.01, 0);
		DomainBox newDomains = floatCsp.domainBox();
		return newDomains;
	}
	/** 
	 * 
	 * @return A newly created DomainBox.
	 */
	public DomainBox fplib2BFilterDomains(DomainBox domains) 
	{	
		floatCsp.setFplibDomains(domains);
		floatCsp.getStatus().moreSolve();
		floatCsp.getStatus().setCurrentTime();
		if (fplibSolver.twoB(0.0)) {
			floatCsp.getStatus().setElapsedTime();
			return floatCsp.domainBox();
		}
		else {
			floatCsp.getStatus().setElapsedTime();
			floatCsp.getStatus().moreFail();
			return null;
		}
	}

	//TODO: save/restore mechanism is used when parsing the method body
	//      to keep the current set of constraints. As the constraints from 
	//      the specification won't change, we should not need to save/restore
	//      them in the floatCSP (=>suppress the calls to floatCSP methods).
	
	@Override
	protected void saveCSP() {
		realCsp.save();
		floatCsp.save();
	}

	@Override
	protected void restoreCSP() {
		realCsp.restore();
		floatCsp.restore();
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
