package com.nano.candy.interpreter.runtime;

import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.builtin.BuiltinVariables;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.sys.CandySystem;
import java.io.File;

/**
 * Every thread has an evaluator-environment which is needed 
 * for the evaluator(VM).
 */ 
public class EvaluatorEnv {
	
	/**
	 * The interface for Candy language in the Java level.
	 */
	protected CNIEnv cniEnv;
	
	/**
	 * One For One.
	 *
	 * A Candy thread corresponds with a Java thread.
	 */
	protected CandyThread thread;
	
	/**
	 * The evaluator used to execute a {@code chunk}.
	 */
	protected Evaluator evaluator;
	
	/**
	 * This represents a source file environment used to store variables 
	 * in the source file.
	 *
	 * <p>Every function will remember the file environment when it was created.
	 * 
	 * <p>When a function is called, the {@code curFileEnv} will be reset to
	 * the file environment that the function is remembered.
	 */
	protected FileEnvironment curFileEnv;
	
	private InterpreterOptions options;
	
	protected EvaluatorEnv(CandyThread thread, InterpreterOptions options) {
		this.thread = thread;
		this.options = options;
		this.evaluator = new CandyV1Evaluator(this);
		this.cniEnv = new CNIEnv(this, evaluator);
	}
	
	public Evaluator getEvaluator() {
		return evaluator;
	}
	
	public CandyThread getCurrentThread() {
		return thread;
	}

	public Frame[] getStack() {
		return evaluator.getStack();
	}
	
	public InterpreterOptions getOptions() {
		return options;
	}

	public CompiledFileInfo getCurRunningFile() {
		return curFileEnv == null ? null : 
			curFileEnv.getCompiledFileInfo();
	}
	
	/**
	 * Returns the parent path of the current running file.
	 */
	public String getCurrentDirectory() {
		File f = getCurRunningFile().getFile();
		if (f.isDirectory()) {
			return f.getAbsolutePath();
		}
		File parent = f.getParentFile();
		if (parent == null) {
			return CandySystem.DEFAULT_USER_DIR;
		}
		return parent.getAbsolutePath();
	}

	/**
	 * Returns java library paths used to load .jar files in
	 * Candy language level.
	 */
	public String[] getJavaLibraryPaths() {
		return new String[] {
			getCurrentDirectory(),
			CandySystem.getCandyLibsPath()
		};
	}
	
	public FileEnvironment getCurrentFileEnv() {
		return curFileEnv;
	}
	
	public String getVariableName(int index) {
		return curFileEnv
			.getVariableTable().getVariableName(index);
	}

	public CandyObject getVariableValue(int index) {
		Variable v = getVariable(index);
		return v != null ? v.getValue() : null;
	}

	public CandyObject getVariableValue(String name) {
		Variable v = getVariable(name);
		return v != null ? v.getValue() : null;
	}

	public Variable getVariable(int index) {
		VariableTable vars = curFileEnv.getVariableTable();
		Variable v = vars.getVariable(index);
		return v != null ? v : 
			BuiltinVariables.getVariable(vars.getVariableName(index));
	}

	public Variable getVariable(String name) {
		Variable v = curFileEnv.getVariableTable().getVariable(name);
		return v != null ? v : BuiltinVariables.getVariable(name);
	}

	public void setVariable(int index, CandyObject value) {
		curFileEnv.getVariableTable().setVariable(index, value);
	}

	public void setVariable(String name, CandyObject value) {
		curFileEnv.getVariableTable().defineVariable(name, value);
	}

	public boolean setVariableIfExists(int index, CandyObject value) {
		return curFileEnv.getVariableTable()
			.setVariableIfExists(index, value);
	}
}
