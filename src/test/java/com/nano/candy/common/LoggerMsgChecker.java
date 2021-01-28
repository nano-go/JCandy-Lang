package com.nano.candy.common;

import com.nano.candy.utils.Logger;
import java.io.IOException;

import static org.junit.Assert.* ;

public class LoggerMsgChecker {
	static final Logger logger = Logger.getLogger() ;
	
	public static void shouldAppearErrors(boolean output, boolean clear) {
		try {
			if (!logger.hadErrors()) {
				fail("There should appear errors.") ;
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
	
	public static void shouldNotAppearErrors(boolean output, boolean clear) {
		try {		
			if (output) {
				logger.printErrors(System.err) ;
			}
			
			if (logger.hadErrors()) {
				if (clear) {
					logger.clearErrors() ;
				}
				fail("There shouldn't appear errors.") ;
			}
			
			if (clear) {
				logger.clearErrors() ;
			}
		} catch (IOException e) {
			fail(e.getMessage()) ;
		}
	}
}

