package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.StackFrame;
import com.nano.candy.interpreter.i2.vm.VM;

@BuiltinClass("StackTraceElement")
public class StackTraceElementObj extends BuiltinObject {
	
	public static final CandyClass STACK_TRACE_ELEMENT_CLASS = 
		BuiltinClassFactory.generate(StackTraceElementObj.class);
	
	public static StackTraceElementObj[] getStackTraceElements(StackFrame stack) {
		return getStackTraceElements(stack, 0);
	}
		
	public static StackTraceElementObj[] getStackTraceElements(StackFrame stack, int offset) {
		StackTraceElementObj[] stackTraceElements =
			new StackTraceElementObj[stack.frameCount()-offset];
		for (int i = 0; offset < stack.frameCount(); i ++, offset ++) {
			stackTraceElements[i] = new StackTraceElementObj(stack.peek(offset));
		}
		return stackTraceElements;
	}
		
	private final String frameName;
	private final String fileName;
	private final int lineNumber;
		
	public StackTraceElementObj() {
		super(STACK_TRACE_ELEMENT_CLASS);
		frameName = "Unknown";
		fileName = "Unknown";
		lineNumber = -1;
	}
	
	public StackTraceElementObj(Frame frame) {
		super(STACK_TRACE_ELEMENT_CLASS);
		frameName = frame.getName();
		fileName = frame.getSourceFileName();
		lineNumber = frame.currentLineExecuted();
	}

	public String getFrameName() {
		return frameName;
	}

	public String getFileName() {
		return fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
	@BuiltinMethod(name = "getLineNumber", argc = 0)
	public void getLineNumber(VM vm) {
		vm.returnFromVM(IntegerObj.valueOf(lineNumber));
	}
	
	@BuiltinMethod(name = "getFileName", argc = 0)
	public void getFileName(VM vm) {
		vm.returnFromVM(StringObj.valueOf(fileName));
	}
	
	@BuiltinMethod(name = "getFrameName", argc = 0)
	public void getFrameName(VM vm) {
		vm.returnFromVM(StringObj.valueOf(frameName));
	}
}
