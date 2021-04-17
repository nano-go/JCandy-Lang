package com.nano.candy.interpreter;
import com.nano.candy.ast.ASTreeNode;

public interface Interpreter {
	public void enter(InterpreterOptions options);
	public void initOrReset();
	public void load(String text);
	public void load(ASTreeNode node);
	public boolean run();
}
