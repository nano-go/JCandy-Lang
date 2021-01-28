package com.nano.candy.interpreter.error;
import com.nano.candy.interpreter.i1.builtin.CandyObject;
import com.nano.candy.interpreter.i1.builtin.type.CandyClass;
import com.nano.candy.parser.TokenKind;

public class UnsupportedOperationError extends CandyRuntimeError {
	
	public UnsupportedOperationError(TokenKind unary, CandyObject operand) {
		this(unary, operand._class());
	}

	public UnsupportedOperationError(TokenKind binary, CandyObject operand1, CandyObject operand2) {
		this(binary, operand1._class(), operand2._class());
	}
	
	public UnsupportedOperationError(TokenKind unary, CandyClass operandType) {
		this(unary, operandType, null);
	}

	public UnsupportedOperationError(TokenKind binary, CandyClass operand1Type, CandyClass operand2Type) {
		super(message(binary, operand1Type, operand2Type));
	}
	
	public UnsupportedOperationError(String msg, Object... args) {
		super(String.format(msg, args));
	}
	
	private static String message(TokenKind operator, CandyClass operand1, CandyClass operand2) {
		if (operand2 != null) {
			return String.format(
				"The binary operator '%s' can't apply to the instances of '%s' and '%s'.",
				operator.getLiteral(),
				operand1.getClassName(), operand2.getClassName()
			) ;
		} else {
			return String.format(
				"The unary operator '%s' can't apply to the instance of '%s'.",
				operator.getLiteral(),
				operand1.getClassName()
			) ;
		}
	}
	
}
