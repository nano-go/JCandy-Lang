package com.nano.candy.interpreter.i2.error;

import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.FrameStack;
import com.nano.candy.sys.CandySystem;
import com.nano.common.io.FilePathUtils;
import java.io.PrintStream;
import java.io.PrintWriter;

public class CandyRuntimeError extends RuntimeException {
	
	public CandyRuntimeError(String msg) {
		super(msg);
	}
	
	public CandyRuntimeError(String format, Object... args) {
		super(String.format(format, args));
	}
	
	public void printStackTrace(FrameStack stack, String indent,
	                            int maxFrame, PrintStream ps) {
		printStackTrace(stack, indent, maxFrame, new PrintWriter(ps));						
	}
	
	public void printStackTrace(FrameStack stack, String indent,
	                            int maxFrame, PrintWriter pw) {
		pw.append(getClass().getSimpleName())
			.append(": ")
			.append(getMessage())
			.append("\n");
		printStackTraceInfo(stack, indent, maxFrame, pw);
		pw.flush();
	}

	private void printStackTraceInfo(FrameStack stack, String indent, 
	                                 int maxFrame, PrintWriter pw) {
		int max = maxFrame < 0 ? Integer.MAX_VALUE : maxFrame;
		int bottom = stack.frameCount() - 
			Math.min(max, stack.frameCount());
		for (int i = stack.frameCount()-1; i >= bottom; i --) {
			Frame frame = stack.getAt(i);
			String info = String.format(
				"%sat %s (%s: line %d)\n",
				indent, frame.name,
				getRelativePathOf(
					frame.chunk.getSourceFileName(), 
					CandySystem.DEFAULT_USER_DIR
				),
				frame.chunk.getLineNumber(frame.pc-1)
			);
			pw.append(info);
		}
		if (bottom != 0) {
			pw.append("    More Frames...\n");
		}
	}
	
	private static String getRelativePathOf(String src, String of) {
		String path = FilePathUtils.getRelativePathOf(src, of);
		if (!path.startsWith("/")) {
			return "/" + path;
		}
		return path;
	}
}
