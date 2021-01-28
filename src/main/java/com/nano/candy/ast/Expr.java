package com.nano.candy.ast;
import com.nano.candy.parser.Token;
import com.nano.candy.parser.TokenKind;
import com.nano.candy.utils.Position;
import java.util.List;

public abstract class Expr extends ASTreeNode {
	
	public static class Lambda extends Expr {
		
		public Stmt.FuncDef funcDef;

		public Lambda(List<String> params, Stmt.Block body) {
			this.funcDef = new Stmt.FuncDef(null, params, body);
			this.funcDef.pos = pos;
		}
		
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class Assign extends Expr {
		public String name;
		public TokenKind assignOperator;
		public Expr rhs;

		public Assign(String name, TokenKind assignOperator, Expr rhs) {
			this.name = name;
			this.assignOperator = assignOperator;
			this.rhs = rhs;
		}
		
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class Binary extends Expr {
		public TokenKind operator;
		public Position operatorPos;
		public Expr left;
		public Expr right;

		public Binary(Expr left, Token opetator, Expr right) {
			this.left = left;
			this.right = right;
			this.operator = opetator.getKind();
			this.operatorPos = opetator.getPos();
			this.pos = left.pos;
		}
		
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class Unary extends Expr {
		public TokenKind operator;
		public Expr expr;

		public Unary(TokenKind opetator, Expr expr) {
			this.operator = opetator;
			this.expr = expr;
		}

		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class StringLiteral extends Expr {
		public String literal;

		public StringLiteral(String literal) {
			this.literal = literal;
		}
		
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}	
	}
	
	public static class DoubleLiteral extends Expr {
		public double value;
		
		public DoubleLiteral(String literal) {
			this(Double.valueOf(literal));
		}

		public DoubleLiteral(double literal) {
			this.value = literal;
		}
		
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class IntegerLiteral extends Expr {
		public long value;

		public IntegerLiteral(String literal) {
			this(Long.valueOf(literal));
		}

		public IntegerLiteral(long literal) {
			this.value = literal;
		}

		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class BooleanLiteral extends Expr {
		public boolean value;

		public BooleanLiteral(boolean value) {
			this.value = value;
		}
		
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class NullLiteral extends Expr {
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class Array extends Expr {
		
		public List<Expr> elements;

		public Array(List<Expr> elements) {
			this.elements = elements;
		}
		
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class This extends Expr {
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}	
	}
	
	public static class Super extends Expr {
		public String reference;

		public Super(String reference) {
			this.reference = reference;
		}

		@Override
		public <R extends Object> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class VarRef extends Expr {
		public String name;
		
		public VarRef(String identifier) {
			this.name = identifier;
		}
		
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class CallFunc extends Expr {
		public Expr expr;
		public List<Expr> arguments;
		
		public CallFunc(Expr expr, List<Expr> arguments) {
			this.expr = expr;
			this.arguments = arguments;
		}

		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class SetAttr extends Expr {
		public GetAttr objExpr;
		public TokenKind assignOperator;
		public Expr rhs;

		public SetAttr(GetAttr objExpr, TokenKind assignOperator, Expr rhs) {
			this.objExpr = objExpr;
			this.assignOperator = assignOperator;
			this.rhs = rhs;
		}

		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class GetAttr extends Expr {
		public Expr objExpr;
		public String attr;

		public GetAttr(Expr objExpr, String attr) {
			this.objExpr = objExpr;
			this.attr = attr;
		}
		
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class SetItem extends Expr {
		public GetItem objExpr;
		public TokenKind assignOperator;
		public Expr rhs;

		public SetItem(GetItem objExpr, TokenKind assignOperator, Expr rhs) {
			this.objExpr = objExpr;
			this.assignOperator = assignOperator;
			this.rhs = rhs;
		}

		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
	
	public static class GetItem extends Expr {
		public Expr objExpr;
		public Expr key;

		public GetItem(Expr objExpr, Expr key) {
			this.objExpr = objExpr;
			this.key = key;
		}
		
		@Override
		public <R> R accept(AstVisitor<R> visitor) {
			return visitor.accept(this);
		}
	}
}
