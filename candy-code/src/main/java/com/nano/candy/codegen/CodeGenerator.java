package com.nano.candy.codegen;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.AstVisitor;
import com.nano.candy.ast.Expr;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.Stmt;
import com.nano.candy.code.Chunk;
import com.nano.candy.code.ConstantValue;
import com.nano.candy.parser.TokenKind;
import com.nano.candy.std.Names;
import com.nano.candy.utils.ArrayUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

import static com.nano.candy.code.OpCodes.*;
import static com.nano.candy.code.ConstantValue.*;

/**
 * This converts a Candy syntax tree to a code chunk.
 */
public class CodeGenerator implements AstVisitor<Void, Void> {
	
	private static class LoopMarker {
		
		/**
		 * The beginning pc of this loop.
		 */
		public int beginningPc;
		
		/**
		 * The break lables in this loop.
		 */
		public int[] breakLabels;
		public int bp;
		
		public LoopMarker(int beginPosition) {
			this.beginningPc = beginPosition;
			this.breakLabels = new int[4];
			this.bp = 0;
		}
		
		public void addLableForBreak(int position) {
			breakLabels = ArrayUtils.growCapacity(
				breakLabels, bp
			);
			breakLabels[bp ++] = position;
		}
		
		public void concatsLableForBreak(ChunkBuilder builder) {
			for (int i = 0; i < bp; i ++) {
				builder.backpatch(breakLabels[i]);
			}
			breakLabels = null;
		}
	}
	
	private static int line(ASTreeNode node) {
		return node.pos.getLine();
	}
	
	private ChunkBuilder builder;
	private LocalTable locals;
	
	private boolean isInteractionMode;
	private boolean isDebugMode;
	
	private boolean isInInitializer;
	private LinkedList<LoopMarker> loopMarkers;
	
	/**
	 * We use indexes to access the global variable instead of names.
	 *
	 * This map stores the indexes of each global variable name.
	 */
	private HashMap<String, Integer> globalVariableTable;
	
	/**
	 * The array stores the global variable name of the specified index.
	 */
	private ArrayList<String> globalVariableNames;
	
	public CodeGenerator(boolean isInteractionMode) {
		this(isInteractionMode, false);
	}

	public CodeGenerator(boolean isInteractionMode, boolean debugMode) {
		this.builder = new ChunkBuilder();
		this.locals = new LocalTable();
		this.loopMarkers = new LinkedList<>();
		this.isInteractionMode = isInteractionMode;
		this.isDebugMode = debugMode;
		this.globalVariableTable = new HashMap<>();
		this.globalVariableNames = new ArrayList<>();
	}
	
	public Chunk genCode(ASTreeNode node) {
		ASTreeNode.accept(node, this);
		builder.emitop(OP_EXIT);
		builder.setSourceFileName(node.pos.getFileName());
		builder.setGlobalVariableNames(globalVariableNames);
		builder.setGlobalVariableTable(globalVariableTable);
		return builder.build(locals.maxSlotCount());
	}
	
	private void walkBlock(Stmt.Block block) {
		for (Stmt stmt : block.stmts) {
			stmt.accept(this);
		}
		if (isDebugMode && block.endPos.isPresent()) {
			builder.emitop(OP_NOP, block.endPos.get().getLine());
		}
	}
	
	private LocalTable enterScope() {
		locals.enterScope();
		return locals;
	}
	
	/**
	 * Exit the current scope and all local variables in this scope is removed.
	 *
	 * @param closeUpvalues If true, emit an instruction to close all upvalues in 
	 *                      the current scope.
	 */
	private ConstantValue.CloseIndexes closeScope(boolean closeUpvalues) {
		if (!closeUpvalues) {
			locals.exitScope();
			return null;
		}
		ConstantValue.CloseIndexes close = locals.getCloseInfo(true);
		emitCloseInstruction(close);
		return close;
	}

	private void emitCloseInstruction(ConstantValue.CloseIndexes closeInfo) {
		if (closeInfo != null) {
			builder.emitopWithConst(OP_CLOSE, closeInfo);
		}
	}
	
