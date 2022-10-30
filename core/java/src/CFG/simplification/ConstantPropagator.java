package CFG.simplification;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;

import validation.Validation;
import validation.util.OpCode;
import validation.util.Type;

import expression.Expression;
import expression.ExpressionVisitor;
import expression.MethodCall;
import expression.ParenExpression;
import expression.logical.AndExpression;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.Comparator;
import expression.logical.EqualExpression;
import expression.logical.InfEqualExpression;
import expression.logical.InfExpression;
import expression.logical.JMLAllDiff;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NondetAssignment;
import expression.logical.NotExpression;
import expression.logical.OrExpression;
import expression.logical.SupEqualExpression;
import expression.logical.SupExpression;
import expression.numeric.BinaryExpression;
import expression.numeric.DivExpression;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.numeric.MinusExpression;
import expression.numeric.NumericExpression;
import expression.numeric.PlusExpression;
import expression.numeric.TimeExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;

import CFG.AssertEndWhile;
import CFG.AssertNode;
import CFG.BlockNode;
import CFG.CFG;
import CFG.CFGNode;
import CFG.CFGVisitException;
import CFG.CFGVisitor;
import CFG.ConditionalNode;
import CFG.EnsuresNode;
import CFG.FunctionCallNode;
import CFG.IfNode;
import CFG.RequiresNode;
import CFG.RootNode;
import CFG.SetOfCFG;
import CFG.WhileNode;
import CFG.OnTheFlyWhileNode;

/**
 * Propagates constants in a set of CFG. It also replaces variables that are aliases of another variable 
 * (e.g. if v1 = v2, v1 is replaced by v2 in all expressions). 
 * 
 * On ne propage pas les constantes éventuellement fixées dans la pré-condition... (A faire)  
 * @author Olivier
 *
 */
public class ConstantPropagator implements CFGVisitor, ExpressionVisitor {
	
	private enum Side {
		LEFT,
		RIGHT,
		NONE;
	}
		
	private Stack<Side> junctionFatherToRemove;

	private Stack<HashMap<Variable, Expression>> savedMaps;
	
	private HashSet<CFGNode> marked;
	
	private HashMap<Variable, Expression> constants;
	
	private boolean skipJunction;
	
	public int nbAssignments;
	
	public int nbNodes;
	
	public ConstantPropagator() {
		junctionFatherToRemove = new Stack<Side>();
		savedMaps = new Stack<HashMap<Variable, Expression>>();
		marked = new HashSet<CFGNode>();
		constants = new HashMap<Variable, Expression>();
		nbAssignments = 0;
		nbNodes = 0;
		skipJunction = false;
	}

	private void propagate(CFG cfg) throws IloException {
		try {			
			cfg.firstNode().accept(this);
		} catch (CFGVisitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void propagate(SetOfCFG program) throws IloException {
		visitAssignments(program.getFieldDeclaration().getBlock());
		propagate(program.getMethod(Validation.pgmMethodName));
	}

	
	/**
	 * Reset propagator internal state. In particular, clears any variable - value mapping.
	 */
	public void init() {
		junctionFatherToRemove.clear();
		marked.clear();
		constants.clear();
		nbAssignments = 0;
		nbNodes = 0;
		skipJunction = false;
	}
	
	/**************************************************/
	/****             CFG VISITOR                 *****/
	/**************************************************/
	
	private boolean checkJunction(CFGNode n) {
		if (n.isJunction()) {
			if (skipJunction) {
				skipJunction = false;
				return true;
			}
			else {
				Side fatherToRemove = this.junctionFatherToRemove.pop();
				if (fatherToRemove == Side.LEFT) {
					n.setLeftFather(n.getRightFather());
					n.setRightFather(null);
					return true;
				}
				else if (fatherToRemove == Side.RIGHT) {
					n.setRightFather(null);
					return true;
				}
				else { //fatherToRemove == Side.NONE
					if (marked.add(n)) { //First visit of the junction node
						this.junctionFatherToRemove.push(Side.NONE);
						savedMaps.push((HashMap<Variable, Expression>)constants.clone());
						return false;
					}
					else { //Second (and last) visit of this junction node
						marked.remove(n);
						constants = mergeBranchesConstants(constants, savedMaps.pop());
						return true;
					}
				}
			}
		}
		else {
			return true;
		}
	}
	
	@Override
	public void visit(RootNode n) throws CFGVisitException, IloException {
		CFGNode firstNode = n.getLeft();
		if (firstNode != null)
			firstNode.accept(this);
		else
			System.err.println("Error (ConstantPropagator): empty CFG!");
	}

	public void visit(EnsuresNode n) throws CFGVisitException {
		if (checkJunction(n)) {
			nbNodes++;
			n.setCondition((Expression)n.getCondition().structAccept(this));
		}
		//End of CFG visit
	}

	public void visit(RequiresNode n) throws CFGVisitException, IloException {
		nbNodes++;
		n.setCondition((Expression)n.getCondition().structAccept(this));
		// Cannot be a junction node
		n.getLeft().accept(this);
	}
 

	public void visit(AssertNode n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			nbNodes++;
			n.setCondition((Expression)n.getCondition().structAccept(this));	
			n.getLeft().accept(this);
		}
	}
	

	public void visit(AssertEndWhile n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			nbNodes++;
			n.setCondition((Expression)n.getCondition().structAccept(this));
			n.getLeft().accept(this);
		}
	}

