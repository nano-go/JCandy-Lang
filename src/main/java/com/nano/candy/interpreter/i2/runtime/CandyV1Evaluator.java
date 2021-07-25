package com.nano.candy.interpreter.i2.runtime;
import com.nano.candy.interpreter.i2.builtin.CandyClass;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.ClassSignature;
import com.nano.candy.interpreter.i2.builtin.ObjectClass;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.MapObj;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.PrototypeFunction;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.TupleObj;
import com.nano.candy.interpreter.i2.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.i2.builtin.type.error.NameError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ElementsUnpacker;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.runtime.FileEnvironment;
import com.nano.candy.interpreter.i2.runtime.Frame;
import com.nano.candy.interpreter.i2.runtime.OperandStack;
import com.nano.candy.interpreter.i2.runtime.StackFrame;
import com.nano.candy.interpreter.i2.runtime.Upvalue;
import com.nano.candy.interpreter.i2.runtime.chunk.Chunk;
import com.nano.candy.interpreter.i2.runtime.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.runtime.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.runtime.chunk.attrs.ErrorHandlerTable;
import com.nano.candy.interpreter.i2.runtime.module.ModuleManager;
import com.nano.candy.interpreter.i2.runtime.module.SourceFileInfo;
import com.nano.candy.sys.CandySystem;
import java.io.File;

import static com.nano.candy.interpreter.i2.code.OpCodes.*;

public class CandyV1Evaluator implements Evaluator {

	private static final byte WIDE_INDEX_MARK = (byte) 0xFF;
	private static final byte EMPTY_UNPACK_FLAGS = 0;
	
	private StackFrame stack;
	private Frame frame;
	private ConstantPool cp;
	private byte[] code;
	private CandyObject[] slots;
	private OperandStack opStack;

	private EvaluatorEnv env;
	
	/**
	 * A function returns a value by the operand stack, but when a function
	 * is a top frame and the function has been executed, the operand stack
	 * is null.
	 *
	 * So we need a variable to store the return value of this function if 
	 * the return value is required.
	 */
	private CandyObject retValue;
	
	protected CandyV1Evaluator(EvaluatorEnv env) {
		this.env = env;
		this.stack = env.thread.stack;
	}
	
	private void pushFrame(Frame frame) {
		stack.pushFrame(frame);
		syncFrameData();
	}
	
	private final void popFrameWithRet() {
		Frame old = popFrame();
		if (frame != null) {
			// push return value to operand stack.
			push(old.opStack.pop());
		} else {
			this.retValue = old.opStack.pop();
		}
	}

	private final Frame popFrame() {
		Frame old = stack.popFrame();
		if (old.isSourceFileFrame()) {
			SourceFileInfo.unmarkRunning(old.chunk.getSourceFileName());
		}
		syncFrameData();
		return old;
	}

	private void syncFrameData() {
		this.frame = stack.peek();
		if (frame == null) {
			resetFrameData();
			return;
		}
		this.cp = frame.chunk.getConstantPool();
		this.slots = frame.slots;
		this.code = frame.chunk.getByteCode();
		this.opStack = frame.opStack;
		env.globalEnv.setCurrentFileEnv(frame.fileEnv);
	}

	private void resetFrameData() {
		this.cp = null;
		this.slots = null;
		this.code = null;
		this.opStack = null;
		this.frame = null;
		env.globalEnv.setCurrentFileEnv((FileEnvironment) null);
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
	
	/**
	 * Push arguments into the operand stack in the right-to-left order.
	 */
	private final void pushArguments(CandyObject... args) {
		for (int i = args.length-1; i >= 0; i --) {
			if (args[i] == null) {
				opStack.push(NullPointer.nil());
			} else {
				opStack.push(args[i]);
			}
		}
	}

	private final CandyObject load(int slot) {
		return slots[slot];
	}

	private final void store(int slot, CandyObject value) {
		slots[slot] = value;
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
			if (env.getCurrentThread().getId() != 1) {
				System.err.printf(
					"An error ocurrs in the thread '%s'.\n",
					env.getCurrentThread().getName()
				);
			}
			System.err.print(err.sprintStackTrace(24));
		}
		resetFrameData();
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
		opStack.clear();
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
					superClass.getCandyClassName()
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
		return signature.setIsInheritable(true).build();
	}

