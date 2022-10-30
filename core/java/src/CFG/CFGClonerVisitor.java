package CFG;


/** 
 * class to visit a CFG and clone it
 * 
 * @author helen
 *
 */
public interface CFGClonerVisitor {
	
	public EnsuresNode visit(EnsuresNode n) throws CFGVisitException;
	
	public RequiresNode visit(RequiresNode n) throws CFGVisitException;
	
	public AssertNode visit(AssertNode n) throws CFGVisitException;
	
	public AssertEndWhile visit(AssertEndWhile n) throws CFGVisitException;
	
	public IfNode visit(IfNode n) throws CFGVisitException;
	
	public WhileNode visit(WhileNode n) throws CFGVisitException;
	
	public BlockNode visit(BlockNode n) throws CFGVisitException;

	public FunctionCallNode visit(FunctionCallNode n) throws CFGVisitException;

	public OnTheFlyWhileNode visit(OnTheFlyWhileNode whileNode) throws CFGVisitException;

}

