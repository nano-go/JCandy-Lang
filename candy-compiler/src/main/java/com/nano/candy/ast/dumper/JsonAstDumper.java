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
	
	private int curIndentCount = 0;
	private String indentStr = "\t";
	private String curIndent = "";
	private boolean dumpPosition;
	
	private String indent() {
		return curIndent;
	}
	
	private void indentInc() {
		curIndentCount ++;
		curIndent = indentStr.repeat(curIndentCount);
	}
	
	private void indentDec() {
		curIndentCount --;
		curIndent = indentStr.repeat(curIndentCount);
	}
	
	/**
	 * "\n\r" -> "\\n\\r"
	 */
	private String toEscapeChars(String str) {
		StringBuilder chars = new StringBuilder();
		for (char c : str.toCharArray()) {
			chars.append(Characters.toEscapeChar(c));
		}
		return chars.toString();
	}

	@Override
	public void dump(DumperOptions options, ASTreeNode node) {
		this.curIndentCount = 0;
		this.curIndent = "";
		this.indentStr = options.getIndent();
		this.dumpPosition = options.isDumpPosition;
		super.dump(options, node);
	}
	
	@Override
	protected String serialize(TreeNode node) {
		return new JsonStrBuilder()
			.enterBlock()	
			.putKV("node_name", node.getNodeName(), false)
			.putKV("position", node.getPos(), true)
			.putNode(node)
			.closeBlock()
			.toString();
	}
	
	@Override
	protected String serialize(Position pos) {
		JsonStrBuilder builder = new JsonStrBuilder()
			.enterBlock()
			.putKV("line", pos.getLine(), false)
			.putKV("col", pos.getCol(), true);
		if (pos.getLineFromSource().isPresent()) {
			builder.putKV("line_text", pos.getLineFromSource().get(), true);
		}
		return builder.closeBlock().toString();
	}

	@Override
	protected String serialize(TokenKind tk) {
		return wrapString(tk.getLiteral());
	}

	@Override
	protected String serialize(Expr.Argument obj) {
		return new JsonStrBuilder()
			.enterBlock()
			.putKV("isUnpack", obj.isUnpack, false)
			.putKV("arg", obj.expr, true)
			.closeBlock()
			.toString();
	}

	@Override
	protected String serialize(Stmt.Parameters obj) {
		return new JsonStrBuilder()
			.enterBlock()
			.putKV("vaArgsIndex", obj.vaArgsIndex, false)
			.putKV("params", obj.params, true)
			.closeBlock()
			.toString();
	}

	@Override
	protected String serialize(Stmt.Parameter param) {
		JsonStrBuilder builder = new JsonStrBuilder();
		builder.enterBlock();
		String name = param.name;
		if (param.isVararg) {
			name = "*" + name;
		}
		builder.putKV("name", name, false);
		if (param.defaultValue.isPresent()) {
			builder.putKV("defaultValue", param.defaultValue.get(), true);
		}
		return builder.closeBlock().toString();
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
		
		private JsonStrBuilder enterBlock() {
			indentInc();
			builder.append("{");
			return this;
		}
		
		private JsonStrBuilder closeBlock() {
			indentDec();
			builder.append("\n").append(indent()).append("}").toString();
			return this;
		}

		private JsonStrBuilder append(String str) {
			builder.append(str);
			return this;
		}

		private JsonStrBuilder putIterator(Iterator iterator) {	
			indentInc();
			builder.append("[");
			if (iterator.hasNext()) {
				putElement(iterator.next(), false);
				while (iterator.hasNext()) {
					putElement(iterator.next(), true);
				}
			}
			indentDec();
			builder.append("\n").append(indent()).append("]");
			return this;
		}

		public JsonStrBuilder putKV(String key, Object value, boolean comma) {
			if (!dumpPosition && value instanceof Position) {
				return this;
			}
			builder.append(comma ? "," : "")
				.append("\n")
				.append(indent())
				.append(wrapString(key))
				.append(": ")
				.append(accept(value));
			return this;
		}
		
		public JsonStrBuilder putNode(TreeNode node) {
			for (Map.Entry<String, Object> entry : node) {
				putKV(entry.getKey(), entry.getValue(), true);
			}
			return this;
		}

		public JsonStrBuilder putElement(Object element, boolean comma) {
			builder.append(comma ? "," : "")
				.append("\n")
				.append(indent())
				.append(accept(element));
			return this;
		}

		@Override
		public String toString() {
			return builder.toString();
		}
	}
}
