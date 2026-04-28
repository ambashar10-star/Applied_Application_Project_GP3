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

/**
 * Runnable class for light sensor.
 * Continuously reads light values in a separate thread.
 */
class LightRunnable implements Runnable {
    private SampleProvider sp;
    private float[] sample;

    public LightRunnable(SampleProvider sp) {
        this.sp = sp;
        sample = new float[sp.sampleSize()];
    }
    @Override
    public void run() {
        // keep updating light value
        while (ShareData.running) {
            
            sp.fetchSample(sample, 0);
            synchronized (ShareData.class) {
                ShareData.light = (int)(sample[0] * 100);
            }
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
    }
}

/**
 * Runnable class for ultrasonic sensor.
 * Continuously reads distance values in a separate thread.
 */
class DistanceRunnable implements Runnable {
    private SampleProvider sp;
    private float[] sample;

    public DistanceRunnable(SampleProvider sp) {
        this.sp = sp;
        sample = new float[sp.sampleSize()];
    }

    @Override
    public void run() {
        // keep updating distance while program is running
        while (ShareData.running) {
            
            sp.fetchSample(sample, 0);

            synchronized (ShareData.class) {
                ShareData.distance = sample[0];
            }
            try { Thread.sleep(30); } catch (InterruptedException e) {}                
        }
    }
}

/**
 * Main robot program.
 * Combines:
 * - PID line following
 * - Object detection
 * - Obstacle bypass and return to line
 */
public class FinalWork  {

    public static void main(String[] args) {

        // create motors
        EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);

        // setup color sensor
        EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S4);
        SampleProvider light = colorSensor.getRedMode();
        
        // setup ultrasonic sensor
        EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S2);
        SampleProvider distance = ultrasonicSensor.getDistanceMode();

        new Thread(new LightRunnable(lightProvider)).start();
        new Thread(new DistanceRunnable(distProvider)).start();

        // PID SETTINGS 
        double Kp = 4.0; 
        double Ki = 0.0; 
        double Kd = 10.0; 
        double targetValue = 10; 
        double baseSpeed = 150; 
        double lastError = 0; 
        double integral = 0;

        // main loop (runs until ESCAPE button is pressed)
        while (!Button.ESCAPE.isDown()) {

            float currentDist;

            synchronized (ShareData.class) {
                currentDist = ShareData.distance;
            }

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

                leftMotor.rotate(-rightAngle, true);
                rightMotor.rotate(rightAngle);

                //BYPASS LOGIC
                if (leftAngle < rightAngle) {

                    // FRONT BYPASS LOOP (LEFT) 
                    boolean frontClear = false;
                    while (!frontClear) {

                        leftMotor.rotate(-leftAngle, true);
                        rightMotor.rotate(leftAngle);

                        leftMotor.forward();
                        rightMotor.forward();
                        Delay.msDelay(3000);

                        leftMotor.stop(true);
                        rightMotor.stop();

                        leftMotor.rotate(leftAngle, true);
                        rightMotor.rotate(-leftAngle);
                        Delay.msDelay(500);

                        synchronized (ShareData.class) {
                            if (ShareData.distance > 0.6) {
                                frontClear = true;
                            }
                        }
                    }

                    // SIDE PASSING LOOP (LEFT)
                    boolean sideClear = false;
                    while (!sideClear) {

                        leftMotor.forward();
                        rightMotor.forward();
                        Delay.msDelay(1000);

                        leftMotor.stop(true);
                        rightMotor.stop();

                        leftMotor.rotate(180, true);
                        rightMotor.rotate(-180);
                        Delay.msDelay(500);

                        synchronized (ShareData.class) {
                            if (ShareData.distance > 0.6) {
                                sideClear = true;
                            } else {
                                leftMotor.rotate(-180, true);
                                rightMotor.rotate(180);
                            }
                        }
                    }
                    
                    // Return to line 
                    leftMotor.rotate(-180, true); 
                    rightMotor.rotate(180);

                    while (true){
                        if (ShareData.light == targetValue) {
                        break;
                        }
                    }
                } else {

                    //FRONT BYPASS LOOP (RIGHT)
                    boolean frontClear = false;
                    while (!frontClear) {

                        leftMotor.rotate(rightAngle, true);
                        rightMotor.rotate(-rightAngle);

                        leftMotor.forward();
                        rightMotor.forward();
                        Delay.msDelay(3000);

                        leftMotor.stop(true);
                        rightMotor.stop();

                        leftMotor.rotate(-rightAngle, true);
                        rightMotor.rotate(rightAngle);
                        Delay.msDelay(500);

                        synchronized (ShareData.class) {
                            if (ShareData.distance > 0.6) {
                                frontClear = true;
                            }
                        }
                    }

                    //SIDE PASSING LOOP (RIGHT)
                    boolean sideClear = false;
                    while (!sideClear) {

                        leftMotor.forward();
                        rightMotor.forward();
                        Delay.msDelay(1000);

                        leftMotor.stop(true);
                        rightMotor.stop();

                        leftMotor.rotate(-180, true);
                        rightMotor.rotate(180);
                        Delay.msDelay(500);

                        synchronized (ShareData.class) {
                            if (ShareData.distance > 0.6) {
                                sideClear = true;
                            } else {
                                leftMotor.rotate(180, true);
                                rightMotor.rotate(-180);
                            }
                        }
                    }
                    
                    // Return to line 
                    leftMotor.rotate(180, true);
                    rightMotor.rotate(-180);

                    while (true){
                        if (ShareData.light == targetValue) {
                        break;
                        }
                    }
                }

            } else {

                //PID LINE FOLLOWING
                int currentLight;

                synchronized (ShareData.class) {
                    currentLight = ShareData.light;
                }

                double error = targetValue - currentLight;
                integral += error;
                double derivative = error - lastError;

                double turn = (Kp * error) + (Ki * integral) + (Kd * derivative);

                leftMotor.setSpeed((int)(baseSpeed + turn));
                rightMotor.setSpeed((int)(baseSpeed - turn));

                leftMotor.forward();
                rightMotor.forward();

                lastError = error;
            }

            try { Thread.sleep(10); } catch (Exception e) {}
        }

       
        ShareData.running = false;

        leftMotor.close();
        rightMotor.close();
        colorSensor.close();
        ultrasonicSensor.close();
    }
}
