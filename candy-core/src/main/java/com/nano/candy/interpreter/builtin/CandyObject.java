package com.nano.candy.interpreter.builtin;

import com.nano.candy.interpreter.builtin.type.BoolObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.MethodObj;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeMethod;
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
	 * This constructor is reflectly called by the CandyClass.
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
		return this instanceof CandyClass;
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
	
	public boolean isInstanceOf(CandyObject obj) {
		CandyClass klass = TypeError.requiresClass(obj);
		return getCandyClass().isSubClassOf(klass);
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
	
	@NativeMethod(name = "isClass")
	public CandyObject isClass(CNIEnv env, CandyObject[] args) {
		return BoolObj.valueOf(isCandyClass());
	}
	
	@NativeMethod(name = "isCallable")
	public CandyObject isCallable(CNIEnv env, CandyObject[] args) {
		return BoolObj.valueOf(isCallable());
	}
	
	@NativeMethod(name = "freeze")
	public CandyObject freeze(CNIEnv env, CandyObject[] args) {
		freeze();
		return null;
	}

	@NativeMethod(name = "frozen")
	public CandyObject frozen(CNIEnv env, CandyObject[] args) {
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

	
	public final CandyObject callSetAttr(CNIEnv env, String name, CandyObject value) {
		if (isBuiltinMetnod(SET_ATTR_MASK)) {
			checkFrozen();
			setAttr(env, name, value);
			return value;
		}
		return getBoundMethod(Names.METHOD_SET_ATTR, SET_ATTR_MASK)
			.call(env, StringObj.valueOf(name), value);
	}
	public CandyObject setAttr(CNIEnv env, String name, CandyObject value) {
		setMetaData(name, value);
		return value;
	}
	@NativeMethod(name = Names.METHOD_SET_ATTR, argc = 2)
	public final CandyObject setAttrMet(CNIEnv env, CandyObject[] args) {
		checkFrozen();
		return setAttr(env, ObjectHelper.asString(args[0]), args[1]);	
	}

	
	public final CandyObject callGetAttr(CNIEnv env, String name) {
		if (isBuiltinMetnod(GET_ATTR_MASK)) {
			return getAttrMet(env, new CandyObject[]{StringObj.valueOf(name)});
		}
		return getBoundMethod(Names.METHOD_GET_ATTR, GET_ATTR_MASK)
			.call(env, StringObj.valueOf(name));
	}
	public CandyObject getAttr(CNIEnv env, String name) {
		CandyObject val = getMetaData(name);
		if (val == null) {
			val = getCandyClass().getBoundMethod(name, this);
		}
		return val;
	}
	@NativeMethod(name = Names.METHOD_GET_ATTR, argc = 1)
	public final CandyObject getAttrMet(CNIEnv env, CandyObject[] args) {
		String name = ObjectHelper.asString(args[0]);
		CandyObject val = getAttr(env, name);
		if (val != null) {
			return val; 
		}
		return callGetUnknownAttr(env, name);
	}
	
	
	public CandyObject callGetUnknownAttr(CNIEnv env, String name) {
		if (isBuiltinMetnod(GET_UNKNOWN_ATTR_MASK)) {
			return getUnknownAttr(env, name);
		}
		return getBoundMethod(Names.METHOD_GET_UNKNOWN_ATTR, GET_UNKNOWN_ATTR_MASK)
			.call(env, StringObj.valueOf(name));
	}
	protected CandyObject getUnknownAttr(CNIEnv env, String name) {
		AttributeError.checkAttributeNull(this, name, null);
		return null;
	}
	@NativeMethod(name = Names.METHOD_GET_UNKNOWN_ATTR, argc = 1)
	public final CandyObject getUnknownAttrMet(CNIEnv env, CandyObject[] args) {
		return getUnknownAttr(env, ObjectHelper.asString(args[0]));
	}
	
	
	public CandyObject callSetItem(CNIEnv env, CandyObject key, CandyObject value) {
		if (isBuiltinMetnod(SET_ITEM_MASK)) {
			checkFrozen();
			return setItem(env, key, value);
		}
		return getBoundMethod(Names.METHOD_SET_ITEM, SET_ITEM_MASK)
			.call(env, key, value);
	}
	protected CandyObject setItem(CNIEnv env, CandyObject key, CandyObject value) {
		new TypeError(
			"'%s'['%s'] = '%s'", 
			getCandyClassName(),
			key.getCandyClassName(),
			value.getCandyClassName()
		).throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_SET_ITEM, argc = 2)
	public final CandyObject setItemMet(CNIEnv env, CandyObject[] args) {
		checkFrozen();
		return setItem(env, args[0], args[1]);
	}
	
	
	public CandyObject callGetItem(CNIEnv env, CandyObject key) {
		if (isBuiltinMetnod(GET_ITEM_MASK)) {
			checkFrozen();
			return getItem(env, key);
		}
		return getBoundMethod(Names.METHOD_GET_ITEM, GET_ITEM_MASK)
			.call(env, key);
	}
	protected CandyObject getItem(CNIEnv env, CandyObject key) {
		new TypeError(
			"'%s'['%s']", getCandyClassName(), key.getCandyClassName()
		).throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_GET_ITEM, argc = 1)
	public final CandyObject getItemMet(CNIEnv env, CandyObject[] args) {
		return getItem(env, args[0]);
	}
	
	
	public CandyObject callPositive(CNIEnv env) {
		if (isBuiltinMetnod(POSTIVE_MASK)) {
			return positive(env);
		}
		return getBoundMethod(Names.METHOD_OP_POSITIVE, POSTIVE_MASK)
			.call(env);
	}
	protected CandyObject positive(CNIEnv env) {
		new TypeError("+").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_POSITIVE)
	public final CandyObject positiveMet(CNIEnv env, CandyObject[] args) {
		return positive(env);
	}

	
	public CandyObject callNegative(CNIEnv env) {
		if (isBuiltinMetnod(NEGATIVE_MASK)) {
			return negative(env);
		}
		return getBoundMethod(Names.METHOD_OP_NEGATIVE, NEGATIVE_MASK)
			.call(env);
	}
	protected CandyObject negative(CNIEnv env) {
		new TypeError("-").throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_NEGATIVE)
	public final CandyObject negativeMet(CNIEnv env, CandyObject[] args) {
		return negative(env);
	}
	
	
	private void throwUnsupportBinaryOperator(String operator, CandyObject operand) {
		new TypeError(
			"The operator '%s' can't apply to types: %s and %s.",
			operator, getCandyClassName(), operand.getCandyClassName()
		).throwSelfNative();
	}
	
	private CandyObject callBinaryOp(CNIEnv env, CandyObject operand,
	                                 String name, int mask) {
		return getBoundMethod(name, mask).call(env, operand);
	}
	
	private BoolObj callRelativeBinaryOp(CNIEnv env, CandyObject operand,
	                                     String name, int mask) {
		CandyObject obj = getBoundMethod(name, mask).call(env, operand);
		return obj.boolValue(env);
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
	
	public CandyObject callAdd(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(ADD_MASK)) {
			return add(env, operand);
		}
		return callBinaryOp(env, operand, Names.METHOD_OP_ADD, ADD_MASK);
	}
	protected CandyObject add(CNIEnv env, CandyObject operand) {
		if (operand instanceof StringObj || this instanceof StringObj) {
			return callStr(env).add(env, operand);
		}
		throwUnsupportBinaryOperator("+", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_ADD, argc = 1)
	public final CandyObject addMet(CNIEnv env, CandyObject[] args) {
		return add(env, args[0]);
	}
	
	
	public CandyObject callSub(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(SUB_MASK)) {
			return sub(env, operand);
		}
		return callBinaryOp(env, operand, Names.METHOD_OP_SUB, SUB_MASK);
	}
	protected CandyObject sub(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("-", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_SUB, argc = 1)
	public final CandyObject subMet(CNIEnv env, CandyObject[] args) {
		return sub(env, args[0]);
	}
	
	
	public CandyObject callMul(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(MUL_MASK)) {
			return mul(env, operand);
		}
		return callBinaryOp(env, operand, Names.METHOD_OP_MUL, MUL_MASK);
	}
	protected CandyObject mul(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("*", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_MUL, argc = 1)
	public final CandyObject mulMet(CNIEnv env, CandyObject[] args) {
		return mul(env, args[0]);
	}
	
	
	public CandyObject callDiv(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(DIV_MASK)) {
			return div(env, operand);
		}
		return callBinaryOp(env, operand, Names.METHOD_OP_DIV, DIV_MASK);
	}
	protected CandyObject div(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("/", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_DIV, argc = 1)
	public final CandyObject divMet(CNIEnv env, CandyObject[] args) {
		return div(env, args[0]);
	}
	
	
	public CandyObject callMod(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(MOD_MASK)) {
			return mod(env, operand);
		}
		return callBinaryOp(env, operand, Names.METHOD_OP_MOD, MOD_MASK);
	}
	protected CandyObject mod(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("%", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_MOD, argc = 1)
	public final CandyObject modMet(CNIEnv env, CandyObject[] args) {
		return mod(env, args[0]);
	}
	
	
	public BoolObj callGt(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(GT_MASK)) {
			return gt(env, operand);
		}
		return callRelativeBinaryOp(env, operand, Names.METHOD_OP_GT, GT_MASK);
	}
	protected BoolObj gt(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator(">", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_GT, argc = 1)
	public final CandyObject gtMet(CNIEnv env, CandyObject[] args) {
		return gt(env, args[0]);
	}
	
	
	public BoolObj callGteq(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(GTEQ_MASK)) {
			return gteq(env, operand);
		}
		return callRelativeBinaryOp(env, operand, Names.METHOD_OP_GTEQ, GTEQ_MASK);
	}
	protected BoolObj gteq(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator(">=", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_GTEQ, argc = 1)
	public final CandyObject gteqMet(CNIEnv env, CandyObject[] args) {
		return gteq(env, args[0]);
	}
	
	
	public BoolObj callLt(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(LT_MASK)) {
			return lt(env, operand);
		}
		return callRelativeBinaryOp(env, operand, Names.METHOD_OP_LT, LT_MASK);
	}
	protected BoolObj lt(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("<", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_LT, argc = 1)
	public final CandyObject ltMet(CNIEnv env, CandyObject[] args) {
		return lt(env, args[0]);
	}
	
	
	public BoolObj callLteq(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(LTEQ_MASK)) {
			return lteq(env, operand);
		}
		return callRelativeBinaryOp(env, operand, Names.METHOD_OP_LTEQ, LTEQ_MASK);
	}
	protected BoolObj lteq(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("<=", operand);
		return null;
	}
	@NativeMethod(name = Names.METHOD_OP_LTEQ, argc = 1)
	public final CandyObject lteqMet(CNIEnv env, CandyObject[] args) {
		return lteq(env, args[0]);
	}
	
	
	public BoolObj callEquals(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(EQ_MASK)) {
			return equals(env, operand);
		}
		return getBoundMethod(Names.METHOD_EQUALS, EQ_MASK)
			.call(env, operand).boolValue(env);
	}
	public BoolObj equals(CNIEnv env, CandyObject operand) {
		return BoolObj.valueOf(this == operand);
	}
	@NativeMethod(name = Names.METHOD_EQUALS, argc = 1)
	public final CandyObject equals(CNIEnv env, CandyObject[] args) {
		return equals(env, args[0]);
	}
	
	
	public IntegerObj callHashCode(CNIEnv env) {
		if (isBuiltinMetnod(HASH_MASK)) {
			return hashCode(env);
		}
		CandyObject obj = getBoundMethod(Names.METHOD_HASH_CODE, HASH_MASK)
			.call(env);
		checkReturnedType(Names.METHOD_HASH_CODE, obj, IntegerObj.INTEGER_CLASS);
		return (IntegerObj) obj;
	}
	public IntegerObj hashCode(CNIEnv env) {
		return IntegerObj.valueOf(super.hashCode());
	}
	@NativeMethod(name = Names.METHOD_HASH_CODE)
	public final CandyObject hashCodeMethod(CNIEnv env, CandyObject[] args) {
		return hashCode(env);
	}
	
	
	public StringObj callStr(CNIEnv env) {
		if (isBuiltinMetnod(STR_MASK)) {
			return str(env);
		}
		CandyObject obj = getBoundMethod(Names.METHOD_STR_VALUE, STR_MASK)
			.call(env);
		checkReturnedType(Names.METHOD_STR_VALUE, obj, StringObj.STRING_CLASS);
		return obj.str(env);
	}
	public StringObj str(CNIEnv env) {
		return StringObj.valueOf(this.toString());
	}
	@NativeMethod(name = Names.METHOD_STR_VALUE)
	public final CandyObject strMet(CNIEnv env, CandyObject[] args) {
		return str(env);
	}
	@Override
	public String toString() {
		return ObjectHelper.toString(
			getCandyClassName(), "hash - " + Integer.toHexString(hashCode())
		);
	}
	
	
	public CandyObject callIterator(CNIEnv env) {
		if (isBuiltinMetnod(ITERATOR_MASK)) {
			return iterator(env);
		}
		return getBoundMethod(Names.METHOD_ITERATOR, ITERATOR_MASK)
			.call(env);
	}
	public CandyObject iterator(CNIEnv env) {
		new TypeError("the object is not iterable.")
			.throwSelfNative();
		return null;
	}
	@NativeMethod(name = Names.METHOD_ITERATOR)
	public final CandyObject iteratorMet(CNIEnv env, CandyObject[] args) {
		return iterator(env);
	}
	
	
	public BoolObj not(CNIEnv env) { 
		return boolValue(env).not(env); 
	}
	public BoolObj boolValue(CNIEnv env) {	
		return BoolObj.TRUE;
	}
	

	@NativeMethod(name = Names.METHOD_INITALIZER)
	public final CandyObject objDefaultInitializer(CNIEnv env, CandyObject[] args) { 
		return this; 
	}

	@NativeMethod(name = "_class")
	public final CandyObject getClass(CNIEnv env, CandyObject[] args) {
		return getCandyClass();
	}
}
