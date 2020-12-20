package MKAgent;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * The main application class. It also provides methods for communication
 * with the game engine.
 */
public class Main {
    /**
     * Input from the game engine.
     */
    private static final Reader input = new BufferedReader(new InputStreamReader(System.in));
    public static Side mySide;
    private static Side oppSide;

    /**
     * Sends a message to the game engine.
     * @param msg The message.
     */
    public static void sendMsg(String msg) {
        System.out.print(msg);
        System.out.flush();
    }

    /**
     * Receives a message from the game engine. Messages are terminated by
     * a '\n' character.
     * @return The message.
     * @throws IOException if there has been an I/O error.
     */
    public static String recvMsg() throws IOException {
        StringBuilder message = new StringBuilder();
        int newCharacter;

        do {
            newCharacter = input.read();
            if (newCharacter == -1)
                throw new EOFException("Input ended unexpectedly.");
            message.append((char)newCharacter);
        } while((char)newCharacter != '\n');

        return message.toString();
    }

    private static Node selectionAndExpansion(Node node) {
        //if node has children which is all visited, check next level.
        if(!node.isLeafNode()){
            if(node.childrenAllVisited())
                return selectionAndExpansion(Collections.max(node.getChildren(),
                                             Comparator.comparing(Node::getUCTValue)));
            else
                return node.getRandomAvaliableChild();
        }

        //if node is has no children, expand it
        return expansion(node);
    }

    private static Node expansion(Node leafNode) {
        Board board = leafNode.getBoard();
        for (int i = 1; i <= board.getNoOfHoles(); i++) {
            Board nodeBoard = new Board(board);
            Move nodeMove = new Move(leafNode.getWhosTurnNext(), i);
            if (Kalah.isLegalMove(nodeBoard, nodeMove)) {
                Side turn = Kalah.makeMove(nodeBoard, nodeMove);
                Node child = new Node(0, 0, turn, nodeMove, nodeBoard, leafNode, new ArrayList<>());
                leafNode.addChild(child);
            }
        }

        if (leafNode.getChildren().size() == 0) {
            return leafNode;
        }
        return leafNode.getChildren().get(new Random().nextInt(leafNode.getChildren().size()));
    }

    private static void rolloutAndBackPropagation(Node node) {
        Node simulateNode = new Node(node);
        Board board = simulateNode.getBoard();
        Side side = simulateNode.getWhosTurnNext();
        int dept = 0;
        while(!Kalah.gameOver(board) && dept < 4)
        {
            ArrayList<Move> legalMoves = Kalah.getAllLegalMoves(board, side);
            Move next_move = legalMoves.get(new Random().nextInt(legalMoves.size()));
            side = Kalah.makeMove(board, next_move);
            dept++;
        }
        double result;
        if(board.weighted_payoff(mySide) > 0)
            result = 1.0;
        else if(board.weighted_payoff(mySide) == 0)
            result = 0.0;
        else
            result = -1.0;
        backPropagation(node, result);

        backPropagation(node, result);
    }

    private static void backPropagation(Node node, double payoff) {
        node.setNoOfVisits(node.getNoOfVisits() + 1);
        node.setTotalScore(node.getTotalScore() + payoff);
        Node parent = node.getParent();
        if (parent != null)
            backPropagation(parent, payoff);
    }

    private static Node getMaxRobustChild(Node root) {
        ArrayList<Node> children = root.getChildren();
        double maxVisited = -1;
        double maxReward = -Double.MAX_VALUE;
        for (Node child : children) {
            double childVisited = child.getNoOfVisits();
            double childReward = child.getTotalScore() / childVisited;
            if (childVisited > maxVisited)
                maxVisited = childVisited;
            if (childReward > maxReward)
                maxReward = childReward;
        }
        for (Node child : children) {
            double childVisited = child.getNoOfVisits();
            double childReward = child.getTotalScore() / childVisited;
            if (childVisited == maxVisited && childReward == maxReward)
                return child;
        }
        return null;
    }


    private static Move MCTSNextMove(Board board, long timeAllowed) {
        int generation = 0;
        final int GEN_LIMIT = Integer.MAX_VALUE;

        long endTime = System.currentTimeMillis() + timeAllowed;

        Node root = new Node(0, 0, mySide, null, board, null, new ArrayList<>());

        Node bestChild = null;

        boolean inLimit = true;
        while (inLimit || bestChild == null) {
            inLimit = System.currentTimeMillis() < endTime && generation < GEN_LIMIT;
            generation++;

            // Selection and Expansion.
            Node nodeToExplore = selectionAndExpansion(root);

            // Rollout and BackPropagation.
            rolloutAndBackPropagation(nodeToExplore);

            if (!inLimit) {
                bestChild = getMaxRobustChild(root);
                if(bestChild != null)
                    return bestChild.getMove();
                else
                    return root.getBestChild().getMove();

            }
        }
        return bestChild.getMove();
    }

    /**
     * The main method, invoked when the program is started.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        boolean may_swap = false;

        // Record the board locally.
        Kalah kalah = new Kalah(new Board(7,7));

        long timeAllowed = 3000;

        try {
            String msg = recvMsg();
            MsgType msg_type = Protocol.getMessageType(msg);

            /*
             Start of the game.

             Determine who is on which side.
             If this side is South, then make a move first;
             If this side is North, then enable may_swap.
            */
            switch (msg_type) {
                case START:
                    System.err.println("A start.");
                    boolean south = Protocol.interpretStartMsg(msg);
                    System.err.println("Starting player? " + south);
                    if (south) {
                        mySide = Side.SOUTH;
                        oppSide = Side.NORTH;
                        sendMsg(Protocol.createMoveMsg(5));
                    } else {
                        mySide = Side.NORTH;
                        oppSide = Side.SOUTH;
                        may_swap = true;
                    }
                    break;
                case END:
                    System.err.println("An end. Bye bye!");
                    return;
                default:
                    System.err.println("State message expected");
                    break;
            }

            // Continues the game
            while (true) {
                System.err.println();
                msg = recvMsg();
                System.err.print("Received: " + msg);

                msg_type = Protocol.getMessageType(msg);

                if (msg_type == MsgType.END)
                    return;

                if (msg_type != MsgType.STATE)
                    throw new InvalidMessageException("State message expected");

                Protocol.MoveTurn r = Protocol.interpretStateMsg(msg, kalah.getBoard());

                if (r.move == -1) {
                    mySide = mySide.opposite();
                    oppSide = oppSide.opposite();
                }

                if (!r.again) {
                    continue;
                }

                if (may_swap) {
                    may_swap = false;
                    if (r.move <= 2) {
                        mySide = mySide.opposite();
                        oppSide = oppSide.opposite();
                        sendMsg(Protocol.createSwapMsg());
                        continue;
                    }
                }

                // Calculate next move using MCTS
                Move next_move = MCTSNextMove(kalah.getBoard(), timeAllowed);
                msg = Protocol.createMoveMsg(next_move.getHole());

                // send message to game engine.
                sendMsg(msg);
            }
        }
        catch (InvalidMessageException e) {
            System.err.println(e.getMessage());
        }
        catch (IOException e)
        {
            System.err.println("This shouldn't happen: " + e.getMessage());
        }
    }
}
