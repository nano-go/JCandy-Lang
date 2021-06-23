package com.nano.candy.ast;
import com.nano.candy.parser.Token;
import com.nano.candy.parser.TokenKind;
import com.nano.candy.std.StringFunctions;
import com.nano.candy.utils.Position;
import java.util.List;
import java.util.Collections;

public abstract class Expr extends ASTreeNode {
	
	public abstract <E> E accept(AstVisitor<?, E> visitor);
	
	public boolean isLiteral() {
		return false;
	}
	
	public boolean isFalsely() {
		return false;
	}
	
	public boolean isNumber() {
		return false;
	}
	
	public boolean isConstant() {
		return false;
	}
	
	protected static abstract class Literal extends Expr {
		@Override
		public boolean isLiteral() {
			return true;
		}

		@Override
		public boolean isConstant() {
			return true;
		}
	}
	
	public static abstract class Number extends Literal {
		@Override
		public boolean isNumber() {
			return true;
		}
		
		public abstract boolean isDouble();
		public abstract double value();

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Number) {
				return value() == ((Number) obj).value();
			}
			return super.equals(obj);
		}
	}
	
	public static class Lambda extends Expr {		
		public Stmt.FuncDef funcDef;

		public Lambda(Stmt.Parameters params, Stmt.Block body) {
			this.funcDef = new Stmt.FuncDef(
				null, params, body
			);
		}
		
		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
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
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class TernaryOperator extends Expr {
		
		public Expr condition;
		public Expr thenExpr;
		public Expr elseExpr;

		public TernaryOperator(Expr condition, Expr thenExpr, Expr elseExpr) {
			this.condition = condition;
			this.thenExpr = thenExpr;
			this.elseExpr = elseExpr;
			this.pos = condition.pos;
		}
		
		@Override
		public <E extends Object> E accept(AstVisitor<?, E> visitor) {
			return visitor.visit(this);
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
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
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
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class StringLiteral extends Literal {
		public String literal;

		public StringLiteral(String literal) {
			this.literal = literal;
		}

		@Override
		public boolean isFalsely() {
			return false;
		}
		
		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}

		@Override
		public String toString() {
			return literal;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof StringLiteral) {
				return StringFunctions.equals(
					literal, ((StringLiteral) obj).literal
				);
			}
			return super.equals(obj);
		}
	}
	
	public static class DoubleLiteral extends Number {
		public double value;

		public DoubleLiteral(double value) {
			this.value = value;
		}
		
		public DoubleLiteral(Token tok) {
			this.value = ((Token.DoubleNumberToken)tok).getValue();
		}
		
		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}
		
		@Override
		public boolean isFalsely() {
			return false;
		}

		@Override
		public boolean isDouble() {
			return true;
		}
		
		@Override
		public double value() {
			return value;
		}

		@Override
		public String toString() {
			return StringFunctions.valueOf(value);
		}
	}
	
	public static class IntegerLiteral extends Number {
		public long value;

		public IntegerLiteral(long value) {
			this.value = value;
		}

		public IntegerLiteral(Token tok) {
			this.value = ((Token.IntegerNumberToken)tok).getValue();
		}

		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}
		
		@Override
		public boolean isFalsely() {
			return false;
		}

		@Override
		public boolean isDouble() {
			return false;
		}

		@Override
		public double value() {
			return value;
		}

		@Override
		public String toString() {
			return StringFunctions.valueOf(value);
		}
	}
	
	public static class BooleanLiteral extends Literal {
		public boolean value;

		public BooleanLiteral(boolean value) {
			this.value = value;
		}
		
		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}

		@Override
		public boolean isFalsely() {
			return !value;
		}

		@Override
		public String toString() {
			return StringFunctions.valueOf(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BooleanLiteral) {
				return value == ((BooleanLiteral) obj).value;
			}
			return super.equals(obj);
		}
	}
	
	public static class NullLiteral extends Literal {
		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}

		@Override
		public boolean isFalsely() {
			return true;
		}

		@Override
		public String toString() {
			return StringFunctions.nullStr();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NullLiteral) {
				return true;
			}
			return super.equals(obj);
		}
	}
	
	public static class Array extends Literal {
		
		public List<Expr> elements;

		public Array(List<Expr> elements) {
			this.elements = elements;
		}
		
		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}

		@Override
		public boolean isConstant() {
			return false;
		}
	}
	
	public static class Tuple extends Literal {

		public List<Expr> elements;

		public Tuple(List<Expr> elements) {
			this.elements = elements;
		}

		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}

		@Override
		public boolean isConstant() {
			return false;
		}
	}
	
	public static class Map extends Literal {
		
		public List<Expr> keys;
		public List<Expr> values;
		
		public Map() {
			this(
				Collections.<Expr>emptyList(),
				Collections.<Expr>emptyList()
			);
		}

		public Map(List<Expr> keys, List<Expr> values) {
			this.keys = keys;
			this.values = values;
		}
		
		@Override
		public <R extends Object> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}
		
		@Override
		public boolean isConstant() {
			return false;
		}
	}
	
	public static class This extends Expr {
		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}	
	}
	
	public static class Super extends Expr {
		public String reference;

		public Super(String reference) {
			this.reference = reference;
		}

		@Override
		public <R extends Object> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class VarRef extends Expr {
		public String name;
		
		public VarRef(String identifier) {
			this.name = identifier;
		}
		
		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class CallFunc extends Expr {
		public Expr expr;
		public List<Argument> arguments;
		
		public CallFunc(Expr expr, List<Argument> arguments) {
			this.expr = expr;
			this.arguments = arguments;
		}
		
		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class Argument {
		public Expr expr;
		public boolean isUnpack;

		public Argument(Expr expr, boolean isUnpacking) {
			this.expr = expr;
			this.isUnpack = isUnpacking;
		}
	}
	
	public static class SetAttr extends Expr {
		public Expr objExpr;
		public String attr;
		public TokenKind assignOperator;
		public Expr rhs;

		public SetAttr(GetAttr getAttrNode, TokenKind assignOperator, Expr rhs) {
			this.objExpr = getAttrNode.objExpr;
			this.attr = getAttrNode.attr;
			this.assignOperator = assignOperator;
			this.rhs = rhs;
		}
		
		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
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
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}
	}
	
	public static class SetItem extends Expr {
		public Expr objExpr;
		public Expr key;
		public TokenKind assignOperator;
		public Expr rhs;

		public SetItem(GetItem getItemNode, TokenKind assignOperator, Expr rhs) {
			this.objExpr = getItemNode.objExpr;
			this.key = getItemNode.key;
			this.assignOperator = assignOperator;
			this.rhs = rhs;
		}

		@Override
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
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
		public <R> R accept(AstVisitor<?, R> visitor) {
			return visitor.visit(this);
		}
	}
}
