public class Square {
    private DoublePoint[] points;
    private int depth;

    public Square(DoublePoint[] points) {
        this.points = points;
    }

    public DoublePoint getPoint(int i){
        return this.points[i];
    }

    public DoublePoint[] getPoints(){
        return this.points;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
