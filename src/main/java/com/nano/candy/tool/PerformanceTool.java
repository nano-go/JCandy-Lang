package com.nano.candy.tool;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.main.CandyOptions;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.utils.Logger;
import com.nano.candy.utils.TableView;
import java.io.File;

public class PerformanceTool implements CandyTool {
	
	private static final Logger logger = Logger.getLogger();
	
	protected PerformanceTool() {}
	
	@Override
	public void run(Interpreter interpreter, CandyOptions options) throws Exception{
		long parserDuration = 0;
		long loadDuration = 0;
		long runDuration = 0;
		for (File src : options.getFiles()) {
			interpreter.initOrReset();
			
			long startTimeMillis = System.currentTimeMillis();
			ASTreeNode node = ParserFactory.newParser(src).parse();
			logger.printAllMessage(true);
			parserDuration += System.currentTimeMillis() - startTimeMillis;
			
			startTimeMillis = System.currentTimeMillis();
			interpreter.load(node, false);
			logger.printAllMessage(true);
			loadDuration += System.currentTimeMillis() - startTimeMillis;
			
			startTimeMillis = System.currentTimeMillis();
			interpreter.run(false);
			runDuration += System.currentTimeMillis() - startTimeMillis;
		}
		
		System.out.printf("%d files.\n", options.getFiles().length);
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