	private void visitBranchingNode(ConditionalNode branchingNode) throws CFGVisitException, IloException {
		if (checkJunction(branchingNode)) {
			LogicalExpression cond = (LogicalExpression)branchingNode.getCondition().structAccept(this);
			if (cond.isConstant()) {
				CFGNode nodeToKeep;
				if (cond.constantBoolean()) {
					// Keep Then branch only
					this.junctionFatherToRemove.push(Side.RIGHT);
					nodeToKeep = branchingNode.getLeft();
				}
				else {
					//Keep Else branch only
					this.junctionFatherToRemove.push(Side.LEFT);
					nodeToKeep = branchingNode.getRight();
				}
			
				CFGNode branchingNodeLeftFather = branchingNode.getLeftFather();
				nodeToKeep.setLeftFather(branchingNodeLeftFather);
				if (branchingNode.hasRightFather()) {
					CFGNode branchingNodeRightFather = branchingNode.getRightFather();
					nodeToKeep.setRightFather(branchingNodeRightFather);
					branchingNodeLeftFather.setLeft(nodeToKeep);
					branchingNodeRightFather.setLeft(nodeToKeep);
					// Warning: if the branching node was also a junction node, 
					// nodeToKeep is now a junction node too and we cannot just call
					// nodeToKeep.accept(this) because it would apply checkJunction 
					// on the same junction node as it was the first time it is handled!
					// So we take care here that the junction will be handled as a junction 
					// already seen with nothing to do for it.
					skipJunction = true;
				}
				else {
					if (branchingNodeLeftFather.getLeft() == branchingNode) {
						branchingNodeLeftFather.setLeft(nodeToKeep);
					}
					else {
						branchingNodeLeftFather.setRight(nodeToKeep);
					}
				}
				nodeToKeep.accept(this);
			}
			else {
				nbNodes++;
				this.junctionFatherToRemove.push(Side.NONE);
				branchingNode.setCondition(cond);
				HashMap<Variable, Expression> savedConstants = (HashMap<Variable, Expression>)constants.clone();
				branchingNode.getLeft().accept(this);
				constants = savedConstants;
				branchingNode.getRight().accept(this);
			}
		}
	}
	
	private HashMap<Variable, Expression> mergeBranchesConstants(HashMap<Variable, Expression> ifConstants, 
																 HashMap<Variable, Expression> elseConstants) 
	{
		Iterator<Variable> keys = ifConstants.keySet().iterator();
		Variable v;
		Expression ifCst, elseCst;
		while (keys.hasNext()) {
			v = keys.next();
			elseCst = elseConstants.get(v);
			if (elseCst == null) {
				keys.remove();
			}
			else {
				ifCst = ifConstants.get(v);
				if (ifCst instanceof Variable || elseCst instanceof Variable) {
					if (!ifCst.equals(elseCst))
						keys.remove();
				}
				else {
				if (v.type() == Type.BOOL) { // v is a LogicalExpression
					if (((LogicalExpression)ifCst).constantBoolean() != ((LogicalExpression)elseCst).constantBoolean())
					{
						keys.remove();
					}
				} // v is a NumericExpression
				else if (((NumericExpression)ifCst).constantNumber().doubleValue() 
						 != 
						 ((NumericExpression)elseCst).constantNumber().doubleValue())
				{
					keys.remove();
				}
			}
			}
		}
		return ifConstants;
	}
	
