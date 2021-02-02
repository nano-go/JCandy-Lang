package com.nano.candy.parser;
import com.nano.candy.config.Config;
import com.nano.candy.utils.Characters;
import com.nano.candy.utils.Position;

class CandyScanner implements Scanner {

	private SourceCodeReader reader;
	private Position startPos;
	private Token tok;
	
	private Position basePos;
	
	/**
	 * If true, '\n' or EOF will be replaced with SEMI.
	 */
	private boolean insertSemi;

	protected CandyScanner(String fileName, char[] in) {
		reader = new SourceCodeReader(fileName, in);
		basePos = reader.pos();
		nextToken();
	}

	@Override
	public Position basePos() {
		return basePos;
	}
	
	@Override
	public Token peek() {
		return tok;
	}

	@Override
	public boolean hasNextToken() {
		return tok.getKind() != TokenKind.EOF;
	}

	@Override
	public Token nextToken() {
		this.tok = privateNextToken();
		return tok;
	}

	public Token privateNextToken() {
		loop: while (true) {
			skipWhitespace();
			
			char ch = reader.peek();
			this.startPos = reader.pos();
			
			if (Characters.isCandyIdentifierStart(ch)) {
				Token identifierToken = readIdentifier();
				insertSemi = shouldInsertSemi(identifierToken);
				return identifierToken;
			}
			
			if (Characters.isDigit(ch)) {
				this.insertSemi = true;
				Token tok = readNumberLiteral();
				if (tok == null) continue loop;
				return tok;
			}
			
			reader.consume();
			TokenKind kind;
			String literal = null;
			boolean isInsertSemi = false;
			
			switch (ch) {
				case Characters.EOF :
					kind = insertSemi ? TokenKind.SEMI : TokenKind.EOF;
					literal = "EOF";
					break;
					
				case '\n' :
					kind = TokenKind.SEMI;
					literal = Config.END_OF_LINE;
					break;
					
				case '"':
					isInsertSemi = true;
					literal = readStringLiteral();
					if (literal == null) {
						continue loop;
					}
					kind = TokenKind.STRING;
					break;
				
				case '/' :
					ch = reader.peek();
					if (ch == '/') {
						readSingleLineComment();
						continue loop;
					} else if (ch == '*') {
						readMultiLineComment();
						continue loop;
					}
					kind = switch2('=', TokenKind.DIV_ASSIGN, TokenKind.DIV);
					break;
					
				case '&' :
					ch = reader.peek();
					if (ch != '&') {
						reader.error(startPos, "Unknown character: '%c'", ch);
						continue loop;
					}
					reader.consume();
					literal = "&&";
					kind = TokenKind.LOGICAL_AND;
					break;
					
				case '|' :
					ch = reader.peek();
					if (ch != '|') {
						reader.error(startPos, "Unknown character: '%c'", ch);
						continue loop;
					}
					reader.consume();
					literal = "||";
					kind = TokenKind.LOGICAL_OR;
					break;
					
				case '!' :
					kind = switch2('=', TokenKind.NOT_EQUAL, TokenKind.NOT);
					break;		
				case '>' :
					kind = switch2('=', TokenKind.GTEQ, TokenKind.GT);
					break;	
				case '<' :
					kind = switch2('=', TokenKind.LTEQ, TokenKind.LT);	
					break;
				case '+' :
					kind = switch2('=', TokenKind.PLUS_ASSIGN, TokenKind.PLUS);
					break;	
				case '-' :
					kind = switch3('=', TokenKind.MINUS_ASSIGN, '>', TokenKind.ARROW, TokenKind.MINUS);
					break;
				case '*' :
					kind = switch2('=', TokenKind.STAR_ASSIGN, TokenKind.STAR);
					break;	
				case '%' :
					kind = switch2('=', TokenKind.MOD_ASSIGN, TokenKind.MOD);
					break;		
				case '=' :
					kind = switch2('=', TokenKind.EQUAL, TokenKind.ASSIGN);
					break;	
					
				
				case '{' :
					kind = TokenKind.LBRACE;
					break;
				case '}' :
					kind = TokenKind.RBRACE;
					break;				
				case '[':
					kind = TokenKind.LBRACKET;
					break;					
				case ']':
					isInsertSemi = true;
					kind = TokenKind.RBRACKET;
					break;				
				case '(' :	
					kind = TokenKind.LPAREN;
					break;				
				case ')' :
					isInsertSemi = true;
					kind = TokenKind.RPAREN;
					break;				
				case ';' :
					kind = TokenKind.SEMI;
					break;			
				case ':' :
					kind = TokenKind.COLON;
					break;					
				case ',' :
					kind = TokenKind.COMMA;
					break;					
				case '.' :
					kind = TokenKind.DOT;
					break;
					
				default :
					reader.error(startPos, "Unknown character: '%c'", ch);
					continue loop;
			}
			this.insertSemi = isInsertSemi;
			return new Token(startPos, literal, kind);
		}
	}

