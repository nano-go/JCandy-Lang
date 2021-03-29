package com.nano.candy.parser;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.std.Names;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import com.nano.common.text.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.nano.candy.parser.TokenKind.*;

class CandyParser implements Parser {
	
	private static final String INITIALIZER_NAME = Names.METHOD_INITALIZER;
	private static final int LOOKAHEAD_K = 2;

	protected final Logger logger = Logger.getLogger();

	protected Scanner scanner;
	private Token[] lookahead;
	private int lp;
	private Token previous;
	private Token peek;
	
	/**
	 * In some cases, I hope the SEMI at the end of a statement can be ignored.
	 * E.g: {@code foreach(lambda e -> println(e))}
	 *
	 * I want this code to be valid instead of reporting the following error:
	 * <code> Missing ';' after 'println(e)' </code>
	 */
	private boolean singleLineLambda;

	public CandyParser(Scanner scanner) {
		this.scanner = scanner;
		this.peek = scanner.peek();
		// avoid null pointer.
		this.previous = peek;
		this.lookahead = new Token[LOOKAHEAD_K];
		this.lookahead[0] = peek;
		for (int i = 1; i < LOOKAHEAD_K; i ++) {
			lookahead[i] = scanner.nextToken();
		}
		this.lp = 0;
	}


	/* =================== helper =================== */
	
	private String tokStr(TokenKind tok) {
		if (StringUtils.isEmpty(tok.getLiteral())) {
			return tok.name();
		}
		return tok.getLiteral();
	}

	protected <R extends ASTreeNode> R locate(Token token, R node) {
		return this.locate(token.getPos(), node);
	}
	
	protected <R extends ASTreeNode> R locate(Position pos, R node) {
		node.pos = pos;
		return node;
	}
	
	protected TokenKind peekKind() {
		return peek.getKind();
	}
	
	protected Token peek() {
		return peek;
	}
	
	protected Token peek(int k) {
		return lookahead[(lp + k) % LOOKAHEAD_K];
	}

	protected Token previous() {
		return previous;
	}
	
	protected void consume() {
		nextToken();
	}

	protected Token nextToken() {
		previous = peek();
		lookahead[lp] = scanner.nextToken();
		lp = (lp + 1) % LOOKAHEAD_K;
		peek = lookahead[lp];
		return peek;
	}

	protected boolean matchIf(TokenKind expected) {
		return matchIf(expected, false);
	}

	protected boolean matchIf(TokenKind expected, boolean error) {
		if (peek().getKind() == expected) {
			consume();
			return true;
		}
		if (error) {
			error(suitableErrorPosition(), "Missing '%s'", tokStr(expected));
		}
		return false;
	}
	
	protected Token matchIf(TokenKind tok, String errmsg, Object... args) {
		Token actual = peek();
		if (peek().getKind() == tok) {
			consume();
			return actual;
		}
		error(actual, errmsg, args);
		return actual;
	}
	
	protected Token match(TokenKind tok) {
		return match(tok, "Expetced '%s', but was '%s'.", 
			  tokStr(tok), tokStr(peek().getKind())
		);
	}
	
	protected Token match(TokenKind tok, String errmsg, Object... args) {
		Token actual = peek();
		if (peek().getKind() == tok) {
			consume();
			return actual;
		}
		error(actual, errmsg, args);
		panic();
		return actual;
	}
	
	protected void matchSEMI() {
		if (singleLineLambda) {
			return;
		}
		
		// e.g: function = lambda -> { return value }
		matchIf(SEMI, previous().getKind() != RBRACE);
	}
	
	private Position suitableErrorPosition() {
		Position currentPosition = peek().getPos();
		Position previousPosition = previous().getPos();
		if (currentPosition.getLine() != previousPosition.getLine()) {
			return previousPosition;
		}
		return currentPosition;
	}

	/* =================== errors =================== */


	protected void panic() {
		throw new ParserError();
	}
	
