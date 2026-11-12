package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.teamcode.Utility.Time;

public class PID_Test {
    private final double Kp, Ki, Kd; // PID gains
    private double target;
    private double integral;
    private double derivative;

    private double prevDifference;

    private final Time time;

    // Constructor to initialize gains
    public PID_Test(double Kp, double Ki, double Kd) {
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
        this.integral = 0;
        time = new Time();
        time.update();
        time.resetTime();
    }

    // Set the desired setpoint
    public void setTarget(double target) {
        this.target = target;
    }

    // Calculate the control output
    public double calculate(double currentValue) {
        time.update();

        double difference = target - currentValue;
        integral += difference * time.getDeltaTime();
        derivative = (difference - prevDifference) / time.getDeltaTime();
        prevDifference = difference;

        double raw = (Kp * difference) + (Ki * integral) + (Kd * derivative);

        double scaled = raw;
        return raw;
    }

}