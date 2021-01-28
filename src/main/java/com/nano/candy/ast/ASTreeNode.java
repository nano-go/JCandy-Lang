package com.nano.candy.ast;
import com.nano.candy.utils.Position;

public abstract class ASTreeNode {
	
	public Position pos;
	public Position pos() {
		return pos;
	}
	
	public abstract <R> R accept(AstVisitor<R> visitor);
}
