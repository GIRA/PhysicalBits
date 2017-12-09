tests
test001TurnOnBytecode
	| program |
	program := Uzi
		program: [:p | p
			script: #test
			ticking: true
			delay: 0
			instructions: [:s | s
				turnOn: 13]].
	self writeFile: 'Test001TurnOnBytecode'
		contents: (UziProtocol new run: program)