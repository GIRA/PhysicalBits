tests
test035StartOnAnotherTaskShouldResetToBeginning
	| program |
	program := Uzi program: [:p | p
		script: #main
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 13;
			prim: #turnOn;
			push: 100;
			prim: #yieldTime;
			push: 13;
			prim: #turnOff;
			push: 1000;
			prim: #yieldTime];
		script: #awake
		ticking: true
		delay: 0
		instructions: [:s | s
			start: #main]].
	self writeFile: 'Test035StartOnAnotherTaskShouldResetToBeginning'
		contents: (UziProtocol new run: program)