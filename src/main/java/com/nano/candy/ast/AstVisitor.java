package com.nano.candy.ast;

public interface AstVisitor<R> {

	public R visit(Program node);
	
	public R visit(Stmt.Return node);
	public R visit(Stmt.Continue node);
	public R visit(Stmt.Break node);
	public R visit(Stmt.While node);
	public R visit(Stmt.Assert node);
	public R visit(Stmt.If node);
	public R visit(Stmt.For node);
	public R visit(Stmt.ExprS node);
	public R visit(Stmt.VarDef node);
	public R visit(Stmt.FuncDef node);
	public R visit(Stmt.ClassDef node);
	public R visit(Stmt.Block node);

	public R visit(Expr.Lambda node);
	public R visit(Expr.CallFunc node);
	public R visit(Expr.Assign node);
	public R visit(Expr.IntegerLiteral node);
	public R visit(Expr.DoubleLiteral node);
	public R visit(Expr.StringLiteral node);
	public R visit(Expr.BooleanLiteral node);
	public R visit(Expr.NullLiteral node);
	public R visit(Expr.Array node);
	public R visit(Expr.VarRef node);
	public R visit(Expr.Unary node);
	public R visit(Expr.Binary node);
	public R visit(Expr.GetItem node);
	public R visit(Expr.SetItem node);
	public R visit(Expr.GetAttr node);
	public R visit(Expr.SetAttr node);
	public R visit(Expr.Super node);
	public R visit(Expr.This node);
	
}
