tests
test093DebuggerSetAllBreakpointsWithMultipleScripts
	| program |	
	program := Uzi program: [:p | p
		script: #test1
		ticking: true
		delay: 0
		instructions: [:s | s
			turnOn: 13;
			turnOn: 11];
		script: #test2
		ticking: true
		delay: 0
		instructions: [:s | s
			turnOff: 11;
			turnOff: 13]].
	self writeFile: thisContext method selector
		program: program