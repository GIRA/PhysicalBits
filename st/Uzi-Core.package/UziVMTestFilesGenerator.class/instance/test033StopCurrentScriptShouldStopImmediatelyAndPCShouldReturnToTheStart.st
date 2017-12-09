tests
test033StopCurrentScriptShouldStopImmediatelyAndPCShouldReturnToTheStart
	| program |
	program := Uzi program: [:p | p
		script: #blink13
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 13;
			prim: #toggle;
			stop: #blink13;
			push: 11;
			prim: #toggle];
		script: #awake
		ticking: true
		delay: 1000
		instructions: [:s | s
			start: #blink13]].
	self writeFile: 'Test033StopCurrentScriptShouldStopImmediatelyAndPCShouldReturnToTheStart'
		contents: (UziProtocol new run: program)