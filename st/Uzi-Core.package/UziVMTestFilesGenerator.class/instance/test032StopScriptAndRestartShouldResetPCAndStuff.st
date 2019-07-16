tests
test032StopScriptAndRestartShouldResetPCAndStuff
	| program |
	program := Uzi program: [:p | p
		script: #blink13
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 13;
			prim: #toggle;
			push: 1000;
			prim: #delayMs;
			push: 11;
			prim: #toggle];
		script: #sleepAwake
		ticking: true
		delay: 0
		instructions: [:s | s
			stop: #blink13;
			push: 500;
			prim: #delayMs;
			start: #blink13;
			push: 500;
			prim: #delayMs]].
	self writeFile: 'Test032StopScriptAndRestartShouldResetPCAndStuff'
		program: program