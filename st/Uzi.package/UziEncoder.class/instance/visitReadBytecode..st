encoding bytecodes
visitReadBytecode: bytecode
	stream nextPut: (16r60 bitOr: bytecode argument)