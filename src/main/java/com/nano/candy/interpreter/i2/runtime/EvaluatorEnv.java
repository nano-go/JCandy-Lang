package com.nano.candy.interpreter.i2.runtime;

import com.nano.candy.interpreter.InterpreterOptions;
import com.nano.candy.interpreter.i2.cni.CNIEnv;
import com.nano.candy.interpreter.i2.runtime.module.SourceFileInfo;
import com.nano.candy.sys.CandySystem;
import java.io.File;

public class EvaluatorEnv {
	protected CNIEnv cniEnv;
	protected GlobalEnvironment globalEnv;
	protected StackFrame stack;
	protected Evaluator evaluator;
	private InterpreterOptions options;
	
	public EvaluatorEnv(InterpreterOptions options) {
		this.globalEnv = new GlobalEnvironment();
		this.stack = new StackFrame(CandySystem.DEFAULT_MAX_STACK);
		this.evaluator = new CandyV1Evaluator(this);
		this.cniEnv = new CNIEnv(this, evaluator);
		this.options = options;
	}
	
	public Evaluator getEvaluator() {
		return evaluator;
	}

	public Frame[] getStack() {
		/*if (evaluator instanceof CandyV1Evaluator) {
			((CandyV1Evaluator) evaluator).syncPcToTopFrame();
		}*/
		Frame[] frames = new Frame[stack.sp()];
		for (int i = 0; i < frames.length; i ++) {
			frames[i] = stack.getAt(stack.sp()-i-1);
		}
		return frames;
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
