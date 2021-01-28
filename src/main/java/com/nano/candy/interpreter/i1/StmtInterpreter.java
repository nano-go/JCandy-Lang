package com.nano.candy.interpreter.i1;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Stmt;
import com.nano.candy.interpreter.error.CandyRuntimeError;
import com.nano.candy.interpreter.i1.ControlFlowException.BreakException;
import com.nano.candy.interpreter.i1.ControlFlowException.ContinueException;
import com.nano.candy.interpreter.i1.ControlFlowException.ReturnException;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.classes.ObjectClass;
import com.nano.candy.interpreter.i1.builtin.type.Callable;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;
import com.nano.candy.interpreter.i1.builtin.type.CandyFunction;
import com.nano.candy.interpreter.i1.builtin.type.CandyMethod;
import com.nano.candy.interpreter.i1.builtin.type.Iterator;
import com.nano.candy.interpreter.i1.builtin.type.NullPointer;
import com.nano.candy.interpreter.i1.env.CommonScope;
import com.nano.candy.interpreter.i1.env.Environment;
import com.nano.candy.interpreter.i1.env.Scope;
import com.nano.candy.interpreter.i1.env.Variable;
import com.nano.candy.parser.TokenKind;
import java.util.HashMap;

public class StmtInterpreter {

	public void execute(AstInterpreter interpreter, Stmt.ClassDef node) {
		Environment env = interpreter.getEnvironment();
		Scope scope = env.getScope();
		
		CandyClass superClass = ObjectClass.getInstance();
		if (node.superClassName.isPresent()) {
			Expr.VarRef varRef = node.superClassName.get();
			CandyObject superClassObject = interpreter.evalExpr(varRef);
			// If the its '_class()' method returns itself, it means it's a class.
			// See CandyClass.java
			if (superClassObject._class() != superClassObject) {	
				throw new CandyRuntimeError(String.format(
					"The super name '%s' is not a class.",
					varRef.name
				));
			}
			superClass = superClassObject._class();
		}
		
		if (!superClass.isInheritable()) {
			throw new CandyRuntimeError(String.format(
				"The class '%s' can't be interited.",
				node.superClassName.get().name
			));
		}
		
		scope = new CommonScope(scope);
		scope.defineVariable(
			TokenKind.SUPER.getLiteral(), 
			superClass
		);
	
		HashMap<String, Callable> methods = new HashMap<>() ;
		for (Stmt.FuncDef funcDef : node.methods) {
			methods.put(funcDef.name.get(), 
				new CandyMethod(scope, funcDef)
			) ;
		}
		
		CandyMethod initializer = null;
		if (node.initializer.isPresent()) {
			initializer = new CandyMethod(scope, node.initializer.get());
		}
		
		scope = scope.getOutterScope();
		
		scope.defineVariable(
			node.name, 
			new CandyClass(node.name, superClass, methods, initializer)
		) ;
	}

	public void execute(AstInterpreter interpreter, Stmt.FuncDef node) {
		Scope current = interpreter.getEnvironment().getScope() ;
		current.defineVariable(
			node.name.get(),
			new CandyFunction(current, node)
		) ;
	}

	public void execute(AstInterpreter interpreter, Stmt.Return node) {
		CandyObject returnedObj = NullPointer.nil() ;
		if (node.expr.isPresent()) {
			returnedObj = interpreter.evalExpr(node.expr.get()) ;
		}
		throw new ReturnException(returnedObj) ; 
	}
	
	public void execute(AstInterpreter interpreter, Stmt.Break node) {
		throw new BreakException() ;
	}

	public void execute(AstInterpreter interpreter, Stmt.Continue node) {
		throw new ContinueException() ;
	}
	
	public void execute(AstInterpreter interpreter, Stmt.While node) {
		while (true) {
			try {
				CandyObject obj = interpreter.evalExpr(node.condition) ;
				if (!obj.booleanValue(interpreter).value()) {
					break ;
				}
				interpreter.executeStmt(node.body) ;
			} catch (BreakException e) {
				break ;
			} catch (ContinueException e) {
				continue ;
			}
		}
	}
	
	public void execute(AstInterpreter interpreter, Stmt.For node) {
		CandyObject iterable = interpreter.evalExpr(node.iterable);
		CandyObject iteratorObj = iterable.iterator(interpreter);
		
		interpreter.syncLocation(node);
		Iterator iterator = new Iterator(iteratorObj);
		
		Scope scope = interpreter.getEnvironment().enterScope();
		Variable iteratingVar = scope.defineVariable(
			node.iteratingVar, NullPointer.nil()
		);
		
		try {
			while (iterator.hasNext(interpreter)) {
				try {
					iteratingVar.setReference(iterator.next(interpreter));
					for (Stmt stmt : node.body.stmts) {
						interpreter.executeStmt(stmt);
					}
				} catch (BreakException e) {
					break;
				} catch (ContinueException e) {
					continue;
				}
			}
		} finally {
			interpreter.getEnvironment().exitScope();
		}
	}

	public void execute(AstInterpreter interpreter, Stmt.If node) {
		if (interpreter.evalExpr(node.condition).booleanValue().value()) {
			interpreter.executeStmt(node.thenBody) ;
		} else if (node.elseBody.isPresent()) {
			interpreter.executeStmt(node.elseBody.get()) ;
		}
	}

	public void execute(AstInterpreter interpreter, Stmt.Block node) {
		Environment env = interpreter.getEnvironment() ;
		env.enterScope();
		try {
			for (Stmt stmt : node.stmts) {
				interpreter.executeStmt(stmt);
			}
		} finally {
			env.exitScope();
		}
	}

	public void execute(AstInterpreter interpreter, Stmt.VarDef node) {
		CandyObject initializer = NullPointer.nil();
		if (node.init.isPresent()) {
			initializer = interpreter.evalExpr(node.init.get());
		}
		interpreter.getEnvironment().getScope().defineVariable(
			node.name, initializer
		) ;
	}

	public void execute(AstInterpreter interpreter, Stmt.Assert node) {
		if (!interpreter.evalExpr(node.condition).booleanValue().value()) {
			String errorMsg = "" ;
			if (node.errorInfo.isPresent()) {
				CandyObject errorInfo = interpreter.evalExpr(node.errorInfo.get());
				errorMsg = errorInfo.toString() ;
			}
			throw new com.nano.candy.interpreter.error.AssertionError(errorMsg);
		}
	}

	public void execute(AstInterpreter interpreter, Stmt.ExprS node) {
		CandyObject obj = interpreter.evalExpr(node.expr) ;
		if (interpreter.isInteratively()) {
			System.out.println(obj.stringValue().value()) ;
		}
	}
}
