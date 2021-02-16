package com.nano.candy.parser;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.nano.candy.parser.TokenKind.*;

class CandyParser implements Parser {
	
	public static final String INITIALIZER_NAME = "init";

	protected final Logger logger = Logger.getLogger();

	protected Scanner scanner;

	private Token previous;
	private Token peek;
	
	/**
	 * In some cases I hope the SEMI token can be ignored.
	 * <p>e.g: {@code foreach(lambda e -> println(e))}</p>
	 * In which the {@code println(e)} will be parsed as an expression statement.
	 * I want this code can work instead of reporting the following error:
	 * <p>{@code println(e) Missing ';'.}</p>
	 */
	private boolean singleLineLambda;

	public CandyParser(Scanner scanner) {
		this.scanner = scanner;
		this.peek = scanner.peek();
		this.previous = peek;
	}


	/* =================== helper =================== */

	protected <R extends ASTreeNode> R location(Token token, R node) {
		return this.location(token.getPos(), node);
	}
	
	protected <R extends ASTreeNode> R location(Position pos, R node) {
		node.pos = pos;
		return node;
	}
	
	protected TokenKind peekKind() {
		return peek.getKind();
	}
	
	protected Token peek() {
		return peek;
	}

	protected Token previous() {
		return previous;
	}

	protected void consume() {
		nextToken();
	}

