package com.nano.candy.interpreter.i2.cni;
import com.nano.candy.interpreter.i2.rtda.FileScope;

public interface NativeContext {
	public void action(FileScope curFileScope);
}
