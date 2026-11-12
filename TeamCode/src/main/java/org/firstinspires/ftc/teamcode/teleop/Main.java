package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Utility.Time;

import org.firstinspires.ftc.teamcode.Utility.Vector2;
import org.firstinspires.ftc.teamcode.parts.Driver;

import java.util.Locale;

@TeleOp(name = "Main TeleOp",group = "Competition Modes")
public class Main extends LinearOpMode {
    public void waitSeconds(double seconds) {
        long startTime = System.currentTimeMillis();
        while(true)
            if (System.currentTimeMillis() - startTime >= seconds * 1000)
                return;
    }
    double power = 1; //Drive power multiplier, changed with D-pad left/right

    int prevOuttakeTicks = 0;
    Time time = new Time(); //Timer helper: provides getTimer(), resetTime() and getDeltaTime()
    public Vector2 pos = new Vector2(); //In ticks (unused)

    public double direction; //In Degrees (unused)

    public Driver drive; //Hardware container: motors, servos and position tracker

    public double previousLeftDistance; //Unused
    public double previousRightDistance; //Unused


    //goBILDA Dual Mode servo in servo mode: 300 degrees over 500-2500us
    //Angle that corresponds to setPosition(1.0)
    static final double SERVO_RANGE_DEGREES = 360;
    //Target angles in degrees - tune these
    static final double nectarAngle = -300; //Backplate angle when A is pressed
    static final double pollenAngle = 300; //Backplate angle when B is pressed
    static final double INCLINE_STEP_DEGREES = 90; //Incline change per D-pad up/down press
    static final double pollen_RMP = 0.5843;
    static final double nectar_RMP = 0.7778;
    boolean prevDpadUp = false; //D-pad up state last loop, used to detect a new press
    boolean prevDpadDown = false; //D-pad down state last loop, used to detect a new press

    //Backplate angles are relative to the servo's center: 0 = middle, +/- = either side (clamped to +/- half the range)
    static double centeredAngleToPosition(double degrees) {
        return Math.max(0, Math.min(1, 0.5 + degrees / SERVO_RANGE_DEGREES));
    }

    //Converts an angle in degrees to a servo position (0.0 - 1.0), clamped to the valid range
    static double angleToPosition(double degrees) {
        return Math.max(0, Math.min(1, degrees / SERVO_RANGE_DEGREES));
    }

    public double backplateServoPosition; //Current backplate target angle, in degrees
    public double inclineServoPosition; //Current incline target angle, in degrees
    public double deltaLeftDistance; //Unused
    public double deltaRightDistance; //Unused

