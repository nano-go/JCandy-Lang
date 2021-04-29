package com.nano.candy.ast;
import com.nano.candy.ast.Expr.TernaryOperator;

public interface AstVisitor<S, E> {

	public void visit(Program node);
	
	public S visit(Stmt.ErrorStmt node);
	public S visit(Stmt.Import node);
	public S visit(Stmt.ImportList node);
	public S visit(Stmt.Return node);
	public S visit(Stmt.Continue node);
	public S visit(Stmt.Break node);
	public S visit(Stmt.While node);
	public S visit(Stmt.Assert node);
	public S visit(Stmt.Raise node);
	public S visit(Stmt.TryIntercept node);
	public S visit(Stmt.Interception node);
	public S visit(Stmt.If node);
	public S visit(Stmt.For node);
	public S visit(Stmt.ExprS node);
	public S visit(Stmt.VarDef node);
	public S visit(Stmt.FuncDef node);
	public S visit(Stmt.ClassDef node);
	public S visit(Stmt.Block node);

	public E visit(Expr.Lambda node);
	public E visit(Expr.CallFunc node);
	public E visit(Expr.Assign node);
	public E visit(Expr.IntegerLiteral node);
	public E visit(Expr.DoubleLiteral node);
	public E visit(Expr.StringLiteral node);
	public E visit(Expr.BooleanLiteral node);
	public E visit(Expr.NullLiteral node);
	public E visit(Expr.Array node);
	public E visit(Expr.Tuple node);
	public E visit(Expr.VarRef node);
	public E visit(Expr.Unary node);
	public E visit(Expr.TernaryOperator node);
	public E visit(Expr.Binary node);
	public E visit(Expr.GetItem node);
	public E visit(Expr.SetItem node);
	public E visit(Expr.GetAttr node);
	public E visit(Expr.SetAttr node);
	public E visit(Expr.Super node);
	public E visit(Expr.This node);
	
}
