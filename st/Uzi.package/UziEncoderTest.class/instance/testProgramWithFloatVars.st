as yet unclassified
testProgramWithFloatVars
	| program encoded |
	program := Uzi program: [:p | p
		script: #blink
		ticking: true
		delay: 0
		bytecodes: [:s | s
			push: 0.5;		
			write: 13]].
	encoded := UziEncoder new encode: program.
	self fail