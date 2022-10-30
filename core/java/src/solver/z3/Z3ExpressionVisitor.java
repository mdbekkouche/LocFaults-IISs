package solver.z3;

import java.util.ArrayList;
import java.util.List;

import solver.z3.Z3Solver;
import validation.util.OpCode;
import expression.ExpressionVisitor;
import expression.ParenExpression;
import expression.MethodCall;
import expression.logical.ArrayAssignment;
import expression.logical.Assignment;
import expression.logical.Comparator;
import expression.logical.JMLAllDiff;
import expression.logical.JMLExistExpression;
import expression.logical.JMLForAllExpression;
import expression.logical.JMLImpliesExpression;
import expression.logical.LogicalExpression;
import expression.logical.LogicalLiteral;
import expression.logical.NondetAssignment;
import expression.logical.NotExpression;
import expression.logical.AndExpression;
import expression.logical.OrExpression;
import expression.numeric.NumericExpression;
import expression.numeric.BinaryExpression;
import expression.numeric.DoubleLiteral;
import expression.numeric.FloatLiteral;
import expression.numeric.IntegerLiteral;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.ConcreteArrayVariable;
import expression.variables.JMLOld;
import expression.variables.Variable;

/**
 * Creates z3 constraints from CPBPV ones.
 * {@link z3Solver} exposes the interface to the concrete z3 SMT solver.
 * The visit may result in a unique concrete constraint (casted into a Long), or in 
 * multiple concrete constraints stored in an <code>ArrayList&lt;Long&gt;</code>.
 * 
 * @author Olivier Ponsini
 *
 */
public class Z3ExpressionVisitor implements ExpressionVisitor {
		
	private Z3Solver z3Solver;
	private Z3VarBlock varBlock;
	private Z3ArrayVarBlock arrayVarBlock;
	
	public Z3ExpressionVisitor(Z3Solver solver, Z3VarBlock vb, Z3ArrayVarBlock avb) {
		this.z3Solver = solver;
		this.varBlock = vb;
		this.arrayVarBlock = avb;
	}
	
	//--------------------------------------------------------------
	// Build new constraints.
	// The new constraints are returned as z3_ast C++ pointers (converted 
	// to 64 bit long integers).
	//--------------------------------------------------------------
	
	public Long visit(Assignment assignment) {
		long leftExpr = (Long)assignment.lhs().structAccept(this);
		long rightExpr = (Long)assignment.rhs().structAccept(this);
		return z3Solver.z3MkOp(OpCode.EQU.ordinal(), leftExpr, rightExpr);
	}

	public Long visit(AndExpression expression) {
		long leftExpr = (Long)expression.arg1().structAccept(this);
		long rightExpr = (Long)expression.arg2().structAccept(this);
		return z3Solver.z3MkOp(
					OpCode.AND.ordinal(), 
					leftExpr, 
					rightExpr);
	}

	public Long visit(OrExpression expression) {
		long leftExpr = (Long)expression.arg1().structAccept(this);
		long rightExpr = (Long)expression.arg2().structAccept(this);
		return z3Solver.z3MkOp(
					OpCode.OR.ordinal(), 
					leftExpr, 
					rightExpr);
	}

	public Long visit(JMLImpliesExpression expression) {
		long leftExpr = (Long)expression.arg1().structAccept(this);
		long rightExpr = (Long)expression.arg2().structAccept(this);
		return z3Solver.z3MkOp(
						OpCode.IMPLIES.ordinal(), 
						leftExpr, 
						rightExpr);
	}

	public Long visit(NotExpression expression) {
		long leftExpr = (Long)expression.arg1().structAccept(this);
		//Second argument to z3MkOp is "0", i.e. "Null" pointer
		return z3Solver.z3MkOp(OpCode.NOT.ordinal(), leftExpr, 0L);
	}

	public Long visit(Comparator expression) {
		long leftExpr = (Long)expression.arg1().structAccept(this);
		long rightExpr = (Long)expression.arg2().structAccept(this);
		return z3Solver.z3MkOp(
					expression.opCode().ordinal(), 
					leftExpr, 
					rightExpr);
	}
	

