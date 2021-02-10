package com.nano.candy.interpreter.i1.resolver;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.comp.Checker;
import com.nano.candy.config.Config;
import com.nano.candy.parser.TokenKind;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class CandyResolver extends Checker {
	
	private Stack<HashSet<String>> scopes;
	private HashMap<Expr, Integer> distances;
	
	public CandyResolver() {
		this.distances = new HashMap<>();
		this.scopes = new Stack<>();
	}
	
	private void enterScope() {
		scopes.push(new HashSet<String>());
		if (Config.DEBUG) {
			System.out.println("Enter Scope");
		}
	}
	
	private void exitScope() {
		scopes.pop();
		if (Config.DEBUG) {
			System.out.println("Exit Scope");
		}
	}
	
	private void define(ASTreeNode node, String name) {
		if (!scopes.isEmpty()) {
			 if (scopes.peek().contains(name)) {
				 return;
			 }
			 scopes.peek().add(name);
			 if (Config.DEBUG) {
				 System.out.println("Define: " + name);
			 }
		}
	}
	
	private void resolveLocals(String name, Expr expr) {
		for (int i = scopes.size() - 1; i >= 0; i --) {
			if (scopes.get(i).contains(name)) {
				distances.put(expr, scopes.size() - i - 1);
				if (Config.DEBUG) {
					System.out.printf(
						"Resolve:\n\tName: %s, Distance: %d\n",
						name, scopes.size() - i - 1
					);
				}
				break;
			}
		}
	}
	
	public void reset() {
		distances.clear();
		scopes.clear();
	}
	
	public HashMap<Expr, Integer> resolve(ASTreeNode node) {
		node.accept(this);
		return distances;
	}

	@Override
	public Void accept(Program node) {
		super.accept(node);
		return null;
	}

	@Override
	public Void accept(Stmt.Block node) {
		enterScope();
		super.accept(node);
		exitScope();
		return null;
	}

	@Override
	public Void accept(Stmt.For node) {
		node.iterable.accept(this);
		enterScope();
		boolean origin = super.inLoop;
		super.inLoop = true;
		define(node, node.iteratingVar);
		for (Stmt stmt : node.body.stmts) {
			stmt.accept(this);
		}
		exitScope();
		super.inLoop = origin;
		return null;
	}

	@Override
	public Void accept(Stmt.ClassDef node) {
		define(node, node.name);
		
		if (node.superClassName.isPresent()) {
			node.superClassName.get().accept(this);
		}
		
		enterScope();
		define(node, TokenKind.SUPER.getLiteral());
		
		enterScope();
		define(node, TokenKind.THIS.getLiteral());
		
		if (node.initializer.isPresent()) {
			Stmt.FuncDef initializer = node.initializer.get();
			define(initializer, initializer.name.get());
		}
		super.accept(node);
		
		exitScope();
		exitScope();
		return null;
	}

	@Override
	public Void accept(Stmt.FuncDef node) {
		if (node.name.isPresent()) {
			define(node, node.name.get());
		}
		enterScope();
		for (String param : node.params) {
			define(node, param);
		}
		super.accept(node);
		exitScope();
		return null;
	}

	@Override
	public Void accept(Stmt.VarDef node) {
		super.accept(node);
		define(node, node.name);
		return null;
	}
	
	@Override
	public Void accept(Expr.Assign node) {
		super.accept(node);
		resolveLocals(node.name, node);
		return null;
	}
	
	@Override
	public Void accept(Expr.VarRef node) {
		resolveLocals(node.name, node);
		return null;
	}

	@Override
	public Void accept(Expr.Super node) {
		super.accept(node);
		resolveLocals(TokenKind.SUPER.getLiteral(), node);
		return null;
	}
	
	@Override
	public Void accept(Expr.This node) {
		super.accept(node);
		resolveLocals(TokenKind.THIS.getLiteral(), node);
		return null;
	}
	
	@Override
	public Void accept(Stmt.Continue node) { return null; }
	@Override
	public Void accept(Stmt.Break node) { return null; }
	@Override
	public Void accept(Expr.DoubleLiteral node) { return null; }
	@Override
	public Void accept(Expr.IntegerLiteral node) { return null; }
	@Override
	public Void accept(Expr.StringLiteral node) { return null; }
	@Override
	public Void accept(Expr.BooleanLiteral node) { return null; }
	@Override
	public Void accept(Expr.NullLiteral node) { return null; }
}