	/**
	 * Creates a prototype function object.
	 *
	 * @param className The name of the class that the method is defined in or null.
	 * @param methodInfo The information of the prototype function.
	 */
	private PrototypeFunction createFunctionObj(String className, ConstantValue.MethodInfo methodInfo) {
		Upvalue[] upvalues = stack.peek().captureUpvalueObjs(methodInfo);
		String tagName = methodInfo.name;
		if (className != null) {
			tagName = ObjectHelper.methodName(className, tagName);
		}
		return new PrototypeFunction(
			stack.peek().chunk, frame.pc, // Start pc.
			upvalues, tagName, 
			methodInfo, env.globalEnv.getCurrentFileEnv()
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

	private CandyObject getGlobalVariable(String name, boolean throwsErrorIfNotFound) {
		CandyObject obj = env.globalEnv.getVariableValue(name);
		if (obj == null && throwsErrorIfNotFound) {
			new NameError("the variable '%s' not found.", name)
				.throwSelfNative();
		}
		return obj;
	}

	private boolean setGlobalVariable(String name, CandyObject val, boolean throwsErrorIfNotFound) {
		if (env.globalEnv.getVariableValue(name) == null) {
			if (throwsErrorIfNotFound) {
				new NameError("the variable '%s' not found.", name)
					.throwSelfNative();
			}
			return false;
		}
		env.globalEnv.setVariable(name, val);
		return true;
	}
	
	@Override
	public CandyObject eval(CallableObj fn, int unpackFlags, CandyObject[] args) {
		if (args != null) {
			pushArguments(args);
		}
		call(fn, args == null ? 0 : args.length, unpackFlags);
		if (!fn.isBuiltin()) {
			evalCurrentFrame(true);
		}
		return frame != null ? pop() : retValue;
	}
	
	@Override
	public void call(CallableObj fn, int argc, int unpackFlags) {
		if (unpackFlags == EMPTY_UNPACK_FLAGS && fn.varArgsIndex() < 0) {
			if (fn.arity() != argc) {
				ArgumentError.throwsArgumentError(fn, argc);
			}
			callFn(fn, argc, unpackFlags);
		} else {
			CandyObject[] args = unpack(fn, argc, unpackFlags);
			pushArguments(args);
			callFn(fn, args.length, unpackFlags);
		}
		if (!fn.isBuiltin()) {
			syncFrameData();
		}
	}

	private final CandyObject[] unpack(CallableObj fn, int argc, int unpackFlags) {
		CandyObject[] args = ElementsUnpacker.unpackFromStack
			(env.cniEnv, opStack, argc, fn.varArgsIndex(), fn.arity(), unpackFlags);
		if (args == null) {
			ArgumentError.throwsArgumentError(fn, argc);
		}
		return args;
	}

	private final void callFn(CallableObj fn, int argc, int unpackFlags) {
		fn.onCall(env.cniEnv, opStack, stack, argc, unpackFlags);
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
		env.globalEnv.setCurrentFileEnv(file);
		SourceFileInfo srcFileInfo = env.getCurSourceFileInfo();
		if (srcFileInfo != null) {
			srcFileInfo.markRunning(env.thread);
		}
		eval(Frame.fetchFrame(
			file.getChunk(), env.globalEnv.getCurrentFileEnv()
		), false);
		ModuleObj moudleObj = 
			env.globalEnv.getCurrentFileEnv().generateModuleObject();
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
			stack.peek().exitJavaMethodAtReturn = true;
		}
		int deepth = stack.sp();
		while (true) {
			try {
				evalCore();
				break;
			} catch (VMExitException e) {
				throw e;
			} catch (ContinueRunException e) {
				if (stack.sp() >= deepth) {
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
				if (stack.sp() < deepth) {
					throw new ContinueRunException();
				}
				continue;
			}		
		}
	}
	
	private void evalCore() {
		loop: for (;;) {
			/*if (tracerManager != null)
				tracerManager.notifyInsStarted(this, pc);*/
			switch (code[frame.pc ++]) {
				case OP_NOP: break;
				case OP_POP: {
					pop();
					break;
				}
				case OP_DUP: {
					push(peek(0));
					break;
				}
				case OP_DUP_2: {
					CandyObject i = peek(1);
					CandyObject j = peek(0);
					push(i);
					push(j);
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
					frame.pc += !pop().boolValue(env.cniEnv).value() ?
						readJumpOffset() : 2;
					break;
				}	
				case OP_POP_JUMP_IF_TRUE: {
					frame.pc += pop().boolValue(env.cniEnv).value() ?
						readJumpOffset() : 2;
					break;
				}		
				case OP_JUMP_IF_FALSE: {
					frame.pc += !peek(0).boolValue(env.cniEnv).value() ?
						readJumpOffset() : 2;
					break;
				}		
				case OP_JUMP_IF_TRUE: {
					frame.pc += peek(0).boolValue(env.cniEnv).value() ?
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
					push(pop().callNegative(env.cniEnv));
					break;
				}
				case OP_POSITIVE: {
					push(pop().callPositive(env.cniEnv));
					break;
				}
				case OP_NOT: {
					push(pop().not(env.cniEnv));
					break;
				}

				/**
				 * Alrithmetical Operations.
				 */		
				case OP_ADD: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callAdd(env.cniEnv, val2));
					break;
				}
				case OP_SUB: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callSub(env.cniEnv, val2));
					break;
				}				
				case OP_MUL: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callMul(env.cniEnv, val2));
					break;
				}				
				case OP_DIV: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callDiv(env.cniEnv, val2));
					break;
				}
				case OP_MOD: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callMod(env.cniEnv, val2));
					break;
				}

				/**
				 * Relation Operations.
				 */
				case OP_INSTANCE_OF: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(BoolObj.valueOf(
						val1.isInstanceOf(val2)
					));
					break;
				}	
				case OP_GT: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callGt(env.cniEnv, val2));
					break;
				}
				case OP_GTEQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callGteq(env.cniEnv, val2));
					break;
				}
				case OP_LT: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callLt(env.cniEnv, val2));
					break;
				}
				case OP_LTEQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callLteq(env.cniEnv, val2));
					break;
				}
				case OP_EQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callEquals(env.cniEnv, val2));
					break;
				}
				case OP_NOTEQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callEquals(env.cniEnv, val2).not(env.cniEnv));
					break;
				}

				/**
				 * Constants.
				 */
				case OP_NULL: {
					push(NullPointer.nil()); 
					break;
				}		
				case OP_DCONST: {
					push(DoubleObj.valueOf(cp.getDouble(readIndex())));
					break;
				}		
				case OP_ICONST: {
					push(IntegerObj.valueOf(cp.getInteger(readIndex())));
					break;
				}		
				case OP_SCONST: {
					push(StringObj.valueOf(cp.getString(readIndex())));
					break;
				}
				case OP_FALSE: {
					push(BoolObj.FALSE);
					break;
				}			
				case OP_TRUE: {
					push(BoolObj.TRUE);
					break;
				}

				/**
				 * Array.
				 */
				case OP_NEW_ARRAY: {
					push(new ArrayObj(cp.getInteger(readIndex())));
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
					push(new MapObj(cp.getInteger(readIndex())));
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
					env.globalEnv.setVariable(
						cp.getString(readIndex()), /* name */
						pop()                      /* value */
					);
					break;
				}		
				case OP_GLOBAL_SET: {
					setGlobalVariable(
						cp.getString(readIndex()), /* name */
						peek(0),                   /* value */
						true
					);					
					break;
				}		
				case OP_GLOBAL_GET: {
					push(getGlobalVariable(
						cp.getString(readIndex()), true));
					break;
				}

				/**
				 * Local Operations.
				 */
				case OP_LOAD:
					push(load(readUint8()));
					break;
				case OP_LOAD0:
				case OP_LOAD1:
				case OP_LOAD2:
				case OP_LOAD3:
				case OP_LOAD4: 
					push(load(code[frame.pc-1]-OP_LOAD0));
					break;

				case OP_STORE:
					store(readUint8(), peek(0));
					break;
				case OP_STORE0: 
				case OP_STORE1: 
				case OP_STORE2: 
				case OP_STORE3: 
				case OP_STORE4:
					store(code[frame.pc-1]-OP_STORE0, peek(0));
					break;	

				case OP_POP_STORE: {
					store(readUint8(), pop());
					break;
				}

				/**
				 * Close Upvalues
				 */
				case OP_CLOSE: {
					ConstantValue.CloseIndexes close =
						(ConstantValue.CloseIndexes) cp.getConstants()[readIndex()];
					stack.peek().closeUpvalues(close);
					break;
				}		

				/**
				 * Upvalues.
				 */
				case OP_LOAD_UPVALUE: {
					push(stack.peek().capturedUpvalues[readUint8()].load());
					break;
				}				
				case OP_STORE_UPVALUE: {
					stack.peek().capturedUpvalues[readUint8()].store(peek(0));
					break;
				}


				/**
				 * Object Operations.
				 */
				case OP_GET_ATTR: {
					push(pop().callGetAttr(env.cniEnv, cp.getString(readIndex())));
					break;
				}		
				case OP_SET_ATTR: {
					CandyObject obj = pop();
					CandyObject value = pop();
					push(obj.callSetAttr(env.cniEnv, cp.getString(readIndex()), value));
					break;
				}
				case OP_GET_ITEM: {
					push(pop().callGetItem(env.cniEnv, pop()));
					break;
				}
				case OP_SET_ITEM: {
					CandyObject obj = pop();
					CandyObject key = pop();
					CandyObject value = pop();
					push(obj.callSetItem(env.cniEnv, key, value));
					break;
				}

				/**
				 * Function Definition.
				 */
				case OP_FUN: {
					ConstantValue.MethodInfo methodInfo = cp.getMethodInfo(readIndex());
					push(createFunctionObj(null, methodInfo));
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
					push(clazz);
					break;
				}

				case OP_SUPER_GET: {
					CandyClass superClass = (CandyClass) pop();
					String name = cp.getString(readIndex());
					CandyObject superMethod = superClass.getBoundMethod(name, pop());
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
					CandyClass superClass = (CandyClass) pop();
					CandyObject instance = pop();
					String name = cp.getString(readIndex());
					CallableObj method= superClass.getBoundMethod(name, instance);
					if (method == null) {
						new AttributeError(
							"The attribute '%s' is not found in the super class '%s'.",
							name, superClass.getName()
						).throwSelfNative();
					}
					method.call(env.cniEnv, argc);
					break;
				}
				case OP_INVOKE: {
					int arity = readUint8();
					String attr = cp.getString(readIndex());
					CandyObject method = pop().callGetAttr(env.cniEnv, attr);
					TypeError.requiresCallable(method).call(env.cniEnv, arity);
					break;
				}
				case OP_CALL_SLOT: {
					int arity = readUint8();	
					TypeError.requiresCallable(load(readUint8()))
						.call(env.cniEnv, arity);
					break;
				}
				case OP_CALL_GLOBAL: {
					int arity = readUint8();
					String name = cp.getString(readIndex());
					TypeError.requiresCallable(
						getGlobalVariable(name, true)
					).call(env.cniEnv, arity);
					break;
				}
				case OP_CALL: {
					int arity = readUint8();
					TypeError.requiresCallable(pop()).call(env.cniEnv, arity);
					break;
				}
				case OP_CALL_EX: {
					int arity = readUint8();
					int unpackFlags = cp.getUnpackFlags(readIndex());
					TypeError.requiresCallable(pop())
						.call(env.cniEnv, arity, unpackFlags);
					break;
				}

				/**
				 * Error Handler.
				 */
				case OP_RAISE: {
					CandyObject err = pop();
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
				case OP_IMPORT: {
					ModuleObj moudleObj = ModuleManager.getManager().importModule(
						env.cniEnv, ObjectHelper.asString(pop())
					);
					env.globalEnv.setVariable(cp.getString(readIndex()), moudleObj);
					break;
				}
				case OP_ASSERT: {
					new com.nano.candy.interpreter.i2.builtin.type.error.
						AssertionError(pop().callStr(env.cniEnv).value()).throwSelfNative();
					break;
				}
				case OP_PRINT: {
					CandyObject obj = pop();
					if (obj == NullPointer.nil()) {
						break;
					}
					System.out.println(obj.callStr(env.cniEnv).value());
					break;
				}
				case OP_RETURN: {
					if (stack.peek().exitJavaMethodAtReturn) {
						popFrameWithRet();
						break loop;
					}
					popFrameWithRet(); 
					break;
				}
				case OP_RETURN_NIL: {
					push(NullPointer.nil());
					if (stack.peek().exitJavaMethodAtReturn) {
						popFrameWithRet();
						break loop;
					}
					popFrameWithRet(); 
					break;
				}
				case OP_EXIT: {
					break loop;
				}
			}
			/*if (tracerManager != null)
				tracerManager.notifyInsEnd(this);*/
		}
	}
}
