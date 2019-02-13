tests
test004Toggle11
	| program |
	program := Uzi
		program: [:p | p
			script: #blink11
			ticking: true
			delay: 1000
			instructions: [:s | s
				push: 11;
				prim: #toggle];
			script: #trigger
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 13;
				prim: #toggle]].
	self writeFile: 'Test004Toggle11'
		contents: (UziProtocol new run: program)