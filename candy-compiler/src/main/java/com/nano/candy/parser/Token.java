package com.nano.candy.parser;
import com.nano.candy.utils.Position;
import java.util.Objects;

public class Token {
	
	public static final String LN_SEMI_LITERAL = "end of line";
	
	public static boolean isNewLineSEMI(Token tok) {
		return tok.getKind() == TokenKind.SEMI && 
			tok.getLiteral().equals(LN_SEMI_LITERAL);
	}
	
	private final Position pos;
	private final String literal;
	private final TokenKind kind;
	
	public Token(Position pos, String literal, TokenKind kind) {
		this.pos = pos;
		this.literal = literal;
		this.kind = kind;
	}
	
	public Position getPos() {
		return pos;
	}

	public String getLiteral() {
		if (literal != null) {
			return literal;
		}
		return kind.literal;
	}

	public TokenKind getKind() {
		return kind;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Token) {
			Token tok = (Token) obj;
			return Objects.equals(pos, tok.getPos()) && 
			       Objects.equals(getLiteral(), tok.getLiteral()) && 
				   kind == tok.kind;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{token: ").append(kind);
		builder.append(String.format(", literal: \"%s\"", literal));
		if (pos != null) {
			builder.append(", row: ").append(pos.getLine());
			builder.append(", col: ").append(pos.getCol());
		}
		builder.append("}");
		return builder.toString();
	}
	
	public static class DoubleNumberToken extends Token {
		private double value;
		public DoubleNumberToken(Position pos, double value, String literal) {
			super(pos, literal, TokenKind.DOUBLE);
			this.value = value;
		}
		
		public double getValue() {
			return value;
		}
	}
	
	public static class IntegerNumberToken extends Token {
		private long value;
		public IntegerNumberToken(Position pos, long value, String literal) {
			super(pos, literal, TokenKind.INTEGER);
			this.value = value;
		}

		public long getValue() {
			return value;
		}
	}
	
}
