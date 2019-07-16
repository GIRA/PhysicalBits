tests
test036ResumeOnARunningTaskShouldHaveNoEffect
	| program |
	program := Uzi program: [:p | p
		script: #main
		ticking: true
		delay: 0
		instructions: [:s | s
			resume: #main;
			push: 13;
			prim: #turnOn;
			push: 100;
			prim: #delayMs;
			resume: #main;
			push: 13;
			prim: #turnOff;
			push: 100;
			prim: #delayMs];
		script: #awake
		ticking: true
		delay: 0
		instructions: [:s | s
			resume: #main]].
	self writeFile: 'Test036ResumeOnARunningTaskShouldHaveNoEffect'
		program: program