package com.nano.candy.interpreter.i2.codegen;
import com.nano.candy.parser.TokenKind;
import com.nano.candy.interpreter.i2.instruction.Instructions;

public class OperatorInstructionMap {
	
	private static final byte[] OPERATOR_INSTRUCTION_MAP = 
		new byte[TokenKind.values().length];
		
	static {
		map(TokenKind.PLUS, Instructions.OP_ADD);
		map(TokenKind.PLUS_ASSIGN, Instructions.OP_ADD);
		
		map(TokenKind.MINUS, Instructions.OP_SUB);
		map(TokenKind.MINUS_ASSIGN, Instructions.OP_SUB);
		
		map(TokenKind.STAR, Instructions.OP_MUL);
		map(TokenKind.STAR_ASSIGN, Instructions.OP_MUL);
		
		map(TokenKind.DIV, Instructions.OP_DIV);
		map(TokenKind.DIV_ASSIGN, Instructions.OP_DIV);
		
		map(TokenKind.MOD, Instructions.OP_MOD);
		map(TokenKind.MOD_ASSIGN, Instructions.OP_MOD);
		
		map(TokenKind.IS, Instructions.OP_INSTANCE_OF);
		map(TokenKind.GT, Instructions.OP_GT);
		map(TokenKind.GTEQ, Instructions.OP_GTEQ);
		map(TokenKind.LT, Instructions.OP_LT);
		map(TokenKind.LTEQ, Instructions.OP_LTEQ);
		map(TokenKind.EQUAL, Instructions.OP_EQ);
		map(TokenKind.NOT_EQUAL, Instructions.OP_NOTEQ);
	}
		
	private static void map(TokenKind binaryOp, byte instruction) {
		OPERATOR_INSTRUCTION_MAP[binaryOp.ordinal()] = instruction;
	}
	
	public static byte lookupOperatorIns(TokenKind operator) {
		byte opcode = OPERATOR_INSTRUCTION_MAP[operator.ordinal()];
		if (opcode == 0) {
			// Unreachable
			throw new Error("Unsupported Operator " + operator.getLiteral());
		}
		return opcode;
	}
	
	private OperatorInstructionMap() {}
}
