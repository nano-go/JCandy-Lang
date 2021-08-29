package com.nano.candy.parser;

import com.nano.candy.utils.Logger;
import java.io.IOException;

import static org.junit.Assert.* ;

public class LoggerMsgChecker {
	static final Logger logger = Logger.getLogger() ;
	
	public static void expectedErrors(boolean output, boolean clear) {
		expectedErrors(output, clear, "");
	}
	
	public static void expectedErrors(boolean output, boolean clear, String msg) {
		try {
			if (!logger.hadErrors()) {
				fail("expected errors: " + msg) ;
			}
			if (output) {
				logger.printErrors(System.err) ;
			}
			
			if (clear) {
				logger.clearErrors() ;
			}
		} catch (IOException e) {
			fail(e.getMessage()) ;
		}
	}   
	
	public static void unexpectedErrors(boolean output, boolean clear) {
		try {		
			if (output) {
				logger.printErrors(System.err) ;
			}
			
			if (logger.hadErrors()) {
				if (clear) {
					logger.clearErrors() ;
				}
				fail("unexpected errors.") ;
			}
			
			if (clear) {
				logger.clearErrors() ;
			}
		} catch (IOException e) {
			fail(e.getMessage()) ;
		}
	}
}

