package stud.g12;

import core.board.Board;
import core.board.PieceColor;
import core.game.Move;

import java.util.ArrayList;

import static core.board.PieceColor.*;
import static core.game.Move.SIDE;
import static stud.g12.Road._FORWARD;


public class BoardPro extends Board {
	private void changeRoads(int pos) {
		changeRoads(pos, whoseMove());
	}
	private void unchangeRoads(Move move) {
		int index0 = move.index1();
		unchangeRoads(index0);
		int index1 = move.index2();
		unchangeRoads(index1);
	}

	private void unchangeRoads(int pos,PieceColor piece) {
		ArrayList<Road> affectedRoads = roadTable.getAffectedRoads(pos);
		if (affectedRoads.isEmpty())
			return;
		for (Road road : affectedRoads) {
			roadTable.removeRoad(road);
			road.removeStone(piece);
			roadTable.addRoad(road);
		}
	}
	private void unchangeRoads(int pos) {
		unchangeRoads(pos,whoseMove());
	}

	int countAllThreats(PieceColor color) {
		ArrayList <Integer> poslist = new  ArrayList<>();
		boolean visit[] = new boolean[SIDE*SIDE];
		for(int i = 0; i < SIDE*SIDE; i++) visit[i] =false;

		RoadSet four = (color == WHITE ? roadTable.getPlayerRoads()[4][0] : roadTable.getPlayerRoads()[0][4]);
		RoadSet five = (color == WHITE ? roadTable.getPlayerRoads()[5][0] : roadTable.getPlayerRoads()[0][5]);

		if (five.size() + four.size() == 0) return 0;

		RoadSet roadSet = (four.size() == 0 ? five : four);

		Road tp = roadSet.iterator().next();
		int pos_start = tp.getStartPos();
		int dir = tp.getDir();
		for (int i = 0; i < 6; i++){
			int pos = pos_start + _FORWARD[dir] * i;
			if(get(pos) != EMPTY) continue;
			changeRoads(pos,color);
			int t = four.size() + five.size();
			unchangeRoads(pos,color);

			if (t == 0) return 1;
		}
		//判断是双迫着还是多迫着，思路：找出所以可能用来堵的位置，遍历 看看存不存在 直接解决所有威胁的情况
		//visit控制防止重复添加点， 因为可能一个点的棋子堵住好几个威胁
		for (Road rd:five){
			 pos_start = rd.getStartPos();
			 dir = rd.getDir();
			for (int j = 0; j < 6; j++){
				int pos = pos_start + _FORWARD[dir] *j;
				if(get(pos) != EMPTY) continue;
				
				if (!visit[pos]){
					visit[pos] = true;
					poslist.add(pos);
				}
			}
		}

		for (Road rd:four){
			 pos_start = rd.getStartPos();
			 dir = rd.getDir();
			for (int j = 0; j < 6; j++){
				int pos = pos_start + _FORWARD[dir] * j;
				if(get(pos) != EMPTY) continue;
				
				if (!visit[pos]){
					visit[pos] = true;
					poslist.add(pos);
				}
			}
		}
		
		boolean flag = false;
		int temp = poslist.size();

		for (int i = 0; i < temp && !flag; i++){
			for (int j = i + 1; j < temp && !flag; j++){
				changeRoads(poslist.get(i), color);
				changeRoads(poslist.get(j), color);
				if (four.size() + five.size() == 0){
					flag = true;
				}
				unchangeRoads(poslist.get(i), color);
				unchangeRoads(poslist.get(j), color);
			}
		}
		return (flag ? 2 : 3);
    }

	ArrayList<Integer> pointlist = new ArrayList<>();
	private boolean[] vis = new boolean [SIDE * SIDE];
	//pointslist存储有可能下的位置和棋盘所具有的分数
	ArrayList<stud.g12.Node> pointslist = new ArrayList<>();

	void findblanks(){
		for(int i = 0; i < SIDE * SIDE; i++){
			if (battle[i] > 0 && get(i) == EMPTY){
				changeRoads(i);
				int score = stud.g12.RoadTable.evaluateChessStatus(whoseMove(), roadTable);
				unchangeRoads(i);
				pointslist.add(new stud.g12.Node(i, score));
			}				
		}
	}

