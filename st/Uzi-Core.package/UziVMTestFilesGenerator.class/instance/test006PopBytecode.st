tests
test006PopBytecode
	| program |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true 
		delay: 0
		instructions: [:s | s
			push: #a;
			write: 13;
			push: 1;
			pop: #a]].
	self writeFile: 'Test006PopBytecode'
		contents: (UziProtocol new run: program)