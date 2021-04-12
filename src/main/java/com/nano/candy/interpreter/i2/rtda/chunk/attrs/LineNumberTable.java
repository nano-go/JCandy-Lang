package com.nano.candy.interpreter.i2.rtda.chunk.attrs;

public class LineNumberTable {
	public final byte[] tableBytes;
	public LineNumberTable(byte[] lineNumberTable) {
		this.tableBytes = lineNumberTable;
	}

	public int startPc(int offset) {
		int startPc = Byte.toUnsignedInt(tableBytes[offset]);
		return (startPc << 8) | Byte.toUnsignedInt(tableBytes[offset + 1]);
	}

	public int lineNumber(int offset) {
		int lineNumber = Byte.toUnsignedInt(tableBytes[offset + 2]);
		return (lineNumber << 8) | Byte.toUnsignedInt(tableBytes[offset + 3]);
	}

	public int findLineNumber(int pc) {
		int l = 0;
		int r = tableBytes.length / 4 - 1;
		int offset=0;
		while (l <= r) {
			int mid = ((l + r) / 2);
			int ix = mid*4;
			int startPc = startPc(ix);
			if (pc >= startPc) {
				offset = ix;
				l = mid + 1;
			} else if (pc < startPc) {
				r = mid - 1;
			} 
		}
		return lineNumber(offset);
	}
}
