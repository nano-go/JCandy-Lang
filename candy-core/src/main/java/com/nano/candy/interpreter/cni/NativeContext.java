package com.nano.candy.interpreter.cni;

import com.nano.candy.interpreter.runtime.GlobalEnvironment;

public interface NativeContext {
	public void action(GlobalEnvironment env);
}