	public void visit(IfNode n) throws CFGVisitException, IloException {
		visitBranchingNode(n);
	}

	// WhileNode have been unfolded, they act like IfNode
	public void visit(WhileNode n) throws CFGVisitException, IloException {
		visitBranchingNode(n);
	}
		
	private void visitAssignments(ArrayList<Assignment> block){
		Variable lhs;
		Expression rhs;
		NumericExpression index;
		Assignment ass;
		ArrayAssignment arrayAss;
		// the set of assignments is treated from the first one to the last one
		// to correctly handle SSA renaming
		ListIterator<Assignment> blockItr = block.listIterator();
		while (blockItr.hasNext()) {
			ass = blockItr.next();
			// If we are doing a copy of arrays, we don't propagate anything yet
			if (!(ass.rhs() instanceof ArrayVariable)) {
				rhs = ass.rhs();
				if (rhs != null) { //not a nondet assignment
					blockItr.remove();
					// Propagate constants and simplify rhs if possible
					rhs = (Expression)rhs.structAccept(this);
					if (ass instanceof ArrayAssignment) {
						arrayAss = (ArrayAssignment)ass;
						index = (NumericExpression)arrayAss.index().structAccept(this);
						blockItr.add(new ArrayAssignment(arrayAss.array(), arrayAss.previousArray(), index, rhs, arrayAss.startLine()));
					}	
					else { // scalar assignment
						lhs = ass.lhs();
						if (rhs.isConstant() || (rhs instanceof Variable)) {
							constants.put(lhs, rhs);
						}
						blockItr.add(new Assignment(lhs, rhs, ass.startLine()));
					}
				}
			}
		}
		nbAssignments += block.size();
	}

