package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Utility.Pos2D;
import org.firstinspires.ftc.teamcode.parts.Driver;

public class Robot {

    public Driver driver;

    private int armPos = 0;
    private int slidesPos = 500;

    private Pos2D drivePos;

    Telemetry telemetry;

    public Robot(HardwareMap hardwareMap, Telemetry telemetry){
        driver = new Driver(hardwareMap, telemetry);
        drivePos = new Pos2D(0, 0, 0);
        this.telemetry = telemetry;
    }

    public void waitSeconds(double seconds) {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < (seconds * 1000)) {
            telemetry.update();
            //drive.toPosition(drivePos);
        }
    }

    public int testAngle(/*int constant, */int b, int m, double slideLength) {
        /*final double angleToTicks = constant;
        final double height = 1600;
        double angle = (90 - Math.toDegrees(Math.acos(height/slideLength))) * angleToTicks;
        return -1000 - angle;*/

        return (int)(b + (Math.sqrt(slideLength) * m));
    }
}
