encoding instructions
visitStartScriptInstruction: instruction
	| index |
	index := (program scriptNames indexOf: instruction argument) - 1.
	stream nextPut: (16rD0 bitOr: index)