	private boolean shouldInsertSemi(Token identifierToken) {
		switch (identifierToken.getKind()) {
			case IDENTIFIER: 
			case TRUE:
			case FALSE:
			case NULL:
			case BREAK:
			case CONTINUE:
			case RETURN:
				return true;
		}
		return false;
	}
	
	private void skipWhitespace() {
		while (true) {
			switch (reader.peek()) {
				case ' ':
				case '\t':
				case '\r':
					reader.consume();
					continue;
				case '\n':
					if (insertSemi) return;
					reader.consume();
					continue;
			}
			return;
		}
	}
	
	private TokenKind switch2(char case1, TokenKind result, TokenKind def) {
		if (reader.peek() == case1) {
			reader.consume();
			return result;
		}
		return def;
	}
	
	private TokenKind switch3(char case1, TokenKind result1, char case2, TokenKind result2, TokenKind def) {
		if (reader.peek() == case1) {
			reader.consume();
			return result1;
		} else if (reader.peek() == case2) {
			reader.consume();
			return result2;
		}
		return def;
	}
	
	private Token readIdentifier() {
		reader.putChar(true);
		while (Characters.isCandyIdentifier(reader.peek())) {
			reader.putChar(true);
		}
		String identifier = reader.savedString();
		return new Token(startPos, identifier, TokenKind.lookupKind(identifier));
	}

	private Token readNumberLiteral() {
		reader.putChar(true);
		boolean hasRadixPoint = false;	
		for (;;) {
			if (Characters.isDigit(reader.peek())) {
				reader.putChar(true);
			} else if (reader.peek() == '_') {
				reader.consume();
			} else break;
		}
		hasRadixPoint = reader.peek() == '.';
		if (hasRadixPoint) {	
			reader.putChar(true);
			for (;;) {
				if (Characters.isDigit(reader.peek())) {
					reader.putChar(true);
				} else if (reader.peek() == '_') {
					reader.consume();
				} else break;
			}
		}
			
		return new Token(
			startPos, reader.savedString(), 
			hasRadixPoint ? TokenKind.DOUBLE : TokenKind.INTEGER
		);
	}
	
	private String readStringLiteral() {
		char ch = reader.peek();
		while (ch != '"') {
			if (reader.isAtEnd() || ch == '\n') {
				reader.error("Unterminated string literal.");
				return null;
			}
			reader.putChar(reader.escapeChar());
			ch = reader.readNextChar();
		}
		reader.consume();
		return reader.savedString();
	}
	
	private void readSingleLineComment() {
		reader.consume();
		char ch = reader.peek();
		while (ch != '\n' && ch != Characters.EOF) {
			ch = reader.readNextChar();
		}
	}
	
	private void readMultiLineComment() {
		reader.consume();
		while (true) {
			if (reader.isAtEnd()) {
				reader.error("Unterminated comment.");
				return;
			}
			char ch = reader.peek();
			reader.consume();
			if (ch == '*') {
				if (reader.peek() == '/') {
					reader.consume();
					return;
				}
			}
		}
	}

}
