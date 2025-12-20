package stud.g22;

import core.board.PieceColor;
import core.game.Game;
import core.game.Move;

import java.util.ArrayList;

import static core.board.PieceColor.BLACK;
import static core.board.PieceColor.EMPTY;
import static core.game.Move.SIDE;
import static stud.g22.Road.DIRECTION;

public class AI extends core.player.AI {
    private BoardPro board = null;
    PieceColor color; //自己棋子的颜色
    private Move myMove; //移动的动作
    private static final int mx_depth = 2; //alpha beta最大深度
    ArrayList<MovePro> record = new ArrayList<>();
    private static final int mx = Integer.MAX_VALUE;
    //棋点的宽度
    public final static int WIDTH = 30;
    ArrayList<Integer> nodeList = new ArrayList<>();
    public boolean[] visable = new boolean[SIDE * SIDE];
    ArrayList<Node> nodesList = new ArrayList<>();
    public int XS = 0; //是否先手

    public AI() {
    }

    @Override
    public String name() {
        // AlphaCatV1
        return "G22";
    }

    @Override
    public void playGame(Game game) {
        super.playGame(game);
        board = new BoardPro();
    }

    @Override
    public Move findNextMove(Move opponentMove) {
        //当前是第一手出棋子
        if (opponentMove == null) {
            Move move = firstMove();
            board.makeMove(move);
            XS = 1;
            return move;
        }
        //把对方下的棋子加入到棋盘中
        board.makeMove(opponentMove);

//        System.out.println(XS);
        if (canWin() != null) { //如果能赢棋
            myMove = canWin();
            board.makeMove(myMove);
            return myMove;
        }

        //下面都是没有必赢的下棋点的情况
        myMove = null;
        color = board.whoseMove();

        //对方没威胁
        if (calculateThreats(color) == 0){
            //DTSS双威胁搜索，进攻，i表示威胁搜索的深度，最大为25
            for (int i = 3; i <= 25; i += 2) {
                if (record.size() >= 1) record.clear();
                if (DTSS(i)) {
                    board.makeMove(myMove);
                    return myMove;
                }
            }
        }

        if (record.size() >= 1) record.clear();
        //对方对我们有双威胁，需要进行防御
        if (calculateThreats(color) == 2){
            //双威胁防御
            ArrayList<MovePro> moves = twoThreatsDefense();
            moves.sort(MovePro.scoreComparator);
            myMove = moves.get(0); //选择score最大的动作move
        } else {
            //alphaBeta剪枝
            alphaBetaSearch(-mx, mx, mx_depth);
        }
        //当前没有威胁，选择对我们最优的点进行落子
        if (myMove == null){
            ArrayList<MovePro> m = freeAttack();
            m.sort(MovePro.scoreComparator);
            myMove = m.get(0);
        }

        board.makeMove(myMove);
        return myMove;
    }


