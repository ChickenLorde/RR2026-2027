package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class TestServo {
    private Servo servoPos;
    private Servo servoRot;

    public void init(HardwareMap hwMap){
        servoPos = hwMap.get(Servo.class,"compression_servo");
        //servoRot = hwMap.get(Servo.class, "compression_servo");

    }

    public void setServoPos(double angle){
        servoPos.setPosition((angle));
    }

    /*public void setServoRot(double angle){
        servoRot.setPosition((angle));

    }*/


}
