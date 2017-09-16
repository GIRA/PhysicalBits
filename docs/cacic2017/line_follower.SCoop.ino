#include <SCoop.h>
#include <L293.h>

int sensor = A1;
int buzzer = 6;
// Configure motors
L293 leftMotor(9, 8, 7);
L293 rightMotor(10, 11, 12);

int victims;

void setup() {  
  // Configure pins
  pinMode(sensor, INPUT);
  pinMode(buzzer, OUTPUT);
  mySCoop.start(); 
}

void loop() { yield(); }

defineTaskLoop(followLine) {
  // Wait until black while moving right
  while (!isOnBlack()) { moveRight(); yield(); }
  // Wait until white, still moving right
  while (!isOnWhite()) { moveRight(); yield(); }
  // Wait until black while moving left
  while (!isOnBlack()) { moveLeft(); yield(); }
  // Wait until white, still moving left
  while (!isOnWhite()) { moveLeft(); yield(); } 
}

defineTaskLoop(detectVictims) {  
  if (isOnVictim()) {
    // Stop following the line
    followLine.pause();
    leftMotor.stop(); rightMotor.stop();
    // Increment victim counter
    victims++;
    // Play alarm
    for (int i = 0; i < victims; i++) {
      digitalWrite(buzzer, HIGH); sleep(500);
      digitalWrite(buzzer, LOW); sleep(500);
    }
    // Resume following the line
    followLine.resume();
    // Wait 100 ms before detecting next victim
    sleep(100);
  }
}

bool isOnBlack() { return analogRead(sensor) < 200; }
bool isOnWhite() { return analogRead(sensor) > 500; }
bool isOnVictim() { return !isOnBlack() && !isOnWhite(); }

void moveRight() {
  leftMotor.forward(255);
  rightMotor.forward(127);
}

void moveLeft() {
  leftMotor.forward(127);
  rightMotor.forward(255);
}
