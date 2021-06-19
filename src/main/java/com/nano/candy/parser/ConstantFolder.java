package com.nano.candy.parser;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Expr;
import com.nano.candy.std.StringFunctions;
import com.nano.candy.utils.Position;

public class ConstantFolder {

	private static final boolean DEBUG_CONSTANT_FOLDING_TRACE = false;

	private static void reportDebug(ASTreeNode node, String message, Object... args) {
		Position pos = node.pos;
		System.out.printf("%s at [%d | %s]\n", 
			String.format(message, args), 
			pos.getLine(), pos.getLineFromSource().get()
		);
	}

	private static boolean isString(Expr expr) {
		return expr instanceof Expr.StringLiteral;
	}

	private static boolean isInteger(Expr expr) {
		return expr instanceof Expr.IntegerLiteral;
	}

	private static boolean isDouble(Expr expr) {
		return expr instanceof Expr.DoubleLiteral;
	}

	private static long intVal(Expr expr) {
		return ((Expr.IntegerLiteral)expr).value;
	}

	private static Expr.BooleanLiteral makeBooleanNode(boolean value, ASTreeNode node) {
		Expr.BooleanLiteral ret = new Expr.BooleanLiteral(value);
		ret.pos = node.pos;
		return ret;
	}

	private static Expr.StringLiteral makeStrLiteralNode(String str, ASTreeNode node) {
		Expr.StringLiteral ret = new Expr.StringLiteral(str);
		ret.pos = node.pos;
		return ret;
	}

	/**
	 * Try to fold unary expression.
	 */
	public static Expr foldUnaryExpr(Expr.Unary unaryExpr) {
		TokenKind operator = unaryExpr.operator;
		Expr expr = unaryExpr.expr;
		if (!expr.isConstant()) {
			return unaryExpr;
		}

		Expr ret = foldUnaryExpr(operator, expr);
		if (ret == null) {
			return unaryExpr;
		}
		if (DEBUG_CONSTANT_FOLDING_TRACE) {
			reportDebug(unaryExpr, "eval %s%s -> %s", 
						operator.getLiteral(), expr, ret);
		}
		return ret;
	}

	private static Expr foldUnaryExpr(TokenKind operator, Expr unaryExpr) {
		switch (operator) {
			case PLUS:
				if (unaryExpr.isNumber()) 
					return unaryExpr;
				break;
			case MINUS:
				if (unaryExpr.isNumber())
					return negative(unaryExpr);
				break;
			case NOT:
				return makeBooleanNode(unaryExpr.isFalsely(), unaryExpr);
		}
		return null;
	}

	private static Expr negative(Expr expr) {
		if (isInteger(expr)) {
			Expr.IntegerLiteral integer = (Expr.IntegerLiteral) expr;
			integer.value = -integer.value;
			return integer;
		} else if (isDouble(expr)) {
			Expr.DoubleLiteral doubl = (Expr.DoubleLiteral) expr;
			doubl.value = -doubl.value;
			return doubl;
		}
		return null;
	}

	/**
	 * Try to fold binary expression.
	 */
	public static Expr foldBinaryExpr(Expr.Binary binaryExpr) {
		Expr left = binaryExpr.left;
		Expr right = binaryExpr.right;
		TokenKind operator = binaryExpr.operator;

		// left expression must be a constant
		if (!left.isConstant()) {
			return binaryExpr;
		}

		Expr ret = foldBinaryExpr(left, operator, right);
		if (ret == null) { 
			return binaryExpr;
		}
		if (DEBUG_CONSTANT_FOLDING_TRACE) {
			reportDebug(binaryExpr, "eval (%s %s %s) -> %s",
						left, operator.getLiteral(), right, ret);
		}
		return ret;
	}

	private static Expr foldBinaryExpr(Expr left, TokenKind operator, Expr right) {

		if (TokenKind.isLogicalOperator(operator)) {
			return evalLogicalOperation(left, operator, right);
		}

		if (!right.isConstant()) return null;

		if (left.isNumber() && right.isNumber()) {
			Expr.Number leftN = (Expr.Number) left;
			Expr.Number rightN = (Expr.Number) right;
			return evalNumber(leftN, operator, rightN);
		}
		if (isString(left) || isString(right)) {
			Expr ret = evalString(left, operator, right);
			if (ret != null) return ret;
		}

		switch (operator) {
			case NOT_EQUAL:
				return makeBooleanNode(!left.equals(right), left);
			case EQUAL:
				return makeBooleanNode(left.equals(right), left);
			case IS:
				return makeBooleanNode(left.getClass() == right.getClass(), left);
		}
		return null;
	}

	private static Expr evalLogicalOperation(Expr left, TokenKind operator, Expr right) {
		switch (operator) {
			case LOGICAL_AND:
				return left.isFalsely() ? left : right;		
			case LOGICAL_OR:
				return left.isFalsely() ? right : left;							
			default:
				throw new Error("Unknown logical operator: " + operator.getLiteral());
		}
	}

	private static Expr evalNumber(Expr.Number left, TokenKind operator, Expr.Number right) {
		double leftV = left.value();
		double rightV = right.value();
		Expr ret = null;
		switch (operator) {
			case PLUS:
				leftV += rightV;
				break;
			case MINUS:
				leftV -= rightV;
				break;
			case STAR:
				leftV *= rightV;
				break;
			case DIV:
				if (rightV == 0) return null;
				leftV /= rightV;
				break;
			case MOD:
				leftV %= rightV;
				break;
			case EQUAL:
				return makeBooleanNode(leftV == rightV, left);
			case NOT_EQUAL:
				return makeBooleanNode(leftV != rightV, left);
			case GT:
				return makeBooleanNode(leftV > rightV, left);
			case GTEQ:
				return makeBooleanNode(leftV >= rightV, left);
			case LT:
				return makeBooleanNode(leftV < rightV, left);
			case LTEQ:
				return makeBooleanNode(leftV <= rightV, left);
			case IS:
				return makeBooleanNode(left.getClass() == right.getClass(), left);
			default:
				throw new Error("Unknown binary operator: " + operator.getLiteral());
		}

		if (isDouble(left) || isDouble(right)) {
			ret = new Expr.DoubleLiteral(leftV); 
		} else {
			ret = new Expr.IntegerLiteral((long)leftV);
		}
		ret.pos = left.pos;

		return ret;
	}

	private static Expr evalString(Expr left, TokenKind operator, Expr right) {
		switch (operator) {
			case PLUS:
				String l = left.toString();
				String r = right.toString();
				return makeStrLiteralNode(l + r, left);		
			case STAR:
				if (isString(left) && isInteger(right)) {
					String str = StringFunctions.repeat(left.toString(), intVal(right));
					return makeStrLiteralNode(str, left);
				}
				break;
		}
		return null;
	}

}
