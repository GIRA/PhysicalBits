tests
test094ProgramWithMultipleImports
	| program |	
	program := '
import sonar from ''Sonar.uzi'';
import leftMotor from ''DCMotor.uzi'';
import rightMotor from ''DCMotor.uzi'';

task setup() {
	sonar.init(trig: A5, echo: A4, maxDist: 200);
	start sonar.reading;
	leftMotor.init(en: D5, f: D7, r: D8);
	rightMotor.init(en: D6, f: D11, r: D9);
}

task move() running 100/s {
	if (sonar.distance_cm() == 0) {
		leftMotor.forward(speed: 1);
		rightMotor.forward(speed: 1);
	} else {
		if (sonar.distance_cm() > 100) {
			leftMotor.forward(speed: 1);
			rightMotor.forward(speed: 1);
		} else {
			if (sonar.distance_cm() > 45) {
				leftMotor.forward(speed: 0.5);
				rightMotor.forward(speed: 0.5);
			} else {
				if (random() < 0.5) {
					while (sonar.distance_cm() < 45) {
						leftMotor.forward(speed: 0.5);
						rightMotor.backward(speed: 0.5);
						delayMs(1000);
					}
				} else {
					while (sonar.distance_cm() < 45) {
						rightMotor.forward(speed: 0.5);
						leftMotor.backward(speed: 0.5);
						delayMs(1000);
					}
				}
			}
		}
	}
}

task blink13() running 1/s {
	toggle(D13);
}'.
	self writeFile: thisContext method selector
		program: program