    int calculateThreats(PieceColor color) {
        //把四路和五路的连子视为威胁
        RoadList fourList, fiveList;
        ArrayList <Integer> poslist = new ArrayList<>();
        boolean visit[] = new boolean[SIDE*SIDE];
        for (int i = 0; i < SIDE * SIDE; i++) visit[i] = false;

        //计算的是color的对手的颜色路，所有的四路和五路
        if (color == BLACK) {
            fourList = board.roadTable.getBwRoads()[0][4];
            fiveList = board.roadTable.getBwRoads()[0][5];
        } else {
            fourList = board.roadTable.getBwRoads()[4][0];
            fiveList = board.roadTable.getBwRoads()[5][0];
        }

        //不存在四路和五路，无威胁
        if (fiveList.size() + fourList.size() == 0) return 0;

        //始点和方向
        RoadList roadList = (fourList.size() == 0 ? fiveList : fourList);

        int start = roadList.get(0).getPos();
        int dir = roadList.get(0).getDir();

        for (int i = 0; i < 6; i++){
            //得到这条路上点的位置，并判断是否有棋子了
            int pos = start + DIRECTION[dir] * i;
            if (board.get(pos) != EMPTY) continue;

            //尝试下一颗，再计算威胁数，当改变棋盘的时候，four和five自动取最新值
            board.addRoads(pos, color);
            int t = fourList.size() + fiveList.size();
            board.removeRoads(pos,color);
            //如果威胁为0，表明是单威胁
            if (t == 0) return 1;
        }

        //否则再考虑是双还是多
        //找出所以能用来堵的位置，去重
        for (int i = 0; i < fiveList.size(); i++){
            start = fiveList.get(i).getPos();
            dir = fiveList.get(i).getDir();
            for (int j = 0; j < 6; j++){
                int pos = start + DIRECTION[dir] *j;
                if (board.get(pos) != EMPTY) continue;

                if (!visit[pos]){
                    visit[pos] = true;
                    poslist.add(pos);
                }
            }
        }

        for (int i = 0; i < fourList.size(); i++){
            start = fourList.get(i).getPos();
            dir = fourList.get(i).getDir();
            for (int j = 0; j < 6; j++){
                int pos = start + DIRECTION[dir] * j;
                if(board.get(pos) != EMPTY) continue;

                if (!visit[pos]){
                    visit[pos] = true;
                    poslist.add(pos);
                }
            }
        }

        boolean is2 = false;
        int temp = poslist.size();
        for (int i = 0; i < temp && !is2; i++){
            for (int j = i + 1; j < temp && !is2; j++){
                board.addRoads(poslist.get(i), color);
                board.addRoads(poslist.get(j), color);
                //如果可以下两个子，把所有四路、五路堵上，就是双威胁
                if (fourList.size() + fiveList.size() == 0){
                    is2 = true;
                }
                board.removeRoads(poslist.get(i), color);
                board.removeRoads(poslist.get(j), color);
            }
        }

        return (is2 ? 2 : 3);
    }

    //双威胁进攻
    ArrayList<MovePro> twoThreatsAttack() {
        RoadList twoList, threeList;
        ArrayList<MovePro> ansList = new ArrayList<>();
        nodeList.clear();

        for (int i = 0; i < SIDE * SIDE; i++) visable[i] = false;
        //可能的点 所在的路
        if(board.whoseMove() == BLACK){
            twoList = board.roadTable.getBwRoads()[2][0];
            threeList = board.roadTable.getBwRoads()[3][0];
        }else{
            twoList = board.roadTable.getBwRoads()[0][2];
            threeList = board.roadTable.getBwRoads()[0][3];
        }

        //潜力点
        for(Road road : twoList){
            for (int i = 0; i < 6; i++) {
                int pos = road.getPos() + DIRECTION[road.getDir()] *i;
                if (board.get(pos) != EMPTY)
                    continue;
                if (visable[pos] == false) {
                    nodeList.add(pos);
                    visable[pos] = true;
                }
            }
        }
        for(Road road: threeList){
            for (int i = 0; i < 6; i++) {
                int pos = road.getPos() + DIRECTION[road.getDir()] *i;
                if (board.get(pos) != EMPTY)
                    continue;
                if (visable[pos] == false) {
                    nodeList.add(pos);
                    visable[pos] = true;
                }
            }
        }
        int sz = nodeList.size();

        //得到威胁点
        for (int i = 0; i < sz; i++) {
            for (int j = i + 1; j < sz; j++) {
                //尝试走两个潜力点
                board.addRoads(nodeList.get(i),board.whoseMove());
                board.addRoads(nodeList.get(j),board.whoseMove());
                //如果此时威胁>=2,说明可行
                if (calculateThreats(board.whoseMove().opposite()) >= 2) {
                    MovePro tm = new MovePro(nodeList.get(i), nodeList.get(j));
                    ansList.add(tm);
                }
                board.removeRoads(nodeList.get(i), board.whoseMove());
                board.removeRoads(nodeList.get(j), board.whoseMove());
            }
        }

        return ansList;
    }

    //是否有必胜走法
    public MovePro canWin(){
        MovePro move = null;

        Road winRoad = board.roadTable.findColorWin(board.whoseMove());
        if (winRoad != null){
            System.out.println("win!");
            int t, step1 = -1, step2 = -1;
            int start = winRoad.getPos();
            int dir = winRoad.getDir();

            for(int i = 0; i < 6; i++){
                t = start + i* DIRECTION[dir];
                if(board.get(t) == EMPTY)
                    if (step1 < 0) step1 = t;
                    else if(step2 < 0) step2 = t;
            }

            //说明只需要下一颗就可以胜利，另一颗随便掷
            if (step2 < 0){
                for(int i = 0; i < SIDE * SIDE ;i++)
                    if(i != step1 && board.get(i) == EMPTY){
                        step2 = i;
                        break;
                    }
            }
            move = new MovePro(step1, step2);
        }
        return move;
    }

