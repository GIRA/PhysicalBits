tests
test075Random
	| program |	
	program := Uzi compileString: '
	var old = 0;
	task main() running 1/s {
		var a = random();
		if (a != old && a >= 0 && a <= 1) {
			turnOn(D13);
		} else {
			turnOff(D13);
		}
		old = a;
	}'.
	self writeFile: thisContext method selector capitalized
		program: program