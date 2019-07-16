tests
test085Minutes
	| program |	
	program := Uzi compileString: '
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
	self writeFile: thisContext method selector capitalized
		program: program