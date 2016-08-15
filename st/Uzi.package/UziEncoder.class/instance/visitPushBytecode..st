encoding bytecodes
visitPushBytecode: bytecode
	| arg |
	arg := (variables indexOf: bytecode argument) - 1.
	stream nextPut: (16r80 bitOr: arg)