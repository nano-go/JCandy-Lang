package com.nano.candy.interpreter.i2.codegen;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.AstVisitor;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.interpreter.i2.rtda.Chunk;
import com.nano.candy.parser.TokenKind;
import com.nano.candy.std.Names;
import com.nano.candy.utils.ArrayUtils;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.nano.candy.interpreter.i2.instruction.Instructions.*;
import static com.nano.candy.interpreter.i2.rtda.ConstantValue.*;

/**
 * This converts the Candy syntax tree to a chunk.
 */
public class CodeGenerator implements AstVisitor<Void, Void> {
	
	/**
	 * A loop marker represents a current loop.
	 */
	private class LoopMarker {
		
		/**
		 * The begin position of the current loop.
		 */
		public int beginPosition;
		
		/**
		 * the unresolve lable of Break statements.
		 */
		public int[] lablePosOfBreakStmts;
		public int bp;
		
		public LoopMarker(int beginPosition) {
			this.beginPosition = beginPosition;
			this.lablePosOfBreakStmts = new int[4]; 
		}
		
		public void addLableForBreak(int position) {
			lablePosOfBreakStmts = ArrayUtils.growCapacity(
				lablePosOfBreakStmts, bp
			);
			lablePosOfBreakStmts[bp ++] = position;
		}
		
		public void concatenetesLableforBreak() {
			for (int i = 0; i < bp; i ++) {
				builder.backpatch(lablePosOfBreakStmts[i]);
			}
			lablePosOfBreakStmts = null;
		}
	}

	private ChunkBuilder builder;
	private LocalsTable locals;
	
	private boolean isInteractionMode;
	
	private LinkedList<LoopMarker> loopMarkers;
	private boolean isInInitializer;
	
	public CodeGenerator(boolean isInteractionMode) {
		this.builder = new ChunkBuilder();
		this.locals = new LocalsTable();
		this.loopMarkers = new LinkedList<>();
		this.isInteractionMode = isInteractionMode;
	}
	
	public Chunk genCode(ASTreeNode node) {
		ASTreeNode.accept(node, this);
		builder.emitop(OP_EXIT);
		builder.setGlobalSlots(locals.maxSlotCount());
		builder.setSourceFileName(node.pos.getFileName());
		return builder.build();
	}
	
	private int line(ASTreeNode node) {
		return node.pos.getLine();
	}
	
	private LocalsTable enterScope() {
		locals.enterScope();
		return locals;
	}
	
	private LocalsTable closeScope(boolean closeSlots) {
		if (!closeSlots) {
			locals.exitScope();
			return locals;
		}
		int index = locals.localCount-1;
		List<LocalsTable.Local> closedLocals = locals.exitScope();
		if (closedLocals.isEmpty()) {
			return locals;
		}
		
		for (LocalsTable.Local local : closedLocals) {
			if (local.isCaptured) {
				builder.emitop(OP_CLOSE_UPVALUE);
				builder.emit1((byte) index);
			}
			index --;
		}
		builder.emitopWithArg(OP_CLOSE_SLOT, locals.localCount);
		return locals;
	}
	
	/**
	 * Declares a variable in the current local scope.
	 *
	 * @return the slot of the given variable or -1 if the current scope 
	 *         is the global scope.
	 */
	private int declrVariable(String name) {
		if (locals.isInGlobal()) {
			return -1;
		}
		int slot = locals.resolveLocalInCurrentDeepth(name);
		if (slot == -1) {
			slot = locals.addLocal(name);
		}
		return slot;
	}
	
	/**
	 * Defines a declared variable or global variable.
	 */
	private void defineVariable(String name, int line) {
		if (locals.isInGlobal()) {
			builder.emitop(OP_GLOBAL_DEFINE, line);
			builder.emitStringConstant(name);
			return;
		}
		int slot = locals.resolveLocalInCurrentDeepth(name);
		if (slot == -1) {
			throw new Error("The '" + name + "' has not been declared.");
		} 
		builder.emitopWithArg(OP_POP_STORE, slot, line);
	}
	
