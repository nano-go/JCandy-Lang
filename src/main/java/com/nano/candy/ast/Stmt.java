package com.nano.candy.ast;

import com.nano.candy.ast.printer.FieldName;
import com.nano.candy.utils.Position;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class Stmt extends ASTreeNode {
	
	public abstract <S> S accept(AstVisitor<S, ?> visitor);
	
	public static class Continue extends Stmt {
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class Break extends Stmt {
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class Return extends Stmt {
		public Optional<Expr> expr;

		public Return(Expr expr) {
			this.expr = Optional.ofNullable(expr);
		}
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class While extends Stmt {
		public Expr condition;
		public Stmt body;

		public While(Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class For extends Stmt {
		public String iteratingVar;
		public Expr iterable;
		public Stmt.Block body;

		public For(String iteratingVar, Expr iterable, Stmt.Block body) {
			this.iteratingVar = iteratingVar;
			this.iterable = iterable;
			this.body = body;
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class If extends Stmt {
		public Expr condition;
		public Stmt thenBody;
		public Optional<Stmt> elseBody;

		public If(Expr condition, Stmt thenBody, Stmt elseBody) {
			this.condition = condition;
			this.thenBody = thenBody;
			this.elseBody = Optional.ofNullable(elseBody);
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class Assert extends Stmt {
		public Expr condition;
		public Expr errorInfo;
		
		public Assert(Position pos, Expr condition, Expr errorInfo) {
			this.condition = condition;
			this.pos = pos;
			if (errorInfo == null) {
				errorInfo = new Expr.StringLiteral("");
				errorInfo.pos = pos;
			}
			this.errorInfo = errorInfo;
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class FuncDef extends Stmt {
		public Optional<String> name;
		public List<String> params;
		public Stmt.Block body;
		
		public FuncDef(String name, List<String> params, Stmt.Block body) {
			this.name = Optional.ofNullable(name) ;
			this.params = params;
			this.body = body;
		}

		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class VarDef extends Stmt {
		public String name;
		public Optional<Expr> init;
		
		public VarDef(String name) {
			this(name, null);
		}

		public VarDef(String name, Expr init) {
			this.name = name;
			this.init = Optional.ofNullable(init);
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class ClassDef extends Stmt {

		@FieldName("className")
		public String name;
		public Optional<Expr.VarRef> superClassName;
		public Optional<Stmt.FuncDef> initializer;
		public List<Stmt.FuncDef> methods;

		public ClassDef(String name, Expr.VarRef superClassName, 
		                List<Stmt.FuncDef> methods) {
			this.name = name;
			this.methods = methods;
			this.superClassName = Optional.ofNullable(superClassName);
			this.initializer = Optional.empty();
		}

		public int constructorParamNumber() {
			return initializer.isPresent() ? initializer.get().params.size() : 0;
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}

	public static class ExprS extends Stmt {
		public Expr expr;
		
		public ExprS(Expr expr) {
			this.expr = expr;
			this.pos = expr.pos;
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class Block extends Stmt {
		public List<Stmt> stmts;
		
		public Block() {
			this(new LinkedList<Stmt>());
		}

		public Block(List<Stmt> stmts) {
			this.stmts = stmts;
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}	
	}
}
