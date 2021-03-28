package com.nano.candy.interpreter.i2.rtda.moudle;
import com.nano.candy.interpreter.i2.builtin.type.MoudleObj;
import com.nano.candy.interpreter.i2.error.NativeError;
import com.nano.candy.interpreter.i2.tool.Compiler;
import com.nano.candy.interpreter.i2.vm.VM;
import java.io.File;
import java.util.HashMap;

public class MoudleManager {
	
	private HashMap<SourceFileInfo, MoudleObj> importedMoudles;
	
	public MoudleManager() {
		importedMoudles = new HashMap<>();
	}
	
    public MoudleObj importFile(VM vm, String relativeDir) {
		File file = new File(vm.getCurrentDirectory(), relativeDir);
		SourceFileInfo srcFile = SourceFileInfo.get(file);
		MoudleObj moudleObj = checkSrcFile(srcFile);
		if (moudleObj == null) {
			moudleObj = runFile(vm, srcFile);
			importedMoudles.put(srcFile, moudleObj);
		}
		return moudleObj;
	}
	
	private MoudleObj checkSrcFile(SourceFileInfo srcFile) {
		if (srcFile.isRunning()) {
			throw new NativeError("Cyclic import.");
		}
		return importedMoudles.get(srcFile);
	}

	private MoudleObj runFile(VM vm, SourceFileInfo srcFile) {
		CompiledFileInfo compiledFile = Compiler.compile(srcFile.getFile(), true);
		vm.loadFile(compiledFile);
		return vm.run();
	}
	
}