	/**
	 * Generate code to load a named variable to stack top.
	 */ 
	private void loadVariable(String name, int line) {
		int slot = locals.resolveLocal(name);
		if (slot != -1) {
			builder.emitLoad(slot, line);
		} else if ((slot = locals.resolveUpvalue(name)) != -1){
			builder.emitopWithArg(OP_LOAD_UPVALUE, slot, line);
		} else {
			builder.emitop(OP_GLOBAL_GET, line);
			builder.emitStringConstant(name);
		}
	}
	
	/**
	 * Generate code to store the stack-top operand to the given 
	 * named variable.
	 */ 
	private void storeVariable(String name, int line) {
		int slot = locals.resolveLocal(name);
		if (slot != -1) {
			builder.emitStore(slot, line);
		} else if ((slot = locals.resolveUpvalue(name)) != -1){
			builder.emitopWithArg(OP_STORE_UPVALUE, slot, line);
		} else {
			builder.emitop(OP_GLOBAL_SET, line);
			builder.emitStringConstant(name);
		}
	}
	
	/**
	 * Parse the assign expression.
	 *
	 * Assign:  a    [op]= value
	 * SetAttr: a.b  [op]= value
	 * SetItem: a[c] [op]= value
	 */ 
	private void parseAssignExpr(Expr lhs, TokenKind operator, Expr rhs) {
		boolean isOnlyAssignOp = operator == TokenKind.ASSIGN;
		if (!isOnlyAssignOp) {
			loadLeftValue(lhs);	
		}
		
		rhs.accept(this);
		
		if (!isOnlyAssignOp) {
			byte opcode = OperatorInstructionMap.lookupOperatorIns(operator);		
			builder.emitop(opcode, line(lhs));
		}
		assignTo(lhs, isOnlyAssignOp);
	}
	
	/**
	 * Load the given lhs expression to stack top.
	 */
	private void loadLeftValue(Expr lhs) {
		if (lhs instanceof Expr.Assign) {	
			loadVariable(((Expr.Assign)lhs).name, line(lhs));	
			return;
		}
		
		if (lhs instanceof Expr.SetAttr) {
			((Expr.SetAttr)lhs).objExpr.accept(this);
			builder.emitop(OP_DUP);
			builder.emitop(OP_GET_ATTR, line(lhs));
			builder.emitStringConstant(((Expr.SetAttr)lhs).attr);
			return;
		}
		
		if (lhs instanceof Expr.SetItem) {
			Expr.SetItem setNode = (Expr.SetItem)lhs;
			setNode.key.accept(this);
			setNode.objExpr.accept(this);
			builder.emitop(OP_DUP_2);
			builder.emitop(OP_GET_ITEM);
			return;
		}
		throw new Error("Unknown Expr Type: " + lhs.getClass().getName());
	}
	
	private void assignTo(Expr leftNode, boolean isOnlyAssignOp) {
		if (leftNode instanceof Expr.Assign) {
			storeVariable(((Expr.Assign)leftNode).name, line(leftNode));
			return;
		}
		
		if (leftNode instanceof Expr.SetAttr) {
			Expr.SetAttr node = ((Expr.SetAttr)leftNode);
			if (isOnlyAssignOp) {
				node.objExpr.accept(this);
			} else {
				// operand stack: obj, value
				// rotate ->      value, obj
				builder.emitop(OP_ROT_2);
			}
			builder.emitop(OP_SET_ATTR, line(leftNode));
			builder.emitStringConstant(node.attr);
			return;
		}
		
		if (leftNode instanceof Expr.SetItem) {
			Expr.SetItem node = ((Expr.SetItem)leftNode);
			if (isOnlyAssignOp) {
				node.key.accept(this);
				node.objExpr.accept(this);
			} else {
				// operand stack: key, obj, value
				// rotate ->      value, key, obj
				builder.emitop(OP_ROT_3);
			}
			builder.emitop(OP_SET_ITEM);
			return;
		}
		
		throw new Error("Unknown Expr Type: " + leftNode.getClass().getName());
	}
	
	/**
	 * Write code to load the speficied function to stack top.
	 */
	private void loadFunction(String name, Stmt.FuncDef node) {
		boolean originalIsInInit = isInInitializer;
		this.isInInitializer = false;
		
		MethodInfo methodInfo = new MethodInfo();
		builder.emitop(OP_FUN, line(node));
		builder.emitConstant(methodInfo);
		makeMethodInfo(methodInfo, name, node, false);
		
		this.isInInitializer = originalIsInInit;
	}
	
