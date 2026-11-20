package org.firstinspires.ftc.teamcode.subsystems;

import org.firstinspires.ftc.teamcode.Utility.Time;

public class PID_Test {
    private final double Kp, Ki, Kd; // PID gains
    private double target;
    private double integral;
    private double derivative;

    private double prevDifference;
    // Bound accumulated error so prolonged error cannot grow the integral term without limit.
    private final double MAX_INTEGRAL = 1.0;

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
        integral = Math.max(-MAX_INTEGRAL, Math.min(MAX_INTEGRAL, integral));
        derivative = (difference - prevDifference) / time.getDeltaTime();
        prevDifference = difference;

        double raw = (Kp * difference) + (Ki * integral) + (Kd * derivative);

        double scaled = raw;
        // Smoothly constrain the controller output to [-1, 1] for motor power use.
        return Math.tanh(raw);
    }

    public void reset() {
        // Clear accumulated/history terms when automatic aiming is enabled again.
        integral = 0;
        prevDifference = 0;
        derivative = 0;
    }

}