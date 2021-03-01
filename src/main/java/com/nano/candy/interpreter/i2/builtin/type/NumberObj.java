package com.nano.candy.interpreter.i2.builtin.type;
import com.nano.candy.interpreter.i2.builtin.BuiltinObject;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinClass;
import com.nano.candy.interpreter.i2.builtin.annotation.BuiltinMethod;
import com.nano.candy.interpreter.i2.builtin.type.classes.BuiltinClassFactory;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.vm.VM;

@BuiltinClass("Number")
public abstract class NumberObj extends BuiltinObject {
	
	public static final CandyClass NUMBER_CLASS = BuiltinClassFactory.generate(NumberObj.class);
	
	public NumberObj(CandyClass clazz) {
		super(clazz);
	}
	
	public static boolean isNumber(CandyObject obj) {
		return isNumber(obj.getCandyClass());
	}
	
	public static boolean isNumber(CandyClass clazz) {
		return clazz == IntegerObj.INTEGER_CLASS || clazz == DoubleObj.DOUBLE_CLASS;
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
	
	@BuiltinMethod(name = "intVal")
	public void intValue(VM vm) {
		if (getCandyClass() == IntegerObj.INTEGER_CLASS) {
			vm.returnFromVM(this);
			return;
		}
		vm.returnFromVM(IntegerObj.valueOf(intValue()));
	}
	
	@BuiltinMethod(name = "doubleVal")
	public void doubleValue(VM vm) {
		if (getCandyClass() == DoubleObj.DOUBLE_CLASS) {
			vm.returnFromVM(this);
			return;
		}
		vm.returnFromVM(DoubleObj.valueOf(doubleValue()));
	}
}
