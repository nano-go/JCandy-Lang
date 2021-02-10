package com.nano.candy.ast;

public abstract class AbstractAstVisitor<R> implements AstVisitor<R> {

	@Override
	public R visit(Program node) {
		for (Stmt stmt : node.block.stmts) {
			stmt.accept(this);
		}
		return null;
	}

	@Override
	public R visit(Stmt.If node) {
		node.condition.accept(this);
		node.thenBody.accept(this);
		if (node.elseBody.isPresent()) {
			node.elseBody.get().accept(this);
		}
		return null;
	}

	@Override
	public R visit(Stmt.While node) {
		node.condition.accept(this);
		node.body.accept(this);
		return null;
	}

	@Override
	public R visit(Stmt.For node) {
		node.iterable.accept(this);
		node.body.accept(this);
		return null;
	}

	@Override
	public R visit(Stmt.Assert node) {
		node.condition.accept(this);
		if (node.errorInfo.isPresent()) {
			node.errorInfo.get().accept(this);
		}
		return null;
	}

	@Override
	public R visit(Stmt.Return node) {
		if (node.expr.isPresent()) {
			return node.expr.get().accept(this);
		}
		return null;
	}

	@Override
	public R visit(Stmt.ExprS node) {
		node.expr.accept(this);
		return null;
	}

	@Override
	public R visit(Stmt.VarDef node) {
		if (node.init.isPresent()) {
			node.init.get().accept(this);
		}
		return null;
	}

	@Override
	public R visit(Stmt.FuncDef node) {
		node.body.accept(this);
		return null;
	}

	@Override
	public R visit(Stmt.ClassDef node) {
		for (Stmt.FuncDef func : node.methods) {
			func.accept(this);
		}
		return null;
	}

	@Override
	public R visit(Stmt.Block node) {
		for (Stmt stmt : node.stmts) {
			stmt.accept(this);
		}
		return null;
	}
	
	@Override
	public R visit(Expr.CallFunc node) {
		node.expr.accept(this);
		for (Expr arg : node.arguments) {
			arg.accept(this);
		}
		return null;
	}

	@Override
	public R visit(Expr.Array node) {
		for (Expr element : node.elements) {
			element.accept(this);
		}
		return null;
	}
	
	@Override
	public R visit(Expr.Lambda node) {
		node.funcDef.accept(this);
		return null;
	}

	@Override
	public R visit(Expr.GetAttr node) {
		node.objExpr.accept(this);
		return null;
	}

	@Override
	public R visit(Expr.SetAttr node) {
		node.rhs.accept(this);
		node.objExpr.accept(this);
		return null;
	}

	@Override
	public R visit(Expr.GetItem node) {
		node.objExpr.accept(this);
		node.key.accept(this);
		return null;
	}

	@Override
	public R visit(Expr.SetItem node) {
		node.objExpr.accept(this);
		node.rhs.accept(this);
		return null;
	}

	@Override
	public R visit(Expr.Assign node) {
		node.rhs.accept(this);
		return null;
	}

	@Override
	public R visit(Expr.Unary node) {
		node.expr.accept(this);
		return null;
	}

	@Override
	public R visit(Expr.Binary node) {
		node.left.accept(this);
		node.right.accept(this);
		return null;
	}

}
