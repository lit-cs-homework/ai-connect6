package stud.g12;

import core.board.PieceColor;
import core.game.Game;
import core.game.Move;

import java.util.ArrayList;

public class AI extends core.player.AI {
	private Move bestMove;

	@Override
	public String name() { /*AlphaCatV2*/return "G12";}

	@Override
	public void playGame(Game game) {
		super.playGame(game);
		board = new BoardPro();
	}
	private BoardPro board = null;
	PieceColor color;

	private static final int MAX_DEPTH = 3;
	ArrayList<MovePro> moveOrder = new ArrayList<>();

	public AI() {
	}

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
			ArrayList<MovePro> moves = board.findGenerateMoves();
			moves.sort(MovePro.scoreComparator);
			bestMove = moves.get(0);
		}
		board.makeMove(bestMove);
		return bestMove;
	}

	boolean DTSS(int depth) {
		if (depth == 0)
			return false;
		if (color == board.whoseMove()) {
			// 如果对方对我方存在威胁，但是我方对对方没有威胁
			if (board.countAllThreats(color) > 0 && board.countAllThreats(color.opposite()) == 0)
				return false;

			ArrayList<MovePro> movesList = board.findDoubleThreats();
			for (MovePro move : movesList) {
				board.makeMove(move);
				moveOrder.add(move);
				boolean flag = DTSS(depth - 1);
				moveOrder.remove(moveOrder.size() - 1);
				board.undoMove(move);
				if (flag)
					return true;
			}
			return false;
		} else {
			if (board.countAllThreats(board.whoseMove()) >= 3) {
				bestMove = moveOrder.get(0);
				return true;
			}
			ArrayList<MovePro> movesList = board.findDoubleBlocks();
			for (MovePro move : movesList) {
				board.makeMove(move);
				moveOrder.add(move);
				boolean flag = DTSS(depth - 1);
				moveOrder.remove(moveOrder.size() - 1);
				board.undoMove(move);

				if (!flag)
					return false;
			}
			return true;
		}
	}

	public int alphaBeta(int alpha, int beta, int turn, int depth) {
		//用评估函数来计算叶子结点的得分
		if (board.gameOver() || depth <= 0) {
			int evaluateScore = stud.g12.RoadTable.evaluateChessStatus(color, board.getRoadTable());
			return evaluateScore;
		}
		ArrayList<MovePro> moves = null;
		int threats = board.countAllThreats(board.whoseMove());
		if (threats == 0) {
			moves = board.findGenerateMoves();
		} else if (threats == 1) {
			moves = board.findSingleBlocks();
		} else if (threats == 2) {
			moves = board.findDoubleBlocks();
		} else {
			moves = board.findTripleBlocks();
		}

		//该我方落子
		if (turn == 1){
			int tAlpha;
			moves.sort(MovePro.scoreComparator);
			for (MovePro move : moves) {
				board.makeMove(move);
				tAlpha = alphaBeta(alpha, beta,0, depth - 1);
				board.undoMove(move);

				//子节点的alpha值大于父结点的alpha值，更新
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
		}
		//该对面落子
		else{
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
}
