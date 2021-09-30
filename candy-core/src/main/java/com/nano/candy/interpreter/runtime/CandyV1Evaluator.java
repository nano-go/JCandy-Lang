package com.nano.candy.interpreter.runtime;

import com.nano.candy.code.Chunk;
import com.nano.candy.code.ConstantPool;
import com.nano.candy.code.ConstantValue;
import com.nano.candy.code.ErrorHandlerTable;
import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.ClassSignature;
import com.nano.candy.interpreter.builtin.ObjectClass;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.BoolObj;
import com.nano.candy.interpreter.builtin.type.CallableObj;
import com.nano.candy.interpreter.builtin.type.DoubleObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.MapObj;
import com.nano.candy.interpreter.builtin.type.ModuleObj;
import com.nano.candy.interpreter.builtin.type.NullPointer;
import com.nano.candy.interpreter.builtin.type.PrototypeFunction;
import com.nano.candy.interpreter.builtin.type.Range;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.TupleObj;
import com.nano.candy.interpreter.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.builtin.type.error.NameError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.ElementsUnpacker;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.runtime.FileEnvironment;
import com.nano.candy.interpreter.runtime.Frame;
import com.nano.candy.interpreter.runtime.FrameStack;
import com.nano.candy.interpreter.runtime.OperandStack;
import com.nano.candy.interpreter.runtime.Upvalue;
import com.nano.candy.interpreter.runtime.module.ModuleManager;
import com.nano.candy.sys.CandySystem;
import java.io.File;
import java.io.PrintStream;

import static com.nano.candy.code.OpCodes.*;

public class CandyV1Evaluator implements Evaluator {

	private static final byte WIDE_INDEX_MARK = (byte) 0xFF;
	private static final byte EMPTY_UNPACK_FLAGS = 0;
	
	private FrameStack stack;
	private Frame frame;
	
	private ConstantPool cp;
	private byte[] code;
	
	private OperandStack opStack;
	private int bp;

	private EvaluatorEnv env;
	
	protected CandyV1Evaluator(EvaluatorEnv env) {
		this.env = env;
		this.stack = new FrameStack(CandySystem.DEFAULT_MAX_STACK);
		this.opStack = new OperandStack(32);
	}
	
	private void pushFrame(Frame frame) {
		stack.pushFrame(frame);	
		opStack.push(frame.frameSize());
		syncFrameData();
		this.opStack.sp += frame.closure.localSizeWithoutArgs;
	}
	
	private Frame popFrame() {
		Frame old = stack.popFrame();
		opStack.pop(bp);
		old.closeAllUpvalues();
		syncFrameData();
		return old;
	}

	@Override
	public Frame[] getStack() {
		Frame[] frames = new Frame[stack.sp()];
		for (int i = 0; i < frames.length; i ++) {
			frames[i] = stack.getAt(stack.sp()-i-1);
		}
		return frames;
	}
	
	private final void returnFrame() {
		Frame old = stack.popFrame();	
		old.closeAllUpvalues();
		CandyObject retValue = opStack.peek(0);
		opStack.pop(bp + 1);
		store(0, retValue);
		syncFrameData();
	}

	private void syncFrameData() {
		this.frame = stack.peek();
		if (frame == null) {
			clearFrameData();
			return;
		}
		this.cp = frame.getChunk().getConstantPool();
		this.code = frame.getChunk().getByteCode();
		this.bp = frame.bp;
		env.curFileEnv = frame.getFileEnv();
	}

	private void clearFrameData() {
		this.cp = null;
		this.code = null;
		this.frame = null;
		this.bp = 0;
		env.curFileEnv = null;
	}
	
	/* -------------------- Execution Helper -------------------- */

	private final int readJumpOffset() {
		return (code[frame.pc] << 8) & 0xFFFF | code[frame.pc + 1] & 0xFF;
	}

	private final int readUint8() {
		return code[frame.pc ++] & 0xFF;
	}

	private final int readIndex(){
		if (code[frame.pc] != WIDE_INDEX_MARK) {
			return code[frame.pc ++] & 0xFF;
		}
		// wide index: 2 unsigned bytes.
		frame.pc ++;
		return (code[frame.pc ++] << 8) & 0xFFFF | code[frame.pc ++] & 0xFF;
	}

