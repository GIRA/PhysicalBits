tests
test034StartOnTheCurrentTaskShouldJumpToTheBeginning
	| program |
	program :=  Uzi program: [:p | p
		script: #main
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 13;
			prim: #turnOn;
			start: #main;
			push: 13;
			prim: #turnOff]].
	self writeFile: 'Test034StartOnTheCurrentTaskShouldJumpToTheBeginning'
		contents: (UziProtocol new run: program)