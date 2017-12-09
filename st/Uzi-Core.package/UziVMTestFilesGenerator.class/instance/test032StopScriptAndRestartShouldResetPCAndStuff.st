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
			prim: #yieldTime;
			push: 11;
			prim: #toggle];
		script: #sleepAwake
		ticking: true
		delay: 0
		instructions: [:s | s
			stop: #blink13;
			push: 500;
			prim: #yieldTime;
			start: #blink13;
			push: 500;
			prim: #yieldTime]].
	self writeFile: 'Test032StopScriptAndRestartShouldResetPCAndStuff'
		contents: (UziProtocol new run: program)