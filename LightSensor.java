package src;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.robotics.Color;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.Button;

public class LightSensor {
    public static void main(String[] args) {
        // Setup Motors and Sensor
        EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);
        EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S4);

        // Color ID Mode
        SampleProvider light = colorSensor.getColorIDMode();
        float[] sample = new float[light.sampleSize()];

        while (!Button.ESCAPE.isDown()) {

            light.fetchSample(sample, 0);
            int color = (int) sample[0];

            if (color == 7) {
                leftMotor.forward();
                rightMotor.forward();
                leftMotor.setSpeed(120);
                rightMotor.setSpeed(120);
            } else if (color != 7) {
                leftMotor.rotate(-90);
                rightMotor.rotate(90);

                while (rightMotor.isMoving()) {
                    light.fetchSample(sample, 0);
                    color = (int) sample[0];

                    if (color == 7) {
                        leftMotor.forward();
                        rightMotor.forward();
                        leftMotor.setSpeed(120);
                        rightMotor.setSpeed(120);
                    }
                }

            }
        }
        leftMotor.close();
        rightMotor.close();
        colorSensor.close();
    }
}
