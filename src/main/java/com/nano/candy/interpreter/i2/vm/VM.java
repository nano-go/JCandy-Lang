package com.nano.candy.interpreter.i2.vm;

import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.ModuleObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.PrototypeFunctionObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.TupleObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.ClassSignature;
import com.nano.candy.interpreter.i2.builtin.type.classes.ObjectClass;
import com.nano.candy.interpreter.i2.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.i2.builtin.type.error.NameError;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.rtda.FileScope;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.GlobalScope;
import com.nano.candy.interpreter.i2.rtda.OperandStack;
import com.nano.candy.interpreter.i2.rtda.StackFrame;
import com.nano.candy.interpreter.i2.rtda.Upvalue;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.ErrorHandlerTable;
import com.nano.candy.interpreter.i2.rtda.module.ModuleManager;
import com.nano.candy.interpreter.i2.rtda.module.SourceFileInfo;
import com.nano.candy.interpreter.i2.vm.tracer.TracerManager;
import com.nano.candy.sys.CandySystem;
import java.io.File;

import static com.nano.candy.interpreter.i2.instruction.Instructions.*;

public final class VM {
	
	public static final boolean DEBUG = false;
	
	private static final byte WIDE_INDEX_MARK = (byte) 0xFF;
	
	private int maxStackDeepth = CandySystem.DEFAULT_MAX_STACK;
	
	private GlobalScope global;
	
	/**
	 * This is used to import source files as moudle to manage.
	 */
	private ModuleManager moudleManager;
	
	/**
	 * Trace code execution and the change of stack. Lazily initalized
	 * upon needed.
	 */
	private TracerManager tracerManager;
	
	private StackFrame stack;
	
	private ConstantPool cp;
	private byte[] code;
	private int pc;
	private CandyObject[] slots;
	private OperandStack opStack;
	
	private InterpreterOptions options;
	
	public VM() {}
	
	public void reset(InterpreterOptions options) {
		this.global = new GlobalScope();
		this.moudleManager = new ModuleManager();
		this.stack = new StackFrame(maxStackDeepth);
		this.options = options;
		this.tracerManager = null;
	}
	
	public InterpreterOptions getOptions() {
		return options;
	}
	
	public CompiledFileInfo getCurRunningFile() {
		return global.curFileScope().getCompiledFileInfo();
	}
	
	public SourceFileInfo getCurSourceFileInfo() {
		CompiledFileInfo compiledFileInfo = global.curFileScope().getCompiledFileInfo();
		if (compiledFileInfo.isRealFile()) {
			return SourceFileInfo.get(compiledFileInfo.getFile());
		}
		return null;
	}
	
	/**
	 * Returns the parent path of the current running file.
	 */
	public String getCurrentDirectory() {
		File f = getCurRunningFile().getFile();
		if (f.isDirectory()) {
			return f.getAbsolutePath();
		}
		File parent = f.getParentFile();
		if (parent == null) {
			return CandySystem.DEFAULT_USER_DIR;
		}
		return parent.getAbsolutePath();
	}
	
	public File getFile(String relativePath) {
		return new File(getCurrentDirectory(), relativePath);
	}
	
	public GlobalScope getGlobalScope() {
		return global;
	}
	
	public ModuleManager getModuleManager() {
		return moudleManager;
	}
	
	public TracerManager getTracerManager() {
		if (tracerManager == null) {
			tracerManager = new TracerManager();
		}
		return tracerManager;
	}
	
	public void loadChunk(Chunk chunk) {
		File f = new File(chunk.getSourceFileName());
		if (f.isFile()) {
			loadFile(new CompiledFileInfo(f.getPath(), chunk, true));
		} else {
			loadFile(new CompiledFileInfo(
				CandySystem.DEFAULT_USER_DIR, chunk, false
			));
		}
	}
	
	/**
	 * VM needs to load a compiled file to run.
	 * VM allows to load and run a compiled file at runtime.
	 */
	public void loadFile(CompiledFileInfo file) {
		global.setFileScope(file);
		pushFrame(Frame.fetchFrame(file.getChunk(), global.curFileScope()));
	}
	
	public StackFrame getFrameStack() {
		return stack;
	}
	
	public void syncPcToTopFrame() {
		if (!stack.isEmpty()) {
			stack.peek().pc = pc;
		}
	}
	
	public void pushFrame(Frame frame) {
		syncPcToTopFrame();
		stack.pushFrame(frame);
		syncFrameData();
		if (tracerManager != null)
			tracerManager.notifyStackPushed(this, stack);
	}
	
	public void popFrame() {
		Frame old = fastPopFrame();
		if (stack.isEmpty()) {
			resetFrameData();
		} else {
			syncFrameData();
		}
		if (tracerManager != null)
			tracerManager.notifyStackPoped(this, old, stack);
		old.recycleSelf();
	}
	
