package validation.visitor;

import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import validation.Validation;
import validation.Validation.VerboseLevel;
import validation.solution.Solution;
import validation.system.xml.ValidationSystemXmlCallbacks;
import validation.util.ChildIterator;
import validation.util.Strategy;
import validation.util.Type;
import validation.util.LoopStatus;
import exception.AnalyzeException;
import expression.Expression;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.LogicalExpression;
import expression.logical.NotExpression;
import expression.numeric.IntegerLiteral;
import expression.numeric.NumericExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.Variable;

/** class to parse and validate Java blocks 
 * the stack exitNodes contains the stacks of nodes that must be executed after having
 * executed the current node (mandatory to be able to make choices
 * for "if else" statement
 * 
 * @author Olivier Ponsini
 * @author Hélène Collavizza
 */

//TODO : where to solve CSP ? : return statement or end of current path ?
//depends on the grammar : only one return statement or many ?
//return statement : an expression or only a variable ?

public class JavaBlockVisitAndValidate extends XMLVisitor {

	// validate a java block that is a set of statements
	static public void validateBlock(Node next, 
			                            ValidationSystemXmlCallbacks constSyst, 
			                            Solution result, 
			                            Stack<ChildIterator> continuation)
		throws AnalyzeException
	{
		if (!isIDSJavaBloc(next)) exception(1);
		ChildIterator c = new ChildIterator(next);
		validateInstructionList(c, constSyst, result, continuation);
	}

	// validate a list of statements
	static private void validateInstructionList(ChildIterator ch, 
												ValidationSystemXmlCallbacks constSyst,
												Solution result,
												Stack<ChildIterator> continuation)
	throws AnalyzeException {
		if (result.isEmpty()) {
			boolean continued=true;
			// continue if there is another child
			while (continued && ch.hasMoreChild()) {
				ChildIterator saveCh = (ChildIterator)ch.clone();
				Node n = ch.next();
				if (isSimple(n)) {
					validateSimpleInstruction(n, constSyst, result, continuation);
				}
				else {
					validateControlInstruction(saveCh, ch, n, constSyst, result, 
							                   continuation);
					continued=false;
				}
			}
		}
	}

	// validate one simple statement 
	// ie a statement with normal control flow : goes to next instruction
	static private void validateSimpleInstruction(Node n, 
												  ValidationSystemXmlCallbacks constSyst,
												  Solution result,
												  Stack<ChildIterator> continuation)
	throws AnalyzeException {
		int nodeType = identifier(n);
		switch (nodeType) {
		
		case VAR_DECL : 
			variableDeclaration(n, constSyst, result, continuation);
			break;

		case ARRAY_DECL : 
			arrayDeclaration(n, constSyst, result, continuation);
			break;

		case VAR_ASSIGN :
			variableAssignment(n, constSyst, result, continuation);
			break;

		case ARRAY_ASSIGN :
			arrayAssignment(n, constSyst, result, continuation);
			break;

		case ASSERT : 
			JMLAssert(n, constSyst);
			break;

		case ASSUME : 
			JMLAssume(n, constSyst);
			break;
		}
	}
	
