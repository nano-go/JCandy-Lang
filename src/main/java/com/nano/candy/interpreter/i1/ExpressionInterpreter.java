package com.nano.candy.interpreter.i1;
import com.nano.candy.ast.Expr;
import com.nano.candy.interpreter.error.ArgumentError;
import com.nano.candy.interpreter.error.AttributeError;
import com.nano.candy.interpreter.error.CandyRuntimeError;
import com.nano.candy.interpreter.error.TypeError;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.ArrayObject;
import com.nano.candy.interpreter.i1.builtin.type.BooleanObject;
import com.nano.candy.interpreter.i1.builtin.type.Callable;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;
import com.nano.candy.interpreter.i1.builtin.type.CandyFunction;
import com.nano.candy.interpreter.i1.builtin.type.DoubleObject;
import com.nano.candy.interpreter.i1.builtin.type.IntegerObject;
import com.nano.candy.interpreter.i1.builtin.type.NullPointer;
import com.nano.candy.interpreter.i1.builtin.type.StringObject;
import com.nano.candy.interpreter.i1.env.Environment;
import com.nano.candy.interpreter.i1.env.Scope;
import com.nano.candy.interpreter.i1.env.Variable;
import com.nano.candy.parser.TokenKind;
import java.util.ArrayList;
import java.util.Optional;

public class ExpressionInterpreter {
	
	public static final String THIS = TokenKind.THIS.getLiteral();
	public static final String SUPER = TokenKind.SUPER.getLiteral();
	
	private Variable lookupVariable(Environment env, String name, Expr node) {
		Optional<Variable> variable = env.getScopeAt(node).lookupVariable(name) ;
		if (!variable.isPresent()) {
			throw new CandyRuntimeError("The veriable '%s' not found.", name);
		}
		return variable.get();
	}
	
	public CandyObject eval(AstInterpreter interpreter, Expr.Lambda node) {
		return new CandyFunction(
			interpreter.getEnvironment().getScope(),
			node.funcDef
		);
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.CallFunc node) {
		CandyObject obj = interpreter.evalExpr(node.expr);
		
		// Syncronizes position to '('
		interpreter.syncLocation(node);
		
		Callable callable = TypeError.checkCallable(obj);
		ArgumentError.checkArguments(callable.arity(), node.arguments.size());
		
		CandyObject[] args = new CandyObject[node.arguments.size()];
		int i = 0;
		for (Expr argument : node.arguments) {
			args[i ++] = interpreter.evalExpr(argument) ;
		}
		return callable.onCall(interpreter, args) ;
	}
	
	public CandyObject eval(AstInterpreter interpreter, Expr.GetAttr node) {
		CandyObject obj = interpreter.evalExpr(node.objExpr);
		CandyObject value = obj.getAttr(interpreter, node.attr);
		AttributeError.requiresAttrNonNull(obj._class(), node.attr, value);
		return value;
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.GetItem node) {
		CandyObject obj = interpreter.evalExpr(node.objExpr);
		CandyObject key = interpreter.evalExpr(node.key);
		return obj.getItem(interpreter, key);
	}
	
	public CandyObject eval(AstInterpreter interpreter, Expr.SetItem node) {
		CandyObject obj = interpreter.evalExpr(node.objExpr.objExpr) ;
		CandyObject key = interpreter.evalExpr(node.objExpr.key);
		
		if (node.assignOperator == TokenKind.ASSIGN) {
			CandyObject rhs = interpreter.evalExpr(node.rhs) ;
			obj.setItem(interpreter, key, rhs) ;
			return rhs;
		}
		
		CandyObject lhs = obj.getItem(key);
		CandyObject rhs = assignOperation(
			interpreter, node.assignOperator, lhs, node.rhs
		) ;
		obj.setItem(interpreter, key, rhs) ;
		return rhs;
	}
	
	public CandyObject eval(AstInterpreter interpreter, Expr.SetAttr node) {
		CandyObject obj = interpreter.evalExpr(node.objExpr.objExpr) ;
		
		if (node.assignOperator == TokenKind.ASSIGN) {
			CandyObject value = interpreter.evalExpr(node.rhs);
			obj.setAttr(interpreter, node.objExpr.attr, value);
			return value;
		}
		
		CandyObject attrValue = obj.getAttr(node.objExpr.attr);
		AttributeError.requiresAttrNonNull(
			obj._class(), node.objExpr.attr, attrValue
		);
		
		CandyObject value = assignOperation(
			interpreter, node.assignOperator, attrValue, node.rhs
		);
		obj.setAttr(interpreter, node.objExpr.attr, value);
		return value;
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.Assign node) {
		Variable variable = lookupVariable(
			interpreter.getEnvironment(),
			node.name, node
		) ;
		CandyObject varRef = variable.getReference() ;
		CandyObject value = assignOperation(
			interpreter, node.assignOperator, varRef, node.rhs
		) ;
		variable.setReference(value) ;
		return value ;
	}

