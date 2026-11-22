package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Utility.Time;

import org.firstinspires.ftc.teamcode.mechanisms.AprilTagWebcam;
import org.firstinspires.ftc.teamcode.mechanisms.ColorSensor;
import org.firstinspires.ftc.teamcode.parts.Driver;
import org.firstinspires.ftc.teamcode.subsystems.PidControl;

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

    public Driver drive; //Hardware container: motors, servos and position tracker


    //Assumed servo travel in degrees corresponding to a position change from 0.0 to 1.0
    static final double SERVO_RANGE_DEGREES = 360;
    //Target angles in degrees - tune these
    static final double nectarAngle = 300; //Backplate target when B is pressed (clamped to the servo range)
    static final double pollenAngle = -300; //Backplate target when Y is pressed (clamped to the servo range)
    static final double INCLINE_STEP_DEGREES = 15; //Incline change per D-pad up/down press
    static final double FLOWER_DOWN_DEGREES = 90; //Flower target when lowered
    static final double FLOWER_UP_DEGREES = 360; //Flower target when raised
    static final double OUTTAKE_RUN_SECONDS = 5.0; //Duration of each timed outtake run
    boolean prevDpadUp = false; //D-pad up state last loop, used to detect a new press
    boolean prevDpadDown = false; //D-pad down state last loop, used to detect a new press
    boolean prevX = false; //Previous X state, used to detect a new press
    boolean flowersDown = false; //Current flower target; starts raised

    double outtakeTargetVelocity = 1700;
    boolean wasLeftDPadPressed = false;
    boolean wasRightDPadPressed = false;

    //55.9, power: 1700, angle: 135
    //45, power: 1590, angle: 0
    double outtakeVelocity = 0;


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

    private double lastBackplateIncrement;
    private final double backplateIncrementDeadzone = 12;

    //far angle 135
    //far power 1670

    public enum AimMode {
        MANUAL, HIVE
    }

    public enum IntakeMode {
        NECTAR, POLLEN
    }

    public enum Alliance {
        RED, BLUE
    }

    private AimMode[] aimModeValues = AimMode.values();

    private Alliance alliance = Alliance.RED;

    public AimMode aimMode;
    public IntakeMode intakeMode;

    PidControl hiveAimPid = new PidControl(0.06, 0, 0.0);

    public AprilTagWebcam webCam = new AprilTagWebcam();
    public ColorSensor colorSensor = new ColorSensor();

    private double colorSensorUpdateTimer;

    public void runOpMode() throws InterruptedException {

        //Driver holds the motors, servos and position tracker; map the flower servos separately
        drive = new Driver(hardwareMap, telemetry);
        drive.flowerLeft = hardwareMap.get(Servo.class, "flowerLeft");
        drive.flowerRight = hardwareMap.get(Servo.class, "flowerRight");
        drive.flowerRight.setDirection(Servo.Direction.REVERSE); //Compensate for mirrored mounting

        InitializeMechanisms();

        //Start both servos at 0 degrees
        backplateServoPosition = 0;
        inclineServoPosition = 135;


        aimMode = AimMode.MANUAL;

        boolean prevA = false;

        while(!isStarted() && !isStopRequested()) {
            boolean aPressed = gamepad1.a;
            if(aPressed && !prevA) {
                if(alliance == Alliance.BLUE) alliance = Alliance.RED;
                else alliance = Alliance.BLUE;
            }
            prevA = aPressed;
        }
        prevA = false;
        double timeTest = 0; //Unused
        double rate = 0.0065; //Unused
        //Track the timed outtake run and detect new A presses without blocking the drive loop.
        ElapsedTime outtakeTimer = new ElapsedTime();
        boolean outtakeRunning = false;

        try {
        while(opModeIsActive())
        {

            time.update();
            colorSensorUpdateTimer += time.getDeltaTime();
            //telemetry.addData("Timer: ",time.getTimer());
            //Every 0.1s, D-pad left/right lowers/raises the drive power by 0.05
            /*if(time.getTimer() >= 0.1)
            {
                time.resetTime();
                if(gamepad1.dpad_left) power -= 0.05;
                if(gamepad1.dpad_right) power += 0.05;
            }*/

            //region scoring
            //Each bumper contributes positive power to the intake.
            drive.intake.setPower(((gamepad1.left_bumper && outtakeVelocity > 1200 || gamepad1.left_trigger > 0 ? 1.0 : 0) - (gamepad1.right_bumper ? -1 : 0)));
            drive.outtake.setVelocity(gamepad1.left_bumper || gamepad1.right_trigger > 0 ? outtakeTargetVelocity : 0);

            boolean dpadL = gamepad1.dpad_left;
            boolean dpadR = gamepad1.dpad_right;

            if(dpadL && !wasLeftDPadPressed) {
                wasLeftDPadPressed = true;
                outtakeTargetVelocity = Math.min(3500, outtakeTargetVelocity + 50);
            }
            if(dpadR && !wasRightDPadPressed) {
                wasRightDPadPressed = true;
                outtakeTargetVelocity = Math.max(0, outtakeTargetVelocity - 20);
            }

            if(!dpadL) wasLeftDPadPressed = false;
            if(!dpadR) wasRightDPadPressed = false;

            //A starts or restarts the timed outtake run; holding A does not restart the timer.
            boolean aPressed = gamepad1.a;
            if (aPressed && !prevA) {
                //outtakeTimer.reset();
                //outtakeRunning = true;

                aimMode = aimModeValues[(aimMode.ordinal() + 1) % aimModeValues.length];
                drive.servoGate.setPosition(aimMode == AimMode.HIVE ? 300 : -300);
                if(aimMode == AimMode.HIVE)
                    hiveAimPid.reset();
            }
            prevA = aPressed;
            //if (outtakeRunning && outtakeTimer.seconds() >= OUTTAKE_RUN_SECONDS) {
                //outtakeRunning = false;
            //}
            //drive.outtake.setPower(outtakeRunning ? 1.0 : 0.0);
            //Backplate servo
            //Y / B set the target angle (degrees); it is converted to 0-1 and sent to the servo
            if(gamepad1.y) intakeMode = IntakeMode.POLLEN;
            if(gamepad1.b) intakeMode = IntakeMode.NECTAR;

            switch(intakeMode) {
                case POLLEN: backplateServoPosition = pollenAngle; break;
                case NECTAR: backplateServoPosition = nectarAngle; break;
            }

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
                double x_speed = gamepad1.left_stick_x; //Strafe input
                double y_speed = -gamepad1.left_stick_y; //Forward/back input
                double rot = -gamepad1.right_stick_x; //Rotation input

                if(aimMode == AimMode.HIVE)
                    rot = HiveAimMode();

                double curAngle = drive.GetYawRadians();
                //drive.positionTracker.getDirection();
                double[] fieldXY = robotToFieldCentric (x_speed, y_speed, curAngle);

                double field_xspeed = fieldXY[0];
                double field_yspeed = fieldXY[1];

                // Change the variables here so we don't have to change them below
                x_speed = field_xspeed;
                y_speed = field_yspeed;

                double motorPower = 1.0; //Fixed max power scale
                double denominator = Math.max(Math.abs(y_speed) + Math.abs(x_speed) + Math.abs(rot), 1); //Scales powers so none exceed 1
                //Per-wheel power before the power multiplier
                double frontLeftPower = (y_speed + x_speed + rot) / denominator * motorPower;
                double backLeftPower = (y_speed - x_speed + rot) / denominator * motorPower;
                double frontRightPower = (y_speed - x_speed - rot) / denominator * motorPower;
                double backRightPower = (y_speed + x_speed - rot) / denominator * motorPower;

                drive.frontLeftMotor.setPower(frontLeftPower * power);
                drive.frontRightMotor.setPower(frontRightPower * power);
                drive.backLeftMotor.setPower(backLeftPower * power);
                drive.backRightMotor.setPower(backRightPower * power);
                /*drive.frontLeftMotor.setPower(frontLeftPower * power);
                drive.frontRightMotor.setPower(frontRightPower * power);
                drive.backLeftMotor.setPower(backLeftPower * power);
                drive.backRightMotor.setPower(backR67reightPower * power);*/

                //Update heading and x/y position, then show them on telemetry
                drive.positionTracker.updateDirection();
                drive.positionTracker.updatePosition();
                //telemetry.addData("Angle Degs", drive.positionTracker.getDirection());
                //String position = //Text shown on telemetry: robot x/y
                //"Pos: "+"("+drive.positionTracker.getX()+" , "+drive.positionTracker.getY()+")";
                //telemetry.addLine(position);

                //Estimate outtake encoder speed from the change in ticks since the last loop.
                int outtakeTicks = drive.outtake.getCurrentPosition();
                outtakeVelocity = (outtakeTicks - prevOuttakeTicks) / time.getDeltaTime();
                prevOuttakeTicks = outtakeTicks;

            //endregion
            //Driver Station telemetry
            telemetry.addData("Wheel Motor Power",power);
            telemetry.addData("Outtake Power", outtakeTargetVelocity);
            //telemetry.addData("Outtake Power: ", drive.outtake.getPower());
            //telemetry.addData("Outtake ticks: ", outtakeTicks);
            //telemetry.addData("Prev outtake ticks: ", prevOuttakeTicks);
            //telemetry.addData("deltaTime ", time.getDeltaTime());
            //telemetry.addData("Outtake velocity", outtakeVelocity);
            //telemetry.addData("Incline",drive.outtakeIncline.getPosition());
            //telemetry.addData("Backplate",drive.outtakeBackplate.getPosition()); //
            //This is the commanded angle relative to center, not measured servo feedback.
            telemetry.addData("Backplate commanded angle (degrees)",
                    inclineServoPosition);
            telemetry.addData("Flowers", flowersDown ? "Down" : "Up");
            telemetry.addData("Aim Mode", aimMode);
            //telemetry.addData("Last Bearing", webCam.getBearing());
            //telemetry.addData("Cam FPS", webCam.getFps());
            if(drive.intake.getPower() > 0.1 || colorSensorUpdateTimer > 0.33) UpdateColorSensor();
            colorSensor.UpdateTelemetry();
            UpdateWebcam();
            telemetry.update();
        }
        } finally {
            //Stop the outtake if the OpMode ends or exits with an exception.
            drive.outtake.setPower(0);
        }
    }

    private void InitializeMechanisms() {
        webCam.init(hardwareMap, telemetry);
        colorSensor.init(hardwareMap, telemetry);
    }

    private void UpdateWebcam() {
        webCam.update();
        webCam.displayAllDetectionTelemetry(alliance);
    }
    private void UpdateColorSensor() {
        colorSensorUpdateTimer = 0;
        int color = colorSensor.getDetectedColor().ordinal();
        if(color == 0 || color == 1 || color == 3) intakeMode = IntakeMode.NECTAR;
        else intakeMode = IntakeMode.POLLEN;
    }

    static double[] robotToFieldCentric (double vxRobot, double vyRobot, double cur_angle_degrees)
    {

        double cos = Math.cos(cur_angle_degrees);
        double sin = Math.sin(cur_angle_degrees);

        double vxField = vxRobot * cos + vyRobot * sin;
        double vyField = -vxRobot * sin + vyRobot * cos;

        return new double[] { vxField, vyField };
    }

    private double HiveAimMode() {
        hiveAimPid.setTarget(0);
        double rotate = hiveAimPid.calculate(webCam.getBearing());

        double maxPower = (intakeMode == IntakeMode.NECTAR ? 1790 : 1590);
        double minPower = (intakeMode == IntakeMode.NECTAR ? 1740 : 1540);
        double powerSlope = (maxPower - minPower)/(55.9-46.0);
        double backplateSlope = (135)/(55.9-46.0);
        outtakeTargetVelocity = minPower + powerSlope * (webCam.getRange() - 46);

        double currentIncline = backplateSlope * (webCam.getRange() - 46);
        if(Math.abs(currentIncline - lastBackplateIncrement) > backplateIncrementDeadzone) {
            inclineServoPosition = (int)currentIncline;
            lastBackplateIncrement = (int)currentIncline;
        }

        if(Math.abs(webCam.getBearing()) < 0.6) return 0;

        return rotate * 0.45;
    }
}