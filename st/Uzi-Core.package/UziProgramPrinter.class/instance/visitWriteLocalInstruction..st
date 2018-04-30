encoding instructions
visitWriteLocalInstruction: instruction
	self indent;
		saveInterval: [self write: 'pop: {1}' format: { instruction argument name asSymbol storeString }]