package com.nano.candy.interpreter.i2.vm.tracer;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.StackFrame;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.HashSet;

public final class TracerManager {
	
	private HashSet<CodeTracer> codeMonitors;
	private HashSet<StackTracer> stackMonitors;
	
	public TracerManager() {
		this.codeMonitors = new HashSet<>();
		this.stackMonitors = new HashSet<>();
	}
	
	public void registerCodeMonitor(CodeTracer codeMonitor) {
		this.codeMonitors.add(codeMonitor);
	}
	
	public void unregisterCodeMonitor(CodeTracer codeMonitor) {
		this.codeMonitors.remove(codeMonitor);
	}
	
	public void registerStackMonitor(StackTracer stackMonitor) {
		this.stackMonitors.add(stackMonitor);
	}
	
	public void unregisterStackMonitor(StackTracer stackMonitor) {
		this.stackMonitors.remove(stackMonitor);
	}
	
	public final void notifyStackPushed(VM vm, StackFrame frameStack) {
		if (stackMonitors.isEmpty()) {
			return;
		}
		for (StackTracer monitor : stackMonitors) {
			monitor.newFramePushed(vm, frameStack);
		}
	}
	
	public final void notifyStackPoped(VM vm, Frame oldFrame, StackFrame stack) {
		if (stackMonitors.isEmpty()) {
			return;
		}
		for (StackTracer monitor : stackMonitors) {
			monitor.oldFramePoped(vm, oldFrame, stack);
		}
	}
	
	public final void notifyInsStarted(VM vm, int pc) {
		if (codeMonitors.isEmpty()) {
			return;
		}
		for (CodeTracer monitor : codeMonitors) {
			monitor.beforeIns(vm, pc);
		}
	}
	
	public final void notifyInsEnd(VM vm) {
		if (codeMonitors.isEmpty()) {
			return;
		}
		for (CodeTracer monitor : codeMonitors) {
			monitor.afterIns(vm);
		}
	}
	
	public HashSet<StackTracer> getStackMonitors() {
		return stackMonitors;
	}
	
	public HashSet<CodeTracer> getCodeMonitors() {
		return codeMonitors;
	}
	
}
