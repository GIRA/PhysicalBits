tests
test085Minutes
	| program |	
	program := '
	task main() running {
		until minutes() >= 1;
		turnOn(D13);
		until minutes() >= 2;
		turnOff(D13);
		until minutes() >= 3;
		turnOn(D13);
		until minutes() >= 4;
		turnOff(D13);
	}'.
	self writeFile: thisContext method selector
		program: program