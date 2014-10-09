visiting
visitPushLiteralBytecode: bytecode
	self writeBytecode: 0 
		argument: (literals indexOf: bytecode argument) - 1	