	protected Token nextToken() {
		previous = peek();
		peek = scanner.nextToken();
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
			error(suitableErrorPosition(), "Missing '%s'", expected.literal);
		}
		return false;
	}

	private Position suitableErrorPosition() {
		Position currentPosition = peek().getPos();
		Position previousPosition = previous().getPos();
		if (currentPosition.getLine() != previousPosition.getLine()) {
			return previousPosition;
		}
		return currentPosition;
	}

	protected Token match(TokenKind tok) {
		Token actual = peek();
		if (peek().getKind() == tok) {
			consume();
			return actual;
		}
		onError(tok, peek());
		return actual;
	}
	
	protected void matchSEMI() {
		if (singleLineLambda) {
			return;
		}
		
		// I want that the ';' is unforced if the previous token is '}'.
		// e.g: function = lambda -> { return value }
		matchIf(SEMI, previous().getKind() != RBRACE);
	}

	/* =================== errors =================== */


	protected void panic() {
		throw new ParserError();
	}

	protected void synchronize() {
		loop: while (true) {
			switch (peek().getKind()) {
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

	protected void error(Token tok, String message, Object... args) {
		error(tok.getPos(), message, args);
	}

	protected void error(Position pos, String message, Object... args) {
		logger.error(pos, String.format(message, args));
	}

	/**
	 * Called by {@link #match(TokenKind)}
	 */
	protected void onError(TokenKind expected, Token actual) {
		error(actual, "Expetced '%s', but '%s'.", expected, actual.getKind());
		panic();
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
			try {
				if (peekKind() != EOF) {
					error(peek(), "Unexpected '%s'.", peekKind());
					synchronize();
					continue;
				}
				consume();
				break;
			} catch (ParserError e) {
				synchronize();
			}
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
		return location(location, block);
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
			return location(body.pos, block);
		}
	}

	/**
	 * ClassDef = "class" <IDENTIFIER> SuperClass "{" Methods "}"
	 */
	private Stmt.ClassDef parseClassDef() {
		Token beginTok = match(CLASS);
		Token name = match(IDENTIFIER);
		Expr.VarRef superClass = parseSuperClass();
		matchIf(LBRACE, true);
		Stmt.ClassDef classDef = new Stmt.ClassDef(
			name.getLiteral(), superClass, new ArrayList<Stmt.FuncDef>()
		);
		parseMethods(classDef);
		matchIf(RBRACE, true);
		return location(beginTok, classDef);
	}

	/**
	 * SuperClass = [ ":" <IDENTIFIER> ]
	 */
	private Expr.VarRef parseSuperClass() {
		if (matchIf(COLON)) {
			Token name = match(IDENTIFIER);
			return location(name, new Expr.VarRef(name.getLiteral()));
		}
		return null;
	}

	/**
	 * Methods = method*
	 */
	private void parseMethods(Stmt.ClassDef classDef) {
		while (true) {
			try {
				if (peekKind() != IDENTIFIER) {
					return;
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
			} catch (ParserError e) {
				synchronize();
			}
		}
	}

	/**
	 * Method = <IDENTIFIER> Params Block
	 */
	private Stmt.FuncDef parseMethod() {
		Token name = match(IDENTIFIER);
		List<String> params = parseParams(true);
		Stmt.Block body = parseBlock();
		return location(name, new Stmt.FuncDef(name.getLiteral(), params, body));
	}

	/**
	 * Stmt = [ Block | ClassDef    // Above
	 *        | IfStmt | WhileStmt | ForStmt
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
	 * IfStmt = "if" "(" Expr ")" [ <SEMI> ] Body [ "else" Body ]
	 */
	private Stmt.If parseIfStmt() {
		Token beginTok = match(IF);
		matchIf(LPAREN, true);
		Expr expr = parseExpr();
		if (matchIf(RPAREN, true)) {
			matchIf(SEMI);
		}
		Stmt thenBody = parseBody(beginTok.getPos(), "Invalid if statement.");
		Stmt elseBody = null;
		if (matchIf(ELSE)) {
			elseBody = parseBody(suitableErrorPosition(), "Invalid if-else statement.");
		}
		return location(beginTok, new Stmt.If(expr, thenBody, elseBody));
	}
	
	/**
	 * WhileStmt = "while" "(" Expr ")" [ <SEMI> ] Body
	 */
	private Stmt.While parseWhileStmt() {
		Token beginTok = match(WHILE);
		matchIf(LPAREN, true);
		Expr expr = parseExpr();
		if (matchIf(RPAREN, true)) {
			matchIf(SEMI);
		}
		Stmt body = parseBody(beginTok.getPos(), "Invalid while statemenet");
		return location(beginTok, new Stmt.While(expr, body));
	}
	
	/**
	 * ForStmt = "for" "(" <IDENTIFIER> "in" Expr ")" Body
	 */
	private Stmt.For parseForStmt() {
		Token beginTok = match(FOR);
		matchIf(LPAREN, true);
		String iteratingVar = match(IDENTIFIER).getLiteral();
		match(IN);
		Expr iterable = parseExpr();
		matchIf(RPAREN, true);
		matchIf(SEMI);
		Stmt.Block body = parseBody(beginTok.getPos(), "Invalid for statement.");
		return location(beginTok, new Stmt.For(iteratingVar, iterable, body));
	}

	/**
	 * Break = "break" <SEMI>
	 */
	private Stmt.Break parseBreak() {
		Token location = match(BREAK);
		matchSEMI();
		return location(location, new Stmt.Break());
	}
	
	/**
	 * Continue = "continue" <SEMI>
	 */
	private Stmt.Continue parseContinue() {
		Token location = match(CONTINUE);
		matchSEMI();
		return location(location, new Stmt.Continue());
	}

	/**
	 * Return = "return" [ Expr ] <SEMI>
	 */
	private Stmt.Return parseReturn() {
		Token location = match(RETURN);
		Expr expr = null;
		if (peekKind() != SEMI) {
			expr = parseExpr();
		}
		matchSEMI();
		return location(location, new Stmt.Return(expr));
	}
	
	/**
	 * VarDef = "var" <IDENTIFIER> [ "=" ExprOrLambda ] <SEMI>
	 */
	private Stmt.VarDef parseVarDef() {
		Token position = match(VAR);
		Token identifier = match(IDENTIFIER);
		Expr initializer = null;
		if (matchIf(ASSIGN)) {
			initializer = parseExprOrLambda();
		}
		matchSEMI();
		return location(
			position, 
			new Stmt.VarDef(identifier.getLiteral(), initializer)
		);
	}

	/**
	 * FunDef = "fun" <IDENTIFIER> Params Block
	 */
	private Stmt.FuncDef parseFunDef() {
		Token beginTok = match(FUN);
		String name = match(IDENTIFIER).getLiteral();
		List<String> params = parseParams(true);
		Stmt.Block body = toFuncBlock(parseBlock());
		return location(beginTok, new Stmt.FuncDef(name, params, body));
	}

	/**
	 * Params = ( "(" Parameters ")" ) | Parameters
	 * Parameters = [ <IDENTIFIER> ( "," <IDENTIFIER> )*
	 */
	private List<String> parseParams(boolean mandatoryParenthesis) {
		ArrayList<String> params = new ArrayList<>(6);
		boolean leftParenMatched = matchIf(LPAREN, mandatoryParenthesis);
		if (leftParenMatched && matchIf(RPAREN)) {
			return params;
		}
		do {
			Token nameTok = peek();
			if (!matchIf(IDENTIFIER)) {
				break;
			}
			params.add(nameTok.getLiteral());
		}while (matchIf(COMMA));
		
		if (mandatoryParenthesis || leftParenMatched) {
			matchIf(RPAREN, true);
		}
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
			return location(
				lhs.pos, new Expr.SetAttr((Expr.GetAttr) lhs, assOperator, rhs)
			);
		}
		
		if (lhs instanceof Expr.VarRef) { 
			return location(
				lhs.pos, new Expr.Assign(((Expr.VarRef) lhs).name, assOperator, rhs)
			);
		}
		
		if (lhs instanceof Expr.GetItem) {
			return location(
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
			return location(
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
	 *            | ( "(" Expr ")" )
	 *            | Array
	 *            | "this"
	 *            | ( "super" "." <IDENTIFIER> )
	 *            )
	 *            ExprSuffix
	 */
	private Expr parseExprTerm() {
		Token beginTok = peek();
		TokenKind tokKind = beginTok.getKind();
		Expr expr = null;
		switch (tokKind) {
			case TRUE: case FALSE:
				consume();
				expr = location(
					beginTok, new Expr.BooleanLiteral(tokKind == TRUE)
				);
				break;
				
			case NULL:
				consume();
				expr = location(beginTok, new Expr.NullLiteral());
				break;
				
			case STRING:
				consume();
				expr = location(
					beginTok, new Expr.StringLiteral(beginTok.getLiteral())
				);
				break;
				
			case IDENTIFIER:
				consume();
				expr = location(
					beginTok, new Expr.VarRef(beginTok.getLiteral())
				);
				break;
				
			case INTEGER:
				consume();
				expr = location(
					beginTok, new Expr.IntegerLiteral(beginTok)
				);
				break;
			
			case DOUBLE:
				consume();
				expr = location(
					beginTok, new Expr.DoubleLiteral(beginTok)
				);
				break;
			
			case LPAREN:
				consume();
				expr = parseExpr();
				matchIf(RPAREN, true);
				break;
			
			case LBRACKET:
				expr = parseArray();
				break;
				
			case THIS:
				consume();
				expr = location(beginTok, new Expr.This());
				break;
				
			case SUPER:
				consume();
				match(DOT);
				Token name = match(IDENTIFIER);
				expr = location(
					beginTok, new Expr.Super(name.getLiteral())
				);
				break;

			default: 
				error(suitableErrorPosition(), "Expected expression.");
				panic();
		}
		return parseExprSuffix(expr);
	}
	
	/**
	 * Array = "[" [ ExprOrLambda ( "," ExprOrLambda )* ]"]"
	 */
	private Expr.Array parseArray() {
		ArrayList<Expr> elements = new ArrayList<>();
		Token beginTok = match(LBRACKET);
		if (matchIf(RBRACKET)) {
			return location(beginTok, new Expr.Array(elements));
		}
		do {
			elements.add(parseExprOrLambda());
		} while (matchIf(COMMA));
		matchIf(RBRACKET, true);
		return location(beginTok, new Expr.Array(elements));
	}

	/**
	 * ExprSuffix = ( Agruments | GetAttr | GetItem )*
	 * GetAttr = "." <IDENTIFIER>
	 * GetItem = "[" Expr "]"
	 */
	private Expr parseExprSuffix(Expr expr) {
		while (true) {
			Token tok = peek();
			switch (tok.getKind()) {
				case LPAREN:
					List<Expr> args = parseArguments();
					expr = location(tok, new Expr.CallFunc(expr, args));
					continue;
					
				case LBRACKET:
					consume();
					Expr key = parseExpr();
					matchIf(RBRACKET, true);
					expr = location(tok, new Expr.GetItem(expr, key));
					continue;
				
				case DOT:
					consume();
					Token attr = match(IDENTIFIER);
					expr = location(tok, new Expr.GetAttr(expr, attr.getLiteral()));
					continue;
			}
			return expr;
		}
	}

	/**
	 * Agruments = "(" [ ExprOrLambda ( "," ExprOrLambda )* ] ")"
	 */
	private List<Expr> parseArguments() {
		consume();
		List<Expr> args = new ArrayList<>();
		if (matchIf(RPAREN)) {
			return args;
		}
		do {
			args.add(parseExprOrLambda());
		} while (matchIf(COMMA));
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