	void findblanks(Road road) {
		for (int i = 0; i < 6; i++) {
			int pos = road.getStartPos() + _FORWARD[road.getDir()] * i;
			if (get(pos) != EMPTY)
				continue;
			if (vis[pos] == false) {
				pointlist.add(pos);
				vis[pos] = true;
			}
		}
	}

	public MovePro findwinMoves(){
		MovePro move = null;
		Road winroad = roadTable.findWinMove(whoseMove());
		if(winroad != null){
			int index,index0 =-1, index1 = -1;
			int startpos = winroad.getStartPos();
			int dir = winroad.getDir();
		    for(int i = 0; i < 6; i++){
		    	index = startpos + i* _FORWARD[dir];
		    	if(get(index) == EMPTY)
		    		if(index0 < 0) index0 = index;
		    		else if(index1<0) index1 = index;
		    }
		    if(index1 < 0){
		    	for(int i = 0; i < SIDE*SIDE ;i++){
		    		if(get(i) == EMPTY && i!= index0){
		    			index1 = i;
		    			break;
		    		}
		    	}
		    }
		    move = new MovePro(index0, index1);
		 }
		return move;
	}
	

	public  ArrayList<MovePro> findGenerateMoves() {
		ArrayList<MovePro> moves = new ArrayList<>();
		pointslist.clear();
		findblanks();
		//对所有有可能下的位置进行排序
		pointslist.sort(stud.g12.Node.scoreComparator);
		//筛选点，减少搜索时间
		int choose_num = Math.min(POINTSWIDTH, pointslist.size() / 2);
		int index = 0;
		for (stud.g12.Node node : pointslist){
			for (int i = ++index; i < choose_num; i++){
				MovePro move = new MovePro(node.getPos(), pointslist.get(i).getPos());
				changeRoads(move);
				move.setScore(stud.g12.RoadTable.evaluateChessStatus(whoseMove(), roadTable));
				unchangeRoads(move);
				moves.add(move);
			}
			choose_num--;
		}
		return moves;
	}

	ArrayList<MovePro> findDoubleThreats() {
		ArrayList<MovePro> movelist = new ArrayList<>();
		pointlist.clear();

		for (int i = 0; i < SIDE*SIDE; i++) vis[i] = false;
		RoadSet myTwo = (whoseMove() == BLACK ? roadTable.getPlayerRoads()[2][0] : roadTable.getPlayerRoads()[0][2]);
		RoadSet myThree = (whoseMove() == BLACK ? roadTable.getPlayerRoads()[3][0] : roadTable.getPlayerRoads()[0][3]);
		for (Road road : myTwo){
			findblanks(road);
		}
		for (Road road: myThree){
			findblanks(road);
		}
		int len = pointlist.size();

		for (int i = 0; i < len; i++) {
			for (int j = i + 1; j < len; j++) {
				changeRoads(pointlist.get(i));
				changeRoads(pointlist.get(j));
				if (countAllThreats(whoseMove().opposite()) >= 2) {
					MovePro testmove = new MovePro(pointlist.get(i), pointlist.get(j));
					movelist.add(testmove);
				}
				unchangeRoads(pointlist.get(i));
				unchangeRoads(pointlist.get(j));
			}
		}
		
		return movelist;
	}
	ArrayList<MovePro> findSingleBlocks(){
		ArrayList<Road> four,five;
		RoadSet R1,R2;
		ArrayList<MovePro> movelist = new ArrayList<>();
		pointlist.clear();
		for(int i = 0; i <SIDE*SIDE;i++) vis[i] = false;


		if(whoseMove() == BLACK){
			four = new ArrayList<Road> (roadTable.getPlayerRoads()[0][4]);
			five = new ArrayList<Road> (roadTable.getPlayerRoads()[0][5]);
			R1 = roadTable.getPlayerRoads()[0][4];
			R2 = roadTable.getPlayerRoads()[0][5];
		}else{
			four = new ArrayList<Road> (roadTable.getPlayerRoads()[4][0]);
			five = new ArrayList<Road> (roadTable.getPlayerRoads()[5][0]);
			R1 = roadTable.getPlayerRoads()[4][0];
			R2 = roadTable.getPlayerRoads()[5][0];
		}

		int lenfour = four.size(), lenfive = five.size();
        for(int j = 0;  j < lenfour; j++){
        	Road road = four.get(j);
        	for (int i = 0; i < 6; i++) {
				int pos = road.getStartPos() + i * _FORWARD[road.getDir()];
				if (get(pos) != EMPTY)
					continue;
				if (vis[pos] == false) {
					changeRoads(pos);
					//能破除，
					if (R1.size() + R2.size() == 0){
						pointlist.add(pos);
					}				
					unchangeRoads(pos);		
					vis[pos] = true;
				}
			}
        }
		for (int j =0 ; j < lenfive ; j++){
			Road road = five.get(j);
			for (int i = 0; i < 6; i++) {
				int pos = road.getStartPos() + i *_FORWARD[road.getDir()];
				if (get(pos) != EMPTY)
					continue;
				if (vis[pos] == false) {
					changeRoads(pos);
					//能破除，
					if (R1.size() + R2.size() == 0){
						pointlist.add(pos);
					}				
					unchangeRoads(pos);		
					vis[pos] = true;
				}
			}
		}
		pointslist.clear();
		findblanks();
		pointslist.sort(stud.g12.Node.scoreComparator);
		
		int sum = 0;
		for(stud.g12.Node node :pointslist){
			sum = node.getScore() + sum;
		}
		int average = sum/pointslist.size();
		int num = 0;
		for(int i = 0; i < pointslist.size();i++){
			if(pointslist.get(i).getScore() < average -1)
				num = i;
		}
		while(pointslist.size() > num){
			pointslist.remove(pointslist.size()-1);
		}
		
		for(Integer point:pointlist){
			for(stud.g12.Node points: pointslist){
				if(point != points.getPos()){
					MovePro move = new MovePro(point, points.getPos());
					changeRoads(move);
					move.setScore(stud.g12.RoadTable.evaluateChessStatus(whoseMove(), roadTable));
					unchangeRoads(move);
					movelist.add(move);
				}
			}	
		}
		return movelist;
	}