	protected void synchronizeInMethodContext() {
		loop: while (true) {
			switch (peekKind()) {
				case RBRACE:
				case FUN:
				case IDENTIFIER:
				case EOF:
					break loop;
			}
			consume();
		}
	}

	protected void synchronize() {
		loop: while (true) {
			switch (peekKind()) {
				case EOF: 
				case LBRACE:
				case IF:
				case WHILE:
				case FOR:
				case FUN:
				case CONTINUE:
				case BREAK:
				case RETURN:
				case ASSERT:
				case VAR:
				case CLASS:
					break loop;
				case SEMI:
					consume();
					break loop;
			}
			consume();
		}
	}
	
	/**
	 * Ensures the current token kind is specified expected kind.
	 *
	 * @param expectedKind the token kind must be in statement first set.
	 */
	protected void ensureExpectedKind(TokenKind expectedKind) {
		if (peekKind() == expectedKind) {
			return;
		}
		error(peek(), "Unexpected '%s'.", tokStr(peekKind()));
		synchronize();
		if (peekKind() != expectedKind) {
			// Avoid cascaded errors.
			insertToken(expectedKind);
		}
	}
	
	/**
	 * Replace the current token with a new token kind.
	 */
	protected Token insertToken(TokenKind kind) {
		Token insertedToken = new Token(peek.getPos(), kind.getLiteral(), kind);
		this.peek = insertedToken;
		return insertedToken;
	}

	protected void error(Token tok, String message, Object... args) {
		error(tok.getPos(), message, args);
	}

	protected void error(Position pos, String message, Object... args) {
		logger.error(pos, String.format(message, args));
	}
	
	/* =================== parse =================== */


	/**
	 * Changes the last statement to the {@code Return} statement 
	 * if it's returnable.
	 */
	private Stmt.Block toFuncBlock(Stmt.Block block) {
		if (block.stmts.isEmpty()) {
			return block;
		}
		int lastIndex = block.stmts.size() - 1;
		Stmt lastStmt = block.stmts.get(lastIndex);
		if (lastStmt instanceof Stmt.ExprS) {
			Stmt.Return ret = new Stmt.Return(((Stmt.ExprS)lastStmt).expr);
			ret.pos = lastStmt.pos;
			block.stmts.set(lastIndex, ret);
		} else if (lastStmt instanceof Stmt.Block) {
			toFuncBlock((Stmt.Block)lastStmt);
		}
		return block;
	}
	
	private boolean isFirstSetOfExpr(TokenKind tk) {
		switch (tk) {
			case IDENTIFIER: case NULL: 
			case DOUBLE:     case INTEGER:
			case STRING:     case LPAREN:
			case TRUE:       case FALSE: 
			case THIS:       case SUPER:
			case LBRACKET:
				return true;
		}
		return TokenKind.isUnaryOperator(tk);
	}

	/**
	 * Program = Stmts <EOF>
	 */ 
	@Override
	public Program parse() {
		Program program = new Program();
		program.setPosition(scanner.basePos());
		while (true) {
			parseStmts(program.block.stmts);
			if (peekKind() != EOF) {
				error(peek(), "Unexpected '%s'.", tokStr(peekKind()));
				synchronize();
				continue;
			}
			consume();
			break;
		}	
		return program;
	}
	
	/**
	 * Block = "{" Stmts "}"
	 */
	private Stmt.Block parseBlock() {
		Stmt.Block block;
		Token location = match(LBRACE);
		block = new Stmt.Block();
		parseStmts(block.stmts);
		matchIf(RBRACE, true);
		return locate(location, block);
	}
	
	/**
	 * Stmts = Stmt*
	 */
	private void parseStmts(List<Stmt> stmts) {
		while (true) {
			try {
				Stmt stmt = parseStmt();
				if (stmt == null) {
					break;
				}
				stmts.add(stmt);
			} catch (ParserError e) {
				synchronize();
			}
		}
	}
	