	// validate one statement
	// case with control instruction
	static private void validateControlInstruction(ChildIterator saveCh,
												   ChildIterator ch,
												   Node n, 
												   ValidationSystemXmlCallbacks constSyst,  
												   Solution result, 
												   Stack<ChildIterator> continuation)
		throws AnalyzeException 
	{
//		Node n = ch.next();
		int nodeType = identifier(n);
		switch (nodeType) {


		case JAVA_RETURN   : 
			javaReturn(n, constSyst, result, continuation);
			break;

		case IF :  
		{
			ChildIterator child = new ChildIterator(n);
			Node next = child.next();

			// condition
			if (!isCond(next)) exception(3);
			// the parameter is the node of the condition and not
			// the condition itself to have a correct SSA naming
			// when backtraking
			Node cond = (new ChildIterator(next)).next();

			// if block
			next = child.next();
			if (!isIf(next))  exception(4);
			Node ifNode = (new ChildIterator(next)).next();

			// else block
			Node elseNode = null;
			next = child.next();
			if (isElse(next)) { 
				elseNode = (new ChildIterator(next)).next();
			}

			// push the node which is after the "if else" statement into continuation
			if (ch.hasMoreChild()) 
				continuation.push((ChildIterator)ch.clone());

			// validate the if else
			LogicalExpression co = LogicalExprVisitor.parse(cond, constSyst);
			if (co.isConstant())
				validateKnownIf(co, ifNode, elseNode, constSyst, result, continuation);
			else
				validateIf(co, ifNode, elseNode, constSyst, result, continuation);

			break;
		}
		case WHILE : { //while node 
			ChildIterator child = new ChildIterator(n);
			Node next = child.next();

			// condition
			if (!isWhileCond(next)) exception(8);
			
			// the parameter is the node of the condition and not
			// the condition itself to have a correct SSA naming
			// when backtraking
			Node cond = (new ChildIterator(next)).next();

			// while block
			next = child.next();
			if (!isWhileBlock(next))  exception(9);
			Node blockNode = (new ChildIterator(next)).next();

			// gets the loop identifier
			int loopNumber = new Integer(((Element) n).getAttribute("id")).intValue();

			//Gets the loop condition expression
			LogicalExpression loopCond = LogicalExprVisitor.parse(cond, constSyst);

			if (loopCond.isConstant()) {
				validateKnownWhile(loopNumber, (ChildIterator)ch.clone(), saveCh, cond, 
						           blockNode, constSyst, result, continuation);
			}
			else
				validateWhile(loopNumber, saveCh, ch, loopCond, blockNode, 
						      constSyst, result, continuation);

		}
		default : { 
		}
		}
	}



	// integer variable declaration
	//<!ELEMENT IDSJavaDeclVarStatment (%ExprNum;)>
	//<!ATTLIST IDSJavaDeclVarStatment type CDATA "int">
	//<!ATTLIST IDSJavaDeclVarStatment name CDATA #IMPLIED>
	private static void variableDeclaration(Node n,
											ValidationSystemXmlCallbacks constSyst,  
											Solution result, 
											Stack<ChildIterator> continuation) 
		throws AnalyzeException  
	{
		// manage SSA and initial value of the variable

		Variable v = constSyst.addNewVar(
				((Element) n).getAttribute("name"), 
				Type.parseType(((Element) n).getAttribute("type")));
		
		ChildIterator child = new ChildIterator(n);
		// set initial value if any
		if (child.hasMoreChild()) {
			Node next = child.next();
			NumericExpression expr = NumericExprVisitor.parse(next,constSyst);
			//By default a new Variable has its constant member set to false
			if (expr.isConstant())
				v.setConstant(expr.constantNumber());
			constSyst.addConstraint(new Assignment(v, expr));
//			constSyst.addConstraint(new Assignment(v,IntegerExpression.associate(constSyst.exprIntTable,expr)));
		}
		// if no initial value, default value is 0
		else {
			v.setConstant(0);
			LogicalExpression le = new Assignment(v, new IntegerLiteral(0));
			constSyst.addConstraint(le);
		}
	}

	// TODO : gerer le type correctement dnas la forme XML
	//<!ELEMENT IDSJavaDeclArrayStatment EMPTY>
	//<!ATTLIST IDSJavaDeclArrayStatment type CDATA "int">
	//<!ATTLIST IDSJavaDeclArrayStatment name CDATA #REQUIRED>
	//<!ATTLIST IDSJavaDeclArrayStatment length CDATA #REQUIRED>
	private static void arrayDeclaration(Node n,
										 ValidationSystemXmlCallbacks constSyst,  
										 Solution result, 
										 Stack<ChildIterator> continuation) 
		throws AnalyzeException  
	{
		// manage SSA and initial value of the variable
		String name = ((Element) n).getAttribute("name");
		int length = Integer.parseInt(((Element) n).getAttribute("length"));
		Type type = Type.parseType(((Element) n).getAttribute("type"));
		constSyst.addNewArrayVar(name, length, type);
	}


