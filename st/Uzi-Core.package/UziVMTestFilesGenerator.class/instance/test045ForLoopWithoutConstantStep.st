tests
test045ForLoopWithoutConstantStep
	| program |
	program := '
	var step = 1;

	task for() running {
		for i = 7 to 11 by step {
			turnOn(i);
		}
	}'.
	self writeFile: 'Test045ForLoopWithoutConstantStep'
		program: program