	/**
	 * This method visits an array assignment expression that will result 
	 * in several concrete constraints.
	 * 
	 * When an element at index <pre>i</pre> of an array <pre>A_k</pre> is assigned, 
	 * the array variable gets a new renaming number <pre>A_(k+1)</pre>. This new array 
	 * is such that for all <pre>j != i</pre>, <pre>A_(k+1)[j] = A_k[j]</pre>, and
	 * <pre>A_(k+1)[i]</pre> takes the value assigned.
	 * The constraints generated reflect the values of the elements of <pre>A_(k+1)</pre>.
	 * @param eltArrayAssign The array assignement expression to visit.
	 *                       It includes an array <code>t</code>, an index 
	 *                       <code>idx</code>, and a value <code>val</code> such that
	 *                       <code>t[idx]=val</code>.
	 * 
	 * @return A (maybe empty) set of concrete constraints of type 
	 *         <code>ArrayList&lt;IloConstraint&gt;</code>.
	 */
	public ArrayList<Long> visit(ArrayAssignment eltArrayAssign) {
		ArrayList<Long> constraints = new ArrayList<Long>();
		//eltArrayAssign contains both A_k and A_(k+1) arrays of concrete variables.
		ConcreteArrayVariable<Long> newArray = 
			arrayVarBlock.get(eltArrayAssign.array().name());
		    //arrayVarBlock.get(eltArrayAssign.array().name());
		ConcreteArrayVariable<Long> oldArray = 
			arrayVarBlock.get(eltArrayAssign.previousArray().name());
		NumericExpression idx = eltArrayAssign.index();
		Long concreteVal = (Long)eltArrayAssign.rhs().structAccept(this);
		System.out.println("array ass rhs: " + eltArrayAssign.rhs());
		
		//Partial evaluation optimization: constraints are simpler if we know the 
		//index value. Shouldn't we report an array out of bounds if case be ?
		if (idx.isConstant()) {
			for (int i = 0; i < newArray.length(); i++) {
				long concreteI = (Long)new IntegerLiteral(i).structAccept(this);
				//We assume idx holds an integer value: we rely on the Java compiler for that
				if (i == idx.constantNumber().intValue())
					constraints.add(
							z3Solver.z3MkOp(
									OpCode.EQU.ordinal(), 
									z3Solver.z3GetArrayVariable(
											newArray.concreteArray(), 
											concreteI), 
									concreteVal));
				else 
					constraints.add(
							z3Solver.z3MkOp(
									OpCode.EQU.ordinal(), 
									z3Solver.z3GetArrayVariable(
											newArray.concreteArray(), 
											concreteI), 
									z3Solver.z3GetArrayVariable(
											oldArray.concreteArray(), 
											concreteI)));
			}
		}
		else {
			long concreteIdx = (Long)idx.structAccept(this);
			for (int i = 0; i < newArray.length(); i++) {
				long concreteI = (Long)new IntegerLiteral(i).structAccept(this);
				constraints.add(
						z3Solver.z3MkIfThenElse(
								z3Solver.z3MkOp(
										OpCode.EQU.ordinal(),  
										concreteIdx,
										concreteI), 
								z3Solver.z3MkOp(
										OpCode.EQU.ordinal(),  
										z3Solver.z3GetArrayVariable(
												newArray.concreteArray(), 
												concreteI),  
										concreteVal),
								z3Solver.z3MkOp(
										OpCode.EQU.ordinal(),  
										z3Solver.z3GetArrayVariable(
												newArray.concreteArray(), 
												concreteI),  
										z3Solver.z3GetArrayVariable(
												oldArray.concreteArray(), 
												concreteI)
								)
						)
				);
			}
		}
		return constraints;
	}

	/**
	 * This method visits a JML <pre>AllDiff</pre> expression that will result 
	 * in a concrete <pre>distinct</pre> constraint.
	 * For the moment, it is only used on arrays. 
	 * 
	 * @param expression The JML <pre>AllDiff</pre> expression to visit. It wraps  
	 *                   the name of the array whose elements should all be different.
	 * @return A concrete constraint (pointer casted to Java long) modeling the 
	 *        JML <pre>AllDiff</pre> expression; or <code>null</code> if the 
	 *        solver could not build the constraint.
	 */	
	public Long visit(JMLAllDiff expression) {
		ConcreteArrayVariable<Long> array = arrayVarBlock.get(expression.array().name());
		long[] elts = new long[array.length()];
		
		for (int i=0; i<array.length(); i++) {
			long concreteI = (Long)new IntegerLiteral(i).structAccept(this);
			elts[i] = (Long)z3Solver.z3GetArrayVariable(array.concreteArray(),	concreteI);
		}
		
		return z3Solver.z3MkDistinct(elts);
	}

