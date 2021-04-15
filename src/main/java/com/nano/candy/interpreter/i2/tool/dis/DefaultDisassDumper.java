package com.nano.candy.interpreter.i2.tool.dis;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.CodeAttribute;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.ErrorHandlerTable;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.LineNumberTable;
import com.nano.common.text.StringUtils;
import java.util.Objects;

public class DefaultDisassDumper implements DisassInsDumper {

	protected String indent;
	protected int indentLevel;
	
	private String curIndent;
	
	protected boolean isDisassCodeAttr;
	protected boolean isDisassConstantPool;
	protected boolean isDisassLineNumberTable;
	protected boolean isDisassFunctions;
	
	public DefaultDisassDumper() {
		this("    ");
	}
	
	public DefaultDisassDumper(String indent) {
		this.indent = Objects.requireNonNull(indent);
		this.indentLevel = 0;
		this.curIndent = "";
	}

	public DefaultDisassDumper setIsDisassCodeAttr(boolean isDisassCodeAttr) {
		this.isDisassCodeAttr = isDisassCodeAttr;
		return this;
	}

	public boolean isDisassCodeAttr() {
		return isDisassCodeAttr;
	}

	public DefaultDisassDumper setIsDisassConstantPool(boolean isDisassConstantPool) {
		this.isDisassConstantPool = isDisassConstantPool;
		return this;
	}

	public boolean isDisassConstantPool() {
		return isDisassConstantPool;
	}

	public DefaultDisassDumper setIsDisassLineNumberTable(boolean isDisassLineNumberTable) {
		this.isDisassLineNumberTable = isDisassLineNumberTable;
		return this;
	}

	public boolean isDisassLineNumberTable() {
		return isDisassLineNumberTable;
	}

	public DefaultDisassDumper setIsDisassFunctions(boolean isDisassFunctions) {
		this.isDisassFunctions = isDisassFunctions;
		return this;
	}

	public boolean isDisassFunctions() {
		return isDisassFunctions;
	}
	
	protected String indent() {
		return curIndent;
	}
	
	protected void enterBlock() {
		indentLevel ++;
		curIndent = indent.repeat(indentLevel);
	}
	
	protected void exitBlock() {
		indentLevel --;
		curIndent = indent.repeat(indentLevel);
	}
	
	protected StringBuilder enterBlock(String name) {
		StringBuilder blockBuilder = new StringBuilder(name);
		enterBlock();
		return blockBuilder;
	}
	
	protected String dumpConstantPool(ConstantPool cp) {
		final int INDEX_DIGITS = (int) Math.log10(cp.size()) + 1;
		final String FMT = "#%-" + INDEX_DIGITS + "d = %-14s%s\n";
		
		StringBuilder builder = enterBlock("Constant Pool:\n");
		
		for (int i = 0; i < cp.size(); i ++) {
			ConstantValue val = cp.getConstants()[i];
			builder.append(indent())
				.append(String.format(FMT, i, val.headName(), val));
		}
		
		exitBlock();
		return builder.toString();
	}

	protected String dumpLineNumberTable(LineNumberTable lineNumberTable) {
		StringBuilder builder = enterBlock("Line Number Table:\n");
		
		final String FMT = "<pc: %d, line: %d>\n";
		final int LEN = lineNumberTable.tableBytes.length;
		for (int i = 0; i < LEN; i += 4) {
			builder.append(indent());
			int startPc = lineNumberTable.startPc(i);
			int line = lineNumberTable.lineNumber(i);
			builder.append(String.format(FMT, startPc, line));
		}
		
		exitBlock();
		return builder.toString();
	}

	protected String dumpCodeAttr(CodeAttribute codeAttr) {
		StringBuilder builder = enterBlock("Code Attrs:\n");
		
		builder.append(indent()).append(String.format(
			"max_stack = %d, max_local = %d, length = %d\n",
			codeAttr.maxStack, codeAttr.maxLocal, codeAttr.length
		));
		String handlerTableStr = dumpErrorHandlerTable(codeAttr.errorHandlerTable);
		if (!StringUtils.isEmpty(handlerTableStr)) {
			builder.append(indent()).append(handlerTableStr);
		}
		exitBlock();
		return builder.toString();
	}

