package com.nano.candy.interpreter.i1.builtin.type;

import com.nano.candy.ast.Stmt;
import com.nano.candy.interpreter.i1.AstInterpreter;
import com.nano.candy.interpreter.i1.ControlFlowException.ReturnException;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.NullPointer;
import com.nano.candy.interpreter.i1.env.CommonScope;
import com.nano.candy.interpreter.i1.env.Environment;
import com.nano.candy.interpreter.i1.env.Scope;

public class CandyFunction extends CallableObject {

	protected final Scope enclosing;
	protected final Stmt.FuncDef node; 

	public CandyFunction(Scope enclosing, Stmt.FuncDef node) {
		super(node.params.size());
		this.enclosing = enclosing;
		this.node = node;
	}
	
	@Override
	public CandyObject onCall(AstInterpreter interpreter, CandyObject[] args) {
		Environment env = interpreter.getEnvironment();
		Scope originEnvScope = env.getScope();
		Scope scope = env.enterScope(new CommonScope(enclosing));
		for (int i = 0; i < args.length; i++) {
			scope.defineVariable(node.params.get(i), args[i]);
		}
		CandyObject returnedObj = NullPointer.nil();
		try {
			for (Stmt stmt : node.body.stmts) {
				interpreter.executeStmt(stmt);
			}
		} catch (ReturnException e) {
			returnedObj = e.returnedObj;
		} finally {
			env.setScope(originEnvScope);
		}
		return returnedObj;
	}
	
}
