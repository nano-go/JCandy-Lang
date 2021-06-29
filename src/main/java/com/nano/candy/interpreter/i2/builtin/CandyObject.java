package com.nano.candy.interpreter.i2.builtin;

import com.nano.candy.interpreter.i2.builtin.type.BoolObj;
import com.nano.candy.interpreter.i2.builtin.type.IntegerObj;
import com.nano.candy.interpreter.i2.builtin.type.MethodObj;
import com.nano.candy.interpreter.i2.builtin.type.StringObj;
import com.nano.candy.interpreter.i2.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.i2.builtin.type.error.TypeError;
import com.nano.candy.interpreter.i2.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.i2.cni.NativeClass;
import com.nano.candy.interpreter.i2.cni.NativeMethod;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.std.Names;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@NativeClass(name = "Object")
public class CandyObject {

	public static final boolean DEBUG = false;
	
	private static final int SET_ATTR_MASK = 1;
	private static final int GET_ATTR_MASK = 1 << 1;
	private static final int GET_UNKNOWN_ATTR_MASK = 1 << 2;
	private static final int SET_ITEM_MASK = 1 << 3;
	private static final int GET_ITEM_MASK = 1 << 4;
	private static final int POSTIVE_MASK = 1 << 5;
	private static final int NEGATIVE_MASK = 1 << 6;
	private static final int ADD_MASK = 1 << 7;
	private static final int SUB_MASK = 1 << 8;
	private static final int MUL_MASK = 1 << 9;
	private static final int DIV_MASK = 1 << 10;
	private static final int MOD_MASK = 1 << 11;
	private static final int GT_MASK = 1 << 12;
	private static final int GTEQ_MASK = 1 << 13;
	private static final int LT_MASK = 1 << 14;
	private static final int LTEQ_MASK = 1 << 15;
	private static final int EQ_MASK = 1 << 16;
	private static final int HASH_MASK = 1 << 17;
	private static final int STR_MASK = 1 << 18;
	private static final int ITERATOR_MASK = 1 << 19;
	
	private Map<String, CandyObject> metaData = Collections.emptyMap();
	private CandyClass klass;
	private boolean frozen;
	
	/**
	 * (builtinMethodFlags & methodMask) means a builtin method.
	 */
	private int builtinMethodFlags;
	
	/**
	 * Every Candy object have a constructor with no-args.
	 * 
	 * If the constructor with no-args is missing, the object can't
	 * be created in the Candy language level.
	 *
	 * This constructor is called by a CandyClass object when an
	 * instance of the class is created.
	 */
	protected CandyObject() {
		this(null);
	}

	public CandyObject(CandyClass klass) {
		this.klass = klass;
	}
	
	protected final void setCandyClass(CandyClass klass) {
		this.klass = Objects.requireNonNull(klass);
	}
	
	protected CandyClass initSelfCandyClass() {
		return null;
	}
	
	public final CandyClass getCandyClass() {
		if (klass == null) klass = initSelfCandyClass();
		return klass;
	}

	public final String getCandyClassName() {
		return getCandyClass().getName();
	}
	
	public final boolean isCandyClass() {
		return getCandyClass() == this;
	}
	
	public final void freeze() {
		this.frozen = true;
	}

	public final boolean frozen() {
		return frozen;
	}
	
	public final void checkFrozen() {
		if (frozen) {
			new AttributeError("The frozen object can't be changed.")
				.throwSelfNative();
		}
	}
	
	public boolean isCallable() {
		return false;
	}
	
	public void setMetaData(String name, CandyObject value) {
		if (metaData.isEmpty()) {
			metaData = new HashMap<>();
		}
		metaData.put(name, value);
	}
	
	public CandyObject getMetaData(String name) {
		return metaData.get(name);
	}
	
	public CandyObject removeMetaData(String name) {
		return metaData.remove(name);
	}
	
	@NativeMethod(name = "isCallable")
	public CandyObject isCallable(VM vm, CandyObject[] args) {
		return BoolObj.valueOf(isCallable());
	}
	
	@NativeMethod(name = "freeze")
	public CandyObject freeze(VM vm, CandyObject[] args) {
		freeze();
		return null;
	}

	@NativeMethod(name = "frozen")
	public CandyObject frozen(VM vm, CandyObject[] args) {
		return BoolObj.valueOf(frozen());
	}
	
	private final boolean isBuiltinMetnod(int mask) {
		return (builtinMethodFlags & mask) != 0;
	}
	
