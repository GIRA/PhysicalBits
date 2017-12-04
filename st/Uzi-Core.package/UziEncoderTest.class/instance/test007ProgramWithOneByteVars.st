tests
test007ProgramWithOneByteVars
	| program encoded vars |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true
		delay: 0
		instructions: [:s | s
			push: 255;
			push: 1]].
	encoded := UziEncoder new encode: program.
	vars := encoded copyFrom: 2 to: 6.
	self assert: #[3 12 0 1 255] equals: vars