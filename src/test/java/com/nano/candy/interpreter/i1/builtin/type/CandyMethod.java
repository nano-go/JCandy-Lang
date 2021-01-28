package com.nano.candy.interpreter.i1.builtin.type;

import com.nano.candy.ast.Stmt;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.env.ClassScope;
import com.nano.candy.interpreter.i1.env.Scope;

public class CandyMethod extends CandyFunction {
    
	public CandyMethod(Scope enclosing, Stmt.FuncDef node) {
		super(enclosing, node);
	}
	
	/**
	 * Binds a function to an instance, only add to the "this" variable.
	 */
	@Override
	public Callable bindToInstance(CandyObject instance) {
		return new CandyFunction(new ClassScope(enclosing, instance), node) ;
	}
	
}
