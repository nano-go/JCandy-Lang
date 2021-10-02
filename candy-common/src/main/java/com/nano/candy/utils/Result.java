package com.nano.candy.utils;

import java.util.Objects;

public class Result<T> {
	
	public static <T> Result<T> success(T result) {
		Result<T> r = new Result<T>();
		r.result = result;
		return r;
	}
	
	public static <T> Result<T> failure(String reason, boolean detailsInLogger) {
		Result<T> r = new Result<T>();
		r.reasonForFailure = reason;
		r.detailsInLogger = detailsInLogger;
		return r;
	}
	
	private T result;
	private String reasonForFailure;
	private boolean detailsInLogger;
	
	private Result() {}
	
	public String getReason() {
		return reasonForFailure;
	}
	
	public boolean detailsInLogger() {
		return detailsInLogger;
	}
	
	public boolean isPresent() {
		return result != null;
	}
	
	public T getResult() {
		return Objects.requireNonNull(result);
	}
}
