package com.nano.candy.interpreter.i2.codegen;
import com.nano.candy.interpreter.i2.error.CandyRuntimeError;
import com.nano.candy.utils.ArrayUtils;
import java.util.ArrayList;
import java.util.List;

public class LocalsTable {
	
	public final static class Local {
		protected String name;
		protected int deepth;
		protected boolean isCaptured;
		public Local(String name, int deepth) {
			this.name = name;
			this.deepth = deepth;
		}
	}
	
	/**
	 * Captured variable from closure.
	 */
	public final static class Upvalue {
		public int slot;
		public boolean isLocal;
		
		public Upvalue(int slot, boolean isLocal) {
			this.slot = slot;
			this.isLocal = isLocal;
		}
	}
	
	protected LocalsTable enclosing;
	protected int deepth;
	
	protected Local[] locals;
	protected int localCount;
	private int maxLocalCount;
	
	private ArrayList<Upvalue> upvalues;
	
	public LocalsTable() {
		locals = new Local[8];
		upvalues = new ArrayList<>();
	}
	
	public boolean isInGlobal() {
		return deepth == 0;
	}
	
	public int maxSlotCount() {
		return maxLocalCount;
	}
	
	public int upvalueCount() {
		return upvalues.size();
	}
	
	public Upvalue[] upvalues() {
		return upvalues.toArray(new Upvalue[upvalues.size()]);
	}
	
	public int addLocal(String name) {
		if (localCount >= Byte.MAX_VALUE) {
			throw new CandyRuntimeError("Too many local variables.");
		}
		locals = ArrayUtils.growCapacity(locals, localCount);
		locals[localCount ++] = new Local(name, deepth);
		maxLocalCount = Math.max(localCount, maxLocalCount);
		return localCount - 1;
	}
	
	/**
	 * Resolve the given named variable in the current scope.
	 *
	 * @return the slot of the given named variable or -1 if not found.
	 */
	public int resolveLocalInCurrentDeepth(String name) {
		for (int i = localCount-1; i >=0; i --) {
			if (deepth != locals[i].deepth) {
				return -1;
			}
			if (name.equals(locals[i].name)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Resolve the given named variable.
	 *
	 * @return the slot of the given named variable or -1 if not found.
	 */
	public int resolveLocal(String name) {
		for (int i = localCount-1; i >=0; i --) {
			if (name.equals(locals[i].name)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Resolve the named local variable from enclosing.
	 *
	 * @return the slot of the upvalue or -1 if the variable not found.
	 */
	public int resolveUpvalue(String name) {
		if (enclosing == null) {
			return -1;
		}
		int local = enclosing.resolveLocal(name);
		if (local != -1) {
			enclosing.locals[local].isCaptured = true;
			return addUpvalue(local, true);
		}
		
		// the name maybe in out of enclosing.
		int upvalue = enclosing.resolveUpvalue(name);
		if (upvalue != -1) {
			return addUpvalue(upvalue, false);
		}
		return -1;
	}
	
	private int addUpvalue(int slot, boolean isLocal) {
		final int LEN = upvalues.size();
		for (int i = 0; i < LEN; i ++) {
			Upvalue upvalue = upvalues.get(i);
			if (upvalue.slot == slot && upvalue.isLocal == isLocal) {
				return i;
			}
		}
		upvalues.add(new Upvalue(slot, isLocal));
		return upvalues.size()-1;
	}
	
	public void enterScope() {
		deepth ++;
	}
	
	/**
	 * Clears all the slots created in current scope and returns it.
	 */
	public List<Local> exitScope() {
		ArrayList<Local> discardedSlots = new ArrayList<>();
		deepth --;
		for (; localCount > 0; localCount --) {
			int i = localCount-1;
			if (locals[i].deepth <= deepth) {
				break;
			}
			discardedSlots.add(locals[i]);
			locals[i] = null;
		}
		return discardedSlots;
	}
	
}