	/**
	 * Stmt = [ Block | ClassDef | Imports
	 *        | IfStmt | Lable | WhileStmt | ForStmt
	 *        | Break | Continue | Return
	 *        | BreakStmt | ContinueStmt| ReturnStmt
	 *        | VarDef | FunDef
	 *        | AssertStmt | ExprStmt
	 *        | ( <SEMI> Stmt )
	 *        ]
	 */
	private Stmt parseStmt() {
		loop: while (true) { 
			TokenKind kind = peek().getKind();
			switch (kind) {
				case IF:
					return parseIfStmt();
				case WHILE:
					return parseWhileStmt();
				case FOR:
					return parseForStmt();
				case IDENTIFIER:
					if (peek(1).getKind() == COLON) {
						return parseLable();
					}
					return parseExprStmt();
				case BREAK:
					return parseBreak();
				case CONTINUE:
					return parseContinue();
				case RETURN:
					return parseReturn();
				case ASSERT:
					return parseAssertStmt();
				case VAR:
					return parseVarDef();
				case FUN:
					return parseFunDef();
				case CLASS:
					return parseClassDef();
				case IMPORT:
					return parseImports();
				case LBRACE:
					return parseBlock();			
				case SEMI:
					consume();
					continue loop;
				default:
					if (isFirstSetOfExpr(kind)) {
						return parseExprStmt();
					}
					return null;
			}
		}
	}
	
	/**
	 * Body = Stmt
	 *
	 * Called by "IfStmt", "WhileStmt", "ForStmt", "LambdaExpr"
	 */
	private Stmt.Block parseBody(Position posIfErr, String msgIfErr) {	
		Stmt body = parseStmt();
		// Reports error if the body is empty.
		if (body == null) {
			error(posIfErr, msgIfErr);
			return null;
		}
		
		if (body instanceof Stmt.Block) {
			// Reduces AST level.
			Stmt.Block block = (Stmt.Block) body;
			return block;
		} else {
			Stmt.Block block =  new Stmt.Block();
			block.stmts.add(body);
			return locate(body.pos, block);
		}
	}

	/**
	 * ClassDef = "class" <IDENTIFIER> SuperClass "{" Methods "}"
	 */
	private Stmt.ClassDef parseClassDef() {
		Token location = match(CLASS);
		Token name = match(IDENTIFIER, "Expected class name.");
		Expr.VarRef superClass = parseSuperClass();
		matchIf(SEMI);
		matchIf(LBRACE, true);
		Stmt.ClassDef classDef = new Stmt.ClassDef(
			name.getLiteral(), superClass, new ArrayList<Stmt.FuncDef>()
		);
		parseMethods(classDef);
		matchIf(RBRACE, true);
		return locate(location, classDef);
	}
	
	/**
	 * Imports = "import" ( ( "(" ImportStmt* ")" <SEMI> ) | ImportStmt )
	 */
	private Stmt parseImports() {
		Token location = match(IMPORT);
		if (matchIf(LPAREN)) {
			List<Stmt.Import> imports = new ArrayList<>();
			if (matchIf(RPAREN)) {
				return locate(location, new Stmt.ImportList(imports));
			}
			do {
				imports.add(parseImportStmt(null));
			} while (!matchIf(RPAREN));
			matchSEMI();
			return locate(location, new Stmt.ImportList(imports));		
		}
		return parseImportStmt(location);
	}
	
	/**
	 * ImportStmt = Expr "as" <IDENTIFIER> <SEMI>
	 */
	private Stmt.Import parseImportStmt(Token begainning) {
		Expr fileExpr = parseExpr();
		match(AS);
		String identigier = match(IDENTIFIER).getLiteral();
		matchSEMI();
		Stmt.Import importstmt = new Stmt.Import(fileExpr, identigier);
		if (begainning != null) {
			return locate(begainning, importstmt);
		}
		return locate(fileExpr.pos, importstmt);
	}

	/**
	 * SuperClass = [ ":" <IDENTIFIER> ]
	 */
	private Expr.VarRef parseSuperClass() {
		if (matchIf(COLON)) {
			Token name = match(IDENTIFIER, "Expected super class name.");
			return locate(name, new Expr.VarRef(name.getLiteral()));
		}
		return null;
	}
	
