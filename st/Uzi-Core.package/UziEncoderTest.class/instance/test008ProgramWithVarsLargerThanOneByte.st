tests
test008ProgramWithVarsLargerThanOneByte
	| program encoded vars |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 256;		
			write: 13]].
	encoded := UziEncoder new encode: program.
	vars := encoded copyFrom: 2 to: 5.
	self assert: #[1 5 1 0] equals: vars