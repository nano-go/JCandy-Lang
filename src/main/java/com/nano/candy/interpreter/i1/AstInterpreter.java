package com.nano.candy.interpreter.i1;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.AstVisitor;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.env.Environment;
import com.nano.candy.interpreter.i1.resolver.CandyResolver;
import com.nano.candy.utils.Position;

public class AstInterpreter implements Interpreter, AstVisitor<CandyObject> {

	private Environment env;
	private ExpressionInterpreter exprInterpreter;
	private StmtInterpreter stmtInterpreter;
	private boolean isInteratively;

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
	public void resolve(ASTreeNode node) {
		CandyResolver resolver = new CandyResolver();
		env.setDistances(resolver.resolve(node));
	}

	@Override
	public boolean run(ASTreeNode node, boolean isInteratively) {
		this.isInteratively = isInteratively;
		node.accept(this);
		return true;
	}

	@Override
	public CandyObject accept(Program node) {
		syncLocation(node);
		for (Stmt stmt : node.block.stmts) {
			executeStmt(stmt);
		}
		return null;
	}

	@Override
	public CandyObject accept(Stmt.FuncDef node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}
	
	@Override
	public CandyObject accept(Stmt.Block node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject accept(Stmt.While node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject accept(Stmt.For node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}
	
	@Override
	public CandyObject accept(Stmt.Return node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject accept(Stmt.Break node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}
	
	@Override
	public CandyObject accept(Stmt.Continue node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject accept(Stmt.If node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject accept(Stmt.Assert node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject accept(Stmt.VarDef node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}
	
	@Override
	public CandyObject accept(Stmt.ClassDef node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}
	
	@Override
	public CandyObject accept(Stmt.ExprS node) {
		syncLocation(node);
		stmtInterpreter.execute(this, node);
		return null;
	}

	@Override
	public CandyObject accept(Expr.GetItem node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.SetItem node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.GetAttr node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.SetAttr node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.Lambda node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.Assign node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.Array node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.IntegerLiteral node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}
	
	@Override
	public CandyObject accept(Expr.DoubleLiteral node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.StringLiteral node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.VarRef node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.Super node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.This node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.Unary node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.Binary node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.BooleanLiteral node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}

	@Override
	public CandyObject accept(Expr.NullLiteral node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}
	
	@Override
	public CandyObject accept(Expr.CallFunc node) {
		syncLocation(node);
		return exprInterpreter.eval(this, node);
	}
}