    //没有威胁，选最好的下
    public ArrayList<MovePro> freeAttack() {
        ArrayList<MovePro> moves = new ArrayList<>();
        nodesList.clear();
        findblanks();
        nodesList.sort(Node.scoreComparator);

        //限制搜索宽度
        int choose_num = Math.min(WIDTH, nodesList.size() / 2);
        int index = 0;

        for(Node node: nodesList){
            for (int i = ++index; i < choose_num; i++){
                MovePro move = new MovePro(node.getPos(),  nodesList.get(i).getPos());

                board.addRoads(move.index1(), board.whoseMove());
                board.addRoads(move.index2(), board.whoseMove());

                move.setScore(RoadTable.valueEstimate( board.whoseMove(),  board.roadTable,XS));

                board.removeRoads(move.index1(),  board.whoseMove());
                board.removeRoads(move.index2(),  board.whoseMove());

                moves.add(move);
            }
            choose_num--;
        }
        return moves;
    }

    void addOne(RoadList la, RoadList lb){
        for(int j = 0;  j < la.size(); j++){
            Road road = la.get(j);
            for (int i = 0; i < 6; i++) {
                int pos = road.getPos() + i * DIRECTION[road.getDir()];
                if (board.get(pos) != EMPTY)
                    continue;
                if (visable[pos] == false) {
                    board.addRoads(pos,board.whoseMove());
                    //能破除，
                    if (la.size() + lb.size() == 0){
                        nodeList.add(pos);
                    }
                    board.removeRoads(pos,board.whoseMove());
                    visable[pos] = true;
                }
            }
        }
    }

    //一个威胁
    ArrayList<MovePro> oneThreatsDefense(){
        RoadList fourList, fiveList;
        ArrayList<MovePro> ansList = new ArrayList<>();
        nodeList.clear();
        int average,sum = 0,num=0;
        for(int i = 0; i <SIDE*SIDE;i++) visable[i] = false;

        //对方的威胁
        if(board.whoseMove() == BLACK){
            fourList = board.roadTable.getBwRoads()[0][4];
            fiveList = board.roadTable.getBwRoads()[0][5];
        }else{
            fourList = board.roadTable.getBwRoads()[4][0];
            fiveList = board.roadTable.getBwRoads()[5][0];
        }

        for (int j = 0;  j < fourList.size(); j++){
            Road road = fourList.get(j);
            for (int i = 0; i < 6; i++) {
                int pos = road.getPos() + i * DIRECTION[road.getDir()];
                if (board.get(pos) != EMPTY)
                    continue;
                if (visable[pos] == false) {
                    board.addRoads(pos,board.whoseMove());
                    //能破除，
                    if (fourList.size() + fiveList.size() == 0){
                        nodeList.add(pos);
                    }
                    board.removeRoads(pos,board.whoseMove());
                    visable[pos] = true;
                }
            }
        }

        for (int j = 0;  j < fiveList.size(); j++){
            Road road = fiveList.get(j);
            for (int i = 0; i < 6; i++) {
                int pos = road.getPos() + i * DIRECTION[road.getDir()];
                if (board.get(pos) != EMPTY)
                    continue;
                if (visable[pos] == false) {
                    board.addRoads(pos,board.whoseMove());
                    //能破除，
                    if (fiveList.size() + fourList.size() == 0){
                        nodeList.add(pos);
                    }
                    board.removeRoads(pos,board.whoseMove());
                    visable[pos] = true;
                }
            }
        }

        //第二个棋子
        nodesList.clear();
        findblanks();
        nodesList.sort(Node.scoreComparator);

        //计算平均值的
        for (Node node:nodesList) sum = node.getScore() + sum;
        average = sum/nodesList.size();
        for(int i=nodesList.size()-1;i>=0;i--){
            if(nodesList.get(i).getScore() >= average) break;
            nodesList.remove(nodesList.size()-1);
        }


        for (Integer node:nodeList){
            for (Node nodes: nodesList){
                if (node != nodes.getPos()){
                    MovePro move = new MovePro(node, nodes.getPos());

                    board.addRoads(move.index1(),board.whoseMove());
                    board.addRoads(move.index2(),board.whoseMove());

                    move.setScore(RoadTable.valueEstimate(board.whoseMove(), board.roadTable,XS));

                    board.removeRoads(move.index1(),board.whoseMove());
                    board.removeRoads(move.index2(),board.whoseMove());

                    ansList.add(move);
                }
            }
        }

        return ansList;
    }
    //找出棋盘中未落子的地方
    void findblanks(){
        for (int i = 0; i < SIDE * SIDE; i++){
            if (board.myBoard[i] > 0 && board.get(i) == EMPTY){
                board.addRoads(i, board.whoseMove());
                int score = RoadTable.valueEstimate(board.whoseMove(), board.roadTable, XS);
                board.removeRoads(i, board.whoseMove());
                nodesList.add(new Node(i, score));
            }
        }
    }

//    void findblanks(Road road) {
//        for (int i = 0; i < 6; i++) {
//            int pos = road.getPos() + DIRECTION[road.getDir()] *i;
//            if (board.get(pos) != EMPTY)
//                continue;
//            if (visable[pos] == false) {
//                nodeList.add(pos);
//                visable[pos] = true;
//            }
//        }
//    }



