package com.nano.candy.interpreter.runtime;

import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.runtime.module.SourceFileInfo;
import com.nano.candy.sys.CandySystem;
import java.io.File;

public class EvaluatorEnv {
	protected CNIEnv cniEnv;
	protected GlobalEnvironment globalEnv;
	protected CandyThread thread;
	protected Evaluator evaluator;
	private InterpreterOptions options;
	
	public EvaluatorEnv(InterpreterOptions options) {
		this(new CandyThread(Thread.currentThread()), options);
	}
	
	protected EvaluatorEnv(CandyThread thread, InterpreterOptions options) {
		this.thread = thread;
		this.options = options;
		this.globalEnv = new GlobalEnvironment();
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
		return thread.getStack();
	}
	
	public InterpreterOptions getOptions() {
		return options;
	}

	public CompiledFileInfo getCurRunningFile() {
		return globalEnv.getCurrentFileEnv().getCompiledFileInfo();
	}

	public SourceFileInfo getCurSourceFileInfo() {
		CompiledFileInfo compiledFileInfo = getCurRunningFile();
		if (compiledFileInfo.isRealFile()) {
			return SourceFileInfo.get(compiledFileInfo.getFile());
		}
		return null;
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
		return globalEnv.getCurrentFileEnv();
	}
	
	public GlobalEnvironment getGlobalEnv() {
		return globalEnv;
	}
}
