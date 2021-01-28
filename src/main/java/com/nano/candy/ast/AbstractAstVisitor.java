package com.nano.candy.ast;

public abstract class AbstractAstVisitor<R> implements AstVisitor<R> {

	@Override
	public R accept(Program node) {
		node.block.accept(this);
		return null;
	}

	@Override
	public R accept(Stmt.If node) {
		node.condition.accept(this);
		node.thenBody.accept(this);
		if (node.elseBody.isPresent()) {
			node.elseBody.get().accept(this);
		}
		return null;
	}

	@Override
	public R accept(Stmt.While node) {
		node.condition.accept(this);
		node.body.accept(this);
		return null;
	}

	@Override
	public R accept(Stmt.For node) {
		node.iterable.accept(this);
		node.body.accept(this);
		return null;
	}

	@Override
	public R accept(Stmt.Assert node) {
		node.condition.accept(this);
		if (node.errorInfo.isPresent()) {
			node.errorInfo.get().accept(this);
		}
		return null;
	}

	@Override
	public R accept(Stmt.Return node) {
		if (node.expr.isPresent()) {
			return node.expr.get().accept(this);
		}
		return null;
	}

	@Override
	public R accept(Stmt.ExprS node) {
		node.expr.accept(this);
		return null;
	}

	@Override
	public R accept(Stmt.VarDef node) {
		if (node.init.isPresent()) {
			node.init.get().accept(this);
		}
		return null;
	}

	@Override
	public R accept(Stmt.FuncDef node) {
		node.body.accept(this);
		return null;
	}

	@Override
	public R accept(Stmt.ClassDef node) {
		for (Stmt.FuncDef func : node.methods) {
			func.accept(this);
		}
		return null;
	}

	@Override
	public R accept(Stmt.Block node) {
		for (Stmt stmt : node.stmts) {
			stmt.accept(this);
		}
		return null;
	}
	
	@Override
	public R accept(Expr.CallFunc node) {
		node.expr.accept(this);
		for (Expr arg : node.arguments) {
			arg.accept(this);
		}
		return null;
	}

	@Override
	public R accept(Expr.Array node) {
		for (Expr element : node.elements) {
			element.accept(this);
		}
		return null;
	}
	
	@Override
	public R accept(Expr.Lambda node) {
		node.funcDef.accept(this);
		return null;
	}

	@Override
	public R accept(Expr.GetAttr node) {
		node.objExpr.accept(this);
		return null;
	}

	@Override
	public R accept(Expr.SetAttr node) {
		node.rhs.accept(this);
		node.objExpr.accept(this);
		return null;
	}

	@Override
	public R accept(Expr.GetItem node) {
		node.objExpr.accept(this);
		node.key.accept(this);
		return null;
	}

	@Override
	public R accept(Expr.SetItem node) {
		node.objExpr.accept(this);
		node.rhs.accept(this);
		return null;
	}

	@Override
	public R accept(Expr.Assign node) {
		node.rhs.accept(this);
		return null;
	}

	@Override
	public R accept(Expr.Unary node) {
		node.expr.accept(this);
		return null;
	}

	@Override
	public R accept(Expr.Binary node) {
		node.left.accept(this);
		node.right.accept(this);
		return null;
	}

}
