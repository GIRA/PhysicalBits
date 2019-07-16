tests
test007PrimBytecode
	| program |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true 
		delay: 0
		instructions: [:s | s
			push: 13;
			prim: #toggle]].
	self writeFile: 'Test007PrimBytecode'
		program: program