    //两个威胁
    ArrayList<MovePro> twoThreatsDefense(){
        RoadList fourList,fiveList;
        for(int i = 0; i <SIDE*SIDE;i++) visable[i] = false;
        ArrayList<MovePro> ansList = new ArrayList<>();
        nodeList.clear();

        //对方的威胁
        if(board.whoseMove() == BLACK){
            fourList = board.roadTable.getBwRoads()[0][4];
            fiveList = board.roadTable.getBwRoads()[0][5];
        }else{
            fourList = board.roadTable.getBwRoads()[4][0];
            fiveList = board.roadTable.getBwRoads()[5][0];
        }

        //把威胁的空白块储存
        for(Road road : fourList){
            for (int i = 0; i < 6; i++) {
                int pos = road.getPos() + DIRECTION[road.getDir()]*i;
                if (board.get(pos) != EMPTY)
                    continue;
                if (visable[pos] == false) {
                    nodeList.add(pos);
                    visable[pos] = true;
                }
            }
        }
        for(Road road : fiveList){
            for (int i = 0; i < 6; i++) {
                int pos = road.getPos() + DIRECTION[road.getDir()]*i;
                if (board.get(pos) != EMPTY)
                    continue;
                if (visable[pos] == false) {
                    nodeList.add(pos);
                    visable[pos] = true;
                }
            }
        }
        //计算破除威胁的各种走法
        for (int i = 0; i < nodeList.size(); i++)
        {
            for (int j = i + 1; j < nodeList.size(); j++)
            {
                board.addRoads(nodeList.get(i),board.whoseMove());
                board.addRoads(nodeList.get(j),board.whoseMove());

                if (fourList.size() + fiveList.size() == 0){
                    MovePro move = new MovePro(nodeList.get(i), nodeList.get(j));
                    move.setScore(RoadTable.valueEstimate(board.whoseMove(), board.roadTable,XS));
                    ansList.add(move);
                }

                board.removeRoads(nodeList.get(i),board.whoseMove());
                board.removeRoads(nodeList.get(j),board.whoseMove());
            }
        }
        return ansList;
    }


//    void addTwoStep(RoadList list){
//        for(Road road : list){
//            for (int i = 0; i < 6; i++) {
//                int pos = road.getPos() + DIRECTION[road.getDir()]*i;
//                if (board.get(pos) != EMPTY)
//                    continue;
//                if (visable[pos] == false) {
//                    nodeList.add(pos);
//                    visable[pos] = true;
//                }
//            }
//        }
//    }
    ArrayList<MovePro> manyThreatsDefense(){
        RoadList fourList,fiveList;
        for(int i = 0; i <SIDE*SIDE;i++) visable[i] = false;
        ArrayList<MovePro> ansList = new ArrayList<>();
        nodeList.clear();

        //对方的威胁
        if(board.whoseMove() == BLACK){
            fourList = board.roadTable.getBwRoads()[0][4];
            fiveList = board.roadTable.getBwRoads()[0][5];
        }else{
            fourList = board.roadTable.getBwRoads()[4][0];
            fiveList = board.roadTable.getBwRoads()[5][0];
        }
        for(int i = 0; i <SIDE*SIDE;i++) visable[i] = false;
        //把威胁的空白块储存
        for(Road road : fourList){
            for (int i = 0; i < 6; i++) {
                int pos = road.getPos() + DIRECTION[road.getDir()]*i;
                if (board.get(pos) != EMPTY)
                    continue;
                if (visable[pos] == false) {
                    nodeList.add(pos);
                    visable[pos] = true;
                }
            }
        }
        for(Road road : fiveList){
            for (int i = 0; i < 6; i++) {
                int pos = road.getPos() + DIRECTION[road.getDir()]*i;
                if (board.get(pos) != EMPTY)
                    continue;
                if (visable[pos] == false) {
                    nodeList.add(pos);
                    visable[pos] = true;
                }
            }
        }
        int minT = fourList.size() + fiveList.size();

        for (int i = 0; i < nodeList.size(); i++)
        {
            for (int j = i + 1; j < nodeList.size(); j++)
            {
                board.addRoads(nodeList.get(i),board.whoseMove());
                board.addRoads(nodeList.get(j),board.whoseMove());
                int nowT = fourList.size() + fiveList.size();
                if (nowT < minT){
                    ansList.clear();
                    minT = nowT;
                    ansList.add(new MovePro(nodeList.get(i),nodeList.get(j)));
                }else if(nowT == minT) {
                    ansList.add(new MovePro(nodeList.get(i),nodeList.get(j)));
                }
                board.removeRoads(nodeList.get(i),board.whoseMove());
                board.removeRoads(nodeList.get(j),board.whoseMove());
            }
        }
        return ansList;
    }