	private MethodInfo makeMethodInfo(Stmt.FuncDef node, boolean isMethod) {
		return makeMethodInfo(new MethodInfo(), node.name.get(), node, isMethod);
	}
	
	private MethodInfo makeMethodInfo(MethodInfo methodInfo, String name, 
	                                  Stmt.FuncDef node, boolean definedInClass) {
		methodInfo.name = name;
		methodInfo.arity = node.params.size();

		enterFunctionScope();
		
		if (definedInClass) {
			declrVariable("this");
			methodInfo.arity ++;
		}

		for (String param : node.params) {
			locals.addLocal(param);
		}

		int posBodyBegin = builder.curCp();
		for (Stmt stmt : node.body.stmts) {
			stmt.accept(this);
		}

		methodInfo.codeBytes = builder.curCp() - posBodyBegin;
		methodInfo.slots = (byte)locals.maxSlotCount();
		methodInfo.stackSize = builder.state().stackSize;
		methodInfo.upvalues = genUpvalueBytes(locals.upvalues());	
		
		closeFunctionScope();
		return methodInfo;
	}
	
	/**
	 * Gen bytes in the specified format for the given upvalues.
	 */
	private byte[] genUpvalueBytes(LocalsTable.Upvalue[] upvalues) {
		byte[] upvalueBytes = new byte[upvalues.length*2];
		for (int i = 0; i < upvalues.length; i ++) {
			upvalueBytes[i*2] = (byte)(upvalues[i].isLocal ? 1 : 0);
			upvalueBytes[i*2 + 1] = (byte) upvalues[i].slot;
		}
		return upvalueBytes;
	}
	
	private void enterFunctionScope() {
		LocalsTable newLocals = new LocalsTable();
		newLocals.enclosing = locals;
		locals = newLocals;
		enterScope();
		builder.newState();
	}

	private void closeFunctionScope() {
		builder.closeState();
		closeScope(false);
		locals = locals.enclosing;
	}
	
	@Override
	public void visit(Program node) {	
		for (Stmt stmt : node.block.stmts) {
			stmt.accept(this);
		}
	}
	
	@Override
	public Void visit(Stmt.ClassDef node) {
		ClassInfo classInfo = new ClassInfo();
		classInfo.className = node.name;
		
		if (node.superClassName.isPresent()) {
			Expr.VarRef superClass = node.superClassName.get();
			// adavance to load super class object to operand stack top.
			loadVariable(superClass.name, line(superClass));
			
			// If true, VM will fetch the super class from 
			// the operand stack top.
			classInfo.hasSuperClass = true;
		} else {
			builder.state().push(1);
		}
		
		declrVariable(node.name);
		enterScope();
		int superSlot = declrVariable("super");
		
		builder.emitop(OP_CLASS, line(node));
		builder.emitConstant(classInfo);
		builder.emit1((byte) superSlot);
		
		classInfo.initializer = initalizer(node);
		classInfo.methods = methods(node);
		
		closeScope(true);
		defineVariable(node.name, -1);
		return null;
	}

	private Optional<MethodInfo> initalizer(Stmt.ClassDef node) {
		if (node.initializer.isPresent()) {
			Stmt.FuncDef initalizer = node.initializer.get();
			this.isInInitializer = true;
			Optional<MethodInfo> methodInfo = Optional.of(
				makeMethodInfo(initalizer, true)
			);
			this.isInInitializer = false;
			return methodInfo;
		} 
		return Optional.empty();
	}

	private MethodInfo[] methods(Stmt.ClassDef node) {
		MethodInfo[] methodInfos = new MethodInfo[node.methods.size()];
		for (int i = 0; i < methodInfos.length; i ++) {
			Stmt.FuncDef funcDef = node.methods.get(i);
			methodInfos[i] = makeMethodInfo(funcDef, true);
		}
		return methodInfos;
	}

	@Override
	public Void visit(Stmt.FuncDef node) {
		String nodeName = node.name.get();
		declrVariable(nodeName);
		loadFunction(nodeName, node);
		defineVariable(nodeName, -1);
		return null;
	}

