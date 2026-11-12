package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.mechanisms.TestServo;

@TeleOp
public class ServoExamples extends OpMode {
    TestServo servo = new TestServo();

    @Override
    public void init(){
        servo.init(hardwareMap);

    }

    @Override
    public void loop(){
        if (gamepad1.dpad_down){
            servo.setServoPos(180);
        }

        if (gamepad1.dpad_up){
            servo.setServoPos(-180);
        }

        /*if(gamepad1.dpad_down){
            servo.setServoRot(-180);
        }

        if (gamepad1.dpad_up){
            servo.setServoRot(180);
        }*/
    }

}
