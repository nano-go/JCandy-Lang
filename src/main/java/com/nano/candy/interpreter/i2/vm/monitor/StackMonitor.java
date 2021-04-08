package com.nano.candy.interpreter.i2.vm.monitor;

import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.interpreter.i2.rtda.FrameStack;

public interface StackMonitor {
	public void newFramePushed(VM vm, FrameStack stack);
	public void oldFramePoped(VM vm, Frame oldFrame, FrameStack stack);
}
