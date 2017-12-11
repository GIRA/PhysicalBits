tests
test037ResumeOnAPausedTaskShouldContinueFromItsCurrentPC
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
		delay: 1000
		instructions: [:s | s
			resume: #main]].
	self writeFile: 'Test037ResumeOnAPausedTaskShouldContinueFromItsCurrentPC'
		contents: (UziProtocol new run: program)