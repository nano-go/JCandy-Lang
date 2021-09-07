package com.nano.candy.interpreter.cni;

import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.runtime.CandyThread;
import com.nano.candy.interpreter.runtime.CompiledFileInfo;
import com.nano.candy.interpreter.runtime.Evaluator;
import com.nano.candy.interpreter.runtime.EvaluatorEnv;
import com.nano.candy.interpreter.runtime.FileEnvironment;
import com.nano.candy.interpreter.runtime.Frame;

public class CNIEnv {
	private EvaluatorEnv evalEnv;
	private Evaluator evaluator;

	public CNIEnv(EvaluatorEnv evalEnv, Evaluator evaluator) {
		this.evalEnv = evalEnv;
		this.evaluator = evaluator;
	}
	
	public Evaluator getEvaluator() {
		return evaluator;
	}
	
	public FileEnvironment getCurrentFileEnv() {
		return evalEnv.getCurrentFileEnv();
	}
	
	public CompiledFileInfo getCurRunningFile() {
		return evalEnv.getCurRunningFile();
	}
	
	public String getCurrentDirectory() {
		return evalEnv.getCurrentDirectory();
	}
	
	public InterpreterOptions getOptions() {
		return evalEnv.getOptions();
	}
	
	public String[] getJavaLibraryPaths() {
		return evalEnv.getJavaLibraryPaths();
	}
	
	public Frame[] getStack() {
		return evalEnv.getStack();
	}
	
	public CandyThread getCurrentThread() {
		return evalEnv.getCurrentThread();
	}
	
	public CandyObject getVariableValue(String name) {
		return evalEnv.getVariableValue(name);
	}
	
	public void setVariable(String name, CandyObject value) {
		evalEnv.setVariable(name, value);
	}
}
