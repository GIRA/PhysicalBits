encoding instructions
visitPrimitiveCallInstruction: instruction
	| arg |
	arg := instruction argument code.
	stream nextPut: (16rA0 bitOr: arg)