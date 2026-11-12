package org.firstinspires.ftc.teamcode.Utility;

public class Time {
    private double previousTime = System.nanoTime();
    private double currentTime = 0;
    private double deltaTime = 0;
    private double timer;

    public void update()
    {
        currentTime = System.nanoTime();
        deltaTime = (currentTime - previousTime) / 1000000000.0;
        previousTime = currentTime;
        timer += deltaTime;
    }
    public double getTimer() {
        return timer;
    }

    public double getDeltaTime()
    {
        return deltaTime;
    }

    public void resetTime()
    {
        timer = 0;
    }
}