	// integer variable assignment  
	// TODO : traiter le type correctement
	private static void variableAssignment(Node n,
										   ValidationSystemXmlCallbacks constSyst,  
										   Solution result, 
										   Stack<ChildIterator> continuation) 
		throws AnalyzeException  
	{
		// parse the integer expression
		ChildIterator child = new ChildIterator(n);
		if (!child.hasMoreChild()) 
			exception(2);
		Node next = child.next();
		NumericExpression expr = NumericExprVisitor.parse(next, constSyst);
		String name = ((Element) n).getAttribute("name");
		if (!constSyst.containsVar(name)) 
			throw new AnalyzeException(name +" : unknown identifier");
		Variable v = constSyst.addVar(name);
		if (expr.isConstant())
			v.setConstant(expr.constantNumber());
		else
			v.resetDomain();
		constSyst.addConstraint(new Assignment(v, expr));
	}

	// integer array assignment  
	// TODO : traiter le type correctement
	private static void arrayAssignment(Node n, 
										ValidationSystemXmlCallbacks constSyst, 
										Solution result, 
										Stack<ChildIterator> continuation) 
		throws AnalyzeException  
	{
		// get array variable
		String name = ((Element) n).getAttribute("name");
		if (!constSyst.containsArrayVar(name)) 
			throw new AnalyzeException(name +" : unknown identifier");
		ArrayVariable previousV = constSyst.getArrayVar(name);
		// parse the integer expression of index
		ChildIterator child = new ChildIterator(n);
		if (!child.hasMoreChild()) 
			exception(10);
		Node next = child.next();
		NumericExpression index = NumericExprVisitor.parse(next, constSyst);
		// parse the integer expression wich is assigned
		if (!child.hasMoreChild()) 
			exception(10);
		next = child.next();
		NumericExpression value = NumericExprVisitor.parse(next, constSyst);
		ArrayVariable v = constSyst.addArrayVar(name);
		constSyst.addConstraint(new ArrayAssignment(v, previousV, index, value));
		// added 4th april
		// to deal with constant values for BubleSort of Mantovani
		if (index.isConstant() && value.isConstant()) 
			setConstantArrayElement(index, value.constantNumber(), constSyst,v, previousV);
	}
	
	
	private static void setConstantArrayElement(NumericExpression index, 
			                                    Number value, 
			                                    ValidationSystemXmlCallbacks constSyst, 
			                                    ArrayVariable v, 
			                                    ArrayVariable previousV) 
	{
		// set the new array element with constant value
		String eltName = v.name()+"_"+index.constantNumber();
		ArrayElement elt;
		if (constSyst.getArrayElt().containsKey(eltName))
			elt = constSyst.getArrayElt().get(eltName);
		else {
			elt = new ArrayElement(v,index);
			constSyst.getArrayElt().put(eltName,elt);
		}
		elt.setConstantNumber(value);
		// the other indexes haven't change
		// they take value of previousV
		// need to set them as constants if previousT is constant
		for (int i=0; i<v.length(); i++) {
			//We assume index is an integer (we rely on Java compiler for that)
			if (i != index.constantNumber().intValue()) {
				String eltNameI = v.name()+"_"+i;
				ArrayElement eltI;
				if (constSyst.getArrayElt().containsKey(eltNameI)) {
					eltI = constSyst.getArrayElt().get(eltNameI);
				}
				else {
					eltI = new ArrayElement(v, new IntegerLiteral(i));
					constSyst.getArrayElt().put(eltNameI,eltI);
				}
				String prevNameI = previousV.name()+"_"+i;
				ArrayElement prevEltI = constSyst.getArrayElt().get(prevNameI);
				if ((prevEltI != null) && prevEltI.isConstant()) {
					eltI.setConstantNumber(prevEltI.constantNumber());
				}
			}
		}
	}
	
	//	 assertion
	// verify if the assertion is true 
	private static void JMLAssert(Node assertNode, ValidationSystemXmlCallbacks constSyst) 
		throws AnalyzeException  
	{
		String message = ((Element)assertNode).getAttribute("message");
		ChildIterator child = new ChildIterator(assertNode);
        LogicalExpression assertion = LogicalExprVisitor.parse(child.next(), constSyst);
        
        constSyst.save();
        boolean verified = constSyst.checkAssertion(assertion, message);
    	constSyst.restore();
       
         if (verified) {
         	//TODO: Here, we add the assertion except for the non enumerable forAll's it may contain.
        	//      We should maybe try to keep them too..
            constSyst.addDNFAssumption(assertion);       	
       }
	}	

