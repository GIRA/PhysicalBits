tests
test005Toggle12
	| program |
	program := Uzi
		program: [:p | p
			script: #blink12
			ticking: true
			delay: 1000
			instructions: [:s | s
				push: 12;
				prim: #toggle];
			script: #trigger
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 13;
				prim: #toggle]].
	self writeFile: 'Test005Toggle12'
		contents: (UziProtocol new run: program)