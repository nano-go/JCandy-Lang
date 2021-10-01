package com.nano.candy.ast.dumper;

import com.nano.candy.ast.ASTreeNode;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * @see JsonAstDumper
 * @see SerializableDumper
 */
public abstract class AstDumper {
	
	/**
	 * Converts the given node into the string representation in the sepcial
	 * format and write it into the specified output stream (in options).
	 */
	public abstract void dump(DumperOptions options, ASTreeNode node);
	
	/**
	 * Returns the string representation of the specified AST node.
	 *
	 * {@code toString(new DumperOptions(), node);}
	 *
	 * @see #toString(DumperOptions, ASTreeNode)
	 */
	public String toString(ASTreeNode node) {
		return toString(new DumperOptions(), node);
	}
	
	/**
	 * Returns the string representation of the specified AST node.
	 *
	 * <p> This method will not change the output of the options.
	 *
	 * @see #dump(DumperOptions, ASTreeNode)
	 */
	public String toString(DumperOptions options, ASTreeNode node) {
		Writer out = options.getOut();
		StringWriter str = new StringWriter();
		options.setOut(str);
		try {
			dump(options, node);
			return str.toString();
		} finally {
			options.setOut(out);
		}
	}
}
