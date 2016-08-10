as yet unclassified
testProgramHeader
	| program encoded |
	program := Uzi program: [:pb | Uzi 
		exampleButton10Led9On: pb;
		examplePot15Led9On: pb].
	encoded := UziEncoder new encode: program.
	self assert: encoded first equals: 2