	/**
	 * This method visits a JML <pre>ForAll</pre> expression that will result 
	 * either in a conjunction of concrete constraints if the <pre>ForAll</pre> is enumerable
	 * or in a concrete <pre>ForAll</pre> constraint otherwise.
	 * 
	 * PRECOND: We suppose a single variable quantifier without inner quantifiers.
	 * @param jmlForAll The JML <pre>ForAll</pre> expression to visit. It includes 
	 *                   an index variable, possibly statically known min and max bounds for 
	 *                   the index and an expression parameterised by the index.
	 * 
	 * @return A concrete constraint modeling the conjunction of the substitutions in the 
	 *         <pre>ForAll</pre> expression of the index values to the index; or a concrete 
	 *         z3 <pre>ForAll</pre> constraint if the index values are not bounded.
	 */	
	public Long visit(JMLForAllExpression jmlForAll) {
		Long result = null;
		
		Variable varIndex = jmlForAll.index();
		LogicalExpression bound = jmlForAll.boundExpression();
		LogicalExpression cond = jmlForAll.condition();
		List<IntegerLiteral> varIndexValues = jmlForAll.enumeration();
		
		if(varIndexValues != null) {
			result = (Long)(cond.substitute(varIndex, varIndexValues.get(0))).structAccept(this);	
			for(int i = 1; i < varIndexValues.size(); i++) {
				result = z3Solver.z3MkOp(
						OpCode.AND.ordinal(), 
						(Long)cond.substitute(varIndex, varIndexValues.get(i)).structAccept(this), 
						result);
			}
		}
		else {  
			//This ForAll is not enumerable. It may already be under the canonical form 
			//(index; true; boundExpression => condition) if it comes from an assertion or 
			//a post-condition. But it is still in the JML form 
			//(index; boundExpression; condition), if it comes from a precondition.
			long[] boundedVars = { varBlock.concreteVar(varIndex.name())};
			LogicalLiteral trueLiteral = new LogicalLiteral(true);
			Long body;
			if (trueLiteral.equals(bound)) {
				body = (Long)cond.structAccept(this);
			}
			else {
				body = (Long)(new JMLImpliesExpression(bound, cond)).structAccept(this);				
			}
			result = z3Solver.z3MkForAll(boundedVars, body);
		}
		return result;
	}

	/**
	 * This method visits a JML <pre>Exist</pre> expression that will result 
	 * either in a disjunction of concrete constraints if the quantifier is enumerable or 
	 * in a z3 concrete <pre>Exist</pre> constraint otherwise.
	 *
	 * PRECOND: We suppose a single variable quantifier without inner quantifiers.
	 * @param jmlExist The JML <pre>Exist</pre> expression to visit. It includes an 
	 *                   index variable, possibly statically known min and max bounds for the index 
	 *                   and an expression parameterised by the index.
	 * 
	 * @return A concrete constraint modeling the
	 *         disjunction of the substitutions in the <pre>Exist</pre> expression of
	 *         the index values to the index; or a z3 concrete <pre>Exist</pre> constraint 
	 *         if the index values are not bounded.
	 */	
	public Long visit(JMLExistExpression jmlExist) {
		Long result = null;
		
		Variable varIndex = jmlExist.index();
		LogicalExpression bound = jmlExist.boundExpression();
		LogicalExpression cond = jmlExist.condition();
		List<IntegerLiteral> varIndexValues = jmlExist.enumeration();
		
		if(varIndexValues != null) {
			result = (Long)cond.substitute(varIndex, varIndexValues.get(0)).structAccept(this);			
			for(int i = 1; i < varIndexValues.size(); i++) {
				result = z3Solver.z3MkOp(
							OpCode.OR.ordinal(),
							(Long)cond.substitute(varIndex, varIndexValues.get(i)).structAccept(this), 
							result);
			}
		}
		else {
			//This Exist is not enumerable. It may already be under the canonical form 
			//(index; true; boundExpression AND condition) if it comes from an assertion or 
			//a post-condition. But it is still in the JML form 
			//(index; boundExpression; condition), if it comes from a precondition.
			long[] boundedVars = { varBlock.concreteVar(varIndex.name())};
			LogicalLiteral trueLiteral = new LogicalLiteral(true);
			Long body;
			if (trueLiteral.equals(bound)) {
				body = (Long)cond.structAccept(this);
			}
			else {
				body = (Long)(new AndExpression(bound, cond)).structAccept(this);				
			}
			result = z3Solver.z3MkExist(boundedVars, body);
		}
		return result;
	}

	
	//--------------------------------------------------------------
	// Transform expressions.
	// z3_ast pointers are handled as long int's wrapped into 
	// Longs. 
	// We use long int's for compatibility with 64-bit platform. 
	//--------------------------------------------------------------

