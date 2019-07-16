tests
test006Toggle2000ms
	| program |
	program := Uzi
		program: [:p | p
			script: #blink
			ticking: true
			delay: 2000
			instructions: [:s | s
				push: 11;
				prim: #toggle];
			script: #trigger
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 13;
				prim: #toggle]].
	self writeFile: 'Test006Toggle2000ms'
		program: program