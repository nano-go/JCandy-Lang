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
	
	// This field is used to analyze the reachability of the statements.
	private boolean reachable = true;
	
	// This field is used to determina whether a while statement is infinite.
	private boolean hadBreak = false;
	private boolean returned = false;
	
	private HashSet<String> lableTable = new HashSet<>();
	
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
		boolean reachable = this.reachable;
		if (!reachable) {
			warn(stmt, "Unreachable.");
		}
		stmt = stmt.accept(this);
		return reachable ? stmt : null;
	}
	
	private Expr visitExpr(Expr expr) {
		return expr.accept(this);
	}
	
	@Override
	public void visit(Program node) {
		visitBlock(node.block);
	}

	@Override
	public Stmt visit(Stmt.ErrorStmt node) {
		throw new Error("Unexpected error statement.");
	}

	@Override
	public Stmt visit(Stmt.Block node) {
		visitBlock(node);
		return node;
	}
	
	private boolean addLable(Stmt.Loop loop) {
		if (loop.lableName.isPresent()) {
			if (lableTable.add(loop.lableName.get())) {
				return true;
			}
			logger.error(loop.lablePos.get(), "the lable is defined.");
			return false;
		}
		return false;
	}
	
	private void removeLable(boolean newLable, Stmt.Loop loop) {
		if (newLable) {
			lableTable.remove(loop.lableName.get());
		}
	}
	
	private void referenceLable(String name, ASTreeNode node) {
		if (!lableTable.contains(name)) {
			error(node, "the lable '%s' is not defined.", name);
		}
	}

	@Override
	public Stmt visit(Stmt.While node) {
		boolean newLable = addLable(node);
		boolean originalInLoop = inLoop;
		boolean originalHadBreak = hadBreak;
		boolean originalReachable = reachable;
		boolean originalReturned = returned;
		this.hadBreak = false;
		this.inLoop = true;
		
		boolean isConstantTrue = false;
		
		node.condition = visitExpr(node.condition);
		node.body = visitStmt(node.body);
		if (node.condition.isConstant()) {
			if (node.condition.isFalsely()) {
				node = null;
			} else {
				isConstantTrue = true;
			}
		}
		
		boolean isInfiniteLoop = isConstantTrue && !hadBreak;
		
		this.inLoop = originalInLoop;
		this.hadBreak = originalHadBreak;
		this.reachable = originalReachable && !isInfiniteLoop;
		this.returned = originalReturned;
		
		removeLable(newLable, node);
		return node;
	}

	@Override
	public Stmt visit(Stmt.For node) {	
		boolean newLable = addLable(node);
		boolean originalInLoop = inLoop;
		boolean originalHadBreak = hadBreak;
		boolean originalReachable = reachable;
		boolean originalReturned = returned;
		this.inLoop = true;
		this.hadBreak = false;
		
		node.iterable = visitExpr(node.iterable);
		checkIterable(node.iterable);
		visitBlock(node.body);
		
		this.inLoop = originalInLoop;
		this.hadBreak = originalHadBreak;
		this.reachable = originalReachable;
		this.returned = originalReturned;
		removeLable(newLable, node);
		return node;
	}
	
	private void checkIterable(Expr iterable) {
		if (iterable.isConstant()) {
			warn(iterable, "The constant '%s' is not iterable.", iterable);
		}
	}
	
	@Override
	public Stmt visit(Stmt.Continue node) {
		if (!inLoop) {
			error(node, "The 'continue' outside loop.");
		} else if (node.lableName.isPresent()) {
			referenceLable(node.lableName.get(), node);
		}
		reachable = false;
		return node;
	}

	@Override
	public Stmt visit(Stmt.Break node) {
		if (!inLoop) {
			error(node, "The 'break' outside loop.");
		} else if (node.lableName.isPresent()) {
			referenceLable(node.lableName.get(), node);
		}
		hadBreak = true;
		reachable = false;
		return node;
	}

	@Override
	public Stmt visit(Stmt.If node) {
		node.condition = visitExpr(node.condition);
		
		boolean originalReachable = reachable;
		boolean originalReturned = returned;
		
		boolean thenBodyReachable;
		boolean elseBodyReachable = true;
		
		Stmt thenBody = visitStmt(node.thenBody);
		thenBodyReachable = this.reachable;
		
		Stmt elseBody = null;
		if (node.elseBody.isPresent()) {
			this.reachable = originalReachable;
			this.returned = originalReturned;
			elseBody = visitStmt(node.elseBody.get());
			elseBodyReachable = this.reachable;
		}
		
		// All the following statements are unreachable if the then-body and the 
		// else-body are both unreachable.
		this.reachable = originalReachable && 
			(thenBodyReachable || elseBodyReachable);
		this.returned = originalReturned;
		
		// Pruning
		if (node.condition.isConstant()) {
			if (node.condition.isFalsely()) {
				return isEmtpy(elseBody) ? null : elseBody;
			}
			return isEmtpy(thenBody) ? null : thenBody;
		}
		
		
		if (isEmtpy(thenBody)) {
			// if (a) {} else {} -> a
			if (isEmtpy(elseBody)) {
				Stmt.ExprS exprs = new Stmt.ExprS(node.condition);
				exprs.pos = node.condition.pos;
				return exprs;
			}
			
			// if (a) {} else print(a); -> if (!a) print(a);
			Position conditionPos = node.condition.pos;
			node.condition = new Expr.Unary(TokenKind.NOT, node.condition);
			node.condition.pos = conditionPos;
			node.thenBody = elseBody;
			node.elseBody = Optional.empty();
		} else {
			node.thenBody = thenBody;
			if (elseBody != null) {
				node.elseBody = Optional.of(elseBody);
			}
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
			String superClassName = node.superClassName.get().name;
			if (superClassName.equals(node.name)) {
				error(node, "A class can't inherti from itself(%s).", node.name);
			}
		}
		
		boolean origin = inClass;
		inClass = true;
		if (node.initializer.isPresent()) {
			node.initializer.get().accept(this);
		}
		checkMethodNames(node);
		inClass = origin;
		return node;
	}
	
	private void checkMethodNames(Stmt.ClassDef classDef) {
		HashSet<String> duplicatedNameHelper = new HashSet<>(classDef.methods.size());
		for (Stmt.FuncDef method : classDef.methods) {
			String name = method.name.get();
			if (duplicatedNameHelper.contains(name)) {
				warn(method, "Duplicated method name '%s' in the class '%s'.",
				     name, classDef.name) ;
			} else duplicatedNameHelper.add(name);
			method.accept(this);
		}
	}
	
	@Override
	public Stmt visit(Stmt.FuncDef node) {
		boolean originalReachable = reachable;
		boolean originalReturned = returned;
		FunctionType originalFuncType = curFunctionType;
		this.curFunctionType = getFuncType(node);
		this.returned = false;
		
		checkParams(node);	
		visitBlock(node.body);
		insertReturnStmt(node);
		
		this.curFunctionType = originalFuncType;
		this.reachable = originalReachable;
		this.returned = originalReturned;
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
				error(node, "Duplicated parameter name '%s' in the function '%s'.", 
					  param, funcName);
			} else {
				duplicatedNameHelper.add(param);
			}
		}

		if (node.params.size() > MAX_PARAMETER_NUMBER) {
			error(node, "Can't have more than %d in the function '%s'.",
				  MAX_PARAMETER_NUMBER, funcName);
		}
	}

	private void insertReturnStmt(Stmt.FuncDef node) {
		if (!reachable || returned) {
			return;
		}
		
		Stmt.Return returnStmt = new Stmt.Return(null);
		returnStmt.pos = Position.PREVIOUS_POSITION;
		node.body.stmts.add(returnStmt);
	}

	@Override
	public Stmt visit(Stmt.Return node) {
		if (node.expr.isPresent()) {
			node.expr = Optional.of(visitExpr(node.expr.get()));
		}
		this.reachable = false;
		this.returned = true;
		switch (curFunctionType) {
			case NONE:
				error(node, "The 'return' outside function.");
				break;
			
			case INIT:
				if (node.expr.isPresent())
					error(node, "Can't return a value from an initializer.");
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
		if (node.arguments.size() > MAX_PARAMETER_NUMBER) {
			error(node, "Too many arguments.");
		}
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
			error(expr, "The constant '%s' is not callable.", expr.toString());
		}
	}
	
	@Override
	public Expr visit(Expr.Lambda node) {
		node.funcDef.accept(this);
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
		node.objExpr = visitExpr(node.objExpr);
		node.key = visitExpr(node.key);
		return node;
	}

	@Override
	public Expr visit(Expr.SetItem node) {
		node.objExpr = visitExpr(node.objExpr);
		node.key = visitExpr(node.key);
		node.rhs = visitExpr(node.rhs);
		return node;
	}
	
	@Override
	public Expr visit(Expr.GetAttr node) {
		node.objExpr = visitExpr(node.objExpr);
		return node;
	}
	
	@Override
	public Expr visit(Expr.SetAttr node) {
		node.objExpr = visitExpr(node.objExpr);
		node.rhs = visitExpr(node.rhs);
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
