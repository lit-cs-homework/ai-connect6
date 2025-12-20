package stud.g12;

import core.board.PieceColor;
import core.game.Move;

import java.util.ArrayList;

import static core.board.Board.FORWARD;
import static core.board.PieceColor.BLACK;
import static core.game.Move.SIDE;
import static stud.g12.Road._FORWARD;

public class RoadTable {
	public void clear() {
		for(int i = 0; i < SIDE; i++){
			for(int j = 0; j < SIDE; j++){
				for(int k = 0 ; k < 4; k++){
					char end_col = (char) ('A'+  i + FORWARD[k][0]*5);
					char end_row =(char) ('A'+ j + FORWARD[k][1]*5);
					boolean active = Move.validSquare( end_col, end_row);
					int indexs = j * SIDE + i;
					roads[indexs][k] = new Road(indexs,k,0,0,active);
					if(active){
						playerRoads[0][0].add(roads[indexs][k]);
					}
				}
			}
		}

		int mid_index = 180;
		ArrayList<Road> affectedRoadss = getAffectedRoads(mid_index);
		for (Road road : affectedRoadss) {
			removeRoad(road);
			road.addStone(BLACK);
			addRoad(road);
		}
	
	}

	public Road findWinMove(PieceColor whoseMove) {
		if(whoseMove == PieceColor.BLACK){
			if(playerRoads[4][0].size() > 0) {
//				return playerRoads[4][0].get(0);
				return playerRoads[4][0].iterator().next();
			}
			else if(playerRoads[5][0].size() > 0)
//				return playerRoads[5][0].get(0);
				return playerRoads[5][0].iterator().next();
		}
		else{
			if(playerRoads[0][4].size() > 0) {
//				return playerRoads[0][4].get(0);
				return playerRoads[0][4].iterator().next();
			}
			else if(playerRoads[0][5].size() > 0)
//				return playerRoads[0][5].get(0);
				return playerRoads[0][5].iterator().next();
		}
		return null;
	}
	public boolean noThreats(PieceColor whoseMove) {
		return true;
	}
	
	private int getPotentialThreats(PieceColor whoseMove) {
		return 0;
	}
	
	//获取将子下在位置pos时，受到影响的所有的有效路
	public ArrayList<Road> getAffectedRoads(int pos){
		//System.out.println("getAffectedRoads start");
		ArrayList<Road> affectedRoads = new ArrayList<>();
		for(int i = 0; i < 6; i++)
			for(int k = 0; k < 4; k++ ){
				int index = pos - _FORWARD[k] *i;
				if(index>=0 && index <SIDE*SIDE){
					if(roads[index][k].islegal() == true)
						affectedRoads.add(roads[index][k]);
				}
			}
		//System.out.println("getAffectedRoads end");
		return affectedRoads;
	}
	
	//将路road，从其所在的路表中删除
	public void removeRoad(Road road)
	{
		roads[road.getStartPos()][road.getDir()] = null;
		playerRoads[road.getBlackNum()][road.getWhiteNum()].remove(road);
	}
	
	//将路road，添加到相应的路表中
	public void addRoad(Road road)
	{
		roads[road.getStartPos()][road.getDir()] = road;
		if(road.getBlackNum() ==7)
			System.out.println(road.getStartPos());
		if(road.getWhiteNum() ==7)
			System.out.println(road.getStartPos());
		playerRoads[road.getBlackNum()][road.getWhiteNum()].add(road);
	}
	
	//基本路表： 以每个点为起点的路 一般有四个 横纵，斜上，斜下
	private Road[][] roads = new Road[SIDE * SIDE][4];

	//按黑白子个数划分的路表：包含0个至6个黑子, 0个至6个白子的路；简称为黑白路表
	//例如： playerRoads[3][2]表示含有3个黑子，2个白子的路
	private RoadSet[][] playerRoads = new RoadSet[7][7];
	
	//求完全的黑路和白路的个数
	public int[] black_roads() {
		int [] temp = new int[7];
		for(int i = 1; i < 7; i++){
			temp[i] = playerRoads[i][0].size();
		}
		return temp;
	}
	public int[] white_roads() {
		int [] temp = new int[7];
		for(int i = 1; i < 7; i++){
			temp[i] = playerRoads[0][i].size();
		}
		return temp;
	}
	//获取有四个子的路


	public final static int[] SCOREOFROAD = {0,9,520,2070,7890,10020,1000000};
	public final static int[] _SCOREOFROAD = {0,3,480,2670,3887,4900,1000000};

	public static int evaluateChessStatus(PieceColor pieceColor,
										  RoadTable roadTable) {
		int currentScore = 0;
		int blackRoadScore = 0;
		int whiteRoadScore = 0;
		int [] numberOfBlackRoad = roadTable.black_roads();
		int [] numberOfWhiteRoad = roadTable.white_roads();

		if (pieceColor == PieceColor.BLACK) {
			for (int i = 1; i < 6; i++) {
				blackRoadScore += numberOfBlackRoad[i] * SCOREOFROAD[i];
				whiteRoadScore += numberOfWhiteRoad[i] * _SCOREOFROAD[i];
			}
			currentScore = blackRoadScore - whiteRoadScore;
		}
		if (pieceColor == PieceColor.WHITE) {
			for (int i = 1; i < 6; i++) {
				blackRoadScore += numberOfBlackRoad[i] * _SCOREOFROAD[i];
				whiteRoadScore += numberOfWhiteRoad[i] * SCOREOFROAD[i];
			}
			currentScore = whiteRoadScore - blackRoadScore;
		}
		//System.out.print(currentScore);
		return currentScore;
	}


	public Road[][] getRoads() {
		return roads;
	}


	public void setRoads(Road[][] roads) {
		this.roads = roads;
	}


	public RoadSet[][] getPlayerRoads() {
		return playerRoads;
	}


	public void setPlayerRoads(RoadSet[][] playerRoads) {
		this.playerRoads = playerRoads;
	}

	public RoadTable() {
		// TODO Auto-generated constructor stub
		for (int i = 0; i < playerRoads.length; i++) {
			for (int j = 0; j < playerRoads[0].length; j++) {
				playerRoads[i][j] = new RoadSet();
			}
		}
	}

	public Road[] getRoads(int startPos) {
		return roads[startPos];
	}
}
