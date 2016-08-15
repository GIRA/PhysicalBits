encoding bytecodes
visitPrimitiveCallBytecode: bytecode
	| arg |
	arg := bytecode argument code.
	stream nextPut: (16rA0 bitOr: arg)