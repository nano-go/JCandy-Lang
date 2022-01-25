package com.nano.candy.parser;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.ast.Program;
import com.nano.candy.utils.CandySourceFile;
import com.nano.candy.utils.Phase;

public interface Parser extends Phase<CandySourceFile, ASTreeNode> {
	// Keep previous interface.
	public Program parse();
}
