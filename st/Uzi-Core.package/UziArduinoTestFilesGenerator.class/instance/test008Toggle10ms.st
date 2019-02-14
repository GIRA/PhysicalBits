tests
test008Toggle10ms
	| program |
	program := Uzi
		program: [:p | p
			script: #blink
			ticking: true
			delay: 10
			instructions: [:s | s
				push: 11;
				prim: #toggle];
			script: #trigger
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 13;
				prim: #toggle]].
	self writeFile: 'Test008Toggle10ms'
		contents: (UziProtocol new run: program)