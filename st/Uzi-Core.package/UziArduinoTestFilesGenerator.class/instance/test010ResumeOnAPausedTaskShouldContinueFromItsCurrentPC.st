tests
test010ResumeOnAPausedTaskShouldContinueFromItsCurrentPC
	| program |
	program := Uzi
		program: [:p | p
			script: #main
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 11;
				prim: #turnOn;
				pause: #main;
				push: 11;
				prim: #turnOff;
				pause: #main];
			script: #awake
			ticking: true
			delay: 100
			instructions: [:s | s
				resume: #main];
			script: #trigger
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 13;
				prim: #toggle]].
	self writeFile: 'Test010ResumeOnAPausedTaskShouldContinueFromItsCurrentPC'
		program: program