	// assumption
	// Add the corresponding constraint 
	private static void JMLAssume(Node assumeNode, ValidationSystemXmlCallbacks constSyst) 
		throws AnalyzeException  
	{
		ChildIterator child = new ChildIterator(assumeNode);
        LogicalExpression assumption = LogicalExprVisitor.parse(child.next(), constSyst);
        
       	//TODO: Here, we add the assumption except for the non enumerable forAll's it may contain.
       	//      We should maybe try to keep them too..
        constSyst.addDNFAssumption(assumption);       	
	}	
	
	// java return 
	// TODO : parser avec exprint ou exprlogical selon le  type de la fonction
	// TODO voir où l'on doit générer l'exception
	// if (!isJavaReturn(n)) exception(6);
	//TODO: avoir la même variable pour JMLResult et JavaResult
	// TODO : renvoyer une expression quelconque et pas une variable
	private static void javaReturn(Node n,
			                       ValidationSystemXmlCallbacks constSyst,  
			                       Solution result, 
			                       Stack<ChildIterator> continuation) 
		throws AnalyzeException  
	{
		Expression expr;
		LogicalExpression updatedPostcond;
		
		constSyst.save();
		
		if (!constSyst.isVoid()) {
			// integer expression of the return statement
			ChildIterator child = new ChildIterator(n);
			Node next = child.next();
			if (constSyst.returnType() == Type.BOOL) {
				expr = LogicalExprVisitor.parse(next,constSyst);
			}
			else {
				expr = NumericExprVisitor.parse(next,constSyst);
			}
			// get SSA variable of the return statement
			String name = "JMLResult";
			if (!constSyst.containsVar(name)) { 
				throw new AnalyzeException("JMLResult: unknown identifier");
			}
			updatedPostcond = constSyst.updatePostcond(constSyst.getVar(name), expr);

			// add a constraint for linking JMLResult and result
			//constSyst.linkResult((IntegerVariable)expr);
		}
		else {
			// need to parse one more time the specification in
			// order to have the right SSA name
			updatedPostcond = constSyst.updatePostcond();
		}
		
		if (constSyst.checkPostcond(updatedPostcond)) {
			//TODO: result is used to stop exploring the branches of the current 
			//      Ensure case when empty, or exploring the current branch when
			//      stopped. Maybe an enumeration could be more explicit and would
			//      allow to get rid of getLastPostcondSolution() and 
			//      lastPostcondSolution
			result.copy(constSyst.getLastPostcondSolution());
		}

		result.stop();	
		constSyst.addPath();
		constSyst.restore();
//		result.reset();
	}
	
	
	// validate "if else" statement
	private static void validateKnownIf(LogicalExpression co, 
										Node ifNode, 
										Node elseNode,
										ValidationSystemXmlCallbacks constSyst,  
										Solution result, 
										Stack<ChildIterator> continuation) 
		throws AnalyzeException  
	{

		if (co.constantBoolean()) {
			takeDecision(co, constSyst);
			validateBlock(ifNode,constSyst,result,continuation);
		}
		else {
			// ATTENTION = ALIASING
			// utiliser d'autres variables pour faire la négation
			// negate the condition
			LogicalExpression notCo = Strategy.negate(co);
			takeDecision(notCo, constSyst);
			if (elseNode!=null)
				validateBlock(elseNode,constSyst,result,continuation);
		}
		// execute the exit nodes
		// savedIf is empty if end of program has been reached
		// result is stopped if decision co was impossible
		executeContinuation(continuation, constSyst, result);
		
	}


	// validate "if else" statement
	private static void validateIf(
			LogicalExpression co, 
			Node ifNode, 
			Node elseNode,
			ValidationSystemXmlCallbacks constSyst,  
			Solution result, 
			Stack<ChildIterator> continuation) 
		throws AnalyzeException  
	{
		// saving context to allow backtrak
		Stack<ChildIterator> saveIf = (Stack<ChildIterator>)continuation.clone();
		constSyst.save();

		// validate assuming cond is taken
		boolean condSat = takeBranch(co, ifNode, constSyst, result, saveIf);

		// execute the exit nodes
		// savedIf is empty if end of program has been reached
		// result is stopped if decision co was impossible
		executeContinuation(saveIf, constSyst, result);
		
		// restoring context
		constSyst.restore();
		Solution resultElse = new Solution();

		// if no error has been found into if part, validate else part
		if (result.isEmpty()) {
			constSyst.save();
			
			// ATTENTION = ALIASING
			// utiliser d'autres variables pour faire la négation
			// negate the condition
			LogicalExpression notCo = Strategy.negate(co);
			
			// co was impossible when validating if, so directly take the else
			if (!condSat) 
				takeElse(notCo, elseNode, constSyst, resultElse, continuation);
			else 
				takeBranch(notCo, elseNode, constSyst, resultElse, continuation);

			// execute the exit nodes
			executeContinuation(continuation, constSyst, resultElse);

			constSyst.restore();
			
			// Restoring result for backtrack
			result.copy(resultElse);
		}

	}
	
