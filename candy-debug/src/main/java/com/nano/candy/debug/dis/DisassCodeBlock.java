package com.nano.candy.debug.dis;
import com.nano.candy.code.CodeAttribute;

public class DisassCodeBlock {
	
	private CodeAttribute attrs;
	private DisassInstruction[] insSet;

	public DisassCodeBlock(CodeAttribute attrs, DisassInstruction[] insSet) {
		this.attrs = attrs;
		this.insSet = insSet;
	}
	
	public CodeAttribute getCodeAttrs() { 
		return attrs;
	}
	
	public int length() { 
		return insSet.length;
	}
	
	public DisassInstruction getIns(int index) { 
		return insSet[index];
	}
}
