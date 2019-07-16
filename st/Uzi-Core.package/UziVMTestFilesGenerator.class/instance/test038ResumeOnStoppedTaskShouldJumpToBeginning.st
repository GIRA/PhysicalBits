tests
test038ResumeOnStoppedTaskShouldJumpToBeginning
	| program |
	program := Uzi program: [:p | p
		script: #main
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 13;
			prim: #turnOn;
			stop: #main;
			push: 13;
			prim: #turnOff;
			stop: #main];
		script: #awake
		ticking: true
		delay: 0
		instructions: [:s | s
			resume: #main]].
	self writeFile: 'Test038ResumeOnStoppedTaskShouldJumpToBeginning'
		program: program