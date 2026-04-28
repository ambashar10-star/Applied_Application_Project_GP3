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
        
        // getRedMode
        SampleProvider light = colorSensor.getRedMode();
        float[] sample = new float[light.sampleSize()];
        int colorSample = (int)(sample[0] * 100);

        while (!Button.ESCAPE.isDown()) {

            light.fetchSample(sample, 0);
            colorSample = (int)(sample[0] * 100);

            if(colorSample < 9) // If it is on the black line
            {
                leftMotor.setSpeed(120);
                rightMotor.setSpeed(100);
                leftMotor.forward();
                rightMotor.forward();
            }
            else if(colorSample == 9) // if it follows the edgr of the line
            {
                leftMotor.setSpeed(100);
                rightMotor.setSpeed(100);
                leftMotor.forward();
                rightMotor.forward();
            }
            else if(colorSample > 9) // if it is off the line
            {
                leftMotor.setSpeed(100);
                rightMotor.setSpeed(120);
                leftMotor.forward();
                rightMotor.forward();
            }
            try 
            {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        leftMotor.close();
        rightMotor.close();
        colorSensor.close();
    }
}
