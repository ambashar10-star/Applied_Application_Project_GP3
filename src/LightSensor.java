package src;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.robotics.Color;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.Button;

/**
 * Shared data class used for communication between threads.
 * Stores the latest light value and running state.
 */
class ShareData {
    public static int light = 0;        // latest light sensor value
    public static boolean running = true; // controls thread execution
}

/**
 * Runnable class for reading the light sensor continuously.
 * This runs in a separate thread so sensor reading does not block the main program.
 */
class LightRunnable implements Runnable {
    private SampleProvider sp;
    private float[] sample;

    /**
     * Constructor for LightRunnable.
     * @param sp SampleProvider for the light sensor
     */
    public LightRunnable(SampleProvider sp) {
        this.sp = sp;
        this.sample = new float[sp.sampleSize()];
    }

    /**
     * Continuously reads light sensor values and updates shared data.
     */
    @Override
    public void run() {
        while (ShareData.running) {

            sp.fetchSample(sample, 0);

            // Simple synchronization (lock) when writing shared data
            synchronized (ShareData.class) {
                ShareData.light = (int)(sample[0] * 100);
            }

            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
    }
}


public class LightSensor {
    public static void main(String[] args) {
        // Setup Motors and Sensor
        EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);
        EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S4);
        SampleProvider light = colorSensor.getRedMode();

        // Start Lightsensor thread
        Thread lightThread = new Thread(new LightRunnable(light));
        lightThread.start();

        //PID CONSTANTS
        double Kp = 4.0;   // Proportional gain
        double Ki = 0.0;   // Integral gain
        double Kd = 10.0;  // Derivative gain
        
        double targetValue = 10; // desired light value (edge of line)
        double baseSpeed = 150;  // normal speed
        
        double lastError = 0;
        double integral = 0;

        LCD.drawString("PID Following...", 0, 0);

        // Main control loop
        while (!Button.ESCAPE.isDown()) {

            int currentLight;

            // Simple synchronization (lock) when reading shared data
            synchronized (ShareData.class) {
                currentLight = ShareData.light;
            }

            // 1. Calculate error
            double error = targetValue - currentLight;
            
            // 2. Integral
            integral += error;
            
            // 3. Derivative
            double derivative = error - lastError;
            
            // 4. PID output
            double turn = (Kp * error) + (Ki * integral) + (Kd * derivative);
            
            // 5. Adjust motor speeds
            leftMotor.setSpeed((int)(baseSpeed + turn));
            rightMotor.setSpeed((int)(baseSpeed - turn));
            
            leftMotor.forward();
            rightMotor.forward();

            lastError = error;

            // Display value
            LCD.clear(1);
            LCD.drawString("Light: " + currentLight, 0, 1);

            try { Thread.sleep(10); } catch (Exception e) {}
        }

        // Stop thread and motors
        ShareData.running = false;

        leftMotor.stop(true);
        rightMotor.stop();

        // Close resources
        colorSensor.close();
        leftMotor.close();
        rightMotor.close();
    }
}
