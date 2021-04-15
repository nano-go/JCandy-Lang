package com.nano.candy.interpreter.i2.rtda.chunk;
import com.nano.candy.interpreter.i2.rtda.chunk.attrs.CodeAttribute;
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
		public byte[] upvalues;
		public ClassInfo classDefinedIn;
		public CodeAttribute attrs;
		
		public MethodInfo() {}
		
		public int getFromPC() {
			return attrs.fromPc;
		}
		
		public int getLength() {
			return attrs.length;
		}
		
		public int getMaxStack() {
			return attrs.maxStack;
		}
		
		public int getMaxLocal() {
			return attrs.maxLocal;
		}
		
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
				name, arity, attrs.maxLocal, attrs.length
			);
		}
	}
	
	public static class ClassInfo extends ConstantValue {
		
		public int fromPC;
		public String className;
		public boolean hasSuperClass;
		public Optional<MethodInfo> initializer;
		public MethodInfo[] methods;
		
		private int length;
		
		public int getLength() {
			if (this.length != 0) {
				return this.length;
			}
			int length = 0;
			if (initializer.isPresent()) {
				length += initializer.get().getLength();
			}
			for (MethodInfo method : methods) {
				length += method.getLength();
			}
			return this.length = length;
		}
		
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
