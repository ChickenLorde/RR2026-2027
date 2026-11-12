package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Utility.Time;
import org.firstinspires.ftc.teamcode.Utility.Vector2;
import org.firstinspires.ftc.teamcode.parts.Driver;

public class PositionTracker {

    //Coords 0,0 is the back-left corner of the field
    private Driver drive;

    private double direction;
    private Vector2 pos;
    private Vector2 startPos;


    private double prevLeftDistance;
    private double prevRightDistance;

    private double deltaLeftDistance, deltaRightDistance;



    public PositionTracker(Driver drive)
    {
        this.drive = drive;
    }

    public PositionTracker(Driver drive, Vector2 startPos)
    {
        this.drive = drive;
        this.startPos = startPos;
        pos = new Vector2(startPos.x, startPos.y);
    }

    public void updateDirection()
    {
        deltaLeftDistance = drive.getFrontLeftDistance() - prevLeftDistance;
        deltaRightDistance = drive.getBackRightDistance() - prevRightDistance;

        //final double directionConst = 12.37222222;
        final double directionConst = 1;
        direction -= (deltaLeftDistance - deltaRightDistance)  / directionConst;

        //if(direction >= 360) direction -= 360;
        //
        // if(direction < 0) direction += 359;
        prevLeftDistance = drive.getFrontLeftDistance();
        prevRightDistance = drive.getBackRightDistance();
    }

    public void updatePosition()
    {
       pos.y += (deltaLeftDistance + deltaRightDistance) / 2.0 * Math.cos(direction * Math.PI / 180.0);
       pos.x += (deltaLeftDistance + deltaRightDistance) / 2.0 * Math.sin(direction * Math.PI / 180.0);
    }

    public double getDirection()
    {
        return direction;
    }

    public double getX() { return pos.x; }
    public double getY() { return pos.y; }
}
