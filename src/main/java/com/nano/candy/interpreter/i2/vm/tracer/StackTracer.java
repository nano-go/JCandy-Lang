package com.nano.candy.interpreter.i2.vm.tracer;

import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.interpreter.i2.rtda.StackFrame;

public interface StackTracer {
	public void newFramePushed(VM vm, StackFrame stack);
	public void oldFramePoped(VM vm, Frame oldFrame, StackFrame stack);
}
