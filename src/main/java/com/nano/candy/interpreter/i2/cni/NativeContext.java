package com.nano.candy.interpreter.i2.cni;
import com.nano.candy.interpreter.i2.rtda.GlobalEnvironment;

public interface NativeContext {
	public void action(GlobalEnvironment env);
}
