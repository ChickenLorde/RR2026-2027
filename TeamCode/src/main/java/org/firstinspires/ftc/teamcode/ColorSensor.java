package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;

import org.firstinspires.ftc.teamcode.mechanisms.ColorCollaborationTest;

public class ColorSensor {

    ColorCollaborationTest test = new ColorCollaborationTest();
    public void init(){
        test.init(hardwareMap);
    }

    public void loop(){
        test.getDetectedColor(telemetry);
    }
}
