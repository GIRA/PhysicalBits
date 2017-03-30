encoding instructions
visitScriptCallInstruction: instruction
	| index |
	index := (program scriptNames indexOf: instruction argument) - 1.
	stream nextPut: (16rC0 bitOr: index)