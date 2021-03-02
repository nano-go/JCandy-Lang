package com.nano.candy.tool;
import com.nano.candy.interpreter.i2.tool.DisassembleTool;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class CandyToolFactory {
	
	private static final HashMap<String, CandyTool> TOOLS = new HashMap<>();
	
	static {
		CandyToolFactory.register("perf", new PerformanceTool());
		CandyToolFactory.register("exe", new ExeTool());
		CandyToolFactory.register("ast", new AstTool());
		
		CandyToolFactory.register("dis", DisassembleTool.DISASSEMBLE_TOOL);
	}
	
	public static Collection<CandyTool> tools() {
		return TOOLS.values();
	}
	
	public static Set<String> names() {
		return TOOLS.keySet();
	}
	
	public static void register(String name, CandyTool tool, String... aliases) {
		TOOLS.put(name, tool);
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
	
	public static boolean isTool(String name) {
		return TOOLS.containsKey(name);
	}
	
	public static CandyTool createCandyTool(String name) {
		CandyTool tool = TOOLS.get(name);
		if (tool == null) {
			return new ExeTool();
		}
		return tool;
	}
}
