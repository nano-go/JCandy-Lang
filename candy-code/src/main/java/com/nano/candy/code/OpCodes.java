package com.nano.candy.code;

public class OpCodes {
	public static final byte OP_NOP                       = (byte)0;

	public static final byte OP_POP                       = (byte)1;
	public static final byte OP_DUP                       = (byte)2;
	public static final byte OP_DUP_2                     = (byte)3;
	public static final byte OP_ROT_2                     = (byte)4;
	public static final byte OP_ROT_3                     = (byte)5;

	public static final byte OP_DCONST                    = (byte)6;
	public static final byte OP_ICONST                    = (byte)7;
	public static final byte OP_SCONST                    = (byte)8;
	public static final byte OP_FALSE                     = (byte)9;
	public static final byte OP_TRUE                      = (byte)10;
	public static final byte OP_NULL                      = (byte)11;

	public static final byte OP_NEGATIVE                  = (byte)12;
	public static final byte OP_POSITIVE                  = (byte)13;
	public static final byte OP_NOT                       = (byte)14;

	public static final byte OP_ADD                       = (byte)15;
	public static final byte OP_SUB                       = (byte)16;
	public static final byte OP_MUL                       = (byte)17;
	public static final byte OP_DIV                       = (byte)18;
	public static final byte OP_MOD                       = (byte)19;
	public static final byte OP_INSTANCE_OF               = (byte)20;

	public static final byte OP_RANGE                     = (byte)21;

	public static final byte OP_EQ                        = (byte)22;
	public static final byte OP_NOTEQ                     = (byte)23;
	public static final byte OP_GT                        = (byte)24;
	public static final byte OP_GTEQ                      = (byte)25;
	public static final byte OP_LT                        = (byte)26;
	public static final byte OP_LTEQ                      = (byte)27;

	public static final byte OP_LS                        = (byte)28;
	public static final byte OP_RS                        = (byte)29;

	public static final byte OP_POP_JUMP_IF_FALSE         = (byte)30;
	public static final byte OP_POP_JUMP_IF_TRUE          = (byte)31;
	public static final byte OP_POP_JUMP_IF_NOT_UNDEFINED = (byte)32;
	public static final byte OP_JUMP_IF_FALSE             = (byte)33;
	public static final byte OP_JUMP_IF_TRUE              = (byte)34;
	public static final byte OP_JUMP                      = (byte)35;
	public static final byte OP_LOOP                      = (byte)36;

	public static final byte OP_LOAD                      = (byte)37;
	public static final byte OP_LOAD0                     = (byte)38;
	public static final byte OP_LOAD1                     = (byte)39;
	public static final byte OP_LOAD2                     = (byte)40;
	public static final byte OP_LOAD3                     = (byte)41;
	public static final byte OP_LOAD4                     = (byte)42;
	public static final byte OP_STORE                     = (byte)43;
	public static final byte OP_STORE0                    = (byte)44;
	public static final byte OP_STORE1                    = (byte)45;
	public static final byte OP_STORE2                    = (byte)46;
	public static final byte OP_STORE3                    = (byte)47;
	public static final byte OP_STORE4                    = (byte)48;
	public static final byte OP_POP_STORE                 = (byte)49;
	public static final byte OP_LOAD_UPVALUE              = (byte)50;
	public static final byte OP_STORE_UPVALUE             = (byte)51;

	public static final byte OP_GET_ATTR                  = (byte)52;
	public static final byte OP_SET_ATTR                  = (byte)53;
	public static final byte OP_GET_ITEM                  = (byte)54;
	public static final byte OP_SET_ITEM                  = (byte)55;

	public static final byte OP_CLOSE                     = (byte)56;

	public static final byte OP_GLOBAL_DEFINE             = (byte)57;
	public static final byte OP_GLOBAL_SET                = (byte)58;
	public static final byte OP_GLOBAL_GET                = (byte)59;

	public static final byte OP_INVOKE                    = (byte)60;
	public static final byte OP_CALL_GLOBAL               = (byte)61;
	public static final byte OP_CALL_SLOT                 = (byte)62;
	public static final byte OP_CALL_EX                   = (byte)63;
	public static final byte OP_CALL                      = (byte)64;

