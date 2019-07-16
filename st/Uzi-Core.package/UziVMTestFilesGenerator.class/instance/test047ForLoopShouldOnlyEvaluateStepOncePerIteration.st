tests
test047ForLoopShouldOnlyEvaluateStepOncePerIteration
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
		for i = 0 to negatedStop() by negatedStep() {
			write(D13, i);
		}
	}'.
	self writeFile: 'Test047ForLoopShouldOnlyEvaluateStepOncePerIteration'
		program: program