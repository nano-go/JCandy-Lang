package com.nano.candy.interpreter.i2.tool.debug.cmds.show;

import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue.MethodInfo;
import com.nano.candy.interpreter.i2.tool.debug.AbstractCommand;
import com.nano.candy.interpreter.i2.tool.debug.CommandLine;
import com.nano.candy.interpreter.i2.tool.debug.CommandOptions;
import com.nano.candy.interpreter.i2.tool.debug.VMTracer;
import com.nano.candy.interpreter.i2.tool.debug.cmds.StandardStyle;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import com.nano.candy.utils.Console;
import com.nano.common.io.FileUtilsQuiet;
import java.io.File;

public class ShowLines extends AbstractCommand {

	@Override
	public String name() {
		return "lines";
	}

	@Override
	public String[] aliases() {
		return new String[] {"ls"};
	}

	@Override
	public String description() {
		return "Show the specified line or function.";
	}

	@Override
	public CommandOptions options() {
		return new CommandOptions()
			.addOption(CommandOptions.OptionType.INTEGER, 
				"lineNumber", true, "Show ten lines around the specified line.")
			.addOption(CommandOptions.OptionType.STRING, 
				"funcName", true, "Show the specified function. Format[class#name or name].");
	}

	@Override
	public void startToExe(VMTracer tracer, CommandLine cmdLine) throws CommandLine.ParserException {
		if (cmdLine.hasOption("funcName")) {
			showFunction(tracer.getVM(), tracer, cmdLine.getString("funcName"));
		} else if (cmdLine.hasOption("lineNumber")) {
			showLines(tracer.getVM(), tracer, cmdLine.getInteger("lineNumber"));
		} else {
			showLines(tracer.getVM(), tracer);
		}
	}

	private void showLines(VM vm, VMTracer tracer) {
		vm.syncPcToTopFrame();
		int line = vm.frame().currentLine();
		showLines(vm, tracer, line);
	}
	
	private void showLines(VM vm, VMTracer tracer, int line) {
		vm.syncPcToTopFrame();
		String srcFileName = vm.frame().chunk.getSourceFileName();
		showLines(tracer.getConsole(), srcFileName, 
		          Math.max(line-4, 0), line+5, line);
	}
	
	/**
	 * Print the specified function.
	 *
	 * @param functionName Format[class_name#name or name]
	 */
	private void showFunction(VM vm, VMTracer tracer, String name) {
		ConstantValue.MethodInfo met = findMethodInfo(tracer, name);
		if (met == null) {
			tracer.getConsole().getPrinter().printf(
				"Undefined function: %s\n", name
			);
			return;
		}
		showFunction(tracer, met);
	}

	private void showFunction(VMTracer tracer, ConstantValue.MethodInfo met) {
		Chunk chunk = tracer.getVM().frame().chunk;
		int from,to;
		int fromPc = met.getFromPC(), len = met.getLength();
		if (met.classDefinedIn != null) {
			// The method defined in a class has no OP_FUN opcode (can't locate),
			// but the line number table records the starting position of 
			// this method by the opcode (OP_NOP) that the met.fromPc is pointed.
			from = chunk.getLineNumber(fromPc);
		} else {
			// Get the OP_FUN opcode position.
			from = chunk.getLineNumber(fromPc - 2);
		}
		to = chunk.getLineNumber(fromPc + len-1);
		showLines(tracer.getConsole(), chunk.getSourceFileName(), from, to);
	}

	private MethodInfo findMethodInfo(VMTracer tracer, String name) {
		String[] classAndFunc = name.split("#", 2);
		if (classAndFunc.length == 2) {
			return findMetInfoFromClass(
				tracer.getConstantPool(), classAndFunc[0], classAndFunc[1]
			);
		} 
		return findMetInfoFromCP(tracer.getConstantPool(), classAndFunc[0]);
	}
	
	private ConstantValue.MethodInfo findMetInfoFromClass(ConstantPool cp, String className, String funcName) {
		for (int i = cp.size()-1; i >= 0; i --) {
			ConstantValue cv = cp.getConstants()[i];
			if (cv instanceof ConstantValue.ClassInfo) {
				ConstantValue.ClassInfo classInfo = (ConstantValue.ClassInfo) cv;
				if (!className.equals(classInfo.className)) {
					continue;
				}
				if (Names.METHOD_INITALIZER.equals(funcName)) {
					return classInfo.initializer.orElse(null);
				}
				for (ConstantValue.MethodInfo met : classInfo.methods) {
					if (funcName.equals(met.name)) {
						return met;
					}
				}
			}
		}
		return null;
	}
	
	private ConstantValue.MethodInfo findMetInfoFromCP(ConstantPool cp, String funcName) {
		for (int i = cp.size()-1; i >= 0; i --) {
			ConstantValue cv = cp.getConstants()[i];
			if (cv instanceof ConstantValue.MethodInfo) {
				ConstantValue.MethodInfo met = (ConstantValue.MethodInfo) cv;
				if (funcName.equals(met.name)) {
					return met;
				}
				continue;
			}
		}
		return null;
	}
	
	public static void showLines(Console console, String sourceFilePath, 
	                             int fromLine, int toLine) {
		showLines(console, sourceFilePath, fromLine, toLine, -1);					
	}
	
	/**
	 * Print the part between the fromLine and toLine of the specified source file
	 * and highlight the specified line.
	 */
	public static void showLines(Console console, String sourceFilePath, 
	                             int fromLine, int toLine, int highlight) {
		String text = FileUtilsQuiet.readText(new File(sourceFilePath));
		if (text == null) {
			console.getPrinter().printf("Can't open the file: %s\n", sourceFilePath);
			return;
		}
		final int DIGITS = (int) Math.log10(toLine) + 1;
		final String LINE_PREFIX_FMT = "%" + DIGITS + "d | ";
		char[] chars = getChars(text);
		int lineNum = 1;
		int from = 0;
		for (int i = 0; i < chars.length; i ++) {
			if (chars[i] == '\n') {
				if (lineNum >= fromLine) {
					String line = String.valueOf(chars, from, i+1-from);
					line = String.format(LINE_PREFIX_FMT, lineNum) + line;
					printLine(console, highlight == lineNum, line);
				}
				lineNum ++;
				if (lineNum > toLine) {
					break;
				}
				from = i+1;
			}
		}
	}

	private static char[] getChars(String text) {
		char[] chars = new char[text.length() + 1]; 
		text.getChars(0, text.length(), chars, 0);
		chars[text.length()] = '\n';
		return chars;
	}

	public static void printLine(Console console, boolean isHighlight, String line) {
		if (isHighlight) {
			line = StandardStyle.highlight(line);	
		}
		console.getPrinter().print(line);
	}
}