	/**
	 * Returns the index of the specified global variable name.
	 */
	private int globalVarIndex(String name) {
		Integer globalVarIndex = globalVariableTable.get(name);
		if (globalVarIndex == null) {
			globalVarIndex = globalVariableNames.size();
			globalVariableNames.add(name);
			globalVariableTable.put(name, globalVarIndex);
		}
		return globalVarIndex;
	}
	
	/**
	 * Declares a variable in the current scope.
	 *
	 * @return the slot of the given variable or -1 if the current scope 
	 *         is the global.
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
	 * Generates code to define a declared or global variable with the 
	 * element at the stack top.
	 */
	private void defineVariable(String name, int line) {
		if (locals.isInGlobal()) {
			builder.emitop(OP_GLOBAL_DEFINE, line);
			builder.emitIndex(globalVarIndex(name));
			return;
		}
		int slot = locals.resolveLocalInCurrentDeepth(name);
		if (slot == -1) {
			throw new Error("The '" + name + "' has not been declared.");
		} 
		builder.emitopWithArg(OP_POP_STORE, slot, line);
	}
	
	/**
	 * Generates code to load a named variable to stack top.
	 */ 
	private void loadVariable(String name, int line) {
		int slot = locals.resolveLocal(name);
		if (slot != -1) {
			builder.emitLoad(slot, line);
		} else if ((slot = locals.resolveUpvalue(name)) != -1){
			builder.emitopWithArg(OP_LOAD_UPVALUE, slot, line);
		} else {
			builder.emitop(OP_GLOBAL_GET, line);
			builder.emitIndex(globalVarIndex(name));
		}
	}
	
	/**
	 * Generates code to store the stack-top operand to the given 
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
			builder.emitIndex(globalVarIndex(name));
		}
	}
	
	/**
	 * Caches the attribute of the given object to the local variable.
	 *
	 * @param attrName the name of the attribute.
	 * @param localVarName the name of the target local variable.
	 * @param objSlot the slot of the object.
	 *
	 * @return the slot of the given local variable.
	 */
	private int cacheAttrToLocal(String attrName, String localVarName, int objSlot) {
		builder.emitLoad(objSlot, -1);
		return cacheAttrToLocal(attrName, localVarName);
	}
	
	private int cacheAttrToLocal(String attrName, String localVarName) {
		int cachedVar = locals.addLocal(localVarName);
		builder.emitop(OP_GET_ATTR);
		builder.emitStringConstant(attrName);
		builder.emitopWithArg(OP_POP_STORE, cachedVar);
		return cachedVar;
	}
	
	/**
	 * Generates code to assign an expression to an attribute, an item
	 * or a variable.
	 *
	 * @param lhs the left-hand side of the assignment.
	 *
	 * @param operator assignment operator or it a binary operator if
	 *                 it is a compound assigment such as a += b.
	 *
	 * @param rhs the right-hand side of the assignment.
	 */ 
	private void writeAssignmentExpr(Expr lhs, TokenKind operator, Expr rhs) {
		boolean isCompoundAssignment = operator != TokenKind.ASSIGN;
		if (isCompoundAssignment) {
			loadLeftHandSide(lhs);
		}
		rhs.accept(this);
		if (isCompoundAssignment) {
			byte opcode = OperatorInstructionMap.lookupOperatorIns(operator);		
			builder.emitop(opcode, line(lhs));
		}	
		assignTo(lhs, isCompoundAssignment);
	}
	
