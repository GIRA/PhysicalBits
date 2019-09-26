tests
test070IsOn
	| program |	
	program := '
	task main() running 1/s {
		if isOn(D13) {
			turnOff(D13);
		} else {
			turnOn(D13);
		}
	}'.
	self writeFile: thisContext method selector
		program: program