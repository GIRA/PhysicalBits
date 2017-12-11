tests
test040StartOnPausedTaskShouldJumpToBeginning
	| program |
	program := Uzi program: [:p | p
		script: #main
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 13;
			prim: #turnOn;
			pause: #main;
			push: 13;
			prim: #turnOff;
			pause: #main];
		script: #awake
		ticking: true
		delay: 0
		instructions: [:s | s
			start: #main]].
	self writeFile: 'Test040StartOnPausedTaskShouldJumpToBeginning'
		contents: (UziProtocol new run: program)