	/**
	 * Methods = method*
	 */
	private void parseMethods(Stmt.ClassDef classDef) {
		loop: while (true) {
			switch (peekKind()) {
				case RBRACE: case EOF:
					return;
				case FUN: case IDENTIFIER: 
					break;
				default:
					synchronizeInMethodContext();
					continue loop;
			}
			Stmt.FuncDef method = parseMethod();
			if (!INITIALIZER_NAME.equals(method.name.get())) {
				classDef.methods.add(method);
				continue;
			}
			if (!classDef.initializer.isPresent()) {
				classDef.initializer = Optional.of(method);
				continue;
			}
			error(method.pos, "Duplicate initializer in the '%s' class.",
				classDef.name
			);
		}
	}

	/**
	 * Method = <IDENTIFIER> Params Block
	 */
	private Stmt.FuncDef parseMethod() {
		matchIf(FUN);
		Token name = peek();
		matchIf(IDENTIFIER, true);
		List<String> params = parseParams(true);
		ensureExpectedKind(TokenKind.LBRACE);
		Stmt.Block body = parseBlock();
		return locate(name, new Stmt.FuncDef(name.getLiteral(), params, body));
	}
	
	/**
	 * IfStmt = "if" "(" Expr ")" [ <SEMI> ] Body [ "else" Body ]
	 */
	private Stmt.If parseIfStmt() {
		Token location = match(IF);
		matchIf(LPAREN, true);
		Expr expr = parseExpr();
		if (matchIf(RPAREN, true)) {
			matchIf(SEMI);
		}
		Stmt thenBody = parseBody(location.getPos(), "Invalid if statement.");
		Stmt elseBody = null;
		if (matchIf(ELSE)) {
			elseBody = parseBody(suitableErrorPosition(), "Invalid if-else statement.");
		}
		return locate(location, new Stmt.If(expr, thenBody, elseBody));
	}
	
	/**
	 * Lable = <IDENTIGIER> ":" ( ForStmt | WhileStmt)
	 */
	private Stmt parseLable() {
		Token name = match(IDENTIFIER);
		match(COLON);
		Stmt.Loop loop = null;
		TokenKind kind = peekKind();
		if (kind == WHILE) {
			loop = parseWhileStmt();
		} else if (kind == FOR) {
			loop = parseForStmt();
		} else {
			error(previous(), "Expected loop statement after lable.");
			return new Stmt.ErrorStmt();
		}
		loop.setLableName(name.getLiteral(), name.getPos());
		return loop;
	}
	
	/**
	 * WhileStmt = "while" "(" Expr ")" [ <SEMI> ] Body
	 */
	private Stmt.While parseWhileStmt() {
		Token location = match(WHILE);
		matchIf(LPAREN, true);
		Expr expr = parseExpr();
		if (matchIf(RPAREN, true)) {
			matchIf(SEMI);
		}
		Stmt body = parseBody(location.getPos(), "Invalid while statemenet");
		return locate(location, new Stmt.While(expr, body));
	}
	
	/**
	 * ForStmt = "for" "(" <IDENTIFIER> "in" Expr ")" [SEMI] Body
	 */
	private Stmt.For parseForStmt() {
		Token location = match(FOR);
		matchIf(LPAREN, true);
		String iteratingVar = match(IDENTIFIER).getLiteral();
		match(IN);
		Expr iterable = parseExpr();
		if (matchIf(RPAREN, true)) {
			matchIf(SEMI);
		}
		Stmt.Block body = parseBody(location.getPos(), "Invalid for statement.");
		return locate(location, new Stmt.For(iteratingVar, iterable, body));
	}

	/**
	 * Break = "break" [ <IDENTIFIER> ] <SEMI>
	 */
	private Stmt.Break parseBreak() {
		Token location = match(BREAK);
		Stmt.Break breakStmt = new Stmt.Break();
		if (peekKind() == IDENTIFIER) {
			breakStmt.lableName = Optional.of(peek().getLiteral());
			consume();
		}
		matchSEMI();
		return locate(location, breakStmt);
	}
	
