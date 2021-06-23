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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.nano.candy.parser.TokenKind.*;

class CandyParser implements Parser {
	
	static final Logger logger = Logger.getLogger();
	
	private static final String INITIALIZER_NAME = Names.METHOD_INITALIZER;
	
	/**
	 * Empty Syntax Tree Node.
	 */
	private static final Stmt EMPTY_STMT = new Stmt.Empty();
	
	/**
	 * The maximum number of parameters that a function can declare.
	 */
	private static final int MAX_PARAMETER_NUMBER = 255;
	
	// LL(2)
	private static final int LOOKAHEAD_K = 2;
	
	private static void reportError(Token tok, String message, Object... args) {
		reportError(tok.getPos(), message, args);
	}

	private static void reportError(Position pos, String message, Object... args) {
		logger.error(pos, String.format(message, args));
	}
	
	private static void reportError(ASTreeNode node, String msg, Object... args) {
		logger.error(node.pos, msg, args);
	}

	private static void reportWarn(ASTreeNode node, String msg, Object... args) {
		logger.warn(node.pos, msg, args);
	}
	
	private enum FunctionType {
		NONE,
		FUNCTION,
		METHOD,
		LAMBDA,
		INIT
	}
	
	
	protected Scanner scanner;
	private Token[] lookahead;
	private int lp;
	private Token previous;
	private Token peek;
	
	/**
	 * If parser is parsing a single-line lambda expression(only have a 
	 * statement), the SEMI('\n' or ';') at the end of the statement is needless.
	 */
	private boolean inSingleLineLambda;
	
	/**
	 * True when parse loop statements such as FOR, WHILE.
	 */
	private boolean inLoop;

	/**
	 * True when parse class statements.
	 */
	private boolean inClass;
	
	private FunctionType curFunctionType = FunctionType.NONE;

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
	
	private static String tokStr(TokenKind tok) {
		if (StringUtils.isEmpty(tok.getLiteral())) {
			return tok.name();
		}
		return tok.getLiteral();
	}

	private static <R extends ASTreeNode> R locate(Token token, R node) {
		return locate(token.getPos(), node);
	}
	
	private static <R extends ASTreeNode> R locate(Position pos, R node) {
		node.pos = pos;
		return node;
	}
	
	private TokenKind peekKind() {
		return peek.getKind();
	}
	
	private Token peek() {
		return peek;
	}
	
	private Token peek(int k) {
		return lookahead[(lp + k) % LOOKAHEAD_K];
	}

	private Token previous() {
		return previous;
	}
	
	private void consume() {
		nextToken();
	}

	private Token nextToken() {
		previous = peek();
		lookahead[lp] = scanner.nextToken();
		lp = (lp + 1) % LOOKAHEAD_K;
		peek = lookahead[lp];
		return peek;
	}

	private boolean matchIf(TokenKind expected) {
		return matchIf(expected, false);
	}

	private boolean matchIf(TokenKind expected, boolean error) {
		if (peek().getKind() == expected) {
			consume();
			return true;
		}
		if (error) {
			reportError(suitableErrorPosition(), "Missing '%s'", tokStr(expected));
		}
		return false;
	}
	
	private Token matchIf(TokenKind tok, String errmsg, Object... args) {
		Token actual = peek();
		if (peek().getKind() == tok) {
			consume();
			return actual;
		}
		reportError(actual, errmsg, args);
		return actual;
	}
	
	private Token match(TokenKind tok) {
		return match(tok, "Expetced '%s', but was '%s'.", 
			  tokStr(tok), tokStr(peek().getKind())
		);
	}
	
	private Token match(TokenKind tok, String errmsg, Object... args) {
		Token actual = peek();
		if (peek().getKind() == tok) {
			consume();
			return actual;
		}
		reportError(actual, errmsg, args);
		panic();
		return actual;
	}
	
