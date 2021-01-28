package com.nano.candy.parser;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Position;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.nano.candy.parser.TokenKind.*;

class CandyParser implements Parser {
	
	public static final String INITIALIZER_NAME = "init";

	protected final Logger logger = Logger.getLogger();

	protected Scanner scanner;

	private LinkedList<Token> lookaheadList = new LinkedList<>();
	private Token previous;
	private Token peek;
	
	/**
	 * In some cases I hope the SEMI token can be ignored.
	 * e.g: {@code foreach(lambda e -> println(e))}
	 * I hope the code can work instead reporting the error:
	 * {@code ...println(e) Missing ';'.}
	 */
	private boolean singleLineLambda;

	public CandyParser(Scanner scanner) {
		this.scanner = scanner;
		this.peek = scanner.peek();
		this.previous = peek;
		this.lookaheadList.add(peek);
	}


	/* =================== helper =================== */

	protected <R extends ASTreeNode> R location(Token token, R node) {
		return this.location(token.getPos(), node);
	}
	
	protected <R extends ASTreeNode> R location(Position pos, R node) {
		node.pos = pos;
		return node;
	}

	protected TokenKind LK(int k) {
		return LA(k).getKind();
	}

	protected Token LA(int k) {
		fill(k);
		return lookaheadList.get(k - 1);
	}

