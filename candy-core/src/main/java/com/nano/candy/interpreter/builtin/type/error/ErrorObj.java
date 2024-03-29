package com.nano.candy.interpreter.builtin.type.error;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.builtin.utils.OptionalArg;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
import com.nano.candy.interpreter.runtime.CarrierErrorException;
import com.nano.candy.interpreter.runtime.Frame;
import com.nano.candy.std.Names;
import java.util.Arrays;

@NativeClass(name = "Error", isInheritable = true)
public class ErrorObj extends CandyObject {
	
	public static final CandyClass ERROR_CLASS = 
		NativeClassRegister.generateNativeClass(ErrorObj.class);
	
	public static ErrorObj asErrorObj(Throwable t) {
		if (t instanceof CarrierErrorException) {
			// This is Candy level exception.
			return ((CarrierErrorException) t).getErrorObj();
		}
		// Java level exception.
		return new NativeError(t);
	}
		
	protected StackTraceElementObj[] stackTraceElements;
	protected String message;
	
	public ErrorObj() { 
		this(ERROR_CLASS); 
	}
	
	public ErrorObj(String message) {
		super(ERROR_CLASS);
		this.message = message;
		this.stackTraceElements = null;
	}
	
	public ErrorObj(String msgFmt, Object... args) {
		super(ERROR_CLASS);
		this.message = String.format(msgFmt, args);
		this.stackTraceElements = null;
	}
	
	protected ErrorObj(CandyClass clazz) {
		super(clazz);
		this.message = "";
		this.stackTraceElements = new StackTraceElementObj[0];
	}
	
	protected ErrorObj(CandyClass clazz, String message) {
		super(clazz);
		this.message = message;
	}

	protected ErrorObj(CandyClass clazz, String msgFmt, Object... args) {
		super(clazz);
		this.message = String.format(msgFmt, args);
	}
	

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String msg) {
		this.message = msg;
	}
	
	public void setStackTraceElements(StackTraceElementObj[] stackTraceElements) {
		this.stackTraceElements = stackTraceElements;
	}
	
	public void setStackTraceElements(Frame stack[]) {
		this.stackTraceElements = StackTraceElementObj.getStackTraceElements(stack);
	}
	
	public StackTraceElementObj[] getStackTraceElements() {
		return stackTraceElements;
	}
	
	public void throwSelfNative() {
		throw new CarrierErrorException(this);
	}
	
	public String sprintStackTrace(int maxFrames) {
		StringBuilder builder = new StringBuilder();
		builder.append(getCandyClassName())
			.append(": ")
			.append(message).append("\n");
		int max = Math.min(maxFrames, stackTraceElements.length);
		for (int i = 0; i < max; i ++) {
			StackTraceElementObj e = stackTraceElements[i];
			builder.append("    at ")
				.append(e.getFrameName())
				.append(String.format(
					" (%s:%d)\n", e.getFileName(), e.getLineNumber()
				));
		}
		if (stackTraceElements.length > max) {
			builder.append("More ").append(stackTraceElements.length-max)
				.append(" ...\n");
		}
		return builder.toString();
	}
	
	@NativeMethod(name = Names.METHOD_INITALIZER)
	public CandyObject init(CNIEnv env, StringObj msg) {
		this.message = msg.value();
		if (this.stackTraceElements != null && 
		     this.stackTraceElements.length != 0 ) {
			return this;
		}
		int offset = 0;
		if (!getCandyClass().getInitializer().isBuiltin()) {
			offset = 1;
		}
		this.stackTraceElements = 
			StackTraceElementObj.getStackTraceElements(env.getStack(), offset);	
		return this;
	}
	
	@NativeMethod(name = "getStackTraceElements")
	public CandyObject getStackTraceElements(CNIEnv env) {
		return new ArrayObj(Arrays.copyOf(
			stackTraceElements, stackTraceElements.length
		));
	}
	
	@NativeMethod(name = "getMessage")
	public CandyObject getMessage(CNIEnv env) {
		return StringObj.valueOf(message);
	}
	
	@NativeMethod(name = "setMessage")
	public CandyObject setMessage(CNIEnv env, StringObj msg) {
		setMessage(msg.value());
		return null;
	}
	
	@NativeMethod(name = "sprintStackTrace")
	public CandyObject sprintStackTrace(CNIEnv env, OptionalArg maxFrames) {
		int maxFramesVal = (int) ObjectHelper.asInteger(maxFrames.getValue(24));
		return StringObj.valueOf(sprintStackTrace(maxFramesVal));
	}
}
