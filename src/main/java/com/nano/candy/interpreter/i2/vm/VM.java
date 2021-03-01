package com.nano.candy.interpreter.i2.vm;

import com.nano.candy.interpreter.i2.builtin.CandyObject;
import com.nano.candy.interpreter.i2.builtin.type.ArrayObj;
import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.CallableObj;
import com.nano.candy.interpreter.i2.builtin.type.DoubleObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.NullPointer;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.UserFunctionObj;
import com.nano.candy.interpreter.i2.builtin.type.classes.CandyClass;
import com.nano.candy.interpreter.i2.builtin.type.classes.ObjectClass;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.error.CandyRuntimeError;
import com.nano.candy.interpreter.i2.error.TypeError;
import com.nano.candy.interpreter.i2.rtda.Chunk;
import com.nano.candy.interpreter.i2.rtda.ChunkAttributes;
import com.nano.candy.interpreter.i2.rtda.ConstantPool;
import com.nano.candy.interpreter.i2.rtda.ConstantValue;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.GlobalEnvironment;
import com.nano.candy.interpreter.i2.rtda.OperandStack;
import com.nano.candy.interpreter.i2.rtda.UpvalueObj;
import com.nano.candy.interpreter.i2.tool.DisassembleTool;
import java.util.Arrays;

import static com.nano.candy.interpreter.i2.instruction.Instructions.*;

public final class VM {
	
	public static final boolean DEBUG = false;
	
	private static final boolean DEBUG_TRACE_OPERAND_STACK = false;
	private static final boolean DEBUG_TRACE_SLOTS = false;
	private static final boolean DEBUG_TRACE_INSTRUCTION = false;
	private static final boolean DEBUG_TRACE_OPEN_UPVALUE = false;
	private static final boolean DEBUG_TRACE_FRAME_STACK = false;
	private static final boolean DEBUG_DISASSEMBLE = false;
	
	private int maxStackDeepth = 1024*2;
	private GlobalEnvironment global;
	
	private Frame[] frameStack;
	
	private int sp;
	private Frame frame;
	
	private ConstantPool cp;
	private byte[] code;
	private int pc;
	
	private CandyObject[] slots;
	private OperandStack opStack;
	
	public VM() {}
	
	public void reset() {
		global = new GlobalEnvironment();
		frameStack = new Frame[16];
	}
	
	public void loadChunk(Chunk chunk) {
		ChunkAttributes attrs = chunk.getAttrs();
		if (attrs.slots == null) {
			throw new Error("The slots of top scope can't be null.");
		}
		Frame topFrame = new Frame(chunk);
		this.frame = topFrame;
		this.frameStack[sp ++] = topFrame;
		resetFrameData();
	}
	
	private void resetFrameData() {
		this.cp = frame.chunk.getConstantPool();
		this.slots = frame.slots;
		this.code = frame.chunk.getByteCode();
		this.pc = frame.pc;
		this.opStack = frame.opStack;
	}
	
	public void syncPcToFrame() {
		Frame top = this.frameStack[sp-1];
		top.pc = this.pc;
	}
	
	public void pushFrame(Frame frame) {
		if (sp >= maxStackDeepth) {
			throw new StackOverflowError();
		}
		if (sp >= frameStack.length) {
			frameStack = Arrays.copyOf(frameStack, frameStack.length*2);
		}
		
		Frame top = this.frameStack[sp-1];
		top.pc = this.pc;
		
		this.frame = frame;
		this.frameStack[sp ++] = frame;
		resetFrameData();
	}
	
	public void popFrame() {
		Frame top = frameStack[-- sp];
		this.frame = frameStack[sp - 1];	
		resetFrameData();	
		push(top.pop());
		
		top.release();
	}
	
	public void clearStackFrame() {
		while (sp > 0) {
			sp --;
			frameStack[sp].release();
			frameStack[sp] = null;
		}
	}
	
	public void returnFromVM(CandyObject returnValue) {
		frame.push(returnValue);
	}

	public void returnNilFromVM() {
		frame.push(NullPointer.nil());
	}

	public int sp() {
		return sp;
	}

	public Frame getFrameAt(int sp) {
		return frameStack[sp - 1];
	}

	public Frame frame() {
		return frame;
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
		return (short)(code[pc] << 8 | code[pc + 1] & 0xFF);
	}
	
	private int readUint8() {
		return code[pc ++] & 0xFF;
	}
	
	private int readIndex(){
		return code[pc ++] & 0xFF;
	}
	
