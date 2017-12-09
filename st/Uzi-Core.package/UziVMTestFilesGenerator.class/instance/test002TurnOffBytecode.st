tests
test002TurnOffBytecode
	| program |
	program := Uzi 
		program: [:p | p
			script: #test
			ticking: true
			delay: 0
			instructions: [:s | s
				turnOff: 13]].
	self writeFile: 'Test002TurnOffBytecode'
		contents: (UziProtocol new run: program)