	// validate a else branch that corresponds to a logical condition
	private static boolean takeBranch(
			LogicalExpression co, 
			Node branch,
			ValidationSystemXmlCallbacks constSyst, 
			Solution result, 
			Stack<ChildIterator> continuation)
	throws AnalyzeException  
	{
		boolean hasBranch = (branch!=null);

		// try decision and add it if it is satisfiable
		boolean condSat = constSyst.tryDecision(co);

		if (condSat) {
			takeDecision(co, constSyst);
			if (hasBranch) {
				validateBlock(branch,constSyst,result,continuation);
			}
		}
		else {
			printImpossible(co, constSyst);
			//Do not execute continuation
			result.stop();
		}
		return condSat;
	}
		
	// PRECOND : cond is infeasible because bool or linear CSP was inconsistent
	// so take else part without verifying if !cond is possible
	private static void takeElse(LogicalExpression co,
								 Node elseNode,
								 ValidationSystemXmlCallbacks constSyst,
								 Solution result,
								 Stack<ChildIterator> continuation)
		throws AnalyzeException  
	{
		boolean isIfElse=(elseNode!=null);
		
		takeDecision(co, constSyst);
		
		if (isIfElse) 
			validateBlock(elseNode,constSyst,result,continuation);
	}

//	// to take a decision 
//	// if tryOK then the decision has already been added into booleanCSP or linearCSP
//	// else, need to add it in all CSPs
//	private static void takeDecision(boolean tryOK, LogicalExpression co, ValidationSystem constSyst) 
//		throws AnalyzeException  
//	{
//		constSyst.addDecision(co);
//		if (tryOK)
//			// add constraint only into completeCSP
//			constSyst.addDecisionConstraint(co); 
//		else {
//			constSyst.addConstraint(co);
//		}
//		//TODO: notifier l'IHM du chemin qui vient d'être pris
//		// ie : la décision c
//	}

	private static void takeDecision(LogicalExpression co, 
									 ValidationSystemXmlCallbacks constSyst) 
		throws AnalyzeException  
	{
		constSyst.addConstraint(co);
		constSyst.addDecision(co);
		//TODO: notifier l'IHM du chemin qui vient d'être pris
		// ie : la décision c
	}
	
