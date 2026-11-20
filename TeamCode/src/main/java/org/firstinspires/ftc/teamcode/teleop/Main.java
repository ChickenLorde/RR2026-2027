package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Utility.Time;

import org.firstinspires.ftc.teamcode.Utility.Vector2;
import org.firstinspires.ftc.teamcode.mechanisms.AprilTagWebcam;
import org.firstinspires.ftc.teamcode.parts.Driver;
import org.firstinspires.ftc.teamcode.subsystems.PID_Test;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

@TeleOp(name = "Main TeleOp",group = "Competition Modes")
public class Main extends LinearOpMode {
    //Busy-wait for the requested duration; currently unused.
    public void waitSeconds(double seconds) {
        long startTime = System.currentTimeMillis();
        while(true)
            if (System.currentTimeMillis() - startTime >= seconds * 1000)
                return;
    }
    double power = 1; //Drive power multiplier; D-pad adjustment below is disabled

    int prevOuttakeTicks = 0; //Previous outtake encoder reading for the velocity calculation
    Time time = new Time(); //Timer helper: provides getTimer(), resetTime() and getDeltaTime()
    public Vector2 pos = new Vector2(); //In ticks (unused)

    public double direction; //In Degrees (unused)

    public Driver drive; //Hardware container: motors, servos and position tracker

    public double previousLeftDistance; //Unused
    public double previousRightDistance; //Unused


    //Assumed servo travel in degrees corresponding to a position change from 0.0 to 1.0
    static final double SERVO_RANGE_DEGREES = 360;
    //Target angles in degrees - tune these
    static final double nectarAngle = 300; //Backplate target when B is pressed (clamped to the servo range)
    static final double pollenAngle = -300; //Backplate target when Y is pressed (clamped to the servo range)
    static final double INCLINE_STEP_DEGREES = 90; //Incline change per D-pad up/down press
    static final double pollen_RMP = 0.5843; //Unused
    static final double nectar_RMP = 0.7778; //Unused
    static final double FLOWER_DOWN_DEGREES = 90; //Flower target when lowered
    static final double FLOWER_UP_DEGREES = 360; //Flower target when raised
    static final double OUTTAKE_RUN_SECONDS = 5.0; //Duration of each timed outtake run
    boolean prevDpadUp = false; //D-pad up state last loop, used to detect a new press
    boolean prevDpadDown = false; //D-pad down state last loop, used to detect a new press
    boolean prevX = false; //Previous X state, used to detect a new press
    boolean prevY = false; //Previous Y state, used to detect a new press
    boolean prevB = false; //Previous B state, used to detect a new press
    boolean flowersDown = false; //Current flower target; starts raised

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

    public enum AimMode {
        MANUAL, HIVE
    }

    // A cycles through the available aiming modes.
    private AimMode[] aimModeValues = AimMode.values();

    public AimMode currentAimMode;

    // Proportional controller that turns the robot to bring the accepted tag's bearing to zero.
    PID_Test hiveAimPid = new PID_Test(0.085, 0., 0.0);

    public AprilTagWebcam webCam = new AprilTagWebcam();;

