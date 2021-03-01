package com.nano.candy.interpreter.i2.rtda;

public class ConstantPool {
	
	private ConstantValue[] cp;

	public ConstantPool(ConstantValue[] cp) {
		this.cp = cp;
	}
	
	public ConstantValue[] getConstants() {
		return cp;
	}
	
	public ConstantValue.MethodInfo getMethodInfo(int index) {
		return (ConstantValue.MethodInfo)cp[index];
	}
	
	public ConstantValue.ClassInfo getClassInfo(int index) {
		return (ConstantValue.ClassInfo)cp[index];
	}
	
	public long getInteger(int index) {
		return ((ConstantValue.ConstantInteger)cp[index]).value();
	}
	
	public double getDouble(int index) {
		return ((ConstantValue.ConstantDouble)cp[index]).value();
	}
	
	public String getString(int index) {
		return ((ConstantValue.ConstantUtf8String)cp[index]).value();
	}
	
}
