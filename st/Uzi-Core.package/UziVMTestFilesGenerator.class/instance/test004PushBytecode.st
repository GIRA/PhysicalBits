tests
test004PushBytecode
	| program |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 1;		
			write: 13]].
	self writeFile: 'Test004PushBytecode'
		program: program