package com.nano.candy.ast;

import com.nano.candy.utils.Position;

public class Program extends ASTreeNode {

	public Stmt.Block block ;

	public Program() {
		this.block = new Stmt.Block() ;
	}
	
	public Program(Stmt.Block block) {
		this.block = block;
	}
	
	public void setPosition(Position basePos) {
		this.block.pos = basePos ;
		this.pos = basePos ;
	}
	
	@Override
	public <R> R accept(AstVisitor<R> visitor) {
		return visitor.accept(this) ;
	}

}
