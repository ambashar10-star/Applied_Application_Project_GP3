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
