tests
test005PushWithFloatingPointVariable
	| program |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 0.2;		
			write: 13]].
	self writeFile: 'Test005PushWithFloatingPointVariable'
		contents: (UziProtocol new run: program)