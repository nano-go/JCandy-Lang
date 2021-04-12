package com.nano.candy.interpreter.i2.vm.monitor;
import com.nano.candy.interpreter.i2.rtda.Frame;
import com.nano.candy.interpreter.i2.rtda.FrameStack;
import com.nano.candy.interpreter.i2.vm.VM;
import java.util.HashSet;

public final class MonitorManager {
	
	private HashSet<CodeMonitor> codeMonitors;
	private HashSet<StackMonitor> stackMonitors;
	
	public MonitorManager() {
		this.codeMonitors = new HashSet<>();
		this.stackMonitors = new HashSet<>();
	}
	
	public void registerCodeMonitor(CodeMonitor codeMonitor) {
		this.codeMonitors.add(codeMonitor);
	}
	
	public void unregisterCodeMonitor(CodeMonitor codeMonitor) {
		this.codeMonitors.remove(codeMonitor);
	}
	
	public void registerStackMonitor(StackMonitor stackMonitor) {
		this.stackMonitors.add(stackMonitor);
	}
	
	public void unregisterStackMonitor(StackMonitor stackMonitor) {
		this.stackMonitors.remove(stackMonitor);
	}
	
	public final void notifyStackPushed(VM vm, FrameStack frameStack) {
		if (stackMonitors.isEmpty()) {
			return;
		}
		for (StackMonitor monitor : stackMonitors) {
			monitor.newFramePushed(vm, frameStack);
		}
	}
	
	public final void notifyStackPoped(VM vm, Frame oldFrame, FrameStack stack) {
		if (stackMonitors.isEmpty()) {
			return;
		}
		for (StackMonitor monitor : stackMonitors) {
			monitor.oldFramePoped(vm, oldFrame, stack);
		}
	}
	
	public final void notifyInsStarted(VM vm, int pc) {
		if (codeMonitors.isEmpty()) {
			return;
		}
		for (CodeMonitor monitor : codeMonitors) {
			monitor.beforeIns(vm, pc);
		}
	}
	
	public final void notifyInsEnd(VM vm) {
		if (codeMonitors.isEmpty()) {
			return;
		}
		for (CodeMonitor monitor : codeMonitors) {
			monitor.afterIns(vm);
		}
	}
	
	public HashSet<StackMonitor> getStackMonitors() {
		return stackMonitors;
	}
	
	public HashSet<CodeMonitor> getCodeMonitors() {
		return codeMonitors;
	}
	
}
