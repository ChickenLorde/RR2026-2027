package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Utility.Time;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.parts.Driver;
import org.firstinspires.ftc.teamcode.parts.Scoring;
import org.firstinspires.ftc.teamcode.subsystems.Robot;

@TeleOp(name = "Wheels Test",group = "Testing Modes")
public class WheelsTest extends LinearOpMode {
    public void waitSeconds(double seconds) {
        long startTime = System.currentTimeMillis();
        while(true)
            if (System.currentTimeMillis() - startTime >= seconds * 1000)
                return;
    }
    double power = 1;

    Time time = new Time();
    public void runOpMode() throws InterruptedException {
        Robot bot = new Robot(hardwareMap, telemetry);
        Driver drive = new Driver(hardwareMap, telemetry);
        Scoring scoring = new Scoring(hardwareMap);

        waitForStart();
        double timeTest = 0;
        double rate = 0.0065;
        while(opModeIsActive())
        {
            time.update();
            telemetry.addData("Timer: ",time.getTimer());
            if(time.getTimer() >= 0.1)
            {
                time.resetTime();
                if(gamepad1.dpad_left) power -= 0.05;
                if(gamepad1.dpad_right) power += 0.05;
            }

            //region scoring

           // scoring.outtake.setPower(gamepad1.right_bumper || gamepad2.dpad_up ? -1.0 : 0);
            scoring.intake.setPower(gamepad1.left_bumper || gamepad2.dpad_up ? 1.0: 0);
            //REVERSE
           // scoring.outtake.setPower(gamepad1.dpad_down ? 1.0 : 0);
            scoring.intake.setPower(gamepad1.dpad_down ? -1.0: 0);

            //endregion
            //region driving
            double x_speed = -gamepad1.left_stick_x;
            double y_speed = gamepad1.left_stick_y;
            double rot = -gamepad1.right_stick_x;

            double denominator = Math.max(Math.abs(y_speed) + Math.abs(x_speed) + Math.abs(rot), 1);
            double frontLeftPower = (y_speed + x_speed - rot) / denominator;
            double backLeftPower = (y_speed - x_speed - rot) / denominator;
            double frontRightPower = (y_speed - x_speed + rot) / denominator;
            double backRightPower = (y_speed + x_speed + rot) / denominator;

            drive.frontLeftMotor.setPower(frontLeftPower * power);
            drive.frontRightMotor.setPower(frontRightPower * power);
            drive.backLeftMotor.setPower(backLeftPower * power);
            drive.backRightMotor.setPower(backRightPower * power);
            //endregion
            telemetry.addData("Wheel Motor Power",power);

            telemetry.addData("Distance",drive.getDistance());
            telemetry.update();
        }
    }
}