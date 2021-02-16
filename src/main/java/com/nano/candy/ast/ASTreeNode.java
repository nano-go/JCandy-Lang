package com.nano.candy.ast;
import com.nano.candy.utils.Position;

public abstract class ASTreeNode {
	
	public Position pos;
	public Position pos() {
		return pos;
	}
	
	public static <E> E visitExpr(Expr expr, AstVisitor<?, E> visitor) {
		return expr.accept(visitor);
	}
	
	public static <S> S visitStmt(Stmt stmt, AstVisitor<S, ?> visitor) {
		return stmt.accept(visitor);
	}
	
	public static void accept(ASTreeNode node, AstVisitor visitor) {
		if (node instanceof Expr) {
			visitExpr((Expr)node, visitor);
			return;
		}
		if (node instanceof Stmt) {
			visitStmt((Stmt)node, visitor);
			return;
		}
		((Program)node).accept(visitor);
	}
	
}
