package com.nano.candy.interpreter.i1.builtin.type;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.StringClass;
import com.nano.candy.std.StringFunctions;
import java.util.concurrent.ExecutionException;

public class StringObject extends CandyObject{
	
	private static final Cache<String, StringObject> CACHE = CacheBuilder.newBuilder()
		.maximumSize(100)
		.weakValues()
		.build() ;
		
	public static StringObject of(String str) {
		try {
			return CACHE.get(str, () -> new StringObject(str)) ;
		} catch (ExecutionException e) {
			return new StringObject(str) ;
		}
	}
	
    private String value ;

	private StringObject(String value) {
		super(StringClass.STRING_CLASS);
		this.value = value;
		freeze();
	}
	
	public String value() {
		return value ;
	}

	@Override
	public CandyObject times(CandyObject obj) {
		if (obj instanceof IntegerObject) {
			long c = ((IntegerObject) obj).intValue();
			return of(StringFunctions.repeat(value, c));
		}
		return super.times(obj);
	}

	@Override
	public StringObject stringValue() {
		return this ;
	}

	@Override
	public String toString() {
		return value ;
	}

	@Override
	public BooleanObject equalTo(CandyObject obj) {
		if (obj instanceof StringObject) {
			return BooleanObject.valueOf(value.equals(((StringObject)obj).value)) ;
		}
		return super.equalTo(obj) ;
	}
}