	ArrayList<MovePro> findDoubleBlocks(){
		ArrayList<MovePro> movelist = new ArrayList<>();
		pointlist.clear();
		for(int i = 0; i <SIDE*SIDE;i++) vis[i] = false;

		RoadSet four = (whoseMove() == WHITE ? roadTable.getPlayerRoads()[4][0] : roadTable.getPlayerRoads()[0][4]);
		RoadSet five = (whoseMove() == WHITE ? roadTable.getPlayerRoads()[5][0] : roadTable.getPlayerRoads()[0][5]);

		for(Road road : four){
			for (int i = 0; i < 6; i++) {
				int pos = road.getStartPos() + _FORWARD[road.getDir()]*i;
				if (get(pos) != EMPTY)
					continue;
				if (vis[pos] == false) {
					pointlist.add(pos);
					vis[pos] = true;
				}
			}
		}
		for(Road road: five){
			for (int i = 0; i < 6; i++) {
				int pos = road.getStartPos() + _FORWARD[road.getDir()]*i;
				if (get(pos) != EMPTY)
					continue;
				if (vis[pos] == false) {
					pointlist.add(pos);
					vis[pos] = true;
				}
			}
		}

		for (int i = 0; i < pointlist.size(); i++)
		{
			for (int j = i + 1; j < pointlist.size(); j++)
			{
				changeRoads(pointlist.get(i));
				changeRoads(pointlist.get(j));
				if (four.size() + five.size() == 0){
					MovePro move = new MovePro(pointlist.get(i), pointlist.get(j));
					move.setScore(stud.g12.RoadTable.evaluateChessStatus(whoseMove(), roadTable));
					movelist.add(move);
				}										
				unchangeRoads(pointlist.get(i));
				unchangeRoads(pointlist.get(j));
			}
		}

		return movelist;
	}
	ArrayList<MovePro> findTripleBlocks(){
		ArrayList<MovePro> movelist = new ArrayList<>();
		pointlist.clear();
		for(int i = 0; i <SIDE*SIDE;i++) vis[i] = false;

		RoadSet four = (whoseMove() == WHITE ? roadTable.getPlayerRoads()[4][0] : roadTable.getPlayerRoads()[0][4]);
		RoadSet five = (whoseMove() == WHITE ? roadTable.getPlayerRoads()[5][0] : roadTable.getPlayerRoads()[0][5]);
		int lenfour = four.size();
		int lenfive = five.size();
		for(int i = 0; i <SIDE*SIDE;i++) vis[i] = false;

		for(Road road : four){
			for (int i = 0; i < 6; i++) {
				int pos = road.getStartPos() + i *_FORWARD[road.getDir()];
				if (get(pos) != EMPTY)
					continue;
				if (vis[pos] == false) {
					pointlist.add(pos);
					vis[pos] = true;
				}
			}
		}
		for(Road road: five){
			for (int i = 0; i < 6; i++) {
				int pos = road.getStartPos() + _FORWARD[road.getDir()] *i ;
				if (get(pos) != EMPTY)
					continue;
				if (vis[pos] == false) {
					pointlist.add(pos);
					vis[pos] = true;
				}
			}
		}
		int minthreats = four.size() + five.size();

		for (int i = 0; i < pointlist.size(); i++)
		{
			for (int j = i + 1; j < pointlist.size(); j++)
			{
				changeRoads(pointlist.get(i));
				changeRoads(pointlist.get(j));
				int nowthreats = four.size() + five.size();
				if (nowthreats < minthreats){
					movelist.clear();
					minthreats = nowthreats;
					movelist.add(new MovePro(pointlist.get(i),pointlist.get(j)));
				}
				else if(nowthreats == minthreats) {
					movelist.add(new MovePro(pointlist.get(i),pointlist.get(j)));
				}						
				unchangeRoads(pointlist.get(i));
				unchangeRoads(pointlist.get(j));
			}
		}
		return movelist;
	}
	//棋子周围16个位置
	int[] arround = {1, 2, -1, -2, SIDE, SIDE * 2, -SIDE, -SIDE * 2,
			SIDE + 1, 2 * SIDE + 2, -SIDE - 1, -(2 * SIDE + 2),
			-SIDE + 1, 2 - 2 * SIDE, SIDE - 1 ,2 * SIDE - 2};
	int[] battle = new int[SIDE * SIDE];

