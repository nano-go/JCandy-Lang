package com.nano.candy.utils;

public abstract class Task<I, O> implements Phase<I, O> {
	
	public static <I, O> Task<I, O> newTask(final Phase<I, O> phase) {
		return new Task<I, O>() {
			@Override
			public Result<O> apply(Context context, I input) {
				return phase.apply(context, input);
			}
		};
	}
	
	public Task() {}
	
	public <R> Task<I, R> then(final Phase<O, R> phase) {
		return new Task<I, R>() {
			public Result<R> apply(Context context, I input) {
				Result<O> out = Task.this.apply(context, input);
				if (!out.isPresent()) {
					// the result is null.
					return (Result<R>) out;
				}
				return phase.apply(context, out.getResult());
			}
		};
	}
}
