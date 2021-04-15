package com.nano.candy.interpreter.i2.tool.debug.cmds.dis;
import com.nano.candy.interpreter.i2.tool.debug.cmds.StandardStyle;
import com.nano.candy.interpreter.i2.tool.dis.DefaultDisassDumper;
import com.nano.candy.interpreter.i2.tool.dis.DisassClass;
import com.nano.candy.interpreter.i2.tool.dis.DisassCodeBlock;
import com.nano.candy.interpreter.i2.tool.dis.DisassInstruction;
import com.nano.candy.interpreter.i2.tool.dis.DisassMethod;

public class HighlightDisassemblerDumper extends DefaultDisassDumper {
	
	private int highlightPc;
	private int from, to;
	private boolean expand;
	
	public HighlightDisassemblerDumper() {
		setIsDisassCodeAttr(true);
		setIsDisassFunctions(true);
	}
	
	public void setRange(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public void setHighlightPc(int highlightPc) {
		this.highlightPc = highlightPc;
	}
	
	@Override
	public String dump(DisassClass ins) {
		return super.dumpClass(ins, expand);
	}

	@Override
	public String dump(DisassMethod ins) {
		return super.dumpMethod(ins, expand);
	}
	
	@Override
	public String dump(DisassCodeBlock codeBlock) {
		StringBuilder builder = new StringBuilder();
		builder.append(dumpCodeAttr(codeBlock.getCodeAttrs()));
		builder.append(indent()).append("Code:\n");
		enterBlock();
		
		this.expand = false;
		int index = findHighlighPCIndex(codeBlock);
		if (index == -1) {
			disassInstructions(codeBlock, builder, 0, codeBlock.length());
		} else {
			disassInstructionsNearBy(codeBlock, builder, index);
		}
		this.expand = true;
		
		exitBlock();
		return builder.toString();
	}

	private void disassInstructionsNearBy(DisassCodeBlock codeBlock, StringBuilder builder, int index) {
		final int length = codeBlock.length();
		int from = Math.max(0, index + this.from);
		int to = Math.min(length, index + this.to);
		disassInstructions(codeBlock, builder, from, to);
	}

	private void disassInstructions(DisassCodeBlock codeBlock, StringBuilder builder, 
									int from, int to) {
		if (from != 0) {
			builder.append(indent()).append("...\n");
		}
		for (int i = from; i < to; i ++) {
			DisassInstruction subins = codeBlock.getIns(i);
			String subinsStr = String.format(
				"%04d: %s", subins.pc(), subins.accept(this)
			);
			if (subins.pc() == highlightPc) {
				subinsStr = StandardStyle.highlight(subinsStr);
			}
			builder.append(indent())
				.append(subinsStr).append("\n");
		}
		if (to != codeBlock.length()) {
			builder.append(indent()).append("...\n");
		}
	}
	
	public int findHighlighPCIndex(DisassCodeBlock codeBlock) {
		int len = codeBlock.getCodeAttrs().length;
		int fromPC = codeBlock.getCodeAttrs().fromPc;
		if (fromPC > highlightPc || highlightPc > fromPC + len) {
			return -1;
		}
		int l = 0;
		int r = codeBlock.length()-1;
		while (l <= r) {
			int mid = l + ((r-l) >> 1);
			DisassInstruction ins = codeBlock.getIns(mid);
			int pc =ins.pc();
			if (highlightPc > pc) {
				l = mid + 1;
			} else if (highlightPc < pc) {
				r = mid - 1;
			} else return mid;
		}
		return -1;
	}
}
