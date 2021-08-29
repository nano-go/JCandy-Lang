package com.nano.candy;
import com.nano.candy.cmd.CandyOptions;
import com.nano.candy.cmd.CandyOptionsParser;
import com.nano.candy.cmd.Options;
import com.nano.candy.cmd.CmdToolException;
import com.nano.candy.interpreter.Interpreter;
import com.nano.candy.interpreter.InterpreterFactory;
import com.nano.candy.sys.CandySystem;
import java.io.File;

public class Main {
	
	private static Interpreter interpreter;
	
	public static void main(String... args){
		try {
			CandySystem.init();
			checkEnv();
			
			CandyOptions options = CandyOptionsParser.parse(args);	
			if (options.isPrintHelper()) {
				options.printHelper();
				return;
			}
			
			interpreter = InterpreterFactory
				.newInterpreter(options.getInterpreterOptions());
			options.getTool().run(interpreter, options);
		} catch (Options.ParseException |
		         CmdToolException |
				 InterruptedException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void checkEnv() {
		if (CandySystem.getCandyHomePath() == null) {
			throw new Options.ParseException("Missing $CANDY_HOME path.");
		}
		File candyHome = new File(CandySystem.getCandyHomePath());
		if (!candyHome.isDirectory()) {
			throw new Options.ParseException("Invalid $CANDY_HOME directory.");
		}
		File candyLibraries = new File(CandySystem.getCandyLibsPath());
		if (!candyLibraries.isDirectory()) {
			throw new Options.ParseException("'$CANDY_HOME/libs' is corrupted.");
		}
	}
}