    //深度威胁空间搜索
    boolean DTSS(int depth) {
        //最深了还没找到，不行咯
        if (depth == 0)
            return false;

        // 该对方了
        if (color != board.whoseMove()) {
            // 堵不住我们捏，赢
            if (calculateThreats(board.whoseMove()) >= 3) {
                myMove = record.get(0);
                return true;
            }
            // 对方进行防御，遍历所有方法
            ArrayList<MovePro> movesList = twoThreatsDefense();
            for (MovePro move : movesList) {
                board.makeMove(move);
                record.add(move);
                boolean flag = DTSS(depth - 1);
                record.remove(record.size() - 1);
                board.undoMove(move);

                if (!flag)  return false;
            }
            return true;
        }
        else { //轮到我们咯
            //如果对方有威胁，我方没有
            if (calculateThreats(color) > 0 && calculateThreats(color.opposite()) == 0)
                return false;

            // 所有可行的双威胁进攻
            ArrayList<MovePro> movesList = twoThreatsAttack();
            for (MovePro move : movesList) {
                board.makeMove(move);
                record.add(move);
                boolean flag = DTSS(depth - 1);
                record.remove(record.size() - 1);
                board.undoMove(move);
                if (flag) return true;
            }
            return false;
        }
    }

    public int alphaBetaSearch(int alpha, int beta, int depth) {
        int value, best = -Integer.MAX_VALUE;

        if (board.gameOver() || depth <= 0) {
            return RoadTable.valueEstimate(color, board.getRoadTable(),XS);
        }

        ArrayList<MovePro> moves = null;
        int threats = calculateThreats(board.whoseMove());

        if (threats == 0) {
            moves = freeAttack();
        } else if (threats == 1) {
            moves = oneThreatsDefense();
        } else if (threats == 2) {
            moves = twoThreatsDefense();
        } else {
            moves = manyThreatsDefense();
        }

        // 启发式排序
        moves.sort(MovePro.scoreComparator);

        for (MovePro move : moves) {
            board.makeMove(move);
            value = -alphaBetaSearch(-beta, -alpha, depth - 1);
            //回退上一步棋
            board.undoMove(move);

            if (value > best) {
                best = value;
                if (best > alpha) {
                    alpha = best;
                }
                //beta剪枝，beta指的父节点的beta值
                if (value >= beta) {
                    break;
                }
            }
            if (depth == mx_depth && value >= best) {
                board.makeMove(move);
                color = color.opposite();
                record.clear();
                if (!DTSS(5)) {
                    myMove = move;
                }
                color = color.opposite();
                //回退上一步棋
                board.undoMove(move);
            }
        }
        return alpha;
    }
}
