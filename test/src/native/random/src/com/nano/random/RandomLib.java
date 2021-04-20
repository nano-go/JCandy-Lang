package com.nano.random;

import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.interpreter.i2.rtda.*;
import com.nano.candy.interpreter.i2.cni.*;
import com.nano.candy.interpreter.i2.builtin.type.*;
import com.nano.candy.interpreter.i2.builtin.*;
import com.nano.candy.interpreter.i2.builtin.utils.*;

public class RandomLib implements NativeContext {

	@Override
	public void action(FileScope curFileScope) {
		NativeFuncRegister.register(curFileScope, RandomLib.class);
	}
	
	@NativeFunc(name = "randomInt", arity = 2)
	public static CandyObject randomInt(VM vm, CandyObject[] args) {
		long from = ObjectHelper.asInteger(args[0]);
		long to = ObjectHelper.asInteger(args[1]);
		return IntegerObj.valueOf(
			from + (long) (Math.random() * (to - from))
		);
	}
	
	@NativeFunc(name = "randomFloat", arity = 2)
	public static CandyObject randomFloat(VM vm, CandyObject[] args) {
		double from = ObjectHelper.asDouble(args[0]);
		double to = ObjectHelper.asDouble(args[1]);
		return DoubleObj.valueOf(
			from + (Math.random() * (to - from))
		);
	}
}