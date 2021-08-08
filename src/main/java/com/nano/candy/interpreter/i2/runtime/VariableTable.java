package com.nano.candy.interpreter.i2.runtime;

import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class VariableTable {
	
	private ArrayList<String> variableNames;
	private HashMap<String, Integer> variableTable;
	
	private Variable[] vars;
	private int varibleSize;

	public VariableTable(int initialCapacity) {
		this.variableNames = new ArrayList<>(initialCapacity);
		this.variableTable = new HashMap<>(initialCapacity);
		this.vars = new Variable[initialCapacity];
		this.varibleSize = 0;
	}
	
	public VariableTable(ArrayList<String> variableNames, HashMap<String, Integer> variableTable) {
		this.variableNames = variableNames;
		this.variableTable = variableTable;
		this.vars = new Variable[variableNames.size()];
		this.varibleSize = vars.length;
	}
	
	public Collection<Variable> getVariables() {
		ArrayList<Variable> vars = new ArrayList<>(varibleSize);
		for (int i = 0; i < varibleSize; i ++) {
			if (this.vars[i] != null) {
				vars.add(this.vars[i]);
			}
		}
		return vars;
	}
	
	/**
	 * Sets the value to the variable specified by the index if the variable
	 * exists.
	 */
	public boolean setVariableIfExists(int index, CandyObject value) {
		if (vars[index] != null) {
			vars[index].setValue(value);
			return true;
		}
		return false;
	}

	/**
	 * Sets the value to the variable specified by the index.
	 *
	 * We assume the index is valid.
	 */
	public void setVariable(int index, CandyObject value) {
		if (vars[index] == null) {
			vars[index] = Variable.getVariable(variableNames.get(index), value);
		} else {
			vars[index].setValue(value);
		}
	}

	public CandyObject getVariableValue(int index) {
		return vars[index] == null ? null : vars[index].getValue();
	}

	public Variable getVariable(int index) {
		return vars[index];
	}

	public Variable getVariable(String name) {
		Integer index = variableTable.get(name);
		return index != null ? vars[index] : null;
	}

	public synchronized void defineCallable(CallableObj callableObj) {
		defineVariable(callableObj.funcName(), callableObj);
	}

	public synchronized void defineClass(CandyClass clazz) {
		defineVariable(clazz.getCandyClassName(), clazz);
	}

	public String getVariableName(int index) {
		if (index >= 0 && index < varibleSize) {
			return variableNames.get(index);
		}
		return null;
	}

	private int getIndex(String name) {
		Integer index = variableTable.get(name);
		if (index == null) {		
			if (varibleSize >= vars.length) {
				int newLength = vars.length < 2 ? 8 : (int)(vars.length*1.5);
				vars = Arrays.copyOf(vars, newLength);
			}
			index = varibleSize ++;
			variableNames.add(name);
			variableTable.put(name, index);
		}
		return index;
	}

	public void defineVariable(String name, CandyObject val) {
		int index = getIndex(name);
		vars[index] = Variable.getVariable(name, val);
	}

	public void defineVariable(Variable variable) {
		int index = getIndex(variable.getName());
		vars[index] = variable;
	}
	
	public void defineAll(VariableTable table) {
		for (int i = 0; i < table.varibleSize; i ++) {
			Variable v = table.vars[i];
			if (v != null) {
				defineVariable(v.getName(), v.getValue());
			}
		}
	}
}
