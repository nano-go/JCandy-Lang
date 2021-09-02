package com.nano.candy.codegen;

import com.nano.candy.code.OpCodes;
import com.nano.candy.parser.TokenKind;

public class OperatorInstructionMap {
	
	private static final byte[] OPERATOR_INSTRUCTION_MAP = 
		new byte[TokenKind.values().length];
	
	static {
		map(TokenKind.PLUS, OpCodes.OP_ADD);
		map(TokenKind.PLUS_ASSIGN, OpCodes.OP_ADD);
		
		map(TokenKind.MINUS, OpCodes.OP_SUB);
		map(TokenKind.MINUS_ASSIGN, OpCodes.OP_SUB);
		
		map(TokenKind.STAR, OpCodes.OP_MUL);
		map(TokenKind.STAR_ASSIGN, OpCodes.OP_MUL);
		
		map(TokenKind.DIV, OpCodes.OP_DIV);
		map(TokenKind.DIV_ASSIGN, OpCodes.OP_DIV);
		
		map(TokenKind.MOD, OpCodes.OP_MOD);
		map(TokenKind.MOD_ASSIGN, OpCodes.OP_MOD);
		
		map(TokenKind.LEFT_SHIFT, OpCodes.OP_LS);
		map(TokenKind.LEFT_SHIFT_ASSIGN, OpCodes.OP_LS);
		
		map(TokenKind.RIGHT_SHIFT, OpCodes.OP_RS);
		map(TokenKind.RIGHT_SHIFT_ASSIGN, OpCodes.OP_RS);
		
		
		map(TokenKind.IS, OpCodes.OP_INSTANCE_OF);
		map(TokenKind.GT, OpCodes.OP_GT);
		map(TokenKind.GTEQ, OpCodes.OP_GTEQ);
		map(TokenKind.LT, OpCodes.OP_LT);
		map(TokenKind.LTEQ, OpCodes.OP_LTEQ);
		map(TokenKind.EQUAL, OpCodes.OP_EQ);
		map(TokenKind.NOT_EQUAL, OpCodes.OP_NOTEQ);
		
		map(TokenKind.DOT_DOT, OpCodes.OP_RANGE);
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
