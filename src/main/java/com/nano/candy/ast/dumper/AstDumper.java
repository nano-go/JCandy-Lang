package com.nano.candy.ast.dumper;
import com.nano.candy.ast.ASTreeNode;
import java.io.OutputStream;

public interface AstDumper {
	void dump(DumperOptions options, ASTreeNode node);
}
