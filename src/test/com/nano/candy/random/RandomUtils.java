package com.nano.candy.random;
import com.nano.candy.parser.TokenKind;

public class RandomUtils {

	public static int randomInt(int from, int to) {
		if (from > to) {
			throw new IllegalArgumentException() ;
		}
		to ++ ;
		return (int)(from + (Math.random() * (to - from))) ;
	}

	public static float randomFloat(float from, float to) {
		if (from > to) {
			throw new IllegalArgumentException() ;
		}
		to ++ ;
		return (float)(from + (Math.random() * (to - from))) ;
	}

	public static char randomChar(char from, char to) {
		if (from > to) {
			throw new IllegalArgumentException() ;
		}
		to ++ ;
		return (char)(from + (Math.random() * (to - from))) ;
	}

	public static boolean randomBoolean() {
		return Math.random() > 0.5 ;
	}

	public static String randomString(int fromLen, int toLen, char[][] ranges) {
		int len = randomInt(fromLen, toLen) ;
		StringBuilder text = new StringBuilder() ;
		for (int i = 0; i < len; i ++) {
			int index = randomInt(0, ranges.length - 1) ;
			text.append(randomChar(ranges[index][0], ranges[index][1])) ;
		}
		return text.toString() ;
	}

	public static String randomLetters(int fromLen, int toLen) {
		return randomString(fromLen, toLen, new char[][] {
								{'a', 'z'}, {'A', 'Z'}
							}) ;
	}

	public static String randomCandyIdentifier() {
		return randomCandyIdentifier(1, 6) ;
	}

	public static String randomCandyIdentifier(int fromLen, int toLen) {
		while (true) {
			String str = randomString(fromLen, toLen, new char[][] {
				{'a', 'z'}, {'A', 'Z'},
			}) ;
			if (TokenKind.lookupKind(str) == TokenKind.IDENTIFIER) {
				return str ;
			}
		}
	}
}
