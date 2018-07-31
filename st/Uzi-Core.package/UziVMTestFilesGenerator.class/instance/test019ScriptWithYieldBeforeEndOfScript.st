tests
test019ScriptWithYieldBeforeEndOfScript
	| program |
	program := Uzi program: [:p | p	
		script: #blink11
		ticking: false
		delay: 0
		instructions: [:s | s
			push: 11;
			prim: #toggle];
		script: #main
		ticking: true
		delay: 0
		instructions: [:s | s
			call: #blink11;
			prim: #pop;
			push: 100;
			prim: #delayMs]].
	self writeFile: 'Test019ScriptWithYieldBeforeEndOfScript'
		contents: (UziProtocol new run: program)