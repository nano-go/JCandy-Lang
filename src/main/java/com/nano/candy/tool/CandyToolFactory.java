package com.nano.candy.tool;
import com.nano.candy.interpreter.i2.tool.DisassembleTool;
import java.util.HashMap;

public class CandyToolFactory {
	
	private static final HashMap<String, CandyTool> TOOLS = new HashMap<>();
	
	static {
		CandyToolFactory.register("perf", new PerformanceTool());
		CandyToolFactory.register("disassemble", DisassembleTool.DISASSEMBLE_TOOL);
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
	
	public static CandyTool createCandyTool(String name) throws UnknownToolException {
		CandyTool tool = TOOLS.get(name);
		if (tool == null) {
			throw new UnknownToolException(name);
		}
		return tool;
	}
}
