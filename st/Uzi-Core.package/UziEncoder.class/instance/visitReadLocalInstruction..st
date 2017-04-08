encoding instructions
visitReadLocalInstruction: instruction
	| index |
	index := script indexOfLocal: instruction argument.
	stream
		nextPut: 16rFF;
		nextPut: index
