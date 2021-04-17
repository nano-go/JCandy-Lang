package com.nano.candy.interpreter.i2.vm.tracer;
import com.nano.candy.interpreter.i2.vm.VM;

public interface CodeTracer {
	public void beforeIns(VM vm, int pc);
	public void afterIns(VM vm);
}
