tests
test046ReverseForLoopWithoutConstantStep
	| program |
	program := Uzi compileString: '
	var step = -1;

	task for() running {
		for i = 11 to 7 by step {
			turnOn(i);
		}
	}'.
	self writeFile: 'Test046ReverseForLoopWithoutConstantStep'
		contents: (UziProtocol new run: program)