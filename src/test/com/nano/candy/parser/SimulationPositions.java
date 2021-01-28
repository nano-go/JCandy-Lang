package com.nano.candy.parser;
import com.nano.candy.common.CandyTestCase;
import com.nano.candy.utils.Position;

public class SimulationPositions{
	
	public static Location loc(int line, int col) {
		return new Location(line, col) ;
	}
	
	protected String input ;
	protected Position[] positions ;
	
	public SimulationPositions(String input, Location[] locations) {
		this.input = input;
		init(input, locations) ;
	}
	
	private void init(String input, Location... locations) {
		Position[] positions = new Position[locations.length] ;
		String forLines = (" " + input).replace("\n", "\n ") ;
		String[] lines = forLines.split(System.lineSeparator()) ;
		for (int i = 0; i < locations.length; i ++) {
			positions[i] = new Position(
				"test.cd", 
				lines[locations[i].line - 1].substring(1),
				locations[i].line, 
				locations[i].col
			) ;
		}
		this.positions = positions ;
	}
	
	public static class Location {
		int line, col ;
		public Location(int line, int col) {
			this.line = line;
			this.col = col;
		}
	}
}
