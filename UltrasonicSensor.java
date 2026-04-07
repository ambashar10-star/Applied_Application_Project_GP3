package src;

import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.lcd.LCD;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;   // allows the sensor to return the samples or data
import lejos.utility.Delay;
// e.g., for getting distance data from sonic sensor etc
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class UltrasonicSensor {

    public static void main(String[] args) {

        // Create motor objects
        EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);


        // Creating an instance of US sensor at port 2
        EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S2);
        
        // Get the distance sample provider
        SampleProvider distance = ultrasonicSensor.getDistanceMode();
        
        // Create a sample array to hold the distance value
        // even though sonic sensor gives distance as an o/p, but since other sensors, e.g., light sensor
        // can provide multiple values, therefore to keep consistency, I'm using sampleprovider
        float[] sample = new float[distance.sampleSize()];
    
        // Keep displaying the distance, until user presses a button
         while (true)
         {
            // Get the curRent distnce reading from the US sensor
            distance.fetchSample(sample, 0);

            // // Display the distance on the LCD screen
            LCD.clear();
            LCD.drawString("Dist: " + sample[0] + " meters", 0, 0);

            // Set speed
            leftMotor.setSpeed(260);
            rightMotor.setSpeed(260);

            leftMotor.forward();
            rightMotor.forward();
            // //Delay.msDelay(3000); 

            if(sample[0] < 0.4){

                LCD.clear();
                LCD.drawString("Dist: " + sample[0] + " meters", 0, 0);

                Sound.beep();

                // Slow down
                leftMotor.setSpeed(120);
                rightMotor.setSpeed(120);
                leftMotor.forward();
                rightMotor.forward();
                Delay.msDelay(300);

                // slightly turn to the right
                leftMotor.setSpeed(170);
                rightMotor.setSpeed(100);
                leftMotor.forward();
                rightMotor.forward();
                Delay.msDelay(3000);

                //  MOVE BESIDE OBJECT WHILE MAINTAINING LEFT
                leftMotor.setSpeed(100);
                rightMotor.setSpeed(150);
                leftMotor.forward();
                rightMotor.forward();
                Delay.msDelay(3000);

                // move forward again
                leftMotor.setSpeed(260);
                rightMotor.setSpeed(260);
                leftMotor.forward();
                rightMotor.forward();
                Delay.msDelay(700);

                // slightly turn to the left again
                leftMotor.setSpeed(100);
                rightMotor.setSpeed(170);
                leftMotor.forward();
                rightMotor.forward();
                Delay.msDelay(5000);

                // slightly turn to the left again (to move forward in a straight line)
                leftMotor.setSpeed(200);
                rightMotor.setSpeed(100);
                leftMotor.forward();
                rightMotor.forward();
                Delay.msDelay(3000);

                // Move forward
                leftMotor.setSpeed(260);
                rightMotor.setSpeed(260);
                leftMotor.forward();
                rightMotor.forward();
                Delay.msDelay(3000);
                break;
            }
            
            // Refresh display every 100 ms
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        
         }

        // leftMotor.stop(true);
        // rightMotor.stop(true);

    }
}