	protected String dumpErrorHandlerTable(ErrorHandlerTable table) {
		if (table == null || table.length() == 0) {
			return "";
		}
		StringBuilder builder = enterBlock("Error Handler Table:\n");
		builder.append(indent()).append(String.format(
			"%-8s %-8s %-8s\n", "from", "to", "handler"
		));
		int len = table.length();
		for (int i = 0; i < len; i ++) {
			ErrorHandlerTable.ErrorHandler errHandlar = table.get(i);
			builder.append(indent()).append(String.format(
				"%-8d %-8d %-8d\n", 
				errHandlar.startPc, errHandlar.endPc, errHandlar.handlerPc
			));
		}
		exitBlock();
		return builder.toString();
	}

	@Override
	public String dump(DisassChunk chunk) {
		StringBuilder builder = new StringBuilder();
		builder.append("Source File: ")
			.append(chunk.getChunk().getSourceFileName())
			.append("\n");
		if (isDisassConstantPool)
			builder.append(dumpConstantPool(chunk.getConstsntPool()));
		if (isDisassLineNumberTable)
			builder.append(dumpLineNumberTable(chunk.getLineNumberTable()));
		builder.append(dump(chunk.getCodeBlock()));
		return builder.toString();
	}
	
	@Override
	public String dump(DisassClass ins) {
		return dumpClass(ins, true);
	}
	
	protected String dumpClass(DisassClass ins, boolean expand) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("class %s",ins.getClassName()));
		if (ins.hasSuperClass()) {
			builder.append(" : ")
				.append(ins.getSuperClassName());
		} 
		builder.append(" {");
		
		if (!expand) {
			builder.append("...}");
			return builder.toString();
		}
		
		builder.append("\n");
		enterBlock();
		for (DisassMethod met : ins.getMethods()) {
			builder.append(indent())
				.append(met.accept(this)).append("\n");
		}
		exitBlock();
		builder.append(indent()).append("}");
		return builder.toString();
	}

	@Override
	public String dump(DisassSimpleInstruction ins) {
		return String.format("%-18s%s",
			ins.getInsName(), ins.getArgumentInfo());
	}

	@Override
	public String dump(DisassMethod ins) {
		return dumpMethod(ins, this.isDisassFunctions);
	}
	
	protected String dumpMethod(DisassMethod ins, boolean expand) {
		ConstantValue.MethodInfo metInfo = ins.getMethodInfo();
		DisassCodeBlock codeBlock = ins.getCodeBlock();
		StringBuilder builder = new StringBuilder();
		String header = String.format(
			"fun %s(%d) {", metInfo.name, metInfo.arity);
		builder.append(header);
		if (!expand) {
			return builder.append("...}").toString();
		}
		
		builder.append("\n");
		enterBlock();
		builder.append(indent()).append(dumpMethodInfo(ins.getMethodInfo()));
		builder.append(indent()).append(dump(codeBlock));
		exitBlock();
		builder.append(indent()).append("}");
		return builder.toString();
	}

	protected String dumpMethodInfo(ConstantValue.MethodInfo methodInfo) {
		if (methodInfo.upvalueCount() == 0) {
			return "No captured upvalues.\n";
		}
		final String FMT = "<local: %s, index: %d>\n";
		StringBuilder builder = enterBlock("Upvalues:\n");	
		for (int i = 0; i < methodInfo.upvalueCount(); i ++) {
			builder.append(indent()).append(String.format(
				FMT, methodInfo.isLocal(i), methodInfo.upvalueIndex(i)
			));
		}
		exitBlock();
		return builder.toString();
	}

	@Override
	public String dump(DisassCodeBlock codeBlock) {
		StringBuilder builder = new StringBuilder();
		if (isDisassCodeAttr) {
			builder.append(dumpCodeAttr(codeBlock.getCodeAttrs()))
				.append(indent());
		}
		builder.append("Code:\n");
		enterBlock();
		
		final int length = codeBlock.length();
		for (int i = 0; i < length; i ++) {
			DisassInstruction subins = codeBlock.getIns(i);
			builder.append(indent())
				.append(dumpSubInstruction(subins, i))
				.append("\n");
		}
		
		exitBlock();
		return builder.toString();
	}
	
	protected String dumpSubInstruction(DisassInstruction subins, int serialNumber) {
		if (subins.isExpandable()) {
			return subins.accept(this);
		} 
		return String.format(
			"%04d: %s", subins.pc(), subins.accept(this)
		);
	}
	
}
