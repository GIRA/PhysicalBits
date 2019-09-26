tests
test071IsOff
	| program |	
	program := '
	task main() running 1/s {
		if isOff(D13) {
			turnOn(D13);
		} else {
			turnOff(D13);
		}
	}'.
	self writeFile: thisContext method selector
		program: program