	/**
	 * Load the left-hand side of the assignment to stack top.
	 */
	private void loadLeftHandSide(Expr lhs) {
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
	
	private void assignTo(Expr leftNode, boolean isCompoundAssignment) {
		if (leftNode instanceof Expr.Assign) {
			storeVariable(((Expr.Assign)leftNode).name, line(leftNode));
			return;
		}	
		if (leftNode instanceof Expr.SetAttr) {
			assignTo((Expr.SetAttr) leftNode, isCompoundAssignment);
			return;
		}
		if (leftNode instanceof Expr.SetItem) {
			assignTo((Expr.SetItem) leftNode, isCompoundAssignment);
			return;
		}
		throw new Error("Unknown Expr Type: " + leftNode.getClass().getName());
	}

	private void assignTo(Expr.SetAttr node, boolean isCompoundAssignment) {
		if (!isCompoundAssignment) {
			node.objExpr.accept(this);
		} else {
			// operand stack: obj, value
			// rotate ->      value, obj
			builder.emitop(OP_ROT_2);
		}
		builder.emitop(OP_SET_ATTR, line(node));
		builder.emitStringConstant(node.attr);
	}
	
	private void assignTo(Expr.SetItem node, boolean isCompoundAssignment) {
		if (!isCompoundAssignment) {
			node.key.accept(this);
			node.objExpr.accept(this);
		} else {
			// operand stack: key, obj, value
			// rotate ->      value, key, obj
			builder.emitop(OP_ROT_3);
		}
		builder.emitop(OP_SET_ITEM);
	}
	
	/**
	 * Generates code to load the speficied function to stack top.
	 */
	private void loadFunction(String name, Stmt.FuncDef node) {
		boolean prevIsInInit = isInInitializer;
		this.isInInitializer = false;
		MethodInfo methodInfo = new MethodInfo();
		builder.emitop(OP_FUN, line(node));
		builder.emitConstant(methodInfo);
		makeMethodInfo(methodInfo, name, node, null);
		this.isInInitializer = prevIsInInit;
	}
	
	private MethodInfo makeMethodInfo(Stmt.FuncDef node, ClassInfo classDefinedIn) {
		return makeMethodInfo(new MethodInfo(), node.name.get(), node, classDefinedIn);
	}
	
	private MethodInfo makeMethodInfo(MethodInfo methodInfo, String name, 
	                                  Stmt.FuncDef node, ClassInfo classDefinedIn) {	
		enterFunctionScope();
		int arity = node.parameters.size();
		int vaArgsIndex = node.parameters.vaArgsIndex;	
		locals.addLocals(node.parameters.params);
		if (classDefinedIn != null) {
			// declare 'this' as the last argument.
			methodInfo.classDefinedIn = classDefinedIn;
			declrVariable("this");
			arity ++;
		}	
		// locate the beginning of the method for debugger.
		if (isDebugMode && classDefinedIn != null) {
			builder.emitop(OP_NOP, node.pos.getLine());
		}
		walkBlock(node.body);
		methodInfo.name = name;	
		methodInfo.arity = arity;
		methodInfo.varArgsIndex = vaArgsIndex;
		methodInfo.upvalues = genUpvalueBytes(locals.upvalues());
		methodInfo.attrs = builder.buildCodeAttr(locals.maxSlotCount());
		closeFunctionScope();
		return methodInfo;
	}
	
	private byte[] genUpvalueBytes(LocalTable.Upvalue[] upvalues) {
		byte[] upvalueBytes = new byte[upvalues.length*2];
		for (int i = 0; i < upvalues.length; i ++) {
			upvalueBytes[i*2] = (byte) (upvalues[i].isLocal ? 1 : 0);
			upvalueBytes[i*2 + 1] = (byte) upvalues[i].slot;
		}
		return upvalueBytes;
	}
	
	private void enterFunctionScope() {
		LocalTable newLocals = new LocalTable();
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
		walkBlock(node.block);
	}

	@Override
	public Void visit(Stmt.Empty node) {
		return null;
	}
	
	@Override
	public Void visit(Stmt.ClassDef node) {
		ClassInfo classInfo = new ClassInfo();
		
		if (node.superClass.isPresent()) {
			Expr superClass = node.superClass.get();
			// push superclass object to the operand stack.
			superClass.accept(this);
			// If true, VM will fetch the superclass from 
			// the operand stack.
			classInfo.hasSuperClass = true;
		} else {
			// If the superclass is not present, the Object will be default
			// superclass.
		}
		
		// OP_CLASS will push a class created by the class-info
		// to stack top.
		builder.state().push(1);
		
		if (!node.isStaticClass) {
			declrVariable(node.name);
		}
		
		enterScope();
		classInfo.fromPC = builder.curCp();	
		emitClass(classInfo, line(node));	
		classInfo.className = node.name;
		classInfo.initializer = initalizer(node, classInfo);
		classInfo.methods = methods(node, classInfo);
		if (isDebugMode && node.endPos.isPresent()) {
			builder.emitop(OP_NOP, node.endPos.get().getLine());
		}
		closeScope(true);
		
		if (!node.isStaticClass) {
			defineVariable(node.name, -1);
		} else {
			// this.className = class
			loadVariable("this", -1);
			builder.emitop(OP_SET_ATTR);
			builder.emitStringConstant(node.name);
		}	
		emitStaticBlock(node);
		return null;
	}

	/**
	 * Generates code to store a class.
	 *
	 * Byte code layout (push a class to stack top):
	 *     1. OP_CLASS
	 *     2. Class Constant (constant index)
	 *     3. Super Class Slot (unsigned byte) 
	 */
	private void emitClass(ClassInfo classInfo, int line) {
		builder.emitop(OP_CLASS, line);
		builder.emitConstant(classInfo);
		builder.emit1((byte) declrVariable("super"));
	}
	
	/**
	 * Static Block:
	 *     static {...}
	 *
	 * We will store this class as a local variable called 'this'.
	 */
	private void emitStaticBlock(Stmt.ClassDef node) {
		if (!node.staticBlock.isPresent()) {
			return;
		}
		enterScope();
		loadClass(node);
		int thisSlot = declrVariable("this");		
		defineVariable("this", thisSlot);
		walkBlock(node.staticBlock.get());
		closeScope(true);
	}
	
	private void loadClass(Stmt.ClassDef node) {
		if (!node.isStaticClass) {
			loadVariable(node.name, -1);
		} else {
			// We are inside stack block.
			// load 'this.className'
			loadVariable("this", -1);
			builder.emitop(OP_GET_ATTR, -1);
			builder.emitStringConstant(node.name);
		}
	}

	private Optional<MethodInfo> initalizer(Stmt.ClassDef node, ClassInfo classDefinedIn) {
		if (!node.initializer.isPresent()) {
			return Optional.empty();
		} 
		this.isInInitializer = true;
		Stmt.FuncDef initalizer = node.initializer.get();
		MethodInfo methodInfo = makeMethodInfo(initalizer, classDefinedIn);
		this.isInInitializer = false;
		return Optional.of(methodInfo);
	}

	private MethodInfo[] methods(Stmt.ClassDef node, ClassInfo classDefinedIn) {
		MethodInfo[] methodInfos = new MethodInfo[node.methods.size()];
		for (int i = 0; i < methodInfos.length; i ++) {
			Stmt.FuncDef funcDef = node.methods.get(i);
			methodInfos[i] = makeMethodInfo(funcDef, classDefinedIn);
		}
		return methodInfos;
	}

	@Override
	public Void visit(Stmt.FuncDef node) {
		String nodeName = node.name.get();
		if (node.isStaticFunc) {
			// Static Function is:
			//     static { // in class
			//         fun @foo() {...}
			//     }
			// 
			// will be compiled into the 
			//     this.nodeName = functionValue
			loadFunction(nodeName, node);
			loadVariable("this", -1);
			builder.emitop(OP_SET_ATTR);
			builder.emitStringConstant(nodeName);
		} else {
			declrVariable(nodeName);
			loadFunction(nodeName, node);
			defineVariable(nodeName, -1);
		}
		return null;
	}

	@Override
	public Void visit(Stmt.ImportList node) {
		for (Stmt.Import i : node.importStmts) {
			i.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(Stmt.Import node) {
		node.fileExpr.accept(this);
		builder.emitop(OP_IMPORT, line(node));
		builder.emitIndex(globalVarIndex(node.asIdentifier));
		return null;
	}

	@Override
	public Void visit(Stmt.Block node) {
		enterScope();
		walkBlock(node);
		closeScope(true);
		return null;
	}
	
	private void enterLoop(LoopMarker loopMarker) {
		loopMarkers.push(loopMarker);
	}

	private void exitLoop() {
		loopMarkers.pop().concatsLableForBreak(builder);
	}
	
	@Override
	public Void visit(Stmt.While node) {
		int beginningPos = builder.curCp();
		enterLoop(new LoopMarker(beginningPos));
				
		// Optimize 'while (true)'
		if (node.condition.isConstant() && !node.condition.isFalsely()) {
			node.body.accept(this);
			builder.emitLoop(beginningPos, -1);
			exitLoop();
			return null;
		}
		
		node.condition.accept(this);
		int jumpOutLable = builder.emitLabel(OP_POP_JUMP_IF_FALSE, line(node));
		node.body.accept(this);
		builder.emitLoop(beginningPos, -1);
		builder.backpatch(jumpOutLable);
		
		exitLoop();
		return null;
	}

	@Override
	public Void visit(Stmt.For node) {
		// push iterable to stack top.
		node.iterable.accept(this);
		
		enterScope();
		int iteratorSlot = invokeIterator(line(node));
		int nextSlot = cacheAttrToLocal(Names.METHOD_ITERATOR_NEXT, "hidden$i_next");
		int hasNextSlot = cacheAttrToLocal(Names.METHOD_ITERATOR_HAS_NEXT, 
			"hidden$i_has_next", iteratorSlot);
		int iteratingSlot = locals.addLocal(node.iteratingVar);
		
		int begainning = builder.curCp();
		enterLoop(new LoopMarker(begainning));
		callLocalVar(hasNextSlot, 0, -1);
		int jumpOutLabel = builder.emitLabel(OP_POP_JUMP_IF_FALSE, -1);
		
		callLocalVar(nextSlot, 0, -1);
		builder.emitopWithArg(OP_POP_STORE, iteratingSlot);
		walkBlock(node.body);
		
		builder.emitLoop(begainning, -1);
		builder.backpatch(jumpOutLabel);
		exitLoop();
		
		closeScope(true);
		return null;
	}
	
	private int invokeIterator(int lineNumber) {
		int iteratorSlot = locals.addLocal("hidden$iterator");
		builder.emitInvoke(Names.METHOD_ITERATOR, 0, lineNumber);
		builder.emitopWithArg(OP_STORE, iteratorSlot, -1);
		return iteratorSlot;
	}
	
	@Override
	public Void visit(Stmt.Continue node) {
		LoopMarker loopMarker = loopMarkers.peek();
		builder.emitLoop(loopMarker.beginningPc, line(node));
		return null;
	}

	@Override
	public Void visit(Stmt.Break node) {
		LoopMarker loopMarker = loopMarkers.peek();
		loopMarker.addLableForBreak(
			builder.emitLabel(OP_JUMP, line(node))
		);
		return null;
	}

	@Override
	public Void visit(Stmt.If node) {
		node.condition.accept(this);
		int jumpOverThanLable = builder.emitLabel(
			OP_POP_JUMP_IF_FALSE, line(node));
		
		node.thenBody.accept(this);
		if (!node.elseBody.isPresent()) {
			builder.backpatch(jumpOverThanLable);
			return null;
		}
		
		int jumpOverElseLable = builder.emitLabel(OP_JUMP, -1);		
		builder.backpatch(jumpOverThanLable);			
		node.elseBody.get().accept(this);
		builder.backpatch(jumpOverElseLable);
		return null;
	}

	@Override
	public Void visit(Stmt.Assert node) {
		node.condition.accept(this);
		int lable = builder.emitLabel(OP_POP_JUMP_IF_TRUE, line(node));
		node.errorInfo.accept(this);
		builder.emitop(OP_ASSERT, line(node));
		builder.backpatch(lable);
		return null;
	}

	@Override
	public Void visit(Stmt.TryIntercept node) {
		int jOutOfInterceptions = genTryBlockCode(node.tryBlock);
		
		// interception-block lables 
		// jump out of interceptions and the else-block.
		int[] lablesJumpOutOfIAE = genInterceptionBlocks(node);
		
		builder.backpatch(jOutOfInterceptions);
		if (node.elseBlock.isPresent()) {
			node.elseBlock.get().accept(this);
		}
		for (int lable : lablesJumpOutOfIAE) {
			builder.backpatch(lable);
		}
		return null;
	}
	
	/**
	 * Generates an error handler to intercept all errors that occurs in
	 * Try-Block.
	 *
	 * @return Returns a lable used to jump out of the interception blocks.
	 */
	private int genTryBlockCode(Stmt.Block tryBlock) {
		int startPc = builder.curCp()+1;
		enterScope();
		walkBlock(tryBlock);
		ConstantValue.CloseIndexes close = closeScope(true);
		int endPc = builder.curCp();
		// lable used to jump over interception blocks if the code in try-block
		// can be executed successfully here.
		int jOutOfInterceptions = builder.emitLabel(OP_JUMP, -1);
		
		int handlerPc = builder.curCp();	
		builder.addErrorHandler(startPc, endPc, handlerPc);
		// VM will jump to here and close upvalues if an 
		// error occurs (means that upvalues do not be correctly closed).
		emitCloseInstruction(close);
		return jOutOfInterceptions;
	}

	/**
	 * Generates interception blocks.
	 *
	 * If an error ocurrs in the try-block, the operand-stack will be cleared
	 * and the intercepted error is pushed to the stack top.
	 */
	private int[] genInterceptionBlocks(Stmt.TryIntercept node) {
		int[] lablesJumpOutOfIAE;
		// Push Error, Change the state.
		builder.state().push(1);
		if (!node.interceptionBlocks.isEmpty()) {
			lablesJumpOutOfIAE = new int[node.interceptionBlocks.size()];
			int i = 0;		
			for (Stmt.Interception interception : node.interceptionBlocks) {
				interception.accept(this);
				lablesJumpOutOfIAE[i ++] = builder.emitLabel(OP_JUMP, -1);
			}
			// Reraise the error if fail to intercept.
			builder.emit1(OP_RAISE);
		} else {
			// If the list of the interception block is empty, any error is intercepted.
			// Pop Error
			builder.emitop(OP_POP);
			lablesJumpOutOfIAE = new int[]{ builder.emitLabel(OP_JUMP, -1) };
		}
		return lablesJumpOutOfIAE;
	}

	@Override
	public Void visit(Stmt.Interception node) {
		int JToErrorOrNextInterception = -1;
		enterScope();
		for (Expr exception : node.exceptions) {
			exception.accept(this);
		}
		JToErrorOrNextInterception = builder.emitLabel(OP_MATCH_ERRORS, -1);
		builder.emit1((byte) node.exceptions.size());
		builder.state().pop(node.exceptions.size());
		
		if (node.name.isPresent()) {
			String name = node.name.get();
			declrVariable(name);
			defineVariable(name, -1);
		} else {
			// Consume the error at the top of the operand stack.
			builder.emitop(OP_POP);
		}
		
		walkBlock(node.block);
		closeScope(true);
		// jump over lable used to jump out of interception blocks.
		// see genInterceptions(Stmt.TryIntercept)
		// 3 is the length of a lable.
		builder.backpatch(JToErrorOrNextInterception, 3);	
		return null;
	}

	@Override
	public Void visit(Stmt.Raise node) {
		node.exceptionExpr.accept(this);
		builder.emitop(OP_RAISE, line(node));
		return null;
	}

	@Override
	public Void visit(Stmt.ExprS node) {
		node.expr.accept(this);
		boolean print = 
			!(node.expr instanceof Expr.Assign) &&
			!(node.expr instanceof Expr.SetItem) &&
			!(node.expr instanceof Expr.SetAttr);
		if (isInteractionMode && locals.isInGlobal() && print) {
			builder.emitop(OP_PRINT, line(node));
		} else {
			builder.emitop(OP_POP, line(node));
		}
		return null;
	}

	@Override
	public Void visit(Stmt.Return node) {
		if (isInInitializer) {
			// VM requires to return the 'this' pointer from a initializer.
			loadVariable("this", line(node));
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
		if (!node.isStatic) {
			declrVariable(node.name);
			defineVariable(node.name, -1);
		} else {
			loadVariable("this", -1);
			builder.emitop(OP_SET_ATTR);
			builder.emitStringConstant(node.name);
		}
		return null;
	}

	@Override
	public Void visit(Expr.Assign node) {
		writeAssignmentExpr(node, node.assignOperator, node.rhs);
		return null;
	}

	@Override
	public Void visit(Expr.TernaryOperator node) {
		node.condition.accept(this);
		int lableJumpToElse = builder.emitLabel(OP_POP_JUMP_IF_FALSE, -1);
		node.thenExpr.accept(this);
		int lableJumpOutElse = builder.emitLabel(OP_JUMP, -1);
		builder.backpatch(lableJumpToElse);
		node.elseExpr.accept(this);
		builder.backpatch(lableJumpOutElse);
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
			default:
				throw new Error("Unknown unary operator: " + node.operator);
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
				bp = builder.emitLabel(OP_JUMP_IF_FALSE, opLine);
				break;
			case LOGICAL_OR:
				bp = builder.emitLabel(OP_JUMP_IF_TRUE, opLine);
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
		int bits = 1;
		int flags = 0;
		for (Expr.Argument arg : node.arguments) {
			if (arg.isUnpack) {
				flags |= bits;
			}
			bits <<= 1;
			arg.expr.accept(this);
		}
		
		final int ARGC = node.arguments.size();
		final int LINE = line(node);
		if (flags != 0) {
			node.expr.accept(this);
			builder.emitopWithArg(OP_CALL_EX, ARGC, LINE);
			builder.emitConstant(new ConstantValue.UnpackFlags(flags));	
		} else if(!emitCallInstruction(node.expr, ARGC, LINE)) {
			node.expr.accept(this);
			builder.emitopWithArg(OP_CALL, ARGC, LINE);
		}
		builder.state().pop(ARGC);
		return null;
	}

	private boolean emitCallInstruction(Expr expr, int ARGC, int LINE) {
		if (expr instanceof Expr.GetAttr) {
			invokeAttribute((Expr.GetAttr) expr, ARGC, LINE);
			return true;
		} 

		if (expr instanceof Expr.Super) {
			invokeSuper((Expr.Super) expr, ARGC, LINE);
			return true;
		}

		if (expr instanceof Expr.VarRef) {
			Expr.VarRef varRef = (Expr.VarRef) expr;		
			int slot = locals.resolveLocal(varRef.name);	
			if (slot != -1) {
				callLocalVar(slot, ARGC, LINE);
				return true;
			}

			if (locals.resolveUpvalue(varRef.name) == -1) {
				callGlobalVar(varRef.name, ARGC, LINE);
				return true;
			}
		}
		return false;
	}

	private void invokeAttribute(Expr.GetAttr node, int argc, int line) {
		node.objExpr.accept(this);
		builder.emitInvoke(node.attr, argc, line);
	}
	
	private void invokeSuper(Expr.Super superNode, int argc, int line) {
		loadVariable("this", line);
		loadVariable("super", -1);
		builder.emitopWithArg(OP_SUPER_INVOKE, argc, -1);
		builder.emitStringConstant(superNode.reference);
	}
	
	private void callLocalVar(int slot, int argc, int line) {
		builder.emitopWithArg(OP_CALL_SLOT, argc, line);
		builder.emit1((byte) slot);
	}
	
	private void callGlobalVar(String name, int argc, int line) {
		builder.emitopWithArg(OP_CALL_GLOBAL, argc, line);
		builder.emitIndex(globalVarIndex(name));
	}

	@Override
	public Void visit(Expr.SetAttr node) {
		writeAssignmentExpr(node, node.assignOperator, node.rhs);
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
		writeAssignmentExpr(node, node.assignOperator, node.rhs);
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
	public Void visit(Expr.Map node) {
		builder.emitop(OP_NEW_MAP, line(node));
		builder.emitIntegerConstant(node.keys.size());
		int size = node.keys.size();
		int p = 0;
		while (size > 0) {
			int c = Math.min(size, 250/2);
			p += c;
			for (int i = 1; i <= c; i ++) {
				node.values.get(p-i).accept(this);
				node.keys.get(p-i).accept(this);
			}
			size -= c;
			builder.state().pop(c*2);
			builder.emitopWithArg(OP_PUT, c);
		}
		return null;
	}

	@Override
	public Void visit(Expr.Tuple node) {
		final int SIZE = node.elements.size();
		if (SIZE > 255) {
			// Unreachable
			throw new Error("size: " + SIZE + " > 255");
		}
		for (int i = SIZE-1; i >= 0; i --) {
			node.elements.get(i).accept(this);
		}
		builder.state().pop(SIZE);
		builder.emitopWithArg(OP_BUILT_TUPLE, SIZE);
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

	@Override
	public Void visit(Stmt.ErrorStmt node) {
		throw new Error("Unexpected error statement.");
	}
}
