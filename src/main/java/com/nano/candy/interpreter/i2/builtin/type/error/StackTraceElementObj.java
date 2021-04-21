package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.StackFrame;
import com.nano.candy.interpreter.i2.vm.VM;

@NativeClass(name = "StackTraceElement")
public class StackTraceElementObj extends BuiltinObject {
	
	public static final CandyClass STACK_TRACE_ELEMENT_CLASS = 
		NativeClassRegister.generateNativeClass(StackTraceElementObj.class);
	
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
	
	@NativeMethod(name = "getLineNumber")
	public CandyObject getLineNumber(VM vm, CandyObject[] args) {
		return IntegerObj.valueOf(lineNumber);
	}
	
	@NativeMethod(name = "getFileName")
	public CandyObject getFileName(VM vm, CandyObject[] args) {
		return StringObj.valueOf(fileName);
	}
	
	@NativeMethod(name = "getFrameName")
	public CandyObject getFrameName(VM vm, CandyObject[] args) {
		return StringObj.valueOf(frameName);
	}
}
