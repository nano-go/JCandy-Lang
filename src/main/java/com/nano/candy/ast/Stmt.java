package com.nano.candy.ast;

import com.nano.candy.ast.printer.FieldName;
import com.nano.candy.utils.Position;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Iterator;

public abstract class Stmt extends ASTreeNode {
	
	public abstract <S> S accept(AstVisitor<S, ?> visitor);
	
	public static class ErrorStmt extends Stmt {
		@Override
		public <S extends Object> S accept(AstVisitor<S, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static abstract class StmtList extends Stmt {	
		public List<Stmt> stmts;

		public StmtList(List<Stmt> list) {
			this.stmts = list;
		}
		
		public int size() {
			return stmts.size();
		}
		
		public boolean isEmpty() {
			return stmts.isEmpty();
		}
		
		public Stmt getFirstStmt() {
			return stmts.get(0);
		}
		
		public Stmt getLastStmt() {
			if (stmts.isEmpty()) {
				return null;
			}
			return stmts.get(stmts.size()-1);
		}
		
		public Stmt getAt(int index) {
			return stmts.get(index);
		}
	}
	
	public static class ImportList extends Stmt {

		public List<Import> importStmts;
		
		public ImportList(List<Import> importStmts) {
			this.importStmts = importStmts;
		}
		
		@Override
		public <S extends Object> S accept(AstVisitor<S, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class Import extends Stmt {
		public Expr fileExpr;
		public String asIdentifier;

		public Import(Expr fileExpr, String identifier) {
			this.fileExpr = fileExpr;
			this.asIdentifier = identifier;
		}
		@Override
		public <S extends Object> S accept(AstVisitor<S, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
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
		
	public static class TryIntercept extends Stmt {

		public Block tryBlock;
		public Optional<Block> elseBlock;
		public Optional<Block> finallyBlock;
		public List<Interception> interceptionBlocks;

		public TryIntercept(Block tryBlock, List<Interception> interceptBlocks, 
		                    Block elseBlock, Block finallyBlock) {
			this.tryBlock = tryBlock;
			this.elseBlock = Optional.ofNullable(elseBlock);
			this.finallyBlock = Optional.ofNullable(finallyBlock);
			if (interceptBlocks == null) {
				this.interceptionBlocks = Collections.emptyList();
			} else {
				this.interceptionBlocks = interceptBlocks;
			}
		}
		
		@Override
		public <S extends Object> S accept(AstVisitor<S, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class Interception extends Stmt {
		public List<Expr> exceptions;
		public Optional<String> name;
		public Block block;

		public Interception(List<Expr> exceptions, String name, Block block) {
			this.exceptions = exceptions == null ? 
				Collections.emptyList() : exceptions;
			this.name = Optional.ofNullable(name);
			this.block = block;
		}
		
		@Override
		public <S extends Object> S accept(AstVisitor<S, ?> visitor) {
			return visitor.visit(this);
		}	
	}
	
	public static class Raise extends Stmt {
		public Expr exceptionExpr;

		public Raise(Expr exceptionExpr) {
			this.exceptionExpr = exceptionExpr;
		}
		
		@Override
		public <S extends Object> S accept(AstVisitor<S, ?> visitor) {
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
		public Parameters parameters;
		public Stmt.Block body;
		
		public FuncDef(String name, Parameters parameters, 
					   Stmt.Block body) {
			this.name = Optional.ofNullable(name) ;
			this.parameters = parameters;
			this.body = body;
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class Parameters {
		public final List<String> params;
		
		/**
		 * The parameter in the vaArgsIndex allows that variable arguments 
		 * are passed to it.
		 */
		public final int vaArgsIndex;
		
		public Parameters(List<String> params, int isVaArgs) {
			this.params = params;
			this.vaArgsIndex = isVaArgs;
		}
		
		public int size() {
			return params.size();
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
		
		public Optional<Position> endPos;

		public ClassDef(String name, Expr.VarRef superClassName, 
		                List<Stmt.FuncDef> methods) {
			this.name = name;
			this.methods = methods;
			this.superClassName = Optional.ofNullable(superClassName);
			this.initializer = Optional.empty();
			this.endPos = Optional.ofNullable(null);
		}

		public int constructorParamNumber() {
			return initializer.isPresent() ? initializer.get().parameters.size() : 0;
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
	
	public static class Block extends StmtList {
		public Optional<Position> endPos;
		
		public Block() {
			this(new LinkedList<Stmt>());
		}

		public Block(List<Stmt> stmts) {
			super(stmts);
			endPos = Optional.empty();
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}	
	}
}