	public final static int POINTSWIDTH = 40;

	private stud.g12.RoadTable roadTable = new stud.g12.RoadTable();

	public stud.g12.RoadTable getRoadTable() {
		return roadTable;
	}


	public BoardPro() {
		super();
		roadTable.clear();
		for(int i = 0; i < SIDE * SIDE; i++) battle[i] = 0;
		//最开始的黑子固定在中心位置
		updateBattleForMove(180);
	}
	public void updateBattleForMove(Move mov) {
		int index0 = mov.index1();
		updateBattleForMove(index0);
		int index1 = mov.index2();
		updateBattleForMove(index1);
	}
	public void updateBattleForUndo(Move mov) {
		int index0 = mov.index1();
		updateBattleForUndo(index0);
		int index1 = mov.index2();
		updateBattleForUndo(index1);
	}
	public void updateBattleForMove(int pos) {
		battle[pos]++;
		for(int i =0 ; i < 16; i++){
			int index = pos + arround[i];
			if(Move.validSquare(index))
				battle[index]++;
		}
	}
	public void updateBattleForUndo(int pos) {
		battle[pos]--;
		for(int i = 0; i < 16; i++){
			int index = pos + arround[i];
			if (Move.validSquare(index))
				battle[index]--;
		}
	}

	@Override
	public void makeMove(Move mov) {
		changeRoads(mov);
		super.makeMove(mov);
		updateBattleForMove(mov);
	}
	public void undoMove(Move mov) {
		super.undo();
		unchangeRoads(mov);
		updateBattleForUndo(mov);
	}

	private void changeRoads(Move move) {
		int index0 = move.index1();
		changeRoads(index0);
		int index1 = move.index2();
		changeRoads(index1);
	}
	private void changeRoads(int pos, PieceColor color) {
		ArrayList<Road> affectedRoads = roadTable.getAffectedRoads(pos);
		if (affectedRoads.isEmpty())
			return;
		for (Road road : affectedRoads) {
			roadTable.removeRoad(road);
			road.addStone(color);
			roadTable.addRoad(road);
		}
	}
}
