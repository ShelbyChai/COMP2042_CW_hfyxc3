package test;

import java.awt.*;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Created by filippo on 04/09/16.
 *
 */
abstract public class Brick  {

    // These 2 var only used in cementBrick
    public static final int DEF_CRACK_DEPTH = 1;
    public static final int DEF_STEPS = 35;

    public static final int UP_IMPACT = 100;
    public static final int DOWN_IMPACT = 200;
    public static final int LEFT_IMPACT = 300;
    public static final int RIGHT_IMPACT = 400;

    // TODO Think what is the point of the name pass in?
    private String name;
    private int fullStrength;
    private int strength;
    private boolean broken;


    // TODO Note to refactor: Brick and Ball class have the same 3 variable declaration
    // Refactor: From protected to private (Shape brickFace)
    // Refactor: Added final attribute to borderColor, innerColor
    private Shape brickFace;
    // Refactor: Rename to borderColor and innerColor
    final private Color borderColor;
    final private Color innerColor;


    public Brick(String name, Point pos,Dimension size,Color borderColor,Color innerColor,int strength){

        broken = false;
        this.name = name;
        brickFace = makeBrickFace(pos,size);
        this.borderColor = borderColor;
        this.innerColor = innerColor;
        fullStrength = this.strength = strength;

    }

    protected abstract Shape makeBrickFace(Point pos,Dimension size);

    public  boolean setImpact(Point2D point , int dir){
        if(broken)
            return false;
        impact();
        return broken;
    }

    public abstract Shape getBrick();

    public Color getBorderColor(){
        return borderColor;
    }

    public Color getInnerColor(){
        return innerColor;
    }

    // Refactor: Rename b -> ball for meaningful naming convention
    public final int findImpact(Ball ball){
        if(broken)
            return 0;
        int out  = 0;
        if(getBrickFace().contains(ball.getRight()))
            out = LEFT_IMPACT;
        else if(getBrickFace().contains(ball.getLeft()))
            out = RIGHT_IMPACT;
        else if(getBrickFace().contains(ball.getUp()))
            out = DOWN_IMPACT;
        else if(getBrickFace().contains(ball.getDown()))
            out = UP_IMPACT;
        return out;
    }

    //Refactor: 'In Clean Code' conditionals should be expressed as positives: isBroken() return broken -> !broken
    public final boolean isBroken(){
        return !broken;
    }

    public void repair() {
        broken = false;
        strength = fullStrength;
    }

    public void impact(){
        strength--;
        broken = (strength == 0);
    }

    // New created getter
    public Shape getBrickFace() {
        return brickFace;
    }
}
