visiting
visitPrimitiveCallBytecode: bytecode
	stream nextPut: ((bytecode code bitShift: 4) 
					bitOr: bytecode argument code)