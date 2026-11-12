package org.firstinspires.ftc.teamcode.Utility;

public class Pos2D {
    private int x, y, r;


    public Pos2D(int x, int y, int r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public Pos2D (int x, int y) {
        this.x = x;
        this.y = y;
        this.r = 0;
    }

    public Pos2D() {
        this.x = 0;
        this.y = 0;
        this.r = 0;
    }

    //Accessors
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getR() { return r; }

    //Mutators
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    public void setR(int r) { this.r = r; }
    public void set(int x, int y, int r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public Pos2D add(Pos2D other) {
        return new Pos2D(
        this.x + other.getX(),
        this.y + other.getY(),
        this.r + other.getR()
        );
    }

    public Pos2D subtract(Pos2D other) {
        return new Pos2D(
        this.x - other.getX(),
        this.y - other.getY(),
        this.r - other.getR()
        );
    }
}
