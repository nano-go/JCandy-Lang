package com.nano.candy.ast.dumper;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Stmt;
import com.nano.candy.parser.TokenKind;
import com.nano.candy.utils.Characters;
import com.nano.candy.utils.Position;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonAstDumper extends SerializableDumper {

	private static String wrapString(String str) {
		return "\"" + str + "\"";
	}
	
	private int indent = 0;
	private String indentStr = "\t";
	private boolean dumpPosition;
	
	private String indent() {
		return indentStr.repeat(indent);
	}
	
	private String toEscapeChars(String str) {
		StringBuilder chars = new StringBuilder();
		for (char c : str.toCharArray()) {
			chars.append(Characters.toEscapeChar(c));
		}
		return chars.toString();
	}

	@Override
	public void dump(DumperOptions options, ASTreeNode node) {
		this.indentStr = options.getIndent();
		this.dumpPosition = options.isDumpPosition;
		super.dump(options, node);
	}
	
	@Override
	protected String serialize(TreeNode node) {
		JsonStrBuilder builder = new JsonStrBuilder();
		indent ++;
		builder.append("{");
		builder.putKV("node_name", node.getNodeName(), false);
		builder.putKV("position", node.getPos(), true);
		for (Map.Entry<String, Object> entry : node) {
			
			builder.putKV(entry.getKey(), entry.getValue(), true);
		}
		indent --;
		builder.append("\n").append(indent()).append("}").toString();
		return builder.toString();
	}
	
	@Override
	protected String serialize(Position pos) {
		JsonStrBuilder builder = new JsonStrBuilder();
		indent ++;
		builder.append("{");
		builder.putKV("line", pos.getLine(), false);
		builder.putKV("col", pos.getCol(), true);
		if (pos.getLineFromSource().isPresent()) {
			builder.putKV("line_text", pos.getLineFromSource().get(), true);
		}
		indent --;
		builder.append("\n").append(indent()).append("}").toString();
		return builder.toString();
	}

	@Override
	protected String serialize(TokenKind tk) {
		return wrapString(tk.getLiteral());
	}

	@Override
	protected String serialize(Expr.Argument obj) {
		JsonStrBuilder builder = new JsonStrBuilder();
		indent ++;
		builder.append("{");
		builder.putKV("isUnpack", obj.isUnpack, false);
		builder.putKV("arg", obj.expr, true);
		indent --;
		builder.append("\n").append(indent()).append("}").toString();
		return builder.toString();
	}

	@Override
	protected String serialize(Stmt.Parameters obj) {
		JsonStrBuilder builder = new JsonStrBuilder();
		indent ++;
		builder.append("{");
		builder.putKV("vaArgsIndex", obj.vaArgsIndex, false);
		builder.putKV("params", obj.params, true);
		indent --;
		builder.append("\n").append(indent()).append("}").toString();
		return builder.toString();
	}
	
	@Override
	protected String serialize(String str) {
		return wrapString(toEscapeChars(str));
	}

	@Override
	protected String serialize(List list) {
		return new JsonStrBuilder()
			.putIterator(list.iterator())
			.toString();	
	}

	@Override
	protected String serialize(Object[] array) {
		return new JsonStrBuilder()
			.putIterator(Arrays.asList(array).iterator())
			.toString();
	}
	
	private class JsonStrBuilder {

		private StringBuilder builder;

		public JsonStrBuilder() {
			builder = new StringBuilder();
		}

		private JsonStrBuilder append(String str) {
			builder.append(str);
			return this;
		}

		private JsonStrBuilder putIterator(Iterator iterator) {	
			indent ++;
			builder.append("[");
			if (iterator.hasNext()) {
				putElement(iterator.next(), false);
				while (iterator.hasNext()) {
					putElement(iterator.next(), true);
				}
			}
			indent --;
			builder.append("\n").append(indent()).append("]");
			return this;
		}

		public void putKV(String key, Object value, boolean comma) {
			if (!dumpPosition && value instanceof Position) {
				return;
			}
			builder.append(comma ? "," : "")
				.append("\n")
				.append(indent())
				.append(wrapString(key))
				.append(": ")
				.append(accept(value));
		}

		public void putElement(Object element, boolean comma) {
			builder.append(comma ? "," : "")
				.append("\n")
				.append(indent())
				.append(accept(element));
		}

		@Override
		public String toString() {
			return builder.toString();
		}
	}
}
