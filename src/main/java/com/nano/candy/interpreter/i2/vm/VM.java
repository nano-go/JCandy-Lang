package com.nano.candy.interpreter.i2.vm;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.MoudleObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.PrototypeFunctionObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.ObjectClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.error.CandyRuntimeError;
import com.nano.candy.interpreter.i2.error.TypeError;
import com.nano.candy.interpreter.i2.rtda.FileScope;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.FrameStack;
import com.nano.candy.interpreter.i2.rtda.GlobalEnvironment;
import com.nano.candy.interpreter.i2.rtda.OperandStack;
import com.nano.candy.interpreter.i2.rtda.UpvalueObj;
import com.nano.candy.interpreter.i2.rtda.chunk.Chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantPool;
import com.nano.candy.interpreter.i2.rtda.chunk.ConstantValue;
import com.nano.candy.interpreter.i2.rtda.moudle.CompiledFileInfo;
import com.nano.candy.interpreter.i2.rtda.moudle.MoudleManager;
import com.nano.candy.interpreter.i2.rtda.moudle.SourceFileInfo;
import com.nano.candy.interpreter.i2.vm.debug.DebugHelper;
import com.nano.candy.interpreter.i2.vm.debug.InstructionBenchmarking;
import com.nano.candy.sys.CandySystem;
import java.io.File;

import static com.nano.candy.interpreter.i2.instruction.Instructions.*;

public final class VM {
	
	public static final boolean DEBUG = false;
	
	private static final boolean DEBUG_TRACE_OPERAND_STACK = false;
	private static final boolean DEBUG_TRACE_SLOTS = false;
	private static final boolean DEBUG_TRACE_INSTRUCTION = false;
	private static final boolean DEBUG_TRACE_OPEN_UPVALUE = false;
	private static final boolean DEBUG_TRACE_FRAME_STACK = false;
	
	private static final byte WIDE_INDEX_MARK = (byte) 0xFF;
	
	private int maxStackDeepth = CandySystem.DEFAULT_MAX_STACK;
	
	private GlobalEnvironment global;
	private MoudleManager moudleManager;
	
	private FrameStack frameStack;
	
	private ConstantPool cp;
	private byte[] code;
	private int pc;
	private CandyObject[] slots;
	private OperandStack opStack;
	
	public VM() {}
	
	public void reset() {
		this.global = new GlobalEnvironment();
		this.moudleManager = new MoudleManager();
		this.frameStack = new FrameStack(maxStackDeepth);
	}
	
	public CompiledFileInfo getCurRunningFile() {
		return global.curFileScope().compiledFileInfo;
	}
	
	public SourceFileInfo getCurSourceFileInfo() {
		CompiledFileInfo compiledFileInfo = global.curFileScope().compiledFileInfo;
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
		return f.getParentFile().getAbsolutePath();
	}
	
	public MoudleManager getMoudleManager() {
		return moudleManager;
	}
	
	public void loadChunk(Chunk chunk) {
		loadFile(new CompiledFileInfo(
			CandySystem.DEFAULT_USER_DIR, chunk, false));
	}
	
	/**
	 * VM needs to load one compiled file (only one at a time) to run.
	 * VM allows to load and run a compiled file at runtime.
	 */
	public void loadFile(CompiledFileInfo file) {
		global.setFileScope(file);
		pushFrame(new Frame(file.getChunk(), global.curFileScope()));
	}
	
	public FrameStack getFrameStack() {
		return frameStack;
	}
	
	public void syncPcToTopFrame() {
		if (!frameStack.isEmpty()) {
			frameStack.peek().pc = pc;
		}
	}
	
	public void pushFrame(Frame frame) {
		syncPcToTopFrame();
		frameStack.pushFrame(frame);
		syncFrameData();
	}
	
	public void popFrame() {
		frameStack.popFrame().release();
		if (frameStack.isEmpty()) {
			resetFrameData();
		} else {
			syncFrameData();
		}
	}
	
	public void popFrameWithRet() {
		Frame top = frameStack.popFrame();
		syncFrameData();
		// push return value to operand stack.
		push(top.pop());
		top.release();
	}
	
	private void syncFrameData() {
		Frame frame = frameStack.peek();
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
		frameStack.peek().push(returnValue);
	}

	public void returnNilFromVM() {
		frameStack.peek().push(NullPointer.nil());
	}
	
