package com.nano.candy.codegen;

import com.nano.candy.code.ConstantValue;
import java.util.ArrayList;
import java.util.HashMap;

import static com.nano.candy.code.ConstantValue.*;

class ConstantPool {
	
	private ArrayList<ConstantValue> constantPool = new ArrayList<>();
	
	private HashMap<ConstantValue, Integer> constantValueIndexMap = new HashMap<>();
	private HashMap<Long, Integer> integerConstantIndexMap = new HashMap<>();
	private HashMap<Double, Integer> doubleConstantIndexMap = new HashMap<>();
	private HashMap<String, Integer> utf8ConstantIndexMap = new HashMap<>();
	
	private <T> int index(HashMap<T, Integer> map, T value) {
		Integer index = map.get(value);
		if (index == null) {
			map.put(value, constantPool.size());
			return -1;
		}
		return index;
	}
	
	public int size() {
		return constantPool.size();
	}
	
	public int addConstantValue(ConstantValue cv) {
		Integer i = constantValueIndexMap.get(cv);
		if (i != null) {
			return i;
		}
		int index = constantPool.size();
		if (index >= 65536) {
			throw new Error("Too many constant value!");
		}
		constantValueIndexMap.put(cv, index);
		constantPool.add(cv);
		return index;
	}
	
	public int addString(String constant) {
		int index = index(utf8ConstantIndexMap, constant);
		if (index == -1) {
			return addConstantValue(new ConstantUtf8String(constant));
		}
		return index;
	}
	
	public int addInteger(long constant) {
		int index = index(integerConstantIndexMap, constant);
		if (index == -1) {
			return addConstantValue(new ConstantInteger(constant));
		}
		return index;
	}
	
	public int addDouble(double constant) {
		int index = index(doubleConstantIndexMap, constant);
		if (index == -1) {
			return addConstantValue(new ConstantDouble(constant));
		}
		return index;
	}
	
	public ConstantValue[] toConstants() {
		return constantPool.toArray(new ConstantValue[0]);
	}
}
