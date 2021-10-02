package com.nano.candy.parser;

import com.nano.candy.utils.Context;
import com.nano.candy.utils.Logger;
import java.io.IOException;

import static org.junit.Assert.* ;

public class LoggerMsgChecker {
	
	public static void expectedErrors(Logger logger, boolean output, boolean clear) {
		expectedErrors(logger, output, clear, "");
	}
	
	public static void expectedErrors(Logger logger, boolean output, boolean clear, String msg) {
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
	
	public static void unexpectedErrors(Logger logger, boolean output, boolean clear) {
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

