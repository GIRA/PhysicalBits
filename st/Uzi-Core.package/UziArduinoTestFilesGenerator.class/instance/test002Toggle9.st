tests
test002Toggle9
	| program |
	program := Uzi
		program: [:p | p
			script: #blink9
			ticking: true
			delay: 100
			instructions: [:s | s
				push: 9;
				prim: #toggle];
			script: #trigger
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 13;
				prim: #toggle]].
	self writeFile: 'Test002Toggle9'
		contents: (UziProtocol new run: program)