	@Override
	public Void visit(Stmt.Block node) {
		enterScope();
		for (Stmt stmt : node.stmts) {
			stmt.accept(this);
		}
		closeScope(true);
		return null;
	}
	
	@Override
	public Void visit(Stmt.While node) {
		int loopPos = builder.curCp();
		loopMarkers.push(new LoopMarker(loopPos));
				
		// Optimize 'while (true)'
		boolean isTrueConstant = node.condition.isConstant() && !node.condition.isFalsely();
		
		int jumpOutLable = -1;
		if (!isTrueConstant) {
			node.condition.accept(this);
			jumpOutLable = builder.emitLable(OP_POP_JUMP_IF_FALSE, line(node));
		}
		
		node.body.accept(this);
		// Jump back to the begin position of While statement
		builder.emitLable(OP_LOOP, builder.curCp() - loopPos + 1, -1);
		
		if (!isTrueConstant) {
			builder.backpatch(jumpOutLable);
		}
		
		loopMarkers.pop().concatenetesLableforBreak();
		return null;
	}

	@Override
	public Void visit(Stmt.For node) {
		node.iterable.accept(this);
		
		enterScope();
		
		int iteratorIndex = invokeIterator(line(node));	
		int iteratingVarSlot = locals.addLocal(node.iteratingVar);
		
		int loopPos = builder.curCp();
		loopMarkers.push(new LoopMarker(loopPos));
		
		invokeHasNext();
		int jumpOutLable = builder.emitLable(OP_POP_JUMP_IF_FALSE, -1);
		invokeNext(iteratingVarSlot, iteratorIndex);	
		
		for (Stmt stmt : node.body.stmts) {
			stmt.accept(this);
		}
		builder.emitLable(OP_LOOP, (builder.curCp() - loopPos + 1), -1);
		
		builder.backpatch(jumpOutLable);
		loopMarkers.pop().concatenetesLableforBreak();
		builder.emitop(OP_POP);
		closeScope(true);	
		return null;
	}
	
	private int invokeIterator(int lineNumber) {
		builder.emitInvoke(Names.METHOD_ITERATOR, 0, lineNumber);
		// add hidden local variable for iterator.
		int iteratorIndex = locals.addLocal("hidden$iterator");
		builder.emitStore(iteratorIndex, -1);
		return iteratorIndex;
	}

	private void invokeHasNext() {
		builder.emitop(OP_DUP);
		builder.emitInvoke(Names.METHOD_ITERATOR_HAS_NEXT, 0, -1);
	}
	
	private void invokeNext(int iteratingVarSlot, int iteratorIndex) {
		builder.emitop(OP_DUP);
		builder.emitInvoke(Names.METHOD_ITERATOR_NEXT, 0, -1);
		builder.emitopWithArg(OP_POP_STORE, iteratingVarSlot, -1);
	}
	
	@Override
	public Void visit(Stmt.Continue node) {
		int continuePos = loopMarkers.peek().beginPosition;
		builder.emitLable(
			OP_LOOP, (builder.curCp() - continuePos + 1), line(node)
		);
		return null;
	}

	@Override
	public Void visit(Stmt.Break node) {
		loopMarkers.peek().addLableForBreak(
			builder.emitLable(OP_JUMP, line(node))
		);
		return null;
	}

	@Override
	public Void visit(Stmt.If node) {
		node.condition.accept(this);
		// Jump to the beginning of the else block or jump out 
		// of the If statement.
		int jumpToElseLable = builder.emitLable(OP_POP_JUMP_IF_FALSE, line(node));
		
		node.thenBody.accept(this);
		if (node.elseBody.isPresent()) {
			// If the else block exists, VM needs to skip the else block
			// at the end of then-body.
			int jumpOutIfLable = builder.emitLable(OP_JUMP, -1);
			
			builder.backpatch(jumpToElseLable);			
			node.elseBody.get().accept(this);
			builder.backpatch(jumpOutIfLable);
		} else {
			builder.backpatch(jumpToElseLable);
		}
		
		return null;
	}

