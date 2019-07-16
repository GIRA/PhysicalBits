tests
test011ForLoopShouldOnlyEvaluateStepOncePerIteration
	| program |
	program := Uzi compileString: '
	var step = -1;
	var stop = -10;

	func negatedStep() {
		step = step * -1;
		return step;
	}

	func negatedStop() {
		stop = stop * -1;
		return stop;
	}

	task for() running {
		for i = 1 to negatedStop() by negatedStep() {
			write(D11, i);
		}
	}
	task trigger() running{
		toggle(13);
	}'.
	self writeFile: 'Test011ForLoopShouldOnlyEvaluateStepOncePerIteration'
		program: program