package org.firstinspires.ftc.teamcode.tuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Utility.Vector2;
import org.firstinspires.ftc.teamcode.parts.Driver;

//Tests for the x and y position of the bot, as well as the direction it's facing
public class PoseTest extends LinearOpMode {
    public Driver driver;

    public Vector2 pos; //In ticks

    public double direction; //In Degrees

    public double previousLeftDistance;
    public double previousRightDistance;

    public double deltaLeftDistance;
    public double deltaRightDistance;

    public void updatePosition()
    {

    }

    public void updateDirection()
    {
        deltaLeftDistance = driver.getFrontLeftDistance() - previousLeftDistance;
        deltaRightDistance = driver.getBackRightDistance() - previousRightDistance;
        direction += (deltaLeftDistance - deltaRightDistance) / driver.distanceBetweenWheels;

        previousLeftDistance = driver.getFrontLeftDistance();
        previousRightDistance = driver.getBackRightDistance();
    }

    public void runOpMode()
    {


    }
}