	private CandyObject assignOperation(AstInterpreter interpreter, 
	                                    TokenKind assOperator, 
							            CandyObject lhsObj, 
							            Expr rhsExpr) {
		CandyObject rhsObj = interpreter.evalExpr(rhsExpr);
		switch (assOperator) {
			case PLUS_ASSIGN:
				rhsObj = lhsObj.plus(interpreter, rhsObj) ;
				break ;
			case MINUS_ASSIGN:
				rhsObj = lhsObj.subtract(rhsObj) ;
				break ;
			case STAR_ASSIGN:
				rhsObj = lhsObj.times(rhsObj) ;
				break ;
			case DIV_ASSIGN:
				rhsObj = lhsObj.divide(rhsObj) ;
				break ;
			case MOD_ASSIGN:
				rhsObj = lhsObj.mod(rhsObj) ;
				break ;
			case ASSIGN: break ;
			default:
				throw new Error("Unknown operator: " + assOperator.getLiteral()) ;
		}
		return rhsObj ;
	}
	
	public CandyObject eval(AstInterpreter interpreter, Expr.Array node) {
		ArrayList<CandyObject> elements = new ArrayList<>(node.elements.size());
		for (Expr e : node.elements) {
			elements.add(interpreter.evalExpr(e));
		}
		return new ArrayObject(elements);
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.DoubleLiteral node) {
		return new DoubleObject(node.value);
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.IntegerLiteral node) {
		return new IntegerObject(node.value);
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.StringLiteral node) {
		return StringObject.of(node.literal) ;
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.BooleanLiteral node) {
		return BooleanObject.valueOf(node.value) ;
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.NullLiteral node) {
		return NullPointer.nil();
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.VarRef node) {
		return lookupVariable(
			interpreter.getEnvironment(), node.name, node
		).getReference();
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.This node) {
		return lookupVariable(
			interpreter.getEnvironment(), THIS, node
		).getReference();
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.Super node) {
		Environment env = interpreter.getEnvironment();
		int distance = env.getDistance(node);
		Optional<Variable> superRef = env.getScopeAt(distance).lookupVariable(SUPER);
		
		if (!superRef.isPresent()) {
			throw new Error("'super' not found.");
		}
		
		CandyClass superClass = superRef.get().getReference();
		Scope scope = interpreter.getEnvironment().getScopeAt(distance - 1);
		Optional<Variable> instance = scope.lookupVariable(THIS);
		if (!instance.isPresent()) {
			throw new Error("'this' not found.");
		}
		
		return (CandyObject) superClass.findMethod(node.reference)
			.bindToInstance(instance.get().getReference());
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.Unary node) {
		CandyObject operand = interpreter.evalExpr(node.expr);
		switch (node.operator) {
			case MINUS: 
				return operand.negative();
			case PLUS:
				return operand.positive();
			case NOT:
				return operand.not(interpreter);
		}
		throw new Error("Unknown operator: " + node.operator);
	}

	public CandyObject eval(AstInterpreter interpreter, Expr.Binary node) {
		CandyObject left = interpreter.evalExpr(node.left);
		interpreter.syncLocation(node.operatorPos);

		switch (node.operator) {
			case LOGICAL_OR:
			case LOGICAL_AND:
				return evalLogicalOperation(interpreter, node, left);
		}

		CandyObject right = interpreter.evalExpr(node.right);
		interpreter.syncLocation(node.operatorPos);
		
		switch (node.operator) {
			case IS:
				return BooleanObject.valueOf(left.instanceOf(right._class()));
			case PLUS:
				return left.plus(interpreter, right);
			case MINUS:
				return left.subtract(right);	
			case STAR:
				return left.times(right);	
			case DIV:
				return left.divide(right);
			case MOD:
				return left.mod(right);
			case GT:
				return left.greaterThan(right);
			case GTEQ:
				return left.greaterThanOrEqualTo(right);
			case LT:
				return left.lessThan(right);
			case LTEQ:
				return left.lessThanOrEqualTo(right);
			case EQUAL:
				return left.equalTo(right);
			case NOT_EQUAL:
				return left.notEqualTo(right);
		}
		throw new Error("Unknown operator: " + node.operator);
	}

	private CandyObject evalLogicalOperation(AstInterpreter interpreter, Expr.Binary node, CandyObject left) {
		if (node.operator == TokenKind.LOGICAL_OR) {
			if (left.booleanValue(interpreter).value()) 
				return left;
		} else {
			if (!left.booleanValue(interpreter).value()) 
				return left;
		}
		return interpreter.evalExpr(node.right);
	}

}
