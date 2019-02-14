tests
test003Toggle10
	| program |
	program := Uzi
		program: [:p | p
			script: #blink10
			ticking: true
			delay: 100
			instructions: [:s | s
				push: 10;
				prim: #toggle];
			script: #trigger
			ticking: true
			delay: 0
			instructions: [:s | s
				push: 13;
				prim: #toggle]].
	self writeFile: 'Test003Toggle10'
		contents: (UziProtocol new run: program)