	private MethodObj getBoundMethod(String name, int mask) {
		MethodObj metObj = getCandyClass().getBoundMethod(name, this);
		if (metObj.isBuiltin()) {
			builtinMethodFlags |= mask;
		}
		return metObj;
	}

	
	public final CandyObject callSetAttr(VM vm, String name, CandyObject value) {
		if (isBuiltinMetnod(SET_ATTR_MASK)) {
			checkFrozen();
			setAttr(vm, name, value);
			return value;
		}
		return getBoundMethod(Names.METHOD_SET_ATTR, SET_ATTR_MASK)
			.callExeUser(vm, StringObj.valueOf(name), value);
	}
	public CandyObject setAttr(VM vm, String name, CandyObject value) {
		setMetaData(name, value);
		return value;
	}
	@NativeMethod(name = Names.METHOD_SET_ATTR, argc = 2)
	public final CandyObject setAttrMet(VM vm, CandyObject[] args) {
		checkFrozen();
		return setAttr(vm, ObjectHelper.asString(args[0]), args[1]);	
	}

	
	public final CandyObject callGetAttr(VM vm, String name) {
		if (isBuiltinMetnod(GET_ATTR_MASK)) {
			return getAttrMet(vm, new CandyObject[]{StringObj.valueOf(name)});
		}
		return getBoundMethod(Names.METHOD_GET_ATTR, GET_ATTR_MASK)
			.callExeUser(vm, StringObj.valueOf(name));
	}
	public CandyObject getAttr(VM vm, String name) {
		CandyObject val = getMetaData(name);
		if (val == null) {
			val = getCandyClass().getBoundMethod(name, this);
		}
		return val;
	}
	@NativeMethod(name = Names.METHOD_GET_ATTR, argc = 1)
	public final CandyObject getAttrMet(VM vm, CandyObject[] args) {
		String name = ObjectHelper.asString(args[0]);
		CandyObject val = getAttr(vm, name);
		if (val != null) {
			return val; 
		}
		return callGetUnknownAttr(vm, name);
	}
	
	
	public CandyObject callGetUnknownAttr(VM vm, String name) {
		if (isBuiltinMetnod(GET_UNKNOWN_ATTR_MASK)) {
			return getUnknownAttr(vm, name);
		}
		return getBoundMethod(Names.METHOD_GET_UNKNOWN_ATTR, GET_UNKNOWN_ATTR_MASK)
			.callExeUser(vm, StringObj.valueOf(name));
	}
	protected CandyObject getUnknownAttr(VM vm, String name) {
		AttributeError.checkAttributeNull(this, name, null);
		return null;
	}
	@NativeMethod(name = Names.METHOD_GET_UNKNOWN_ATTR, argc = 1)
	public final CandyObject getUnknownAttrMet(VM vm, CandyObject[] args) {
		return getUnknownAttr(vm, ObjectHelper.asString(args[0]));
	}
	
	
	public CandyObject callSetItem(VM vm, CandyObject key, CandyObject value) {
		if (isBuiltinMetnod(SET_ITEM_MASK)) {
			checkFrozen();
			return setItem(vm, key, value);
		}
		return getBoundMethod(Names.METHOD_SET_ITEM, SET_ITEM_MASK)
			.callExeUser(vm, key, value);
	}
	protected CandyObject setItem(VM vm, CandyObject key, CandyObject value) {
		new TypeError(
			"'%s'['%s'] = '%s'", 
			getCandyClassName(),
			key.getCandyClassName(),
			value.getCandyClassName()
		).throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_SET_ITEM, argc = 2)
	public final CandyObject setItemMet(VM vm, CandyObject[] args) {
		checkFrozen();
		return setItem(vm, args[0], args[1]);
	}
	
	
	public CandyObject callGetItem(VM vm, CandyObject key) {
		if (isBuiltinMetnod(GET_ITEM_MASK)) {
			checkFrozen();
			return getItem(vm, key);
		}
		return getBoundMethod(Names.METHOD_GET_ITEM, GET_ITEM_MASK)
			.callExeUser(vm, key);
	}
	protected CandyObject getItem(VM vm, CandyObject key) {
		new TypeError(
			"'%s'['%s']", getCandyClassName(), key.getCandyClassName()
		).throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_GET_ITEM, argc = 1)
	public final CandyObject getItemMet(VM vm, CandyObject[] args) {
		return getItem(vm, args[0]);
	}
	
	
	public CandyObject callPositive(VM vm) {
		if (isBuiltinMetnod(POSTIVE_MASK)) {
			return positive(vm);
		}
		return getBoundMethod(Names.METHOD_OP_POSITIVE, POSTIVE_MASK)
			.callExeUser(vm);
	}
	protected CandyObject positive(VM vm) {
		new TypeError("+").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_POSITIVE)
	public final CandyObject positiveMet(VM vm, CandyObject[] args) {
		return positive(vm);
	}

	
	public CandyObject callNegative(VM vm) {
		if (isBuiltinMetnod(NEGATIVE_MASK)) {
			return negative(vm);
		}
		return getBoundMethod(Names.METHOD_OP_NEGATIVE, NEGATIVE_MASK)
			.callExeUser(vm);
	}
	protected CandyObject negative(VM vm) {
		new TypeError("-").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_NEGATIVE)
	public final CandyObject negativeMet(VM vm, CandyObject[] args) {
		return negative(vm);
	}
	
	
	private void throwUnsupportBinaryOperator(String operator, CandyObject operand) {
		new TypeError(
			"The operator '%s' can't apply to types: %s and %s.",
			operator, getCandyClassName(), operand.getCandyClassName()
		).throwSelfNative();
	}
	
