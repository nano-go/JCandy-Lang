package com.nano.candy.interpreter;
import com.nano.candy.ast.ASTreeNode;

public interface Interpreter {
	public void initOrReset();
	public void load(ASTreeNode node);
	public boolean run(boolean isInteratively);
}
