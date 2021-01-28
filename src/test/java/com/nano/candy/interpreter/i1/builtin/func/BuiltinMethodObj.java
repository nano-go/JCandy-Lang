package com.nano.candy.interpreter.i1.builtin.func;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.func.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i1.builtin.type.Callable;
import com.nano.candy.interpreter.i1.builtin.type.CallableObject;
import com.nano.candy.interpreter.i1.builtin.type.StringObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BuiltinMethodObj extends CallableObject {

	public static List<BuiltinMethodObj> getBuiltinMethods(Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		ArrayList<BuiltinMethodObj> builtinMethods = new ArrayList<>();
		BuiltinMethodObj initializer = null;
		for (Method method : methods) {
			if (!method.isAnnotationPresent(BuiltinMethod.class)) {
				continue;
			}
			BuiltinMethod m = method.getAnnotation(BuiltinMethod.class);
			String name = m.value();
			if ("".equals(name)) {
				if (initializer != null) {
					throw new Error("Reapt initalizer in " + clazz.getName());
				}
				initializer = new BuiltinMethodObj(name, m.argc(), method);
				continue;
			}
			builtinMethods.add(new BuiltinMethodObj(name, m.argc(), method));
		}
		if (initializer != null) builtinMethods.add(initializer);
		return builtinMethods;
	}

	protected CandyObject instance;
	private String name;
	private Method method;

	public BuiltinMethodObj(String name, int arity, Method method) {
		super(arity);
		this.name = name;
		this.method = method;
	}

	public String name() {
		return name;
	}

	@Override
	public StringObject stringValue() {
		return StringObject.of("<built-in method: parameters(" + arity + ") >");
	}

	@Override
	public Callable bindToInstance(CandyObject instance) {
		BuiltinMethodObj method = new BuiltinMethodObj(name, arity(), this.method);
		method.instance = instance;
		return method;
	}

	@Override
	public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
		try {
			return (CandyObject) method.invoke(instance, interpreter, args);
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			e.printStackTrace();
		} catch (IllegalArgumentException | 
		         IllegalAccessException e) {
			e.printStackTrace();
		}
		throw new Error();
	}

}
