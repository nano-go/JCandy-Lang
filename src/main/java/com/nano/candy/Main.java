package com.nano.candy;
import com.nano.candy.main.CandyRun;
import java.io.IOException;
import org.apache.commons.cli.ParseException;

public class Main {
	public static void main(String... args) throws IOException{
		try {
			new CandyRun(args).main() ;
		} catch ( ParseException e) {
			System.err.println(e.getMessage()) ;
			System.exit(1) ;
		}
	}
}
