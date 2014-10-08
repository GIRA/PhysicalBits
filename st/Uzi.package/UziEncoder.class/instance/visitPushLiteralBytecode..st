visiting
visitPushLiteralBytecode: bytecode
	stream nextPut: ((bytecode code bitShift: 4)
				bitOr: (literals indexOf: bytecode argument) - 1)
		