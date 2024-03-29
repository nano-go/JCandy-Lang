package com.nano.candy.utils;
import java.util.HashMap;
import java.util.Objects;

public class Context {
	
	public static interface Factory<T> {
		T make(Context c);
	}
	
	private static final ThreadLocal<Context> context = new ThreadLocal<Context>() {
		@Override
		protected Context initialValue() {
			return new Context();
		}
	};
	
	public static Context getThreadLocalContext() {
		return context.get();
	}
	
	protected final HashMap<Class<?>, Factory<?>> kf = new HashMap<>();
	protected final HashMap<Class<?>, Object> kv = new HashMap<>();
	
	public Context() {
		put(Logger.class, Logger.newLogger());
	}

	public <T> void put(Class<T> key, T val) {
		kv.put(key, Objects.requireNonNull(val));
	}
	
	public <T> void put(Class<T> key, Factory<T> fac) {
		kf.put(key, Objects.requireNonNull(fac));
	}
	
	public <T> T get(Class<T> key) {
		Object value = kv.get(key);
		if (value == null) {
			Factory<T> fac = (Context.Factory<T>) kf.get(key);
			if (fac != null) {
				value = Objects.requireNonNull(fac.make(this));
				kv.put(key, value);
			}
		}
		return (T) value;
	}
}