	/**
	 * Continue = "continue" [ <IDENTIFIER> ] <SEMI>
	 */
	private Stmt.Continue parseContinue() {
		Token location = match(CONTINUE);
		Stmt.Continue continueStmt = new Stmt.Continue();
		if (peekKind() == IDENTIFIER) {
			continueStmt.lableName = Optional.of(peek().getLiteral());
			consume();
		}
		matchSEMI();
		return locate(location, continueStmt);
	}

	/**
	 * Return = "return" [ ExprOrLambda ] <SEMI>
	 */
	private Stmt.Return parseReturn() {
		Token location = match(RETURN);
		Expr expr = null;
		if (peekKind() != SEMI) {
			expr = parseExprOrLambda();
		}
		matchSEMI();
		return locate(location, new Stmt.Return(expr));
	}
	
	/**
	 * VarDef = "var" <IDENTIFIER> [ "=" ExprOrLambda ] <SEMI>
	 */
	private Stmt.VarDef parseVarDef() {
		Token position = match(VAR);
		Token identifier = match(IDENTIFIER, "Expected variable name.");
		Expr initializer = null;
		if (matchIf(ASSIGN)) {
			initializer = parseExprOrLambda();
		}
		matchSEMI();
		return locate(
			position, 
			new Stmt.VarDef(identifier.getLiteral(), initializer)
		);
	}

	/**
	 * FunDef = "fun" <IDENTIFIER> Params Block
	 */
	private Stmt.FuncDef parseFunDef() {
		Token location = match(FUN);
		String name = match(IDENTIFIER, "Expected function name.").getLiteral();
		List<String> params = parseParams(true);
		Stmt.Block body = toFuncBlock(parseBlock());
		return locate(location, new Stmt.FuncDef(name, params, body));
	}

	/**
	 * Params = ( ( "(" Parameters ")" ) | Parameters ) [ <SEMI> ]
	 * Parameters = [ <IDENTIFIER> ( "." <IDENTIFIER> )*
	 */
	private List<String> parseParams(boolean forcibleParenthesis) {
		ArrayList<String> params = new ArrayList<>(6);
		boolean leftParenMatched = matchIf(LPAREN, forcibleParenthesis);
		if (leftParenMatched && matchIf(RPAREN)) {
			matchIf(SEMI);
			return params;
		}
		do {
			Token nameTok = peek();
			if (!matchIf(IDENTIFIER)) {
				break;
			}
			params.add(nameTok.getLiteral());
		} while (matchIf(COMMA));
		
		if (forcibleParenthesis || leftParenMatched) {
			matchIf(RPAREN, "Expected parameter declaration.");
		}
		// syntax: fun a()\n{
		matchIf(SEMI);
		return params;
	}
	
	/**
	 * AssertStmt = "assert" Expr [ ":" Expr ] <SEMI>
	 */
	private Stmt.Assert parseAssertStmt() {
		Token location = match(ASSERT);
		Expr expected = parseExpr();
		Expr errorInfo = null;
		if (matchIf(COLON)) {
			errorInfo = parseExpr();
		}
		matchSEMI();
		return new Stmt.Assert(location.getPos(), expected, errorInfo);
	}

	/**
	 * ExprStmt = Expr <SEMI>
	 */
	private Stmt.ExprS parseExprStmt() {
		Expr expr = parseExpr();
		matchSEMI();
		return new Stmt.ExprS(expr);
	}

