# APPLIED APPLICATION PROJECT 

-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 1. Group Members and Roles

- **Nevindi Rathukumarage (Group Leader)**  
  Worked on the ultrasonic sensor implementation together with Roshan Kumar Sar. Also coordinated the overall structure of the project.

- **Bashar Abdul Mumin**  
  Worked on the light sensor (color sensor) implementation together with Oluchukwu Onyido. Also responsible for integrating the system, debugging, and managing version control using GitHub.

- **Roshan Kumar Sar**  
  Contributed to the development and testing of the ultrasonic sensor and obstacle avoidance logic.

- **Oluchukwu Onyido**  
  Contributed to the development and testing of the light sensor and line-following logic.

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 2. Project Overview

This project implements a LEGO EV3 robot that is able to:

- Follow a line using a color (light) sensor  
- Detect obstacles using an ultrasonic sensor  
- Decide a direction (left or right) based on available space  
- Avoid the obstacle and return to the line  
- Continue line following after avoidance  

The robot uses **multi-threading** to ensure that sensor data is updated continuously while the robot is moving.

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 3. Project Structure

The repository contains the following files:

- `FinalWork.java`  
  → Main integrated program combining line following and obstacle avoidance  

- `LightSensor.java`  
  → Standalone testing of the light sensor and PID line-following logic  

- `UltrasonicSensor.java`  
  → Standalone testing of the ultrasonic sensor and obstacle detection logic  

- Flowchart  
  → Used to design and guide the program logic before implementation  

- `javadoc/` folder  
  → Generated JavaDoc documentation for the project source code  

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 4. Threads and Their Purpose

The program uses two threads to continuously read sensor data:

### LightRunnable

This thread continuously reads light intensity values from the color sensor.  
It updates the shared variable:

`ShareData.light`

This value is used in the main program for:

- PID line following  
- Detecting when the robot has returned to the line  

### DistanceRunnable

This thread continuously reads distance values from the ultrasonic sensor.  
It updates the shared variable:

`ShareData.distance`

This value is used in the main program for:

- Detecting obstacles  
- Scanning left and right  
- Decision making during obstacle avoidance  

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 5. Communication Between Threads

- Both threads share data using the `ShareData` class  
- Synchronization (`synchronized`) is used when reading and writing shared variables  
- This prevents conflicts between threads and ensures stable sensor readings  

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 6. Main Logic Explanation

The robot operates in two main modes:

### 6.1 Line Following (PID Control)

- The robot follows the edge of the line using PID control  
- The light sensor value is compared to a target value  
- The error is calculated and used to adjust motor speeds  
- Motor speeds are continuously updated to keep the robot on the line  

PID uses:

- **Proportional (Kp)** → reacts to current error  
- **Integral (Ki)** → corrects accumulated error (set to 0 for stability)  
- **Derivative (Kd)** → smooths movement and reduces oscillation  

### 6.2 Obstacle Detection and Avoidance

When an object is detected:

1. The robot stops  
2. It scans left using the ultrasonic sensor  
3. It returns to center  
4. It scans right  
5. It compares which side clears the obstacle faster  
6. It selects the better direction  
7. It performs a bypass using timed forward and turning movements  
8. It turns back toward the track  
9. It moves forward until the line is detected  
10. It resumes line following  

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 7. Development Approach

- Individual components were first developed and tested separately:
  - Light sensor (line following)  
  - Ultrasonic sensor (obstacle detection)  

- Separate test files were created:
  - `LightSensor.java`  
  - `UltrasonicSensor.java`  

- A flowchart was created to plan the logic before coding  

- After testing, the components were integrated into a single program (`FinalWork.java`)  

- Debugging and tuning were performed during testing  

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 8. Additional Notes

- Threads were used as required in the course guidelines  
- Communication between threads was implemented using shared variables  
- Synchronization ensured safe data access between threads  
- PID control was used for smoother line following  
- Timing-based movement was used for obstacle avoidance  

The system demonstrates integration of:

- Sensors  
- Motors  
- Multi-threading  
- Decision-making logic  

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 9. Repository Link

https://github.com/ambashar10-star/Applied_Application_Project_GP3
