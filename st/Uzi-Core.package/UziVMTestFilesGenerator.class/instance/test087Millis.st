tests
test087Millis
	| program |	
	program := Uzi compileString: '
	task main() running {
		until millis() >= 1000;
		turnOn(D13);
		until millis() >= 2000;
		turnOff(D13);
		until millis() >= 3000;
		turnOn(D13);
		until millis() >= 4000;
		turnOff(D13);
	}'.
	self writeFile: thisContext method selector capitalized
		program: program