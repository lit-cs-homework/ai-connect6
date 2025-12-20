package stud.g12;

import core.game.Move;

import java.util.Comparator;

public class MovePro extends Move implements Comparable<MovePro>{

	public MovePro(char col0, char row0, char col1, char row1) {
		super(col0, row0, col1, row1);
		// TODO Auto-generated constructor stub
	}

	public MovePro(int index0, int index1) {
		super(index0, index1);
		// TODO Auto-generated constructor stub
	}
	public MovePro(int index0, int index1, int score) {
		this(index0, index1);
		this.score = score;
		// TODO Auto-generated constructor stub
	}

	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	
	public Move getMove() {
		return new Move(this.index1(), this.index2());
	}
	

	@Override
	public int compareTo(MovePro mov) {
		// TODO Auto-generated method stub
		return this.score - mov.score;
	}
	
	public static Comparator<MovePro> scoreComparator = new Comparator<MovePro>(){
	       public int compare(MovePro m1, MovePro m2) {
	           return m2.score - m1.score;
	        }
	};
	
	private int score;
}
