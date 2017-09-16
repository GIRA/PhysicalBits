#include <L293.h>

int sensor = A1;
int buzzer = 6;
// Configure motors
L293 leftMotor(9, 8, 7);
L293 rightMotor(10, 11, 12);

int victims, lastVictimTime, state;

void setup() {
	// Configure pins
	pinMode(sensor, INPUT);
	pinMode(buzzer, OUTPUT);
}

void loop() {
	followLine();
	detectVictims();
}

void followLine() {
	switch (state) {
		case 0: 
			// Wait until black while moving right
			moveRight();
			if (isOnBlack()) state = 1;
			break;
		case 1:
			// Wait until white, still moving right
			moveRight();
			if (isOnWhite()) state = 2;
			break;
		case 2:
			// Wait until black while moving left
			moveLeft();
			if (isOnBlack()) state = 3;
			break;
		case 3:
			// Wait until white, still moving left
			moveLeft();
			if (isOnWhite()) state = 0;
			break;
	}
}

void detectVictims() {
	// Wait 100 ms before detecting next victim
	if (millis() - lastVictimTime < 100) return;
	if (isOnVictim()) {
		// Stop motors
		leftMotor.stop(); rightMotor.stop();
		// Increment victim counter
		victims++;
		// Play alarm
		for (int i = 0; i < victims; i++) {
			digitalWrite(buzzer, HIGH); delay(500);
			digitalWrite(buzzer, LOW); delay(500);
		}
		lastVictimTime = millis();
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
