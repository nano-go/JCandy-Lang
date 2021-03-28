package com.nano.candy.interpreter;
import com.nano.candy.ast.ASTreeNode;

public interface Interpreter {
	public void initOrReset();
	
	/**
	 * Callback when this program normally ends(effective only 
	 * in non-interation mode).
	 */
	public void onExit();
	
	public void load(ASTreeNode node, boolean interatively);
	public boolean run();
}
