package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ColorSensor {

    private boolean isInitialized = false;
    private Telemetry telemetry;

    NormalizedColorSensor colorSensor;

    DetectedColor lastDetectedColor = DetectedColor.UNKNOWN;
    public enum DetectedColor {
        RED,
        BLUE,
        YELLOW,
        UNKNOWN;
    }

    public void init(HardwareMap hwMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        colorSensor = hwMap.get(NormalizedColorSensor.class, "color sensor");
        isInitialized = true;
    }

    public DetectedColor getDetectedColor() {
        if(!isInitialized) return DetectedColor.UNKNOWN;
        NormalizedRGBA colors = colorSensor.getNormalizedColors(); //return 4 values

        float normRed, normGreen, normBlue;
        normRed = colors.red/ colors.alpha;
        normGreen = colors.green/ colors.alpha;
        normBlue = colors.blue/ colors.alpha;

        telemetry.addData("red", normRed);
        telemetry.addData("green", normGreen);
        telemetry.addData("blue", normBlue);

        //How many times larger the color needs to be than another to be considered a significant difference
        final double significantThreshold = 1.35;

        //If the red value is significantly larger than the other colors, then the color is red
        if(normRed/normGreen > significantThreshold && normRed/normBlue > significantThreshold) lastDetectedColor = DetectedColor.RED;

        //If the blue value is significantly larger than the other colors, then it is blue
        if(normBlue/normRed > significantThreshold && normBlue/normGreen > significantThreshold) lastDetectedColor = DetectedColor.BLUE;

        //If the values for red and green are close (yellow) and both are significantly larger than blue then it is yellow
        if(normRed/normGreen < significantThreshold && normRed/normBlue > significantThreshold && normGreen/normBlue > significantThreshold) lastDetectedColor = DetectedColor.YELLOW;

        return lastDetectedColor;
    }

    public void UpdateTelemetry() {
        telemetry.addData("Detected Intake Color", lastDetectedColor.toString());
    }
}
