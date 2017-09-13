tests
test003ProgramHeader2
	| program encoded |
	program := Uzi program: [:pb | Uzi
		exampleButton10Led9On: pb;
		examplePot15Led9On: pb;
		exampleBlinkLed9On: pb].
	encoded := UziEncoder new encode: program.
	self assert: encoded first equals: 3