package com.nano.candy.interpreter.i1;
import com.nano.candy.ast.ASTreeNode;
import com.nano.candy.interpreter.error.CandyRuntimeError;
import com.nano.candy.utils.Position;

/**
 * Refectively initialize by @{link com.nano.candy.runtime.interpreter.InterpreterFactory}
 */
public class InterpreterImpl extends AstInterpreter {

	@Override
	public boolean run(ASTreeNode node, boolean isInteratively) {
		try {
			return super.run(node, isInteratively) ;
		} catch (CandyRuntimeError | 
		         ArithmeticException | 
				 IllegalArgumentException |
		         IndexOutOfBoundsException e) {
			reportError(getEnvironment().getCurrentLocation(), e);
		} catch (Throwable e) {
			reportError(getEnvironment().getCurrentLocation(), e);
			e.printStackTrace();
		}
		return false;
	}

	private void reportError(Position loc, Throwable e) {
		StringBuilder errorMsg = new StringBuilder();
		errorMsg.append(e.getClass().getSimpleName())
			.append(": ")
			.append(e.getMessage())
			.append("\n    in source file: ")
			.append(loc.getFileName());
		if (loc.getLineFromSource().isPresent()) {	
			errorMsg.append(String.format(
				"\n    %d | %s", 
				loc.getLine(), loc.getLineFromSource().get().trim()
			));
		} else {
			errorMsg.append("; at line ").append(loc.getLine()).append(".");
		}
		System.err.println(errorMsg.toString());
	}
	
}
