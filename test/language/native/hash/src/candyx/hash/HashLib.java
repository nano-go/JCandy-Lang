package candyx.hash;

import com.nano.candy.interpreter.i2.cni.NativeContext;
import com.nano.candy.interpreter.i2.rtda.FileScope;

public class HashLib implements NativeContext {

	@Override
	public void action(FileScope curFileScope) {
		curFileScope.defineClass(HashMapObj.HASHMAP_CLASS);
	}
}
