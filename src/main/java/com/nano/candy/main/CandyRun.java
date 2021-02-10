package com.nano.candy.main;
import com.nano.candy.ast.Program;
import com.nano.candy.ast.printer.AstPrinter;
import com.nano.candy.ast.printer.AstPrinters;
import com.nano.candy.parser.ParserFactory;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.InterpreterFactory;
import com.nano.candy.interpreter.error.ExitError;
import com.nano.candy.utils.Logger;
import com.nano.common.io.FileUtils;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.ParseException;

public class CandyRun {
	
	private static final Logger logger = Logger.getLogger();
	private static final Interpreter interpreter = InterpreterFactory.newInterpreter("i1");
	
	private CandyOptions options; 
	
	public CandyRun(String... args) throws ParseException {
		this.options = CandyOptionsParser.parse(args);
	}
	
	public void main() throws IOException {
		if (options.isPrintHelper()) {
			options.printHelper();
			return;
		}

		if (options.isPrintAst()) {
			printAst();
			return;
		}

		if (options.isInteractively()) {
			runInteractively();
		} else {
			run();
		}
	}

	private void printAst() throws IOException, RuntimeException {
		AstPrinter printer = AstPrinters.newPrinter(options);
		for (File f : options.getFiles()) {
			Program program = ParserFactory.newParser(f).parse();
			printMessage(true);
			printer.print(System.out, program);
			System.out.println();
		}
	}

	private void run() throws IOException {
		boolean isFailed = false;
		for (File sourceFile : options.getFiles()) {
			interpreter.initOrReset();
			if (!run(sourceFile, true, false)) {
				isFailed = true;
			}
		}
		if (isFailed) {
			System.exit(1);
		}
	}

	private void runInteractively() throws IOException {
		interpreter.initOrReset();
		StringBuilder input = new StringBuilder();
		boolean inMultiLineMode = false;
		java.util.Scanner scanner = new java.util.Scanner(System.in);
		while (true) {
			System.out.print(">>> ");
			if (!scanner.hasNext()) {
				return;
			}
			String line = scanner.nextLine().trim();
			if (line.endsWith("$")) {
				inMultiLineMode = !inMultiLineMode;
				line = line.substring(0, line.length() - 1);
			}
			input.append(line).append("\n");
			if (!inMultiLineMode) {
				run("command line", input.toString(), false, true);
				// clear saved input
				input.delete(0, input.length());
			}
		}
	}
	
	private boolean run(File sourceFile, boolean exitIfError, boolean interactively) throws IOException {
		return this.run(
			sourceFile.getPath(), 
			FileUtils.readText(sourceFile), 
			exitIfError, 
			interactively
		);
	}

	private boolean run(String fileName, 
	                    String content, 
	                    boolean exitIfError, 
	                    boolean interactively) throws IOException
	{
		try {
			Program program = ParserFactory.newParser(fileName, content).parse();
			if (!printMessage(exitIfError)) return false;
			interpreter.load(program);
			if (!printMessage(exitIfError)) return false;
			return interpreter.run(interactively);
		} catch (ExitError e) {
			System.exit(e.getCode());
		}
		return false;
	}

	private static boolean printMessage(boolean exitIfError) throws IOException {
		if (logger.hadWarns()) {
			logger.printWarns(System.out);
		}
		if (logger.hadErrors()) {
			logger.printErrors(System.err);
			logger.clearAllMessages();
			if(exitIfError) System.exit(1);
			return false;
		}
		logger.clearAllMessages();
		return true;
	}
	
}
