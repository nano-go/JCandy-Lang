package com.nano.candy.interpreter.i1.resolver;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.AbstractAstVisitor;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.config.Config;
import com.nano.candy.parser.TokenKind;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class CandyResolver extends AbstractAstVisitor<Void> {
	
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
		ASTreeNode.accept(node, this);
		return distances;
	}

	@Override
	public void visit(Program node) {
		super.visit(node);
	}

	@Override
	public Void visit(Stmt.Block node) {
		enterScope();
		super.visit(node);
		exitScope();
		return null;
	}

	@Override
	public Void visit(Stmt.For node) {
		node.iterable.accept(this);
		enterScope();
		define(node, node.iteratingVar);
		for (Stmt stmt : node.body.stmts) {
			stmt.accept(this);
		}
		exitScope();
		return null;
	}

	@Override
	public Void visit(Stmt.ClassDef node) {
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
			initializer.accept(this);
			define(initializer, initializer.name.get());
		}
		for (Stmt.FuncDef func : node.methods) {
			func.accept(this);
		}
		
		exitScope();
		exitScope();
		return null;
	}

	@Override
	public Void visit(Stmt.FuncDef node) {
		if (node.name.isPresent()) {
			define(node, node.name.get());
		}
		enterScope();
		for (String param : node.params) {
			define(node, param);
		}
		for (Stmt stmt : node.body.stmts) {
			stmt.accept(this);
		}
		exitScope();
		return null;
	}

	@Override
	public Void visit(Stmt.VarDef node) {
		super.visit(node);
		define(node, node.name);
		return null;
	}
	
	@Override
	public Void visit(Expr.Assign node) {
		super.visit(node);
		resolveLocals(node.name, node);
		return null;
	}
	
	@Override
	public Void visit(Expr.VarRef node) {
		resolveLocals(node.name, node);
		return null;
	}

	@Override
	public Void visit(Expr.Super node) {
		resolveLocals(TokenKind.SUPER.getLiteral(), node);
		return null;
	}
	
	@Override
	public Void visit(Expr.This node) {
		resolveLocals(TokenKind.THIS.getLiteral(), node);
		return null;
	}
	
	@Override
	public Void visit(Stmt.Continue node) { return null; }
	@Override
	public Void visit(Stmt.Break node) { return null; }
	@Override
	public Void visit(Expr.DoubleLiteral node) { return null; }
	@Override
	public Void visit(Expr.IntegerLiteral node) { return null; }
	@Override
	public Void visit(Expr.StringLiteral node) { return null; }
	@Override
	public Void visit(Expr.BooleanLiteral node) { return null; }
	@Override
	public Void visit(Expr.NullLiteral node) { return null; }
}
