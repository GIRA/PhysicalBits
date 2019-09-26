tests
test086Seconds
	| program |	
	program := '
	task main() running {
		until seconds() >= 1;
		turnOn(D13);
		until seconds() >= 2;
		turnOff(D13);
		until seconds() >= 3;
		turnOn(D13);
		until seconds() >= 4;
		turnOff(D13);
	}'.
	self writeFile: thisContext method selector
		program: program