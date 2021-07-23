package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.error.StackTraceElementObj;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.runtime.Frame;

@NativeClass(name = "StackTraceElement")
public class StackTraceElementObj extends CandyObject {
	
	public static final CandyClass STACK_TRACE_ELEMENT_CLASS = 
		NativeClassRegister.generateNativeClass(StackTraceElementObj.class);
	
	public static StackTraceElementObj[] getStackTraceElements(Frame[] stack) {
		return getStackTraceElements(stack, 0);
	}
		
	public static StackTraceElementObj[] getStackTraceElements(Frame[] stack, int offset) {
		StackTraceElementObj[] stackTraceElements =
			new StackTraceElementObj[stack.length-offset];
		for (int i = 0; offset < stack.length; i ++, offset ++) {
			stackTraceElements[i] = new StackTraceElementObj(stack[offset]);
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
	public CandyObject getLineNumber(CNIEnv env, CandyObject[] args) {
		return IntegerObj.valueOf(lineNumber);
	}
	
	@NativeMethod(name = "getFileName")
	public CandyObject getFileName(CNIEnv env, CandyObject[] args) {
		return StringObj.valueOf(fileName);
	}
	
	@NativeMethod(name = "getFrameName")
	public CandyObject getFrameName(CNIEnv env, CandyObject[] args) {
		return StringObj.valueOf(frameName);
	}
}