	public Frame frame() {
		return frameStack.peek();
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
	
	private PrototypeFunctionObj createFunctionObj(CandyClass clazz, ConstantValue.MethodInfo methodInfo) {
		UpvalueObj[] upvalues = frame().makeUpvalueObjs(methodInfo);
		String tagName = methodInfo.name;
		if (clazz != null) {
			tagName = ObjectHelper.methodName(clazz, tagName);
		}
		return new PrototypeFunctionObj(
			frame().chunk, pc, 
			upvalues, tagName, methodInfo, global.curFileScope()
		);
	}
	
	public MoudleObj run() {
		SourceFileInfo srcFileInfo = getCurSourceFileInfo();
		if (srcFileInfo != null) {
			srcFileInfo.markRunning();
		}
		
		runFrame(false);
		
		if (srcFileInfo != null) {
			srcFileInfo.unmarkRunning();
		}
		
		FileScope fs = global.curFileScope();
		MoudleObj moudleObj = new MoudleObj(
			fs.compiledFileInfo.getAbsPath(), fs.vars);
		popFrame();
		return moudleObj;
	}
	
	public void runFrame(boolean exitMethod) {	
		Frame curFrame = frame();
		
		loop: for (;;) {
			if (DEBUG_TRACE_OPERAND_STACK) {
				DebugHelper.traceOperandStack(frame().opStack);
			}
			
			if (DEBUG_TRACE_SLOTS) {
				DebugHelper.traceSlots(frame());
			}
			
			if (DEBUG_TRACE_OPEN_UPVALUE) {
				DebugHelper.traceOpenUpvalues(frame().openUpvalues);
			}
			
			if (DEBUG_TRACE_FRAME_STACK) {
				DebugHelper.traceFrameStack(frameStack);
			}
			
			if (DEBUG_TRACE_INSTRUCTION) {
				DebugHelper.traceInstruction(pc, code);
				System.out.println();
			}
			
			if (InstructionBenchmarking.DEBUG) {
				InstructionBenchmarking.getInstance().startExeInstructoon(code[pc]);
			}
			
			switch (code[pc ++]) {
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
					int elements = readUint8();
					ArrayObj arr = (ArrayObj) peek(elements);
					for (int i = 0; i < elements; i ++) {
						arr.append(pop());
					}
					break;
				}
				
				/**
				 * Global Operarions.
				 */
				case OP_GLOBAL_DEFINE: {
					String name = cp.getString(readIndex());
					global.setVar(name, pop());
					break;
				}		
				case OP_GLOBAL_SET: {
					String name = cp.getString(readIndex());
					if (global.getVar(name) == null) {
						throw new CandyRuntimeError("the variable '%s' not found.", name);
					}
					global.setVar(name, peek(0));					
					break;
				}		
				case OP_GLOBAL_GET: {
					String name = cp.getString(readIndex());
					CandyObject value = global.getVar(name);
					if (value == null) {
						throw new CandyRuntimeError("the variable '%s' not found.", name);
					}
					push(value);
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
				case OP_CLOSE_SLOT: {
					final int lastIndex = readUint8();
					for (int i = frame().slots.length-1; i >= lastIndex; i --) {
						frame().slots[i] = null;
					}
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
				case OP_CLOSE_UPVALUE: {
					frame().closeUpvalues(readUint8());
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
					pop().getItemApi(this);
					break;
				}
				case OP_SET_ITEM: {
					pop().setItemApi(this);
					break;
				}
					
				/**
				 * Function Definition.
				 */
				case OP_FUN: {
					ConstantValue.MethodInfo methodInfo = cp.getMethodInfo(readIndex());
					push(createFunctionObj(null, methodInfo));
					pc += methodInfo.codeBytes;
					break;
				}
				
				/**
				 * Class Definition.
				 */
				case OP_CLASS: {
					ConstantValue.ClassInfo classInfo = cp.getClassInfo(readIndex());
					CandyClass superClass;
					if (classInfo.hasSuperClass) {
						CandyObject superObj = pop();
						if (!(superObj instanceof CandyClass)) {
							throw new TypeError(
								"A class can't inherti a non-class: %s -> '%s'", 
								classInfo.className, superObj.getCandyClassName()
							);
						}
						superClass = (CandyClass) superObj;
					} else {
						superClass = ObjectClass.getObjClass();
					}
					store(readUint8(), superClass);
					
					CandyClass clazz = new CandyClass(classInfo.className, superClass);
					
					if (classInfo.initializer.isPresent()) {
						ConstantValue.MethodInfo init = classInfo.initializer.get();
						clazz.setInitalizer(createFunctionObj(clazz, init));
						pc += init.codeBytes;
					}
					for (ConstantValue.MethodInfo methodInfo : classInfo.methods) {
						clazz.defineMethod(methodInfo.name, createFunctionObj(clazz, methodInfo));
						pc += methodInfo.codeBytes;
					}
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
					int arity = readUint8();
					CandyClass clazz = (CandyClass) pop();
					CandyObject instance = pop();
					String methodName = cp.getString(readIndex());
					CallableObj method= clazz.getBoundMethod(
						methodName, instance
					);
					if (method == null) {
						throw new CandyRuntimeError(
							"'%s->%s' class has no method '%s'.",
							instance.getCandyClass().getClassName(),
							clazz.getClassName(), methodName
						);
					}
					ObjectHelper.checkIsValidCallable(method, arity);
					method.onCall(this);
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
					CandyObject function = global.getVar(name);
					if (function == null) {
						throw new CandyRuntimeError("the variable '%s' not found.", name);
					}
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
				 * Other.
				 */
				case OP_IMPORT: {
					MoudleObj moudleObj = moudleManager.importFile(
						this, ObjectHelper.asString(pop())
					);
					global.setVar(cp.getString(readIndex()), moudleObj);
					break;
				}
				case OP_ASSERT: {
					throw new com.nano.candy.interpreter.i2.error.
						AssertionError(pop().strApiExeUser(this).value());
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
					if (exitMethod) {
						if (frame() == curFrame) {
							popFrameWithRet();
							break loop;
						}
					} 
					popFrameWithRet(); 
					break;
				}
				case OP_RETURN_NIL: {
					push(NullPointer.nil());
					if (exitMethod) {
						if (frame() == curFrame) {
							popFrameWithRet();
							break loop;
						}
					} 
					popFrameWithRet(); 
					break;
				}
				case OP_EXIT: {
					break loop;
				}
			}
			if (InstructionBenchmarking.DEBUG) {
				InstructionBenchmarking.getInstance().endExeInstruction();
			}
		}		
	}
	
}
