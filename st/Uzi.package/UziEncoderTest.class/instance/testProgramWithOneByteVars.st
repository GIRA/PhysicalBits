as yet unclassified
testProgramWithOneByteVars
	| program encoded vars |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true
		delay: 0
		bytecodes: [:s | s
			push: 255;
			push: 1]].
	encoded := UziEncoder new encode: program.
	vars := encoded copyFrom: 2 to: 5.
	self assert: #[2 8 1 255] equals: vars