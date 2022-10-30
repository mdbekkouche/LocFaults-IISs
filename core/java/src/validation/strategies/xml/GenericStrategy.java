package validation.strategies.xml;

import java.util.ArrayList;
import java.util.HashMap;

import expression.Expression;
import expression.logical.LogicalExpression;
import expression.variables.ArrayElement;
import expression.variables.ArrayVariable;
import expression.variables.SymbolTable;
import expression.variables.Variable;
import expression.variables.Variable.Use;

import validation.solution.Solution;
import validation.system.xml.ValidationSystem;
import validation.system.xml.ValidationSystemXmlCallbacks;
import validation.util.LoopStatus;
import validation.util.Type;
import validation.visitor.EnsureCase;

/**
 * This strategy provides a solver independent skeleton for other strategies to 
 * specialize.
 * 
 * @author Olivier Ponsini
 *
 */
public class GenericStrategy implements ValidationSystemXmlCallbacks {
	/**
	 * This is the solver dependent validation system. 
	 */
	ValidationSystem vs;
	
	public GenericStrategy(String name) {
		vs = ValidationSystem.createXmlValidationSystem(name);
	}


	@Override
	public ArrayVariable addArrayVar(String n) {
		return vs.addArrayVar(n);
	}

	@Override
	public void addCNFPrecond(LogicalExpression expr) {
		vs.addCNFPrecond(expr);
	}

	@Override
	public void addDNFAssumption(LogicalExpression expr) {
		vs.addDNFAssumption(expr);
	}

	@Override
	public void addDecision(LogicalExpression e) {
		vs.addDecision(e);
	}

	@Override
	public ArrayVariable addNewArrayVar(String n, int l, Type t) {
		return vs.addNewArrayVar(n, l, t);
	}

	@Override
	public ArrayVariable addNewArrayVar(String n, int l, Type t, Use u) {
		return vs.addNewArrayVar(n, l, t, u);
	}

	@Override
	public Variable addNewVar(String n, Type t) {
		return vs.addNewVar(n, t);
	}

	@Override
	public Variable addNewVar(String n, Type t, Use u) {
		return vs.addNewVar(n, t, u);
	}

	@Override
	public void addPath() {
		vs.addPath();
	}

	@Override
	public void addPostcond(LogicalExpression pc) {
		vs.addPostcond(pc);
	}

	@Override
	public Variable addVar(String n) {
		return vs.addVar(n);
	}

	@Override
	public int casePathNumber() {
		return vs.casePathNumber();
	}

	@Override
	public boolean containsArrayVar(String n) {
		return vs.containsArrayVar(n);
	}

	@Override
	public boolean containsVar(String n) {
		return vs.containsVar(n);
	}

	@Override
	public HashMap<String, ArrayElement> getArrayElt() {
		return vs.getArrayElt();
	}

	@Override
	public ArrayVariable getArrayVar(String n) {
		return vs.getArrayVar(n);
	}

	@Override
	public Variable getVar(String n) {
		return vs.getVar(n);
	}

	@Override
	public boolean isParsingMethodBody() {
		return vs.isParsingMethodBody();
	}

	@Override
	public boolean isParsingPrecond() {
		return vs.isParsingPrecond();
	}

	@Override
	public boolean isVoid() {
		return vs.isVoid();
	}

	@Override
	public LoopStatus loops() {
		return vs.loops();
	}

	@Override
	public LogicalExpression updatePostcond(Variable v, Expression expr) {
		return vs.updatePostcond(v, expr);
	}

	@Override
	public String printDecision() {
		return vs.printDecision();
	}

	@Override
	public String printStatus() {
		return vs.printStatus();
	}

	@Override
	public void displaySolution(Solution sol) {
		vs.displaySolution(sol);
	}	

	@Override
	public String printVariableValues() {
		return vs.printVariableValues();
	}

	@Override
	public void resetPath() {
		vs.resetPath();
	}

	@Override
	public Type returnType() {
		return vs.returnType();
	}

	@Override
	public void setParsingMethodBody() {
		vs.setParsingMethodBody();
	}

	@Override
	public void setParsingPrecond(boolean isParsingPrecond) {
		vs.setParsingPrecond(isParsingPrecond);
	}

	@Override
	public void setPostcond(EnsureCase s) {
		vs.setPostcond(s);
	}

	@Override
	public LogicalExpression getPostcond() {
		return vs.getPostcond();
	}

	@Override
	public void setReturnType(Type t) {
		vs.setReturnType(t);
	}

	@Override
	public boolean solve(Solution result) {
		return vs.solve(result);
	}

	@Override
	public LogicalExpression updatePostcond() {
		return vs.updatePostcond();
	}

	@Override
	public void addConstraint(LogicalExpression le) {
		vs.addConstraint(le);
	}

	@Override
	public void addVar(Variable v) {
		vs.addVar(v);		
	}

	@Override
	public boolean checkAssertion(LogicalExpression le, String message) {
		return vs.checkAssertion(le, message);
	}

	@Override
	public boolean checkPostcond(LogicalExpression le) {
		return vs.checkPostcond(le);
	}

	@Override
	public void restore() {
		vs.restore();
	}

	@Override
	public void save() {
		vs.save();
	}

	@Override
	public boolean tryDecision(LogicalExpression le) {
		return vs.tryDecision(le);
	}

	@Override
	public void validate() {
		vs.validate();
	}

	public Solution getLastPostcondSolution() {
		return vs.getLastPostcondSolution();
	}


	@Override
	public void reset(Solution sol) {
		System.err.println("Error: unimplemented reset method in Generic Strategy!");
		System.exit(-1);
	}


	@Override
	public ArrayList<?> getConstraints() {
		return vs.getConstraints();
	}


	@Override
	public SymbolTable copySymbolTable() {
		return vs.copySymbolTable();
	}


	@Override
	public HashMap<String, ?> copyVariableMap() {
		return vs.copyVariableMap();
	}


	@Override
	public void setConstraints(ArrayList<?> savedCtr) {
		vs.setConstraints(savedCtr);
	}


	@Override
	public void setSymbolTable(SymbolTable savedSymbolTable) {
		vs.setSymbolTable(savedSymbolTable); 		
	}


	@Override
	public void setVariableMap(HashMap<String, ?> savedVar) {
		vs.setVariableMap(savedVar);
	}

	@Override
	public boolean isFeasible() {
		return vs.isFeasible();
	}

	@Override
	public boolean solve() {
		return vs.solve();
	}

}
