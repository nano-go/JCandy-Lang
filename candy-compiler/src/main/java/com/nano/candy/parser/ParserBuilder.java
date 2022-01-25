package com.nano.candy.parser;

import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.utils.CandySourceFile;
import com.nano.candy.utils.Context;
import com.nano.candy.utils.Phase;
import com.nano.candy.utils.Task;
import java.util.Objects;

public class ParserBuilder {
	
	public static ParserBuilder instance(Context ctx) {
		ParserBuilder builder = ctx.get(ParserBuilder.class);
		if (builder == null) {
			builder = new ParserBuilder(ctx);
			ctx.put(ParserBuilder.class, builder);
		}
		return builder;
	}
	
	protected boolean keepComment;
	protected boolean keepEndPos;
	protected boolean isDebugMode = true;
	
	protected Context ctx;
	
	public ParserBuilder(Context ctx) {
		this.ctx = Objects.requireNonNull(ctx);
	}
	
	public ParserBuilder setKeepComment(boolean keepComment) {
		this.keepComment = keepComment;
		return this;
	}

	public boolean isKeepComment() {
		return keepComment;
	}

	public ParserBuilder setKeepEndPos(boolean keepEndPos) {
		this.keepEndPos = keepEndPos;
		return this;
	}

	public boolean isKeepEndPos() {
		return keepEndPos;
	}

	public ParserBuilder setIsDebugMode(boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
		return this;
	}

	public boolean isDebugMode() {
		return isDebugMode;
	}
	
	public Phase<CandySourceFile, ASTreeNode> newPhase() {
		return new CandyParser(this);
	}
	
	public Task<CandySourceFile, ASTreeNode> newTask() {
		return Task.newTask(newPhase());
	}
	
	public Parser newParser(CandySourceFile srcFile) {
		Scanner scanner = newScanner(srcFile);
		return new CandyParser(ctx, scanner, this);
	}
	
	public Parser newParser(String fileName, CharSequence input) {
		Scanner scanner = newScanner(fileName, input);
		return newParser(scanner);
	}
	
	public Parser newParser(Scanner scanner) {
		return new CandyParser(ctx, scanner, this);
	}

	public Scanner newScanner(CandySourceFile srcFile) {
		return newScanner(srcFile.getPath(), srcFile.getContent());
	}
	
	public Scanner newScanner(String fileName, CharSequence input) {
		return new CandyScanner(ctx, keepComment, 
								fileName, input.toString().toCharArray());
	}
	
}
