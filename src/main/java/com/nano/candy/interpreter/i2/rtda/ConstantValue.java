package com.nano.candy.interpreter.i2.rtda;
import java.util.Optional;


public abstract class ConstantValue {
	
	public abstract String headName();
	
	public static class ConstantDouble extends ConstantValue {
		double float64;
		public ConstantDouble(double float64) {
			this.float64 = float64;
		}
		
		public double value() {
			return float64;
		}

		@Override
		public String headName() {
			return "Double";
		}

		@Override
		public String toString() {
			return String.valueOf(float64);
		}
	}
	
	public static class ConstantInteger extends ConstantValue {
		long integer;
		public ConstantInteger(long integer) {
			this.integer = integer;
		}
		
		public long value() {
			return integer;
		}

		@Override
		public String headName() {
			return "Integer";
		}
		
		@Override
		public String toString() {
			return String.valueOf(integer);
		}
	}
	
	public static class ConstantUtf8String extends ConstantValue {
		String str;
		public ConstantUtf8String(String str) {
			this.str = str;
		}
		
		public String value() {
			return str;
		}

		@Override
		public String headName() {
			return "UTF-8";
		}
		
		@Override
		public String toString() {
			return str;
		}
	}
	
	public static class MethodInfo extends ConstantValue {

		public String name;
		public int arity;
		public byte slots;
		public int stackSize;
		public int codeBytes;
		public byte[] upvalues;
		
		public MethodInfo() {}
		
		public int upvalueCount() {
			return upvalues.length / 2;
		}
		
		public boolean isLocal(int offset) {
			return upvalues[offset*2] != 0;
		}
		
		public int upvalueIndex(int offset) {
			return upvalues[offset*2 + 1];
		}

		@Override
		public String headName() {
			return "Method Info";
		}

		@Override
		public String toString() {
			return String.format(
				"(Name: %s, Arity: %d, Slots: %d, Length %d)",
				name, arity, slots, codeBytes
			);
		}
	}
	
	public static class ClassInfo extends ConstantValue {
		
		public String className;
		public boolean hasSuperClass;
		public Optional<MethodInfo> initializer;
		public MethodInfo[] methods;
		
		@Override
		public String headName() {
			return "Class Info";
		}

		@Override
		public String toString() {
			return className;
		}
	}
}