	@Override
	public Void visit(Stmt.Assert node) {
		node.condition.accept(this);
		int lable = builder.emitLable(OP_POP_JUMP_IF_TRUE, line(node));
		node.errorInfo.accept(this);
		builder.emitop(OP_ASSERT, line(node));
		builder.backpatch(lable);
		return null;
	}

	@Override
	public Void visit(Stmt.ExprS node) {
		node.expr.accept(this);
		boolean shouldPrint = 
			!(node.expr instanceof Expr.Assign) &&
			!(node.expr instanceof Expr.SetItem) &&
			!(node.expr instanceof Expr.SetAttr);
		if (isInteractionMode && locals.isInGlobal() && shouldPrint) {
			builder.emitop(OP_PRINT, line(node));
		} else {
			builder.emitop(OP_POP, line(node));
		}
		return null;
	}

	@Override
	public Void visit(Stmt.Return node) {
		if (isInInitializer) {
			// the 'this' pointer is at the slot 0.
			// VM requires to return the 'this' pointer in initializer.
			builder.emitop(OP_LOAD0, line(node));
		} else if (node.expr.isPresent()) {
			node.expr.get().accept(this);
		} else {
			builder.emitop(OP_RETURN_NIL);
			return null;
		}
		
		builder.emitop(OP_RETURN);
		return null;
	}

	@Override
	public Void visit(Stmt.VarDef node) {
		if (node.init.isPresent()) {
			node.init.get().accept(this);
		} else {
			builder.emitop(OP_NULL, line(node));
		}
		declrVariable(node.name);
		defineVariable(node.name, -1);
		return null;
	}

	@Override
	public Void visit(Expr.Assign node) {
		parseAssignExpr(node, node.assignOperator, node.rhs);
		return null;
	}

	@Override
	public Void visit(Expr.Unary node) {
		node.expr.accept(this);
		switch (node.operator) {
			case PLUS:
				builder.emitop(OP_POSITIVE, line(node));
				break;
			case MINUS:
				builder.emitop(OP_NEGATIVE, line(node));
				break;
			case NOT:
				builder.emitop(OP_NOT, line(node));
				break;
		}
		return null;
	}

	@Override
	public Void visit(Expr.Binary node) {
		switch (node.operator) {
			case LOGICAL_AND:
			case LOGICAL_OR:
				genBinaryLogicalOpCode(node);
				return null;
		}
		
		node.left.accept(this);
		node.right.accept(this);
		
		byte opcode = OperatorInstructionMap.lookupOperatorIns(node.operator);
		builder.emitop(opcode, line(node));
		return null;
	}

	private void genBinaryLogicalOpCode(Expr.Binary node) {
		node.left.accept(this);
		int bp;
		int opLine = node.operatorPos.getLine();
		switch (node.operator) {
			case LOGICAL_AND:
				bp = builder.emitLable(OP_JUMP_IF_FALSE, opLine);
				break;
			case LOGICAL_OR:
				bp = builder.emitLable(OP_JUMP_IF_TRUE, opLine);
				break;
			default:
				throw new Error(node.operator.getLiteral());
		}
		
		// Discard the left-hand value
		builder.emitop(OP_POP);
		node.right.accept(this);
		
		builder.backpatch(bp);
	}

	@Override
	public Void visit(Expr.Lambda node) {
		String name = genLambdaName(node);
		loadFunction(name, node.funcDef);
		return null;
	}
	
	private String genLambdaName(Object obj) {
		return "lambda$" + obj.hashCode();
	}