	public void visit(BlockNode n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			nbNodes++;
			visitAssignments(n.getBlock());
			n.getLeft().accept(this);
		}
	}	
	
	public void visit(FunctionCallNode n) throws CFGVisitException, IloException {
		if (checkJunction(n)) {
			nbNodes++;
			// add variables in parameter passing assignments 
			visitAssignments(n.getParameterPassing().getBlock());
			// add global variables which are used in the function
			visitAssignments(n.getLocalFromGlobal().getBlock());
		
			//TODO: We should propagate constants in function calls too, although it may 
			//     	be less efficient than when functions are inlined
		
			n.getLeft().accept(this);
		}
	}
	
	@Override
	public void visit(OnTheFlyWhileNode whileNode) {
		// TODO Auto-generated method stub
		
	}


	/**************************************************/
	/****        EXPRESSION VISITOR               *****/
	/**************************************************/


	@Override
	public Object visit(ParenExpression expression) {
		return expression.arg1.structAccept(this);
	}


	@Override
	public Object visit(Variable variable) {
		Expression cst = constants.get(variable);
		if (cst == null) {
			return variable;
		}
		else {
			return cst;
		}
	}

	
	@Override
	public Object visit(ArrayElement elt) {
		// Simplifying index expression
		NumericExpression index = (NumericExpression)elt.index().structAccept(this);
		return new ArrayElement(elt.array(), index);
	}

	
	@Override
	public Object visit(IntegerLiteral literal) {
		return literal;
	}



	@Override
	public Object visit(FloatLiteral literal) {
		return literal;
	}



	@Override
	public Object visit(DoubleLiteral literal) {
		return literal;
	}



	@Override
	public Object visit(LogicalLiteral literal) {
		return literal;
	}


	@Override
	public Object visit(BinaryExpression expression) {
		NumericExpression arg1 = (NumericExpression)expression.arg1().structAccept(this);
		NumericExpression arg2 = (NumericExpression)expression.arg2().structAccept(this);
		switch (expression.opCode()) {
		case ADD:
			return visitAdd(arg1, arg2, expression.type());
		case SUB:
			return visitSub(arg1, arg2, expression.type());
		case MUL:
			return visitMul(arg1, arg2, expression.type());
		case DIV:
			return visitDiv(arg1, arg2, expression.type());
		default:
			System.err.println("Error (ConstantPropagator): unknown binary expression: " + expression);
			return expression;
		}
	}

	private NumericExpression visitAdd(NumericExpression arg1, NumericExpression arg2, Type type) {
		if (arg1.isConstant()) {
			if (arg1.constantNumber().doubleValue() == 0) {
				return arg2;
			}
			else if (arg2.isConstant()) {
				switch(type) {
				case INT:
					return new IntegerLiteral(
							arg1.constantNumber().intValue() + arg2.constantNumber().intValue());
				case FLOAT:
					return new FloatLiteral(
							arg1.constantNumber().floatValue() + arg2.constantNumber().floatValue());
				case DOUBLE:	
					return new DoubleLiteral(
							arg1.constantNumber().doubleValue() + arg2.constantNumber().doubleValue());
				default:
					System.err.println("Error (ConstantPropagator - Plus): wrong type (" + type + ")!");
				}
			}
		}
		else if (arg2.isConstant() && arg2.constantNumber().doubleValue() == 0) {
			return arg1;
		}
		return new PlusExpression(arg1, arg2);
	}
	
	private NumericExpression visitSub(NumericExpression arg1, NumericExpression arg2, Type type) {
		if (arg2.isConstant()) {
			if (arg2.constantNumber().doubleValue() == 0) {
				return arg1;
			}
			else if (arg1.isConstant()) {
				switch(type) {
				case INT:
					return new IntegerLiteral(
							arg1.constantNumber().intValue() - arg2.constantNumber().intValue());
				case FLOAT:
					return new FloatLiteral(
							arg1.constantNumber().floatValue() - arg2.constantNumber().floatValue());
				case DOUBLE:	
					return new DoubleLiteral(
							arg1.constantNumber().doubleValue() - arg2.constantNumber().doubleValue());
				default:
					System.err.println("Error (ConstantPropagator - Minus): wrong type (" + type + ")!");
				}
			}
		}
		return new MinusExpression(arg1, arg2);
	}

	private NumericExpression visitMul(NumericExpression arg1, NumericExpression arg2, Type type) {
		if (arg1.isConstant()) {
			double arg1Val = arg1.constantNumber().doubleValue();
			if (arg1Val == 0) {
				return new IntegerLiteral(0);
			}
			else if (arg1Val == 1) {
				return arg2;
			}
			else if (arg2.isConstant()) {
				switch(type) {
				case INT:
					return new IntegerLiteral(
							arg1.constantNumber().intValue() * arg2.constantNumber().intValue());
				case FLOAT:
					return new FloatLiteral(
							arg1.constantNumber().floatValue() * arg2.constantNumber().floatValue());
				case DOUBLE:	
					return new DoubleLiteral(
							arg1.constantNumber().doubleValue() * arg2.constantNumber().doubleValue());
				default:
					System.err.println("Error (ConstantPropagator - Time): wrong type (" + type + ")!");
				}
			}
		}
		else if (arg2.isConstant()) {
			double arg2Val = arg2.constantNumber().doubleValue();
			if (arg2Val == 0) {
				return new IntegerLiteral(0);
			}
			else if (arg2Val == 1) {
				return arg1;
			}
		}
		return new TimeExpression(arg1, arg2);
	}

	private NumericExpression visitDiv(NumericExpression num, NumericExpression denom, Type type) {
		if (denom.isConstant()) {
			if (denom.constantNumber().doubleValue() == 1) {
				return num;
			}
			else if (num.isConstant()) {
				switch(type) {
				case INT:
					return new IntegerLiteral(
							num.constantNumber().intValue()	/ denom.constantNumber().intValue());
				case FLOAT:
					return new FloatLiteral(
							num.constantNumber().floatValue() / denom.constantNumber().floatValue());
				case DOUBLE:
					return new DoubleLiteral(
							num.constantNumber().doubleValue() / denom.constantNumber().doubleValue());
				default:
					System.err.println("Error (ConstantPropagator - Div): wrong type (" + type + ")!");
				}
			}
		}
		return new DivExpression(num, denom);
	}


	@Override
	public Object visit(Comparator expression) {
		Expression arg1 = (Expression)expression.arg1().structAccept(this);
		Expression arg2 = (Expression)expression.arg2().structAccept(this);
		Expression result = visitComparator(arg1, arg2, expression.opCode());
		if (result == null)
			return expression;
		else
			return result;
	}
	
	private LogicalExpression visitComparator(Expression arg1, Expression arg2, OpCode cmp) {
		if (arg1.isConstant() && arg2.isConstant()) {
			// We assume comparisons are between NumericExpression or (exclusive) LogicalExpression
			if (arg1 instanceof NumericExpression) {
				double val1 = ((NumericExpression)arg1).constantNumber().doubleValue();
				double val2 = ((NumericExpression)arg2).constantNumber().doubleValue();
				switch (cmp) {
				case EQU:
					return new LogicalLiteral(val1 == val2);
				case LT:
					return new LogicalLiteral(val1 < val2);
				case LEQ:
					return new LogicalLiteral(val1 <= val2);
				case GT:
					return new LogicalLiteral(val1 > val2);
				case GEQ:
					return new LogicalLiteral(val1 >= val2);
				}
			}
			else { //It is a LogicalExpression, only equality is allowed
				boolean val1 = ((LogicalExpression)arg1).constantBoolean();
				boolean val2 = ((LogicalExpression)arg2).constantBoolean();
				if (cmp == OpCode.EQU) {
					return new LogicalLiteral(val1 == val2);
				}
			}
		}
		
		switch (cmp) {
		case EQU:
			return new EqualExpression(arg1, arg2);
		case LT:
			return new InfExpression(arg1, arg2);
		case LEQ:
			return new InfEqualExpression(arg1, arg2);
		case GT:
			return new SupExpression(arg1, arg2);
		case GEQ:
			return new SupEqualExpression(arg1, arg2);		
		default:
			System.err.println("Error (ConstantPropagator): Not a valid comparison expression (" + arg1 + " " + cmp + " " + arg2 + ")!");
			return null;
		}

	}

	@Override
	public Object visit(AndExpression expression) {
		LogicalExpression arg1 = (LogicalExpression)expression.arg1().structAccept(this);
		LogicalExpression arg2 = (LogicalExpression)expression.arg2().structAccept(this);
		if (arg1.isConstant()) {
			if (arg1.constantBoolean()) 
				return arg2;
			else 
				return arg1;
		}
		else if (arg2.isConstant()) {
			if (arg2.constantBoolean()) 
				return arg1;
			else 
				return arg2;
		}
		return new AndExpression(arg1, arg2);
	}



	@Override
	public Object visit(OrExpression expression) {
		LogicalExpression arg1 = (LogicalExpression)expression.arg1().structAccept(this);
		LogicalExpression arg2 = (LogicalExpression)expression.arg2().structAccept(this);
		if (arg1.isConstant()) {
			if (arg1.constantBoolean())
				return arg1;
			else 
				return arg2;
		}
		else if (arg2.isConstant()) {
			if (arg2.constantBoolean()) 
				return arg2;
			else 
				return arg1;
		}
		return new OrExpression(arg1, arg2);
	}

	@Override
	public Object visit(NotExpression expression) {
		LogicalExpression arg1 = (LogicalExpression)expression.arg1().structAccept(this);
		if (arg1.isConstant()) {
			if (arg1.constantBoolean()) 
				return new LogicalLiteral(false);
			else
				return new LogicalLiteral(true);
		}
		return new NotExpression(arg1);
	}


	@Override
	public Object visit(JMLImpliesExpression expression) {
		LogicalExpression arg1 = (LogicalExpression)expression.arg1().structAccept(this);
		LogicalExpression arg2 = (LogicalExpression)expression.arg2().structAccept(this);
		if (arg1.isConstant()) {
			if (arg1.constantBoolean())
				return arg2;
			else 
				return new LogicalLiteral(true);
		}
		else if (arg2.isConstant()) {
			if (arg2.constantBoolean()) 
				return arg2;
			else 
				return new NotExpression(arg1);
		}
		return new JMLImpliesExpression(arg1, arg2);
	}

	@Override
	public Object visit(JMLForAllExpression forAll) {
		LogicalExpression bound = (LogicalExpression)forAll.boundExpression().structAccept(this);
		LogicalExpression condition = (LogicalExpression) forAll.condition().structAccept(this);
		return new JMLForAllExpression(forAll.index(), bound, condition);
	}

	@Override
	public Object visit(JMLExistExpression exist) {
		LogicalExpression bound = (LogicalExpression)exist.boundExpression().structAccept(this);
		LogicalExpression condition = (LogicalExpression) exist.condition().structAccept(this);
		return new JMLExistExpression(exist.index(), bound, condition);
	}



	@Override
	public Object visit(Assignment assignment) {
		// Assignments are visited by group when visiting blocks, this method should never be called
		return null;
	}


	@Override
	public Object visit(JMLAllDiff expression) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Object visit(NondetAssignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Object visit(ArrayAssignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ArrayVariable variable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MethodCall mc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(JMLOld expression) {
		return this.visit(expression.oldValue());
	}
}
