package test.com.nano.candy.i2.rtda.chunk;
import com.nano.candy.interpreter.i2.runtime.chunk.attrs.LineNumberTable;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.*;

public class LineNumberTableTest {

	private static class LineNumber {
		int startPC;
		int lineNumber;
		public LineNumber(int startPC, int lineNumber) {
			this.startPC = startPC;
			this.lineNumber = lineNumber;
		}

		@Override
		public String toString() {
			return String.format("(pc: %d, line %d)", startPC, lineNumber);
		}
	}
	
	@Test public void lineNumberTableTest() {
		for (int i = 0; i < 10; i ++) {
			LineNumber[] sample = randomSample();
			test(sample);
		}
	}

	private void test(LineNumber[] sample) {
		LineNumberTable lineNumberTable = createLineNumberTable(sample);
		for (int i = 0; i < sample.length; i ++) {
			int startPC = sample[i].startPC;
			int endPC;
			if (i != sample.length - 1) {
				endPC = sample[i + 1].startPC-1;
			} else {
				endPC = startPC + 5;
			}
			for (int pc = startPC; pc < endPC; pc ++) {
				String msg = String.format(
					"line pc: %d, search pc: %d\n%s\n", startPC, pc,
					Arrays.toString(sample)
				);
				assertEquals(
					msg, sample[i].lineNumber, 
					lineNumberTable.findLineNumber(pc)
				);
			}
		}
	}

	private LineNumberTable createLineNumberTable(LineNumber[] sample) {
		byte[] bytes = new byte[sample.length*4];
		int offset = 0;
		for (LineNumber line : sample) {
			bytes[offset]     = (byte) (line.startPC >> 8);
			bytes[offset + 1] = (byte) line.startPC;
			bytes[offset + 2] = (byte) (line.lineNumber >> 8);
			bytes[offset + 3] = (byte) line.lineNumber;
			offset += 4;
		}
		return new LineNumberTable(bytes);
	}
	
	private LineNumber[] randomSample() {
		ArrayList<LineNumber> sample = new ArrayList<>();
		int size = (int) (1 + Math.random() * 100);
		int previousStartPC = 0;
		for (int i = 0; i < size; i ++) {
			previousStartPC += (1 + Math.random() * 100);
			sample.add(new LineNumber(previousStartPC, (int) (Math.random()*100)));
		}
		return sample.toArray(new LineNumber[0]);
	}
}
