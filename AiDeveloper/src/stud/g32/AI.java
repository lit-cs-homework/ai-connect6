package stud.g32;

import core.board.Board;
import core.board.PieceColor;
import core.game.Game;
import core.game.Move;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AI extends core.player.AI {
    private final int mode;
    private int steps;
    private final Random rnd = new Random();
    private static final int BOARD_SIZE = 19;

    public AI(int mode) {
        this.mode = mode;
    }
    public AI() {
        this.mode = 2; // 默认模式 2
    }

    @Override
    public Move findNextMove(Move opponentMove) {
        if (opponentMove != null) {
            this.board.makeMove(opponentMove);
        }

        Move myMove = (mode == 3) ? findMode3Move() : findMode2Move();

        // 执行落子并返回
        this.board.makeMove(myMove);
        steps++;
        return myMove;
    }

    /**
     * 走法 2:
     * 1. 随机掷骰子确定第一子位置。
     * 2. 第二子在第一子相邻的空位随机选。
     * 3. 若无相邻空位，则全盘随机选。
     */
    private Move findMode2Move() {
        List<Integer> allEmpties = getAllEmptyIndices(); // 获取第一子候选位置
        if (allEmpties.isEmpty()) return null;

        // 1. 随机第一子
        int idx1 = allEmpties.get(rnd.nextInt(allEmpties.size()));

        // 2. 尝试寻找邻位空位
        List<Integer> neighbors = getEmptyNeighbors(idx1); // 获取邻位空位列表
        int idx2;

        if (!neighbors.isEmpty()) {
            idx2 = neighbors.get(rnd.nextInt(neighbors.size())); // 随机选一个邻位
        } else {
            // 3. 若无邻位，全盘随机选一个（避开 idx1）
            List<Integer> remainingEmpties = new ArrayList<>(allEmpties); //第二子候选位置
            remainingEmpties.remove(Integer.valueOf(idx1));
            if (remainingEmpties.isEmpty()) {
                idx2 = idx1; // 极特殊情况：棋盘只有一个位置
            } else {
                idx2 = remainingEmpties.get(rnd.nextInt(remainingEmpties.size()));
            }
        }

        return createMove(idx1, idx2);
    }

    /**
     * 走法 3:
     * 1. 在中心 13x13 区域尝试掷骰子。
     * 2. 若连续 10 次不中（位置已被占），则在 19x19 范围内寻找。
     */
    private Move findMode3Move() {
        // 获取第一子
        int idx1 = pickMode3SingleIndex(-1);
        // 获取第二子（不能与第一子相同）
        int idx2 = pickMode3SingleIndex(idx1);

        return createMove(idx1, idx2);
    }

    /**
     * 走法 3 的单子选择逻辑
     * @param excludeIdx 需要排除的索引（选第二子时使用）
     */
    private int pickMode3SingleIndex(int excludeIdx) {
        // 1. 尝试在中心 13x13 区域找（row/col 从 3 到 15）
        for (int i = 0; i < 10; i++) {
            int r = 3 + rnd.nextInt(13);
            int c = 3 + rnd.nextInt(13);
            int idx = r * BOARD_SIZE + c;
            if (isAvailable(idx, excludeIdx)) {
                return idx;
            }
        }

        // 2. 连续 10 次未中，在全盘洗牌找空位
        List<Integer> allEmpties = getAllEmptyIndices();
        if (excludeIdx != -1) allEmpties.remove(Integer.valueOf(excludeIdx)); //如果该第二子落子，则排除掉第一子位置

        if (allEmpties.isEmpty()) return (excludeIdx != -1) ? excludeIdx : 0; // 极特殊情况：棋盘无空位

        Collections.shuffle(allEmpties); // 洗牌随机化
        return allEmpties.get(0); // 返回第一个空位
    }

    // --- 辅助工具方法 ---
    /*  * 检查指定索引位置是否可用（不等于 excludeIdx 且为空）
     */
    private boolean isAvailable(int idx, int excludeIdx) {
        return idx != excludeIdx && board.get_board()[idx] == PieceColor.EMPTY;
    }

    private List<Integer> getAllEmptyIndices() {
        List<Integer> list = new ArrayList<>();
        PieceColor[] cells = board.get_board();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == PieceColor.EMPTY) list.add(i);
        }
        return list;
    }

    private List<Integer> getEmptyNeighbors(int index) {
        List<Integer> neighbors = new ArrayList<>();
        int r = index / BOARD_SIZE; // row
        int c = index % BOARD_SIZE; // col

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr;
                int nc = c + dc;
                if (nr >= 0 && nr < BOARD_SIZE && nc >= 0 && nc < BOARD_SIZE) {
                    int nIdx = nr * BOARD_SIZE + nc;
                    if (board.get_board()[nIdx] == PieceColor.EMPTY) { // 空位
                        neighbors.add(nIdx);
                    }
                }
            }
        }
        return neighbors;
    }

    private Move createMove(int idx1, int idx2) {
        return new Move(idx1,idx2);
    }



    @Override
    public void playGame(Game game) {
        super.playGame(game);
        board = new Board(name() + "Board");
        steps = 0;
    }

    public String name() {
        return "G02-RandomAI-V2(Mode" + mode + ")";
    }
}
