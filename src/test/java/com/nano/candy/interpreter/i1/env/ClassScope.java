package com.nano.candy.interpreter.i1.env;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.parser.TokenKind;
import java.util.Optional;

/**
 * The implementation of a method in Candy is to bind a function to an
 * instance. When binding, the new scope is insert as the enclosing for the function
 * and the 'this' pointer will be defined to the new scope, but the new 
 * scope has only a 'this' pointer. So this class is for that.
 */
public class ClassScope implements Scope {
	
	private static final String THIS_KEYWORD = TokenKind.THIS.getLiteral();

	private Variable thisPointer;
	private Scope outter;

	public ClassScope(Scope outter, CandyObject instance) {
		this.outter = outter;
		this.thisPointer = new Variable(THIS_KEYWORD, this);
		this.thisPointer.setReference(instance);
	}
	
	@Override
	public Scope getOutterScope() {
		return outter;
	}

	@Override
	public Optional<Variable> lookupVariable(String name) {
		if (THIS_KEYWORD.hashCode() != name.hashCode() || !THIS_KEYWORD.equals(name)) {
			return outter.lookupVariable(name);
		}
		return Optional.of(thisPointer);
	}

	@Override
	public Optional<Variable> lookupVariableInCurrentScope(String name) {
		if (THIS_KEYWORD.hashCode() != name.hashCode() || !THIS_KEYWORD.equals(name)) {
			return Optional.empty();
		}
		return Optional.of(thisPointer);
	}

	@Override
	public Variable defineVariable(String name, CandyObject reference) {
		throw new Error("Can't define variable in the class scope: " + name);
	}
	
}