    public void runOpMode() throws InterruptedException {

        //Driver holds the motors, servos and position tracker; map the flower servos separately
        drive = new Driver(hardwareMap, telemetry);
        drive.flowerLeft = hardwareMap.get(Servo.class, "flowerLeft");
        drive.flowerRight = hardwareMap.get(Servo.class, "flowerRight");
        drive.flowerRight.setDirection(Servo.Direction.REVERSE); //Compensate for mirrored mounting

        //Start both servos at 0 degrees
        backplateServoPosition = 0;
        inclineServoPosition = 0;

        currentAimMode = AimMode.MANUAL;
        drive.servoGate.setPosition(300);

        GoBildaPinpointDriver pinpoint =
                hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

        pinpoint.setOffsets(-84.0, -168.0, DistanceUnit.MM);
        pinpoint.setEncoderResolution(
                GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.FORWARD);

        pinpoint.resetPosAndIMU(); // Keep the robot still during calibration.

        webCam.init(hardwareMap, telemetry);
        waitForStart(); //Wait for Play to be pressed
        double timeTest = 0; //Unused
        double rate = 0.0065; //Unused
        //Track timed outtake runs without blocking the drive loop.
        ElapsedTime outtakeTimer = new ElapsedTime();
        boolean outtakeRunning = false;
        boolean prevA = false;
        try {
            while (opModeIsActive()) {

                time.update();
                telemetry.addData("Timer: ", time.getTimer());
                //Every 0.1s, D-pad left/right lowers/raises the drive power by 0.05
            /*if(time.getTimer() >= 0.1)
            {
                time.resetTime();
                if(gamepad1.dpad_left) power -= 0.05;
                if(gamepad1.dpad_right) power += 0.05;
            }*/

                //region scoring
                //Each bumper contributes positive power to the intake.
                if (gamepad1.left_bumper) {
                    drive.intake.setPower(1);
                } else if (gamepad1.right_bumper) {
                    drive.intake.setPower(-1);
                } else {
                    drive.intake.setPower(0);
                }
                //A switches aiming modes; holding it does not repeatedly switch modes.
                boolean aPressed = gamepad1.a;
                if (aPressed && !prevA) {
                    currentAimMode = aimModeValues[(currentAimMode.ordinal() + 1) % aimModeValues.length];
                    drive.servoGate.setPosition(currentAimMode == AimMode.HIVE
                            ? 300 : -300);
                    if (currentAimMode == AimMode.HIVE) {
                        hiveAimPid.reset();
                    }
                }
                prevA = aPressed;
                //Backplate servo
                //Y / B set the target angle and start/restart the timed outtake run on a new press.
                if (gamepad1.y) {
                    backplateServoPosition = pollenAngle;
                }
                if (gamepad1.b) {
                    backplateServoPosition = nectarAngle;
                }
                boolean yPressed = gamepad1.y;
                boolean bPressed = gamepad1.b;
                if ((yPressed && !prevY) || (bPressed && !prevB)) {
                    outtakeTimer.reset();
                    outtakeRunning = true;
                }
                prevY = yPressed;
                prevB = bPressed;
                if (outtakeRunning && outtakeTimer.seconds() >= OUTTAKE_RUN_SECONDS) {
                    outtakeRunning = false;
                }
                drive.outtake.setPower(outtakeRunning ? 1.0 : 0.0);

                //Incline servo
                //D-pad up/down: each new press (not hold, via prev flags) adds/subtracts INCLINE_STEP_DEGREES,
                //then the angle is clamped to 0 - SERVO_RANGE_DEGREES
                if (gamepad1.dpad_up && !prevDpadUp) inclineServoPosition += INCLINE_STEP_DEGREES;
                if (gamepad1.dpad_down && !prevDpadDown)
                    inclineServoPosition -= INCLINE_STEP_DEGREES;
                inclineServoPosition = Math.max(0, Math.min(SERVO_RANGE_DEGREES, inclineServoPosition));
                prevDpadUp = gamepad1.dpad_up;
                prevDpadDown = gamepad1.dpad_down;

                drive.outtakeBackplate.setPosition(centeredAngleToPosition(backplateServoPosition));
                drive.outtakeIncline.setPosition(angleToPosition(inclineServoPosition));
                //Each new X press toggles both flowers between their up and down targets.
                if (gamepad1.x && !prevX) flowersDown = !flowersDown;
                prevX = gamepad1.x;
                double flowerPosition = angleToPosition(
                        flowersDown ? FLOWER_DOWN_DEGREES : FLOWER_UP_DEGREES);
                drive.flowerLeft.setPosition(flowerPosition);
                drive.flowerRight.setPosition(flowerPosition);


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

                // In HIVE mode, automatic tag alignment replaces manual rotation input.
                if (currentAimMode == AimMode.HIVE) {
                    rot = HiveAimMode();
                }

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

                pinpoint.update();
                Pose2D pose = pinpoint.getPosition();
                telemetry.addData("X (in)", pose.getX(DistanceUnit.INCH));
                telemetry.addData("Y (in)", pose.getY(DistanceUnit.INCH));
                telemetry.addData("Heading (deg)", pose.getHeading(AngleUnit.DEGREES));

                //Estimate outtake encoder speed from the change in ticks since the last loop.
                int outtakeTicks = drive.outtake.getCurrentPosition();
                double outtakeVelocity = (outtakeTicks - prevOuttakeTicks) / time.getDeltaTime();
                prevOuttakeTicks = outtakeTicks;

                //endregion
                //Driver Station telemetry
                telemetry.addData("Wheel Motor Power", power);
                telemetry.addData("Outtake", outtakeRunning ? "Running" : "Stopped");
                //telemetry.addData("Outtake Power: ", drive.outtake.getPower());
                //telemetry.addData("Outtake ticks: ", outtakeTicks);
                //telemetry.addData("Prev outtake ticks: ", prevOuttakeTicks);
                //telemetry.addData("deltaTime ", time.getDeltaTime());
                //telemetry.addData("Outtake velocity", String.format(Locale.getDefault(),"%.4f", drive.outtake.getPower()));            telemetry.addData("Incline",drive.outtakeIncline.getPosition());
                telemetry.addData("Backplate", drive.outtakeBackplate.getPosition());
                //This is the commanded angle relative to center, not measured servo feedback.
                telemetry.addData("Backplate commanded angle (degrees)",
                        (drive.outtakeBackplate.getPosition() - 0.5) * SERVO_RANGE_DEGREES);
                telemetry.addData("Flowers", flowersDown ? "Down" : "Up");
                telemetry.addData("Aim Mode", currentAimMode);
                telemetry.addData("Last Bearing", webCam.getBearing());
                telemetry.addData("Cam FPS", webCam.getFps());

                UpdateWebcam();
                telemetry.update();
            }
        } finally {
            //Stop the outtake if the OpMode ends or exits with an exception.
            drive.outtake.setPower(0);

        }

    }


    private void UpdateWebcam() {
        // Refresh detections before reporting them; HiveAimMode uses the latest accepted bearing.
        webCam.update();
        webCam.displayAllDetectionTelemetry();
    }

    private double HiveAimMode() {
        // Zero bearing means the tag is centered; ignore small errors to prevent jitter.
        hiveAimPid.setTarget(0);
        double rotate = hiveAimPid.calculate(webCam.getBearing());

        if(Math.abs(webCam.getBearing()) < 4) return 0;

        // Limit the auto-aim contribution so it does not command full rotation abruptly.
        return rotate * 0.45;
    }


}