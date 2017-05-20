encoding instructions
visitPopInstruction: instruction
	| arg |
	arg := (globals indexOf: instruction argument) - 1.
	stream nextPut: (16r90 bitOr: arg)
	