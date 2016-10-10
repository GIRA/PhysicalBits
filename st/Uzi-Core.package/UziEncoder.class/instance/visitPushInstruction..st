encoding instructions
visitPushInstruction: instruction
	| arg |
	arg := (variables indexOf: instruction argument) - 1.
	stream nextPut: (16r80 bitOr: arg)