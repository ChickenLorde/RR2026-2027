package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@TeleOp(name = "Color Sensing",group = "Competition Modes")
public class ColorCollaborationTest extends LinearOpMode {
    NormalizedColorSensor colorSensor;

    @Override
    public void runOpMode() throws InterruptedException {
        init(hardwareMap);
        waitForStart();
        while(opModeIsActive()) {
            getDetectedColor(telemetry);
        }
    }

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

        DetectedColor color = DetectedColor.UNKNOWN;

        //How many times larger the color needs to be than another to be considered a significant difference
        final double significantThreshold = 1.35;

        //If the red value is significantly larger than the other colors, then the color is red
        if(normRed/normGreen > significantThreshold && normRed/normBlue > significantThreshold) color = DetectedColor.RED;

        //If the blue value is significantly larger than the other colors, then it is blue
        if(normBlue/normRed > significantThreshold && normBlue/normGreen > significantThreshold) color = DetectedColor.BLUE;

        //If the values for red and green are close (yellow) and both are significantly larger than blue then it is yellow
        if(normRed/normGreen < significantThreshold && normRed/normBlue > significantThreshold && normGreen/normBlue > significantThreshold) color = DetectedColor.YELLOW;

        telemetry.addData("Detected Color", color.toString());

        telemetry.update();


    }

}
