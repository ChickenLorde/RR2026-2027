package org.firstinspires.ftc.teamcode.parts;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Utility.Pos2D;
import org.firstinspires.ftc.teamcode.Utility.Time;
import org.firstinspires.ftc.teamcode.Utility.Vector2;
import org.firstinspires.ftc.teamcode.mechanisms.PositionTracker;
import org.firstinspires.ftc.teamcode.subsystems.Robot;

import org.firstinspires.ftc.teamcode.subsystems.PID_Test;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

public class Driver {


    public DcMotor frontLeftMotor;
    public DcMotor backLeftMotor;
    public DcMotor frontRightMotor;
    public DcMotor backRightMotor;

    public DcMotor intake;

    public DcMotor outtake;
    public Servo outtakeIncline;
    public Servo outtakeBackplate;

    public double Kp, Ki, Kd;


    public final double outtakePower = 0.8;
    public final double ticksPerMeter = 1529.0;
    public final double distanceBetweenWheels = 0.495;

    public PositionTracker positionTracker;

    /* NOTES:
    * 500 ticks per wheel rotation
    * Distance between FL & BR wheels: 0.495m
    * Wheel Diameter: 0.104 m
    * Wheel Circumference: 0.327 m
    * 1 tick = 0.000654 m
    * 1 meter = 1529 ticks
     */

    Telemetry telemetry;
    public Driver(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        //Control buttons
        frontLeftMotor =  hardwareMap.dcMotor.get("frontLeft");
        backLeftMotor =  hardwareMap.dcMotor.get("backLeft");
        frontRightMotor =  hardwareMap.dcMotor.get("frontRight");
        backRightMotor =  hardwareMap.dcMotor.get("backRight");
        outtake = hardwareMap.dcMotor.get("outtake");
        outtakeIncline = hardwareMap.get(Servo.class,"incline");
        outtakeBackplate = hardwareMap.get(Servo.class, "backplate");
        intake = hardwareMap.dcMotor.get("intake");
        positionTracker = new PositionTracker(this, new Vector2(0, 0));


        Kp = 1.5;
        Ki = 0;
        Kd = 0;

        frontLeftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //backLeftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        //frontRightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        outtake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        outtake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //backLeftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //frontRightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        frontRightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backRightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        backLeftMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        //leftOuttake.setDirection(DcMotorSimple.Direction.REVERSE);
        //rightOuttake.setDirection(DcMotorSimple.Direction.FORWARD);


            }

    public void stopOuttake()
    {
        outtake.setPower(0);
    }

    public void moveTo(double x, double y)
    {
           double direction = Math.atan2(y-positionTracker.getY(),x-positionTracker.getX());

           turnTo(direction, false);
           int distance = (int)Math.sqrt(Math.pow(x - positionTracker.getX(), 2) + Math.pow(y - positionTracker.getY(), 2));
           move(distance,1);

    }

    public void easePower(DcMotor motor, double power, double deltaTime)
    {
        if(motor.getPower() < power) motor.setPower(motor.getPower() + 5 * deltaTime);
        else if (motor.getPower() > power) motor.setPower(motor.getPower() -5 * deltaTime);

        if(Math.abs(motor.getPower() - power) < 0.05) motor.setPower(power);
    }

