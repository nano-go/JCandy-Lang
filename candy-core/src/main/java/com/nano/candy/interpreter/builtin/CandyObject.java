package com.nano.candy.interpreter.builtin;

import com.nano.candy.interpreter.builtin.type.BoolObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.MethodObj;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.error.AttributeError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.HashSymbolTable;
import com.nano.candy.interpreter.builtin.utils.ObjAttribute;
import com.nano.candy.interpreter.builtin.utils.ObjectHelper;
import com.nano.candy.interpreter.builtin.utils.SymbolTable;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeMethod;
import com.nano.candy.std.AttributeModifiers;
import com.nano.candy.std.CandyAttrSymbol;
import com.nano.candy.std.Names;
import java.util.Set;

/**
 * Candy objects are Java objects allocated on the heap. 
 *
 * <p>In Candy language, everything is an object, including numbers, classes, 
 * functions and modules...
 * 
 * <p>There is a Candy class for each object and the Candy class is also an object which 
 * is an instance of the `Callable` class.
 *
 * <p>The {@code NativeClass} annotation helps us to create a CandyClass object 
 * correspoding to a Java class. For more details see the {@code candy-cni-processor} 
 * module and {@link com.nano.candy.interpreter.cni.NativeClassRegister}.
 */
@NativeClass(name = "Object", isInheritable = true)
public class CandyObject {
	
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
	private static final int LSHIFT_MASK = 1 << 16;
	private static final int RSHIFT_MASK = 1 << 17;
	private static final int EQ_MASK = 1 << 18;
	private static final int HASH_MASK = 1 << 19;
	private static final int STR_MASK = 1 << 20;
	private static final int ITERATOR_MASK = 1 << 21;
	
	private SymbolTable metaData = SymbolTable.empty();
	
	/**
	 * Each object has a Candy class object.
	 *
	 * <p>Note that the {@link com.nano.candy.interpreter.builtin.type.CallableObj}
	 * is a special class which its class object it itself.
	 */
	private CandyClass klass;
	
	/**
	 * If an object is frozen, its attributes can not be changed.
	 */
	private boolean frozen;
	
	/**
	 * (builtinMethodFlags & methodMask) means that the method is 
	 * a built-in method.
	 */
	private int builtinMethodFlags;
	
	/**
	 * If the constructor with no-args is present, the object can
	 * be created in the Candy language.
	 *
	 * <p> For Example:
	 * 
	 * <pre>{@code
	 * var instanceA = Object() // create an object. 
	 * }</pre>
	 *
	 * <p> This constructor is reflectly called by the CandyClass.
	 */
	protected CandyObject() {
		this(null);
	}

	public CandyObject(CandyClass klass) {
		this.klass = klass;
	}
	
	/**
	 * This method is provided for the Candy class.
	 */
	protected final void setCandyClass(CandyClass klass) {
		this.klass = klass;
	}
	
	protected CandyClass initSelfCandyClass() {
		return null;
	}
	
	/**
	 * Returns the class of this object.
	 */
	public final CandyClass getCandyClass() {
		if (klass == null) klass = initSelfCandyClass();
		return klass;
	}
	
	/**
	 * Returns the class name of this object.
	 */
	public final String getCandyClassName() {
		return getCandyClass().getName();
	}
	
	/**
	 * Returns {@code true} if this object is a class, otherwise {@false}.
	 */
	public final boolean isCandyClass() {
		return this instanceof CandyClass;
	}
	
	/**
	 * Freezes this object. A frozen object can not be changed.
	 */
	public final void freeze() {
		this.frozen = true;
	}

	/**
	 * Returns {@code true} this object is frozen, otherwise {@code false}.
	 */
	public final boolean frozen() {
		return frozen;
	}
	
	/**
	 * If this object is frozen, An attribute error is thrown.
	 */
	public final void checkFrozen() {
		if (frozen) {
			new AttributeError("The frozen object can't be changed.")
				.throwSelfNative();
		}
	}
	
	/**
	 * Returns {@code true} this object is callable, otherwise {@code false}.
	 */
	public boolean isCallable() {
		return false;
	}
	
	/**
	 * Returns {@code true} if this object is an instance of the specified
	 * class, otherwise {@code false}.
	 *
	 * <p>Note that if the specified object is not a class, A type error
	 * will be thrown.
	 */
	public boolean isInstanceOf(CandyObject klass) {
		return getCandyClass().isSubClassOf(TypeError.requiresClass(klass));
	}
	
	/**
	 * Returns the number of attributes in this object.
	 */
	public int getMetaDataSize() {
		return this.metaData.size();
	}
	
