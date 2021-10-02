package com.nano.candy.cmd;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.code.Chunk;
import com.nano.candy.codegen.CodeGenerator;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.utils.Context;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.TableView;
import java.io.File;

public class PerformanceTool implements CandyTool {

	protected PerformanceTool() {}

	@Override
	public String groupName() {
		return "Performance";
	}

	@Override
	public String groupHelper() {
		return "Performance Test Tool.";
	}

	@Override
	public String[] aliases() {
		return new String[]{"perf"};
	}

	@Override
	public void defineOptions(Options options) {}

	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {
		Context c = new Context();
		Logger logger = c.get(Logger.class);
		
		long parserDuration = 0;
		long codegenDuration = 0;
		long runDuration = 0;
		File src = options.getSourceFile();
		
		long startTimeMillis = System.currentTimeMillis();
		ASTreeNode node = ParserFactory.newParser(c, src).parse();
		logger.printAllMessage(true);
		parserDuration += System.currentTimeMillis() - startTimeMillis;

		startTimeMillis = System.currentTimeMillis();
		Chunk chunk = new CodeGenerator(false).genCode(node);
		logger.printAllMessage(true);
		codegenDuration += System.currentTimeMillis() - startTimeMillis;
		
		if (logger.hadErrors()) {
			System.exit(65);
		}
		
		startTimeMillis = System.currentTimeMillis();
		interpreter.execute(chunk);
		runDuration += System.currentTimeMillis() - startTimeMillis;
		
		TableView tableView = new TableView();
		tableView.setHeaders("parser", "codegen", "running");
		tableView.addItem(
			ms2str(parserDuration), 
			ms2str(codegenDuration), 
			ms2str(runDuration)
		);
		System.out.println(tableView.toString());
	}

	private static String ms2str(long ms) {
		return String.format("%dms", ms);
	}

}
