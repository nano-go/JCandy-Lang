package com.nano.candy.ast;

public interface AstVisitor<R> {

	public R accept(Program node);
	
	public R accept(Stmt.Return node);
	public R accept(Stmt.Continue node);
	public R accept(Stmt.Break node);
	public R accept(Stmt.While node);
	public R accept(Stmt.Assert node);
	public R accept(Stmt.If node);
	public R accept(Stmt.For node);
	public R accept(Stmt.ExprS node);
	public R accept(Stmt.VarDef node);
	public R accept(Stmt.FuncDef node);
	public R accept(Stmt.ClassDef node);
	public R accept(Stmt.Block node);

	public R accept(Expr.Lambda node);
	public R accept(Expr.CallFunc node);
	public R accept(Expr.Assign node);
	public R accept(Expr.IntegerLiteral node);
	public R accept(Expr.DoubleLiteral node);
	public R accept(Expr.StringLiteral node);
	public R accept(Expr.BooleanLiteral node);
	public R accept(Expr.NullLiteral node);
	public R accept(Expr.Array node);
	public R accept(Expr.VarRef node);
	public R accept(Expr.Unary node);
	public R accept(Expr.Binary node);
	public R accept(Expr.GetItem node);
	public R accept(Expr.SetItem node);
	public R accept(Expr.GetAttr node);
	public R accept(Expr.SetAttr node);
	public R accept(Expr.Super node);
	public R accept(Expr.This node);
	
}
