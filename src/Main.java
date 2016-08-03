import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Stack;

public class Main extends Application{
    public enum ColorMode{
        BLACK_WHITE, TREE, RANDOM
    }

    private Canvas canvas;
    private GraphicsContext graphicsContext;

    private int initialRectWidth = 40;
    private int initialRectHeight = 40;
    private Stack<Square> squareStack;

    private ColorMode colorMode = ColorMode.RANDOM;

    private boolean randomBranchSplit = true;
    private boolean calculateAllRects = false;

    private int calculationCycles = 20;

    private boolean onlySquares = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        int canvasWidth = 1200;
        int canvasHeight = 800;

        primaryStage.setTitle("Pythagoras Tree Algorithm");

        Group group = new Group();
        this.canvas = new Canvas(canvasWidth, canvasHeight);
        this.graphicsContext = this.canvas.getGraphicsContext2D();
        group.getChildren().add(this.canvas);

        this.squareStack = new Stack<>();

        this.initialRectWidth = 100;
        this.initialRectHeight = 100;

        DoublePoint points[] = new DoublePoint[4];

        int x1 = (canvasWidth - this.initialRectWidth) / 2;
        int x2 = (canvasWidth + this.initialRectWidth) / 2;
        int y = canvasHeight - this.initialRectHeight;

        points[0] = new DoublePoint(x1, canvasHeight);
        points[1] = new DoublePoint(x2, canvasHeight);
        points[2] = new DoublePoint(x2, y);
        points[3] = new DoublePoint(x1, y);

        Square firstSquare = new Square(points);
        firstSquare.setDepth(0);
        this.squareStack.add(firstSquare);

        long start = System.currentTimeMillis();
        while(!this.squareStack.isEmpty()){
            pythagorasTreeAlgorithm();
        }
        System.out.println("Calculation-Time: " + (System.currentTimeMillis() - start));