	/**
	 * Associates the specified name with the specified value.
	 *
	 * <p> If the attribute(name) is present, the attribute's modifiers will not be 
	 * changed.
	 *
	 * <p> If the attribute is not present, A new attribute with the modifiers
	 * {@code PUBLIC} will be inserted.
	 */
	public void setMetaData(String name, CandyObject value) {
		if (metaData.isEmpty()) {
			metaData = providesSymbolTable();
		}
		metaData.put(name, value);
	}
	
	public void setMetaData(String name, CandyObject value, byte modifiers) {
		if (metaData.isEmpty()) {
			metaData = providesSymbolTable();
		}
		metaData.putWithModfiers(name, value, modifiers);
	}
	
	public void setBuiltinMetaData(String name, CandyObject value) {
		setMetaData(name, value, AttributeModifiers.BUILTIN);
	}
	
	public CandyObject addAttrs(Set<CandyAttrSymbol> attrs) {
		if (attrs.isEmpty()) {
			return this;
		}
		if (metaData.isEmpty()) {
			metaData = providesSymbolTable();
		}
		metaData.putAll(attrs);
		return this;
	}
	
	protected SymbolTable providesSymbolTable() {
		return new HashSymbolTable();
	}
	
	public CandyObject getMetaData(String name) {
		return metaData.get(name);
	}
	
	public CandyObject removeMetaData(String name) {
		return metaData.remove(name);
	}
	
	
	/************************** Native Methods **************************/
	
	@NativeMethod(name = "isClass")
	protected CandyObject isClass(CNIEnv env) {
		return BoolObj.valueOf(isCandyClass());
	}
	
	@NativeMethod(name = "isCallable")
	protected CandyObject isCallable(CNIEnv env) {
		return BoolObj.valueOf(isCallable());
	}
	
	@NativeMethod(name = "freeze")
	protected CandyObject freeze(CNIEnv env) {
		freeze();
		return null;
	}

