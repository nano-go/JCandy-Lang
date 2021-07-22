package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.NumberObj;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeClassRegister;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;

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
	public CandyObject add(VM vm, CandyObject operand) {
		if (isNumber(operand)) {
			NumberObj n = (NumberObj) operand;
			if (isDouble() || n.isDouble()) {
				return DoubleObj.valueOf(doubleValue() + n.doubleValue());
			}
			return IntegerObj.valueOf(intValue() + n.intValue());
		}
		return super.add(vm, operand);
	}

	@Override
	public CandyObject sub(VM vm, CandyObject operand) {
		if (isNumber(operand)) {
			NumberObj n = (NumberObj) operand;
			if (isDouble() || n.isDouble()) {
				return DoubleObj.valueOf(doubleValue() - n.doubleValue());
			}
			return IntegerObj.valueOf(intValue() - n.intValue());
		}
		return super.sub(vm, operand);
	}

	@Override
	public CandyObject mul(VM vm, CandyObject operand) {
		if (isNumber(operand)) {
			NumberObj n = (NumberObj) operand;
			if (isDouble() || n.isDouble()) {
				return DoubleObj.valueOf(doubleValue() * n.doubleValue());
			}
			return IntegerObj.valueOf(intValue() * n.intValue());
		}
		return super.mul(vm, operand);
	}

	@Override
	public CandyObject div(VM vm, CandyObject operand) {
		if (isNumber(operand)) {
			NumberObj n = (NumberObj) operand;
			if (isDouble() || n.isDouble()) {
				return DoubleObj.valueOf(doubleValue() / n.doubleValue());
			}
			return IntegerObj.valueOf(intValue() / n.intValue());
		}
		return super.div(vm, operand);
	}
	
	@Override
	public CandyObject mod(VM vm, CandyObject operand) {
		if (isNumber(operand)) {
			NumberObj n = (NumberObj) operand;
			if (isDouble() || n.isDouble()) {
				return DoubleObj.valueOf(doubleValue() % n.doubleValue());
			}
			return IntegerObj.valueOf(intValue() % n.intValue());
		}
		return super.mod(vm, operand);
	}

	@Override
	public BoolObj gt(VM vm, CandyObject operand) {
		if (isNumber(operand)) {
			return BoolObj.valueOf(doubleValue() > ((NumberObj)operand).doubleValue());
		}
		return super.gt(vm, operand);
	}
	
	@Override
	public BoolObj gteq(VM vm, CandyObject operand) {
		if (isNumber(operand)) {
			return BoolObj.valueOf(doubleValue() >= ((NumberObj)operand).doubleValue());
		}
		return super.gteq(vm, operand);
	}

	@Override
	public BoolObj lt(VM vm, CandyObject operand) {
		if (isNumber(operand)) {
			return BoolObj.valueOf(doubleValue() < ((NumberObj)operand).doubleValue());
		}
		return super.lt(vm, operand);
	}

	@Override
	public BoolObj lteq(VM vm, CandyObject operand) {
		if (isNumber(operand)) {
			return BoolObj.valueOf(doubleValue() <= ((NumberObj)operand).doubleValue());
		}
		return super.lteq(vm, operand);
	}

	@Override
	public BoolObj equals(VM vm, CandyObject operand) {
		if (isNumber(operand)) {
			return BoolObj.valueOf(doubleValue() == ((NumberObj)operand).doubleValue());
		}
		return super.equals(vm, operand);
	}
	
	@NativeMethod(name = "intVal")
	public CandyObject intValue(VM vm, CandyObject[] args) {
		if (getCandyClass() == IntegerObj.INTEGER_CLASS) {
			return this;
		}
		return IntegerObj.valueOf(intValue());
	}
	
	@NativeMethod(name = "doubleVal")
	public CandyObject doubleValue(VM vm, CandyObject[] args) {
		if (getCandyClass() == DoubleObj.DOUBLE_CLASS) {
			return this;
		}
		return DoubleObj.valueOf(doubleValue());
	}
}
