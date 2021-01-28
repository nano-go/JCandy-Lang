package com.nano.candy.interpreter.i1.env;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import java.util.HashMap;
import java.util.Optional;

public class CommonScope implements Scope {
	
	private HashMap<String, Variable> definedVariables; 
	private Scope outter;
	
	public CommonScope(Scope outter) {
		this.definedVariables = new HashMap<>();
		this.outter = outter;
	}
	
	@Override
	public Scope getOutterScope() {
		return outter;
	}

	@Override
	public Optional<Variable> lookupVariable(String name) {
		Optional<Variable> variable = lookupVariableInCurrentScope(name);
		if (!variable.isPresent() && outter != null) {
			return outter.lookupVariable(name);
		}
		return variable;
	}

	@Override
	public Optional<Variable> lookupVariableInCurrentScope(String name) {
		return Optional.ofNullable(definedVariables.get(name));
	}

	@Override
	public Variable defineVariable(String name, CandyObject reference) {
		Variable var = definedVariables.get(name);
		if (var == null) {
			var = defineVariableInCurrentScope(name, reference);
			definedVariables.put(name, var);
		} else {
			var.setReference(reference);
		}
		return var;
	}
	
	protected Variable defineVariableInCurrentScope(String name, CandyObject reference) {
		Variable var = new Variable(name, this, reference);
		return var;
	}
    
}
