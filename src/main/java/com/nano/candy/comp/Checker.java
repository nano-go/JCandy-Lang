package com.nano.candy.comp;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.AbstractAstVisitor;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.utils.Logger;
import java.util.HashSet;

public class Checker extends AbstractAstVisitor<Void> {
	
	private static final String INITIALIZER_NAME = "init";
	private static final int MAX_PARAMETER_NUMBER = 255;
	
	protected static final Logger logger = Logger.getLogger();

	protected static void error(ASTreeNode node, String msg, Object... args) {
		logger.error(node.pos, msg, args);
	}

	protected static void warn(ASTreeNode node, String msg, Object... args) {
		logger.warn(node.pos, msg, args);
	}
	
	protected boolean inLoop;
	protected boolean inClass;
	protected FunctionType curFunctionType = FunctionType.NONE;

	enum FunctionType {
		NONE,
		FUNCTION,
		METHOD,
		LAMBDA,
		INIT
	}
	
	@Override
	public Void accept(Program node) {
		for (Stmt stmt : node.block.stmts) {
			stmt.accept(this);
		}
		return null;
	}

	@Override
	public Void accept(Stmt.While node) {
		boolean origin = inLoop;
		inLoop = true;
		super.accept(node);
		inLoop = origin;
		return null;
	}

	@Override
	public Void accept(Stmt.For node) {
		boolean origin = inLoop;
		inLoop = true;
		node.iterable.accept(this);
		for (Stmt stmt : node.body.stmts) {
			stmt.accept(this);
		}
		inLoop = origin;
		return null;
	}
	
	@Override
	public Void accept(Stmt.Continue node) {
		if (!inLoop) {
			error(node, "The 'continue' outside loop.");
		}
		return null;
	}

	@Override
	public Void accept(Stmt.Break node) {
		if (!inLoop) {
			error(node, "The 'break' outside loop.");
		}
		return null;
	}

	@Override
	public Void accept(Stmt.ClassDef node) {
		if (curFunctionType != FunctionType.NONE) {
			error(node, "Can't define a class in a function.");
		}
		
		if (node.superClassName.isPresent()) {
			if (node.superClassName.get().name.equals(node.name)) {
				error(node, "A class can't inherti from itself(%s).", node.name);
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
		return null;
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

	@Override
	public Void accept(Stmt.FuncDef node) {
		FunctionType origin = curFunctionType;
		curFunctionType = getFuncType(node);
		
		HashSet<String> duplicatedNameHelper = new HashSet<>(node.params.size());
		for (String param : node.params) {
			if (duplicatedNameHelper.contains(param)) {
				error(node, "Duplicated parameter '%s' in the function '%s'.", param,
				    node.name.isPresent() ? node.name.get() : "lambda func");
			}
			duplicatedNameHelper.add(param);
		}
		
		if (node.params.size() > MAX_PARAMETER_NUMBER) {
			error(node, "The number of the parameter of the function '%s' must be less than %d.",
			      node.name.isPresent() ? node.name.get() : "lambda expression",
				  MAX_PARAMETER_NUMBER);
		}
		for (Stmt stmt : node.body.stmts) {
			stmt.accept(this);
		}
		curFunctionType = origin;
		return null;
	}

	@Override
	public Void accept(Stmt.Return node) {
		super.accept(node);
		switch (curFunctionType) {
			case NONE:
				error(node, "The 'return' outside function.");
				break;
			
			case INIT:
				if (node.expr.isPresent())
					error(node, "Can't return from constructor.");
				break;
		}
		return null;
	}
	
	/*===================== check expression ==================*/

	@Override
	public Void accept(Expr.CallFunc node) {
		super.accept(node);
		checkCallable(node.expr);
		return null;
	}
	
	private void checkCallable(Expr expr) {
		if (expr instanceof Expr.VarRef ||
		    expr instanceof Expr.CallFunc ||
			expr instanceof Expr.Assign ||
			expr instanceof Expr.GetAttr ||
			expr instanceof Expr.Super) {
			return ;
		}
		error(expr, "The expression is not callable.");
	}

	@Override
	public Void accept(Expr.Super node) {
		if (!inClass) {
			error(node, "The 'super' outside class.");
		}
		return null;
	}

	@Override
	public Void accept(Expr.This node) {
		if (!inClass) {
			error(node, "The 'this' outside class.");
		}
		return null;
	}
	
	@Override
	public Void accept(Expr.IntegerLiteral node) {
		return null;
	}

	@Override
	public Void accept(Expr.DoubleLiteral node) {
		return null;
	}

	@Override
	public Void accept(Expr.StringLiteral node) {
		return null;
	}

	@Override
	public Void accept(Expr.BooleanLiteral node) {
		return null;
	}

	@Override
	public Void accept(Expr.NullLiteral node) {
		return null;
	}

	@Override
	public Void accept(Expr.VarRef node) {
		return null;
	}

}
