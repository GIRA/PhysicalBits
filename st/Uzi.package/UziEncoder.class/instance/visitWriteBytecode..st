encoding bytecodes
visitWriteBytecode: bytecode
	stream nextPut: (16r40 bitOr: bytecode argument)