	private UserFunctionObj makeFunctionObject(CandyClass clazz, ConstantValue.MethodInfo methodInfo) {
		UpvalueObj[] upvalues = frame.makeUpvalueObjs(methodInfo);
		String tagName = methodInfo.name;
		if (clazz != null) {
			tagName = ObjectHelper.methodName(clazz, tagName);
		}
		return new UserFunctionObj(
			frame.chunk, pc, 
			methodInfo.name, tagName, upvalues,
			methodInfo.arity, methodInfo.slots, methodInfo.stackSize
		);
	}
	
	public void run() {
		runFrame(false);
	}
	
	public void runFrame(boolean exitMethod) {
		if (DEBUG_DISASSEMBLE) {
			System.out.println(
				new DisassembleTool(frame.chunk).disassemble()
			);
		}
		
		Frame curFrame = frame;
		
		loop: for (;;) {
			if (DEBUG_TRACE_OPERAND_STACK) {
				DebugHelper.traceOperandStack(frame.opStack);
			}
			
			if (DEBUG_TRACE_SLOTS) {
				DebugHelper.traceSlots(frame);
			}
			
			if (DEBUG_TRACE_OPEN_UPVALUE) {
				DebugHelper.traceOpenUpvalues(frame.openUpvalues);
			}
			
			if (DEBUG_TRACE_FRAME_STACK) {
				DebugHelper.traceFrameStack(frameStack, sp);
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
				 * Lable Instructions.
				 */
				case OP_POP_JUMP_IF_FALSE: {
					if (!pop().boolValue(this).value()) {
						// pc-1 is jump instruction pointer.
						pc += readJumpOffset()-1;
					} else {
						// skip jump-offset.
						pc += 2;
					}
					break;
				}	
				case OP_POP_JUMP_IF_TRUE: {
					if (pop().boolValue(this).value()) {
						pc += readJumpOffset()-1;
					} else {
						pc += 2;
					}
					break;
				}		
				case OP_JUMP_IF_FALSE: {
					if (!peek(0).boolValue(this).value()) {
						pc += readJumpOffset()-1;
					} else {
						pc += 2;
					}
					break;
				}		
				case OP_JUMP_IF_TRUE: {
					if (peek(0).boolValue(this).value()) {
						pc += readJumpOffset()-1;
					} else {						
						pc += 2;
					}
					break;
				}		
				case OP_JUMP: {
					pc += readJumpOffset()-1;
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
					push(DoubleObj.valueOf(cp.getDouble(readUint8())));
					break;
				}		
				case OP_ICONST: {
					push(IntegerObj.valueOf(cp.getInteger(readUint8())));
					break;
				}		
				case OP_SCONST: {
					push(StringObj.valueOf(cp.getString(readUint8())));
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
					String name = cp.getString(readUint8());
					global.setVar(name, pop());
					break;
				}		
				case OP_GLOBAL_SET: {
					String name = cp.getString(readUint8());
					if (global.getVar(name) == null) {
						throw new CandyRuntimeError("the variable '%s' not found.", name);
					}
					global.setVar(name, peek(0));					
					break;
				}		
				case OP_GLOBAL_GET: {
					String name = cp.getString(readUint8());
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
					for (int i = frame.slots.length-1; i >= lastIndex; i --) {
						frame.slots[i] = null;
					}
					break;
				}		
				
				/**
				 * Upvalues.
				 */
				case OP_LOAD_UPVALUE: {
					push(frame.capturedUpvalues[readUint8()].load());
					break;
				}				
				case OP_STORE_UPVALUE: {
					frame.capturedUpvalues[readUint8()].store(peek(0));
					break;
				}
				case OP_CLOSE_UPVALUE: {
					int closedUpvalues = readUint8();
					frame.closeUpvalues(closedUpvalues);
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
					push(makeFunctionObject(null, methodInfo));
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
						clazz.setInitalizer(makeFunctionObject(clazz, init));
						pc += init.codeBytes;
					}
					for (ConstantValue.MethodInfo methodInfo : classInfo.methods) {
						clazz.defineMethod(methodInfo.name, makeFunctionObject(clazz, methodInfo));
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
						if (frame == curFrame) {
							popFrame();
							break loop;
						}
					} 
					popFrame(); 
					break;
				}
				case OP_RETURN_NIL: {
					push(NullPointer.nil());
					if (exitMethod) {
						if (frame == curFrame) {
							popFrame();
							break loop;
						}
					} 
					popFrame(); 
					break;
				}
				case OP_EXIT: {
					clearStackFrame();
					break loop;
				}
			}
			if (InstructionBenchmarking.DEBUG) {
				InstructionBenchmarking.getInstance().endExeInstruction();
			}
		}		
	}
	
}