	public void popFrameWithRet() {
		Frame old = fastPopFrame();
		syncFrameData();
		// push return value to operand stack.
		push(old.pop());
		if (tracerManager != null)
			tracerManager.notifyStackPoped(this, old, stack);
		old.recycleSelf();
	}
	
	public void clearFrameStack() {
		while (!stack.isEmpty()) {
			fastPopFrame();
		}
		resetFrameData();
	}
	
	private final Frame fastPopFrame() {
		Frame old = stack.popFrame();
		if (old.isTopFrame()) {
			SourceFileInfo.unmarkRunning(old.chunk.getSourceFileName());
		}
		return old;
	}
	
	private void syncFrameData() {
		Frame frame = stack.peek();
		this.cp = frame.chunk.getConstantPool();
		this.slots = frame.slots;
		this.code = frame.chunk.getByteCode();
		this.pc = frame.pc;
		this.opStack = frame.opStack;
		global.setFileScope(frame.fileScope);
	}
	
	private void resetFrameData() {
		this.cp = null;
		this.slots = null;
		this.code = null;
		this.pc = 0;
		this.opStack = null;
		global.setFileScope((FileScope) null);
	}
	
	public void returnFromVM(CandyObject returnValue) {
		stack.peek().push(returnValue);
	}

	public void returnNilFromVM() {
		stack.peek().push(NullPointer.nil());
	}
	
	public  Frame frame() {
		return stack.peek();
	}

	/* -------------------- Execution Helper -------------------- */
	
	public CandyObject pop() {
		return opStack.pop();
	}

	public CandyObject peek(int k) {
		return opStack.peek(k);
	}

	public void push(CandyObject obj) {
		opStack.push(obj);
	}

	private CandyObject load(int slot) {
		return slots[slot];
	}

	private void store(int slot, CandyObject value) {
		slots[slot] = value;
	}

	private int readJumpOffset() {
		return (code[pc] << 8) & 0xFFFF | code[pc + 1] & 0xFF;
	}

	private int readUint8() {
		return code[pc ++] & 0xFF;
	}

	private int readIndex(){
		if (code[pc] != WIDE_INDEX_MARK) {
			return code[pc ++] & 0xFF;
		}
		// wide index: 2 unsigned bytes.
		pc ++;
		return (code[pc ++] << 8) & 0xFFFF | code[pc ++] & 0xFF;
	}
	
