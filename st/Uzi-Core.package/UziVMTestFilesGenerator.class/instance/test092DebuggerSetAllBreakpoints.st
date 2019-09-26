tests
test092DebuggerSetAllBreakpoints
	| program |	
	program := Uzi program: [:p | p
	script: #test
	ticking: true
	delay: 0
	instructions: [:s | s
		turnOn: 13;
		turnOn: 11;
		turnOff: 11;
		turnOff: 13]].
	self writeFile: thisContext method selector
		program: program