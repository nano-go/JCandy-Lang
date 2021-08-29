package com.nano.candy.interpreter.builtin.type;
import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.NumberObj;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeMethod;

@NativeClass(name = "Number")
public abstract class NumberObj extends CandyObject {
	
	public static final CandyClass NUMBER_CLASS = 
		NativeClassRegister.generateNativeClass(NumberObj.class);
	
	public NumberObj(CandyClass clazz) {
		super(clazz);
		freeze();
	}
	
	public static boolean isNumber(CandyObject obj) {
		return obj instanceof IntegerObj || obj instanceof DoubleObj;
	}
	
	public static boolean isNumber(CandyClass clazz) {
		if (clazz.isCandyClass()) return false;
		return clazz.isSubClassOf(NUMBER_CLASS);
	}

	public abstract long intValue();
	public abstract double doubleValue();
	
	protected abstract boolean isDouble();
	
	@Override
	public CandyObject add(CNIEnv env, CandyObject operand) {
		if (isNumber(operand)) {
			NumberObj n = (NumberObj) operand;
			if (isDouble() || n.isDouble()) {
				return DoubleObj.valueOf(doubleValue() + n.doubleValue());
			}
			return IntegerObj.valueOf(intValue() + n.intValue());
		}
		return super.add(env, operand);
	}

	@Override
	public CandyObject sub(CNIEnv env, CandyObject operand) {
		if (isNumber(operand)) {
			NumberObj n = (NumberObj) operand;
			if (isDouble() || n.isDouble()) {
				return DoubleObj.valueOf(doubleValue() - n.doubleValue());
			}
			return IntegerObj.valueOf(intValue() - n.intValue());
		}
		return super.sub(env, operand);
	}

	@Override
	public CandyObject mul(CNIEnv env, CandyObject operand) {
		if (isNumber(operand)) {
			NumberObj n = (NumberObj) operand;
			if (isDouble() || n.isDouble()) {
				return DoubleObj.valueOf(doubleValue() * n.doubleValue());
			}
			return IntegerObj.valueOf(intValue() * n.intValue());
		}
		return super.mul(env, operand);
	}

	@Override
	public CandyObject div(CNIEnv env, CandyObject operand) {
		if (isNumber(operand)) {
			NumberObj n = (NumberObj) operand;
			if (isDouble() || n.isDouble()) {
				return DoubleObj.valueOf(doubleValue() / n.doubleValue());
			}
			return IntegerObj.valueOf(intValue() / n.intValue());
		}
		return super.div(env, operand);
	}
	
	@Override
	public CandyObject mod(CNIEnv env, CandyObject operand) {
		if (isNumber(operand)) {
			NumberObj n = (NumberObj) operand;
			if (isDouble() || n.isDouble()) {
				return DoubleObj.valueOf(doubleValue() % n.doubleValue());
			}
			return IntegerObj.valueOf(intValue() % n.intValue());
		}
		return super.mod(env, operand);
	}

	@Override
	public BoolObj gt(CNIEnv env, CandyObject operand) {
		if (isNumber(operand)) {
			return BoolObj.valueOf(doubleValue() > ((NumberObj)operand).doubleValue());
		}
		return super.gt(env, operand);
	}
	
	@Override
	public BoolObj gteq(CNIEnv env, CandyObject operand) {
		if (isNumber(operand)) {
			return BoolObj.valueOf(doubleValue() >= ((NumberObj)operand).doubleValue());
		}
		return super.gteq(env, operand);
	}

	@Override
	public BoolObj lt(CNIEnv env, CandyObject operand) {
		if (isNumber(operand)) {
			return BoolObj.valueOf(doubleValue() < ((NumberObj)operand).doubleValue());
		}
		return super.lt(env, operand);
	}

	@Override
	public BoolObj lteq(CNIEnv env, CandyObject operand) {
		if (isNumber(operand)) {
			return BoolObj.valueOf(doubleValue() <= ((NumberObj)operand).doubleValue());
		}
		return super.lteq(env, operand);
	}

	@Override
	public BoolObj equals(CNIEnv env, CandyObject operand) {
		if (isNumber(operand)) {
			return BoolObj.valueOf(doubleValue() == ((NumberObj)operand).doubleValue());
		}
		return super.equals(env, operand);
	}
	
	@NativeMethod(name = "intVal")
	public CandyObject intValue(CNIEnv env, CandyObject[] args) {
		if (getCandyClass() == IntegerObj.INTEGER_CLASS) {
			return this;
		}
		return IntegerObj.valueOf(intValue());
	}
	
	@NativeMethod(name = "doubleVal")
	public CandyObject doubleValue(CNIEnv env, CandyObject[] args) {
		if (getCandyClass() == DoubleObj.DOUBLE_CLASS) {
			return this;
		}
		return DoubleObj.valueOf(doubleValue());
	}
}