	/**
	 * Expr =  BinaryExpr [ AssignOp ExprOrLambda ]
	 * AssignOp = "+=" | "-=" | "*=" | "/=" | "%=" | "="
	 */
	private Expr parseExpr() {
		Expr lhs = parseBinaryExpr(0);
		if (!TokenKind.isAssignOperator(peek().getKind())) {
			return lhs;
		}
		
		TokenKind assOperator = peek().getKind();
		consume();
		Expr rhs = parseExprOrLambda();
		
		if (lhs instanceof Expr.GetAttr) {
			return locate(
				lhs.pos, new Expr.SetAttr((Expr.GetAttr) lhs, assOperator, rhs)
			);
		}
		
		if (lhs instanceof Expr.VarRef) { 
			return locate(
				lhs.pos, new Expr.Assign(((Expr.VarRef) lhs).name, assOperator, rhs)
			);
		}
		
		if (lhs instanceof Expr.GetItem) {
			return locate(
				lhs.pos, new Expr.SetItem((Expr.GetItem) lhs, assOperator, rhs)
			);
		}
		
		error(lhs.pos, "Invalid left hand side.");
		return lhs;
	}

	/**
	 * BinaryExpr = UnaryExpr ( BinaryOp UnaryExpr )*
	 * BinaryOp = "+" | "-" | "*" | "/"...
	 */
	private Expr parseBinaryExpr(int prec) {
		Expr left = parseUnaryExpr();
		while (true) {
			Token operator = peek();
			int curPrec = TokenKind.precedence(operator.getKind());
			if (curPrec <= prec) break;
			consume();
			Expr right = parseBinaryExpr(curPrec);
			left = new Expr.Binary(left, operator, right);
		}
		return left;
	}

	/**
	 * UnaryExpr = [ UnaryOp ] ExprTerm
	 */
	private Expr parseUnaryExpr() {
		Token operator = peek();
		if (TokenKind.isUnaryOperator(operator.getKind())) {
			consume();
			return locate(
				operator, 
				new Expr.Unary(operator.getKind(), parseExprTerm())
			);
		}
		return parseExprTerm();
	}

	/**
	 * ExprTerm = ( <TRUE> | <FALSE>
	 *            | <NULL>
	 *            | <STRING>
	 *            | <INTEGER>
	 *            | <DOUBLE>
	 *            | <IDENTIFIER>
	 *            | ( "(" [ ExprOrLambda [ "," Tuple ] ] ")" )
	 *            | Array
	 *            | "this"
	 *            | ( "super" "." <IDENTIFIER> )
	 *            )
	 *            ExprSuffix
	 */
	private Expr parseExprTerm() {
		Token location = peek();
		TokenKind tokKind = location.getKind();
		Expr expr = null;
		switch (tokKind) {
			case TRUE: case FALSE:
				consume();
				expr = locate(
					location, new Expr.BooleanLiteral(tokKind == TRUE)
				);
				break;
				
			case NULL:
				consume();
				expr = locate(location, new Expr.NullLiteral());
				break;
				
			case STRING:
				consume();
				expr = locate(
					location, new Expr.StringLiteral(location.getLiteral())
				);
				break;
				
			case IDENTIFIER:
				consume();
				expr = locate(
					location, new Expr.VarRef(location.getLiteral())
				);
				break;
				
			case INTEGER:
				consume();
				expr = locate(
					location, new Expr.IntegerLiteral(location)
				);
				break;
			
			case DOUBLE:
				consume();
				expr = locate(
					location, new Expr.DoubleLiteral(location)
				);
				break;
			
			case LPAREN:
				consume();
				if (matchIf(RPAREN)) {
					expr = new Expr.Tuple(new ArrayList<Expr>());
					locate(location, expr);
					break;
				}
				expr = parseExprOrLambda();
				if (matchIf(COMMA)) {
					expr = parseTuple(expr);
					locate(location, expr);
				}
				matchIf(RPAREN, true);
				break;
			
			case LBRACKET:
				expr = parseArray();
				break;
				
			case THIS:
				consume();
				expr = locate(location, new Expr.This());
				break;
				
			case SUPER:
				consume();
				match(DOT);
				Token name = match(IDENTIFIER);
				expr = locate(
					location, new Expr.Super(name.getLiteral())
				);
				break;

			default: 
				error(suitableErrorPosition(), "Expected expression.");
				panic();
		}
		return parseExprSuffix(expr);
	}

