package com.nano.candy.utils;

public enum StyleCode {
	WHITE(30),
    WHITE_BACKGROUND(40),
    RED(31),
    RED_BACKGROUND(41),
    GREEN(32),
    GREEN_BACKGROUND(42),
    YELLOW(33),
    YELLOW_BACKGROUND(43),
    BLUE(34),
    BLUE_BACKGROUND(44),
    MAGENTA(35),
    MAGENTA_BACKGROUND(45),
    CYAN(36),
    CYAN_BACKGROUND(46),
    BLACK(37),
    BLACK_BACKGROUND(47),

	DEFAULT(0),
    BOLD(1),
    ITATIC(3),
    UNDERLINE(4),
    REVERSE(7);
	
	int code;
	private StyleCode(int code){
		this.code = code;
	}
	
	public static String render(String str, StyleCode w, StyleCode fg, StyleCode bg) {
		StringBuilder builder = new StringBuilder();
		return builder.append("\033[")
			.append(w.code).append(";")
			.append(fg.code).append(";")
			.append(bg.code).append("m")
			.append(str).append("\033[0m").toString();
	}
	
	public static String render(String str, StyleCode fg, StyleCode bg) {
		return render(str, DEFAULT, fg, bg);
	}
	
	public static String render(String str, StyleCode bg) {
		return render(str, DEFAULT, DEFAULT, bg);
	}
	
	public String render(String str) {
		return StyleCode.render(str, this);
	}
}