	private final CandyObject pop() {
		return opStack.pop();
	}

	private final CandyObject peek(int k) {
		return opStack.peek(k);
	}

	private final void push(CandyObject obj) {
		opStack.push(obj);
	}
	
	private final CandyObject load(int slot) {
		return opStack.operands[bp + slot];
	}

	private final void store(int slot, CandyObject value) {
		opStack.operands[bp + slot] = value;
	}

	private boolean tryToHandleError(ErrorObj err, boolean printError) {
		if (err.getStackTraceElements() == null) {
			err.setStackTraceElements(env.getStack());
		}
		ErrorHandlerTable. ErrorHandler handler = findExceptionHandler();
		if (handler != null) {
			handleError(err, handler);
			return true;
		}	
		if (printError) {
			PrintStream stderr = env.getOptions().getStderr();
			if (env.getCurrentThread().getId() != 1) {
				stderr.printf(
					"An error ocurrs in the thread '%s'.\n",
					env.getCurrentThread().getName()
				);
			}
			stderr.print(err.sprintStackTrace(24));
		}
		clearFrameData();
		return false;
	}

	private ErrorHandlerTable.ErrorHandler findExceptionHandler() {
		while (!stack.isEmpty()) {
			Frame frame = stack.peek();
			ErrorHandlerTable.ErrorHandler handler = null;
			ErrorHandlerTable table = frame.getErrorHandlerTable();
			if (table == null || 
				(handler = table.findExceptionHandler(frame.pc)) == null) {
				popFrame();
				continue;
			}
			return handler;
		}
		return null;
	}

	private void handleError(ErrorObj err, ErrorHandlerTable.ErrorHandler handler) {	
		opStack.clearOperands(bp + frame.getMaxLocal());
		frame.pc = handler.handlerPc;
		push(err);
	}

	/**
	 * Returns the super class by the specified class information.
	 *
	 * @return the non-null super class.
	 */
	private CandyClass getSuperClassOf(ConstantValue.ClassInfo classInfo) {
		if (classInfo.hasSuperClass) {
			CandyObject superObj = pop();
			if (!(superObj instanceof CandyClass)) {
				new TypeError("A class can't inherit a non-class: %s -> '%s'", 
					classInfo.className, superObj.getCandyClassName()
				).throwSelfNative();
			}
			CandyClass superClass = (CandyClass) superObj;
			if (!superClass.isInheritable()) {
				new TypeError("The '%s' is a non-inheritable class.", 
					superClass.getName()
				).throwSelfNative();
			}
			return (CandyClass) superObj;
		} 
		return ObjectClass.getObjClass();
	}

	private CandyClass createClass(CandyClass superClass, ConstantValue.ClassInfo classInfo) {
		ClassSignature signature = new ClassSignature(classInfo.className, superClass);
		if (classInfo.initializer.isPresent()) {
			ConstantValue.MethodInfo init = classInfo.initializer.get();
			signature.setInitializer(createFunctionObj(classInfo.className, init));
			frame.pc += init.getLength();
		}
		for (ConstantValue.MethodInfo methodInfo : classInfo.methods) {
			PrototypeFunction prototypefunc = 
				createFunctionObj(classInfo.className, methodInfo);
			signature.defineMethod(methodInfo.name, prototypefunc);
			frame.pc += methodInfo.getLength();
		}
		return signature.setPredefinedAttrs(classInfo.attrs)
			.setIsInheritable(true).build();
	}

	/**
	 * Creates a prototype function object.
	 *
	 * @param className The name of the class that the method is defined in or null.
	 * @param methodInfo The information of the prototype function.
	 */
	private PrototypeFunction createFunctionObj(String className, ConstantValue.MethodInfo methodInfo) {
		Upvalue[] upvalues = stack.peek().captureUpvalueObjs(opStack, methodInfo);
		String tagName = methodInfo.name;
		if (className != null) {
			tagName = ObjectHelper.methodName(className, tagName);
		}
		return new PrototypeFunction(
			stack.peek().getChunk(), frame.pc, // Start pc.
			upvalues, tagName, 
			methodInfo, env.curFileEnv
		);
	}

