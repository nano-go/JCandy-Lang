package com.nano.candy.parser;

import com.nano.candy.sys.CandySystem;
import com.nano.candy.utils.Characters;
import com.nano.candy.utils.Position;
import java.util.LinkedList;
import java.util.Queue;

class CandyScanner implements Scanner {

	private SourceCodeReader reader;
	private Position startPos;
	private Token tok;
	
	
	private Queue<Token> parsedToken = new LinkedList<>();
	
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
		if (!parsedToken.isEmpty()) {
			this.tok = parsedToken.poll();
		} else {
			this.tok = privateNextToken();
		}
		return tok;
	}

	public Token privateNextToken() {
		scanAgain: while (true) {
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
				if (tok == null) {
					continue scanAgain;
				}
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
					literal = CandySystem.END_OF_LINE;
					break;
					
				case '@':
					isInsertSemi = true;
					kind = TokenKind.AT_IDENTIFIER;
					literal = readAtIdentifier();
					break;
					
				case '"':
					readStringLiteral(false);
					if (parsedToken.isEmpty()) {
						continue scanAgain;
					} else {
						this.insertSemi = true;
						return parsedToken.poll();
					}
					
				case '/' :
					ch = reader.peek();
					if (ch == '/') {
						skipSingleLineComment();
						continue scanAgain;
					} else if (ch == '*') {
						skipMultiLineComment();
						continue scanAgain;
					}
					kind = switch2('=', TokenKind.DIV_ASSIGN, TokenKind.DIV);
					break;
					
				case '&' :
					ch = reader.peek();
					if (ch != '&') {
						reader.error(startPos, "Unknown character: '%c'", ch);
						continue scanAgain;
					}
					reader.consume();
					literal = "&&";
					kind = TokenKind.LOGICAL_AND;
					break;
					
				case '|' :
					ch = reader.peek();
					if (ch != '|') {
						kind = TokenKind.BIT_OR;
						break;
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
				case '?':
					kind = TokenKind.QUESITION;
					break;			
				case ':' :
					kind = TokenKind.COLON;
					break;					
				case ',' :
					kind = TokenKind.COMMA;
					break;					
				case '.' :
					kind = TokenKind.DOT;
					if (reader.peek() == '.') {
						reader.consume();
						kind = TokenKind.DOT_DOT;
					}
					break;
				default :
					reader.error(startPos, "Unknown character: '%c'", ch);
					continue scanAgain;
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
			case THIS:
				return true;
		}
		return false;
	}
	
	private void skipWhitespace() {
		skipWhitespace(false);
	}
	
	private void skipWhitespace(boolean stopAtNewLine) {
		while (true) {
			switch (reader.peek()) {
				case ' ':
				case '\t':
				case '\r':
					reader.consume();
					continue;
				case '\n':
					if (insertSemi || stopAtNewLine) return;
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
	
	private String readAtIdentifier() {
		if (!Characters.isCandyIdentifierStart(reader.peek())) {
			reader.error("Unexpected '%s'.", reader.peek());
			return "";
		}
		do {
			reader.putChar(true);
		} while (Characters.isCandyIdentifier(reader.peek()));
		return reader.savedString();
	}
	
	private Token readIdentifier() {
		reader.putChar(true);
		while (Characters.isCandyIdentifier(reader.peek())) {
			reader.putChar(true);
		}
		String identifier = reader.savedString();
		return new Token(startPos, identifier, TokenKind.lookupKind(identifier));
	}
	
	private String baseName(int base) {
		switch (base) {
			case 2:
				return "binary";
			case 8:
				return "octal";
			case 10:
				return "decimal";
			case 16:
				return "hexadecimal";
		}
		// Unreachable
		throw new Error("unknown base: " + base);
	}
	
	private boolean digits(int base) {
		boolean hasError = false;
		for (;;) {
			char ch = reader.peek();
			int point = base;
			
			if (ch >= '0' && ch <= '9') {
				point = ch - '0';
			} else if (reader.peek() == '_') {
				reader.consume();
				continue;
			} else {
				char lower = Characters.lower(ch);
				if ('a' <= lower && lower <= 'f') {
					point = lower - 'a' + 10;
				} else break;
			}
			
			if (point >= base) {
				reader.error("Invalid digit '%c' in %s literal.",
					ch, baseName(base)
				);
				hasError = true;	
			} else {
				reader.putChar(ch);
			}
			reader.consume();
		}
		return hasError;
	}
	
	private Token readNumberLiteral() {
		char ch = reader.peek();
		int base = 10;
		boolean hasPrefix = false;
		
		if (ch == '0') {
			reader.putChar(true);
			switch (reader.peek()) {
				case 'x': case 'X':
					hasPrefix = true;
					reader.putChar(true);
					base = 16;
					break;
				case 'o': case 'O':
					hasPrefix = true;
					reader.putChar(true);
					base = 8;
					break;
				case 'b': case 'B':
					hasPrefix = true;
					reader.putChar(true);
					base = 2;
					break;
				case '.': break;
				default: base = 8;
			}
		} else {
			reader.putChar(true);
		}
		
		boolean hasError = digits(base);
		boolean hasRadixPoint = false;
		if (reader.peek() == '.') {
			Position pos = reader.pos();
			reader.consume();
			if (Characters.isDigit(reader.peek()) || reader.peek() == '_') {
				hasRadixPoint = true;
				reader.putChar('.');
				if (base != 10) {
					reader.error(pos,
						"Invalid radix point in %s literal.", 
						baseName(base)
					);
					hasError = true;
				}
				hasError = digits(base) || hasError;
			} else { 
				// 123. will be recognized as a number and a dot.
				TokenKind kind = TokenKind.DOT;
				if (reader.peek() == '.') {
					kind = TokenKind.DOT_DOT;
					reader.consume();
				}
				this.insertSemi = false;
				parsedToken.add(
					new Token(pos, kind.getLiteral(), kind));
			}
		}
		
		if (hasError) {
			return null;
		}
		
		return toNumberToken(
			reader.savedString(), 
			hasPrefix, hasRadixPoint, base
		);
	}
	
	private Token toNumberToken(String lit, boolean hasPrefix, boolean hasRadixPoint, int base) {
		String numberLit = lit;
		if (hasPrefix) {
			// remove the prefix of the literal such as 0x...
			numberLit = lit.substring(2);
			// valueOf("") will be error. replace it with "0"
			if (numberLit.length() == 0) {
				numberLit = "0";
			}
		}
		
		try {
			if (hasRadixPoint) {
				double value = Double.valueOf(numberLit);
				return new Token.DoubleNumberToken(
					startPos, value, lit
				);
			}
			long value = Long.valueOf(numberLit, base);
			return new Token.IntegerNumberToken(
				startPos, value, lit
			);
		} catch (NumberFormatException e) {
			reader.error(startPos, "Invalid number literal '%s'", lit);
			return null;
		}
	}
	
	private void readStringLiteral(boolean inInterpolatedStr) {
		char ch = reader.peek();
		outloop: while (true) {
			switch (ch) {
				case '\n': case Characters.EOF:
					reader.error("Unterminated string literal.");
					parsedToken.clear();
					return;
				
				case '"':
					if (inInterpolatedStr) {
						reader.error("Unterminated string literal in " + 
							" the interpolated string.");
						return;
					}
					break outloop;
					
				case '$':
					reader.consume();
					if (reader.peek() == '{') {
						reader.consume();
						readInterpolatedString();
						startPos = reader.pos();	
					} else {
						reader.error("Expect '{' after '$'.");
					}
					ch = reader.peek();
					break;
					
				default:
					ch = reader.escapeChar();
					if (!inInterpolatedStr || ch != '"') {
						reader.putChar(ch);
						ch = reader.readNextChar();
					} else {
						break outloop;
					}	
			}
		}
		reader.consume();
		parsedToken.offer(
			new Token(startPos, reader.savedString(), TokenKind.STRING));
	}

	/**
	 * If we find an interpolated string, we treat the saved string as a 
	 * INTERPOLATION and the interpolation between '${' and '}' will be 
	 * treat as a serial of the normal token and put them into the 
	 * 'parsedTokens' list.
	 *
	 * This string:
	 *
	 *     "abcd${a + b}"
	 *
	 * is tokenized to:
	 *
	 *     (INTERPOLATION, "abcd")
	 *     (IDENTIFIER,    "a")
	 *     (PLUS,          "+")
	 *     (IDENTIFIER,    "b")
	 *     (STRING,        "")
	 *
	 * The last empty string token tells Parser to end the interpolated
	 * string parsing.
	 *
	 * If you want to use string literals in the interpolation, you would use
	 * '\' to escape '"'.
	 */
	private void readInterpolatedString() {
		parsedToken.offer(
			new Token(startPos, reader.savedString(), TokenKind.INTERPOLATION));
		int previousNumToks = parsedToken.size();
		outloop: while (true) {
			skipWhitespace(true);
			switch (reader.peek()) {
				case '\n': case Characters.EOF: 
				case '"':
					reader.error("Missing '}'.");
					break outloop;
					
				case '}':
					if (previousNumToks == parsedToken.size()) {
						reader.error("Empty interpolation string.");
						// Insert an empty string to prevent errors in Parser.
						parsedToken.offer(
							new Token(reader.pos(), "", TokenKind.STRING));
					}
					reader.consume();
					break outloop;
				
				case '\\':
					reader.consume();
					readEscapeInInterpolationContext();
					break;
					
				default: 
					parsedToken.offer(privateNextToken());
					break;
			}
		}
	}

	private void readEscapeInInterpolationContext() {
		switch (reader.peek()) {
			case '"':
				reader.consume();
				readStringLiteral(true);
				break;
				
			case '{': case '}':
				parsedToken.offer(privateNextToken());
				break;
				
			default:
				reader.error(
					"Can't escape char %s in interpolation contexts.", 
					reader.peek()
				);
		}
	}
	
	private void skipSingleLineComment() {
		// consume '/'
		reader.consume();
		char ch = reader.peek();
		while (ch != '\n' && ch != Characters.EOF) {
			ch = reader.readNextChar();
		}
	}
	
	private void skipMultiLineComment() {
		// consume '*'
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
