package com.nano.candy.ast;

/**
 * This class can help you to traverse the AST.
 */
public abstract class AbstractAstVisitor<S, E> implements AstVisitor<S, E> {

	@Override
	public void visit(Program node) {
		node.block.accept(this);
	}

	@Override
	public S visit(Stmt.ErrorStmt node) { return null; }
	@Override
	public S visit(Stmt.Empty node) { return null; }
	
	@Override
	public S visit(Stmt.Import node) {
		node.modulePath.accept(this);
		return null;
	}

	@Override
	public S visit(Stmt.ImportList node) {
		for (Stmt s : node.importStmts) {
			s.accept(this);
		}
		return null;
	}

	@Override
	public S visit(Stmt.Return node) {
		if (node.expr.isPresent()) {
			node.accept(this);
		}
		return null;
	}
	
	@Override
	public S visit(Stmt.While node) {
		node.condition.accept(this);
		node.body.accept(this);
		return null;
	}

	@Override
	public S visit(Stmt.Assert node) {
		node.condition.accept(this);
		node.errorInfo.accept(this);
		return null;
	}

	@Override
	public S visit(Stmt.Raise node) {
		node.exceptionExpr.accept(this);
		return null;
	}

	@Override
	public S visit(Stmt.TryIntercept node) {
		node.tryBlock.accept(this);
		for (Stmt s : node.interceptionBlocks) {
			s.accept(this);
		}
		if (node.elseBlock.isPresent()) {
			node.elseBlock.get().accept(this);
		}
		return null;
	}

	@Override
	public S visit(Stmt.Interception node) {
		for (Expr e : node.exceptions) {
			e.accept(this);
		}
		node.block.accept(this);
		return null;
	}

	@Override
	public S visit(Stmt.If node) {
		node.condition.accept(this);
		node.thenBody.accept(this);
		if (node.elseBody.isPresent()) {
			node.elseBody.get().accept(this);
		}
		return null;
	}

	@Override
	public S visit(Stmt.For node) {
		node.iterable.accept(this);
		node.body.accept(this);
		return null;
	}

	@Override
	public S visit(Stmt.ExprS node) {
		node.expr.accept(this);
		return null;
	}

	@Override
	public S visit(Stmt.VarDef node) {
		if (node.init.isPresent()) {
			node.init.get().accept(this);
		}
		return null;
	}

	@Override
	public S visit(Stmt.FuncDef node) {
		for (Stmt.Parameter p : node.parameters.params) {
			if (p.defaultValue.isPresent()) {
				p.defaultValue.get().accept(this);
			}
		}
		node.body.accept(this);
		return null;
	}

	@Override
	public S visit(Stmt.ClassDef node) {
		if (node.superClass.isPresent()) {
			node.superClass.get().accept(this);
		}
		if (node.initializer.isPresent()) {
			node.initializer.get().accept(this);
		}
		for (Stmt method : node.methods) {
			method.accept(this);
		}
		if (node.staticBlock.isPresent()) {
			node.staticBlock.get().accept(this);
		}
		return null;
	}

	@Override
	public S visit(Stmt.Block node) {
		for (Stmt s : node.stmts) {
			s.accept(this);
		}
		return null;
	}

	@Override
	public E visit(Expr.Lambda node) {
		node.funcDef.accept(this);
		return null;
	}

	@Override
	public E visit(Expr.CallFunc node) {
		node.expr.accept(this);
		for (Expr.Argument arg : node.arguments) {
			arg.expr.accept(this);
		}
		return null;
	}

	@Override
	public E visit(Expr.Assign node) {
		node.rhs.accept(this);
		return null;
	}

	@Override
	public E visit(Expr.Array node) {
		for (Expr element : node.elements) {
			element.accept(this);
		}
		return null;
	}

	@Override
	public E visit(Expr.Tuple node) {
		for (Expr element : node.elements) {
			element.accept(this);
		}
		return null;
	}

	@Override
	public E visit(Expr.Map node) {
		for (Expr key : node.keys) {
			key.accept(this);
		}
		for (Expr val : node.values) {
			val.accept(this);
		}
		return null;
	}
	
	@Override
	public E visit(Expr.Unary node) {
		node.expr.accept(this);
		return null;
	}

	@Override
	public E visit(Expr.TernaryOperator node) {
		node.condition.accept(this);
		node.thenExpr.accept(this);
		node.elseExpr.accept(this);
		return null;
	}

	@Override
	public E visit(Expr.Binary node) {
		node.left.accept(this);
		node.right.accept(this);
		return null;
	}

	@Override
	public E visit(Expr.GetItem node) {
		node.objExpr.accept(this);
		node.key.accept(this);
		return null;
	}

	@Override
	public E visit(Expr.SetItem node) {
		node.objExpr.accept(this);
		node.key.accept(this);
		node.rhs.accept(this);
		return null;
	}

	@Override
	public E visit(Expr.GetAttr node) {
		node.objExpr.accept(this);
		return null;
	}

	@Override
	public E visit(Expr.SetAttr node) {
		node.objExpr.accept(this);
		node.rhs.accept(this);
		return null;
	}
}
