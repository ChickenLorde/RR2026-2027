package org.firstinspires.ftc.teamcode.tutorials;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

@TeleOp
public class FlywheelTunerTutorial extends OpMode {

    public DcMotorEx outtake;

    double highVelocity = 1600;
    double lowVelocity = 900;
    double currentTargetVelocity = highVelocity;

    double F = 0;
    double P = 0;

    double[] stepSizes = {10.0, 1.0, 0.1, 0.001, 0.0001};
    int stepIndex = 1;

    public void init(){
        outtake = hardwareMap.get(DcMotorEx.class, "outtake");
        outtake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        outtake.setDirection(DcMotorSimple.Direction.REVERSE);

        PIDFCoefficients pidfCoefficients = new PIDFCoefficients(P, 0, 0, F);
        outtake.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);
        telemetry.addLine("Init complete");
    }
    public void loop(){
        //getting current gamepad commands
        //set target velocity
        //update telemetry

        if (gamepad1.yWasPressed()){
            if (currentTargetVelocity == highVelocity){
                currentTargetVelocity = lowVelocity;
            } else {
                currentTargetVelocity = highVelocity;
            }
        }

        if(gamepad1.bWasPressed()){
            stepIndex = (stepIndex + 1) % stepSizes.length;
        }

        if (gamepad1.dpadLeftWasPressed()){
            F += stepSizes[stepIndex];
        }
        if(gamepad1.dpadRightWasPressed()){
            F -= stepSizes[stepIndex];
        }

        if (gamepad1.dpadUpWasPressed()){
            P += stepSizes[stepIndex];
        }
        if(gamepad1.dpadDownWasPressed()){
            P -= stepSizes[stepIndex];
        }
        //Set new PIDF coefficients
        PIDFCoefficients pidfCoefficients = new PIDFCoefficients(P, 0, 0, F);
        outtake.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);

        outtake.setVelocity(currentTargetVelocity);

        double currentVelocity = outtake.getVelocity();
        double error = currentTargetVelocity - currentVelocity;

        telemetry.addData("Target Velocity", currentTargetVelocity);
        telemetry.addData("Current Velocity", "%.2f", currentVelocity);
        telemetry.addData("Error", "%.2f", error);
        telemetry.addLine("------------------------------------");
        telemetry.addData("Tuning P", "%.4f (D-Pad U/D)", P);
        telemetry.addData("Tuning F", "%.4f (D-Pad L/R)", F);
        telemetry.addData("Step Size", "%.4f (B Button)", stepSizes[stepIndex]);


    }
}