	public static final byte OP_RETURN_NIL                = (byte)65;
	public static final byte OP_RETURN                    = (byte)66;

	public static final byte OP_CLASS                     = (byte)67;
	public static final byte OP_SUPER_GET                 = (byte)68;
	public static final byte OP_SUPER_INVOKE              = (byte)69;
	public static final byte OP_FUN                       = (byte)70;

	public static final byte OP_RAISE                     = (byte)71;
	public static final byte OP_MATCH_ERRORS              = (byte)72;

	public static final byte OP_IMPORT_NAME               = (byte)73;
	public static final byte OP_IMPORT                    = (byte)74;

	public static final byte OP_NEW_ARRAY                 = (byte)75;
	public static final byte OP_BUILT_TUPLE               = (byte)76;
	public static final byte OP_APPEND                    = (byte)77;
	public static final byte OP_NEW_MAP                   = (byte)78;
	public static final byte OP_PUT                       = (byte)79;
	public static final byte OP_ASSERT                    = (byte)80;
	public static final byte OP_PRINT                     = (byte)81;

	public static final byte OP_EXIT                      = (byte)82;

	public static final byte INSTRUCTION_NUMBER = 83;

	public static final String[] INSTRUCTION_NAMES = new String[83];
	static {
		INSTRUCTION_NAMES[OP_NOP]                       = "nop";
		INSTRUCTION_NAMES[OP_POP]                       = "pop";
		INSTRUCTION_NAMES[OP_DUP]                       = "dup";
		INSTRUCTION_NAMES[OP_DUP_2]                     = "dup_2";
		INSTRUCTION_NAMES[OP_ROT_2]                     = "rot_2";
		INSTRUCTION_NAMES[OP_ROT_3]                     = "rot_3";
		INSTRUCTION_NAMES[OP_DCONST]                    = "dconst";
		INSTRUCTION_NAMES[OP_ICONST]                    = "iconst";
		INSTRUCTION_NAMES[OP_SCONST]                    = "sconst";
		INSTRUCTION_NAMES[OP_FALSE]                     = "const_false";
		INSTRUCTION_NAMES[OP_TRUE]                      = "const_true";
		INSTRUCTION_NAMES[OP_NULL]                      = "const_null";
		INSTRUCTION_NAMES[OP_NEGATIVE]                  = "negative";
		INSTRUCTION_NAMES[OP_POSITIVE]                  = "positive";
		INSTRUCTION_NAMES[OP_NOT]                       = "not";
		INSTRUCTION_NAMES[OP_ADD]                       = "add";
		INSTRUCTION_NAMES[OP_SUB]                       = "sub";
		INSTRUCTION_NAMES[OP_MUL]                       = "mul";
		INSTRUCTION_NAMES[OP_DIV]                       = "div";
		INSTRUCTION_NAMES[OP_MOD]                       = "mod";
		INSTRUCTION_NAMES[OP_INSTANCE_OF]               = "instanceof";
		INSTRUCTION_NAMES[OP_RANGE]                     = "range";
		INSTRUCTION_NAMES[OP_EQ]                        = "cmpeq";
		INSTRUCTION_NAMES[OP_NOTEQ]                     = "cmpneq";
		INSTRUCTION_NAMES[OP_GT]                        = "cmpgt";
		INSTRUCTION_NAMES[OP_GTEQ]                      = "cmpgteq";
		INSTRUCTION_NAMES[OP_LT]                        = "cmplt";
		INSTRUCTION_NAMES[OP_LTEQ]                      = "cmplteq";
		INSTRUCTION_NAMES[OP_LS]                        = "left_shift";
		INSTRUCTION_NAMES[OP_RS]                        = "right_shift";
		INSTRUCTION_NAMES[OP_POP_JUMP_IF_FALSE]         = "pop_jump_if_false";
		INSTRUCTION_NAMES[OP_POP_JUMP_IF_TRUE]          = "pop_jump_if_true";
		INSTRUCTION_NAMES[OP_POP_JUMP_IF_NOT_UNDEFINED] = "pop_jump_if_not_undefined";
		INSTRUCTION_NAMES[OP_JUMP_IF_FALSE]             = "jump_if_false";
		INSTRUCTION_NAMES[OP_JUMP_IF_TRUE]              = "jump_if_true";
		INSTRUCTION_NAMES[OP_JUMP]                      = "jump";
		INSTRUCTION_NAMES[OP_LOOP]                      = "loop";
		INSTRUCTION_NAMES[OP_LOAD]                      = "load";
		INSTRUCTION_NAMES[OP_LOAD0]                     = "load0";
		INSTRUCTION_NAMES[OP_LOAD1]                     = "load1";
		INSTRUCTION_NAMES[OP_LOAD2]                     = "load2";
		INSTRUCTION_NAMES[OP_LOAD3]                     = "load3";
		INSTRUCTION_NAMES[OP_LOAD4]                     = "load4";
		INSTRUCTION_NAMES[OP_STORE]                     = "store";
		INSTRUCTION_NAMES[OP_STORE0]                    = "store0";
		INSTRUCTION_NAMES[OP_STORE1]                    = "store1";
		INSTRUCTION_NAMES[OP_STORE2]                    = "store2";
		INSTRUCTION_NAMES[OP_STORE3]                    = "store3";
		INSTRUCTION_NAMES[OP_STORE4]                    = "store4";
		INSTRUCTION_NAMES[OP_POP_STORE]                 = "pop_store";
		INSTRUCTION_NAMES[OP_LOAD_UPVALUE]              = "load_upvalue";
		INSTRUCTION_NAMES[OP_STORE_UPVALUE]             = "store_upvalue";
		INSTRUCTION_NAMES[OP_GET_ATTR]                  = "get_attr";
		INSTRUCTION_NAMES[OP_SET_ATTR]                  = "set_attr";
		INSTRUCTION_NAMES[OP_GET_ITEM]                  = "get_item";
		INSTRUCTION_NAMES[OP_SET_ITEM]                  = "set_item";
		INSTRUCTION_NAMES[OP_CLOSE]                     = "close";
		INSTRUCTION_NAMES[OP_GLOBAL_DEFINE]             = "global_define";
		INSTRUCTION_NAMES[OP_GLOBAL_SET]                = "globalset";
		INSTRUCTION_NAMES[OP_GLOBAL_GET]                = "globalget";
		INSTRUCTION_NAMES[OP_INVOKE]                    = "invoke";
		INSTRUCTION_NAMES[OP_CALL_GLOBAL]               = "call_global_var";
		INSTRUCTION_NAMES[OP_CALL_SLOT]                 = "call_slot";
		INSTRUCTION_NAMES[OP_CALL_EX]                   = "call_ex";
		INSTRUCTION_NAMES[OP_CALL]                      = "call";
		INSTRUCTION_NAMES[OP_RETURN_NIL]                = "return_nil";
		INSTRUCTION_NAMES[OP_RETURN]                    = "return";
		INSTRUCTION_NAMES[OP_CLASS]                     = "class";
		INSTRUCTION_NAMES[OP_SUPER_GET]                 = "super_get";
		INSTRUCTION_NAMES[OP_SUPER_INVOKE]              = "super_invoke";
		INSTRUCTION_NAMES[OP_FUN]                       = "fun";
		INSTRUCTION_NAMES[OP_RAISE]                     = "raise";
		INSTRUCTION_NAMES[OP_MATCH_ERRORS]              = "match_errors";
		INSTRUCTION_NAMES[OP_IMPORT_NAME]               = "import_name";
		INSTRUCTION_NAMES[OP_IMPORT]                    = "import";
		INSTRUCTION_NAMES[OP_NEW_ARRAY]                 = "new_array";
		INSTRUCTION_NAMES[OP_BUILT_TUPLE]               = "built_tuple";
		INSTRUCTION_NAMES[OP_APPEND]                    = "append";
		INSTRUCTION_NAMES[OP_NEW_MAP]                   = "new_map";
		INSTRUCTION_NAMES[OP_PUT]                       = "put";
		INSTRUCTION_NAMES[OP_ASSERT]                    = "assert";
		INSTRUCTION_NAMES[OP_PRINT]                     = "print";
		INSTRUCTION_NAMES[OP_EXIT]                      = "exit";
	}

	public static String getName(byte opcode) {
		return INSTRUCTION_NAMES[opcode];
	}
}