    public void runOpMode() throws InterruptedException {

        //Driver holds the motors, servos and position tracker
        drive = new Driver(hardwareMap, telemetry);

        //Start both servos at 0 degrees
        backplateServoPosition = 0;
        inclineServoPosition = 0;
        waitForStart(); //Wait for Play to be pressed
        double timeTest = 0; //Unused
        double rate = 0.0065; //Unused
        while(opModeIsActive())
        {

            time.update();
            telemetry.addData("Timer: ",time.getTimer());
            //Every 0.1s, D-pad left/right lowers/raises the drive power by 0.05
            /*if(time.getTimer() >= 0.1)
            {
                time.resetTime();
                if(gamepad1.dpad_left) power -= 0.05;
                if(gamepad1.dpad_right) power += 0.05;
            }*/

            //region scoring
            //Intake (right trigger, max 60%) and outtake (left trigger, max 100%); ignores presses under 0.1
            drive.intake.setPower((gamepad1.left_bumper ? 1 : 0) - (gamepad1.right_bumper ? -1 : 0));

            //Backplate servo
            //A / B set the target angle (degrees); it is converted to 0-1 and sent to the servo
            if(gamepad1.y) backplateServoPosition = pollenAngle;
            if(gamepad1.a) backplateServoPosition = nectarAngle;

            //Incline servo
            //D-pad up/down: each new press (not hold, via prev flags) adds/subtracts INCLINE_STEP_DEGREES,
            //then the angle is clamped to 0 - SERVO_RANGE_DEGREES
            if(gamepad1.dpad_up && !prevDpadUp) inclineServoPosition += INCLINE_STEP_DEGREES;
            if(gamepad1.dpad_down && !prevDpadDown) inclineServoPosition -= INCLINE_STEP_DEGREES;
            inclineServoPosition = Math.max(0, Math.min(SERVO_RANGE_DEGREES, inclineServoPosition));
            prevDpadUp = gamepad1.dpad_up;
            prevDpadDown = gamepad1.dpad_down;

            drive.outtakeBackplate.setPosition(centeredAngleToPosition(backplateServoPosition));
            drive.outtakeIncline.setPosition(angleToPosition(inclineServoPosition));


                //scoring.intake.setPower(gamepad1.left_bumper || gamepad2.dpad_up ? 1.0: 0);
            //REVERSE
            //scoring.outtake.setPower(gamepad1.dpad_down ? 1.0 : 0);
            /*if(gamepad1.left_bumper)
                drive.intake.setPower(-1.0);
            else if(gamepad1.right_bumper)
                drive.intake.setPower(0.6);
            else
                drive.intake.setPower(0);*/

            //endregion
            //region driving
                //Mecanum drive: left stick = forward/back + strafe, right stick = rotation.
                //Wheel powers are scaled down if any would exceed 1, then multiplied by power and eased
                double x_speed = -gamepad1.left_stick_x; //Strafe input
                double y_speed = gamepad1.left_stick_y; //Forward/back input
                double rot = -gamepad1.right_stick_x; //Rotation input
                double motorPower = 1.0; //Fixed max power scale
                double denominator = Math.max(Math.abs(y_speed) + Math.abs(x_speed) + Math.abs(rot), 1); //Scales powers so none exceed 1
                //Per-wheel power before the power multiplier
                double frontLeftPower = (y_speed + x_speed + rot) / denominator * motorPower;
                double backLeftPower = (y_speed - x_speed + rot) / denominator * motorPower;
                double frontRightPower = (y_speed - x_speed - rot) / denominator * motorPower;
                double backRightPower = (y_speed + x_speed - rot) / denominator * motorPower;

                drive.easePower(drive.frontLeftMotor, frontLeftPower * power, time.getDeltaTime());
                drive.easePower(drive.frontRightMotor, frontRightPower * power, time.getDeltaTime());
                drive.easePower(drive.backLeftMotor, backLeftPower * power, time.getDeltaTime());
                drive.easePower(drive.backRightMotor, backRightPower * power, time.getDeltaTime());
                /*drive.frontLeftMotor.setPower(frontLeftPower * power);
                drive.frontRightMotor.setPower(frontRightPower * power);
                drive.backLeftMotor.setPower(backLeftPower * power);
                drive.backRightMotor.setPower(backR67reightPower * power);*/

                //Update heading and x/y position, then show them on telemetry
                drive.positionTracker.updateDirection();
                drive.positionTracker.updatePosition();
                telemetry.addData("Angle Degs", drive.positionTracker.getDirection());
                String position = //Text shown on telemetry: robot x/y
                "Pos: "+"("+drive.positionTracker.getX()+" , "+drive.positionTracker.getY()+")";
                telemetry.addLine(position);

                int outtakeTicks = drive.outtake.getCurrentPosition();
                double outtakeVelocity = (outtakeTicks - prevOuttakeTicks) / time.getDeltaTime();
                prevOuttakeTicks = outtakeTicks;

            //endregion
            //Driver Station telemetry
            telemetry.addData("Wheel Motor Power",power);
            telemetry.addData("Outtake", "Left Bumper");
            telemetry.addData("Outtake Power: ", gamepad1.left_trigger);
            telemetry.addData("Outtake ticks: ", outtakeTicks);
            telemetry.addData("Prev outtake ticks: ", prevOuttakeTicks);
            telemetry.addData("deltaTime ", time.getDeltaTime());
            telemetry.addData("Outtake velocity", String.format(Locale.getDefault(),"%.4f", drive.outtake.getPower()));            telemetry.addData("Incline",drive.outtakeIncline.getPosition());
            telemetry.addData("Backplate",drive.outtakeBackplate.getPosition());
            telemetry.update();
        }
    }
}