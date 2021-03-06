package com.nano.candy.cmd;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.Options;
import com.nano.candy.utils.TableView;
import java.io.File;

public class PerformanceTool implements CandyTool {

	private static final Logger logger = Logger.getLogger();

	protected PerformanceTool() {}

	@Override
	public String groupName() {
		return "Performance";
	}

	@Override
	public String groupHelper() {
		return "Performance test tool.";
	}

	@Override
	public String[] aliases() {
		return new String[]{"perf"};
	}

	@Override
	public void defineOptions(Options options) {}

	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception {
		long parserDuration = 0;
		long loadDuration = 0;
		long runDuration = 0;
		File src = options.getSourceFile();
		interpreter.initOrReset();
		long startTimeMillis = System.currentTimeMillis();
		ASTreeNode node = ParserFactory.newParser(src).parse();
		logger.printAllMessage(true);
		parserDuration += System.currentTimeMillis() - startTimeMillis;

		startTimeMillis = System.currentTimeMillis();
		interpreter.load(node);
		logger.printAllMessage(true);
		loadDuration += System.currentTimeMillis() - startTimeMillis;

		startTimeMillis = System.currentTimeMillis();
		interpreter.run();
		runDuration += System.currentTimeMillis() - startTimeMillis;
		
		TableView tableView = new TableView();
		tableView.setHeaders("parser", "loading", "running");
		tableView.addItem(
			ms2str(parserDuration), 
			ms2str(loadDuration), 
			ms2str(runDuration)
		);
		System.out.println(tableView.toString());
	}

	private static String ms2str(long ms) {
		return String.format("%dms", ms);
	}

}
