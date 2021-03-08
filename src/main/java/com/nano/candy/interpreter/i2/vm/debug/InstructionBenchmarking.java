package com.nano.candy.interpreter.i2.vm.debug;

import com.nano.candy.interpreter.i2.instruction.Instructions;
import com.nano.candy.utils.TableView;

public class InstructionBenchmarking {

	public static final boolean DEBUG = false;

	private static final InstructionBenchmarking instance = new InstructionBenchmarking();

	public static InstructionBenchmarking getInstance() {
		return instance;
	}

	private int[] exeCounter;
	private long[] totalDuration;

	private long start;
	private byte opcode;

	public InstructionBenchmarking() {
		exeCounter = new int[Instructions.INSTRUCTION_NUMBER];
		totalDuration = new long[Instructions.INSTRUCTION_NUMBER];
	}

	public void startExeInstructoon(byte opcode) {
		this.exeCounter[opcode] ++;
		this.opcode = opcode;
		this.start = System.nanoTime();
	}

	public void endExeInstruction() {
		this.totalDuration[opcode] += System.nanoTime() - start;
	}

	public void printResult() {
		TableView tableView = new TableView();
		tableView.setSpace("  ");
		tableView.setHeaders("OpName", "Counter", "Average", "Ops");
		long averageTotal = 0;
		long validInsCount = 0;
		for (byte opcode = 0; opcode < Instructions.INSTRUCTION_NUMBER; opcode ++) {
			if (exeCounter[opcode] < 50 || opcode == Instructions.OP_EXIT) {
				continue;
			}

			validInsCount ++;
			long averageExeTime = totalDuration[opcode] / exeCounter[opcode];		
			long ops;
			if (averageExeTime != 0) {
				ops = (long)Math.pow(10, 9) / averageExeTime;
			} else {
				ops = (long)Math.pow(10, 9);
			}
			averageTotal += averageExeTime;

			tableView.addItem(
				Instructions.getName(opcode),
				String.valueOf(exeCounter[opcode]),
				String.valueOf(averageExeTime) + " ns",
				String.valueOf(ops) + "/ops"
			);
		}
		System.out.println(tableView.toString());
		if (validInsCount == 0) {
			return;
		}
		averageTotal /= validInsCount;
		long ops = (long)Math.pow(10, 9) / averageTotal;
		System.out.printf("Total: %d ops.\n", ops);
	}
}