	public boolean tryToHandleError(ErrorObj err, boolean printError) {
		syncPcToTopFrame();
		if (err.getStackTraceElements() == null) {
			err.setStackTraceElements(stack);
		}
		ErrorHandlerTable. ErrorHandler handler = findExceptionHandler();
		if (handler != null) {
			handleError(err, handler);
			return true;
		}	
		if (printError) {
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

	public void handleError(ErrorObj err, ErrorHandlerTable.ErrorHandler handler) {	
		opStack.clear();
		pc = handler.handlerPc;
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
			pc += init.getLength();
		}
		for (ConstantValue.MethodInfo methodInfo : classInfo.methods) {
			PrototypeFunctionObj prototypefunc = 
				createFunctionObj(classInfo.className, methodInfo);
			signature.defineMethod(methodInfo.name, prototypefunc);
			pc += methodInfo.getLength();
		}
		return signature.setIsInheritable(true).build();
	}
	
	/**
	 * Creates a prototype function object.
	 *
	 * @param className The name of the class that the method is defined in or null.
	 * @param methodInfo The information of the prototype function.
	 */
	private PrototypeFunctionObj createFunctionObj(String className, ConstantValue.MethodInfo methodInfo) {
		Upvalue[] upvalues = frame().captureUpvalueObjs(methodInfo);
		String tagName = methodInfo.name;
		if (className != null) {
			tagName = ObjectHelper.methodName(className, tagName);
		}
		return new PrototypeFunctionObj(
			frame().chunk, pc, // Start pc.
			upvalues, tagName, 
			methodInfo, global.curFileScope()
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
	 * Instruction: OP_SUPER_INVOKE
	 *
	 * Call the specified name of the method of the specified class.
	 */
	private void evalOpSuperInvoke() {
		int arity = readUint8();
		CandyClass superClass = (CandyClass) pop();
		CandyObject instance = pop();
		String methodName = cp.getString(readIndex());
		CallableObj method= superClass.getBoundMethod(
			methodName, instance
		);
		if (method == null) {
			new TypeError(
				"'%s->%s' class has no method '%s'.",
				instance.getCandyClass().getClassName(),
				superClass.getClassName(), methodName
			).throwSelfNative();
		}
		ObjectHelper.checkIsValidCallable(method, arity);
		method.onCall(this);
	}
	
	private CandyObject getGlobalVariable(String name, boolean throwsErrorIfNotFound) {
		CandyObject obj = global.getVar(name);
		if (obj == null && throwsErrorIfNotFound) {
			new NameError("the variable '%s' not found.", name)
				.throwSelfNative();
		}
		return obj;
	}
	
	private boolean setGlobalVariable(String name, CandyObject val, boolean throwsErrorIfNotFound) {
		if (global.getVar(name) == null) {
			if (throwsErrorIfNotFound) {
				new NameError("the variable '%s' not found.", name)
					.throwSelfNative();
			}
			return false;
		}
		global.setVar(name, val);
		return true;
	}
	
	/* -------------------- Execution --------------------*/
	
	public int runHandleError() {
		int code = 0;
		while (true) {
			try {
				run();
				break;
			} catch (VMExitException e){
				code = e.code;
				break;
			} catch (Throwable e) {
				ErrorObj err;
				if (e instanceof CarrierErrorException) {
					err = ((CarrierErrorException) e).getErrorObj();
				} else {
					err = new NativeError(e);
				}
				if (tryToHandleError(err, true)) continue;
				if (DEBUG) e.printStackTrace();
				code = 1;
				break;
			}
		}
		clearFrameStack();
		return code;
	}
	
	public ModuleObj run() {
		SourceFileInfo srcFileInfo = getCurSourceFileInfo();
		if (srcFileInfo != null) {
			srcFileInfo.markRunning();
		}
		// OP_EXIT will help VM to exit runFrame method.
		runFrame(false);
		
		ModuleObj moudleObj = 
			global.curFileScope().generateModuleObject();
		popFrame();
		return moudleObj;
	}
	
	public void runFrame(boolean exitMethodAtFrameEnd) {
		if (exitMethodAtFrameEnd) {
			frame().exitMethodAtReturn = true;
		}
		
		loop: for (;;) {
			if (tracerManager != null)
				tracerManager.notifyInsStarted(this, pc);
			switch (code[pc ++]) {
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
					if (!pop().boolValue(this).value()) {
						pc += readJumpOffset();
					} else {
						pc += 2;
					}
					break;
				}	
				case OP_POP_JUMP_IF_TRUE: {
					if (pop().boolValue(this).value()) {
						pc += readJumpOffset();
					} else {
						pc += 2;
					}
					break;
				}		
				case OP_JUMP_IF_FALSE: {
					if (!peek(0).boolValue(this).value()) {
						pc += readJumpOffset();
					} else {
						pc += 2;
					}
					break;
				}		
				case OP_JUMP_IF_TRUE: {
					if (peek(0).boolValue(this).value()) {
						pc += readJumpOffset();
					} else {
						pc += 2;
					}
					break;
				}		
				case OP_JUMP: {
					pc += readJumpOffset();
					break;
				}
				case OP_LOOP: {
					pc -= readJumpOffset();
					break;
				}
				
				/**
				 * Unary Operations.
				 */
				case OP_NEGATIVE: {
					pop().negativeApi(this);
					break;
				}
				case OP_POSITIVE: {
					pop().positiveApi(this);
					break;
				}
				case OP_NOT: {
					push(pop().not(this));
					break;
				}

				/**
				 * Alrithmetical Operations.
				 */		
				case OP_ADD: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					val1.addApi(this, val2);
					break;
				}
				case OP_SUB: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					val1.subApi(this, val2);
					break;
				}				
				case OP_MUL: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					val1.mulApi(this, val2);
					break;
				}				
				case OP_DIV: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					val1.divApi(this, val2);
					break;
				}
				case OP_MOD: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					val1.modApi(this, val2);
					break;
				}
				
