package stud.g02;

import core.board.PieceColor;
import core.game.Game;
import core.game.Move;

import java.util.ArrayList;

public class AI extends core.player.AI {
	// 搜索的深度
	private static final int MAX_DEPTH = 3;
	/* 记录一下行棋的序列 */
	ArrayList<MovePro> moveOrder = new ArrayList<>();

	@Override
	public Move findNextMove(Move opponentMove) {
		if (opponentMove == null) {
			Move move = firstMove();
			board.makeMove(move);
			return move;
		} else {
			board.makeMove(opponentMove);
		}

		bestMove = board.findwinMoves();
		if (bestMove != null) {
			board.makeMove(bestMove);
			return bestMove;
		}

		// 记录我方颜色
		color = board.whoseMove();
		// 因为有findwinmoves的原因 所以从搜索到第三层开始
		moveOrder.clear();
		for (int i = 3; i <= 27; i += 2) {
			if (DTSS(i)) {
				board.makeMove(bestMove);

				return bestMove;
			}
		}

		alphaBeta(-Integer.MAX_VALUE, Integer.MAX_VALUE,1, MAX_DEPTH);

		if (bestMove == null) {
			//随意在棋盘上找位置，然后对每个位置进行排序
			ArrayList<MovePro> moves = board.findGenerateMoves();
			moves.sort(MovePro.scoreComparator);
			bestMove = moves.get(0);
		}
		board.makeMove(bestMove);
		return bestMove;
	}

	boolean DTSS(int depth) {
		// depth为0，搜索达到最大深度还没有找到连续双威胁的情况，return false；
		if (depth == 0)
			return false;
		// 当我方行棋时
		if (color == board.whoseMove()) {
			// 如果对方对我方存在威胁，但是我方对对方没有威胁
			if (board.countAllThreats(color) > 0 && board.countAllThreats(color.opposite()) == 0)
				return false;

			// 找到我方行棋成为双威胁的所有着法
			ArrayList<MovePro> movesList = board.findDoubleThreats();
			for (MovePro move : movesList) {
				board.makeMove(move);
				moveOrder.add(move);
				boolean flag = DTSS(depth - 1);
				moveOrder.remove(moveOrder.size() - 1);
				board.undoMove(move);
				// 根据算法，存在即可返回true
				if (flag)
					return true;
			}
			return false;
		}
		// 对方行棋时
		else {
			// 如果堵不住我方对于对方的威胁
			if (board.countAllThreats(board.whoseMove()) >= 3) {
				bestMove = moveOrder.get(0);
				return true;
			}
			// 找到对方用来堵的所有着法
			ArrayList<MovePro> movesList = board.findDoubleBlocks();
			for (MovePro move : movesList) {
				board.makeMove(move);
				moveOrder.add(move);
				boolean flag = DTSS(depth - 1);
				moveOrder.remove(moveOrder.size() - 1);
				board.undoMove(move);
				// 根据算法，必须全部可以出现双威胁，否则 搜索失败
				if (!flag)
					return false;
			}
			return true;
		}
	}

	public int alphaBeta(int alpha,int beta, int turn, int depth) {
		if (board.gameOver() || depth <= 0) {
			//叶子结点返回评价值
			int evaluateScore = RoadTable.evaluateChessScore(color, board.getRoadTable());
			return evaluateScore;
		}
		ArrayList<MovePro> moves = null;
		int threats = board.countAllThreats(board.whoseMove());
		if (threats == 0) {
			//对方对自己没有威胁，找一步对自己最优的棋下
			moves = board.findGenerateMoves();
		} else if (threats == 1) {
			//对方对自己有单威胁，需要进行单威胁防御
			moves = board.findSingleBlocks();
		} else if (threats == 2) {
			//对方对自己有双威胁，需要进行双威胁防御
			moves = board.findDoubleBlocks();
		} else {
			//对方对自己有三威胁，需要进行三威胁防御
			moves = board.findTripleBlocks();
		}

		//轮到自己下棋
		if (turn == 1){
			// 启发式排序
			int tAlpha;
			moves.sort(MovePro.scoreComparator);
			for (MovePro move : moves) {
				board.makeMove(move);
				tAlpha = alphaBeta(alpha, beta,0, depth - 1);
				board.undoMove(move);

				//子节点的beta > alpha，更新
				if (tAlpha > alpha){
					alpha = tAlpha;
					if (depth == MAX_DEPTH){
						board.makeMove(move);
						color = color.opposite();
						moveOrder.clear();
						// 加一步反向DTSS搜索 防止自己防御失误
						if (!DTSS(7)) {
							bestMove = move;
						}
						color = color.opposite();
						board.undoMove(move);
					}
				}
				//beta剪枝
				if (alpha >= beta){
					return beta;
				}
			}
			return alpha;
		} else {
			//min
			// 启发式排序
			int tBeta;
			moves.sort(MovePro.scoreComparator);
			for (MovePro move : moves) {
				board.makeMove(move);
				tBeta = alphaBeta(alpha, beta,1, depth - 1);
				board.undoMove(move);
				if (beta > tBeta){
					beta = tBeta;
				}
				//alpha剪枝
				if (alpha >= beta){
					return alpha;
				}
			}
			return beta;
		}
	}


	private Move bestMove;

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "stud.g02 AlphaCatV3";
	}

	@Override
	public void playGame(Game game) {
		super.playGame(game);
		board = new BoardPro();
	}

	// 自己保有的棋盘
	private BoardPro board = null;
	PieceColor color;
}
