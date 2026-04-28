package src;

import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.lcd.LCD;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;   
import lejos.utility.Delay;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Shared data class for communication between threads.
 * Stores distance value and running state.
 */
class ShareData {
    public static float distance = 0;   // latest distance reading
    public static boolean running = true; // controls thread execution
}

/**
 * Runnable class for ultrasonic sensor.
 * Continuously reads distance in a separate thread.
 */
class DistanceRunnable implements Runnable {
    private SampleProvider sp;
    private float[] sample;

    /**
     * Constructor for DistanceRunnable.
     * @param sp SampleProvider for ultrasonic sensor
     */
    public DistanceRunnable(SampleProvider sp) {
        this.sp = sp;
        this.sample = new float[sp.sampleSize()];
    }

    /**
     * Continuously updates distance value.
     */
    @Override
    public void run() {
        while (ShareData.running) {

            sp.fetchSample(sample, 0);

            // Simple synchronization (lock) when writing shared data
            synchronized (ShareData.class) {
                ShareData.distance = sample[0];
            }

            try {
                Thread.sleep(30); // small delay to prevent echo interference
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * Main class for ultrasonic sensor testing and obstacle avoidance.
 * Uses threading for sensor reading and performs overtaking logic.
 */
public class UltrasonicSensor {

    public static void main(String[] args) {

        // Create motor objects
        EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);


        // Creating an instance of US sensor at port 2
        EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S2);
        SampleProvider distanceProvider = ultrasonicSensor.getDistanceMode();

        // Start background thread
        Thread distThread = new Thread(new DistanceRunnable(distanceProvider));
        distThread.start();

        leftMotor.setSpeed(200);
        rightMotor.setSpeed(200);

        // Required for local operations if needed
        float[] sample = new float[distanceProvider.sampleSize()];

        while (!Button.ESCAPE.isDown()) {

            float currentDist;

            // Simple synchronization (lock) when reading shared data
            synchronized (ShareData.class) {
                currentDist = ShareData.distance;
            }

            leftMotor.forward();
            rightMotor.forward();

            //OBJECT DETECTED
            if (currentDist < 0.3 && currentDist > 0) {

                leftMotor.stop(true);
                rightMotor.stop();
                Sound.beep();

                //SCAN LEFT
                rightMotor.resetTachoCount();
                leftMotor.backward();
                rightMotor.forward();

                while (true) {
                    synchronized (ShareData.class) {
                        if (ShareData.distance > 0.6) break;
                    }
                }

                leftMotor.stop(true);
                rightMotor.stop();
                int leftAngle = rightMotor.getTachoCount();

                // Return to center
                leftMotor.rotate(leftAngle, true);
                rightMotor.rotate(-leftAngle);

                //SCAN RIGHT
                leftMotor.resetTachoCount();
                leftMotor.forward();
                rightMotor.backward();

                while (true) {
                    synchronized (ShareData.class) {
                        if (ShareData.distance > 0.6) break;
                    }
                }

                leftMotor.stop(true);
                rightMotor.stop();
                int rightAngle = leftMotor.getTachoCount();

                // Return to center
                leftMotor.rotate(-rightAngle, true);
                rightMotor.rotate(rightAngle);

                // DECISION
                if (leftAngle < rightAngle) {

                    LCD.drawString("Left is Clearer", 0, 2);

                    leftMotor.rotate(-leftAngle, true);
                    rightMotor.rotate(leftAngle);

                    leftMotor.forward();
                    rightMotor.forward();
                    Delay.msDelay(3070);

                    leftMotor.rotate(leftAngle, true);
                    rightMotor.rotate(-leftAngle);

                    boolean passed = false;

                    while (!passed) {

                        leftMotor.forward();
                        rightMotor.forward();
                        Delay.msDelay(1000);

                        leftMotor.stop(true);
                        rightMotor.stop();

                        // Peek right
                        leftMotor.rotate(180, true);
                        rightMotor.rotate(-180);

                        synchronized (ShareData.class) {
                            if (ShareData.distance < 0.5) {
                                leftMotor.rotate(-180, true);
                                rightMotor.rotate(180);
                            } else {
                                leftMotor.rotate(-180, true);
                                rightMotor.rotate(180);

                                leftMotor.forward();
                                rightMotor.forward();
                                Delay.msDelay(1000);

                                leftMotor.rotate(leftAngle, true);
                                rightMotor.rotate(-leftAngle);

                                leftMotor.forward();
                                rightMotor.forward();
                                Delay.msDelay(3070);

                                leftMotor.rotate(-leftAngle, true);
                                rightMotor.rotate(leftAngle);

                                passed = true;
                            }
                        }
                    }
                } 
                else {

                    LCD.drawString("Right is Clearer", 0, 2);

                    leftMotor.rotate(rightAngle, true);
                    rightMotor.rotate(-rightAngle);

                    leftMotor.forward();
                    rightMotor.forward();
                    Delay.msDelay(3070);

                    leftMotor.rotate(-rightAngle, true);
                    rightMotor.rotate(rightAngle);

                    boolean passed = false;

                    while (!passed) {

                        leftMotor.forward();
                        rightMotor.forward();
                        Delay.msDelay(1000);

                        leftMotor.stop(true);
                        rightMotor.stop();

                        // Peek left
                        leftMotor.rotate(-180, true);
                        rightMotor.rotate(180);

                        synchronized (ShareData.class) {
                            if (ShareData.distance < 0.5) {
                                leftMotor.rotate(180, true);
                                rightMotor.rotate(-180);
                            } else {
                                leftMotor.rotate(180, true);
                                rightMotor.rotate(-180);

                                leftMotor.forward();
                                rightMotor.forward();
                                Delay.msDelay(1000);

                                leftMotor.rotate(-rightAngle, true);
                                rightMotor.rotate(rightAngle);

                                leftMotor.forward();
                                rightMotor.forward();
                                Delay.msDelay(3070);

                                leftMotor.rotate(rightAngle, true);
                                rightMotor.rotate(-rightAngle);

                                passed = true;
                            }
                        }
                    }
                }
            }
        }

        //CLEANUP 
        ShareData.running = false;

        leftMotor.close();
        rightMotor.close();
        ultrasonicSensor.close();
    }
}

