package org.firstinspires.ftc.teamcode.parts;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Scoring {
    public DcMotor intake;
    //public DcMotor outtake;

    public enum EaseMode{
        SINE, TAN
    }
    public EaseMode easeMode;
    public Scoring(HardwareMap hardwareMap)
    {
        intake = hardwareMap.dcMotor.get("intake");
        //outtake = hardwareMap.dcMotor.get("outtake");

        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //outtake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public double EasePower(double time, EaseMode easeMode)
    {
        time = Math.min(1,time);
        time = Math.max(0,time);
        if(easeMode == EaseMode.SINE)
            return (Math.sin((time - 0.5)*Math.PI)+1)/2.0;
        else if(easeMode == EaseMode.TAN)
            return Math.pow(Math.tan(Math.PI*time/4.0),3);
        return 0;
    }
}
