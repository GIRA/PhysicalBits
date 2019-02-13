tests
test007Toggle500ms
	| program |
	program := Uzi
		program: [:p | p
			script: #blink
			ticking: true
			delay: 500
			instructions: [:s | s
				push: 11;
				prim: #toggle];
			script: #trigger
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 13;
				prim: #toggle]].
	self writeFile: 'Test007Toggle500ms'
		contents: (UziProtocol new run: program)