    public void turnTo(double degrees, boolean updateTelemetry)
    {
        while(Math.abs(degrees - positionTracker.getDirection()) > 5)
        {
            double power = (positionTracker.getDirection() < degrees) ? -1.0 : 1.0;
                frontLeftMotor.setPower(-power);
                frontRightMotor.setPower(power);
                backLeftMotor.setPower(-power);
                backRightMotor.setPower(power);
                positionTracker.updateDirection();
                positionTracker.updatePosition();

            if(updateTelemetry)
            {

                telemetry.addData("Angle",positionTracker.getDirection());
                telemetry.update();
            }
        }


    }
    public void turnTo(double degrees)
    {
        while(Math.abs(degrees - positionTracker.getDirection()) > 5)
        {
            double power = (positionTracker.getDirection() < degrees) ? 0.5 : -0.5;
            frontLeftMotor.setPower(-power);
            frontRightMotor.setPower(power);
            backLeftMotor.setPower(-power);
            backRightMotor.setPower(power);
            positionTracker.updateDirection();
            positionTracker.updatePosition();
        }
    }
    public void move(int ticks, double power)
    {

        double startDistance = getDistance();
        PID_Test pid1 = new PID_Test(Kp, Ki, Kd);

        pid1.setTarget(ticks);
        double distance = Double.MAX_VALUE;
        Time time = new Time();
        while(Math.abs(ticks-distance) > 6)
        {
            time.update();
            distance = getDistance() - startDistance;

            double power1 = -pid1.calculate(distance);

            easePower(frontLeftMotor,power1 * 0.001 * power,time.getDeltaTime());
            easePower(frontRightMotor,power1 * 0.001 * power,time.getDeltaTime());
            easePower(backLeftMotor,power1 * 0.001 * power,time.getDeltaTime());
            easePower(backRightMotor,power1 * 0.001 * power,time.getDeltaTime());
            /*frontLeftMotor.setPower(power1 * 0.001 * power);
            frontRightMotor.setPower(power1 * 0.001 * power);
            backLeftMotor.setPower(power1 * 0.001 * power);
            backRightMotor.setPower(power1 * 0.001 * power);*/

            positionTracker.updatePosition();
            positionTracker.updateDirection();

            telemetry.addData("Target Distance",ticks);
            telemetry.addData("Distance",getDistance());
            telemetry.addData("Difference",Math.abs(ticks-distance));
            telemetry.addData("PID",power1);
            telemetry.update();
        }
        frontLeftMotor.setPower(0);
        frontRightMotor.setPower(0);
        backLeftMotor.setPower(0);
        backRightMotor.setPower(0);
    }

    public void turn(int ticks)
    {

        double startDistance = getDistance();
        PID_Test pid1 = new PID_Test(Kp, Ki, Kd);

        pid1.setTarget(ticks);
        double distance = Double.MAX_VALUE;
        while(Math.abs(ticks-distance) > 4)
        {
            distance = getTurnDistance();
            double power1 = -pid1.calculate(distance);
            frontLeftMotor.setPower(power1 * -0.001);
            frontRightMotor.setPower(power1 * 0.001);
            backLeftMotor.setPower(power1 * -0.001);
            backRightMotor.setPower(power1 * 0.001);
            positionTracker.updatePosition();
            positionTracker.updateDirection();

            telemetry.clearAll();
            telemetry.addData("Target Distance",ticks);
            telemetry.addData("Distance",getTurnDistance());
            telemetry.addData("Difference",Math.abs(ticks-distance));
            telemetry.addData("PID",power1);
            telemetry.update();
        }
        frontLeftMotor.setPower(0);
        frontRightMotor.setPower(0);
        backLeftMotor.setPower(0);
        backRightMotor.setPower(0);
    }

    public void moveTest(int ticks)
    {
        double startDistance = getDistance();
        double delta = Double.MAX_VALUE;
        double speed = 0.4;

        double difference = Double.MAX_VALUE;
        while(difference > 80)
        {
            delta = getDistance() - startDistance;
            double currentSpeed = (delta >= 0 ? -speed : speed);

            frontLeftMotor.setPower(currentSpeed);
            frontRightMotor.setPower(currentSpeed);
            backLeftMotor.setPower(currentSpeed);
            backRightMotor.setPower(currentSpeed);

            difference = Math.abs(ticks - delta);

            telemetry.addData("Delta",delta);
            telemetry.addData("Difference",difference);
            telemetry.update();

        }
    }

    public void forwardNoPID(int ticks)
    {

    }
    public double getDistance()
    {
        return (-frontLeftMotor.getCurrentPosition() - backRightMotor.getCurrentPosition()) / 2.0;
    }

    public double getTurnDistance()
    {
        return (2 * frontLeftMotor.getCurrentPosition() -  2 * backRightMotor.getCurrentPosition()) / 4.0;
    }

    public double getFrontLeftDistance()
    {
        return -frontLeftMotor.getCurrentPosition();
    }

    public double getBackRightDistance()
    {
        return -backRightMotor.getCurrentPosition();
    }

    public double ticksToMeters(int ticks)
    {
        return ticks/ticksPerMeter;
    }
    public void addTelemetry()
    {
        telemetry.addData("Left Power",frontLeftMotor.getPower());
        telemetry.addData("Left Power",frontRightMotor.getPower());
        telemetry.update();
    }

    public void resetPower()
    {
        frontLeftMotor.setPower(0);
        frontRightMotor.setPower(0);
        backLeftMotor.setPower(0);
        backRightMotor.setPower(0);
    }
}
