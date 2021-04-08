package com.nano.candy.interpreter.i2.vm.monitor;
import com.nano.candy.interpreter.i2.vm.VM;

public interface CodeMonitor {
	public void beforeIns(VM vm, int pc);
	public void afterIns(VM vm);
}
