package com.nano.candy;
import com.nano.candy.cmd.ToolException;
import com.nano.candy.main.CandyRun;
import com.nano.candy.utils.Options;

public class Main {
	public static void main(String... args){
		try {
			new CandyRun(args).main();
		}  catch (Options.ParseException |
		          ToolException |
				  InterruptedException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
