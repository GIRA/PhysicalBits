tests
test009PrimitiveYieldTime
	| program |
	program := Uzi
		program: [:p | p
			script: #blink
			ticking: true
			delay: 100
			instructions: [:s | s
				turnOn: 11;
				push: 500;
				prim: #delayMs;
				turnOff: 11];
			script: #trigger
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 13;
				prim: #toggle]].
	self writeFile: 'Test009PrimitiveYieldTime'
		program: program