				/**
				 * Relation Operations.
				 */
				case OP_INSTANCE_OF: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(BoolObj.valueOf(
						val1.getCandyClass().isSubClassOf(val2.getCandyClass())
					));
					break;
				}	
				case OP_GT: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					val1.gtApi(this, val2);
					break;
				}
				case OP_GTEQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					val1.gteqApi(this, val2);
					break;
				}
				case OP_LT: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					val1.ltApi(this, val2);
					break;
				}
				case OP_LTEQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					val1.lteqApi(this, val2);
					break;
				}
				case OP_EQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					val1.equalsApi(this, val2);
					break;
				}
				case OP_NOTEQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.equalsApiExeUser(this, val2).not(this));
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
					global.setVar(
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
				case OP_LOAD: {
					push(load(readUint8()));
					break;
				}
				case OP_LOAD0: {
					push(load(0));
					break;
				}
				case OP_LOAD1: {
					push(load(1));
					break;
				}
				case OP_LOAD2: {
					push(load(2));
					break;
				}
				case OP_LOAD3: {
					push(load(3));
					break;
				}
				case OP_LOAD4: {
					push(load(4));
					break;
				}
				case OP_STORE: {
					store(readUint8(), peek(0));
					break;
				}
				case OP_STORE0: {
					store(0, peek(0));
					break;
				}
				case OP_STORE1: {
					store(1, peek(0));
					break;
				}
				case OP_STORE2: {
					store(2, peek(0));
					break;
				}
				case OP_STORE3: {
					store(3, peek(0));
					break;
				}
				case OP_STORE4: {
					store(4, peek(0));
					break;
				}
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
					frame().closeUpvalues(close);
					break;
				}		
				
				/**
				 * Upvalues.
				 */
				case OP_LOAD_UPVALUE: {
					push(frame().capturedUpvalues[readUint8()].load());
					break;
				}				
				case OP_STORE_UPVALUE: {
					frame().capturedUpvalues[readUint8()].store(peek(0));
					break;
				}
				
				
				/**
				 * Object Operations.
				 */
				case OP_GET_ATTR: {
					pop().getAttrApi(this, cp.getString(readIndex()));
					break;
				}		
				case OP_SET_ATTR: {
					CandyObject obj = pop();
					CandyObject value = pop();
					obj.setAttrApi(this, cp.getString(readIndex()), value);
					break;
				}
				case OP_GET_ITEM: {
					pop().getItemApi(this, pop());
					break;
				}
				case OP_SET_ITEM: {
					CandyObject obj = pop();
					CandyObject key = pop();
					CandyObject value = pop();
					obj.setItemApi(this, key, value);
					break;
				}
					
				/**
				 * Function Definition.
				 */
				case OP_FUN: {
					ConstantValue.MethodInfo methodInfo = cp.getMethodInfo(readIndex());
					push(createFunctionObj(null, methodInfo));
					pc += methodInfo.getLength();
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
					CandyClass clazz = (CandyClass) pop();
					push(clazz.getBoundMethod(
						cp.getString(readIndex()), pop())
					);
					break;
				}
				
				/**
				 * Call Operations.
				 */
				case OP_SUPER_INVOKE: {
					evalOpSuperInvoke();
					break;
				}
				case OP_INVOKE: {
					int arity = readUint8();
					String attr = cp.getString(readIndex());
					CandyObject method = pop().getAttrApiExeUser(this, attr);
					ObjectHelper.checkIsValidCallable(method, arity);
					method.onCall(this);
					break;
				}
				case OP_CALL_SLOT: {
					int arity = readUint8();	
					CandyObject function = load(readUint8());
					ObjectHelper.checkIsValidCallable(function, arity);
					function.onCall(this);
					break;
				}
				case OP_CALL_GLOBAL: {
					int arity = readUint8();
					String name = cp.getString(readIndex());
					CandyObject function = getGlobalVariable(name, true);
					ObjectHelper.checkIsValidCallable(function, arity);
					function.onCall(this);
					break;
				}
				case OP_CALL: {
					int arity = readUint8();
					CandyObject operand = pop();
					ObjectHelper.checkIsValidCallable(operand, arity);
					operand.onCall(this);
					break;
				}
				
				/**
				 * Error Handler.
				 */
				case OP_RAISE: {
					CandyObject err = pop();
					TypeError.checkTypeMatched(ErrorObj.ERROR_CLASS, err);
					if (!tryToHandleError((ErrorObj) err, true)) {
						throw new VMExitException(1);
					}
					break;
				}
				case OP_MATCH_ERRORS: {
					int offset = readJumpOffset();
					pc += 2;
					int count = readUint8();
					CandyObject error = peek(count);
					boolean matched = count == 0;
					for (int i = 0; i < count; i ++) {
						CandyObject obj = pop();
						if (!matched && error.getCandyClass()
							.isSubClassOf(obj.getCandyClass())) {
							matched = true;
						}
					}
					if (!matched) {
						pc += offset-3;
					}
					break;
				}	
				
				/**
				 * Other.
				 */
				case OP_IMPORT: {
					ModuleObj moudleObj = moudleManager.importModule(
						this, ObjectHelper.asString(pop())
					);
					global.setVar(cp.getString(readIndex()), moudleObj);
					break;
				}
				case OP_ASSERT: {
					new com.nano.candy.interpreter.i2.builtin.type.error.
						AssertionError(pop().strApiExeUser(this).value()).throwSelfNative();
					break;
				}
				case OP_PRINT: {
					CandyObject obj = pop();
					if (obj == NullPointer.nil()) {
						break;
					}
					System.out.println(obj.strApiExeUser(this).value());
					break;
				}
				case OP_RETURN: {
					if (frame().exitMethodAtReturn) {
						popFrameWithRet();
						break loop;
					}
					popFrameWithRet(); 
					break;
				}
				case OP_RETURN_NIL: {
					push(NullPointer.nil());
					if (frame().exitMethodAtReturn) {
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
			if (tracerManager != null)
				tracerManager.notifyInsEnd(this);
		}
	}
}
