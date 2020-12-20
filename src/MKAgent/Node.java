package MKAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static MKAgent.Main.mySide;

public class Node implements Comparable<Node> {
    // Describes how we get here, null for root node.
    private Move move;
    // Describes who should move next.
    private Side whosTurnNext;

    private Node parent;
    private Board board;
    private int noOfVisits;
    private double totalScore;
    private ArrayList<Node> children;

    public Node(int noOfVisits, double totalScore, Side whosTurnNext, Move move, Board board, Node parent, ArrayList<Node> children) {
        this.noOfVisits = noOfVisits;
        this.totalScore = totalScore;
        this.board = new Board(board);
        this.move = move;
        this.whosTurnNext = whosTurnNext;
        this.parent = parent;
        this.children = children;
    }

    public Node(Node node) {
        this.noOfVisits = node.noOfVisits;
        this.totalScore = node.totalScore;
        this.parent = node.parent;
        this.board = new Board(node.board);
        // Should be a deep copy.
        this.children = new ArrayList<>();
        this.children.addAll(node.children);
        this.whosTurnNext = node.whosTurnNext;
        this.move = node.move;
    }

    public boolean isLeafNode() {
        return this.children.size() == 0;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public Node getRandomChild() {
        return this.children.get((int)(Math.random() * this.children.size()));
    }

    // Setters, Getters
    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return this.parent;
    }

    public void setNoOfVisits(int noOfVisits) {
        this.noOfVisits = noOfVisits;
    }

    public int getNoOfVisits() {
        return this.noOfVisits;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public double getTotalScore() {
        return this.totalScore;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return this.board;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }

    public ArrayList<Node> getChildren() {
        return this.children;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public Move getMove() {
        return this.move;
    }

    public Side getWhosTurnNext() {
        return whosTurnNext;
    }

    public Double getUCTValue() {
        if (Kalah.gameOver(board))
            return (double)0;

        if (noOfVisits == 0)
            return Double.MAX_VALUE;

        /*
            UCB(Si) = avg(Vi) + c*sqrt(ln(N)/ni)
            Vi is average value of its children nodes
            c is constant (usually 2)
            N is total number of visits
            n is current node's number of visits
         */
        double visits = noOfVisits;
        if(this.move.getSide() == mySide)
            return (totalScore / visits + 2.5 * Math.sqrt(2 * Math.log(this.getParent().getNoOfVisits()) / visits));
        else
            return ((1 - totalScore / visits)
                    + 2.5 * Math.sqrt(2 * Math.log(this.getParent().getNoOfVisits()) / visits));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Node node = (Node) o;
        return Objects.equals(move, node.move) &&
                Objects.equals(parent, node.parent) &&
                Objects.equals(board, node.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(move, whosTurnNext, parent, board, noOfVisits, totalScore, children);
    }

    @Override
    public int compareTo(Node o) {
        return this.getUCTValue().compareTo(o.getUCTValue());
    }

    public boolean childrenAllVisited() {
        for(Node child : this.children){
            if(child.getNoOfVisits() == 0)
                return false;
        }
        return true;
    }

    public Node getRandomAvaliableChild() {
        for(Node child : this.children){
            if(child.getNoOfVisits() == 0)
                return child;
        }
        return this;
    }

    public Node getBestChild(){
        return Collections.max(this.children, (first, second) -> {
            double vit1 = first.getSecureValue();
            double vit2 = second.getSecureValue();
            return Double.compare(vit1, vit2);
        });

    }

    public double getSecureValue(){
        return (totalScore / noOfVisits - Math.sqrt(2 * Math.log(this.getParent().getNoOfVisits()) / noOfVisits));
    }
}
