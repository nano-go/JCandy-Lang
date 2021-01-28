package com.nano.candy.ast.printer;
import com.nano.candy.ast.ASTreeNode;
import java.io.PrintStream;

public abstract class AstPrinter {
	public abstract String toString(ASTreeNode node);
	public void print(PrintStream printStream, ASTreeNode node) {
		printStream.print(toString(node));
	}
}
