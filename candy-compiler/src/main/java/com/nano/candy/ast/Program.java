package com.nano.candy.ast;

import com.nano.candy.utils.Position;
import java.util.Optional;

public class Program extends ASTreeNode {
	
	public Stmt.Block block;
	public Optional<String> docComment;

	public Program() {
		this(new Stmt.Block());
	}
	
	public Program(Stmt.Block block) {
		this.block = block;
		this.docComment = Optional.empty();
	}
	
	public void setPosition(Position basePos) {
		this.block.pos = basePos;
		this.pos = basePos;
	}
	
	public void accept(AstVisitor visitor) {
		visitor.visit(this);
	}

}