	private static void printImpossible(LogicalExpression co,  
										ValidationSystemXmlCallbacks constSyst) 
	{
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Decision " + co + " is impossible on path " + constSyst.casePathNumber());
		}
	}


	// validate a while with identifier n
	// if it is the first time that this while is found, then make all possible unfoldings
	// else enter one more time into the loop
	private static void validateKnownWhile(
				int loopNumber,
				ChildIterator next,
				ChildIterator ch,
				Node cond, 
				Node block,
				ValidationSystemXmlCallbacks constSyst,  
				Solution result, 
				Stack<ChildIterator> continuation) 
		throws AnalyzeException  
	{
		// the loop is not being executed
		// first time entering into the loop
		if (!constSyst.loops().isCurrent(loopNumber)) {
//			System.err.println("Validating loop # " + loopNumber + " with known condition : " + LogicalExprVisitor.parse(cond, constSyst,false));
//			Stack<ChildIterator> continueWhile = (Stack<ChildIterator>)continuation.clone();
			constSyst.loops().start(loopNumber,constSyst.casePathNumber());
//			push the node which is after the "if else" statement into continuation
//			do it here because it must be pushed only once
			
			// besoin de connaitre le path si plusieurs chemins et boucles imbriquées 
			// avec if à l'intérieur (cf selectionSort)
			if (ch.hasMoreChild()&&constSyst.loops().getPath()==constSyst.casePathNumber()) {
//			if (ch.hasMoreChild()) 
				continuation.push((ChildIterator)next.clone());
//				System.out.println("path pushed " + (ChildIterator)next.clone());
			}
			else {
				constSyst.loops().setPath(constSyst.casePathNumber());
			}
//			continueWhile.push((ChildIterator)next.clone());
			// take the while
//			takeKnownWhile(ch,cond, block,constSyst,result,continueWhile);

			takeKnownWhile(ch, cond, block, constSyst, result, continuation);

		}
		else {
			// one more path through the loop
			takeKnownWhile(ch, cond, block, constSyst, result, continuation);
		}
	}
	
	private static void validateWhile(int loopNumber, 
									  ChildIterator curInstructionIter,
									  ChildIterator nextInstructionIter,
									  LogicalExpression loopCond, 
									  Node body,
									  ValidationSystemXmlCallbacks constSyst,
									  Solution result, 
									  Stack<ChildIterator> continuation) 
		throws AnalyzeException  
	{
		boolean canExit = false, canUnfold = false;
		LoopStatus loopStatus = constSyst.loops();
		//Loop is not yet being unfolded
		if (!loopStatus.isCurrent(loopNumber)) {
			loopStatus.start(loopNumber, constSyst.casePathNumber());
		}
		
		//We now have to explore two branches depending on the satisfiability
		//of the condition and of its negation. It is important here to begin with 
		//the negation of the condition because we could otherwise reach the max number 
		//of unfolding and ends the verification skipping this branch.
		
		//loop status, validation system and continuations may be modified if we can 
		//exit the loop
		loopStatus.save();
		constSyst.save();
		Stack<ChildIterator> savedContinuation = (Stack<ChildIterator>)continuation.clone();
		
		canExit = tryExitWhile(loopCond, loopStatus, nextInstructionIter, 
							  constSyst, continuation, result);

		loopStatus.restore();
		constSyst.restore();

		//Exit branch led to no error, try to enter the loop
		if(result.isEmpty()) {
			result.start();
			
			canUnfold = tryEnterWhile(loopCond, body, loopStatus, curInstructionIter,
					                  constSyst, savedContinuation, result);

			//Neither the condition, nor its negation is satisfiable
			//Is such a case possible ?
			if(!canExit && !canUnfold) {
				System.err.println("Error: condition of loop #" + loopNumber
						           + " is neither satisfiable nor unsatisfiable!"); 
				System.exit(3);
			
			}
		}
	}
	
	private static boolean tryExitWhile(LogicalExpression loopCond, 
			                            LoopStatus loopStatus,
			                            ChildIterator nextInstructionIter,
			                            ValidationSystemXmlCallbacks constSyst,
			                            Stack<ChildIterator> continuation,
			                            Solution result) 
		throws AnalyzeException  
{
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Trying to exit loop #" + loopStatus.current() 
				           + " at unfolding #" + loopStatus.currentUnfold());
		}

		//Check if we can exit the loop
		NotExpression exitCond = new NotExpression(loopCond);
		if (!constSyst.tryDecision(exitCond)) {
			//System has no solution, exiting the loop now is not possible
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Exiting is not possible.");
			}
			return false;
		}

		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Exiting is possible.");
		}

		//Linear system has a solution; no matter if the complete system has one,
		//we check the branch where exiting the loop now is possible
		takeDecision(exitCond, constSyst);
		
		//Ends iterating on this loop
		loopStatus.end();
		
		// push instructions following the while loop if there's any in the same block
		if(nextInstructionIter.hasMoreChild()) {
			continuation.push(nextInstructionIter);
		}

		//Go on with instructions following the loop
		executeContinuation(continuation, constSyst, result);
		
		return true;
	}
	
	private static boolean tryEnterWhile(LogicalExpression loopCond,
										 Node body,
                                         LoopStatus loopStatus,
                                         ChildIterator curInstructionIter,
                                         ValidationSystemXmlCallbacks constSyst,
                                         Stack<ChildIterator> continuation,
                                         Solution result) 
		throws AnalyzeException  
	{
		System.out.println("Trying to enter loop #" + loopStatus.current() 
		           		   + " at unfolding #" + loopStatus.currentUnfold());

		//Check if we can enter the loop
		if (!constSyst.tryDecision(loopCond)) {
			//System has no solution, entering the loop now is not possible
			if (VerboseLevel.TERSE.mayPrint()) {
				System.out.println("Entering is not possible.");
			}
			return false;
		} //Last unfolding reached
		else if (loopStatus.currentUnfold() >= Validation.maxUnfoldings) {
			System.err.println("Error: Maximum unfolding number (" 
			           + Validation.maxUnfoldings
			           + ") reached for loop number " + loopStatus.current()); 
			System.exit(2);
		}
		
		if (VerboseLevel.TERSE.mayPrint()) {
			System.out.println("Entering is possible. Unfolding...");
		}

		//Linear system has a solution; no matter if the complete system has one,
		//we check the branch where entering the loop now is possible
		takeDecision(loopCond, constSyst);
		
		//Update the loop status
		loopStatus.addUnfold();

		//push the while loop itself and instructions following it in the same block
		//to be able to enter the loop one more time if need be
		continuation.push(curInstructionIter);

		//Unfold = add the loop body instructions before the loop
		validateBlock(body, constSyst, result, continuation);

		//Go on with the while loop again
		executeContinuation(continuation, constSyst, result);
		
		return true;
	}
	
	// take a while whose decision is known
	private static void takeKnownWhile(ChildIterator ch, Node cond, Node block,
			ValidationSystemXmlCallbacks constSyst, Solution result, 
			Stack<ChildIterator> continuation) 
	throws AnalyzeException  
	{
		if (result.stopped()) 
			System.err.println("Result has been stopped!");
		else {
			// parsing condition with or without booleans
			// need to parse it for each unfolding because SSA names may have changed
			LogicalExpression co = LogicalExprVisitor.parse(cond, constSyst);

			// decision co is possible, it is possible to enter one more time into the loop
			if (co.constantBoolean()) {	
				if(constSyst.loops().currentUnfold() >= Validation.maxUnfoldings) {
						System.err.println("Error: Maximum unfolding number (" 
								           + Validation.maxUnfoldings
					                       + ") reached for loop number " 
					                       + constSyst.loops().current()); 
						System.exit(2);
				}
				// one more unfold
				constSyst.loops().addUnfold();
				// take the decision
				takeDecision(co, constSyst);

				// push while itself to enter one more time into the loop
				continuation.push((ChildIterator)ch.clone());
//				System.out.println("before " + continuation);
				// validate instruction block
				validateBlock(block, constSyst, result, continuation);
				// execute continuation to enter one more time into the loop
//				System.out.println("after " + continuation);
				executeContinuation(continuation, constSyst, result);
			}
			else {
				// end of the loop
//				System.out.println("End of loop " + constSyst.loops().current());
				constSyst.loops().end();
				executeContinuation(continuation, constSyst, result);
//				executeContinuation("sortie known while ",continuation, constSyst,result );
				}
			}
		}

	/** executes the exit nodes that have been stored into the stack 
	 * @throws ConstraintException 
	 * @throws AnalyzeException */
	private static void executeContinuation(Stack<ChildIterator> continuation, 
			                                ValidationSystemXmlCallbacks constSyst,
			                                Solution result) 
		throws AnalyzeException 
	{
		int i = 0;
		while (!continuation.empty() && !result.stopped()) {
			i++;
			ChildIterator n = (ChildIterator)continuation.pop().clone();
			validateInstructionList(n, constSyst, result, continuation);
		}
	}
	
	//----------------------------------------------------------
	// g�re les exceptions
	static protected void exception(int n)  throws AnalyzeException{
		String s="  In JavaBlock ";
		switch (n) {
		case 1: s="IDSJavaBloc  ";break;
		case 2: s="in java assignment, integer value ";break;
		case 3: s="in JavaIfStatement, condition ";break;
		case 4: s="in JavaIfStatement, if ";break;
		case 5: s="in JavaIfStatement, else ";break;
		case 6: s="IDSJavaReturnStatement ";break;
		case 7: s=" variable in JavaReturnStatement is ";break;
		case 8: s="in JavaWhileStatement, condition  ";break;
		case 9: s="in JavaWhileStatement, whileBlock  ";break;
		case 10: s="in array assignment,  integer value ";break;
		}
		throw new AnalyzeException(s + " expected");
	}


}
