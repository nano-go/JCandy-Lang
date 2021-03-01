package com.nano.candy.interpreter.i2.builtin.type.classes;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.error.CandyRuntimeError;
import com.nano.candy.interpreter.i2.vm.VM;
import java.lang.reflect.Constructor;

public class BuiltinClassEntity extends CandyClass {
	
	private Class<? extends CandyObject> clazz;
	
	public BuiltinClassEntity(Class<? extends CandyObject> clazz, String name, CandyClass superClass) {
		super(name, superClass);
		this.clazz = clazz;
	}

	@Override
	public void onCall(VM vm) {
		try {
			Constructor<? extends CandyObject> constructor = clazz.getConstructor();
			CandyObject instance = constructor.newInstance();
			if (initializer == null) {
				vm.returnFromVM(instance);
			} else {
				vm.push(instance);
				initializer.onCall(vm);
			}
		} catch (CandyRuntimeError e) {
			throw e;
		} catch (Exception e) {
			throw new CandyRuntimeError(e.getMessage());
		}
	}
}