	private void matchSEMI() {
		if (inSingleLineLambda) {
			return;
		}
		
		// e.g: var a = lambda -> { return value } // missing ';'
		// Scanner can't insert a SEMI token to the end of this expression
		// if the previous kind is '}'.
		matchIf(SEMI, previous().getKind() != RBRACE);
	}
	
	/**
	 * Linux style (break before braces) or Chained method invocations...
	 *
	 * Scanner will insert a SEMI in some cases, e.g:
	 * 1. func a(a, b)\n /the insertion of a SEMI/ {}
	 * 2. if (expr)\n /the insertion of a SEMI/ {}
	 * 3. stream(arr)\n /insertion/ .foreach(...)\n /insertion/ .count()
	 * 4. [\n expr1,\n expr2,\n expr3\n /insertion/ ]
	 * ...
	 */
	private void ignorableLinebreak() {
		matchIf(SEMI, false);
	}
	
	/**
	 * In some cases, the syntax error may be located on the next token line;
	 */
	private Position suitableErrorPosition() {
		Position currentPosition = peek().getPos();
		Position previousPosition = previous().getPos();
		if (currentPosition.getLine() != previousPosition.getLine()) {
			return previousPosition;
		}
		return currentPosition;
	}
	
	/* ======================== checks ======================== */
	
	public Stmt checkClass(Stmt.ClassDef node) {
		if (node.initializer.isPresent()) {
			Stmt.FuncDef initialzier = node.initializer.get();
			insertSuperInitCall(initialzier.pos, initialzier.body.stmts);
		}
		return node;
	}
	
	/**
	 * The initalizer of the super class must be invoked by the current
	 * class if this class defines a initalizer.
	 *
	 * If user do not invoke the initalizer of the super class(first statement),
	 * it will be inserted.
	 */
	private void insertSuperInitCall(Position pos, List<Stmt> stmts) {
		if (stmts.isEmpty() || !isSuperCallInit(stmts.get(0))) {
			stmts.add(0, newSuperInitCall(pos));
		}
	}

	private Stmt newSuperInitCall(Position pos) {
		Expr.Super superExpr = locate(pos, new Expr.Super(INITIALIZER_NAME));
		Expr.CallFunc callSuperExpr = new Expr.CallFunc
		(superExpr, Collections.<Expr.Argument>emptyList());
		callSuperExpr.pos = pos;
		return locate(pos, new Stmt.ExprS(callSuperExpr));
	}

	private boolean isSuperCallInit(Stmt stmt) {
		if (stmt instanceof Stmt.ExprS) {
			Expr expr = ((Stmt.ExprS) stmt).expr;
			if (!(expr instanceof Expr.CallFunc)) {
				return false;
			}
			Expr.CallFunc callFunc = (Expr.CallFunc) expr;
			if (!(callFunc.expr instanceof Expr.Super)) {
				return false;
			}
			return ((Expr.Super) callFunc.expr)
				.reference.equals(INITIALIZER_NAME);
		}
		if (stmt instanceof Stmt.StmtList) {
			Stmt.StmtList stmts = (Stmt.StmtList)stmt;
			return !stmts.isEmpty() && isSuperCallInit(stmts.getFirstStmt());
		}
		return false;
	}
	
	
	public Stmt.FuncDef checkFunc(Stmt.FuncDef node) {
		checkParams(node);
		insertReturnStmt(node);
		return node;
	}
	
	private void checkParams(Stmt.FuncDef node) {
		String funcName = node.name.isPresent() ? node.name.get() : "lambda";
		HashSet<String> duplicatedNameHelper = 
			new HashSet<>(node.parameters.size());
		for (String param : node.parameters.params) {
			if (duplicatedNameHelper.contains(param)) {
				reportError(
					node, "Duplicated parameter name '%s' in the function '%s'.", 
					param, funcName
				);
			} else {
				duplicatedNameHelper.add(param);
			}
		}
		if (node.parameters.size() > MAX_PARAMETER_NUMBER) {
			reportError(
				node, "Can't have parameters more than %d in the function '%s'.",
				MAX_PARAMETER_NUMBER, funcName
			);
		}
	}
	
