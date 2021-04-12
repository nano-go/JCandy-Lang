package com.nano.candy.interpreter.i2.rtda.moudle;
import com.nano.candy.interpreter.i2.builtin.type.MoudleObj;
import com.nano.candy.interpreter.i2.builtin.type.error.NativeError;
import com.nano.candy.interpreter.i2.tool.Compiler;
import com.nano.candy.interpreter.i2.vm.VM;
import com.nano.candy.sys.CandySystem;
import java.io.File;
import java.util.HashMap;

public class MoudleManager {
	
	private HashMap<SourceFileInfo, MoudleObj> importedMoudles;
	
	public MoudleManager() {
		importedMoudles = new HashMap<>();
	}
	
	public static File findSourceFile(String env, String path) {
		int index = path.lastIndexOf("/");
		String name = path;
		if (index > 0) {
			name = path.substring(index + 1);
		}
		if (name.length() > 0 && name.lastIndexOf(".") < 0) {
			path += "." + CandySystem.FILE_SUFFIX;
		}
		return new File(env, path);
	}
	
    public MoudleObj importFile(VM vm, String relativePath) {
		SourceFileInfo srcFile = SourceFileInfo.get(
			findSourceFile(vm.getCurrentDirectory(), relativePath)
		);
		MoudleObj moudleObj = checkSrcFile(srcFile);
		if (moudleObj == null) {
			moudleObj = runFile(vm, srcFile);
			importedMoudles.put(srcFile, moudleObj);
		}
		return moudleObj;
	}
	
	private MoudleObj checkSrcFile(SourceFileInfo srcFile) {
		if (srcFile.isRunning()) {
			new NativeError("Cyclic import.").throwSelfNative();
		}
		return importedMoudles.get(srcFile);
	}

	private MoudleObj runFile(VM vm, SourceFileInfo srcFile) {
		CompiledFileInfo compiledFile = Compiler.compile(srcFile.getFile(), true);
		vm.loadFile(compiledFile);
		return vm.run();
	}
	
}
