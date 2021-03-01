package com.nano.candy.utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class TableView {
	
	private String[] headers;
	private List<String[]> items;
	private String space;
	
	public TableView() {
		items = new ArrayList<>();
		space = "    ";
	}
	
	public void setHeaders(String... headers) {
		this.headers = headers;
	}
	
	public void setSpace(String space) {
		this.space = space;
	}
	
	public void addItem(String... cols) {
		if (cols.length != headers.length) {
			throw new Error(String.format(
				"Headers: %d, But: %d", headers.length, cols.length
			));
		}
		items.add(cols);
	}
	
	private StringBuilder appendCol(StringBuilder builder, int col, int width, String content) {
		String format = "%-" + width + "s";
		builder.append(String.format(format, content)).append(space);
		if (col != headers.length - 1) builder.append(space);
		return builder;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		int[] colWidths = new int[headers.length];
		for (int j = 0; j < headers.length; j ++) {
			int width = measureWidth(j);
			colWidths[j] = width;
			appendCol(builder, j, width, headers[j]);
		}
		for (String[] item : items) {
			builder.append("\n");
			for (int j = 0; j < item.length; j ++) {
				appendCol(builder, j, colWidths[j], item[j]);
			} 
		}
		return builder.toString();
	}

	private int measureWidth(int col) {
		int max = headers[col].length();
		for (String[] item : items) {
			max = Math.max(item[col].length(), max);
		}
		return max;
	}
}
