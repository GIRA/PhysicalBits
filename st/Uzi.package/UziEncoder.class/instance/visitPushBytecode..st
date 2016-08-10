visiting
visitPushBytecode: bytecode
	self writeBytecode: 2
		argument: (variables indexOf: bytecode argument) - 1	