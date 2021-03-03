package com.nano.candy.ast.printer;

import com.nano.candy.parser.TokenKind;
import com.nano.candy.utils.Position;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonAstPrinter extends SerializablePrinter {

	private int indent = 0;
	private String indentStr = "  ";
	
	private String indent() {
		return indentStr.repeat(indent);
	}
	
	private String wrapString(String str) {
		return "\"" + str + "\"";
	}

	@Override
	protected String accept(Object obj) {
		if (obj == null) return "null";
		return super.accept(obj);
	}

	private String toEscapeChars(String str) {
		StringBuilder chars = new StringBuilder();
		for (char c : str.toCharArray()) {
			switch (c) {
				case '\t': chars.append("\\t"); break;
				case '\r': chars.append("\\r"); break;
				case '\f': chars.append("\\f"); break;
				case '\n': chars.append("\\n"); break;
				case '\"': chars.append("\\\""); break;
				default  : chars.append(c);
			}
		}
		return chars.toString();
	}
	
	private StringBuilder putIterator(Iterator iterator) {
		StringBuilder builder = new StringBuilder();
		indent ++;
		builder.append("[");
		if (iterator.hasNext()) {
			putElement(builder, iterator.next(), false);
			while (iterator.hasNext()) {
				putElement(builder, iterator.next(), true);
			}
		}
		indent --;
		return builder.append("\n").append(indent()).append("]");
		
	}
	
	private StringBuilder putKeyV(StringBuilder builder, String key, Object value, boolean comma) {
		return builder.append(comma ? "," : "")
				.append("\n")
				.append(indent())
				.append(wrapString(key))
				.append(": ")
				.append(accept(value));
	}
	
	private StringBuilder putElement(StringBuilder builder, Object element, boolean comma) {
		return builder.append(comma ? "," : "")
				.append("\n")
				.append(indent())
				.append(accept(element));
	}

	@Override
	protected String serialize(String str) {
		return wrapString(toEscapeChars(str));
	}

	@Override
	protected String serialize(List list) {
		return putIterator(list.iterator()).toString();
	}

	@Override
	protected String serialize(Object[] array) {
		return putIterator(Arrays.asList(array).iterator()).toString();
	}
	
	@Override
	protected String serialize(Position pos) {
		StringBuilder builder = new StringBuilder();
		indent ++;
		builder.append("{");
		putKeyV(builder, "line", pos.getLine(), false);
		putKeyV(builder, "col", pos.getCol(), true);
		if (pos.getLineFromSource().isPresent()) {
			putKeyV(builder, "line_text", pos.getLineFromSource().get(), true);
		}
		indent --;
		return builder.append("\n").append(indent()).append("}").toString();
	}

	@Override
	protected String serialize(TreeNode node) {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		indent ++;
		putKeyV(builder, "node_name", node.getNodeName(), false);
		putKeyV(builder, "position", node.getPos(), true);
		for (Map.Entry<String, Object> entry : node) {
			putKeyV(builder, entry.getKey(), entry.getValue(), true);
		}
		indent --;
		return builder.append("\n").append(indent()).append("}").toString();
	}

	@Override
	protected String serialize(TokenKind tk) {
		return wrapString(tk.getLiteral());
	}
}