        Scene primaryScene = new Scene(group);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    public void pythagorasTreeAlgorithm(){
        Square square = this.squareStack.pop();

        double rectWidthX = square.getPoint(1).x - square.getPoint(0).x;
        double rectWidthY = square.getPoint(1).y - square.getPoint(0).y;

        double rectHeightX = square.getPoint(2).x - square.getPoint(1).x;
        double rectHeightY = square.getPoint(2).y - square.getPoint(1).y;

        if(square.getDepth() >= this.calculationCycles)
            return;

        if(!this.calculateAllRects && (getMagnitude(new DoublePoint(rectWidthX, rectWidthY)) < 0.1 || getMagnitude(new DoublePoint(rectHeightX, rectHeightY)) < 0.1))
            return;

        //Creating the left Square
        Random random = new Random();

        Color setColor;
        switch (this.colorMode){
            case BLACK_WHITE:
                setColor = Color.BLACK;
                break;
            case RANDOM:
                setColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                break;
            case TREE:
                int green = square.getDepth() * 12;
                if(green < 255){
                    setColor = Color.rgb(255 - square.getDepth() * 10, green, 0);
                }else{
                    setColor = Color.DARKGREEN;
                }
                break;
            default:
                setColor = Color.PINK;
        }

        graphicsContext.setStroke(setColor);

        //Find a divider-point on the squares upper edge
        double dividerPointT;
        if(this.randomBranchSplit){
            dividerPointT = random.nextDouble();
        }else{
            dividerPointT = 0.5;
        }

        double dividerPointX = (1 - dividerPointT) * square.getPoint(2).x + dividerPointT * square.getPoint(3).x;
        double dividerPointY = (1 - dividerPointT) * square.getPoint(2).y + dividerPointT * square.getPoint(3).y;
        DoublePoint dividerPointCoordinates = new DoublePoint(dividerPointX, dividerPointY);

        //Calculate the height needed to create the new two squares
        double p = getMagnitude(new DoublePoint(dividerPointCoordinates.x - square.getPoint(3).x, dividerPointCoordinates.y - square.getPoint(3).y));
        double q = getMagnitude(new DoublePoint(square.getPoint(2).x - dividerPointCoordinates.x, square.getPoint(2).y - dividerPointCoordinates.y));
        double h = Math.sqrt(p * q);

        //Left Square Second Point
        DoublePoint leftSquareP2 = new DoublePoint(dividerPointX, dividerPointY);
        DoublePoint directionVectorP3DP = new DoublePoint(dividerPointX - square.getPoint(3).x, dividerPointY - square.getPoint(3).y);
        double helperMag = getMagnitude(directionVectorP3DP);
        directionVectorP3DP.setLocation(directionVectorP3DP.x * h / helperMag, directionVectorP3DP.y * h / helperMag);
        leftSquareP2.setLocation(leftSquareP2.x + directionVectorP3DP.y, leftSquareP2.y - directionVectorP3DP.x);

        //Left Square Third Point
        DoublePoint normalVectorLP2P2 = new DoublePoint(leftSquareP2.y - square.getPoint(3).y, -(leftSquareP2.x - square.getPoint(3).x));
        DoublePoint leftSquareP3 = new DoublePoint(leftSquareP2.x + normalVectorLP2P2.x, leftSquareP2.y + normalVectorLP2P2.y);

        //Left Square Fourth Point
        DoublePoint directionVectorP3LP2 = new DoublePoint(square.getPoint(3).x - leftSquareP2.x, square.getPoint(3).y - leftSquareP2.y);
        DoublePoint leftSquareP4 = new DoublePoint(leftSquareP3.x + directionVectorP3LP2.x, leftSquareP3.y + directionVectorP3LP2.y);

        //Create Square Points Array
        DoublePoint[] firstSquarePoints = new DoublePoint[4];
        firstSquarePoints[0] = square.getPoint(3);
        firstSquarePoints[1] = leftSquareP2;
        firstSquarePoints[2] = leftSquareP3;
        firstSquarePoints[3] = leftSquareP4;

        Square firstSquare = new Square(firstSquarePoints);
        firstSquare.setDepth(square.getDepth() + 1);
        this.squareStack.push(firstSquare);

        //Creating the right square
        //Right Square Third Point
        DoublePoint normalVectorP2LP2 = new DoublePoint(square.getPoint(2).y - leftSquareP2.y, -square.getPoint(2).x + leftSquareP2.x);
        DoublePoint rightSquareP3 = new DoublePoint(square.getPoint(2).x + normalVectorP2LP2.x, square.getPoint(2).y + normalVectorP2LP2.y);

        //Right Square Fourth Point
        DoublePoint directionVectorRP3P2 = new DoublePoint(rightSquareP3.x - square.getPoint(2).x, rightSquareP3.y - square.getPoint(2).y);
        DoublePoint rightSquareP4 = new DoublePoint(leftSquareP2.x + directionVectorRP3P2.x, leftSquareP2.y + directionVectorRP3P2.y);

        //Create Square Points Array
        DoublePoint[] secondSquarePoints = new DoublePoint[4];
        secondSquarePoints[0] = leftSquareP2;
        secondSquarePoints[1] = square.getPoint(2);
        secondSquarePoints[2] = rightSquareP3;
        secondSquarePoints[3] = rightSquareP4;

        Square secondSquare = new Square(secondSquarePoints);
        secondSquare.setDepth(square.getDepth() + 1);
        this.squareStack.push(secondSquare);

        DoublePoint b = null;
        for(int i = 0; i < 3; ++i){
            DoublePoint a = square.getPoint(i);
            b = square.getPoint(i + 1);

            graphicsContext.strokeLine(a.x, a.y, b.x, b.y);
        }
        DoublePoint a = square.getPoint(0);
        graphicsContext.strokeLine(a.x, a.y, b.x, b.y);
    }

    public static double getMagnitude(DoublePoint d){
        return Math.sqrt(Math.pow(d.x, 2) + Math.pow(d.y, 2));
    }
}