	public Object visit(ParenExpression expression) {
		return expression.arg1().structAccept(this);
	}

	public Long visit(IntegerLiteral literal) {
		return z3Solver.z3MkIntCst(literal.constantNumber().intValue());
	}

	public Long visit(FloatLiteral literal) {
		return z3Solver.z3MkRealCst(literal.toString());
	}

	public Long visit(DoubleLiteral literal) {
		return z3Solver.z3MkRealCst(literal.toString());
	}

	public Long visit(LogicalLiteral literal) {
		return z3Solver.z3MkBoolCst(literal.constantBoolean());
	}

	public Long visit(Variable variable) {
		return varBlock.concreteVar(variable.name());
	}

	public Long visit(BinaryExpression expression) {
		long leftExpr = (Long)expression.arg1().structAccept(this);
		long rightExpr = (Long)expression.arg2().structAccept(this);
		return z3Solver.z3MkOp(
						expression.opCode().ordinal(), 
						leftExpr, 
						rightExpr);
	}

	/**
	 * This method visits an array variable, this will result 
	 * in a concrete array.
	 * @param array The array variable to visit.
	 * 
	 * @return The concrete array (of type <code>Z3_ast</code> casted into a long integer)
	 *         corresponding to the array variable <code>array</code>.
	 */
	public Long visit(ArrayVariable array) {
		return arrayVarBlock.get(array.name()).concreteArray();
	}

	/**
	 * This method visits an array element expression that will result 
	 * in a concrete expression.
	 * @param elt The array element expression to visit.
	 *            It includes an array <code>t</code>, and an index 
	 *            <code>idx</code>.
	 * 
	 * @return Either a concrete expression <code>select</code> (similar to the 
	 *         <code>element</code> constraint); or <code>null</code> if something went wrong.
	 */
	public Long visit(ArrayElement elt) {
		ConcreteArrayVariable<Long> array = arrayVarBlock.get(elt.array().name());
		NumericExpression idx = (NumericExpression) elt.index();
//		System.out.println("Z3visit " + array + " " + elt.array().name() + " " + idx);
//		System.out.println("concrete " + array.concreteArray());
		//Partial evaluation if the index value is known. 
		//Shouldn't we report an array out of bounds if case be ?
		if(idx.isConstant()) {
			//We assume idx holds an integer value: we rely on the Java compiler for that
			int idxValue = idx.constantNumber().intValue();
			if((0 <= idxValue) && (idxValue < array.length())) {
				return z3Solver.z3GetArrayVariable(
						array.concreteArray(), 
						(Long)new IntegerLiteral(idxValue).structAccept(this));
			}
		}
		//idx.constant() is false or the index is out of bound
		//we cannot simply consider an index out of bound as an error here, because the array access
		//may be part of a guarded constraint e.g. i!=-1 => t[i] = 0 where the guard would anyway
		//prevent the access even if 'i' evaluates to -1 in the current CSP. In this case, we should 
		//still build normally the constraint and let the CSP handle it. In the case of a real out of
		//bound access, we unfortunately won't report it ; but that's our design choice to ignore
		//array out of bounds for the moment.
		return z3Solver.z3GetArrayVariable(array.concreteArray(), (Long)idx.structAccept(this));
	}

	public Object visit(MethodCall mc) {
		System.err.println("Error : Method calls not allowed with Z3 !");
		System.err.println(mc);
		System.exit(-1);
		return null;
	}

	@Override
	public Object visit(NondetAssignment assignment) {
		System.err.println("Error : trying to convert an assignment to nondet into a constraint for Z3!");
		System.err.println(assignment);
		System.exit(-1);
		return null;
	}

	@Override
	public Object visit(JMLOld expression) {
		return expression.oldValue().structAccept(this);
	}
	
}