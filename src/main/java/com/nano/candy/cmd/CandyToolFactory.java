package com.nano.candy.cmd;

import com.nano.candy.interpreter.i2.tool.DisassembleTool;
import java.util.Collection;
import java.util.HashMap;

public class CandyToolFactory {

	private static final HashMap<String, CandyTool> TOOLS_WITHOUT_ALIASES = new HashMap<>();
	private static final HashMap<String, CandyTool> TOOLS = new HashMap<>();

	static {
		CandyToolFactory.register(new PerformanceTool());
		CandyToolFactory.register(new ExeTool());
		CandyToolFactory.register(new AstTool());
		CandyToolFactory.register(new DisassembleTool());
		// CandyToolFactory.register(new DebugerTool());
	}

	public static void register(CandyTool tool) {
		TOOLS_WITHOUT_ALIASES.put(tool.groupName(), tool);
		TOOLS.put(tool.groupName(), tool);
		String[] aliases = tool.aliases();
		if (aliases == null) {
			return;
		}
		for (String alias : aliases) {
			if (TOOLS.containsKey(alias)) {
				throw new Error("Duplicated tool name: " + alias);
			}
			TOOLS.put(alias, tool);
		}
	}

	public static Collection<CandyTool> tools() {
		return TOOLS_WITHOUT_ALIASES.values();
	}

	public static boolean isTool(String name) {
		return TOOLS.containsKey(name);
	}

	public static CandyTool getCandyTool(String name) {
		CandyTool tool = TOOLS.get(name);
		if (tool == null) {
			return new ExeTool();
		}
		return tool;
	}
}

