package com.nano.candy.interpreter.i2.builtin.functions;

import com.nano.candy.interpreter.i2.vm.VM;

@FunctionalInterface
public interface Callback {
	public void onCall(VM vm);
}