	private CandyObject callBinaryOp(VM vm, CandyObject operand,
	                                 String name, int mask) {
		return getBoundMethod(name, mask).callExeUser(vm, operand);
	}
	
	private BoolObj callRelativeBinaryOp(VM vm, CandyObject operand,
	                                     String name, int mask) {
		CandyObject obj = getBoundMethod(name, mask).callExeUser(vm, operand);
		return obj.boolValue(vm);
	}
	
	private void checkReturnedType(String name, CandyObject obj, CandyClass expectedType) {
		if (obj.getCandyClass().isSubClassOf(expectedType)) {
			return;
		}
		new TypeError(
			"%s expects to return a(n) %s value, but %s value returned.",
			name, expectedType.getName(), obj.getCandyClassName()
		).throwSelfNative();
		throw new Error();
	}
	
	public CandyObject callAdd(VM vm, CandyObject operand) {
		if (isBuiltinMetnod(ADD_MASK)) {
			return add(vm, operand);
		}
		return callBinaryOp(vm, operand, Names.METHOD_OP_ADD, ADD_MASK);
	}
	protected CandyObject add(VM vm, CandyObject operand) {
		if (operand instanceof StringObj || this instanceof StringObj) {
			return callStr(vm).add(vm, operand);
		}
		throwUnsupportBinaryOperator("+", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_ADD, argc = 1)
	public final CandyObject addMet(VM vm, CandyObject[] args) {
		return add(vm, args[0]);
	}
	
	
	public CandyObject callSub(VM vm, CandyObject operand) {
		if (isBuiltinMetnod(SUB_MASK)) {
			return sub(vm, operand);
		}
		return callBinaryOp(vm, operand, Names.METHOD_OP_SUB, SUB_MASK);
	}
	protected CandyObject sub(VM vm, CandyObject operand) {
		throwUnsupportBinaryOperator("-", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_SUB, argc = 1)
	public final CandyObject subMet(VM vm, CandyObject[] args) {
		return sub(vm, args[0]);
	}
	
	
	public CandyObject callMul(VM vm, CandyObject operand) {
		if (isBuiltinMetnod(MUL_MASK)) {
			return mul(vm, operand);
		}
		return callBinaryOp(vm, operand, Names.METHOD_OP_MUL, MUL_MASK);
	}
	protected CandyObject mul(VM vm, CandyObject operand) {
		throwUnsupportBinaryOperator("*", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_MUL, argc = 1)
	public final CandyObject mulMet(VM vm, CandyObject[] args) {
		return mul(vm, args[0]);
	}
	
	
	public CandyObject callDiv(VM vm, CandyObject operand) {
		if (isBuiltinMetnod(DIV_MASK)) {
			return div(vm, operand);
		}
		return callBinaryOp(vm, operand, Names.METHOD_OP_DIV, DIV_MASK);
	}
	protected CandyObject div(VM vm, CandyObject operand) {
		throwUnsupportBinaryOperator("/", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_DIV, argc = 1)
	public final CandyObject divMet(VM vm, CandyObject[] args) {
		return div(vm, args[0]);
	}
	
	
	public CandyObject callMod(VM vm, CandyObject operand) {
		if (isBuiltinMetnod(MOD_MASK)) {
			return mod(vm, operand);
		}
		return callBinaryOp(vm, operand, Names.METHOD_OP_MOD, MOD_MASK);
	}
	protected CandyObject mod(VM vm, CandyObject operand) {
		throwUnsupportBinaryOperator("%", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_MOD, argc = 1)
	public final CandyObject modMet(VM vm, CandyObject[] args) {
		return mod(vm, args[0]);
	}
	
	
	public BoolObj callGt(VM vm, CandyObject operand) {
		if (isBuiltinMetnod(GT_MASK)) {
			return gt(vm, operand);
		}
		return callRelativeBinaryOp(vm, operand, Names.METHOD_OP_GT, GT_MASK);
	}
	protected BoolObj gt(VM vm, CandyObject operand) {
		throwUnsupportBinaryOperator(">", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_GT, argc = 1)
	public final CandyObject gtMet(VM vm, CandyObject[] args) {
		return gt(vm, args[0]);
	}
	
	
	public BoolObj callGteq(VM vm, CandyObject operand) {
		if (isBuiltinMetnod(GTEQ_MASK)) {
			return gteq(vm, operand);
		}
		return callRelativeBinaryOp(vm, operand, Names.METHOD_OP_GTEQ, GTEQ_MASK);
	}
	protected BoolObj gteq(VM vm, CandyObject operand) {
		throwUnsupportBinaryOperator(">=", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_GTEQ, argc = 1)
	public final CandyObject gteqMet(VM vm, CandyObject[] args) {
		return gteq(vm, args[0]);
	}
	
	
	public BoolObj callLt(VM vm, CandyObject operand) {
		if (isBuiltinMetnod(LT_MASK)) {
			return lt(vm, operand);
		}
		return callRelativeBinaryOp(vm, operand, Names.METHOD_OP_LT, LT_MASK);
	}
	protected BoolObj lt(VM vm, CandyObject operand) {
		throwUnsupportBinaryOperator("<", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_LT, argc = 1)
	public final CandyObject ltMet(VM vm, CandyObject[] args) {
		return lt(vm, args[0]);
	}
	
	
	public BoolObj callLteq(VM vm, CandyObject operand) {
		if (isBuiltinMetnod(LTEQ_MASK)) {
			return lteq(vm, operand);
		}
		return callRelativeBinaryOp(vm, operand, Names.METHOD_OP_LTEQ, LTEQ_MASK);
	}
	protected BoolObj lteq(VM vm, CandyObject operand) {
		throwUnsupportBinaryOperator("<=", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_LTEQ, argc = 1)
	public final CandyObject lteqMet(VM vm, CandyObject[] args) {
		return lteq(vm, args[0]);
	}
	
	
	public BoolObj callEquals(VM vm, CandyObject operand) {
		if (isBuiltinMetnod(EQ_MASK)) {
			return equals(vm, operand);
		}
		return getBoundMethod(Names.METHOD_EQUALS, EQ_MASK)
			.callExeUser(vm, operand).boolValue(vm);
	}
	public BoolObj equals(VM vm, CandyObject operand) {
		return BoolObj.valueOf(this == operand);
	}
	@NativeMethod(name = Names.METHOD_EQUALS, argc = 1)
	public final CandyObject equals(VM vm, CandyObject[] args) {
		return equals(vm, args[0]);
	}
	
	
	public IntegerObj callHashCode(VM vm) {
		if (isBuiltinMetnod(HASH_MASK)) {
			return hashCode(vm);
		}
		CandyObject obj = getBoundMethod(Names.METHOD_HASH_CODE, HASH_MASK)
			.callExeUser(vm);
		checkReturnedType(Names.METHOD_HASH_CODE, obj, IntegerObj.INTEGER_CLASS);
		return (IntegerObj) obj;
	}
	public IntegerObj hashCode(VM vm) {
		return IntegerObj.valueOf(super.hashCode());
	}
	@NativeMethod(name = Names.METHOD_HASH_CODE)
	public final CandyObject hashCodeMethod(VM vm, CandyObject[] args) {
		return hashCode(vm);
	}
	
	
	public StringObj callStr(VM vm) {
		if (isBuiltinMetnod(STR_MASK)) {
			return str(vm);
		}
		CandyObject obj = getBoundMethod(Names.METHOD_STR_VALUE, STR_MASK)
			.callExeUser(vm);
		checkReturnedType(Names.METHOD_STR_VALUE, obj, StringObj.STRING_CLASS);
		return obj.str(vm);
	}
	public StringObj str(VM vm) {
		return StringObj.valueOf(this.toString());
	}
	@NativeMethod(name = Names.METHOD_STR_VALUE)
	public final CandyObject strMet(VM vm, CandyObject[] args) {
		return str(vm);
	}
	@Override
	public String toString() {
		return ObjectHelper.toString(
			getCandyClassName(), "hash - " + Integer.toHexString(hashCode())
		);
	}
	
	
	public CandyObject callIterator(VM vm) {
		if (isBuiltinMetnod(ITERATOR_MASK)) {
			return iterator(vm);
		}
		return getBoundMethod(Names.METHOD_ITERATOR, ITERATOR_MASK)
			.callExeUser(vm);
	}
	public CandyObject iterator(VM vm) {
		new TypeError("the object is not iterable.")
			.throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_ITERATOR)
	public final CandyObject iteratorMet(VM vm, CandyObject[] args) {
		return iterator(vm);
	}
	
	
	public BoolObj not(VM vm) { 
		return boolValue(vm).not(vm); 
	}
	public BoolObj boolValue(VM vm) {	
		return BoolObj.TRUE;
	}
	

	@NativeMethod(name = Names.METHOD_INITALIZER)
	public final CandyObject objDefaultInitializer(VM vm, CandyObject[] args) { 
		return this; 
	}

	@NativeMethod(name = "_class")
	public final CandyObject getClass(VM vm, CandyObject[] args) {
		return getCandyClass();
	}
}
