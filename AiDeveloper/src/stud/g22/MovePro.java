package stud.g22;

import core.game.Move;

import java.util.Comparator;

public class MovePro extends Move implements Comparable<MovePro>{
    private int score;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public MovePro(int index0, int index1) {
        super(index0, index1);
    }
    //比较器，用来比较MoveMaster的score属性，降序排序，选择最大的score
    public static Comparator<MovePro> scoreComparator = new Comparator<MovePro>() {
        public int compare(MovePro m1, MovePro m2) {
            return m2.score - m1.score;
        }
    };
    @Override
    public int compareTo(MovePro o) {
        return this.score - o.score;
    }
}
