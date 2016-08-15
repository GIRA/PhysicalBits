encoding bytecodes
visitTurnOffBytecode: bytecode
	stream nextPut: (16r20 bitOr: bytecode argument)