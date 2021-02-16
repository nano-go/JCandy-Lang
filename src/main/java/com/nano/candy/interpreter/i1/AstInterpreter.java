package com.nano.candy.interpreter.i1;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.AstVisitor;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.comp.Checker;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.env.Environment;
import com.nano.candy.interpreter.i1.resolver.CandyResolver;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;

public class AstInterpreter implements Interpreter, AstVisitor<CandyObject, CandyObject> {

	private Environment env;
	private ExpressionInterpreter exprInterpreter;
	private StmtInterpreter stmtInterpreter;
	private boolean isInteratively;

	private ASTreeNode node;

	protected AstInterpreter() {
		this.exprInterpreter = new ExpressionInterpreter();
		this.stmtInterpreter = new StmtInterpreter();
	}

	public boolean isInteratively() {
		return isInteratively;
	}
	
	public CandyObject evalExpr(Expr expr) {
		return expr.accept(this);
	}
	
	public CandyObject executeStmt(Stmt stmt) {
		return stmt.accept(this);
	}
	
	public Environment getEnvironment() {
		return env;
	}
	
	public void syncLocation(ASTreeNode node) {
		env.syncLocation(node);
	}
	
	public void syncLocation(Position pos) {
		env.syncLocation(pos);
	}
	
	@Override
	public void initOrReset() {
		this.env = new Environment();
	}
	
	@Override
	public void load(ASTreeNode node) {
		CandyResolver resolver = new CandyResolver();
		env.setDistances(resolver.resolve(node));
		this.node = node;
	}

	@Override
	public boolean run(boolean isInteratively) {
		this.isInteratively = isInteratively;
		ASTreeNode.accept(node, this);
		return true;
	}

	@Override
	public void visit(Program node) {
		syncLocation(node);
		for (Stmt stmt : node.block.stmts) {
			executeStmt(stmt);
		}
	}

	@Override
	public CandyObject visit(Stmt.FuncDef node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}
	
	@Override
	public CandyObject visit(Stmt.Block node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject visit(Stmt.While node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject visit(Stmt.For node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}
	
	@Override
	public CandyObject visit(Stmt.Return node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject visit(Stmt.Break node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}
	
	@Override
	public CandyObject visit(Stmt.Continue node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject visit(Stmt.If node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject visit(Stmt.Assert node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject visit(Stmt.VarDef node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}
	
	@Override
	public CandyObject visit(Stmt.ClassDef node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}
	
	@Override
	public CandyObject visit(Stmt.ExprS node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject visit(Expr.GetItem node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.SetItem node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.GetAttr node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.SetAttr node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.Lambda node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.Assign node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.Array node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.IntegerLiteral node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}
	
	@Override
	public CandyObject visit(Expr.DoubleLiteral node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.StringLiteral node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.VarRef node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.Super node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.This node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.Unary node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.Binary node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.BooleanLiteral node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject visit(Expr.NullLiteral node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}
	
	@Override
	public CandyObject visit(Expr.CallFunc node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}
}
