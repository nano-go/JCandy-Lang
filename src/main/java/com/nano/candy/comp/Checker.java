package com.nano.candy.comp;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.AstVisitor;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.parser.TokenKind;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Optional;

public class Checker implements AstVisitor<Stmt, Expr> {
	
	private static final String INITIALIZER_NAME = "init";
	private static final int MAX_PARAMETER_NUMBER = 255;
	
	protected static final Logger logger = Logger.getLogger();

	protected static void error(ASTreeNode node, String msg, Object... args) {
		logger.error(node.pos, msg, args);
	}

	protected static void warn(ASTreeNode node, String msg, Object... args) {
		logger.warn(node.pos, msg, args);
	}
	
	public static void check(ASTreeNode node){
		ASTreeNode.accept(node, new Checker());
	}
	
	enum FunctionType {
		NONE,
		FUNCTION,
		METHOD,
		LAMBDA,
		INIT
	}
	
	protected boolean inLoop;
	protected boolean inClass;
	protected FunctionType curFunctionType = FunctionType.NONE;
	
	// This field is used to determine the reachability of a statement.
	protected boolean reachable = true;
	
	private boolean checkReachable(Stmt stmt) {
		if (!reachable) {
			warn(stmt, "Unreachable.");
			return false;
		}
		return true;
	}
	
	private void checkIterable(Expr iterable) {
		if (iterable.isLiteral() && !(iterable instanceof Expr.Array)) {
			warn(iterable, "The constant is not iterable.");
		}
	}
	
	private boolean isEmtpy(Stmt stmt) {
		if (stmt == null) return true;
		if (stmt instanceof Stmt.Block) {
			return ((Stmt.Block)stmt).stmts.isEmpty();
		}
		return false;
	}
	
	private void visitBlock(Stmt.Block block) {
		ListIterator<Stmt> i = block.stmts.listIterator();
		while (i.hasNext()) {
			Stmt stmt = i.next();
			stmt = visitStmt(stmt);
			if (stmt == null) {
				i.remove();
			} else {
				i.set(stmt);
			}
		}
	}
	
	private Stmt visitStmt(Stmt stmt) {
		boolean reachable = checkReachable(stmt);
		stmt = stmt.accept(this);
		if (!reachable) {
			return null;
		}
		return stmt;
	}
	
	private Expr visitExpr(Expr expr) {
		return expr.accept(this);
	}
	
	@Override
	public void visit(Program node) {
		visitBlock(node.block);
	}

	@Override
	public Stmt visit(Stmt.Block node) {
		visitBlock(node);
		return node;
	}

	@Override
	public Stmt visit(Stmt.While node) {
		boolean originalInLoop = inLoop;
		boolean originalReachable = reachable;
		this.reachable = true;
		this.inLoop = true;
		
		node.condition = visitExpr(node.condition);
		visitStmt(node.body);
		if (node.condition.isFalsely()) {
			node = null;
		} 
		
		this.inLoop = originalInLoop;
		this.reachable = originalReachable;
		return node;
	}

	@Override
	public Stmt visit(Stmt.For node) {	
		boolean originalInLoop = inLoop;
		boolean originalReachable = reachable;
		this.reachable = true;
		this.inLoop = true;
		
		node.iterable = visitExpr(node.iterable);
		checkIterable(node.iterable);
		visitStmt(node.body);
		
		this.inLoop = originalInLoop;
		this.reachable = originalReachable;
		return node;
	}
	
	@Override
	public Stmt visit(Stmt.Continue node) {
		if (!inLoop) {
			error(node, "The 'continue' outside loop.");
		}
		reachable = false;
		return node;
	}

	@Override
	public Stmt visit(Stmt.Break node) {
		if (!inLoop) {
			error(node, "The 'break' outside loop.");
		}
		reachable = false;
		return node;
	}

	@Override
	public Stmt visit(Stmt.If node) {
		node.condition = visitExpr(node.condition);
		
		boolean thenBodyIsReachable;
		boolean elseBodyIsReachable = true;
		
		Stmt thenBody = visitStmt(node.thenBody);
		thenBodyIsReachable = this.reachable;
		this.reachable = true;
		
		Stmt elseBody = null;
		if (node.elseBody.isPresent()) {
			elseBody = node.elseBody.get();
			elseBody = visitStmt(elseBody);
			elseBodyIsReachable = this.reachable;
		}
		
		this.reachable = thenBodyIsReachable || elseBodyIsReachable;
		
		if (node.condition.isConstant()) {
			if (node.condition.isFalsely()) {
				return isEmtpy(elseBody) ? null : elseBody;
			}
			return isEmtpy(thenBody) ? null : thenBody;
		}
		
		node.thenBody = thenBody;
		if (elseBody != null) {
			node.elseBody = Optional.ofNullable(elseBody);
		}
		
		if (isEmtpy(node.thenBody)) {
			Position conditionPos = node.condition.pos;
			node.condition = new Expr.Unary(TokenKind.NOT, node.condition);
			node.condition.pos = conditionPos;
			node.thenBody = elseBody;
			node.elseBody = Optional.empty();
		}
		return node;
	}

	@Override
	public Stmt visit(Stmt.Assert node) {
		node.condition = visitExpr(node.condition);
		if (node.condition.isConstant() && !node.condition.isFalsely()) {
			return null;
		}
		node.errorInfo = visitExpr(node.errorInfo);
		return node;
	}

	@Override
	public Stmt visit(Stmt.ExprS node) {
		node.expr = visitExpr(node.expr);
		return node;
	}

	@Override
	public Stmt visit(Stmt.VarDef node) {
		if (node.init.isPresent()) {
			node.init = Optional.of(visitExpr(node.init.get()));
		}
		return node;
	}

