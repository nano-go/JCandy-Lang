package com.nano.candy.utils;

public interface Phase<IN, OUT> {
	Result<OUT> apply(Context context, IN input);
}
