package com.nano.candy.parser;
import com.google.common.collect.ImmutableMap;
import com.nano.candy.utils.Position;
import java.util.Optional;
import java.util.Objects;

public class Token {

	private final Position pos;
	private final Optional<String> literal;
	private final TokenKind kind;
	
	public Token(Position pos, String literal, TokenKind kind) {
		this.pos = pos;
		this.literal = Optional.ofNullable(literal);
		this.kind = kind;
	}
	
	public Position getPos() {
		return pos;
	}

	public String getLiteral() {
		if (literal.isPresent()) {
			return literal.get();
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
			return    Objects.equals(pos, tok.getPos())
			       && Objects.equals(getLiteral(), tok.getLiteral())
				   && kind == tok.kind;
		}
		return false;
	}

	@Override
	public String toString() {
		return com.google.common.base.MoreObjects.toStringHelper(this)
			.add("pos", pos)
			.add("literal", getLiteral())
			.add("kind", kind)
			.toString();
	}
	
}