	/**
	 * Instruction: OP_APPEND
	 *
	 * Append {readUint8()} elements to the bottom array object.
	 */
	private void evalOpAppned() {
		int elements = readUint8();
		ArrayObj arr = (ArrayObj) peek(elements);
		for (int i = 0; i < elements; i ++) {
			arr.append(pop());
		}
	}

	/**
	 * Instruction: OP_PUT
	 *
	 * Append {readUint8()} KV(key and value) to the bottom map object.
	 */
	private void evalOpPut() {
		int numKVs = readUint8();
		MapObj map = (MapObj) peek(numKVs*2);
		for (int i = 0; i < numKVs; i ++) {
			CandyObject key = pop();
			CandyObject value = pop();
			map.put(env.cniEnv, key, value);
		}
	}

	/**
	 * Instruction: OP_BUILT_TUPLE
	 *
	 * Build a tuple to stack-top.
	 */
	private void evalOpBuiltTuple() {
		int size = readUint8();
		if (size == 0) {
			push(TupleObj.EMPTY_TUPLE);
			return;
		}
		CandyObject[] elements = new CandyObject[size];
		for (int i = 0; i < size; i ++) {
			elements[i] = pop();
		}
		push(new TupleObj(elements));
	}

	/**
	 * Instruction: OP_MATCH_ERRORS
	 */
	private void evalOpMatchErrors() {
		int offset = readJumpOffset();
		frame.pc += 2;
		int count = readUint8();
		CandyObject error = peek(count);
		boolean matched = count == 0;
		for (int i = 0; i < count; i ++) {
			CandyClass interceptedErrorType = checkIsErrorClass(pop());
			if (!matched && error.getCandyClass()
				.isSubClassOf(interceptedErrorType)) {
				matched = true;
			}
		}
		if (!matched) {
			frame.pc += offset-3;
		}
	}
	
	public static CandyClass checkIsErrorClass(CandyObject obj) {
		String name;
		if (obj instanceof CandyClass) {
			CandyClass errClass = (CandyClass)obj;
			if (errClass.isSubClassOf(ErrorObj.ERROR_CLASS)){
				return errClass;
			}
			name = errClass.getName();
		} else { 
			name = "instance of the " + obj.getCandyClassName();
		}
		new TypeError("The '%s' is not an error class. The 'intercept' statmenet" +
			" only accept classes that inherit from Error.", name)
			.throwSelfNative();
		return null;
	}

	private CandyObject getGlobalVariable(int index, boolean throwsErrorIfNotFound) {
		CandyObject obj = env.getVariableValue(index);
		if (obj == null && throwsErrorIfNotFound) {
			new NameError("the variable '%s' not found.", env.getVariableName(index))
				.throwSelfNative();
		}
		return obj;
	}

	private boolean setGlobalVariable(int index, CandyObject val, boolean throwsErrorIfNotFound) {
		if (!env.setVariableIfExists(index, val)) {
			if (throwsErrorIfNotFound) {
				new NameError("the variable '%s' not found.", env.getVariableName(index))
					.throwSelfNative();
			}
			return false;
		}
		return true;
	}

	@Override
	public void enterFunction(PrototypeFunction function) {
		pushFrame(Frame.fetchFrame(function, opStack));
	}
	
	@Override
	public CandyObject eval(CallableObj fn, int unpackFlags, CandyObject... args) {
		if (args != null) {
			opStack.pushArguments(args);
		}
		call(fn, args == null ? 0 : args.length, unpackFlags);
		if (!fn.isBuiltin()) {
			evalCurrentFrame(true);
		}
		return pop();
	}
	
	@Override
	public void call(CallableObj fn, int argc, int unpackFlags) {
		argc = checkArgument(fn, argc, unpackFlags);
		fn.onCall(env.cniEnv, opStack, argc, unpackFlags);
	}
	
	private final int checkArgument(CallableObj fn, int argc, int unpackFlags) {
		if (unpackFlags == EMPTY_UNPACK_FLAGS && fn.vaargIndex() < 0 &&
		    fn.optionalArgFlags() == 0) {
			if (fn.arity() != argc) {
				ArgumentError.throwsArgumentError(fn, argc);
			}
		} else {
			CandyObject[] args = unpack(fn, argc, unpackFlags);
			opStack.pushArguments(args);
			argc = args.length;
		}
		return argc;
	}

