package stud.g22;


import core.board.PieceColor;

import java.util.ArrayList;

import static core.board.PieceColor.BLACK;
import static core.game.Move.SIDE;

public class RoadTable {
    private Road[][] basicRoads = new Road[SIDE * SIDE][4]; //横 + 纵 + 斜上 + 斜
    private RoadList[][] bwRoads = new RoadList[7][7]; //黑白路表
    //四个方向
    public static final int[][] GOGO = new int[][]{{0, 1}, {1, 0}, {1, 1}, {1, -1}};

    //方向：下， 右，右下，右上；  {行增量，列增量}
    public static final int[] DIRECTION = {SIDE, 1, SIDE + 1, -SIDE + 1};
    public static int valueEstimate(PieceColor pieceColor, RoadTable roadTable,int xs){
        if (xs == 1){  //表示后手
            return valueEstimateS(pieceColor,roadTable);
        } else {  //xs = 0 表示先手
            return valueEstimateF(pieceColor,roadTable);
        }
    }
    public static int valueEstimateS(PieceColor pieceColor, RoadTable roadTable) {
        int blackValue = 0;
        int whiteValue = 0;
        int[] numberOfBlackRoad = roadTable.allBlackCount();
        int[] numberOfWhiteRoad = roadTable.allWhiteCount();
        if (pieceColor == PieceColor.BLACK) {
            for (int i = 1; i < 6; i++) {
                blackValue += numberOfBlackRoad[i] * curValueS[i];
                whiteValue += numberOfWhiteRoad[i] * opposeValueS[i];
            }
            return blackValue - whiteValue;
        }
        if (pieceColor == PieceColor.WHITE) {
            for (int i = 1; i < 6; i++) {
                blackValue += numberOfBlackRoad[i] * opposeValueS[i];
                whiteValue += numberOfWhiteRoad[i] * curValueS[i];
            }
            return whiteValue - blackValue;
        }
        return 0;
    }
    public static int valueEstimateF(PieceColor pieceColor, RoadTable roadTable) {
        int blackValue = 0;
        int whiteValue = 0;
        int[] numberOfBlackRoad = roadTable.allBlackCount();
        int[] numberOfWhiteRoad = roadTable.allWhiteCount();

        if (pieceColor == PieceColor.BLACK) {
            for (int i = 1; i < 6; i++) {
                blackValue += numberOfBlackRoad[i] * curValueF[i];
                whiteValue += numberOfWhiteRoad[i] * opposeValueF[i];
            }
            return blackValue - whiteValue;
        }
        if (pieceColor == PieceColor.WHITE) {
            for (int i = 1; i < 6; i++) {
                blackValue += numberOfBlackRoad[i] * opposeValueF[i];
                whiteValue += numberOfWhiteRoad[i] * curValueF[i];
            }
            return whiteValue - blackValue;
        }
        return 0;
    }

    public Road[][] getBasicRoads() {
        return basicRoads;
    }

    public void setBasicRoads(Road[][] basicRoads) {
        this.basicRoads = basicRoads;
    }

    public RoadList[][] getBwRoads() {
        return bwRoads;
    }

    public void setbwRoads(RoadList[][] bwRoads) {
        this.bwRoads = bwRoads;
    }

    public RoadTable() {
        for (int i = 0; i < bwRoads.length; i++)
            for (int j = 0; j < bwRoads[0].length; j++)
                bwRoads[i][j] = new RoadList();
    }

    //获取以pos为起点的四条路
    public Road[] getRoads(int pos) {
        return basicRoads[pos];
    }

    //看能不能直接胜利
    public Road findColorWin(PieceColor col) {
        if (col == PieceColor.BLACK) {
            if (bwRoads[5][0].size() > 0){
                return bwRoads[5][0].get(0);
            }

            if (bwRoads[4][0].size() > 0){
                return bwRoads[4][0].get(0);
            }

        } else {
            if (bwRoads[0][5].size() > 0){
                return bwRoads[0][5].get(0);
            }

            if (bwRoads[0][4].size() > 0){
                return bwRoads[0][4].get(0);
            }
        }
        return null;
    }

    //下在pos后，受到影响的所有的有效路
    public ArrayList<Road> getAffectedRoads(int pos) {
        ArrayList<Road> affectedRoads = new ArrayList<>();
        for (int i = 0; i < 6; i++)
            for (int k = 0; k < 4; k++) {
                int index = pos - DIRECTION[k] * i; //受影响的pos
                if (index >= 0 && index < SIDE * SIDE) {
                    if (basicRoads[index][k].getLegal() == true)
                        affectedRoads.add(basicRoads[index][k]);
                }
            }
        return affectedRoads;
    }

    public void removeRoad(Road road) {
        basicRoads[road.getPos()][road.getDir()] = null;
        bwRoads[road.getBlackNum()][road.getWhiteNum()].remove(road);
    }

    public void addRoad(Road road) {
        basicRoads[road.getPos()][road.getDir()] = road;
        bwRoads[road.getBlackNum()][road.getWhiteNum()].add(road);
    }

    public int[] allBlackCount() {
        int[] temp = new int[7];
        for (int i = 1; i < 7; i++) {
            temp[i] = bwRoads[i][0].size();
        }
        return temp;
    }

    public int[] allWhiteCount() {
        int[] temp = new int[7];
        for (int i = 1; i < 7; i++) {
            temp[i] = bwRoads[0][i].size();
        }
        return temp;
    }
    public void initTable() {
        for (int i = 0; i < SIDE; i++) {
            for (int j = 0; j < SIDE; j++) {
                for (int k = 0; k < 4; k++) {
                    int c = i + GOGO[k][0] * 5;
                    int r = j + GOGO[k][1] * 5;
                    boolean legal;
                    if (c >= 0 && c <= SIDE - 1 && r >= 0 && r <= SIDE - 1) legal = true;
                    else legal = false;

                    int indexs = j * SIDE + i;
                    basicRoads[indexs][k] = new Road(indexs, k, 0, 0, legal);
                    if (legal) bwRoads[0][0].add(basicRoads[indexs][k]);
                }
            }
        }
        // 初始棋子都是下在棋盘中间，故需要修改路表
        int mid_index = 180;
        ArrayList<Road> changedRoads = getAffectedRoads(mid_index);
        for (Road road : changedRoads) {
            removeRoad(road);
            road.addStone(BLACK);
            addRoad(road);
        }
    }
    //每个棋子在有0，1，2...6个时该路所具有的价值
    public final static int[] curValueF = {0, 9, 520, 2070, 7890, 10020, 1000000};
    public final static int[] opposeValueF = {0, 3, 480, 2670, 3887, 4900, 1000000};
    public final static int[] curValueS = {0, 17, 78, 141, 788, 1030, 10000};
    public final static int[] opposeValueS = {0, 17, 78, 241, 988, 1030, 10000};
//    public final static int[] curValueS = {0, 1, 20, 40, 200, 200, 1000000};
//    public final static int[] opposeValueS = {0, 17, 25, 50, 6000, 6000, 1000000};
}
