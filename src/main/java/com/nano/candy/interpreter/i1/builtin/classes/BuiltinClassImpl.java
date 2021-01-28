package com.nano.candy.interpreter.i1.builtin.classes;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.annotation.BuiltinClass;
import com.nano.candy.interpreter.i1.builtin.func.BuiltinMethodObj;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;
import java.util.Iterator;
import java.util.List;

public class BuiltinClassImpl extends CandyClass {
	
	public static BuiltinClassImpl newBuiltinClass(Class<? extends CandyObject> clazz) {
		if (!clazz.isAnnotationPresent(BuiltinClass.class)) {
			throw new Error(clazz.getSimpleName() + " is not built-in class.");
		}
		BuiltinClass builtinClass = clazz.getAnnotation(BuiltinClass.class);
		CandyClass superClass = ObjectClass.getInstance();
		String name = builtinClass.value();
		boolean isInheritable = builtinClass.isInheritable();
		return new BuiltinClassImpl(clazz, name, superClass, isInheritable);
	}
	
	private Class<? extends CandyObject> clazz;
	private boolean isBuiltinClassAnnotation;
	
	private BuiltinClassImpl(Class<? extends CandyObject> clazz, String name, CandyClass superClass, boolean isInheritable) {
		this(clazz, name, superClass);
		if (!isInheritable) {
			makeUninheritable();
		}
		isBuiltinClassAnnotation = true;
	}
	
	public BuiltinClassImpl(Class<? extends CandyObject> clazz, String name) {
		this(clazz, name, ObjectClass.getInstance());
	}
	
	public BuiltinClassImpl(Class<? extends CandyObject> clazz, String name, CandyClass superClass) {
		super(name, superClass);
		this.clazz = clazz;
	}
	
	public void defineMethods() {
		List<BuiltinMethodObj> methodList = BuiltinMethodObj.getBuiltinMethods(clazz);
		Iterator<BuiltinMethodObj> iterator = methodList.iterator();
		if (!iterator.hasNext()) {
			return;
		}
		for (;;) {
			BuiltinMethodObj method = iterator.next();
			if (!iterator.hasNext()) {
				if ("".equals(method.name())) {
					super.initializer = method;
				} else {
					defineMethod(method.name(), method);
				}
				break;
			}
			defineMethod(method.name(), method);
		}
	}

	@Override
	public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
		if (!isBuiltinClassAnnotation) {
			return super.onCall(interpreter, args);
		}
		try {
			CandyObject obj= clazz.newInstance();
			getInitializer().bindToInstance(obj).onCall(interpreter, args);
			return obj;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		throw new Error();
	}
	
}
