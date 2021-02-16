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
	// This field is used to determina a while statement is a infinite loop.
	private boolean hadBreak = false;
	private boolean returned = false;
	
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
		boolean originalHadBreak = hadBreak;
		boolean originalReachable = reachable;
		boolean originalReturned = returned;
		this.hadBreak = false;
		this.inLoop = true;
		
		boolean isConstantTrue = false;
		
		node.condition = visitExpr(node.condition);
		visitStmt(node.body);
		if (node.condition.isConstant()) {
			if (node.condition.isFalsely()) {
				node = null;
			} else {
				isConstantTrue = true;
			}
		}
		
		// The while statement is infinite loop if the condition expression
		// is a true constant and the while body has no break statements.
		// The next statements are unreachable if the while statement is infinite loop.
		this.reachable = originalReachable && (!isConstantTrue || hadBreak);
		this.returned = originalReturned;
		this.hadBreak = originalHadBreak;
		this.inLoop = originalInLoop;	
		return node;
	}

	@Override
	public Stmt visit(Stmt.For node) {	
		boolean originalInLoop = inLoop;
		boolean originalHadBreak = hadBreak;
		boolean originalReachable = reachable;
		boolean originalReturned = returned;
		this.inLoop = true;
		this.hadBreak = false;
		
		node.iterable = visitExpr(node.iterable);
		checkIterable(node.iterable);
		visitStmt(node.body);
		
		this.inLoop = originalInLoop;
		this.hadBreak = originalHadBreak;
		this.reachable = originalReachable;
		this.returned = originalReturned;
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
		
		// The next statements are unreachable if the then-body and 
		// the else-body are both unreachable.
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
		
		node.thenBody = thenBody;
		if (elseBody != null) {
			node.elseBody = Optional.of(elseBody);
		}
		
		// if (a) {} else print(a); -> if (!a) printa();
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
		checkMethodNames(node);
		inClass = origin;
		return node;
	}
	
	private void checkMethodNames(Stmt.ClassDef classDef) {
		HashSet<String> duplicatedNameHelper = new HashSet<>(classDef.methods.size());
		for (Stmt.FuncDef method : classDef.methods) {
			String name = method.name.get();
			if (duplicatedNameHelper.contains(name)) {
				warn(method, "Duplicated method '%s' in the class '%s'.",
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
