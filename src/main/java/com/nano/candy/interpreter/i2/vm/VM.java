package com.nano.candy.interpreter.i2.vm;

import com.nano.candy.interpreter.InterpreterOptions;
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
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.type.error.ErrorObj;
import com.nano.candy.interpreter.i2.builtin.type.error.NameError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.rtda.FileEnvironment;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.GlobalEnvironment;
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

import static com.nano.candy.interpreter.i2.code.OpCodes.*;

public final class VM {
	
	public static final boolean DEBUG = false;
	private static final byte WIDE_INDEX_MARK = (byte) 0xFF;
	
	private GlobalEnvironment globalEnv;
	
	/**
	 * This is used to import source files as moudles to manage.
	 */
	private ModuleManager moudleManager;
	
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
		this.globalEnv = new GlobalEnvironment();
		this.moudleManager = new ModuleManager();
		this.stack = new StackFrame(CandySystem.DEFAULT_MAX_STACK);
		this.options = options;
		this.tracerManager = null;
	}
	
	public InterpreterOptions getOptions() {
		return options;
	}
	
	public CompiledFileInfo getCurRunningFile() {
		return globalEnv.getCurrentFileEnv().getCompiledFileInfo();
	}
	
	public SourceFileInfo getCurSourceFileInfo() {
		CompiledFileInfo compiledFileInfo = getCurRunningFile();
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
	
	/**
	 * Returns java library paths used to load .jar files in
	 * Candy language level.
	 */
	public String[] getJavaLibraryPaths() {
		return new String[] {
			getCurrentDirectory(),
			CandySystem.getCandyLibsPath()
		};
	}
	
	public GlobalEnvironment getGlobalEnv() {
		return globalEnv;
	}
	
	public FileEnvironment getCurrentFileEnv() {
		return globalEnv.getCurrentFileEnv();
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
		globalEnv.setCurrentFileEnv(file);
		pushFrame(Frame.fetchFrame(file.getChunk(), globalEnv.getCurrentFileEnv()));
	}
	
	public StackFrame getFrameStack() {
		return stack;
	}
	
	public void syncPcToTopFrame() {
		if (!stack.isEmpty()) {
			stack.peek().pc = pc;
		}
	}
	
	private void pushFrame(Frame frame) {
		syncPcToTopFrame();
		stack.pushFrame(frame);
		syncFrameData();
		if (tracerManager != null)
			tracerManager.notifyStackPushed(this, stack);
	}
	
	private void popFrame() {
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
	
	private void popFrameWithRet() {
		Frame old = fastPopFrame();
		syncFrameData();
		// push return value to operand stack.
		push(old.pop());
		if (tracerManager != null)
			tracerManager.notifyStackPoped(this, old, stack);
		old.recycleSelf();
	}
	
	private final Frame fastPopFrame() {
		Frame old = stack.popFrame();
		if (old.isSourceFileFrame()) {
			SourceFileInfo.unmarkRunning(old.chunk.getSourceFileName());
		}
		return old;
	}
	
	private void clearFrameStack() {
		while (!stack.isEmpty()) {
			fastPopFrame();
		}
		resetFrameData();
	}
	
	private void syncFrameData() {
		Frame frame = stack.peek();
		this.cp = frame.chunk.getConstantPool();
		this.slots = frame.slots;
		this.code = frame.chunk.getByteCode();
		this.pc = frame.pc;
		this.opStack = frame.opStack;
		globalEnv.setCurrentFileEnv(frame.fileEnv);
	}
	
	private void resetFrameData() {
		this.cp = null;
		this.slots = null;
		this.code = null;
		this.pc = 0;
		this.opStack = null;
		globalEnv.setCurrentFileEnv((FileEnvironment) null);
	}
	
	public void returnFromVM(CandyObject returnValue) {
		if (returnValue == null) {
			returnValue = NullPointer.nil();
		}
		stack.peek().push(returnValue);
	}

	public void returnNilFromVM() {
		stack.peek().push(NullPointer.nil());
	}
	
	public Frame frame() {
		return stack.peek();
	}

	/* -------------------- Execution Helper -------------------- */
	
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
	
	private boolean tryToHandleError(ErrorObj err, boolean printError) {
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

	private void handleError(ErrorObj err, ErrorHandlerTable.ErrorHandler handler) {	
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
			PrototypeFunction prototypefunc = 
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
	private PrototypeFunction createFunctionObj(String className, ConstantValue.MethodInfo methodInfo) {
		Upvalue[] upvalues = frame().captureUpvalueObjs(methodInfo);
		String tagName = methodInfo.name;
		if (className != null) {
			tagName = ObjectHelper.methodName(className, tagName);
		}
		return new PrototypeFunction(
			frame().chunk, pc, // Start pc.
			upvalues, tagName, 
			methodInfo, globalEnv.getCurrentFileEnv()
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
			map.put(this, key, value);
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
		method.call(this, argc);
	}
	
	/**
	 * Instruction: OP_SUPER_GET
	 */
	private void evalSuperGet() {
		CandyClass superClass = (CandyClass) pop();
		String name = cp.getString(readIndex());
		CandyObject superMethod = superClass.getBoundMethod(name, pop());
		if (superMethod == null) {
			new AttributeError(
				"The attribute '%s' is not found in the super class '%s'.",
				name, superClass.getName()
			).throwSelfNative();
		}
	}
	
	/**
	 * Instruction: OP_MATCH_ERRORS
	 */
	private void evalOpMatchErrors() {
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
	}	
	
	private CandyObject getGlobalVariable(String name, boolean throwsErrorIfNotFound) {
		CandyObject obj = globalEnv.getVariableValue(name);
		if (obj == null && throwsErrorIfNotFound) {
			new NameError("the variable '%s' not found.", name)
				.throwSelfNative();
		}
		return obj;
	}
	
	private boolean setGlobalVariable(String name, CandyObject val, boolean throwsErrorIfNotFound) {
		if (globalEnv.getVariableValue(name) == null) {
			if (throwsErrorIfNotFound) {
				new NameError("the variable '%s' not found.", name)
					.throwSelfNative();
			}
			return false;
		}
		globalEnv.setVariable(name, val);
		return true;
	}
	
	/* -------------------- Execution --------------------*/
	
	public void runPrototypeFunction(PrototypeFunction func, int argc) {
		Frame newFrame = Frame.fetchFrame(func);	
		for (int i = 0; i < argc; i ++) {
			newFrame.store(i, pop());
		}
		pushFrame(newFrame);
	}
	
	public int runHandleError() {
		int code = 0;
		try {
			run();
		} catch (VMExitException e) {
			code = e.code;
		}
		clearFrameStack();
		return code;
	}
	
	public ModuleObj run() {
		SourceFileInfo srcFileInfo = getCurSourceFileInfo();
		if (srcFileInfo != null) {
			srcFileInfo.markRunning();
		}
		runFrame(false);
		ModuleObj moudleObj = 
			globalEnv.getCurrentFileEnv().generateModuleObject();
		popFrame();
		return moudleObj;
	}
	
	public void runFrame(boolean exitMethodAtFrameEnd) {
		if (exitMethodAtFrameEnd) {
			frame().exitMethodAtReturn = true;
		}
		int deepth = stack.sp();
		while (true) {
			try {
				runCore();
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
					// unable to catch the thrown error and 
					// throws the exception to exit the VM.
					throw new VMExitException(70);
				}
				if (stack.sp() < deepth) {
					throw new ContinueRunException();
				}
				continue;
			}		
		}
	}
	
	private void runCore() {
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
					pc += !pop().boolValue(this).value() ?
						readJumpOffset() : 2;
					break;
				}	
				case OP_POP_JUMP_IF_TRUE: {
					pc += pop().boolValue(this).value() ?
						readJumpOffset() : 2;
					break;
				}		
				case OP_JUMP_IF_FALSE: {
					pc += !peek(0).boolValue(this).value() ?
						readJumpOffset() : 2;
					break;
				}		
				case OP_JUMP_IF_TRUE: {
					pc += peek(0).boolValue(this).value() ?
						readJumpOffset() : 2;
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
					push(pop().callNegative(this));
					break;
				}
				case OP_POSITIVE: {
					push(pop().callPositive(this));
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
					push(val1.callAdd(this, val2));
					break;
				}
				case OP_SUB: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callSub(this, val2));
					break;
				}				
				case OP_MUL: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callMul(this, val2));
					break;
				}				
				case OP_DIV: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callDiv(this, val2));
					break;
				}
				case OP_MOD: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callMod(this, val2));
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
					push(val1.callGt(this, val2));
					break;
				}
				case OP_GTEQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callGteq(this, val2));
					break;
				}
				case OP_LT: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callLt(this, val2));
					break;
				}
				case OP_LTEQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callLteq(this, val2));
					break;
				}
				case OP_EQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callEquals(this, val2));
					break;
				}
				case OP_NOTEQ: {
					CandyObject val2 = pop();
					CandyObject val1 = pop();
					push(val1.callEquals(this, val2).not(this));
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
					globalEnv.setVariable(
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
					push(load(code[pc-1]-OP_LOAD0));
					break;
				
				case OP_STORE:
					store(readUint8(), peek(0));
					break;
				case OP_STORE0: 
				case OP_STORE1: 
				case OP_STORE2: 
				case OP_STORE3: 
				case OP_STORE4:
					store(code[pc-1]-OP_STORE0, peek(0));
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
					push(pop().callGetAttr(this, cp.getString(readIndex())));
					break;
				}		
				case OP_SET_ATTR: {
					CandyObject obj = pop();
					CandyObject value = pop();
					push(obj.callSetAttr(this, cp.getString(readIndex()), value));
					break;
				}
				case OP_GET_ITEM: {
					push(pop().callGetItem(this, pop()));
					break;
				}
				case OP_SET_ITEM: {
					CandyObject obj = pop();
					CandyObject key = pop();
					CandyObject value = pop();
					push(obj.callSetItem(this, key, value));
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
					evalSuperGet();
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
					CandyObject method = pop().callGetAttr(this, attr);
					TypeError.requiresCallable(method).call(this, arity);
					break;
				}
				case OP_CALL_SLOT: {
					int arity = readUint8();	
					TypeError.requiresCallable(load(readUint8()))
						.call(this, arity);
					break;
				}
				case OP_CALL_GLOBAL: {
					int arity = readUint8();
					String name = cp.getString(readIndex());
					TypeError.requiresCallable(
						getGlobalVariable(name, true)
					).call(this, arity);
					break;
				}
				case OP_CALL: {
					int arity = readUint8();
					TypeError.requiresCallable(pop()).call(this, arity);
					break;
				}
				case OP_CALL_EX: {
					int arity = readUint8();
					int unpackFlags = cp.getUnpackFlags(readIndex());
					TypeError.requiresCallable(pop())
						.call(this, arity, unpackFlags);
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
					ModuleObj moudleObj = moudleManager.importModule(
						this, ObjectHelper.asString(pop())
					);
					globalEnv.setVariable(cp.getString(readIndex()), moudleObj);
					break;
				}
				case OP_ASSERT: {
					new com.nano.candy.interpreter.i2.builtin.type.error.
						AssertionError(pop().callStr(this).value()).throwSelfNative();
					break;
				}
				case OP_PRINT: {
					CandyObject obj = pop();
					if (obj == NullPointer.nil()) {
						break;
					}
					System.out.println(obj.callStr(this).value());
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