	private final CandyObject[] unpack(CallableObj fn, int argc, int unpackFlags) {
		opStack.reverse(argc);
		CandyObject[] args = ElementsUnpacker.unpackFromStack
			(env.cniEnv, opStack, argc, 
				fn.vaargIndex(), fn.arity(), 
				unpackFlags, fn.optionalArgFlags());
		if (args == null) {
			ArgumentError.throwsArgumentError(fn, argc);
		}
		return args;
	}
	
	@Override
	public ModuleObj eval(Chunk chunk) {
		File f = new File(chunk.getSourceFileName());
		if (f.isFile()) {
			return eval(new CompiledFileInfo(f.getPath(), chunk, true));
		} else {
			return eval(new CompiledFileInfo(
				CandySystem.DEFAULT_USER_DIR, chunk, false
			));
		}
	}
	
	@Override
	public ModuleObj eval(CompiledFileInfo file) {
		env.curFileEnv = new FileEnvironment(file);
		PrototypeFunction topFunction = new PrototypeFunction(
			file.getChunk(), env.curFileEnv
		);
		// OP_EXIT will return the eval method.
		eval(Frame.fetchFrame(topFunction, opStack), false);
		ModuleObj moudleObj = 
			env.curFileEnv.generateModuleObject();
		popFrame();
		return moudleObj;
	}

	@Override
	public void eval(Frame frame, boolean exitJavaMethodAtReturn) {
		pushFrame(frame);
		evalCurrentFrame(exitJavaMethodAtReturn);
	}

	private void evalCurrentFrame(boolean exitJavaMethodAtReturn) {
		if (exitJavaMethodAtReturn) {
			stack.peek().exitRunAtReturn = true;
		}
		stack.markDepth();
		try {
			evalCoreCatchExceptions();
		} finally {
			stack.unmarkDepth();
		}
	}
	
	/**
	 * Only allowed to be called by {@code evalCurrentFrame} or, the 
	 * current stack depth is marked before it's called.
	 */
	private void evalCoreCatchExceptions() {
		while (true) {
			try {
				evalCore();
				break;
			} catch (VMExitException e) {
				throw e;
			} catch (ContinueRunException e) {
				if (stack.sp() >= stack.getCurrentDepth()) {
					continue;
				}
				throw e;
			} catch (Throwable e) {
				if (!tryToHandleError(ErrorObj.asErrorObj(e), true)) {
					// unable to catch the thrown error and throws 
					// the exception to tell the interpreter to end the 
					// current thread.
					throw new VMExitException(70);
				}
				if (stack.sp() >= stack.getCurrentDepth()) {
					continue;
				}
				throw new ContinueRunException();
			}		
		}
	}
	
