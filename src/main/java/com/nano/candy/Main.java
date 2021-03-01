package com.nano.candy;
import com.nano.candy.main.CandyRun;
import com.nano.candy.tool.UnknownToolException;
import org.apache.commons.cli.ParseException;

public class Main {
	public static void main(String... args){
		try {
			new CandyRun(args).main();
		}  catch (ParseException | 
		          UnknownToolException | 
				  InterruptedException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