	@Override
	public Stmt visit(Stmt.ClassDef node) {
		if (curFunctionType != FunctionType.NONE) {
			error(node, "Can't define the class in a function.");
		}
		
		if (node.superClassName.isPresent()) {
			if (node.superClassName.get().name.equals(node.name)) {
				error(node, "A class can't inherti itself(%s).", node.name);
			}
		}
		
		boolean origin = inClass;
		inClass = true;
		if (node.initializer.isPresent()) {
			node.initializer.get().accept(this);
		}
		
		HashSet<String> duplicatedNameHelper = new HashSet<>(node.methods.size());
		for (Stmt.FuncDef method : node.methods) {
			String name = method.name.get();
			if (duplicatedNameHelper.contains(name)) {
				warn(method, "Duplicated method '%s' in the class '%s'.",
				     name, node.name) ;
			} else duplicatedNameHelper.add(name);
			method.accept(this);
		}
		
		inClass = origin;
		return node;
	}
	
	@Override
	public Stmt visit(Stmt.FuncDef node) {
		boolean originalReachable = reachable;
		FunctionType originalFuncType = curFunctionType;
		this.reachable = true;
		this.curFunctionType = getFuncType(node);	
		checkParams(node);	
		visitBlock(node.body);
		this.curFunctionType = originalFuncType;
		this.reachable = originalReachable;
		return node;
	}
	
	private FunctionType getFuncType(Stmt.FuncDef node) {
		if (!node.name.isPresent()) {
			return FunctionType.LAMBDA;
		} 
		if (inClass) {
			return INITIALIZER_NAME.equals(node.name.get()) 
				? FunctionType.INIT
				: FunctionType.METHOD;
		} 
		return FunctionType.FUNCTION;
	}
	
	private void checkParams(Stmt.FuncDef node) {
		String funcName = node.name.isPresent() ? node.name.get() : "lambda";
		HashSet<String> duplicatedNameHelper = new HashSet<>(node.params.size());
		for (String param : node.params) {
			if (duplicatedNameHelper.contains(param)) {
				error(node, 
					  "Duplicated parameter '%s' in the function '%s'.", 
					  param, funcName	  
				);
			}
			duplicatedNameHelper.add(param);
		}
		
		if (node.params.size() > MAX_PARAMETER_NUMBER) {
			error(node, 
				  "The number of the parameter of the function '%s' must be less than %d.",
				  funcName, MAX_PARAMETER_NUMBER
			);
		}
	}

	@Override
	public Stmt visit(Stmt.Return node) {
		if (node.expr.isPresent()) {
			node.expr = Optional.of(visitExpr(node.expr.get()));
		}
		this.reachable = false;
		switch (curFunctionType) {
			case NONE:
				error(node, "The 'return' outside function.");
				break;
			
			case INIT:
				if (node.expr.isPresent())
					error(node, "Can't return from initalizer.");
				break;
		}
		return node;
	}

	
	/*===================== check expression ==================*/
	
	@Override
	public Expr visit(Expr.Assign node) {
		node.rhs = visitExpr(node.rhs);
		return node;
	}
	
	@Override
	public Expr visit(Expr.Unary node) {
		node.expr = visitExpr(node.expr);
		return ConstantFolder.foldUnaryExpr(node);
	}

	@Override
	public Expr visit(Expr.Binary node) {
		node.left = visitExpr(node.left);
		node.right = visitExpr(node.right);
		return ConstantFolder.foldBinaryExpr(node);
	}
	
	@Override
	public Expr visit(Expr.CallFunc node) {
		node.expr = visitExpr(node.expr);
		ListIterator<Expr> i = node.arguments.listIterator();
		while (i.hasNext()) {
			Expr arg = i.next();
			i.set(visitExpr(arg));
		}
		checkCallable(node.expr);
		return node;
	}
	
	private void checkCallable(Expr expr) {
		if (expr.isConstant()) {
			error(expr, "The constant is not callable.");
		}
	}
	
	@Override
	public Expr visit(Expr.Lambda node) {
		visitStmt(node.funcDef);
		return node;
	}

	@Override
	public Expr visit(Expr.Super node) {
		if (!inClass) {
			error(node, "The 'super' outside class.");
		}
		return node;
	}

	@Override
	public Expr visit(Expr.This node) {
		if (!inClass) {
			error(node, "The 'this' outside class.");
		}
		return node;
	}
	
	@Override
	public Expr visit(Expr.GetItem node) {
		node.key = visitExpr(node.key);
		return node;
	}

	@Override
	public Expr visit(Expr.SetItem node) {
		visitExpr(node.objExpr);
		node.rhs = visitExpr(node.rhs);
		return node;
	}
	
	@Override
	public Expr visit(Expr.SetAttr node) {
		node.rhs = visitExpr(node.rhs);
		return node;
	}

	@Override
	public Expr visit(Expr.GetAttr node) {
		return node;
	}

	@Override
	public Expr visit(Expr.Array node) {
		ListIterator<Expr> i = node.elements.listIterator();
		while (i.hasNext()) {
			Expr expr = i.next();
			i.set(visitExpr(expr));
		}
		return node;
	}
	
	@Override
	public Expr visit(Expr.IntegerLiteral node) {
		return node;
	}

	@Override
	public Expr visit(Expr.DoubleLiteral node) {
		return node;
	}

	@Override
	public Expr visit(Expr.StringLiteral node) {
		return node;
	}

	@Override
	public Expr visit(Expr.BooleanLiteral node) {
		return node;
	}

	@Override
	public Expr visit(Expr.NullLiteral node) {
		return node;
	}

	@Override
	public Expr visit(Expr.VarRef node) {
		return node;
	}

}
