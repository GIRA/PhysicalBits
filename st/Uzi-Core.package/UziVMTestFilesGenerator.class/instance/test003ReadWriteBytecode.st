tests
test003ReadWriteBytecode
	| program |
	program := Uzi 
		program: [:p | p
			script: #test
			ticking: true
			delay: 0
			instructions: [:s | s
				read: 15;
				write: 13]].
	self writeFile: 'Test003ReadWriteBytecode'
		contents: (UziProtocol new run: program)