	/**
	 * Every functions must have a return statement at the end.
	 */
	private void insertReturnStmt(Stmt.FuncDef node) {
		if (!isReturnStmt(node.body.getLastStmt())) {
			Stmt.Return returnStmt = new Stmt.Return(null);
			returnStmt.pos = Position.PREVIOUS_POSITION;
			node.body.stmts.add(returnStmt);
		}
	}

	private boolean isReturnStmt(Stmt stmt) {
		if (stmt instanceof Stmt.Return) {
			return true;
		}
		if (stmt instanceof Stmt.StmtList) {
			Stmt.StmtList stmts = (Stmt.StmtList) stmt;
			return !stmts.isEmpty() && isReturnStmt(stmts.getLastStmt());
		}
		return false;
	}
	
	/* ======================== errors ======================== */

	private void panic() {
		throw new ParserError();
	}
	
	private void unexpected(Token tok) {
		reportError(tok, "Unexpected %s", tokStr(tok.getKind()));
	}
	
	private void synchronizeInMethodContext() {
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

	private void synchronize() {
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
				case IMPORT:
				case RAISE:
				case TRY:
					break loop;
				case SEMI:
					consume();
					break loop;
			}
			consume();
		}
	}
	
	/**
	 * Ensures that the kind of the current token is the specified token kind.
	 * If it's not, Parser will report an error and change lt.
	 *
	 * @param expectedKind the token kind must be in the FIRST-SET of statements.
	 */
	private void ensureExpectedKind(TokenKind expectedKind) {
		if (peekKind() == expectedKind) {
			return;
		}
		unexpected(peek());
		synchronize();
		if (peekKind() != expectedKind) {
			// Avoid cascaded errors.
			insertToken(expectedKind);
		}
	}
	
	/**
	 * Replace the current token with a new token kind.
	 */
	private Token insertToken(TokenKind kind) {
		Token insertedToken = new Token(peek.getPos(), kind.getLiteral(), kind);
		this.peek = insertedToken;
		return insertedToken;
	}
	
	/* ======================== parse ======================== */


	/**
	 * Changes the last statement in the specified block into the 
	 * {@code Return-Stmt} if it's returnable.
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
	
	/**
	 * Lambda or Map is an expression but it can't be treated as
	 * a statement(ExprStmt).
	 */
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
				reportError(peek(), "Unexpected '%s'.", tokStr(peekKind()));
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
		Token endPos = peek();
		if (matchIf(RBRACE, true)) {
			block.endPos = Optional.of(endPos.getPos());
		}
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
	 *        | IfStmt | WhileStmt | ForStmt
	 *        | Break | Continue | Return
	 *        | BreakStmt | ContinueStmt| ReturnStmt
	 *        | VarDef | FunDef
	 *        | TryInterceptStmt | RaiseStmt
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
				case TRY:
					return parseTryInterceptStmt();
				case RAISE:
					return parseRaiseStmt();
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
		if (body == null) {
			reportError(posIfErr, msgIfErr);
			return null;
		}
		
		if (body instanceof Stmt.Block) {
			return (Stmt.Block) body;
		}
		
		Stmt.Block block =  new Stmt.Block();	
		block.stmts.add(body);
		return locate(body.pos, block);
	}

	/**
	 * ClassDef = "class" <IDENTIFIER> SuperClass "{" Methods "}"
	 */
	private Stmt parseClassDef() {
		boolean origin = inClass;
		inClass = true;
		try {
			Token location = match(CLASS);
			Token name = match(IDENTIFIER, "Expected class name.");
			Expr superClass = parseSuperClass();
			ignorableLinebreak();
			matchIf(LBRACE, true);
			Stmt.ClassDef classDef = new Stmt.ClassDef(
				name.getLiteral(), superClass, new ArrayList<Stmt.FuncDef>()
			);
			parseMethods(classDef);
			Token endPos = peek();
			if (matchIf(RBRACE, true)) {
				classDef.endPos = Optional.of(endPos.getPos());
			}
			return checkClass(locate(location, classDef));
		} finally {
			this.inClass = origin;
		}
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
					unexpected(peek());
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
			reportError(method.pos, "Duplicate initializer in the '%s' class.",
				  classDef.name
			);
		}
	}

	/**
	 * Method = <IDENTIFIER> Params Block
	 */
	private Stmt.FuncDef parseMethod() {
		FunctionType originalFuncType = curFunctionType;
		try {
			matchIf(FUN);
			Token name = peek();
			this.curFunctionType = INITIALIZER_NAME.equals(name.getLiteral())
				? FunctionType.INIT : FunctionType.METHOD;
			matchIf(IDENTIFIER, true);
			Stmt.Parameters params = parseParams(false);
			ensureExpectedKind(TokenKind.LBRACE);
			Stmt.Block body = parseBlock();
			return checkFunc(
				locate(name, new Stmt.FuncDef(name.getLiteral(), params, body))
			);
		} finally {
			this.curFunctionType = originalFuncType;
		}
	}
	
	/**
	 * SuperClass = [ ":" <IDENTIFIER> ]
	 */
	private Expr parseSuperClass() {
		if (matchIf(COLON)) {
			return parseExpr(); 
		}
		return null;
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
	 * IfStmt = "if" "(" Expr ")" [ <SEMI> ] Body [ "else" Body ]
	 */
	private Stmt parseIfStmt() {
		Token location = match(IF);
		matchIf(LPAREN, true);
		Expr expr = parseExpr();
		if (matchIf(RPAREN, true)) {
			ignorableLinebreak();
		}
		Stmt thenBody = parseBody(location.getPos(), "Invalid if statement.");
		Stmt elseBody = null;
		if (matchIf(ELSE)) {
			elseBody = parseBody(suitableErrorPosition(), "Invalid if-else statement.");
		}
		
		return constantFolde_IF(
			locate(location, new Stmt.If(expr, thenBody, elseBody))
		);
	}

	private Stmt constantFolde_IF(Stmt.If node) {
		Stmt thenBody = node.thenBody;
		Stmt elseBody = node.elseBody.orElse(EMPTY_STMT);	
		if (node.elseBody.isPresent()) {
			elseBody = node.elseBody.get();
		}
		if (node.condition.isConstant()) {
			return node.condition.isFalsely() ? elseBody : thenBody;
		}
		return node;
	}
	
	/**
	 * WhileStmt = "while" "(" Expr ")" [ <SEMI> ] Body
	 */
	private Stmt parseWhileStmt() {
		boolean originalInLoop = inLoop;
		this.inLoop = true;	
		try {
			Token location = match(WHILE);
			matchIf(LPAREN, true);
			Expr expr = parseExpr();
			if (matchIf(RPAREN, true)) {
				ignorableLinebreak();
			}
			Stmt body = parseBody(
				location.getPos(), "Invalid while statemenet");
			if (expr.isConstant() && expr.isFalsely()) {
				return EMPTY_STMT;
			}
			return locate(location, new Stmt.While(expr, body));
		} finally {
			this.inLoop = originalInLoop;
		}
	}
	
	/**
	 * ForStmt = "for" "(" <IDENTIFIER> "in" Expr ")" [SEMI] Body
	 */
	private Stmt.For parseForStmt() {
		boolean originalInLoop = inLoop;
		this.inLoop = true;	
		try {
			Token location = match(FOR);
			matchIf(LPAREN, true);
			String iteratingVar = match(IDENTIFIER).getLiteral();
			match(IN);
			Expr iterable = parseExpr();
			if (matchIf(RPAREN, true)) {
				ignorableLinebreak();
			}
			Stmt.Block body = parseBody(
				location.getPos(), "Invalid for statement.");
			return locate(location, new Stmt.For(iteratingVar, iterable, body));
		} finally {
			this.inLoop = originalInLoop;
		}
	}

	/**
	 * Break = "break" <SEMI>
	 */
	private Stmt.Break parseBreak() {
		Token location = match(BREAK);
		Stmt.Break breakStmt = locate(location, new Stmt.Break());
		if (!inLoop) {
			reportError(breakStmt, "The 'break' outside loop.");
		}
		matchSEMI();
		return breakStmt;
	}
	
	/**
	 * Continue = "continue" <SEMI>
	 */
	private Stmt.Continue parseContinue() {
		Token location = match(CONTINUE);
		Stmt.Continue continueStmt = locate(location, new Stmt.Continue());
		if (!inLoop) {
			reportError(continueStmt, "The 'continue' outside loop.");
		}
		matchSEMI();
		return continueStmt;
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
		if (this.curFunctionType == FunctionType.NONE) {
			reportError(location, "The 'return' outside function.");
		}
		matchSEMI();
		return locate(location, new Stmt.Return(expr));
	}
	
	/**
	 * VarDef = "var" <IDENTIFIER> [ "=" Expr ] <SEMI>
	 */
	private Stmt.VarDef parseVarDef() {
		Token position = match(VAR);
		Token identifier = match(IDENTIFIER, "Expected variable name.");
		Expr initializer = null;
		if (matchIf(ASSIGN)) {
			initializer = parseExpr();
		}
		matchSEMI();
		return locate(position, 
			new Stmt.VarDef(identifier.getLiteral(), initializer)
		);
	}

	/**
	 * FunDef = "fun" <IDENTIFIER> Params Block
	 */
	private Stmt.FuncDef parseFunDef() {
		FunctionType originalFuncType = curFunctionType;
		this.curFunctionType = FunctionType.FUNCTION;
		try {
			Token location = match(FUN);
			String name = match(IDENTIFIER, "Expected function name.").getLiteral();	
			Stmt.Parameters params = parseParams(false);
			Stmt.Block body = toFuncBlock(parseBlock());
			Stmt.FuncDef funcDef = locate(
				location, new Stmt.FuncDef(name, params, body)
			);
			return checkFunc(funcDef);
		} finally {
			this.curFunctionType = originalFuncType;
		}
	}

	/**
	 * Params = ( ( "(" Parameters ")" ) | Parameters ) [ <SEMI> ]
	 *
	 * Parameters = [ ( [ "*" ] <IDENTIFIER> )  [ "," Parameters ] ]
	 */
	private Stmt.Parameters parseParams(boolean optionalParentheses) {
		ArrayList<String> params = new ArrayList<>(6);
		boolean leftParenMatched = matchIf(LPAREN, !optionalParentheses);
		if (leftParenMatched && matchIf(RPAREN)) {
			ignorableLinebreak();
			return new Stmt.Parameters(params, -1);
		}
		int vaArgIndex = -1;
		do {
			if (matchIf(STAR)) {
				if (vaArgIndex != -1) {
					reportError(previous, "A function only have a parameter to" 
						  + " aceept variable arguments.");
				}
				vaArgIndex = params.size();
				matchIf(IDENTIFIER, true);
			} else if (!matchIf(IDENTIFIER)) {
				break;
			}
			Token nameTok = previous();
			params.add(nameTok.getLiteral());
		} while (matchIf(COMMA));	
		if (!optionalParentheses || leftParenMatched) {
			matchIf(RPAREN, "Expected parameter declaration.");
		}
		ignorableLinebreak();
		return new Stmt.Parameters(params, vaArgIndex);
	}
	
	/**
	 * RaiseStmt = "raise" Expr
	 */
	private Stmt.Raise parseRaiseStmt() {
		Token location = match(RAISE);
		return locate(location, new Stmt.Raise(parseExpr()));
	}

	/**
	 * TryInterceptStmt = "try" Block [ InterceptionStmts ] 
	 *                    [ ElseStmt ] [ FinallyStmt ]
	 */
	private Stmt.TryIntercept parseTryInterceptStmt() {
		Token location = match(TRY);
		Stmt.Block tryBlock = parseBlock();
		List<Stmt.Interception> interceptionStmts = null;
		Stmt.Block elseBlock = null;
		Stmt.Block finallyBlock = null;
		if (matchIf(INTERCEPT)) {
			interceptionStmts = parseInterceptionStmts();
		}
		if (matchIf(ELSE)) {
			elseBlock = parseBlock();
		}
		if (matchIf(FINALLY)) {
			finallyBlock = parseBlock();
		}
		return locate(location, new Stmt.TryIntercept(
			tryBlock, interceptionStmts, elseBlock, finallyBlock
		));
	}

	/**
	 * InterceptionStmts = ( InterceptionStmt )*
	 */
	private List<Stmt.Interception> parseInterceptionStmts() {
		List<Stmt.Interception> stmts = new ArrayList<>(4);
		do {
			stmts.add(parseInterceptionStmt());
		} while (matchIf(INTERCEPT));
		return stmts;
	}

	/**
	 * InterceptionStmt = "intercept" [ Expr ( "," Expr )* ]
	 *                    [ "as" <IDENTIFIER> ] Block
	 */
	private Stmt.Interception parseInterceptionStmt() {
		Token location = previous();
		String exceptionIdentifier = null;
		List<Expr> exceptions = new ArrayList<>();
		if (isFirstSetOfExpr(peekKind())) {
			do {
				exceptions.add(parseExpr());
			} while (matchIf(COMMA));
		}
		if (exceptions.size() > 255) {
			reportError(location, "Can't declare exceptions more than 255.");
		}
		if (matchIf(AS)) {
			exceptionIdentifier = match(IDENTIFIER).getLiteral();
		}
		ignorableLinebreak();
		Stmt.Block block = parseBlock();
		return locate(location, new Stmt.Interception(
			exceptions, exceptionIdentifier, block
		));
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
	 * Expr =  TernaryExpr [ Assignment ]
	 */
	private Expr parseExpr() {
		Expr lhs = parseTernaryOperator();
		if (!TokenKind.isAssignOperator(peek().getKind())) {
			return lhs;
		}
		return locate(lhs.pos, parseeAssignment(lhs));
	}
	
	/**
	 * Assignment = AssignOp Expr
	 * AssignOp = "+=" | "-=" | "*=" | "/=" | "%=" | "="
	 */
	private Expr parseeAssignment(Expr lhs) {
		TokenKind assOperator = peek().getKind();
		consume();
		Expr rhs = parseExpr();
		
		if (lhs instanceof Expr.VarRef) { 
			return new Expr.Assign(((Expr.VarRef) lhs).name, assOperator, rhs);
		}
		if (lhs instanceof Expr.GetAttr) {
			return new Expr.SetAttr((Expr.GetAttr) lhs, assOperator, rhs);
		}
		if (lhs instanceof Expr.GetItem) {
			return new Expr.SetItem((Expr.GetItem) lhs, assOperator, rhs);
		}
		
		reportError(lhs.pos, "Invalid left hand side.");
		return lhs;
	}
	
	/**
	 * Ternary = BinaryExpr [ "?" Expr ":" Expr ]
	 */
	private Expr parseTernaryOperator() {
		Expr condition = parseBinaryExpr(0);
		if (matchIf(QUESITION)) {
			Expr thanExpr = parseExpr();
			matchIf(COLON, true);
			Expr elseExpr = parseExpr();
			if (condition.isConstant()) {
				return condition.isFalsely() ? elseExpr : thanExpr;
			}
			return new Expr.TernaryOperator(condition, thanExpr, elseExpr);
		}
		return condition;
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
			left = ConstantFolder.foldBinaryExpr(
				new Expr.Binary(left, operator, right));
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
			Expr.Unary unaryExpr = locate(
				operator, 
				new Expr.Unary(operator.getKind(), parseExprTerm())
			);
			return ConstantFolder.foldUnaryExpr(unaryExpr);
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
	 *            | ( "(" [ Expr [ "," Tuple ] ] ")" )
	 *            | Array
	 *            | Map
	 *            | "this"
	 *            | ( "super" "." <IDENTIFIER> )
	 *            | InterpolatedString
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
				expr = parseExpr();
				if (matchIf(COMMA)) {
					expr = parseTuple(expr, location);
				}
				matchIf(RPAREN, true);
				break;
			
			case LBRACKET:
				expr = parseArray();
				break;
			
			case LBRACE:
				expr = parseMap();
				break;
				
			case THIS:
				consume();
				expr = locate(location, new Expr.This());
				if (!inClass) {
					reportError(expr, "The 'this' outside class.");
				}
				break;
				
			case SUPER:
				consume();
				match(DOT);
				Token name = match(IDENTIFIER);
				expr = locate(
					location, new Expr.Super(name.getLiteral())
				);
				if (!inClass) {
					reportError(expr, "The 'super' outside class.");
				}
				break;
				
			case INTERPOLATION:
				expr = parseInterpolatedString();
				break;
				
			case LAMBDA:
				expr = parseLambdaExpr();
				break;
				
			default: 
				reportError(suitableErrorPosition(), "Expected a expression terminal.");
				panic();
		}
		return parseExprSuffix(expr);
	}
	
	/**
	 * InterpolatedString = ( <INTERPOLATION> Expr )+ <STRING>
	 *
	 * Interpolation is syntatic sugar for calling "".join(...)
	 * So that the string:
	 *
	 *     "a${b+c}d${e}" 
	 *
	 * will be compiled to the:
	 *
	 *     "".join(["a", b+c, "d", e])
	 *
	 * Empty strings will be ignored.
	 */
	private Expr parseInterpolatedString() {
		ArrayList<Expr> arr = new ArrayList<>();
		Token first = peek();
		while (true) {
			Token tok = peek();
			if (tok.getKind() == INTERPOLATION) {
				consume();
				if (!"".equals(tok.getLiteral())) {
					arr.add(strLitNode(tok));
				}		
				arr.add(parseExpr());
				continue;
			}
			if (peekKind() == STRING) {
				consume();
				if (!"".equals(tok.getLiteral())) {
					arr.add(strLitNode(tok));
				}
				break;
			}	
			reportError(tok, "Syntax error in the interpolated string.");
			break;
		}
		return joinExprArr(first, arr);
	}
	
	private Expr.StringLiteral strLitNode(Token tok) {
		return strLitNode(tok, tok.getLiteral());
	}

	private Expr.StringLiteral strLitNode(Token tok, String lit) {
		return locate(tok, new Expr.StringLiteral(lit));
	}
	
	private Expr joinExprArr(Token location, ArrayList<Expr> exprArr) {
		Expr array = locate(location, new Expr.Array(exprArr));
		Expr joinMet = new Expr.GetAttr(strLitNode(location, ""), "join");
		List<Expr.Argument> args = new ArrayList<>();
		args.add(new Expr.Argument(array, false));
		return locate(location, new Expr.CallFunc(joinMet, args));
	}

	/**
	 * Tuple = [ Expr ( "," Expr )* [ "," ] [ SEMI ] ]
	 */
	private Expr parseTuple(Expr expr, Token location) {
		List<Expr> tuple = new ArrayList<>();
		tuple.add(expr);
		do {
			if (peekKind() == RPAREN) {
				break;
			}
			tuple.add(parseExpr());
		} while (matchIf(COMMA));
		ignorableLinebreak();
		if (tuple.size() > 255) {
			reportError(location, 
				"The length of a tuple literal can't be greater than 255.");
		}
		return locate(location, new Expr.Tuple(tuple));
	}
	
	/**
	 * Array = "[" [ Expr ( "," Expr )* ] [ SEMI ] "]"
	 */
	private Expr.Array parseArray() {
		ArrayList<Expr> elements = new ArrayList<>();
		Token location = match(LBRACKET);
		if (matchIf(RBRACKET)) {
			return locate(location, new Expr.Array(elements));
		}
		do {
			// Trailing commas
			if (peekKind() == TokenKind.RBRACKET) {
				break;
			}
			elements.add(parseExpr());
		} while (matchIf(COMMA));
		ignorableLinebreak();
		matchIf(RBRACKET, true);
		return locate(location, new Expr.Array(elements));
	}
	
	/**
	 * Map = "{" [ KV ( "," KV )* ] [ <SEMI> ] "}"
	 */
	private Expr parseMap() {
		Token location = match(LBRACE);
		if (matchIf(RBRACE)) {
			return locate(location, new Expr.Map());
		}
		List<Expr> keys = new ArrayList<>(8);
		List<Expr> values = new ArrayList<>(8);
		do {
			// Trailing commas
			if (peekKind() == TokenKind.RBRACE) {
				break;
			}
			parseKV(keys, values);
		} while (matchIf(COMMA));
		ignorableLinebreak();
		matchIf(RBRACE, true);
		return locate(location, new Expr.Map(keys, values));
	}
	
	/**
	 * KV = Expr ":" Expr
	 */
	private void parseKV(List<Expr> keys, List<Expr> values) {
		keys.add(parseExpr());
		matchIf(COLON, true);
		values.add(parseExpr());
	}

	/**
	 * ExprSuffix = ( Agruments | GetAttr | GetItem )*
	 * GetAttr = [ SEMI ] "." <IDENTIFIER>
	 * GetItem = "[" Expr "]"
	 */
	@SuppressWarnings("fallthrough")
	private Expr parseExprSuffix(Expr expr) {
		while (true) {
			Token tok = peek();
			switch (tok.getKind()) {
				case LPAREN:
					List<Expr.Argument> args = parseArguments(tok.getPos());
					expr = locate(tok, new Expr.CallFunc(expr, args));
					continue;
					
				case LBRACKET:
					consume();
					Expr key = parseExpr();
					matchIf(RBRACKET, true);
					expr = locate(tok, new Expr.GetItem(expr, key));
					continue;
				
				case SEMI:
					// chained method syntax:
					//     stream()\n.filter()\n.map()\n.close()
					if (peek(1).getKind() != DOT) {
						break;
					}
					// consume '\n'
					consume();
					// reset position marker.
					tok = peek();
					// continue to parse down dot.
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
	 * Agruments = "(" [ [ "*" ] Expr ( "," [ "*" ] Expr )* ] 
	 *             [ <SEMI> ] ")"
	 */
	private List<Expr.Argument> parseArguments(Position pos) {
		consume();
		List<Expr.Argument> args = new ArrayList<>();
		if (matchIf(RPAREN)) {
			return args;
		}
		do {
			if (peekKind() == RPAREN) {
				break;
			}
			boolean isUnpack = matchIf(STAR);
			Expr arg = parseExpr();
			args.add(new Expr.Argument(arg, isUnpack));
		} while (matchIf(COMMA));
		ignorableLinebreak();
		matchIf(RPAREN, true);
		if (args.size() > MAX_PARAMETER_NUMBER) {
			reportError(pos, "Too many arguments.");
		}
		return args;
	}
	
	/**
	 * LambdaExpr = "lambda" Params "->" Body
	 */
	private Expr.Lambda parseLambdaExpr() {
		FunctionType originalFuncType = this.curFunctionType;
		this.curFunctionType = FunctionType.LAMBDA;	
		try {
			Token location = match(LAMBDA);
			Stmt.Parameters params = parseParams(true);
			match(ARROW);
			
			boolean originalSingleLineLambda = inSingleLineLambda;
			inSingleLineLambda = peekKind() != TokenKind.LBRACE;
			Stmt.Block body = toFuncBlock(
				parseBody(location.getPos(), "Invalid lambda expression.")
			);	
			inSingleLineLambda = originalSingleLineLambda;
			
			Expr.Lambda lambda = new Expr.Lambda(params, body);
			lambda.pos = location.getPos();
			lambda.funcDef.pos = location.getPos();
			checkFunc(lambda.funcDef);
			return lambda;
		} finally {
			this.curFunctionType = originalFuncType;
		}
	}

}
