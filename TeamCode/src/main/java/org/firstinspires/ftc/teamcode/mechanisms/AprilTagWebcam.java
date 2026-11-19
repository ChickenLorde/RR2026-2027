package org.firstinspires.ftc.teamcode.mechanisms;

import android.graphics.Canvas;
import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AprilTagWebcam {
    private AprilTagProcessor aprilTagProcessor;
    private VisionPortal visionPortal;

    private List<AprilTagDetection> detectedTags = new ArrayList<>();

    private Telemetry telemetry;

    private double lastBearing = 0;
    private boolean exposureSet = false;

    public void init(HardwareMap hwMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        aprilTagProcessor = new AprilTagProcessor.Builder()
                .setDrawTagID(true)
                .setDrawTagOutline(true)
                .setDrawAxes(true)
                .setDrawCubeProjection(true)
                .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                .build();
        aprilTagProcessor.setDecimation(2);
        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hwMap.get(WebcamName.class,"Webcam 1"));
        builder.setCameraResolution(new Size(640, 480));
        builder.addProcessor(aprilTagProcessor);
        builder.enableLiveView(false);

        visionPortal = builder.build();
        visionPortal.resumeStreaming();


    }

    private void applyManualExposure() {
        if (exposureSet || visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            return;
        }
        ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);
        GainControl gainControl = visionPortal.getCameraControl(GainControl.class);

        if (exposureControl != null && gainControl != null) {
            exposureControl.setMode(ExposureControl.Mode.Manual);
            exposureControl.setExposure(5, TimeUnit.MILLISECONDS);
            gainControl.setGain(220);
            exposureSet = true;
        }
    }

    public void update() {
        applyManualExposure();
        detectedTags = aprilTagProcessor.getDetections();

    }
    public List<AprilTagDetection> getDetectedTags(){
        return detectedTags;
    }

    public void displayDetectionTelemetry(AprilTagDetection detected){
        if(detected == null) {return;}

        AprilTagClusterDetection cluster = null;
        AprilTagSingleDetection single = null;
        if(detected instanceof AprilTagClusterDetection)
        {
            cluster = (AprilTagClusterDetection) detected;
            if(cluster.metadata != null && cluster.metadata.name.charAt(0) != 'R') return;
            lastBearing = cluster.ftcPose.bearing;
        }
        if(detected instanceof AprilTagSingleDetection)
        {
            single = (AprilTagSingleDetection) detected;
            if(single.metadata != null && single.metadata.name.charAt(0) != 'R') return;
            lastBearing = single.ftcPose.bearing;
        }
        if(cluster == null && single == null) return;



        if (cluster != null && cluster.metadata != null) {
            telemetry.addLine(String.format("\n==== %s", cluster.metadata.name));
            telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", cluster.ftcPose.x, cluster.ftcPose.y, cluster.ftcPose.z));
            telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", cluster.ftcPose.pitch, cluster.ftcPose.roll, cluster.ftcPose.yaw));
            telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", cluster.ftcPose.range, cluster.ftcPose.bearing, cluster.ftcPose.elevation));
        } else {
            telemetry.addLine(String.format("\n==== (ID %d) Unknown", single.id));
            telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", single.center.x, single.center.y));
        }
    }

    public void displayAllDetectionTelemetry() {
        for(AprilTagDetection detected : detectedTags)
            displayDetectionTelemetry(detected);
    }
    public void stop() {
        if(visionPortal != null)
            visionPortal.close();
    }

    public double getBearing() {
        return lastBearing;
    }

    public double getFps() {
        return visionPortal.getFps();
    }
}