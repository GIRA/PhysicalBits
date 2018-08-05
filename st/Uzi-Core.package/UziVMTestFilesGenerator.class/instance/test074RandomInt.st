tests
test074RandomInt
	| program |	
	program := Uzi compileString: '
	var old = 0;
	task main() running 1/s {
		var a = randomInt(-100000, 100000);
		if (a != old && a >= -100000 && a <= 100000) {
			turnOn(D13);
		} else {
			turnOff(D13);
		}
		old = a;
	}'.
	self writeFile: thisContext method selector capitalized
		contents: (UziProtocol new run: program)