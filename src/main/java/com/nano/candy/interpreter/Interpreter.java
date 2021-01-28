package com.nano.candy.interpreter;
import com.nano.candy.ast.ASTreeNode;

public interface Interpreter {
	public void initOrReset();
	public void resolve(ASTreeNode node);
	public boolean run(ASTreeNode node, boolean isInteratively);
}
