package com.nano.candy.interpreter.i2.builtin.type.error;

import com.nano.candy.interpreter.i2.builtin.CandyObjEntity;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.rtda.FrameStack;
import com.nano.candy.interpreter.i2.vm.CarrierErrorException;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.Arrays;

@BuiltinClass(value = "Error", isInheritable = true)
public class ErrorObj extends CandyObjEntity {
	
	public static final CandyClass ERROR_CLASS = 
		BuiltinClassFactory.generate(ErrorObj.class);
	
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
	
	public void setStackTraceElements(FrameStack stack) {
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
		return builder.toString();
	}
	
	@BuiltinMethod(name = "", argc = 1)
	protected void init(VM vm) {
		if (this.stackTraceElements != null) {
			vm.returnFromVM(this);
			return;
		}
		vm.syncPcToTopFrame();
		int offset = 0;
		if (!getCandyClass().getInitializer().isBuiltin()) {
			offset = 1;
		}
		this.stackTraceElements = 
			StackTraceElementObj.getStackTraceElements(vm.getFrameStack(), offset);	
		this.message = ObjectHelper.asString(vm.pop());
		vm.returnFromVM(this);
	}
	
	@BuiltinMethod(name = "getStackTraceElements", argc = 0)
	protected void getStackTraceElements(VM vm) {
		vm.returnFromVM(new ArrayObj(Arrays.copyOf(
			stackTraceElements, stackTraceElements.length
		)));
	}
	
	@BuiltinMethod(name = "getMessage", argc = 0)
	protected void getMessage(VM vm) {
		vm.returnFromVM(StringObj.valueOf(message));
	}
	
	@BuiltinMethod(name = "setMessage", argc = 1)
	protected void setMessage(VM vm) {
		setMessage(ObjectHelper.asString(vm.pop()));
		vm.returnNilFromVM();
	}
	
	@BuiltinMethod(name = "sprintStackTrace", argc = 1)
	protected void sprintStackTrace(VM vm) {
		int maxFrames = (int) ObjectHelper.asInteger(vm.pop());
		vm.returnFromVM(StringObj.valueOf(sprintStackTrace(maxFrames)));
	}
}
