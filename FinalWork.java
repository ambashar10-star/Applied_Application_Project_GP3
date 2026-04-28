package src;

import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/**
 * Shared data class for communication between threads.
 * Stores light intensity and distance values.
 */
class ShareData {
    public static int light = 0;
    public static float distance = 0;             
    public static boolean running = true; 
}

// Thread for ultrasonic sensor (distance)
class UltrasonicThread extends Thread {
    private SampleProvider sp;
    private float[] sample;

    public UltrasonicThread(SampleProvider sp) {
        this.sp = sp;
        sample = new float[sp.sampleSize()];
    }

    public void run() {
        // keep updating distance while program is running
        while (ShareData.running) {
            sp.fetchSample(sample, 0);           
            ShareData.dist = sample[0];          
            Delay.msDelay(50);                  
        }
    }
}

// Thread for light sensor
class LightThread extends Thread {
    private SampleProvider sp;
    private float[] sample;

    public LightThread(SampleProvider sp) {
        this.sp = sp;
        sample = new float[sp.sampleSize()];
    }

    public void run() {
        // keep updating light value
        while (ShareData.running) {
            sp.fetchSample(sample, 0);          
            ShareData.light = (int)(sample[0] * 100); 
            Delay.msDelay(20);                  
        }
    }
}

public class mainWork  {

    public static void main(String[] args) {

        // create motors
        EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);

        // setup ultrasonic sensor
        EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S2);
        SampleProvider distance = ultrasonicSensor.getDistanceMode();

        // setup color sensor
        EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S4);
        SampleProvider light = colorSensor.getRedMode();

        // start sensor threads (they run in background)
        UltrasonicThread uThread = new UltrasonicThread(distance);
        LightThread lThread = new LightThread(light);

        uThread.start();
        lThread.start();

        // main loop (runs until ESCAPE button is pressed)
        while (!Button.ESCAPE.isDown()) {

            // read latest values from threads
            float dist = ShareData.dist;
            int intensity = ShareData.light;

            // display values on screen
            LCD.clear();
            LCD.drawString("Dist: " + dist, 0, 0);
            LCD.drawString("Light: " + intensity, 0, 1);

          
            if(dist < 0.25){

                LCD.clear();
                LCD.drawString("Dist: " + dist + " meters", 0, 0);

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

                // // Move forward
                // leftMotor.setSpeed(260);
                // rightMotor.setSpeed(260);
                // leftMotor.forward();
                // rightMotor.forward();
                // Delay.msDelay(3000);
                // break;
            }
            

            
            // LINE FOLLOWING
           else if(intensity < 10) // If it is on the black line
            {
                leftMotor.setSpeed(100);
                rightMotor.setSpeed(120);
                leftMotor.forward();
                rightMotor.forward();
            }
            else if(intensity == 10) // if it follows the edgr of the line
            {
                leftMotor.setSpeed(100);
                rightMotor.setSpeed(100);
                leftMotor.forward();
                rightMotor.forward();
            }
            else if(intensity > 10) // if it is off the line
            {
                leftMotor.setSpeed(120);
                rightMotor.setSpeed(100);
                leftMotor.forward();
                rightMotor.forward();
            }
            Delay.msDelay(50); 
        }
    

        // stop threads when program ends
        ShareData.running = false;

        // close hardware
        leftMotor.close();
        rightMotor.close();
        colorSensor.close();
        ultrasonicSensor.close();
    }
}
