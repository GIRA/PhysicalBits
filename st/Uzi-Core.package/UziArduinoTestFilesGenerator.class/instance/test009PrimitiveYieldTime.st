tests
test009PrimitiveYieldTime
	| program |
	program := Uzi
		program: [:p | p
			script: #blink
			ticking: true
			delay: 500
			instructions: [:s | s
				turnOn: 11;
				push: 1000;
				prim: #delayMs;
				turnOff: 11];
			script: #trigger
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 13;
				prim: #toggle]].
	self writeFile: 'Test009PrimitiveYieldTime'
		contents: (UziProtocol new run: program)