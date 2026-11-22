package org.firstinspires.ftc.teamcode.Auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Utility.Time;
import org.firstinspires.ftc.teamcode.parts.Driver;

@Autonomous(name = "Drive Right Auto (0s Delay)")
public class DriveRightAuto extends LinearOpMode {
    private final Time time = new Time();

    //Delay for the drive to start in seconds
    protected double startDelay = 0;

    @Override
    public void runOpMode() throws InterruptedException
    {
        Driver drive = new Driver(hardwareMap, telemetry);

        waitForStart();
        time.resetTime();

        try {
            while (opModeIsActive() && !isStopRequested()) {
                time.update();

                if(time.getTime() < startDelay) continue;

                double x_speed = 1;

                double curAngle = drive.GetYawRadians();
                //drive.positionTracker.getDirection();
                double[] fieldXY = drive.robotToFieldCentric(x_speed, 0, curAngle);

                double field_xspeed = fieldXY[0];

                // Change the variables here so we don't have to change them below
                x_speed = field_xspeed;

                double motorPower = 0.7; //Fixed max power scale
                double denominator = Math.max(Math.abs(x_speed), 1); //Scales powers so none exceed 1
                //Per-wheel power before the power multiplier
                double frontLeftPower = (x_speed) / denominator * motorPower;
                double backLeftPower = (-x_speed) / denominator * motorPower;
                double frontRightPower = (-x_speed) / denominator * motorPower;
                double backRightPower = (x_speed) / denominator * motorPower;

                drive.frontLeftMotor.setPower(frontLeftPower);
                drive.frontRightMotor.setPower(frontRightPower);
                drive.backLeftMotor.setPower(backLeftPower);
                drive.backRightMotor.setPower(backRightPower);

                if (time.getTime() > startDelay + 0.43) {
                    drive.resetPower();
                    break;
                }
            }
        } finally {
            drive.resetPower();
        }
    }

    @Autonomous(name = "Drive Right Auto (5s Delay)")
    public static class DriveRightAuto5 extends DriveRightAuto {
        @Override
        public void runOpMode() throws InterruptedException {
            startDelay = 5;
            super.runOpMode();
        }
    }
    @Autonomous(name = "Drive Right Auto (10s Delay)")
    public static class DriveRightAuto10 extends DriveRightAuto {
        @Override
        public void runOpMode() throws InterruptedException {
            startDelay = 10;
            super.runOpMode();
        }
    }
    @Autonomous(name = "Drive Right Auto (15s Delay)")
    public static class DriveRightAuto15 extends DriveRightAuto {
        @Override
        public void runOpMode() throws InterruptedException {
            startDelay = 15;
            super.runOpMode();
        }
    }
    @Autonomous(name = "Drive Right Auto (20s Delay)")
    public static class DriveRightAuto20 extends DriveRightAuto {
        @Override
        public void runOpMode() throws InterruptedException {
            startDelay = 20;
            super.runOpMode();
        }
    }
}