	private void evalCore() {
		OperandStack opStack = this.opStack;
		Frame frame = this.frame;
		byte[] code = this.code;
		loop: for (;;) {
			switch (code[frame.pc ++]) {
				case OP_NOP: break;
				case OP_POP: {
					opStack.pop();
					break;
				}
				case OP_DUP: {
					opStack.push(opStack.peek(0));
					break;
				}
				case OP_DUP_2: {
					CandyObject i = opStack.peek(1);
					CandyObject j = opStack.peek(0);
					opStack.push(i);
					opStack.push(j);
					break;
				}
				case OP_ROT_2: {
					opStack.swap();
					break;
				}
				case OP_ROT_3: {
					opStack.rotThree();
					break;
				}

				/**
				 * Label Instructions.
				 */
				case OP_POP_JUMP_IF_FALSE: {
					frame.pc += !opStack.pop().boolValue(env.cniEnv).value() ?
						readJumpOffset() : 2;
					break;
				}	
				case OP_POP_JUMP_IF_TRUE: {
					frame.pc += opStack.pop().boolValue(env.cniEnv).value() ?
						readJumpOffset() : 2;
					break;
				}
				case OP_POP_JUMP_IF_NOT_UNDEFINED: {
					frame.pc += opStack.pop() != NullPointer.undefined() ?
						readJumpOffset() : 2;
					break;
				}
				case OP_JUMP_IF_FALSE: {
					frame.pc += !opStack.peek(0).boolValue(env.cniEnv).value() ?
						readJumpOffset() : 2;
					break;
				}		
				case OP_JUMP_IF_TRUE: {
					frame.pc += opStack.peek(0).boolValue(env.cniEnv).value() ?
						readJumpOffset() : 2;
					break;
				}		
				case OP_JUMP: {
					frame.pc += readJumpOffset();
					break;
				}
				case OP_LOOP: {
					frame.pc -= readJumpOffset();
					break;
				}		

				/**
				 * Unary Operations.
				 */
				case OP_NEGATIVE: {
					opStack.push(opStack.pop().callNegative(env.cniEnv));
					break;
				}
				case OP_POSITIVE: {
					opStack.push(opStack.pop().callPositive(env.cniEnv));
					break;
				}
				case OP_NOT: {
					opStack.push(opStack.pop().not(env.cniEnv));
					break;
				}

				/**
				 * Alrithmetical Operations.
				 */		
				case OP_ADD: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callAdd(env.cniEnv, val2));
					break;
				}
				case OP_SUB: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callSub(env.cniEnv, val2));
					break;
				}				
				case OP_MUL: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callMul(env.cniEnv, val2));
					break;
				}				
				case OP_DIV: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callDiv(env.cniEnv, val2));
					break;
				}
				case OP_MOD: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callMod(env.cniEnv, val2));
					break;
				}

				/**
				 * Relation Operations.
				 */
				case OP_INSTANCE_OF: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(BoolObj.valueOf(
						val1.isInstanceOf(val2)
					));
					break;
				}	
				case OP_GT: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callGt(env.cniEnv, val2));
					break;
				}
				case OP_GTEQ: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callGteq(env.cniEnv, val2));
					break;
				}
				case OP_LT: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callLt(env.cniEnv, val2));
					break;
				}
				case OP_LTEQ: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callLteq(env.cniEnv, val2));
					break;
				}
				case OP_EQ: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callEquals(env.cniEnv, val2));
					break;
				}
				case OP_NOTEQ: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callEquals(env.cniEnv, val2).not(env.cniEnv));
					break;
				}
				
				case OP_LS: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callLShift(env.cniEnv, val2));
					break;
				}
				
				case OP_RS: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(val1.callRShift(env.cniEnv, val2));
					break;
				}
				
				/**
				 * Other Operator
				 */
				case OP_RANGE: {
					CandyObject val2 = opStack.pop();
					CandyObject val1 = opStack.pop();
					opStack.push(new Range(
						ObjectHelper.asInteger(val1), 
						ObjectHelper.asInteger(val2)
					));
					break;
				}

				/**
				 * Constants.
				 */
				case OP_NULL: {
					opStack.push(NullPointer.nil()); 
					break;
				}		
				case OP_DCONST: {
					opStack.push(DoubleObj.valueOf(cp.getDouble(readIndex())));
					break;
				}		
				case OP_ICONST: {
					opStack.push(IntegerObj.valueOf(cp.getInteger(readIndex())));
					break;
				}		
				case OP_SCONST: {
					opStack.push(StringObj.valueOf(cp.getString(readIndex())));
					break;
				}
				case OP_FALSE: {
					opStack.push(BoolObj.FALSE);
					break;
				}			
				case OP_TRUE: {
					opStack.push(BoolObj.TRUE);
					break;
				}

				/**
				 * Array.
				 */
				case OP_NEW_ARRAY: {
					opStack.push(new ArrayObj(cp.getInteger(readIndex())));
					break;
				}
				case OP_APPEND: {
					evalOpAppned();
					break;
				}

				/**
				 * Map
				 */
				case OP_NEW_MAP: {
					opStack.push(new MapObj(cp.getInteger(readIndex())));
					break;
				}
				case OP_PUT: {
					evalOpPut();
					break;
				}

				/**
				 * Tuple
				 */
				case OP_BUILT_TUPLE: {
					evalOpBuiltTuple();
					break;
				}

				/**
				 * Global Operarions.
				 */
				case OP_GLOBAL_DEFINE: {
					env.setVariable(
						readIndex(),               /* name index */
						opStack.pop()              /* value */
					);
					break;
				}		
				case OP_GLOBAL_SET: {
					setGlobalVariable(
						readIndex(),               /* name index */
						opStack.peek(0),           /* value */
						true
					);					
					break;
				}		
				case OP_GLOBAL_GET: {
					opStack.push(getGlobalVariable(readIndex(), true));
					break;
				}

				/**
				 * Local Operations.
				 */
				case OP_LOAD:
					opStack.push(opStack.operands[bp + readUint8()]);
					break;
				case OP_LOAD0:
					opStack.push(opStack.operands[bp + 0]);
					break;
				case OP_LOAD1:
					opStack.push(opStack.operands[bp + 1]);
					break;
				case OP_LOAD2:
					opStack.push(opStack.operands[bp + 2]);
					break;
				case OP_LOAD3:
					opStack.push(opStack.operands[bp + 3]);
					break;
				case OP_LOAD4: 
					opStack.push(opStack.operands[bp + 4]);
					break;

				case OP_STORE:
					opStack.operands[bp + readUint8()] = opStack.peek(0);
					break;
				case OP_STORE0: 
					opStack.operands[bp + 0] = opStack.peek(0);
					break;
				case OP_STORE1: 
					opStack.operands[bp + 1] = opStack.peek(0);
					break;
				case OP_STORE2: 
					opStack.operands[bp + 2] = opStack.peek(0);
					break;
				case OP_STORE3:
					opStack.operands[bp + 3] = opStack.peek(0);
					break;
				case OP_STORE4:
					opStack.operands[bp + 4] = opStack.peek(0);
					break;	

				case OP_POP_STORE: {
					opStack.operands[bp + readUint8()] = opStack.pop();
					break;
				}

				/**
				 * Close Upvalues
				 */
				case OP_CLOSE: {
					ConstantValue.CloseIndexes close =
						(ConstantValue.CloseIndexes) cp.getConstants()[readIndex()];
					frame.closeUpvalues(close);
					break;
				}		

				/**
				 * Upvalues.
				 */
				case OP_LOAD_UPVALUE: {
					opStack.push(frame.closure.upvalues[readUint8()].load());
					break;
				}				
				case OP_STORE_UPVALUE: {
					frame.closure.upvalues[readUint8()].store(opStack.peek(0));
					break;
				}


				/**
				 * Object Operations.
				 */
				case OP_GET_ATTR: {
					opStack.push(opStack.pop().callGetAttr(env.cniEnv, cp.getString(readIndex())));
					break;
				}		
				case OP_SET_ATTR: {
					CandyObject obj = opStack.pop();
					CandyObject value = opStack.pop();
					opStack.push(obj.callSetAttr(env.cniEnv, cp.getString(readIndex()), value));
					break;
				}
				case OP_GET_ITEM: {
					opStack.push(opStack.pop().callGetItem(env.cniEnv, opStack.pop()));
					break;
				}
				case OP_SET_ITEM: {
					CandyObject obj = opStack.pop();
					CandyObject key = opStack.pop();
					CandyObject value = opStack.pop();
					opStack.push(obj.callSetItem(env.cniEnv, key, value));
					break;
				}

				/**
				 * Function Definition.
				 */
				case OP_FUN: {
					ConstantValue.MethodInfo methodInfo = cp.getMethodInfo(readIndex());
					opStack.push(createFunctionObj(null, methodInfo));
					frame.pc += methodInfo.getLength();
					break;
				}

				/**
				 * Class Definition.
				 */
				case OP_CLASS: {
					ConstantValue.ClassInfo classInfo = cp.getClassInfo(readIndex());
					CandyClass superClass = getSuperClassOf(classInfo);
					store(readUint8(), superClass);
					CandyClass clazz = createClass(superClass, classInfo);
					opStack.push(clazz);
					break;
				}

				case OP_SUPER_GET: {
					CandyClass superClass = (CandyClass) opStack.pop();
					String name = cp.getString(readIndex());
					CandyObject superMethod = superClass.getBoundMethod(name, opStack.pop());
					if (superMethod == null) {
						new AttributeError(
							"The attribute '%s' is not found in the super class '%s'.",
							name, superClass.getName()
						).throwSelfNative();
					}
					break;
				}

				/**
				 * Call Operations.
				 */
				case OP_SUPER_INVOKE: {
					int argc = readUint8();
					CandyClass superClass = (CandyClass) opStack.pop();
					CandyObject instance = opStack.pop();
					String name = cp.getString(readIndex());
					CallableObj method= superClass.getBoundMethod(name, instance);
					if (method == null) {
						new AttributeError(
							"The attribute '%s' is not found in the super class '%s'.",
							name, superClass.getName()
						).throwSelfNative();
					}
					call(method, argc, EMPTY_UNPACK_FLAGS);
					frame = this.frame;
					code = this.code;
					break;
				}
				case OP_INVOKE: {
					int arity = readUint8();
					String attr = cp.getString(readIndex());
					CandyObject method = opStack.pop().callGetAttr(env.cniEnv, attr);
					call(TypeError.requiresCallable(method),
						arity, EMPTY_UNPACK_FLAGS);
					frame = this.frame;
					code = this.code;
					break;
				}
				case OP_CALL_SLOT: {
					int arity = readUint8();
					call(TypeError.requiresCallable(load(readUint8())),
						arity, EMPTY_UNPACK_FLAGS);
					frame = this.frame;
					code = this.code;
					break;
				}
				case OP_CALL_GLOBAL: {
					int arity = readUint8();
					call(TypeError.requiresCallable(
						getGlobalVariable(readIndex(), true)
					), arity, EMPTY_UNPACK_FLAGS);
					frame = this.frame;
					code = this.code;
					break;
				}
				case OP_CALL: {
					int arity = readUint8();
					call(TypeError.requiresCallable(opStack.pop()), 
						arity, EMPTY_UNPACK_FLAGS);
					frame = this.frame;
					code = this.code;
					break;
				}
				case OP_CALL_EX: {
					int arity = readUint8();
					int unpackFlags = cp.getUnpackFlags(readIndex());
					call(TypeError.requiresCallable(opStack.pop()), 
						arity, unpackFlags);
					frame = this.frame;
					code = this.code;
					break;
				}

				/**
				 * Error Handler.
				 */
				case OP_RAISE: {
					CandyObject err = opStack.pop();
					TypeError.checkTypeMatched(ErrorObj.ERROR_CLASS, err);
					((ErrorObj) err).throwSelfNative();				
					break;
				}
				case OP_MATCH_ERRORS: {
					evalOpMatchErrors();
					break;
				}	

				/**
				 * Other.
				 */
				case OP_IMPORT_NAME: {
					ModuleObj moudleObj = ModuleManager.getManager().importModule(
						env.cniEnv, ObjectHelper.asString(opStack.pop())
					);
					env.setVariable(readIndex(), moudleObj);
					break;
				}
				
				case OP_IMPORT: {
					ModuleManager.getManager().importModule(
						env.cniEnv, ObjectHelper.asString(opStack.pop())
					).addToEnv(env.getCurrentFileEnv());
					break;
				}
				
				case OP_ASSERT: {
					new com.nano.candy.interpreter.builtin.type.error.
						AssertionError(opStack.pop().callStr(env.cniEnv).value()).throwSelfNative();
					break;
				}
				case OP_PRINT: {
					CandyObject obj = opStack.pop();
					if (obj == NullPointer.nil()) {
						break;
					}
					env.getOptions().getStdout().println(
						obj.callStr(env.cniEnv).value());
					break;
				}
				case OP_RETURN: {
					if (stack.peek().exitRunAtReturn) {
						returnFrame();
						break loop;
					}
					returnFrame();
					frame = this.frame;
					code = this.code;
					break;
				}
				case OP_RETURN_NIL: {
					opStack.push(NullPointer.nil());
					if (stack.peek().exitRunAtReturn) {
						returnFrame();
						break loop;
					}
					returnFrame(); 
					frame = this.frame;
					code = this.code;
					break;
				}
				case OP_EXIT: {
					break loop;
				}
			}
		}
	}
}
