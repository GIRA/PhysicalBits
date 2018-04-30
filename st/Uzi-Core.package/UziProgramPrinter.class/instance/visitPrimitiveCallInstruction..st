encoding instructions
visitPrimitiveCallInstruction: instruction
	self indent;
		saveInterval: [self write: 'prim: {1}' format: { instruction argument name asSymbol storeString }]