	/**
	 * Tuple = [ ExprOrLambda ( "," ExprOrLambda )* [ "," ] [ SEMI ] ]
	 */
	private Expr parseTuple(Expr expr) {
		List<Expr> tuple = new ArrayList<>();
		tuple.add(expr);
		do {
			if (peekKind() == RPAREN) {
				break;
			}
			tuple.add(parseExprOrLambda());
		} while (matchIf(COMMA));
		matchIf(SEMI);
		return new Expr.Tuple(tuple);
	}
	
	/**
	 * Array = "[" [ ExprOrLambda ( "," ExprOrLambda )* ] [ SEMI ] "]"
	 */
	private Expr.Array parseArray() {
		ArrayList<Expr> elements = new ArrayList<>();
		Token location = match(LBRACKET);
		if (matchIf(RBRACKET)) {
			return locate(location, new Expr.Array(elements));
		}
		do {
			if (peekKind() == TokenKind.RBRACKET) {
				break;
			}
			elements.add(parseExprOrLambda());
		} while (matchIf(COMMA));
		matchIf(SEMI);
		matchIf(RBRACKET, true);
		return locate(location, new Expr.Array(elements));
	}

	/**
	 * ExprSuffix = ( Agruments | GetAttr | GetItem )*
	 * GetAttr = "." <IDENTIFIER>
	 * GetItem = "[" Expr "]"
	 */
	@SuppressWarnings("fallthrough")
	private Expr parseExprSuffix(Expr expr) {
		while (true) {
			Token tok = peek();
			switch (tok.getKind()) {
				case LPAREN:
					List<Expr> args = parseArguments();
					expr = locate(tok, new Expr.CallFunc(expr, args));
					continue;
					
				case LBRACKET:
					consume();
					Expr key = parseExpr();
					matchIf(RBRACKET, true);
					expr = locate(tok, new Expr.GetItem(expr, key));
					continue;
				
				case SEMI:
					// for supporting following chained syntax:
					//     stream()\n.filter()\n.map()\n.close()
					if (peek(1).getKind() != DOT) {
						break;
					} 
					consume(); /* down: parse dot. */
				case DOT:
					consume();
					Token attr = match(IDENTIFIER, "Expected attribute name.");
					expr = locate(tok, new Expr.GetAttr(expr, attr.getLiteral()));
					continue;
			}
			return expr;
		}
	}

	/**
	 * Agruments = "(" [ ExprOrLambda ( "," ExprOrLambda )* ] [ <SEMI> ] ")"
	 */
	private List<Expr> parseArguments() {
		consume();
		List<Expr> args = new ArrayList<>();
		if (matchIf(RPAREN)) {
			return args;
		}
		do {
			if (peekKind() == RPAREN) {
				break;
			}
			args.add(parseExprOrLambda());
		} while (matchIf(COMMA));
		matchIf(SEMI);
		matchIf(RPAREN, true);
		return args;
	}
	
	/**
	 * ExprOrLambda = LambdaExpr | Expr
	 */
	private Expr parseExprOrLambda() {
		if (peekKind() == TokenKind.LAMBDA) {
			return parseLambdaExpr();
		} else {
			return parseExpr();
		}
	}
	
	/**
	 * LambdaExpr = "lambda" Params "->" Body
	 */
	private Expr.Lambda parseLambdaExpr() {
		Token location = match(LAMBDA);
		List<String> params = parseParams(false);
		match(ARROW);
		
		boolean originalSingleLineLambda = singleLineLambda;
		singleLineLambda = peekKind() != TokenKind.LBRACE;
		
		Stmt.Block body = toFuncBlock(
			parseBody(location.getPos(), "Invalid lambda expression.")
		);	
		
		singleLineLambda = originalSingleLineLambda;
		Expr.Lambda lambda = new Expr.Lambda(params, body);
		lambda.pos = location.getPos();
		lambda.funcDef.pos = location.getPos();
		return lambda;
	}

}
