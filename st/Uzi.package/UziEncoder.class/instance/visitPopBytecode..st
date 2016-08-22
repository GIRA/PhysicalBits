encoding bytecodes
visitPopBytecode: bytecode
	| arg |
	arg := (variables indexOf: bytecode argument) - 1.
	stream nextPut: (16r90 bitOr: arg)
	