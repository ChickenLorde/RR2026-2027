package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ColorCollaborationTest {
    NormalizedColorSensor colorSensor;

    public enum DetectedColor {
        RED,
        BLUE,
        YELLOW,
        UNKNOWN;
    }

    public void init(HardwareMap hwMap) {
        colorSensor = hwMap.get(NormalizedColorSensor.class, "color sensor");
    }

    public void getDetectedColor(Telemetry telemetry) {
        NormalizedRGBA colors = colorSensor.getNormalizedColors(); //return 4 values

        float normRed, normGreen, normBlue;
        normRed = colors.red/ colors.alpha;
        normGreen = colors.green/ colors.alpha;
        normBlue = colors.blue/ colors.alpha;

        telemetry.addData("red", normRed);
        telemetry.addData("green", normGreen);
        telemetry.addData("blue", normBlue);
        telemetry.update();

        /*
            RED =
            YELLOW =
            BLUE =
         */
    }

}