	/**
	 * For calling attributes, super methods, global vars and local vars, 
	 * we are going to optimize them by the following superinstrcutions:
	 *
	 * OP_SUPER_GET, OP_CALL  -> OP_SUPER_INVOKE
	 * OP_GET_ATTR, OP_CALL   -> OP_INVOKE
	 * OP_LOAD, OP_CALL       -> OP_CALL_SLOT
	 * OP_GET_GLOBAL, OP_CALL -> OP_CALL_GLOBAL
	 */
	@Override
	public Void visit(Expr.CallFunc node) {	
		Collections.reverse(node.arguments);
		for (Expr arg : node.arguments) {
			arg.accept(this);
		}
		
		final int argc = node.arguments.size();
		
		// call attributes
		if (node.expr instanceof Expr.GetAttr) {
			Expr.GetAttr getAttrNode = (Expr.GetAttr) node.expr;
			getAttrNode.objExpr.accept(this);
			builder.emitInvoke(getAttrNode.attr, argc, line(node));
			builder.state().pop(argc);
			return null;
		} 
		
		// call super methods
		if (node.expr instanceof Expr.Super) {
			Expr.Super superNode = (Expr.Super) node.expr;
			loadVariable("this", line(node));
			loadVariable("super", -1);
			builder.emitopWithArg(
				OP_SUPER_INVOKE, argc, -1);
			builder.emitStringConstant(superNode.reference);
			builder.state().pop(argc);
			return null;
		}
		
		if (node.expr instanceof Expr.VarRef) {
			Expr.VarRef varRef = (Expr.VarRef) node.expr;
			int slot = locals.resolveLocal(varRef.name);
			// call local vars
			if (slot != -1) {
				builder.emitopWithArg(OP_CALL_SLOT, argc, line(node));
				builder.emit1((byte) slot);
				builder.state().pop(argc);
				return null;
			}
			
			// call global vars
			if (locals.resolveUpvalue(varRef.name) == -1) {
				builder.emitopWithArg(OP_CALL_GLOBAL, argc, line(node));
				builder.emitStringConstant(varRef.name);
				builder.state().pop(argc);
				return null;
			}
		} 
		
		node.expr.accept(this);
		builder.emitopWithArg(
			OP_CALL, node.arguments.size(), line(node));
		builder.state().pop(node.arguments.size());
		return null;
	}

	@Override
	public Void visit(Expr.SetAttr node) {
		parseAssignExpr(node, node.assignOperator, node.rhs);
		return null;
	}

	@Override
	public Void visit(Expr.GetAttr node) {
		node.objExpr.accept(this);
		builder.emitop(OP_GET_ATTR, line(node));
		builder.emitStringConstant(node.attr);
		return null;
	}

	@Override
	public Void visit(Expr.SetItem node) {
		parseAssignExpr(node, node.assignOperator, node.rhs);
		return null;
	}

	@Override
	public Void visit(Expr.GetItem node) {
		node.key.accept(this);
		node.objExpr.accept(this);
		builder.emitop(OP_GET_ITEM);
		return null;
	}

	@Override
	public Void visit(Expr.Array node) {
		builder.emitop(OP_NEW_ARRAY, line(node));
		builder.emitIntegerConstant(node.elements.size());
		int size = node.elements.size();
		int p = 0;
		while (size > 0) {
			int c = Math.min(size, 255);
			p += c;
			for (int i = 1; i <= c; i ++) {
				node.elements.get(p-i).accept(this);
			}
			size -= c;
			builder.state().pop(c);
			builder.emitopWithArg(OP_APPEND, c);
		}
		return null;
	}
	
	@Override
	public Void visit(Expr.IntegerLiteral node) {
		builder.emitop(OP_ICONST, line(node));
		builder.emitIntegerConstant(node.value);
		return null;
	}

	@Override
	public Void visit(Expr.DoubleLiteral node) {
		builder.emitop(OP_DCONST, line(node));
		builder.emitDoubleConstant(node.value);
		return null;
	}

	@Override
	public Void visit(Expr.StringLiteral node) {
		builder.emitop(OP_SCONST, line(node));
		builder.emitStringConstant(node.literal);
		return null;
	}

	@Override
	public Void visit(Expr.BooleanLiteral node) {
		builder.emitop(
			node.value ? OP_TRUE : OP_FALSE, line(node)
		);
		return null;
	}

	@Override
	public Void visit(Expr.NullLiteral node) {
		builder.emitop(OP_NULL, line(node));
		return null;
	}

	@Override
	public Void visit(Expr.VarRef node) {
		loadVariable(node.name, line(node));
		return null;
	}

	@Override
	public Void visit(Expr.Super node) {
		loadVariable("this", line(node));
		loadVariable("super", -1);
		builder.emitop(OP_SUPER_GET);
		builder.emitStringConstant(node.reference);
		return null;
	}

	@Override
	public Void visit(Expr.This node) {
		loadVariable("this", line(node));
		return null;
	}
}
