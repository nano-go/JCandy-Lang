package com.nano.candy.interpreter.cni;

import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.runtime.Evaluator;
import com.nano.candy.interpreter.runtime.EvaluatorEnv;
import com.nano.candy.interpreter.runtime.Frame;
import com.nano.candy.interpreter.runtime.CandyThread;

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
	
	public EvaluatorEnv getEvaluatorEnv() {
		return evalEnv;
	}
	
	public Frame[] getStack() {
		return evalEnv.getStack();
	}
	
	public CandyThread getCurrentThread() {
		return evalEnv.getCurrentThread();
	}
	
	public CandyObject getVariableValue(String name) {
		return evalEnv.getGlobalEnv().getVariableValue(name);
	}
	
	public void setVariable(String name, CandyObject value) {
		evalEnv.getGlobalEnv().setVariable(name, value);
	}
}