	@NativeMethod(name = "frozen")
	protected CandyObject frozen(CNIEnv env) {
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
			return ObjectHelper.preventNull(setAttrMet(env, name, value));
		}
		checkFrozen();
		return getBoundMethod(Names.METHOD_SET_ATTR, SET_ATTR_MASK)
			.call(env, StringObj.valueOf(name), value);
	}
	protected CandyObject setAttr(CNIEnv env, String name, CandyObject value) {
		boolean isAccessedFromThis = AttributeModifiers.isAccessedFromThis(name);
		if (isAccessedFromThis) {
			name = AttributeModifiers.getAttrNameIfAccessedFromThis(name);
		}
		ObjAttribute attr = metaData.getAttr(name);
		if (attr == null) {
			setMetaData(name, value);
			return value;
		}
		byte modifiers = attr.getModifiers();
		if (AttributeModifiers.isBuiltin(modifiers)) {
			AttributeError.throwReadOnlyError(name);
		}
		value = ObjectHelper.preventNull(value);
		if (isAccessedFromThis) {
			// Attributes modified by any modifiers can be changed from
			// 'this'(keyword) except 'builtin'.
			attr.setValue(value);
			return value;
		}
		if (AttributeModifiers.isPrivate(modifiers)) {
			AttributeError.throwHasNoAttr(this, name);
		}
		if (AttributeModifiers.isReadOnly(modifiers)) {
			AttributeError.throwReadOnlyError(name);
		}
		attr.setValue(value);
		return value;
	}
	@NativeMethod(name = Names.METHOD_SET_ATTR)
	public final CandyObject setAttrMet(CNIEnv env, String name, CandyObject value) {
		checkFrozen();
		return setAttr(env, name, value);
	}

	
	public final CandyObject callGetAttr(CNIEnv env, String name) {
		if (isBuiltinMetnod(GET_ATTR_MASK)) {
			return getAttrMet(env, name);
		}
		return getBoundMethod(Names.METHOD_GET_ATTR, GET_ATTR_MASK)
			.call(env, StringObj.valueOf(name));
	}
	protected CandyObject getAttr(CNIEnv env, String name) {
		boolean isAccessFromThis = AttributeModifiers.isAccessedFromThis(name);
		if (isAccessFromThis) {
			name = AttributeModifiers.getAttrNameIfAccessedFromThis(name);
		}
		ObjAttribute attr = metaData.getAttr(name);
		if (attr == null) {
			return getCandyClass().getBoundMethod(name, this);
		}
		if (isAccessFromThis) { 
			// 'this' keyword has the highest access permission.
			return attr.getValue();
		}
		byte modifiers = attr.getModifiers();
		if (AttributeModifiers.isPrivate(modifiers)) {
			AttributeError.throwHasNoAttr(this, name);
		}
		if (AttributeModifiers.isWriteOnly(modifiers)) {
			AttributeError.throwWriteOnlyError(name);
		}
		return attr.getValue();
	}
	@NativeMethod(name = Names.METHOD_GET_ATTR)
	protected final CandyObject getAttrMet(CNIEnv env, String name) {
		CandyObject val = getAttr(env, name);
		if (val != null) {
			return val; 
		}
		return callGetUnknownAttr(env, name);
	}
	
	
	public CandyObject callGetUnknownAttr(CNIEnv env, String name) {
		if (isBuiltinMetnod(GET_UNKNOWN_ATTR_MASK)) {
			return ObjectHelper.preventNull(getUnknownAttr(env, name));
		}
		return getBoundMethod(Names.METHOD_GET_UNKNOWN_ATTR, GET_UNKNOWN_ATTR_MASK)
			.call(env, StringObj.valueOf(name));
	}
	@NativeMethod(name = Names.METHOD_GET_UNKNOWN_ATTR)
	public CandyObject getUnknownAttr(CNIEnv env, String name) {
		AttributeError.throwHasNoAttr(this, name);
		return null;
	}
	
	
	public CandyObject callSetItem(CNIEnv env, CandyObject key, CandyObject value) {
		if (isBuiltinMetnod(SET_ITEM_MASK)) {
			return ObjectHelper.preventNull(setItem(env, key, value));
		}
		checkFrozen();
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
	
	@NativeMethod(name = Names.METHOD_SET_ITEM)
	protected final CandyObject setItemMet(CNIEnv env, CandyObject key, CandyObject value) {
		checkFrozen();
		return setItem(env, key, value);
	}
	
	
	public CandyObject callGetItem(CNIEnv env, CandyObject key) {
		if (isBuiltinMetnod(GET_ITEM_MASK)) {
			return ObjectHelper.preventNull(getItem(env, key));
		}
		return getBoundMethod(Names.METHOD_GET_ITEM, GET_ITEM_MASK)
			.call(env, key);
	}
	@NativeMethod(name = Names.METHOD_GET_ITEM)
	protected CandyObject getItem(CNIEnv env, CandyObject key) {
		new TypeError(
			"'%s'['%s']", getCandyClassName(), key.getCandyClassName()
		).throwSelfNative();
		return null;
	}
	
	
	public CandyObject callPositive(CNIEnv env) {
		if (isBuiltinMetnod(POSTIVE_MASK)) {
			return ObjectHelper.preventNull(positive(env));
		}
		return getBoundMethod(Names.METHOD_OP_POSITIVE, POSTIVE_MASK)
			.call(env);
	}
	@NativeMethod(name = Names.METHOD_OP_POSITIVE)
	public CandyObject positive(CNIEnv env) {
		new TypeError("+").throwSelfNative();
		return null;
	}

	
	public CandyObject callNegative(CNIEnv env) {
		if (isBuiltinMetnod(NEGATIVE_MASK)) {
			return ObjectHelper.preventNull(negative(env));
		}
		return getBoundMethod(Names.METHOD_OP_NEGATIVE, NEGATIVE_MASK)
			.call(env);
	}
	@NativeMethod(name = Names.METHOD_OP_NEGATIVE)
	protected CandyObject negative(CNIEnv env) {
		new TypeError("-").throwSelfNative();
		return null;
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
			return ObjectHelper.preventNull(add(env, operand));
		}
		return callBinaryOp(env, operand, Names.METHOD_OP_ADD, ADD_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_ADD)
	protected CandyObject add(CNIEnv env, CandyObject operand) {
		if (operand instanceof StringObj || this instanceof StringObj) {
			return callStr(env).add(env, operand);
		}
		throwUnsupportBinaryOperator("+", operand);
		return null;
	}
	
	public CandyObject callSub(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(SUB_MASK)) {
			return ObjectHelper.preventNull(sub(env, operand));
		}
		return callBinaryOp(env, operand, Names.METHOD_OP_SUB, SUB_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_SUB)
	protected CandyObject sub(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("-", operand);
		return null;
	}
	
	
	public CandyObject callMul(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(MUL_MASK)) {
			return ObjectHelper.preventNull(mul(env, operand));
		}
		return callBinaryOp(env, operand, Names.METHOD_OP_MUL, MUL_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_MUL)
	protected CandyObject mul(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("*", operand);
		return null;
	}
	
	
	public CandyObject callDiv(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(DIV_MASK)) {
			return ObjectHelper.preventNull(div(env, operand));
		}
		return callBinaryOp(env, operand, Names.METHOD_OP_DIV, DIV_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_DIV)
	protected CandyObject div(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("/", operand);
		return null;
	}
	
	
	public CandyObject callMod(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(MOD_MASK)) {
			return mod(env, operand);
		}
		return callBinaryOp(env, operand, Names.METHOD_OP_MOD, MOD_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_MOD)
	protected CandyObject mod(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("%", operand);
		return null;
	}
	
	public CandyObject callRShift(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(RSHIFT_MASK)) {
			return ObjectHelper.preventNull(rshift(env, operand));
		}
		return callBinaryOp(
			env, operand, Names.METHOD_OP_RSHIFT, RSHIFT_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_RSHIFT)
	protected CandyObject rshift(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator(">>", operand);
		return null;
	}
	
	public CandyObject callLShift(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(LSHIFT_MASK)) {
			return ObjectHelper.preventNull(lshift(env, operand));
		}
		return callBinaryOp(
			env, operand, Names.METHOD_OP_LSHIFT, LSHIFT_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_LSHIFT)
	protected CandyObject lshift(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("<<", operand);
		return null;
	}
	
	
	public static BoolObj preventNullForBool(BoolObj obj) {
		return obj == null ? BoolObj.FALSE : obj;
	}
	
	public BoolObj callGt(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(GT_MASK)) {
			return preventNullForBool(gt(env, operand));
		}
		return callRelativeBinaryOp(env, operand, Names.METHOD_OP_GT, GT_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_GT)
	protected BoolObj gt(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator(">", operand);
		return null;
	}
	
	
	public BoolObj callGteq(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(GTEQ_MASK)) {
			return preventNullForBool(gteq(env, operand));
		}
		return callRelativeBinaryOp(env, operand, Names.METHOD_OP_GTEQ, GTEQ_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_GTEQ)
	protected BoolObj gteq(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator(">=", operand);
		return null;
	}
	
	
	public BoolObj callLt(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(LT_MASK)) {
			return preventNullForBool(lt(env, operand));
		}
		return callRelativeBinaryOp(env, operand, Names.METHOD_OP_LT, LT_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_LT)
	protected BoolObj lt(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("<", operand);
		return null;
	}
	
	
	public BoolObj callLteq(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(LTEQ_MASK)) {
			return preventNullForBool(lteq(env, operand));
		}
		return callRelativeBinaryOp(env, operand, Names.METHOD_OP_LTEQ, LTEQ_MASK);
	}
	@NativeMethod(name = Names.METHOD_OP_LTEQ)
	protected BoolObj lteq(CNIEnv env, CandyObject operand) {
		throwUnsupportBinaryOperator("<=", operand);
		return null;
	}
	
	public BoolObj callEquals(CNIEnv env, CandyObject operand) {
		if (isBuiltinMetnod(EQ_MASK)) {
			return preventNullForBool(equals(env, operand));
		}
		return getBoundMethod(Names.METHOD_EQUALS, EQ_MASK)
			.call(env, operand).boolValue(env);
	}
	@NativeMethod(name = Names.METHOD_EQUALS)
	protected BoolObj equals(CNIEnv env, CandyObject operand) {
		return BoolObj.valueOf(this == operand);
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
	/**
	 * Note that the return value can not be {@code null}.
	 */
	@NativeMethod(name = Names.METHOD_HASH_CODE)
	protected IntegerObj hashCode(CNIEnv env) {
		return IntegerObj.valueOf(super.hashCode());
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
	/**
	 * Note that the return value can not be {@code null}.
	 */
	@NativeMethod(name = Names.METHOD_STR_VALUE)
	protected StringObj str(CNIEnv env) {
		return StringObj.valueOf(this.toString());
	}
	@Override
	public String toString() {
		return ObjectHelper.toString(
			getCandyClassName(), "hash - " + Integer.toHexString(hashCode())
		);
	}
	
	
	public CandyObject callIterator(CNIEnv env) {
		if (isBuiltinMetnod(ITERATOR_MASK)) {
			return ObjectHelper.preventNull(iterator(env));
		}
		return getBoundMethod(Names.METHOD_ITERATOR, ITERATOR_MASK)
			.call(env);
	}
	@NativeMethod(name = Names.METHOD_ITERATOR)
	protected CandyObject iterator(CNIEnv env) {
		new TypeError("the object is not iterable.")
			.throwSelfNative();
		return null;
	}
	
	
	public BoolObj not(CNIEnv env) { 
		return boolValue(env).not(env); 
	}
	public BoolObj boolValue(CNIEnv env) {	
		return BoolObj.TRUE;
	}
	

	@NativeMethod(name = Names.METHOD_INITALIZER)
	protected final CandyObject objDefaultInitializer(CNIEnv env) { 
		return this; 
	}

	@NativeMethod(name = "_class")
	protected final CandyObject getClass(CNIEnv env) {
		return getCandyClass();
	}
}