	private void fill(int k) {
		for (int i = lookaheadList.size(); i < k; i ++) {
			lookaheadList.add(scanner.nextToken());
		}
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
		if (!lookaheadList.isEmpty()) {
			lookaheadList.removeFirst();
		}
		previous = peek();
		peek = LA(1);
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
		Position pos = peek().getPos();
		if (pos.getLine() != previous().getPos().getLine()) {
			pos = previous().getPos();
		}
		return pos;
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
		
		// If the previous token is '}', for example:
		// iterator._next = lambda -> { return value }
		// I would like that the ';' is unforced.
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
				if (peek().getKind() != EOF) {
					error(peek(), "Unexpected '%s'.", peek.getKind());
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
	 * Body = StmtOrDeclr
	 *
	 * Called by "IfStmt", "WhileStmt", "ForStmt", "LambdaExpr"
	 */
	private Stmt.Block parseBody(Position posIfErr, String msgIfErr) {
		Stmt.Block block =  new Stmt.Block();
		
		Stmt body = parseStmt();
		// Reports error if the body is empty.
		if (body == null) {
			error(posIfErr, msgIfErr);
			return null;
		}
		
		// Maybe get block.
		if (body instanceof Stmt.Block) {
			// Reduces AST level.
			block = (Stmt.Block) body;
		} else {
			block.stmts.add(body);
		}
		
		if (block.pos != null) {
			return block;
		}
		return location(body.pos, block);
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
				if (peek().getKind() != IDENTIFIER) {
					return;
				}
				Stmt.FuncDef method = parseMethod();
				if (INITIALIZER_NAME.equals(method.name.get())) {
					if (!classDef.initializer.isPresent()) {
						classDef.initializer = Optional.of(method);
						continue;
					}
					error(method.pos, 
						"Duplicated initializer in the '%s' class.",
						classDef.name
					);
				} else classDef.methods.add(method);
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
	 * Stmt = [ IfStmt | WhileStmt | ForStmt
	 *        | ExprStmt | PrintStmt | AssertStmt
	 *        | BreakStmt | ContinueStmt| ReturnStmt
	 *        | VarDef | FunDef
	 *        | Block
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
	 * FunDef = "fun" <IDENTIFIER> Params Block
	 */
	private Stmt.FuncDef parseFunDef() {
		Token location = match(FUN);
		String name = match(IDENTIFIER).getLiteral();
		List<String> params = parseParams(true);
		Stmt.Block body = parseBlock();
		return location(location, new Stmt.FuncDef(name, params, body));
	}

	/**
	 * Params = ( "(" Parameters ")" ) | Parameters
	 * Parameters = [ <IDENTIFIER> ( "," <IDENTIFIER> )*
	 */
	private List<String> parseParams(boolean mandatoryParens) {
		ArrayList<String> params = new ArrayList<>();
		boolean isMatchLParen = matchIf(LPAREN, mandatoryParens);
		if (isMatchLParen && matchIf(RPAREN)) {
			return params;
		}
		do {
			Token nameTok = peek();
			if (!matchIf(IDENTIFIER)) {
				break;
			}
			params.add(nameTok.getLiteral());
		}while (matchIf(COMMA));
		
		if (mandatoryParens || isMatchLParen) matchIf(RPAREN, true);
		return params;
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
	 * While = "while" "(" Expr ")" [ <SEMI> ] Body
	 */
	private Stmt.While parseWhileStmt() {
		Token beginPos = match(WHILE);
		matchIf(LPAREN, true);
		Expr expr = parseExpr();
		if (matchIf(RPAREN, true)) {
			matchIf(SEMI);
		}
		Stmt body = parseBody(beginPos.getPos(), "Invalid while statmenet");
		return location(beginPos, new Stmt.While(expr, body));
	}
	
	/**
	 * ForStmt = "for" "(" <IDENTIFIER> "in" Expr ")" Body
	 */
	private Stmt.For parseForStmt() {
		Token beginPos = match(FOR);
		matchIf(LPAREN, true);
		String elementName = match(IDENTIFIER).getLiteral();
		match(IN);
		Expr iterable = parseExpr();
		matchIf(RPAREN, true);
		matchIf(SEMI);
		Stmt.Block body = parseBody(beginPos.getPos(), "Invalid for statement.");
		return location(beginPos, new Stmt.For(elementName, iterable, body));
	}
	
	/**
	 * IfStmt = "if" "(" Expr ")" [ <SEMI> ] Body [ "else" Body ]
	 */
	private Stmt.If parseIfStmt() {
		Token beginPos = match(IF);
		matchIf(LPAREN, true);
		Expr expr = parseExpr();
		if (matchIf(RPAREN, true)) {
			matchIf(SEMI);
		}
		Stmt thenBody = parseBody(beginPos.getPos(), "Invalid if statement.");
		Stmt elseBody = null;
		if (matchIf(ELSE)) {
			elseBody = parseBody(suitableErrorPosition(), "Invalid if statement.");
		}
		return location(beginPos, new Stmt.If(expr, thenBody, elseBody));
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
	 * Break = "break" <SEMI>
	 */
	private Stmt.Break parseBreak() {
		Token location = match(BREAK);
		matchSEMI();
		return location(location, new Stmt.Break());
	}
	
	/**
	 * Return = "return" [ Expr ] <SEMI>
	 */
	private Stmt.Return parseReturn() {
		Token location = match(RETURN);
		Expr expr = null;
		if (peek().getKind() != SEMI) {
			expr = parseExpr();
		}
		matchSEMI();
		return location(location, new Stmt.Return(expr));
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
		return location(location, new Stmt.Assert(expected, errorInfo));
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
				lhs.pos, new Expr.Assign(((Expr.VarRef)lhs).name, assOperator, rhs)
			);
		}
		
		if (lhs instanceof Expr.GetItem) {
			return location(
				lhs.pos, new Expr.SetItem((Expr.GetItem)lhs, assOperator, rhs)
			);
		}
		
		error(lhs.pos, "Invalid left hand side.");
		return lhs;
	}

	/**
	 * BinaryExpr = UnaryExpr ( BinaryOp UnaryExpr )*
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
	 *            | <STRING_LITERAL>
	 *            | <INTEGER>
	 *            | <DOUBLE>
	 *            | <IDENTIFIER>
	 *            | ( "(" Expr ")" )
	 *            | <STRING_LITERAL>
	 *            | Array
	 *            | "this"
	 *            | "super" )
	 *            ExprSuffix
	 */
	private Expr parseExprTerm() {
		Token beginTok = peek();
		Expr expr = null;
		switch (beginTok.getKind()) {
			case TRUE: case FALSE:
				consume();
				expr = location(
					beginTok, 
					new Expr.BooleanLiteral(beginTok.getKind() == TRUE)
				);
				break;
				
			case NULL:
				consume();
				expr = location(beginTok, new Expr.NullLiteral());
				break;
				
			case STRING:
				consume();
				expr = location(
					beginTok, 
					new Expr.StringLiteral(beginTok.getLiteral())
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
					beginTok, new Expr.IntegerLiteral(beginTok.getLiteral())
				);
				break;
			
			case DOUBLE:
				consume();
				expr = location(
					beginTok, new Expr.DoubleLiteral(beginTok.getLiteral())
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
				if (beginTok.getPos().getLine() == previous().getPos().getLine()) {
					error(
						beginTok, 
						"Expected expr factor, but found '%s'.",
						beginTok.getKind()
					);
				} else {
					error(previous(), "Expected expr factor.");
				}
				panic();
		}
		return parseExprSuffix(expr);
	}
	
	/**
	 * Array = "[" Expr* "]"
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
	 * ExprSuffix = ( Agruments | GetAttr | GetItem)*
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
		if (peek().getKind() == TokenKind.LAMBDA) {
			return parseLambdaExpr();
		} else {
			return parseExpr();
		}
	}
	
	/**
	 * LambdaExpe = <LAMBDA> Params <ARROW> Body
	 */
	private Expr.Lambda parseLambdaExpr() {
		Token location = match(LAMBDA);
		List<String> params = parseParams(false);
		match(ARROW);
		
		boolean singleLineLambdaOrigin = singleLineLambda;
		// if the body is a single line statement like:
		// lambda e -> println(e)
		singleLineLambda = peek().getKind() != TokenKind.LBRACE;
		
		Stmt.Block body = parseBody(location.getPos(), "Invalid lambda expression.");
		
		singleLineLambda = singleLineLambdaOrigin;
		return location(location, new Expr.Lambda(params, body));
	}

}
