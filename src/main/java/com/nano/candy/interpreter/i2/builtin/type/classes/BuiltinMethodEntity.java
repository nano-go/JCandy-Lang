package com.nano.candy.interpreter.i2.builtin.type.classes;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.error.CandyRuntimeError;
import com.nano.candy.interpreter.i2.error.NativeError;
import com.nano.candy.interpreter.i2.vm.VM;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * This is an unbound method entity which implements onCall via
 * reflection.
 */
public class BuiltinMethodEntity extends CallableObj {

	public static BuiltinMethodEntity[] createMethodEntities(CandyClass candyClass, 
															 Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		ArrayList<BuiltinMethodEntity> builtinMethods = new ArrayList<>();
		for (Method method : methods) {
			if (!method.isAnnotationPresent(BuiltinMethod.class)) {
				continue;
			}
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			BuiltinMethod signature = method.getAnnotation(BuiltinMethod.class);
			String tagName = ObjectHelper.methodName(candyClass, signature.name());
			builtinMethods.add(new BuiltinMethodEntity(method, tagName,
			                   signature.name(), signature.argc()));
		}
		return builtinMethods.toArray(new BuiltinMethodEntity[0]);
	}

	private Method method;
	private int arity;
	
	private BuiltinMethodEntity(Method method, String tagName, String name, int arity) {
		super(name, tagName, arity + 1);
		this.method = method;
		this.arity = arity;
	}

	@Override
	public void onCall(VM vm) {
		// receives the instance of the class in which the method is defined is.
		CandyObject instance = vm.pop();
		try {
			method.invoke(instance, vm);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof CandyRuntimeError) {
				throw (CandyRuntimeError) cause;
			}
			throw new NativeError(cause);
		} catch (CandyRuntimeError e) {
			throw e;
		} catch (Exception e) {
			throw new NativeError(e);
		}
	}

	@Override
	protected String toStringTag() {
		return "built-in method";
	}

	@Override
	public boolean isBuiltin() {
		return true;
	}
}
