package com.nano.candy.ast;

import com.nano.candy.std.CandyAttrSymbol;
import com.nano.candy.utils.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
	
	public static class Empty extends Stmt {

		@Override
		public <S extends Object> S accept(AstVisitor<S, ?> visitor) {
			return visitor.visit(this);
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
		public Expr modulePath;
		public Optional<String> asIdentifier;

		public Import(Expr modulePath, String identifier) {
			this.modulePath = modulePath;
			this.asIdentifier = Optional.ofNullable(identifier);
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
		public List<Interception> interceptionBlocks;

		public TryIntercept(Block tryBlock, List<Interception> interceptBlocks, 
		                    Block elseBlock) {
			this.tryBlock = tryBlock;
			this.elseBlock = Optional.ofNullable(elseBlock);
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
		public boolean isStaticFunc;
		
		public FuncDef(String name, Parameters parameters, 
					   Stmt.Block body) {
			this.name = Optional.ofNullable(name) ;
			this.parameters = parameters;
			this.body = body;
		}
		
		public FuncDef(boolean isStaticFunc, 
		               String name, Parameters parameters, 
					   Stmt.Block body) {
			this.isStaticFunc = isStaticFunc;
			this.name = Optional.ofNullable(name);
			this.parameters = parameters;
			this.body = body;
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class Parameters {
		
		public static Parameters empty() {
			return new Parameters(new ArrayList<String>(), -1);
		}
		
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
		public boolean isStatic;
		
		public VarDef(String name) {
			this(false, name, null);
		}

		public VarDef(boolean isStatic, String name, Expr init) {
			this.isStatic = isStatic;
			this.name = name;
			this.init = Optional.ofNullable(init);
		}
		
		@Override
		public <R> R accept(AstVisitor<R, ?> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class ClassDef extends Stmt {

		/**
		 * The declared name of this class.
		 */
		public String name;
		
		/**
		 * The optional super class.
		 */
		public Optional<Expr> superClass;
		
		/**
		 * The initializer of this class. Each class has only one 
		 * or zero initializer.
		 */
		public Optional<Stmt.FuncDef> initializer;
		
		/**
		 * The static block initalizes some attributes of this class
		 * object.
		 */
		public Optional<Block> staticBlock;
		
		/**
		 * The predefined some attributes with modifiers.
		 *
		 * <pre>
		 * class Foo {
		 *     pri a, b
		 * }
		 */
		public Set<CandyAttrSymbol> attrs;
		
		/**
		 * The method list.
		 */
		public List<Stmt.FuncDef> methods;
		
		/**
		 * The class is inside of the static block of other classes.
		 *
		 * <pre>
		 * For Example:
		 *     class Foo {
		 *         static class Bar {...}
		 *     }
		 * </pre>
		 *
		 * The {@code Foo} class will be a static class.
		 */
		public boolean isStaticClass;
		
		/**
		 * The character '}' position.
		 *
		 * <p> This position is used for the debugger.
		 */
		public Optional<Position> endPos;

		public ClassDef(boolean isStaticClass, 
		                String name, Expr superClass, 
		                List<Stmt.FuncDef> methods) {
			this(isStaticClass, name, superClass, methods, null);
		}
		
		public ClassDef(boolean isStaticClass, 
		                String name, Expr superClass, 
		                List<Stmt.FuncDef> methods, Block staticBlock) {
			this.isStaticClass = isStaticClass;
			this.name = name;
			this.methods = methods;
			this.attrs = Collections.emptySet();
			this.superClass = Optional.ofNullable(superClass);
			this.staticBlock = Optional.ofNullable(staticBlock);
			this.initializer = Optional.empty();
			this.endPos = Optional.ofNullable(null);
		}
		
		public void createNewStaticBlockIfNotPresent() {
			if (!this.staticBlock.isPresent()) {
				this.staticBlock = Optional.of(new Stmt.Block());
			}
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
			this(new ArrayList<Stmt>());
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
